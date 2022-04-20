package com.abstractphil.croppulser.controller;

import com.abstractphil.croppulser.AbsCropPulser;
import com.abstractphil.croppulser.config.MainConfig;
import com.abstractphil.croppulser.config.PulserConfig;
import com.abstractphil.croppulser.listeners.CropPulserItem;
import com.abstractphil.croppulser.shapes.Area3D;
import com.abstractphil.croppulser.tools.AbsItemUtil;
import com.abstractphil.croppulser.tools.InventoryUtil;
import com.abstractphil.croppulser.tools.NBTUtil;
import com.redmancometh.configcore.config.ConfigManager;
import com.redmancometh.warcore.util.ItemUtil;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.Tuple;
import ninja.coelho.dimm.libutil.asyncholo.*;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;

import static com.abstractphil.croppulser.listeners.PulserListeners.preparePulserTask;

@Getter
public class PulserController {
	private final ConfigManager<MainConfig> cfg =
				new ConfigManager("croppulser.json", MainConfig.class);
	private static final HashMap<Chunk, HashMap<CraftBlockState, BukkitTask>> pulserTasks = new HashMap<>();
	//private AsyncHolosContext holoInterface;
	private final HashMap<Location, List<AsyncHolo>> holoRegistry = new HashMap<>();
	private static CropPulserItem itemData;
	public static boolean ENABLED = true;
	public static boolean VERBOSE = false;

	public static ArrayList<String> getImportantProperties(ItemStack item) {
		if(isPulserItemStack(item)) {
			ArrayList<String> out = new ArrayList<>();
			out.add(NBTUtil.getStringData(item, "uuid"));
			return out;
		}
		return new ArrayList<>();
	}

	public static void cleanUpPulsers() {
		HashMap<Chunk, CraftBlockState> markedForDeath = new HashMap<>();
		for (Map.Entry<Chunk, HashMap<CraftBlockState, BukkitTask>> entry : pulserTasks.entrySet()) {
			Chunk chunk = entry.getKey();
			HashMap<CraftBlockState, BukkitTask> key = entry.getValue();
			for (Map.Entry<CraftBlockState, BukkitTask> e : key.entrySet()) {
				CraftBlockState state = e.getKey();
				BukkitTask task = e.getValue();
				if (state.getBlock().getType() == Material.AIR) {
					markedForDeath.put(chunk, state);
				}
			}
		}
		for (Map.Entry<Chunk, CraftBlockState> deathPair : markedForDeath.entrySet()) {
			pulserTasks.get(deathPair.getKey()).get(deathPair.getValue()).cancel();
			pulserTasks.get(deathPair.getKey()).remove(deathPair.getValue());
		}
	}

	public static void reloadPulsers() {
		if(VERBOSE){ System.out.println("Reloading all crop pulsers"); }
		pulserTasks.forEach( (chunk, map) -> {
			map.forEach( (tile, task) -> {
				task.cancel();
			});
		});
		pulserTasks.clear();
		for(Player pl : Bukkit.getOnlinePlayers()) {
			for(Chunk chunk : pl.getWorld().getLoadedChunks()) {
				if(!PulserController.ENABLED)
					System.out.println("You cannot reload pulsers with them disabled.");
				try {
					// Create pulserData from loaded KV data.
					for(BlockState uState : chunk.getTileEntities()) {
						if(uState instanceof CraftBlockState) {
							CraftBlockState state = (CraftBlockState)uState;
							if (PulserController.isPulserBlockState(state)) {
								if(VERBOSE) System.out.println("Preparing to register pulser; " + state.getTileEntity());
								PulserController.registerPulserTimer(
										chunk, state,
										preparePulserTask(state.getBlock()));
								if(VERBOSE) System.out.println("Pulser loaded; " + state.getTileEntity());
							}
						} else {
							if(VERBOSE) System.out.println("Is not block state;");
							if(VERBOSE) System.out.println(uState.getBlock());
						}
					}
				} catch(Exception ex){
					System.out.println("Failed to load pulsers; ");
					ex.printStackTrace();
				}
			}
		}
	}

	public void init() {
		cfg.init();
		itemData = new CropPulserItem();
		PulserConfig confi = cfg.getConfig().getPulserConfig().get("pulser");
		String name = confi.getName();
		String material = confi.getMaterial();
		List<String> list = confi.getLore();
		itemData.setItemConfig(name, list, material);
		ENABLED = Boolean.parseBoolean(confi.getEnabled());
		//holoInterface = ((ninja.coelho.dimm.DIMMPlugin)Bukkit.getPluginManager().getPlugin("DIMM")).getLibUtil().getAsyncHolos().on("AbsCropPulser", new ManagedCloseCallback() {
		//	@Override
		//	public void accept(AutoCloseable autoCloseable) {
		//		// Clean up.
		//	}
		//});
		reloadPulsers();
	}

	public void terminate() {
		pulserTasks.forEach( (chunk, map) -> {
			map.forEach( (tile, task) -> {
				task.cancel();
			});
		});
		pulserTasks.clear();
		//for(Map.Entry<Location, List<AsyncHolo>> holo : holoRegistry.entrySet()) {
		//	holo.getValue().forEach( holoI -> {
		//		holoInterface.despawn(holoI);
		//	});
		//}
		//holoRegistry.clear();
	}

	public void addPulserItems(Player player, int amount) {
		player.getInventory().addItem(makePulserItem(amount, ""));
	}

	public void restorePulserItem(Player player, CraftBlockState state) {
		ItemStack item = ItemUtil.buildItem(
				itemData.getMaterialData(),
				itemData.getNameData(),
				itemData.getLoreData());
		item.setAmount(1);
		if(state.getTileEntity().kv.containsKey("uuid"))
			item = NBTUtil.setData(item, "uuid", state.getTileEntity().kv.get("uuid"));
		item = NBTUtil.setData(item, "blockpulser", true);
		InventoryUtil.safeAddCropPulserInventory(player, item);
	}

	public static ItemStack makePulserItem(int amount, String uuid) {
		System.out.println(itemData);
		ItemStack item = ItemUtil.buildItem(
				itemData.getMaterialData(),
				itemData.getNameData(),
				itemData.getLoreData());
		item.setAmount(amount);
		if(uuid.equals(""))
			uuid = UUID.randomUUID().toString();
		item = NBTUtil.setData(item, "uuid", uuid);
		item = NBTUtil.setData(item, "blockpulser", true);
		return item;
	}

	public void restoreItem(Player player, CraftBlockState state) {
		ItemStack item = ItemUtil.buildItem(
				itemData.getMaterialData(),
				itemData.getNameData(),
				itemData.getLoreData());
		item.setAmount(1);
		if(state.getTileEntity().kv.containsKey("uuid")) {
			item = NBTUtil.setData(item, "uuid", state.getTileEntity().kv.get("uuid"));
		}
		item = NBTUtil.setData(item, "blockpulser", true);
		InventoryUtil.safeAddCropPulserInventory(player, item);
	}

	public PulserConfig getPulserConfig() {
		return cfg.getConfig().getPulserConfig().get("pulser");
	}

	@Nullable
	public static HashMap<CraftBlockState, BukkitTask> getPulserTimers(Chunk chunkIn) {
		if(pulserTasks.containsKey(chunkIn)) return pulserTasks.get(chunkIn);
		return null;
	}

	public static HashMap<Chunk, HashMap<CraftBlockState, BukkitTask>> getPulserTasks() {
		return pulserTasks;
	}

	public static void registerPulserTimer(Chunk chunk, CraftBlockState blockState, BukkitTask bukkitTask) {
		if(!pulserTasks.containsKey(chunk)) pulserTasks.put(chunk, new HashMap<>());
		pulserTasks.get(chunk).put(blockState, bukkitTask);

		System.out.println("Pulser task registered;");
		System.out.println(blockState.getTileEntity());
	}

	public static void unregisterPulserTimer(Chunk chunk, CraftBlockState blockState) {
		if(!pulserTasks.containsKey(chunk)) pulserTasks.put(chunk, new HashMap<>());
		if(pulserTasks.get(chunk).containsKey(blockState)) {
			pulserTasks.get(chunk).get(blockState).cancel();
			pulserTasks.get(chunk).remove(blockState);
			System.out.println("Pulser task unregistered;");
			System.out.println(blockState.getTileEntity());
			//System.out.println(pulserTasks);

		}
	}

	public static void setKVPulserData(Player player, CraftBlockState blockState, ItemStack item) {
		blockState.getTileEntity().b(new NBTTagCompound());
		blockState.getTileEntity().kv.put("blockpulser", player.getUniqueId().toString());
		if(NBTUtil.getStringData(item, "uuid") != null)
			blockState.getTileEntity().kv.put("uuid", NBTUtil.getStringData(item, "uuid"));
	}

	public static void removeKVPulserData(CraftBlockState blockState) {
		blockState.getTileEntity().kv.remove("blockpulser");
		System.out.println("Pulser KV removed;");
		System.out.println(blockState.getTileEntity());
	}

	public void spawnHolo(Location location) {
		//if(holoRegistry.containsKey(location)) removeHolo(location);
		//VirtualHolo holo = (VirtualHolo)holoInterface.spawn(location, AsyncHoloAlign.TOP);
		//VirtualHoloLineText holoLine1 = (VirtualHoloLineText)holo.appendTextLine(itemData.getNameData());
		//ArrayList<AsyncHolo> holoOut = new ArrayList<>();
		//holoOut.add(holo);
		//holoOut.add((AsyncHolo)holoLine1);
		//holoRegistry.put(location, holoOut);
	}

	public void removeHolo(Location location) {
		//if(holoRegistry.containsKey(location)){
		//	for(AsyncHolo holo : holoRegistry.get(location)) {
		//		holoInterface.despawn(holo);
		//	}
		//}
	}

	public static boolean isPulserBlockState(BlockState bStateIn) {
		if(!(bStateIn instanceof CraftBlockState)) return false;
		CraftBlockState blockState = (CraftBlockState) bStateIn;
		if(blockState.getTileEntity() == null || blockState.getTileEntity().kv == null)
			return false;
		return(blockState.getTileEntity().kv.containsKey("blockpulser"));
	}

	public static boolean isPulserItemStack(ItemStack item) {
		return AbsItemUtil.nbt().hasData(item, "blockpulser");
	}

	private static boolean insideRadius(int range, Location loc1, Location loc2) {
		int l1_x1 = loc1.getBlockX() - range;
		int l1_x2 = loc1.getBlockX() + range;
		int l1_z1 = loc1.getBlockZ() - range;
		int l1_z2 = loc1.getBlockZ() + range;

		int l2_x1 = loc2.getBlockX() - range;
		int l2_x2 = loc2.getBlockX() + range;
		int l2_z1 = loc2.getBlockZ() - range;
		int l2_z2 = loc2.getBlockZ() + range;
		Area3D hostArea = new Area3D(
				l1_x1, l1_x2,
				loc1.getBlockY(), loc1.getBlockY(),
				l1_z1, l1_z2);
		Area3D checkArea = new Area3D(l2_x1, l2_x2, loc2.getBlockY(), loc2.getBlockY(), l2_z1, l2_z2);
		return(hostArea.intersecting3DAreas(checkArea));
	}

	private static Tuple<Integer, Integer> getDistance(int range, Location loc1, Location loc2) {
		return( new Tuple<>((range * 2 + 1 - Math.abs(loc1.getBlockX() - loc2.getBlockX()) ),
				(range * 2 + 1 - Math.abs(loc1.getBlockZ() - loc2.getBlockZ()))));
	}

	public static boolean areAnyPulsersTooClose(CraftBlockState craftBlockStateIn) {
		Location l2 = craftBlockStateIn.getLocation();
		for (Map.Entry<Chunk, HashMap<CraftBlockState, BukkitTask>> entry : pulserTasks.entrySet()) {
			HashMap<CraftBlockState, BukkitTask> key = entry.getValue();
			for (Map.Entry<CraftBlockState, BukkitTask> e : key.entrySet()) {
				CraftBlockState state = e.getKey();
				Location l1;
				l1 = state.getLocation();
				int r = AbsCropPulser.getInstance().getMainController().getPulserConfig().getCropRange();
				if(insideRadius(r, l2, l1) && l2.getBlockY() == l1.getBlockY() &&
						craftBlockStateIn.getWorld().getUID() == entry.getKey().getWorld().getUID()) {
					return true;
				}
			}
		}
		return false;
	}


}
