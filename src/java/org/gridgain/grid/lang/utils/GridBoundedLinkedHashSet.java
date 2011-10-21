// Copyright (C) GridGain Systems, Inc. Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.lang.utils;

import java.util.*;

/**
 * Hash table and linked list implementation of the {@code Set} interface,
 * with predictable iteration order. This implementation differs from
 * {@code HashSet} in that it maintains a doubly-linked list running through
 * all of its entries. This linked list defines the iteration ordering,
 * which is the order in which elements were inserted into the set
 * (<i>insertion-order</i>). Note that insertion order is <i>not</i> affected
 * if an element is <i>re-inserted</i> into the set. (An element {@code e}
 * is reinserted into a set {@code s} if {@code s.add(e)} is invoked when
 * {@code s.contains(e)} would return {@code true} immediately prior to
 * the invocation.)
 * <p>
 * HashSet is also has maximum capacity. When it is reached the
 * newest elements supersede eldest ones.
 *
 * @param <E> Set element.
 * @author 2005-2011 Copyright (C) GridGain Systems, Inc.
 * @version 3.5.0c.20102011
 */
public class GridBoundedLinkedHashSet<E> extends GridSerializableSet<E> implements Cloneable {
    /** */
    private static final Object FAKE = Boolean.TRUE;

    /** */
    @SuppressWarnings({"TransientFieldNotInitialized", "CollectionDeclaredAsConcreteClass"})
    private transient HashMap<E, Object> map;

    /**
     * Constructs a new, empty set; the backing {@code LinkedHashMap}
     * instance has default initial capacity (16) and load factor (0.75).
     *
     * @param maxCap Maximum set capacity.
     */
    public GridBoundedLinkedHashSet(int maxCap) {
        assert maxCap > 0;

        map = new GridBoundedLinkedHashMap<E, Object>(maxCap);
    }

    /**
     * Constructs a new set containing the elements in the specified collection.
     * The {@code LinkedHashMap} is created with default load factor (0.75)
     * and an initial capacity sufficient to contain the elements in the specified
     * collection.
     *
     * @param c The collection whose elements are to be placed into this set.
     * @param maxCap Maximum set capacity.
     */
    public GridBoundedLinkedHashSet(Collection<? extends E> c, int maxCap) {
        assert maxCap > 0;

        map = new GridBoundedLinkedHashMap<E, Object>(Math.max((int) (c.size() / 0.75f) + 1, 16), maxCap);

        addAll(c);
    }

    /**
     * Constructs a new, empty set; the backing {@code LinkedHashMap}
     * instance has the specified initial capacity and the specified load factor.
     *
     * @param initCap The initial capacity of the hash map.
     * @param maxCap Maximum set capacity.
     * @param loadFactor the Load factor of the hash map.
     */
    public GridBoundedLinkedHashSet(int initCap, int maxCap, float loadFactor) {
        assert maxCap > 0;

        map = new GridBoundedLinkedHashMap<E, Object>(initCap, maxCap, loadFactor);
    }

    /**
     * Constructs a new, empty set; the backing {@code LinkedHashMap}
     * instance has the specified initial capacity and the specified load factor.
     *
     * @param initCap The initial capacity of the hash map.
     * @param maxCap Maximum set capacity.
     * @param loadFactor the Load factor of the hash map.
     * @param accessOrder {@code True} for access order, {@code false} for insertion order.
     */
    public GridBoundedLinkedHashSet(int initCap, int maxCap, float loadFactor, boolean accessOrder) {
        assert maxCap > 0;

        map = new GridBoundedLinkedHashMap<E, Object>(initCap, maxCap, loadFactor, accessOrder);
    }

    /**
     * Constructs a new, empty set; the backing {@code LinkedHashHashMap}
     * instance has the specified initial capacity and default load factor, which
     * is {@code 0.75}.
     *
     * @param initCap The initial capacity of the hash table.
     * @param maxCap Maximum capacity.
     */
    public GridBoundedLinkedHashSet(int initCap, int maxCap) {
        assert maxCap > 0;

        map = new GridBoundedLinkedHashMap<E, Object>(initCap, maxCap);
    }

    /**
     * Returns an iterator over the elements in this set. The elements are
     * returned in no particular order.
     *
     * @return An iterator over the elements in this set.
     * @see ConcurrentModificationException
     */
    @Override public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return The number of elements in this set (its cardinality).
     */
    @Override public int size() {
        return map.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code True} if this set contains no elements.
     */
    @Override public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     *
     * @param o Element whose presence in this set is to be tested.
     * @return {@code true} if this set contains the specified element.
     */
    @SuppressWarnings({"SuspiciousMethodCalls"})
    @Override public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     *
     * @param o Element to be added to this set.
     * @return {@code True} if the set did not already contain the specified
     *      element.
     */
    @Override public boolean add(E o) {
        return map.put(o, FAKE) == null;
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o Object to be removed from this set, if present.
     * @return {@code True} if the set contained the specified element.
     */
    @SuppressWarnings({"ObjectEquality"})
    @Override public boolean remove(Object o) {
        return map.remove(o) == FAKE;
    }

    /**
     * Removes all of the elements from this set.
     */
    @Override public void clear() {
        map.clear();
    }

    /**
     * Returns a shallow copy of this {@code GridBoundedLinkedHashSet}
     * instance: the elements themselves are not cloned.
     *
     * @return a shallow copy of this set.
     * @throws CloneNotSupportedException Thrown if cloning is not supported.
     */
    @SuppressWarnings("unchecked")
    @Override public Object clone() throws CloneNotSupportedException {
        GridBoundedLinkedHashSet<E> newSet = (GridBoundedLinkedHashSet<E>)super.clone();

        newSet.map = (HashMap<E, Object>)map.clone();

        return newSet;
    }
}
