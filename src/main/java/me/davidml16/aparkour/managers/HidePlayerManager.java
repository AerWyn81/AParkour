package me.davidml16.aparkour.managers;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class HidePlayerManager {
    private final HashMap<String, PlayerState> playerStates;
    private final HashMap<UUID, Integer> cooldowns;

    private final Main main;
    private int configCooldown;
    private final boolean isLegacy;

    public HidePlayerManager(Main main, boolean isLegacy) {
        this.main = main;
        this.isLegacy = isLegacy;

        this.configCooldown = main.getConfig().getInt("HideItemCooldown");

        playerStates = new HashMap<>();
        cooldowns = new HashMap<>();

        if (configCooldown == -1)
            configCooldown = 0;
    }

    public PlayerState getPlayerState(Player p) {
        PlayerState playerState = playerStates.get(p.getPlayer().getName());
        return playerState == null ? PlayerState.SHOWN : playerState;
    }

    public Integer getCooldown (UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return 0;
        return configCooldown - (((int) System.currentTimeMillis() / 1000) - cooldowns.get(uuid));
    }

    public boolean isPlayerInCooldown(UUID uuid) {
        if (cooldowns.containsKey(uuid))
            return (((int) System.currentTimeMillis() / 1000) - cooldowns.get(uuid) < configCooldown);

        return false;
    }

    public void setPlayerState(Player player, PlayerState playerState) {
        if (playerState == PlayerState.HIDDEN) {
            hide(player);
            player.sendMessage(main.getLanguageHandler().getMessage("Messages.HideItemHide"));
        }
        else {
            show(player);
            player.sendMessage(main.getLanguageHandler().getMessage("Messages.HideItemShow"));
        }

        playerStates.put(player.getName(), playerState);
        cooldowns.put(player.getUniqueId(), (int)System.currentTimeMillis()/1000);
    }

    private void hide(Player player) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.getUniqueId() != player.getUniqueId()) {
                if (isLegacy)
                    player.hidePlayer(onlinePlayer);
                else
                    player.hidePlayer(main, onlinePlayer);
            }
        });
    }

    private void show(Player player) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.getUniqueId() != player.getUniqueId() && (!isVanished(onlinePlayer) || player.hasPermission("aparkour.seevanished")))
                if (isLegacy)
                    player.showPlayer(onlinePlayer);
                else
                    player.showPlayer(main, onlinePlayer);
        });
    }

    public ItemStack getItem(Player p) {
        return getPlayerState(p) == PlayerState.SHOWN ? main.getParkourItems().getShowItem() : main.getParkourItems().getHideItem();
    }

    private boolean isVanished(final Player player) {
        for (final MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}
