package me.macitron3000.brogolem;

import org.bukkit.entity.Snowman;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class BroGolem extends JavaPlugin {
    public Set<Snowman> snowmen;

    @Override
    public void onEnable() {
        // Initialize Snowman set
        snowmen = new HashSet<>(2);

        // Register listener
        getServer().getPluginManager().registerEvents(new SnowChadListener(this), this);

        // :D
        getLogger().info("BroGolem has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BroGolem has been disabled. Thanks for using!");
    }
}
