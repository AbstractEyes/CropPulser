package com.abstractphil.croppulser.config;

import lombok.Data;

import java.util.Map;

@Data
public class MainConfig {
    private Map<String, PulserConfig> pulserConfig;
}
