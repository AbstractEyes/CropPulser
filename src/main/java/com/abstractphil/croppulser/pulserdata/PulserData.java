package com.abstractphil.croppulser.pulserdata;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

@Data
public class PulserData {
    String playerID;
    Location location;
    int boneMeal;
    public String serialize(){
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("pulser", this);
        return cfg.saveToString();
    }

    private void setPulserData(PulserData deserialized) {
        location = deserialized.getLocation();
        playerID = deserialized.getPlayerID();
        boneMeal = deserialized.getBoneMeal();
    }

    public PulserData(String blKVString) {
        try {
            YamlConfiguration cfg = new YamlConfiguration();
            cfg.loadFromString(blKVString);
            PulserData deserialized = (PulserData)cfg.get("pulser");
            setPulserData(deserialized);
        } catch (Exception ex) {
            System.out.println("Deserialization error");
            ex.printStackTrace();
        }
    }
}
