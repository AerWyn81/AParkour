package me.davidml16.aparkour.events;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.api.events.ParkourStartEvent;
import me.davidml16.aparkour.data.Parkour;
import me.davidml16.aparkour.data.ParkourSession;
import me.davidml16.aparkour.managers.ColorManager;
import me.davidml16.aparkour.utils.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Event_PlateStart implements Listener {

    private Main main;
    public Event_PlateStart(Main main) {
        this.main = main;
    }

    private List<Player> cooldown = new ArrayList<Player>();

    private static final HashMap<Player, Long> ironPlateResetDenyCooldown = new HashMap<>();

    @EventHandler
    public void Plate(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        Action action = e.getAction();

        if (action == Action.PHYSICAL) {
            if (e.getClickedBlock().getType() == Material.IRON_PLATE) {

                Parkour parkour = main.getParkourHandler().getParkourByLocation(e.getClickedBlock().getLocation());

                if (parkour == null) return;

                e.setCancelled(true);

                if (!parkour.getCheckpointLocations().contains(e.getClickedBlock().getLocation())) {
                    if (e.getClickedBlock().getLocation().equals(parkour.getStart().getLocation())) {
                        if (parkour.isPermissionRequired()) {
                            if (!main.getPlayerDataHandler().playerHasPermission(p, parkour.getPermission())) {
                                if (!cooldown.contains(p)) {
                                    cooldown.add(p);
                                    p.sendMessage(ColorManager.translate(main.getLanguageHandler().getPrefix() + " " + parkour.getPermissionMessage()));
                                    Sounds.playSound(p, p.getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
                                    Bukkit.getScheduler().runTaskLaterAsynchronously(main, () -> cooldown.remove(p), 40);
                                    return;
                                }
                                return;
                            }
                        }

                        if (!ironPlateResetDenyCooldown.containsKey(p)) {
                            ironPlateResetDenyCooldown.put(p, System.currentTimeMillis() + 1500);

                            if (main.getTimerManager().hasPlayerTimer(p)) {
                                String resetting = main.getLanguageHandler().getMessage("RestartMessage.Message");
                                if(resetting.length() > 0)
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', resetting));

                                main.getParkourHandler().resetPlayer(p);
                            }

                            startParkour(p, parkour);
                        } else {
                            if (ironPlateResetDenyCooldown.get(p) <= System.currentTimeMillis()) {
                                ironPlateResetDenyCooldown.remove(p);
                            } else {
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private void startParkour(Player p, Parkour parkour) {
        String message = main.getLanguageHandler().getMessage("Messages.Started");
        if(message.length() > 0)
            p.sendMessage(message);

        main.getSoundUtil().playStart(p);

        if (main.isParkourItemsEnabled()) {
            main.getPlayerDataHandler().savePlayerInventory(p);
            if (parkour.getCheckpoints().size() > 0) {
                p.getInventory().setItem(main.getConfig().getInt("Items.Restart.InventorySlot"), main.getParkourItems().getRestartItem());
                p.getInventory().setItem(main.getConfig().getInt("Items.Checkpoint.InventorySlot"), main.getParkourItems().getCheckpointItem());

                String state = main.getHideItem().getPlayerState().getPlayerState(p);
                boolean hasHiddenPlayers;

                if (Arrays.asList("hidden", "shown").contains(state)) {
                    hasHiddenPlayers = state.equalsIgnoreCase("hidden");
                } else {
                    hasHiddenPlayers = !main.getHideItem().getHideItemConfig().DEFAULT_SHOWN();
                }

                final ItemStack hideItem;

                if (hasHiddenPlayers) {
                    hideItem = main.getHideItem().getHideItemConfig().SHOW_ITEM();
                } else {
                    hideItem = main.getHideItem().getHideItemConfig().HIDE_ITEM();
                }

                if (hasHiddenPlayers) {
                    main.getHideItem().getPlayerState().setPlayerState(p, "hidden");
                }

                if (!main.getHideItem().getHideItemConfig().DISABLE_ITEMS()) {
                    p.getInventory().setItem(main.getHideItem().getHideItemConfig().ITEM_SLOT() - 1, hideItem);
                }
            } else {
                p.getInventory().setItem(main.getConfig().getInt("Items.Restart.InventorySlot"), main.getParkourItems().getRestartItem());
            }
        }

        p.setFlying(false);

        main.getTitleUtil().sendStartTitle(p, parkour);

        main.getSessionHandler().createSession(p, parkour);

        Bukkit.getPluginManager().callEvent(new ParkourStartEvent(p, parkour));
    }

}