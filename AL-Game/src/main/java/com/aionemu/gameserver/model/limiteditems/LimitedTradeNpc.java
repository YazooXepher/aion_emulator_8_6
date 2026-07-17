package com.aionemu.gameserver.model.limiteditems;

import java.util.ArrayList;

/**
 * @author xTz
 */
public class LimitedTradeNpc {

	private ArrayList<LimitedItem> limitedItems;

	public LimitedTradeNpc(ArrayList<LimitedItem> limitedItems) {
		this.limitedItems = limitedItems;

	}

	public void putLimitedItems(ArrayList<LimitedItem> limitedItems) {
		this.limitedItems.addAll(limitedItems);
	}

	public ArrayList<LimitedItem> getLimitedItems() {
		return limitedItems;
	}
}
