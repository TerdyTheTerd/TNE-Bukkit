package com.github.tnerevival.commands.shop;

import com.github.tnerevival.TNE;
import com.github.tnerevival.commands.TNECommand;
import com.github.tnerevival.core.Message;
import com.github.tnerevival.core.shops.Shop;
import com.github.tnerevival.utils.MISCUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Daniel on 8/10/2016.
 */
public class ShopBrowseCommand extends TNECommand {

  public ShopBrowseCommand(TNE plugin) {
    super(plugin);
  }

  @Override
  public String getName() {
    return "browse";
  }

  @Override
  public String[] getAliases() {
    return new String[0];
  }

  @Override
  public String getNode() {
    return "tne.shop.browse";
  }

  @Override
  public boolean console() {
    return false;
  }

  @Override
  public void help(CommandSender sender) {
    sender.sendMessage(ChatColor.GOLD + "/shop browse <name> - Browse the specified shop.");
  }

  @Override
  public boolean execute(CommandSender sender, String[] arguments) {
    if(arguments.length >= 1) {
      if(Shop.exists(arguments[0], MISCUtils.getWorld(getPlayer(sender)))) {
        Shop s = Shop.getShop(arguments[0], MISCUtils.getWorld(getPlayer(sender)));

        if(s.getShoppers() != null && s.getShoppers().size() >= TNE.configurations.getInt("Core.Shops.Shoppers")) {
          sender.sendMessage(new Message("Messages.Shop.Shoppers").translate());
          return false;
        }

        ((Player)sender).openInventory(s.getInventory(Shop.canModify(s.getName(), getPlayer(sender))));
        return true;
      }
      getPlayer(sender).sendMessage(new Message("Messages.Shop.None").translate());
      return false;
    }
    help(sender);
    return false;
  }

}