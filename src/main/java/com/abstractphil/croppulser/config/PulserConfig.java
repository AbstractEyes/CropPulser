package com.abstractphil.croppulser.config;

import lombok.Data;

import java.util.List;

@Data
public class PulserConfig {
    private List<String> messages, lore, cropBlocks;
    private String name, material, enabled;
    private int minTimer, maxTimer, delayTimer, cropRange, successChance;
}
