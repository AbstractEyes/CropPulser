package com.abstractphil.croppulser;

import com.abstractphil.croppulser.commands.*;
import com.abstractphil.croppulser.controller.PulserController;
import com.abstractphil.croppulser.listeners.PulserListeners;
import ninja.coelho.dimm.libutil.LibUtilImpl;
import ninja.coelho.dimm.libutil.asyncholo.AsyncHolos;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;

@Getter
public class AbsCropPulser extends JavaPlugin {
	private PulserController mainController;


	@Override
	public void onEnable() {
		super.onEnable();
		this.mainController = new PulserController();
		this.mainController.init();
		System.out.println("Initialized crop pulser.");
		getCommand("givepulser").setExecutor(new GivePulser());
		getCommand("pulsercleanup").setExecutor(new CleanUpPulsers());
		getCommand("pulsercheck").setExecutor(new CheckPulsers());
		getCommand("pulserenable").setExecutor(new EnablePulsers());
		getCommand("pulserreload").setExecutor(new ReloadPulsers());
		Bukkit.getPluginManager().registerEvents(new PulserListeners(), this);
	}

	@Override
	public void onDisable() {
		mainController.terminate();
		super.onDisable();
	}

	public static AbsCropPulser getInstance() {
		return JavaPlugin.getPlugin(AbsCropPulser.class);
	}
}
