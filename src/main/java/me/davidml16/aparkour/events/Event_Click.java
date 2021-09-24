package me.davidml16.aparkour.events;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.api.events.ParkourReturnEvent;
import me.davidml16.aparkour.data.ParkourSession;
import me.davidml16.aparkour.data.Plate;
import me.davidml16.aparkour.data.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class Event_Click implements Listener {

    private final Main main;
    public Event_Click(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onClicker(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (main.getTimerManager().hasPlayerTimer(p)) {
            if (!Bukkit.getVersion().contains("1.8")) {
                if (e.getHand() == EquipmentSlot.OFF_HAND) return;
            }

            e.setCancelled(true);

            if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;

            ItemStack item = p.getItemInHand();
            if (item == null)
                return;

            if (item.equals(main.getParkourItems().getRestartItem())) {
                if (main.getSessionHandler().getSession(p) != null) {
                    main.getConfirmationGUI().open(p, main.getSessionHandler().getSession(p).getParkour().getId());
                }

                return;
            }

            if (item.equals(main.getParkourItems().getCheckpointItem())) {
                if (main.getSessionHandler().getSession(p) != null) {
                    ParkourSession session = main.getSessionHandler().getSession(p);

                    if (session.getLastCheckpoint() < 0) {
                        String message = main.getLanguageHandler().getMessage("Messages.Return");
                        if (message.length() > 0)
                            p.sendMessage(message);

                        if (main.isKickParkourOnFail()) {
                            main.getParkourHandler().resetPlayer(p);
                            p.getLocation().setPitch(session.getParkour().getSpawn().getPitch());
                            p.getLocation().setYaw(session.getParkour().getSpawn().getYaw());
                            p.teleport(session.getParkour().getSpawn());
                        } else {
                            Location loc = session.getParkour().getStart().getLocation().clone();
                            loc.add(0.5, 0, 0.5);
                            p.getLocation().setPitch(loc.getPitch());
                            p.getLocation().setYaw(loc.getYaw());
                            p.teleport(loc);
                        }
                    } else if (session.getLastCheckpoint() >= 0) {
                        Plate lastCheckpoint = session.getParkour().getCheckpoints().get(session.getLastCheckpoint());

                        p.getLocation().setPitch(lastCheckpoint.getLocation().getPitch());
                        p.getLocation().setYaw(lastCheckpoint.getLocation().getYaw());
                        p.teleport(session.getLastCheckpointLocation());

                        String message = main.getLanguageHandler().getMessage("Messages.ReturnCheckpoint");
                        if (message.length() > 0)
                            p.sendMessage(message.replaceAll("%checkpoint%", Integer.toString(session.getLastCheckpoint() + 1)));
                    }

                    main.getSoundUtil().playReturn(p);

                    p.setFallDistance(0);
                    p.setNoDamageTicks(40);

                    Bukkit.getPluginManager().callEvent(new ParkourReturnEvent(p, session.getParkour()));
                }

                return;
            }

            if (item.equals(main.getParkourItems().getShowItem()) || item.equals(main.getParkourItems().getHideItem())) {
                //Delay needed, main hand fire twice...
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    if (main.getHidePlayerManager().isPlayerInCooldown(p.getUniqueId())) {
                        p.sendMessage(main.getLanguageHandler().getMessage("Messages.HideItemCooldown").replaceAll("%cooldown%", String.valueOf(main.getHidePlayerManager().getCooldown(p.getUniqueId()))));
                        return;
                    }

                    main.getHidePlayerManager().setPlayerState(p, main.getHidePlayerManager().getPlayerState(p) == PlayerState.SHOWN ? PlayerState.HIDDEN : PlayerState.SHOWN);
                    p.getInventory().setItem(main.getConfig().getInt("Items.HideItem.InventorySlot") - 1, main.getHidePlayerManager().getItem(p));
                }, 1L);
            }
        }
    }
}