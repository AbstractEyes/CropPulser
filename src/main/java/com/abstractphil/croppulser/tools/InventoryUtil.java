package com.abstractphil.croppulser.tools;

import com.abstractphil.croppulser.controller.PulserController;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class InventoryUtil {

    public static void safeAddCropPulserInventory(Player player, ItemStack item) {

        if(canHoldItem(player.getInventory(), item)) {
            player.getInventory().addItem(item);
            player.updateInventory();
        } else {
            ArrayList<String> props = PulserController.getImportantProperties(item);
            Bukkit.dispatchCommand( Bukkit.getConsoleSender(),
                    "claimgive " + player.getName() + " " + "FULL_INVENTORY" + " " +
                            "givepulser " + player.getName() + " 1 " + props.get(0)
            );
        }
    }

    public static boolean itemHasNBT(ItemStack item) {
        NBTTagCompound tags = NBTUtil.getNBTTags(item);
        if(tags != null && !tags.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean canStack(ItemStack item1, ItemStack item2) {
        // If nbt not matching or neither has nbt it's automatically no match.
        if(itemHasNBT(item1) != itemHasNBT(item2) ||
                !itemHasNBT(item1) && !itemHasNBT(item2))
            return false;
        NBTTagCompound tags1 = NBTUtil.getNBTTags(item1);
        NBTTagCompound tags2 = NBTUtil.getNBTTags(item2);
        // If neither has nbt, the matching type and max stack determines if they match.
        if(tags1 != null && !tags1.isEmpty() && tags2 != null && !tags2.isEmpty()) {
            if(tags1.c().equals(tags2.c())) {
                for(String tag : tags1.c()) {
                    //System.out.println("Iterated NBT; " + tag);
                    //System.out.println(tags1.get(tag));
                    //System.out.println(tags2.get(tag));
                    //System.out.println(!tags1.get(tag).equals(tags2.get(tag)));
                    if(!tags1.get(tag).equals(tags2.get(tag))) return false;
                }
            }
        }
        return true;
    }

    public static boolean canHoldItem(Inventory inventory, ItemStack item) {
        if (inventory.firstEmpty() != -1){
            return true;
        } else {
            for (int i=0; i<inventory.getSize(); i++) {
                ItemStack checkItem = inventory.getItem(i);
                if (checkItem.getAmount()+item.getAmount()<=item.getMaxStackSize()) {
                    if (checkItem.getType().equals(item.getType()) && canStack(item, checkItem)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static ItemStack addItemEmptySlot(Inventory inventory, ItemStack item) {
        ItemStack[] contents = inventory.getContents();
        for (int i = 0, contentsLength = contents.length; i < contentsLength; i++) {
            ItemStack iItem = contents[i];
            if (iItem.getType() == Material.AIR) {
                inventory.setItem(i, item);
                return iItem;
            }
        }
        return item;
    }

}
