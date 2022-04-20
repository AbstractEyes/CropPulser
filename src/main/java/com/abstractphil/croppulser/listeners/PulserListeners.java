package com.abstractphil.croppulser.listeners;

import com.abstractphil.croppulser.AbsCropPulser;
import com.abstractphil.croppulser.controller.PulserController;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

import static com.abstractphil.croppulser.controller.PulserController.VERBOSE;

public class PulserListeners implements Listener {

    public static PulserController controller() {
        return AbsCropPulser.getInstance().getMainController();
    }

    public PulserListeners() {
        super();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void blockBreakPulser(BlockBreakEvent event) {
        if(!PulserController.ENABLED) return;
        if(event.isCancelled() ||
                event.getPlayer() == null ||
                event.getBlock().getState() == null)
            return;
        if(!PulserController.ENABLED) {
            return;
        };
        if(event.getBlock().getState() instanceof CraftBlockState) {
            CraftBlockState craftBlockState = (CraftBlockState) event.getBlock().getState();

            if (PulserController.isPulserBlockState(craftBlockState)) {
                // Remove pulser from the pulserData and blockKV lists.
                PulserController.unregisterPulserTimer(event.getBlock().getChunk(), craftBlockState);
                PulserController.removeKVPulserData(craftBlockState);
                controller().restorePulserItem(event.getPlayer(), craftBlockState);
                if(VERBOSE) System.out.println("Pulser broken; " + craftBlockState.getTileEntity());
                event.setDoesDrop(false);
                event.getBlock().setType(Material.AIR);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void blockPlacePulser(BlockPlaceEvent event) {
        if(event.isCancelled() ||
                event.getPlayer() == null ||
                event.getPlayer().getItemInHand() == null ||
                event.getPlayer().getItemInHand().getType() == Material.AIR ||
                !PulserController.isPulserItemStack(event.getPlayer().getItemInHand()) ||
                event.getBlock().getState() == null)
            return;
        if(!PulserController.ENABLED) {
            event.setCancelled(true);
            return;
        };
        if(event.getBlock().getState() instanceof CraftBlockState) {
            CraftBlockState tileEntity = (CraftBlockState) event.getBlock().getState();
            if(tileEntity != null) {
                if(!PulserController.areAnyPulsersTooClose(tileEntity)) {
                    PulserController.registerPulserTimer(event.getBlock().getChunk(), tileEntity,
                            preparePulserTask(event.getBlock()));
                    PulserController.setKVPulserData(event.getPlayer(), tileEntity, event.getItemInHand());
                    if(VERBOSE) System.out.println("Pulser placed; " + tileEntity.getTileEntity());
                } else {
                    if(VERBOSE) event.getPlayer().sendMessage("Pulser placed too close to other pulser.");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onLoadPulser(ChunkLoadEvent event) {
        if(!PulserController.ENABLED) return;
        if(event.getWorld() == null) return;
        try {
            // Create pulserData from loaded KV data.
            for(BlockState uState : event.getChunk().getTileEntities()) {
                if(uState instanceof CraftBlockState) {
                    CraftBlockState state = (CraftBlockState)uState;
                    if (PulserController.isPulserBlockState(state)) {
                        if(VERBOSE) System.out.println("Preparing to register pulser; " + state.getTileEntity());
                        PulserController.registerPulserTimer(
                                event.getChunk(), state,
                                preparePulserTask(state.getBlock()));
                        if(VERBOSE) System.out.println("Pulser loaded; " + state.getTileEntity());
                    }
                } else {
                    System.out.println("Is not block state, failed to load pulser;");
                    System.out.println(uState.getBlock());
                }
            }
        } catch(Exception ex){
            System.out.println("Failed to load pulsers; ");
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onUnloadPulser(ChunkUnloadEvent event) {
        // Probably not necessary.
        if(PulserController.getPulserTasks().containsKey(event.getChunk())) {
            if(PulserController.getPulserTimers(event.getChunk()) != null){
                PulserController.getPulserTimers(event.getChunk()).forEach( (key, val) -> {
                    val.cancel();
                });
            }
        }
    }

    public static BukkitTask preparePulserTask(Block blockIn) {
        int startTimer = controller().getPulserConfig().getMinTimer();
        int maxTimer = controller().getPulserConfig().getMaxTimer();
        int delayTimer = controller().getPulserConfig().getDelayTimer();
        int range = controller().getPulserConfig().getCropRange();
        int odds = controller().getPulserConfig().getSuccessChance();
        return new BukkitRunnable() {
            public final Block block = blockIn;
            public int counter = ThreadLocalRandom.current().nextInt(startTimer, maxTimer);
            private void resetCounter() {
                counter = ThreadLocalRandom.current().nextInt(startTimer, maxTimer);
            }
            @Override
            public void run() {
                // If host block becomes air, something happened to the event should cancel.
                if(!PulserController.ENABLED) return; // Do not cancel task.
                counter--;
                if(counter <= 0) {
                    resetCounter();
                    try {
                        //System.out.println("Pulser task running; " + block.getState().getLocation());
                        if(block.getType() == Material.AIR) {
                            System.out.println("Pulser is air, which means it's broken and must be cancelled.");
                            System.out.println(block.getLocation());
                            cancel();
                            PulserController.cleanUpPulsers();
                            return;
                        }
                        // Check surrounding area for CROPS
                        //System.out.println("Running pulser at; " + blockIn.getLocation());
                        int x1 = -range, x2 = range;
                        int z1 = -range, z2 = range;
                        int y = 0;
                        boolean cropsGrew = false;

                        for(int xi = x1; xi <= x2; xi++) {
                            for(int zi = z1; zi <= z2; zi++) {
                                Block relativeBlock = block.getRelative(xi, y, zi);
                                if(relativeBlock == block) continue;
                                cropsGrew = runPulser(relativeBlock);
                            }
                        }
                    if(cropsGrew)
                        System.out.println(
                                controller().getPulserConfig().getMessages().get(0));
                    } catch (Exception ex) {
                        cancel();
                        ex.printStackTrace();
                    }
                }

            }

            @SuppressWarnings("deprecation")
            public boolean runPulser(Block block) {
                Material material = block.getType();
                byte currentCropState = block.getData();
                net.minecraft.server.v1_8_R3.Block blk = net.minecraft.server.v1_8_R3.Block.getById(block.getTypeId());

                switch(material) {
                    case SOIL:
                    case WHEAT:
                    case CROPS:
                        // Do simple growth.
                        if(currentCropState < 7) {
                            block.setData((byte)(currentCropState + 1));
                        }
                        return false;
                    case NETHER_STALK:
                    case SUGAR_CANE_BLOCK:
                        // Do climbing growth.
                        blk = net.minecraft.server.v1_8_R3.Block.getById(block.getTypeId());
                        // public void b(World var1, BlockPosition var2, IBlockData var3, Random var4) {
                        if(ThreadLocalRandom.current().nextInt(0, 99) <=
                                AbsCropPulser.getInstance().getMainController().getPulserConfig().getSuccessChance()) {
                            Block upBlock = block.getRelative(BlockFace.UP);
                            if(upBlock.getType() == Material.SUGAR_CANE_BLOCK ||
                                    upBlock.getType() == Material.NETHER_STALK) {
                                blk.b(((CraftWorld)upBlock.getWorld()).getHandle(),
                                        new BlockPosition(upBlock.getX(), upBlock.getY(), upBlock.getZ()),
                                        blk.fromLegacyData(upBlock.getData()),
                                        ThreadLocalRandom.current());
                            }
                        }
                    case CARROT:
                    case POTATO:
                    case COCOA:
                    case MELON_STEM:
                    case PUMPKIN_STEM:
                        //block.setData((byte) Math.min(1 + block.getData(), 7));
                        blk = net.minecraft.server.v1_8_R3.Block.getById(block.getTypeId());
                        // public void b(World var1, BlockPosition var2, IBlockData var3, Random var4) {
                        if(ThreadLocalRandom.current().nextInt(0, 99) <=
                                AbsCropPulser.getInstance().getMainController().getPulserConfig().getSuccessChance()) {
                            blk.b(((CraftWorld)block.getWorld()).getHandle(),
                                    new BlockPosition(block.getX(), block.getY(), block.getZ()),
                                    blk.fromLegacyData(block.getData()),
                                    ThreadLocalRandom.current());
                        }
                        return false;
                    default:
                        return false;
                }
            }
        }.runTaskTimer(AbsCropPulser.getInstance(), delayTimer, 1);
    }



}
