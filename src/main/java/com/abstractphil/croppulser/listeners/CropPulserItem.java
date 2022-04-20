package com.abstractphil.croppulser.listeners;

import com.abstractphil.croppulser.AbsCropPulser;
import com.redmancometh.reditems.EnchantType;
import com.redmancometh.reditems.abstraction.Effect;
import lombok.Data;
import org.bukkit.Material;

import java.util.List;

@Data
public class CropPulserItem {
    String nameData;
    Material materialData;
    List<String> loreData;

    public void setItemConfig(String nameIn, List<String> loreIn, String materialIn){
        nameData = nameIn;
        loreData = loreIn;
        materialData = Material.getMaterial(materialIn);
    }

}
