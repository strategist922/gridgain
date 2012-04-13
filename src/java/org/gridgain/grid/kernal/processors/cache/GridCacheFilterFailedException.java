// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.processors.cache;

import org.gridgain.grid.typedef.internal.*;

/**
 * Thrown when an operation is performed on removed entry.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.2c.12042012
 */
public class GridCacheFilterFailedException extends Exception {
    /** Value for which filter failed. */
    private final Object val;

    /**
     * Empty constructor.
     */
    public GridCacheFilterFailedException() {
        val = null;
    }

    /**
     * @param val Value for which filter failed.
     */
    public GridCacheFilterFailedException(Object val) {
        this.val = val;
    }

    /**
     * @return Value for failed filter.
     */
    @SuppressWarnings({"unchecked"})
    public <V> V value() {
        return (V)val;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridCacheFilterFailedException.class, this);
    }
}
