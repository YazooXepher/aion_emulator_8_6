package com.aionemu.commons.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author NB4L1
 */
@SuppressWarnings("unchecked")
public class AEFastSet<E> extends AEFastCollection<E> implements Set<E> {

	private final ConcurrentHashMap<E, Object> map;

	public AEFastSet() {
		map = new ConcurrentHashMap<E, Object>();
	}

	public AEFastSet(int capacity) {
		map = new ConcurrentHashMap<E, Object>(capacity);
	}

	public AEFastSet(Set<? extends E> elements) {
		map = new ConcurrentHashMap<E, Object>(elements.size());
		addAll(elements);
	}

	public boolean isShared() {
		return true;
	}

	@Override
	public boolean add(E value) {
		return map.put(value, new Object()) == null;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o) != null;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public String toString() {
		return super.toString() + "-" + map.keySet().toString();
	}
}