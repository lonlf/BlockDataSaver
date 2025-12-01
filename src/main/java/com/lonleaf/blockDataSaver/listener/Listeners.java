package com.lonleaf.blockDataSaver.listener;

import com.lonleaf.blockDataSaver.data.DataManager;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

public class Listeners implements Listener {

    @EventHandler(priority = EventPriority.NORMAL,ignoreCancelled = true)
    private void onPlace(BlockPlaceEvent e) {
        final ItemStack item = e.getItemInHand().clone();
        item.setAmount(1);
        if(DataManager.isModifiedBlock(item)) {
            DataManager.saveBlock(item, e.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    private void onBreak(BlockDropItemEvent e) {
        BlockState block = e.getBlockState();
        var location = block.getLocation();
        var items = e.getItems();
        if(DataManager.contains(block)){
            var item = DataManager.withdrawBlock(location);
            if(item!=null) {
                items.clear();
                location.getWorld().dropItemNaturally(block.getLocation(), item);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    private void onBreakNoDrop(BlockBreakEvent e){
        var block = e.getBlock().getState();
        var location = block.getLocation();
        if(!e.isDropItems()){
            if(DataManager.contains(block)){
                var item = DataManager.withdrawBlock(location);
                if(item!=null) {
                    location.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
        }
    }

}
