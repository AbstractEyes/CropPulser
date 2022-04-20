package com.abstractphil.croppulser.commands;

import com.abstractphil.croppulser.controller.PulserController;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.entity.Player;

import static com.abstractphil.croppulser.listeners.PulserListeners.preparePulserTask;

public class ReloadPulsers implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) return false;
        try {
            PulserController.reloadPulsers();
        } catch (Exception ex) {
            System.out.println("Failed to gift pulser.");
            ex.printStackTrace();
        }
        return true;
    }

}