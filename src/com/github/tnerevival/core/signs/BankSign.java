package com.github.tnerevival.core.signs;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.github.tnerevival.utils.BankUtils;
import com.github.tnerevival.utils.MISCUtils;

public class BankSign extends TNESign {

	public BankSign(UUID owner) {
		super(owner);
		setType(SignType.BANK);
	}

	@Override
	public boolean onClick(Player player) {
		return false;
	}

	@Override
	public boolean onRightClick(Player player) {
		if(player.hasPermission(SignType.BANK.getUsePermission()) && getPermission() != null && player.hasPermission(getPermission())) {
			inventory = BankUtils.getBankInventory(MISCUtils.getID(player));
			if(!super.onOpen(player, SignType.BANK)) {
				player.openInventory(inventory);
			}
		}
		return false;
	}
}