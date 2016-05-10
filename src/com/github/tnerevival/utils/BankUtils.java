package com.github.tnerevival.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;

import com.github.tnerevival.TNE;
import com.github.tnerevival.account.Account;
import com.github.tnerevival.account.Bank;
import com.github.tnerevival.serializable.SerializableItemStack;

public class BankUtils {
	public static void applyInterest(UUID id) {
		Account account = AccountUtils.getAccount(id);
		Iterator<Entry<String, Bank>> it = account.getBanks().entrySet().iterator();
		
		while(it.hasNext()) {
			Entry<String, Bank> entry = it.next();
			
			if(interestEnabled(entry.getKey())) {
				Double gold = entry.getValue().getGold();
				Double interestEarned = gold * interestRate(entry.getKey());
				entry.getValue().setGold(gold + AccountUtils.round(interestEarned));
			}
		}
	}
	
	public static Boolean interestEnabled(String world) {
		if(MISCUtils.multiWorld()) {
			if(MISCUtils.worldConfigExists("Worlds." + world + ".Bank.Interest.Enabled")) {
				return TNE.instance.worldConfigurations.getBoolean("Worlds." + world + ".Bank.Interest.Enabled");
			}
		}
		return TNE.configurations.getBoolean("Core.Bank.Interest.Enabled");
	}
	
	public static Double interestRate(String world) {
		if(MISCUtils.multiWorld()) {
			if(MISCUtils.worldConfigExists("Worlds." + world + ".Bank.Interest.Rate")) {
				return TNE.instance.worldConfigurations.getDouble("Worlds." + world + ".Bank.Interest.Rate");
			}
		}
		return TNE.configurations.getDouble("Core.Bank.Interest.Rate");
	}
	
	public static Boolean hasBank(UUID id, String world) {
		return AccountUtils.getAccount(id).getBanks().containsKey(world);
	}
	
	public static Boolean hasBank(UUID id) {
		String world = TNE.instance.defaultWorld;
		if(MISCUtils.multiWorld()) {
			world = MISCUtils.getWorld(id);
		}
		if(world == null) {
			TNE.instance.getLogger().warning("***WORLD NAME IS NULL***");
			return false;
		}
		return hasBank(id, world);
	}
	
	public static Bank getBank(UUID id) {
		String world = TNE.instance.defaultWorld;
		if(MISCUtils.multiWorld()) {
			world = MISCUtils.getWorld(id);	
		}
		return getBank(id, world);
	}
	
	public static Bank getBank(UUID id, String world) {
		return AccountUtils.getAccount(id).getBank(world);
	}
	
	public static Bank fromString(String bankString) {
		String[] variables = bankString.split("\\:");
		Bank bank;
		try {
			UUID id = UUID.fromString(variables[0]);
			bank = new Bank(id, Integer.parseInt(variables[2]), Double.parseDouble(variables[3]));
		} catch(IllegalArgumentException e) {
			bank = new Bank(MISCUtils.getID(variables[0]), Integer.parseInt(variables[2]), Double.parseDouble(variables[3]));
		}
		
		List<SerializableItemStack> items = new  ArrayList<SerializableItemStack>();
		
		if(!variables[4].equalsIgnoreCase("TNENOSTRINGVALUE")) {
			String[] itemStrings = variables[4].split("\\*");
			for(String s : itemStrings) {
				items.add(MISCUtils.itemstackFromString(s));
			}
		}
		bank.setItems(items);
		return bank;
	}
	
	public static Inventory getBankInventory(UUID id, String world) {
		
		if(!hasBank(id, world)) {
			return null;
		}
		
		if(!AccountUtils.getAccount(id).getStatus().getBank()) {
			return null;
		}
		
		Bank bank = getBank(id, world);
		String gold = "Gold: " + MISCUtils.getShort(bank.getGold());
		Inventory bankInventory = Bukkit.createInventory(null, size(world), ChatColor.WHITE + "Bank " + ChatColor.GOLD + gold);
		if(bank.getItems().size() > 0) {
			List<SerializableItemStack> items = bank.getItems();
			
			for(SerializableItemStack stack : items) {
				bankInventory.setItem(stack.getSlot(), stack.toItemStack());
			}
		}
		return bankInventory;
	}
	
	public static Inventory getBankInventory(UUID id) {
		String world = TNE.instance.defaultWorld;
		if(MISCUtils.multiWorld()) {
			world = MISCUtils.getWorld(id);	
		}
		return getBankInventory(id, world);
	}
	
	public static Double getBankBalance(UUID id, String world) {
		if(!hasBank(id, world)) {
			return null;
		} else {
			if(!AccountUtils.getAccount(id).getStatus().getBank()) {
				return 0.0;
			}
			Bank bank = getBank(id, world);
			return AccountUtils.round(bank.getGold());
		}
	}
	
	public static Double getBankBalance(UUID id) {
		return getBankBalance(id, TNE.instance.defaultWorld);
	}
	
	public static void setBankBalance(UUID id, String world, Double amount) {
		if(!AccountUtils.getAccount(id).getStatus().getBank()) {
			return;
		}
		if(hasBank(id, world)) {
			Bank bank = getBank(id, world);
			bank.setGold(AccountUtils.round(amount));
			return;
		}
	}
	
	public static void setBankBalance(UUID id, Double amount) {
		setBankBalance(id, TNE.instance.defaultWorld, amount);
	}
	
	public static Boolean bankHasFunds(UUID id, String world, Double amount) {
		amount = AccountUtils.round(amount);
		return (getBankBalance(id, world) != null) ? getBankBalance(id, world) >= amount : false;
	}
	
	public static Boolean bankHasFunds(UUID id, Double amount) {
		return bankHasFunds(id,TNE.instance.defaultWorld, amount);
	}
	
	public static Boolean bankDeposit(UUID id, Double amount) {
		if(!AccountUtils.getAccount(id).getStatus().getBank()) {
			return false;
		}
		amount = AccountUtils.round(amount);
		if(AccountUtils.hasFunds(id, amount)) {
			Bank bank = getBank(id);
			bank.setGold(bank.getGold() + amount);
			AccountUtils.removeFunds(id, amount);
			return true;
		} else {
			return false;
		}
	}
	
	public static Boolean bankWithdraw(UUID id, Double amount) {
		if(!AccountUtils.getAccount(id).getStatus().getBank()) {
			return false;
		}
		amount = AccountUtils.round(amount);
		if(bankHasFunds(id, amount)) {
			Bank bank = getBank(id);
			bank.setGold(bank.getGold() - amount);
			AccountUtils.addFunds(id, amount);
			return true;
		} else {
			return false;
		}
	}
	
	//Configuration-related Utils
	
	public static Integer size(String world) {
		Integer rows = 3;
		if(MISCUtils.multiWorld()) {
			if(MISCUtils.worldConfigExists("Worlds." + world + ".Bank.Rows")) {
				rows = TNE.instance.worldConfigurations.getInt("Worlds." + world + ".Bank.Rows");
			}
		} else {
			rows = TNE.configurations.getInt("Core.Bank.Rows");
		}
		return (rows >= 1 && rows <= 6) ? (rows * 9) : 27;
	}
	
	public static Boolean enabled(String world) {
		if(MISCUtils.multiWorld()) {
			if(MISCUtils.worldConfigExists("Worlds." + world + ".Bank.Enabled")) {
				return TNE.instance.worldConfigurations.getBoolean("Worlds." + world + ".Bank.Enabled");
			}
		}
		return TNE.configurations.getBoolean("Core.Bank.Enabled");
	}
	
	public static Boolean command(String world) {
		if(MISCUtils.multiWorld()) {
			if(MISCUtils.worldConfigExists("Worlds." + world + ".Bank.Command")) {
				return TNE.instance.worldConfigurations.getBoolean("Worlds." + world + ".Bank.Command");
			}
		}
		return TNE.configurations.getBoolean("Core.Bank.Command");
	}
	
	public static Double cost(String world) {
		if(MISCUtils.multiWorld()) {
			if(MISCUtils.worldConfigExists("Worlds." + world + ".Bank.Cost")) {
				return AccountUtils.round(TNE.instance.worldConfigurations.getDouble("Worlds." + world + ".Bank.Cost"));
			}
		}
		return AccountUtils.round(TNE.configurations.getDouble("Core.Bank.Cost"));
	}
	
	public static Boolean sign(String world) {
		if(MISCUtils.multiWorld()) {
			if(MISCUtils.worldConfigExists("Worlds." + world + ".Bank.Sign")) {
				return TNE.instance.worldConfigurations.getBoolean("Worlds." + world + ".Bank.Sign");
			}
		}
		return TNE.configurations.getBoolean("Core.Bank.Sign");
	}
	
	public static Boolean npc(String world) {
		if(MISCUtils.multiWorld()) {
			if(MISCUtils.worldConfigExists("Worlds." + world + ".Bank.NPC")) {
				return TNE.instance.worldConfigurations.getBoolean("Worlds." + world + ".Bank.NPC");
			}
		}
		return TNE.configurations.getBoolean("Core.Bank.NPC");
	}
}