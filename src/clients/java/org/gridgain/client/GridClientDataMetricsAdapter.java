// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.client;

/**
 * Adapter for cache metrics.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.1c.07042012
 */
public class GridClientDataMetricsAdapter implements GridClientDataMetrics {
    /** Create time. */
    private long createTime = System.currentTimeMillis();

    /** Last read time. */
    private volatile long readTime = System.currentTimeMillis();

    /** Last update time. */
    private volatile long writeTime = System.currentTimeMillis();

    /** Number of reads. */
    private volatile int reads;

    /** Number of writes. */
    private volatile int writes;

    /** Number of hits. */
    private volatile int hits;

    /** Number of misses. */
    private volatile int misses;

    /** {@inheritDoc} */
    @Override public long createTime() {
        return createTime;
    }

    /** {@inheritDoc} */
    @Override public long writeTime() {
        return writeTime;
    }

    /** {@inheritDoc} */
    @Override public long readTime() {
        return readTime;
    }

    /** {@inheritDoc} */
    @Override public int reads() {
        return reads;
    }

    /** {@inheritDoc} */
    @Override public int writes() {
        return writes;
    }

    /** {@inheritDoc} */
    @Override public int hits() {
        return hits;
    }

    /** {@inheritDoc} */
    @Override public int misses() {
        return misses;
    }

    public void createTime(long createTime) {
        this.createTime = createTime;
    }

    public void readTime(long readTime) {
        this.readTime = readTime;
    }

    public void writeTime(long writeTime) {
        this.writeTime = writeTime;
    }

    public void reads(int reads) {
        this.reads = reads;
    }

    public void writes(int writes) {
        this.writes = writes;
    }

    public void hits(int hits) {
        this.hits = hits;
    }

    public void misses(int misses) {
        this.misses = misses;
    }
}
