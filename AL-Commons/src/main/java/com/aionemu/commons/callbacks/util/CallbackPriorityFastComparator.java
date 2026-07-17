package com.aionemu.commons.callbacks.util;

import com.aionemu.commons.callbacks.Callback;
import java.util.Comparator;

public class CallbackPriorityFastComparator implements Comparator<Callback<?>> {

	private final CallbackPriorityComparator cpc = new CallbackPriorityComparator();

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CallbackPriorityFastComparator)) return false;
		return cpc.equals(((CallbackPriorityFastComparator) obj).cpc);
	}

	@Override
	public int compare(Callback<?> o1, Callback<?> o2) {
		return cpc.compare(o1, o2);
	}
}