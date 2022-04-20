package com.abstractphil.croppulser.commands;

import com.abstractphil.croppulser.controller.PulserController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

//
public class CheckPulsers implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) return false;
        try {
            PulserController.getPulserTasks().forEach( (chunk, map) ->{
                map.forEach((entity, task) -> {
                    System.out.println("Chunk; " + chunk);
                    System.out.println("TileEntity; " + entity);
                    System.out.println("Task; " + task);
                });
            });
            return true;
        } catch (Exception ex) {
            System.out.println("Failed to check active pulsers, error.");
            ex.printStackTrace();
        }
        return true;
    }

}
