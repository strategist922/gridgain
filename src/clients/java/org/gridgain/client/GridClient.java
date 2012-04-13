// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.client;

import org.gridgain.client.balancer.*;

import java.util.*;

/**
 * Java client API.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.2c.12042012
 */
public interface GridClient {
    /**
     * Gets a unique client identifier. This identifier is generated by factory on client creation
     * and used in identification and authentication procedure on server node.
     *
     * @return Generated client id.
     */
    public UUID id();
    
    /**
     * Gets a data projection for a default grid cache with {@code null} name.
     *
     * @return Data projection for grid cache with {@code null} name.
     * @throws GridClientException If client was closed.
     */
    public GridClientData data() throws GridClientException;

    /**
     * Gets a data projection for grid cache with name <tt>cacheName</tt>. If
     * no data configuration with given name was provided at client startup, an
     * exception will be thrown.
     *
     * @param cacheName Grid cache name for which data projection should be obtained.
     * @return Data projection for grid cache with name <tt>cacheName</tt>.
     * @throws GridClientException If client was closed or no configuration with given name was provided.
     */
    public GridClientData data(String cacheName) throws GridClientException;

    /**
     * Gets a default compute projection. Default compute projection will include all nodes
     * in remote grid. Selection of node that will be connected to perform operations will be 
     * done according to {@link GridClientLoadBalancer} provided in client configuration or
     * according to affinity if projection call involves affinity key.
     * <p>
     * More restricted projection configurations may be created with {@link GridClientCompute} methods.
     *
     * @return Default compute projection.
     *
     * @see GridClientCompute
     */
    public GridClientCompute compute();

    /**
     * Adds topology listener. Remote grid topology is refreshed every
     * {@link GridClientConfiguration#getTopologyRefreshFrequency()} milliseconds. If any node was added or removed,
     * a listener will be notified.
     *
     * @param lsnr Listener to add.
     */
    public void addTopologyListener(GridClientTopologyListener lsnr);

    /**
     * Removes previously added topology listener.
     *
     * @param lsnr Listener to remove.
     */
    public void removeTopologyListener(GridClientTopologyListener lsnr);

    /**
     * Gets an unmodifiable snapshot of topology listeners list.
     *
     * @return List of topology listeners.
     */
    public Collection<GridClientTopologyListener> topologyListeners();
}
