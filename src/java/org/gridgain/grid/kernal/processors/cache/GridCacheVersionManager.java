// Copyright (C) GridGain Systems, Inc. Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.processors.cache;

import org.gridgain.grid.*;
import org.gridgain.grid.events.*;
import org.gridgain.grid.lang.utils.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import static org.gridgain.grid.GridEventType.*;

/**
 * Makes sure that cache lock order values come in proper sequence.
 * <p>
 * NOTE: this class should not make use of any cache specific structures,
 * like, for example GridCacheContext, as it may be reused between different
 * caches.
 *
 * @author 2005-2011 Copyright (C) GridGain Systems, Inc.
 * @version 3.5.0c.06102011
 */
public class GridCacheVersionManager<K, V> extends GridCacheManager<K, V> {
    /**
     * Current order. Initialize to current time to make sure that
     * local version increments even after restarts.
     * <p>
     * Must be static, so different caches could get the latest version
     * from each other (required for Near and DHT contract).
     */
    private final AtomicLong order = new AtomicLong(System.currentTimeMillis());

    /** Last version. */
    private final AtomicReference<GridCacheVersion> last = new AtomicReference<GridCacheVersion>();

    /** Serializable transaction flag. */
    private boolean txSerEnabled;

    /** */
    private final GridLocalEventListener discoLsnr = new GridLocalEventListener() {
        @Override public void onEvent(GridEvent evt) {
            assert evt.type() == EVT_NODE_METRICS_UPDATED;

            GridDiscoveryEvent discoEvt = (GridDiscoveryEvent)evt;

            GridNode node = cctx.discovery().node(discoEvt.nodeId());

            if (node != null && !node.id().equals(cctx.nodeId()))
                onReceived(discoEvt.eventNodeId(), node.metrics().getLastDataVersion());
        }
    };

    /**
     * @return Pre-generated UUID.
     */
    private GridUuid uuid() {
        return GridUuid.randomUuid();
    }

    /** {@inheritDoc} */
    @Override public void start0() throws GridException {
        txSerEnabled = cctx.config().isTxSerializableEnabled();

        last.set(new GridCacheVersion(order.get(), uuid()));

        cctx.gridEvents().addLocalEventListener(discoLsnr, EVT_NODE_METRICS_UPDATED);
    }

    /** {@inheritDoc} */
    @Override protected void onKernalStart0() throws GridException {
        for (GridNode n : cctx.discovery().remoteNodes())
            onReceived(n.id(), n.metrics().getLastDataVersion());
    }

    /** {@inheritDoc} */
    @Override protected void stop0(boolean cancel, boolean wait) {
        cctx.gridEvents().removeLocalEventListener(discoLsnr, EVT_NODE_METRICS_UPDATED);
    }

    /**
     * @param nodeId Node ID.
     * @param ver Remote version.
     */
    public void onReceived(UUID nodeId, GridCacheVersion ver) {
        onReceived(nodeId, ver.order());
    }

    /**
     * @param nodeId Node ID.
     * @param ver Remote version.
     */
    public void onReceived(UUID nodeId, long ver) {
        if (ver > 0)
            while (true) {
                long order = this.order.get();

                // If another version is larger, we update.
                if (ver > order) {
                    if (!this.order.compareAndSet(order, ver))
                        // Try again.
                        continue;
                    else if (log.isDebugEnabled())
                        log.debug("Updated version from node [nodeId=" + nodeId + ", ver=" + ver + ']');
                }
                else if (log.isDebugEnabled()) {
                    log.debug("Did not update version from node (version has lower order) [nodeId=" + nodeId +
                        ", ver=" + ver + ", curOrder=" + this.order + ']');
                }

                break;
            }
    }

    /**
     * @param nodeId Node ID.
     * @param ver Received version.
     * @return Next version.
     */
    public GridCacheVersion onReceivedAndNext(UUID nodeId, GridCacheVersion ver) {
        onReceived(nodeId, ver);

        return next();
    }

    /**
     * The version is generated by taking last order plus one and random {@link UUID}.
     * Such algorithm ensures that lock IDs constantly grow in value and older
     * lock IDs are smaller than new ones. Therefore, older lock IDs appear
     * in the pending set before newer ones, hence preventing starvation.
     *
     * @return New lock order.
     */
    public GridCacheVersion next() {
        GridUuid id = uuid();

        if (txSerEnabled) {
            synchronized (last) {
                GridCacheVersion next = new GridCacheVersion(order.incrementAndGet(), id);

                last.set(next);

                return next;
            }
        }
        else {
            GridCacheVersion next = new GridCacheVersion(order.incrementAndGet(), id);

            last.set(next);

            return next;
        }
    }

    /**
     * Gets last generated version without generating a new one.
     *
     * @return Last generated version.
     */
    public GridCacheVersion last() {
        if (txSerEnabled) {
            synchronized (last) {
                return last.get();
            }
        }
        else
            return last.get();
    }
}
