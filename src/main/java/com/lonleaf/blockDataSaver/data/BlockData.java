package com.lonleaf.blockDataSaver.data;

import com.lonleaf.blockDataSaver.BlockDataSaver;
import com.lonleaf.blockDataSaver.database.Database;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class BlockData {

    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final ReadWriteNBT nbt;

    public BlockData(Location location, String snbt){
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.nbt = NBT.parseNBT(snbt);
    }
    public BlockData(ItemStack item,Location location){
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.nbt = NBT.itemStackToNBT(item);
    }

    public BlockData(String key){
        String[] array =key.split(",");
        this.world = array[1];
        this.x = Double.parseDouble(array[2]);
        this.y = Double.parseDouble(array[3]);
        this.z = Double.parseDouble(array[4]);
        this.nbt = NBT.parseNBT(BlockDataSaver.getDatabase().get(key));
    }

    public String getWorld() {
        return world;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    public ReadWriteNBT getNbt() {
        return nbt;
    }

    public ItemStack getItem(){
        return NBT.itemStackFromNBT(nbt);
    }

    public String getNBTString(){
        return nbt.toString();
    }

    public void save(){
        BlockDataSaver.getDatabase().put(buildKey(),getNBTString());
    }

    public void remove(){
        BlockDataSaver.getDatabase().delete(buildKey());
    }

    private String buildKey(){
        String p = "1205-";
        if(BlockDataSaver.is1_20_5Plus()) p = "1205+";
        return p+","+world+","+x+","+y+","+z;
    }

    @Override
    public String toString(){
        return buildKey();
    }
}
