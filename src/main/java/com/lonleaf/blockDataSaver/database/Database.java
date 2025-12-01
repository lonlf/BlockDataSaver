package com.lonleaf.blockDataSaver.database;

import com.lonleaf.blockDataSaver.BlockDataSaver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Database {
    private final JavaPlugin plugin = BlockDataSaver.getInstance();
    private Connection connection;

    private final FileConfiguration config = BlockDataSaver.getPluginConfig();

    public void initialize() {

        setupSQLite();
        createTable();

        // 每5分钟发送一次心跳（单位：ticks）
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!isConnectionValid()) {
                reconnect();
            }
        }, 6000L, 6000L); // 20 ticks = 1秒，6000 ticks = 5分钟
    }

    private void setupSQLite() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/database.db");
            plugin.getLogger().info("Connected to SQLite database.");
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to SQLite database", e);
        }
    }

    private void reconnect(){
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            setupSQLite();

        } catch (SQLException e) {
            plugin.getLogger().severe("Database reconnection failed: " + e.getMessage());
        }
    }

    public boolean isConnectionValid() {
        if (connection == null) {
            return false;
        }

        try (Statement stmt = connection.createStatement()) {
            // Ping! Pong!(
            return stmt.execute("SELECT 1");
        } catch (SQLException e) {
            plugin.getLogger().warning("Database ping failed: " + e.getMessage());
            return false;
        }
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            // 创建玩家数据表，只包含UUID、等级和经验
            String createTable = "CREATE TABLE IF NOT EXISTS block_data_saver ("
                    + "key TEXT PRIMARY KEY NOT NULL,"
                    + "value TEXT"
                    + ")";

            statement.execute(createTable);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create database table", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }

    // 添加或更新键值对
    public void put(String key, String value) {
        String sql = "INSERT OR REPLACE INTO block_data_saver(key, value) VALUES(?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    // 获取值
    public String get(String key) {
        String sql = "SELECT value FROM block_data_saver WHERE key = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        String sql = "SELECT key FROM block_data_saver"; // 没有 LIMIT

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                keys.add(rs.getString("key"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return keys;
    }

    /**
     * 高效检查键是否存在
     * @param key 要查找的键
     * @return true表示存在，false表示不存在
     */
    public boolean contains(String key) {
        String sql = "SELECT 1 FROM block_data_saver WHERE key = ? LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }


    // 删除键值对
    public void delete(String key) {
        String sql = "DELETE FROM block_data_saver WHERE key = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
