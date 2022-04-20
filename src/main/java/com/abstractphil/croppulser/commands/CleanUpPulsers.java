package com.abstractphil.croppulser.commands;

import com.abstractphil.croppulser.controller.PulserController;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CleanUpPulsers implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) return false;
        try {
            PulserController.cleanUpPulsers();
            return true;
        } catch (Exception ex) {
            System.out.println("Failed to clean pulsers, error.");
            ex.printStackTrace();
        }
        return true;
    }

}
