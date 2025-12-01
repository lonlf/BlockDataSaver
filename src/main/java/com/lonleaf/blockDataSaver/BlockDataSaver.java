package com.lonleaf.blockDataSaver;

import com.lonleaf.blockDataSaver.data.DataManager;
import com.lonleaf.blockDataSaver.database.Database;
import com.lonleaf.blockDataSaver.listener.Listeners;
import de.tr7zw.nbtapi.utils.DataFixerUtil;
import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

//import javax.xml.crypto.Data;
import java.io.File;

public final class BlockDataSaver extends JavaPlugin implements Listener {

    private static JavaPlugin instance;
    private static FileConfiguration config;
    private static Database database;

    private static final boolean is1_20_5plus = DataFixerUtil.getCurrentVersion() >= DataFixerUtil.VERSION1_20_5;


    @Override
    public void onEnable() {
        var plugin = Bukkit.getPluginManager().getPlugin("NBTAPI");
        if (plugin==null||!plugin.isEnabled()){
            this.getLogger().info(ChatColor.GREEN + "Required dependency 'NBTAPI' not found.Please install it!");
            onDisable();
        }
//        String version = Bukkit.getVersion();
        instance = this;
        config = getConfig();
        // make sure data file exists.
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        var database = new File(getDataFolder(), "database.db");
        if (!database.exists()) {
            saveResource("database.db", false);  // 复制默认的 database.db 文件到插件目录
        }
        Bukkit.getPluginManager().registerEvents(new Listeners(), this);
//        if(version.contains("1.20")){
//            is_1_20_5plus = version.contains("1.20.5") ||
//                    version.contains("1.20.6");
//        }else is_1_20_5plus = version.contains("1.21") ||
//                version.contains("1.22");
        BlockDataSaver.database = new Database();
        BlockDataSaver.database.initialize();
        DataManager.init();
        DataManager.dataTransVersion();
        this.getLogger().info(ChatColor.GREEN + "BlockDataSaver Loaded.");
    }

    public static JavaPlugin getInstance(){
        return instance;
    }

    public static FileConfiguration getPluginConfig(){
        return config;
    }

    public static Database getDatabase(){
        return database;
    }

    public static boolean is1_20_5Plus(){
        return is1_20_5plus;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(database!=null) {
            database.close();
        }
        this.getLogger().info(ChatColor.GREEN + "BlockDataSaver disabled.");
    }
}
