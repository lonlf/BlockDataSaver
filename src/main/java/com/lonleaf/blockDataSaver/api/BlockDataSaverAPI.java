package com.lonleaf.blockDataSaver.api;

import com.lonleaf.blockDataSaver.BlockDataSaver;
import com.lonleaf.blockDataSaver.data.DataManager;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

/**
 * BlockDataSaver 对外公共 API，供其他插件调用。
 * <p>
 * 传入方块坐标，获取该方块被保存的方块数据（Spigot 原生类）：
 * <ul>
 *     <li>{@link #getBlockData(Location)} 返回 {@link org.bukkit.block.data.BlockData}</li>
 *     <li>{@link #getItem(Location)} 返回保存的完整 {@link ItemStack}（包含全部 NBT）</li>
 * </ul>
 * 注意：请在服务器主线程中调用（内部使用 SQLite 数据库，非线程安全）。
 */
public final class BlockDataSaverAPI {

    private BlockDataSaverAPI() {
    }

    // ==================== 查询 ====================

    /**
     * 判断指定位置是否保存了方块数据。
     *
     * @param location 方块坐标
     * @return true 表示存在
     */
    public static boolean hasData(Location location) {
        return location != null && location.getWorld() != null && DataManager.contains(location);
    }

    public static boolean hasData(Block block) {
        return block != null && hasData(block.getLocation());
    }

    /**
     * 获取指定位置保存的完整物品（包含全部 NBT 数据）。
     *
     * @param location 方块坐标
     * @return 保存的 ItemStack，无数据时返回 null
     */
    public static ItemStack getItem(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return DataManager.getBlock(location);
    }

    public static ItemStack getItem(Block block) {
        return block == null ? null : getItem(block.getLocation());
    }

    /**
     * 获取指定位置保存的方块数据（Spigot 原生 {@link BlockData}）。
     *
     * @param location 方块坐标
     * @return 对应的方块数据，无数据时返回 null
     */
    public static BlockData getBlockData(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        ItemStack item = DataManager.getBlock(location);
        return item == null ? null : toBlockData(item);
    }

    public static BlockData getBlockData(Block block) {
        return block == null ? null : getBlockData(block.getLocation());
    }

    // ==================== 写入 ====================

    /**
     * 保存物品数据到指定位置。
     *
     * @param item     要保存的物品
     * @param location 方块坐标
     */
    public static void saveBlock(ItemStack item, Location location) {
        if (item == null || location == null || location.getWorld() == null) {
            return;
        }
        DataManager.saveBlock(item, location);
    }

    public static void saveBlock(ItemStack item, Block block) {
        if (block != null) {
            saveBlock(item, block.getLocation());
        }
    }

    /**
     * 删除指定位置保存的方块数据。
     *
     * @param location 方块坐标
     */
    public static void removeBlock(Location location) {
        if (location != null && location.getWorld() != null) {
            DataManager.removeBlock(location);
        }
    }

    public static void removeBlock(Block block) {
        if (block != null) {
            removeBlock(block.getLocation());
        }
    }

    /**
     * 取出指定位置的物品并删除保存的数据。
     *
     * @param location 方块坐标
     * @return 保存的 ItemStack，无数据时返回 null
     */
    public static ItemStack withdrawBlock(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return DataManager.withdrawBlock(location);
    }

    public static ItemStack withdrawBlock(Block block) {
        return block == null ? null : withdrawBlock(block.getLocation());
    }

    // ==================== 内部转换 ====================

    /**
     * 将保存的物品转换为 Spigot 方块数据。
     * 优先取物品材质对应的默认方块数据，若物品 NBT 中带有方块状态属性（如 1.20.5+ 的
     * {@code minecraft:block_state} 组件），则一并应用。
     */
    private static BlockData toBlockData(ItemStack item) {
        BlockData data;
        try {
            data = Bukkit.createBlockData(item.getType());
        } catch (IllegalArgumentException e) {
            return null;
        }
        ReadWriteNBT stateNbt = getBlockStateNbt(item);
        if (stateNbt == null || stateNbt.getKeys().isEmpty()) {
            return data;
        }
        // 根据物品 NBT 中的方块状态属性构造方块数据字符串，例如 minecraft:oak_stairs[facing=north,half=bottom]
        StringBuilder sb = new StringBuilder(item.getType().getKey().toString()).append('[');
        for (String key : stateNbt.getKeys()) {
            sb.append(key).append('=').append(stateNbt.getString(key)).append(',');
        }
        sb.setLength(sb.length() - 1);
        sb.append(']');
        try {
            return Bukkit.createBlockData(sb.toString());
        } catch (IllegalArgumentException e) {
            return data;
        }
    }

    /**
     * 从物品 NBT 中提取方块状态属性（兼容 1.20.5+ 与旧版本格式）。
     */
    private static ReadWriteNBT getBlockStateNbt(ItemStack item) {
        ReadWriteNBT itemNbt = NBT.itemStackToNBT(item);
        if (BlockDataSaver.is1_20_5Plus()) {
            ReadWriteNBT components = itemNbt.getCompound("components");
            return components == null ? null : components.getCompound("minecraft:block_state");
        }
        ReadWriteNBT tag = itemNbt.getCompound("tag");
        return tag == null ? null : tag.getCompound("BlockStateTag");
    }
}
