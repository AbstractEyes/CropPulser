package com.abstractphil.croppulser.commands;

import com.abstractphil.croppulser.controller.PulserController;
import com.abstractphil.croppulser.tools.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GivePulser implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.isOp()) return false;
		try {
			if(Bukkit.getPlayer(args[0]) != null) {
				Player player = Bukkit.getPlayer(args[0]);
				Inventory inventory = player.getInventory();
				String uuid = "";
				if(args.length > 2) {
					uuid = args[2];
				}
				int amount = Integer.parseInt(args[1]);
				for(int i = 0; i < amount; i ++ ) {
					ItemStack item = PulserController.makePulserItem(1, uuid);
					InventoryUtil.safeAddCropPulserInventory(player, item);
				}
			}
			return true;
		} catch (Exception ex) {
			System.out.println("Failed to give crop pulser.");
			ex.printStackTrace();
		}
		return true;

		/*
		if (!sender.isOp()) return false;
		try {
			Bukkit.getPlayer(args[0]).getInventory().addItem(
					PulserController.makePulserItem(Integer.parseInt(args[1])));
			return true;
		} catch (Exception ex) {
			System.out.println("Failed to gift pulser.");
			ex.printStackTrace();
		}
		return true;
		 */
	}

}
