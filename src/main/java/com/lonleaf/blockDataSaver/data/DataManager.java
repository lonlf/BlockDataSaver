package com.lonleaf.blockDataSaver.data;

import com.lonleaf.blockDataSaver.BlockDataSaver;
import com.lonleaf.blockDataSaver.database.Database;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.utils.DataFixerUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

public class DataManager {
    private static final boolean is1_20_5plus = BlockDataSaver.is1_20_5Plus();
    private static final Database database = BlockDataSaver.getDatabase();

    private static final HashSet<String> dataKeySet = new HashSet<>();

    public static void init(){
        dataTransVersion();

        dataKeySet.addAll(database.getAllKeys());
    }

    public static void dataTransVersion(){
        for(var k: database.getAllKeys()){
            var v = database.get(k);
            String[] array =k.split(",");
            var version = array[0].equals("1205+");
            if(!(is1_20_5plus && version)){
                ReadWriteNBT nbt = NBT.parseNBT(v); // 解析NBT
                try{
                    if (is1_20_5plus) {//旧物品
                        DataFixerUtil.fixUpItemData(nbt, DataFixerUtil.VERSION1_12_2, DataFixerUtil.VERSION1_20_5); // 升级到1.20.5格式
                    } else {//新物品，旧版本
                        DataFixerUtil.fixUpItemData(nbt, DataFixerUtil.VERSION1_20_5, DataFixerUtil.getCurrentVersion()); // 降级到当前版本
                        String[] a =k.split(",");
                        BlockDataSaver.getInstance().getLogger().info("The block located at world:"+a[1]+" X:"+a[2]+" Y:"+a[3]+" Z:"+a[4]+" has been downgraded from version 1.20.5+ to the current version. Please verify the existence of this block yourself.");
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                database.delete(k);
                database.put(changeKeyVersion(k),nbt.toString());
            }
        }
    }

    private static String changeKeyVersion(String key){
        String[] array =key.split(",");
        String p = "1205-";
        if(is1_20_5plus) p = "1205+";
        return p+","+array[1]+","+array[2]+","+array[3]+","+array[4];
    }

    public static boolean checkIfSameBlockMaterial(ItemStack dropped, ItemStack saved){
        var nbt = NBT.itemStackToNBT(dropped);
        nbt.getString("id");
        var nbt_ = NBT.itemStackToNBT(saved);
        nbt_.getString("id");
        return nbt.equals(nbt_);
    }

    public static boolean contains(String k){
        return dataKeySet.contains(k);
    }

    public static boolean contains(BlockState blockState){
        return dataKeySet.contains(buildKey(blockState.getLocation()));
    }

    public static boolean contains(Location location){
        return dataKeySet.contains(buildKey(location));
    }

    public static boolean isModifiedBlock(ItemStack item){
        var cleanOne = new ItemStack(item);
        cleanOne.setItemMeta(null);
        return !item.isSimilar(cleanOne);
    }

    public static void saveBlock(ItemStack item,Location location){
        var data = new BlockData(item,location);
        data.save();
        dataKeySet.add(data.toString());
    }

    /**
     * get block item and delete data in database
     * @param location of block
     * @return null if no block in database
     */
    public static ItemStack withdrawBlock(Location location){
        var k = buildKey(location);
        BlockData data = null;
        if(contains(k)) {
            data = new BlockData(k);
            dataKeySet.remove(k);
            data.remove();
            return data.getItem();
        }else return null;

    }

    public static void removeBlock(Location location){
        var k = buildKey(location);
        BlockData data = null;
        if(contains(k)) {
            data = new BlockData(k);
            dataKeySet.remove(k);
            data.remove();
        }
    }

    public static ItemStack getBlock(Location location){
        var k = buildKey(location);
        BlockData data = null;
        if(contains(k)) {
            data = new BlockData(k);
            return data.getItem();
        }else return null;
    }

    private static String buildKey(Location location){
        String p = "1205-";
        if(BlockDataSaver.is1_20_5Plus()) p = "1205+";
        return p+","+location.getWorld().getName()+","+location.getX()+","+location.getY()+","+location.getZ();
    }
}
