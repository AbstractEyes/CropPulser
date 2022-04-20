package com.abstractphil.croppulser.commands;

import com.abstractphil.croppulser.controller.PulserController;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EnablePulsers implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) return false;
        try {
            if(Boolean.parseBoolean(args[0]) || !Boolean.parseBoolean(args[0])){
                boolean currentBool = PulserController.ENABLED;
                PulserController.ENABLED = Boolean.parseBoolean(args[0]);
                if(currentBool != Boolean.parseBoolean(args[0]) &&
                        PulserController.ENABLED){
                    PulserController.reloadPulsers();
                }
            } else {
                System.out.println("Please use true/false (true enable, false disable)");
            }
            return true;
        } catch (Exception ex) {
            System.out.println("Failed to enable or disable pulsers.");
            ex.printStackTrace();
        }
        return true;
    }

}
