package com.lonleaf.blockDataSaver;


import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BlockDataSaver extends JavaPlugin implements Listener {

    private String originBlockLocation,originWorld;
    private FileConfiguration dataConfig;
    private File dataFile;

    @Override
    public void onEnable() {
        // Plugin startup logic
        //String[] version = Bukkit.getVersion().split("-",1);
        //String[] version = version_ogin[0].split("\\.",3);
        //this.getLogger().info(ChatColor.GREEN + version_ogin[0] + "@@@" + version[1] + "@@@");
        String version = Bukkit.getVersion();
        if(version.contains("1.20") || version.contains("1.15")|| version.contains("1.16")|| version.contains("1.17")|| version.contains("1.18")|| version.contains("1.19")){
            // 确保插件数据文件夹存在
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            Bukkit.getPluginManager().registerEvents(this, this);
            this.getLogger().info(ChatColor.GREEN + "方块数据保存§7：§r§2加载完成-loaded");
            this.getLogger().info(ChatColor.GREEN + "方块数据保存§7：§r§2Blog:lonleaf.com");
        }else {
            Bukkit.getPluginManager().disablePlugin(this);
            this.getLogger().info(ChatColor.GREEN + "方块数据保存§7：§r§c服务端版本低于或等于1.15,请使用1.15+的MC版本，插件已经卸载-unloaded-need higher MC version!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    private void onPlace(BlockPlaceEvent e) {
        final ItemStack item = e.getItemInHand().clone();
        item.setAmount(1);
        ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
        if(item.hasItemMeta()){
            originWorld = e.getBlockPlaced().getWorld().getName();
            int[] location = new int[3];
            location[0] = e.getBlockPlaced().getX();
            location[1] = e.getBlockPlaced().getY();
            location[2] = e.getBlockPlaced().getZ();
            originBlockLocation = location[0]+","+location[1]+","+location[2];
            // String name = NBT.get(item, nbt -> (String) nbt.getString("display.Name"));
            String json = nbtItem.toString();
            dataSaver(json,originBlockLocation,originWorld);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    private void onBreak(BlockDropItemEvent e) {
        BlockState block = e.getBlockState();
        String world = block.getWorld().getName();
        String blockLocation = blockLocation(block);
        if(getData(world,blockLocation) != null){
            List<Item> dropList = e.getItems();
            Iterator<Item> drops = dropList.iterator();
            int counter = 1, counts = 0;
            Item drop;
            do {
                if (!drops.hasNext()) {
                    counts = 1;
                    return;
                }
                counter++;
                drop = drops.next();
            } while (counts == 1);
            String json = getData(world, blockLocation);
            //this.getLogger().info(ChatColor.GREEN + "json" + json);
            ItemStack item = NBT.itemStackFromNBT(NBT.parseNBT(json));
            if (item != null) {
                drop.setItemStack(item);
                dataCleaner(world + "." + blockLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    private void onBreakNoDrop(BlockBreakEvent e){
        String blockLocation = blockLocation(e.getBlock().getState());
        String world = e.getBlock().getWorld().getName();
        if(e.isDropItems() && getData(world,blockLocation) != null){
            String json = getData(world, blockLocation);
            //this.getLogger().info(ChatColor.GREEN + "json" + json);
            ItemStack item = NBT.itemStackFromNBT(NBT.parseNBT(json));
            e.setDropItems(false);
            if (item != null) {
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), item);
                dataCleaner(world + "." + blockLocation);
            }
        }
    }

    public String blockLocation(BlockState block){
        int[] location = new int[3];
        location[0] = block.getX();
        location[1] = block.getY();
        location[2] = block.getZ();
        return location[0]+","+location[1]+","+location[2];
    }

    public void dataSaver(String nbtData,String blockLocation,String world){
        dataFile = new File(getDataFolder(), "database.yml");
        if (!dataFile.exists()) {
            saveResource("database.yml", false);  // 复制默认的 data.yml 文件到插件目录
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        saveData(world+"."+blockLocation,nbtData);
    }

    public void saveData(String key, String value) {

        // 写入数据
        dataConfig.set(key, value);

        // 保存更改到 data.yml 文件
        try {
            dataConfig.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getData(String originWorld,String originBlockLocation) {
        // 读取数据
        return dataConfig.getString(originWorld+"."+originBlockLocation);
    }

    public void dataCleaner(String key) {
        dataConfig.set(key, null);

        try {
            dataConfig.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getLogger().info(ChatColor.GREEN + "方块数据保存§7：§r§c插件已经卸载-unloaded");
        Bukkit.getPluginManager().disablePlugin(this);
    }
}
