package me.macitron3000.brogolem;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.logging.Logger;

public class SnowChadListener implements Listener {
    private final BroGolem plugin;

    public SnowChadListener(BroGolem p) {
        this.plugin = p;
    }

    // Helper function for validStructure. Checks whether an individual block,
    // given an offset from another (jack o' lantern), is a snow block.
    private boolean isSnowBlock(Block center, int xOffset, int yOffset, int zOffset) {
        return center.getRelative(xOffset, yOffset, zOffset).getType() == Material.SNOW_BLOCK;
    }

    // Super bare-bones class that holds the result boolean and
    // axis alignment char. Basically a struct.
    private class ResultPair {
        public boolean result;
        public char alignment;

        public ResultPair(char align) {
            this.result = true;
            this.alignment = align;
        }

        public ResultPair() {
            this.result = false;
            this.alignment = '0';
        }
    }

    // Takes the jack o' lantern block, and checks if the snow blocks around
    // it form a SnowChad. Returns whether the structure is indeed a SnowChad,
    // and a single character 'x' or 'z' for on which axis it's aligned.
    private ResultPair validSnowchad(Block b) {
        // Block underneath, just check here so we can make the other checks
        // nice and symmetric.
        if (!isSnowBlock(b, 0, -1, 0)) {
            return new ResultPair();
        }

        // Aligned along Z-axis
        boolean zValid =
                isSnowBlock(b, 3, 0, 0) && isSnowBlock(b, -3, 0, 0) &&
                isSnowBlock(b, 3, -1, 0) && isSnowBlock(b, -3, -1, 0) &&
                isSnowBlock(b, 2, -1, 0) && isSnowBlock(b, -2, -1, 0) &&
                isSnowBlock(b, 1, -1, 0) && isSnowBlock(b, -1, -1, 0) &&
                isSnowBlock(b, 1, -2, 0) && isSnowBlock(b, -1, -2, 0) &&
                isSnowBlock(b, 1, -3, 0) && isSnowBlock(b, -1, -3, 0);
        if (zValid) {
            this.plugin.getLogger().info("SnowChad found! (z-axis)");
            return new ResultPair('z');
        }

        // Aligned along X-axis
        boolean xValid =
                isSnowBlock(b, 0, 0, 3) && isSnowBlock(b, 0, 0, -3) &&
                isSnowBlock(b, 0, -1, 3) && isSnowBlock(b, 0, -1, -3) &&
                isSnowBlock(b, 0, -1, 2) && isSnowBlock(b, 0, -1, -2) &&
                isSnowBlock(b, 0, -1, 1) && isSnowBlock(b, 0, -1, -1) &&
                isSnowBlock(b, 0, -2, 1) && isSnowBlock(b, 0, -2, -1) &&
                isSnowBlock(b, 0, -3, 1) && isSnowBlock(b, 0, -3, -1);

        // If not aligned along X-axis, we don't have a snowman.
        if (xValid) {
            this.plugin.getLogger().info("SnowChad found! (x-axis");
            return new ResultPair('x');
        }
        return new ResultPair();
    }

    @EventHandler
    public void onSnowchadCreate(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();

        // If the player can't place blocks here (for some reason), or if the
        // block placed isn't a jack o' lantern, we have nothing to do here.
        if (!event.canBuild() || placed.getType() != Material.JACK_O_LANTERN) {
            return;
        }

        // Then we know the block placed was a jack o' lantern and the player
        // can build, so go about checking if the structure is the MegaChad format.
        ResultPair res = validSnowchad(placed);
        if (!res.result) {
            return;
        }
        char align = res.alignment;

        // Now that we've found a Snowchad structure, remove all the blocks in
        // that space and spawn a snowman.
        // Explicitly remove jack o' lantern and block below, for symmetry.
        placed.getRelative(0, -1, 0).setType(Material.AIR);
        placed.setType(Material.AIR);
        for (int y = 0; y <= 3; y++) {
            for (int i = 1; i <= 3; i++) {
                int xOffset = 0;
                int zOffset = 0;
                if (align == 'x') {
                    zOffset = -i;
                } else {
                    xOffset = -i;
                }

                placed.getRelative(xOffset, -y, zOffset).setType(Material.AIR);
                placed.getRelative(-xOffset, -y, -zOffset).setType(Material.AIR);
            }
        }

        // And spawn the actual Snowchad!
        // TODO: make it a chad
        Snowman chad = (Snowman) placed.getWorld().spawnEntity(
                placed.getRelative(0, -3, 0).getLocation(),
                EntityType.SNOWMAN);
        //chad.setDerp(true);
        chad.setCustomName("SnowChad");
        chad.setCustomNameVisible(true);

        Logger log = this.plugin.getLogger();

        AttributeInstance att = chad.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (att == null) {
            log.warning("Getting max health attribute on newly spawned Snowchad returned null.");
            return;
        }
        double health = 0;
        switch (chad.getWorld().getDifficulty()) {
            case EASY:
                health = 75;
                break;
            case NORMAL:
                health = 150;
                break;
            case HARD:
                health = 300;
                break;
        }
        att.setBaseValue(health);
        chad.setHealth(health);

        att = chad.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (att == null) {
            log.warning("Getting movement speed attribute on newly spawned Snowchad returned null.");
            return;
        }
        att.setBaseValue(att.getValue() * 2);

        att = chad.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
        if (att == null) {
            log.warning("Getting follow range attribute on newly spawned Snowchad returned null.");
            return;
        }
        att.setBaseValue(att.getValue() * 5);

        this.plugin.snowmen.add(chad);
    }

    @EventHandler
    public void onExplosionDamage(EntityDamageEvent event) {
        // Only negating explosion damage for the Snowmen in BroGolem's Set
        if (event.getEntityType() != EntityType.SNOWMAN) {
            return;
        }
        // Only concerned with explosions
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
              && cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }

        Snowman chad = (Snowman) event.getEntity();
        // If our chad is in main plugin's snowmen, cancel explosion damage.
        if (this.plugin.snowmen.contains(chad)) {
            event.setCancelled(true);
        }
    }
}
