// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.cache.eviction.fifo;

import org.gridgain.grid.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.cache.eviction.*;
import org.gridgain.grid.lang.utils.*;
import org.gridgain.grid.lang.utils.GridConcurrentLinkedDeque.*;
import org.gridgain.grid.typedef.internal.*;

import java.util.*;

import static org.gridgain.grid.cache.GridCachePeekMode.*;

/**
 * Eviction policy based on {@code First In First Out (FIFO)} algorithm. This
 * implementation is very efficient since it does not create any additional
 * table-like data structures. The {@code FIFO} ordering information is
 * maintained by attaching ordering metadata to cache entries.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.1c.07042012
 */
public class GridCacheFifoEvictionPolicy<K, V> implements GridCacheEvictionPolicy<K, V>,
    GridCacheFifoEvictionPolicyMBean {
    /** Tag. */
    private final String meta = UUID.randomUUID().toString();

    /** Maximum size. */
    private volatile int max = -1;

    /** Flag indicating whether empty entries are allowed. */
    private volatile boolean allowEmptyEntries = true;

    /** FIFO queue. */
    private final GridConcurrentLinkedDeque<GridCacheEntry<K, V>> queue =
        new GridConcurrentLinkedDeque<GridCacheEntry<K, V>>();

    /**
     * Constructs FIFO eviction policy with all defaults.
     */
    public GridCacheFifoEvictionPolicy() {
        // No-op.
    }

    /**
     * Constructs FIFO eviction policy with maximum size. Empty entries are allowed.
     *
     * @param max Maximum allowed size of cache before entry will start getting evicted.
     */
    public GridCacheFifoEvictionPolicy(int max) {
        A.ensure(max > 0, "max > 0");

        this.max = max;
    }

    /**
     * Constructs FIFO eviction policy with maximum size and allow empty entries flag specified.
     *
     * @param max Maximum allowed size of cache before entry will start getting evicted.
     * @param allowEmptyEntries If false, {@code false} empty entries will be evicted immediately.
     */
    public GridCacheFifoEvictionPolicy(int max, boolean allowEmptyEntries) {
        this(max);

        this.allowEmptyEntries = allowEmptyEntries;
    }

    /**
     * Gets maximum allowed size of cache before entry will start getting evicted.
     *
     * @return Maximum allowed size of cache before entry will start getting evicted.
     */
    @Override public int getMaxSize() {
        return max;
    }

    /**
     * Sets maximum allowed size of cache before entry will start getting evicted.
     *
     * @param max Maximum allowed size of cache before entry will start getting evicted.
     */
    @Override public void setMaxSize(int max) {
        A.ensure(max > 0, "max > 0");

        this.max = max;
    }

    /** {@inheritDoc} */
    @Override public boolean isAllowEmptyEntries() {
        return allowEmptyEntries;
    }

    /** {@inheritDoc} */
    @Override public void setAllowEmptyEntries(boolean allowEmptyEntries) {
        this.allowEmptyEntries = allowEmptyEntries;
    }

    /** {@inheritDoc} */
    @Override public int getCurrentSize() {
        return queue.size();
    }

    /** {@inheritDoc} */
    @Override public String getMetaAttributeName() {
        return meta;
    }

    /**
     * Gets read-only view on internal {@code FIFO} queue in proper order.
     *
     * @return Read-only view ono internal {@code 'FIFO'} queue.
     */
    public Collection<GridCacheEntry<K, V>> queue() {
        return Collections.unmodifiableCollection(queue);
    }

    /** {@inheritDoc} */
    @Override public void onEntryAccessed(boolean rmv, GridCacheEntry<K, V> entry) {
        if (!rmv) {
            if (!entry.isCached())
                return;

            boolean shrink = false;

            if (!allowEmptyEntries && empty(entry)) {
                Node<GridCacheEntry<K, V>> node = entry.meta(meta);

                if (node != null)
                    queue.unlinkx(node);

                if (!entry.evict()) {
                    if (node != null)
                        entry.removeMeta(meta, node);

                    shrink = touch(entry);
                }
                else {
                    // Entry was evicted, we must remove it from queue if concurrent touch() call added it.
                    node = entry.meta(meta);

                    if (node != null)
                        queue.unlinkx(node);
                }
            }
            else
                shrink = touch(entry);

            // Shrink only if queue was changed.
            if (shrink)
                shrink();
        }
        else {
            Node<GridCacheEntry<K, V>> node = entry.removeMeta(meta);

            if (node != null)
                queue.unlinkx(node);
        }
    }

    /**
     * @param entry Entry to touch.
     * @return {@code True} if queue has been changed by this call.
     */
    private boolean touch(GridCacheEntry<K, V> entry) {
        Node<GridCacheEntry<K, V>> node = entry.meta(meta);

        // Entry has not been enqueued yet.
        if (node == null) {
            while (true) {
                node = queue.offerLastx(entry);

                if (entry.putMetaIfAbsent(meta, node) != null) {
                    // Was concurrently added, need to clear it from queue.
                    queue.unlinkx(node);

                    // Queue has not been changed.
                    return false;
                }
                else if (node.item() != null) {
                    if (!entry.isCached()) {
                        // Was concurrently evicted, need to clear it from queue.
                        queue.unlinkx(node);

                        return false;
                    }

                    return true;
                }
                // If node was unlinked by concurrent shrink() call, we must repeat the whole cycle.
                else if (!entry.removeMeta(meta, node))
                    return false;
            }
        }

        // Entry is already in queue.
        return false;
    }

    /**
     * Shrinks FIFO queue to maximum allowed size.
     */
    private void shrink() {
        int max = this.max;

        int startSize = queue.sizex();

        for (int i = 0; i < startSize && queue.sizex() > max; i++) {
            GridCacheEntry<K, V> entry = queue.poll();

            if (entry == null)
                break;

            if (!entry.evict()) {
                entry.removeMeta(meta);

                touch(entry);
            }
        }
    }

    /**
     * Checks entry for empty value.
     *
     * @param entry Entry to check.
     * @return {@code True} if entry is empty.
     */
    private boolean empty(GridCacheEntry<K, V> entry) {
        try {
            return !entry.hasValue(GLOBAL);
        }
        catch (GridException e) {
            U.error(null, e.getMessage(), e);

            assert false : "Should never happen: " + e;

            return false;
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridCacheFifoEvictionPolicy.class, this);
    }
}
