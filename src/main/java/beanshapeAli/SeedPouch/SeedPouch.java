package beanshapeAli.SeedPouch;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

public class SeedPouch extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getLogger().info("Seed Pouch enabled.");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Seed Pouch disabled.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInOffHand();

        if (!isPouch(player.getInventory().getItemInMainHand())) {
            return;
        }

        assert clickedBlock != null;
        if (clickedBlock.getType() == Material.FARMLAND && isSeed(heldItem)) {
            player.playSound(player.getLocation(), Sound.ITEM_CROP_PLANT, 1.0f, 1.0f);
            int seedsUsed = plantAoE(clickedBlock, heldItem.getType(), player);
            player.swingMainHand();
            consumeSeeds(player, seedsUsed);
        }

    }

    private boolean isPouch(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        return item.getPersistentDataContainer().has(NamespacedKey.fromString("newcoming:plantaoe"));
    }

    private boolean isSeed(ItemStack heldItem) {
        Material heldType = heldItem.getType();
        return heldType == Material.WHEAT_SEEDS
                || heldType == Material.BEETROOT_SEEDS
                || heldType == Material.POTATO
                || heldType == Material.CARROT
                || heldType == Material.PUMPKIN_SEEDS
                || heldType == Material.MELON_SEEDS
                || heldType == Material.TORCHFLOWER_SEEDS
                || heldType == Material.PITCHER_POD;
    }

    private Material seedMap(Material seedType) {
        return switch (seedType) {
            case WHEAT_SEEDS -> Material.WHEAT;
            case BEETROOT_SEEDS -> Material.BEETROOTS;
            case POTATO -> Material.POTATOES;
            case CARROT -> Material.CARROTS;
            case PUMPKIN_SEEDS -> Material.PUMPKIN_STEM;
            case MELON_SEEDS -> Material.MELON_STEM;
            case TORCHFLOWER_SEEDS -> Material.TORCHFLOWER_CROP;
            case PITCHER_POD -> Material.PITCHER_CROP;
            default -> Material.AIR;
        };
    }

    private int plantAoE(Block center, Material seedType, Player player) {
        int planted = 0;
        int seedsHeld = player.getInventory().getItemInOffHand().getAmount();

        int radius = 2;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block blockBelow = center.getRelative(x, 0, z);
                Block plantUpon = center.getRelative(x, 1, z);

                if (blockBelow.getType() == Material.FARMLAND && plantUpon.getType() == Material.AIR && seedsHeld > planted) {
                    plantUpon.setType(seedMap(seedType));
                    plantUpon.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, plantUpon.getLocation(), 2);
                    planted++;
                }
            }
        }

        return planted;
    }

    private void consumeSeeds(Player player, int seedsUsed) {
        if (!player.getGameMode().name().equalsIgnoreCase("CREATIVE")) {

            ItemStack heldItem = player.getInventory().getItemInOffHand();

            if (heldItem.getAmount() <= seedsUsed) {
                player.getInventory().setItemInOffHand(null);
            } else {
                heldItem.setAmount(heldItem.getAmount() - seedsUsed);
            }
        }
    }
}