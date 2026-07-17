package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.GodStone;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

import java.util.ArrayList;

/**
 * @author Avol modified by ATracer
 */
public class SM_UPDATE_PLAYER_APPEARANCE extends AionServerPacket {

	public int playerId;
	public int size;
	public ArrayList<Item> items;

	public SM_UPDATE_PLAYER_APPEARANCE(int playerId, ArrayList<Item> items) {
		this.playerId = playerId;
		this.items = items;
		this.size = items.size();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerId);

		int mask = 0;
		for (Item item : items) {
			if (item.getItemTemplate().isTwoHandWeapon()) {
				ItemSlot[] slots = ItemSlot.getSlotsFor(item.getEquipmentSlot());
				mask |= slots[0].getSlotIdMask();
			}
			else {
				mask |= item.getEquipmentSlot();
			}
		}

		writeD(mask); // item size DBS

		for (Item item : items) {
			writeD(item.getItemSkinTemplate().getTemplateId());
			GodStone godStone = item.getGodStone();
			writeD(godStone != null ? godStone.getItemId() : 0);
			writeD(item.getItemColor());
			if (item.getItemTemplate().isAccessory()) {
				if (item.getItemTemplate().isPlume()) {
					float authorize = item.getEnchantOrAuthorizeLevel() / 5;
					if (item.getEnchantOrAuthorizeLevel() >= 5) {
						authorize = authorize > 2.0F ? 2.0F : authorize;
						writeD((int) authorize << 3);
					}
					else {
						writeD(0);
					}
				}
				else if (item.getItemTemplate().isBracelet()) {
					if (item.getEnchantOrAuthorizeLevel() >= 5 && item.getEnchantOrAuthorizeLevel() < 10) {
						writeD(96);
					}
					else if (item.getEnchantOrAuthorizeLevel() >= 10) {
						writeD(160);
					}
					else {
						writeD(32);
					}
				}
				else {
					writeD(item.getEnchantOrAuthorizeLevel() >= 5 ? 2 : 0);
				}
			}
			else if ((item.getItemTemplate().isWeapon()) || (item.getItemTemplate().isTwoHandWeapon())) {
				writeD(item.getEnchantOrAuthorizeLevel() == 15 ? 2 : item.getEnchantOrAuthorizeLevel() >= 20 ? 4 : 0);
			}
			else {
				writeD(0);
			}
		}
	}
}
