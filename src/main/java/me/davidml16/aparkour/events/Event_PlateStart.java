package me.davidml16.aparkour.events;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.Parkour;
import me.davidml16.aparkour.handlers.ParkourHandler;
import me.davidml16.aparkour.managers.ColorManager;
import me.davidml16.aparkour.utils.Sounds;
import me.davidml16.aparkour.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Event_PlateStart implements Listener {

    private final Main main;
    public Event_PlateStart(Main main) {
        this.main = main;
    }

    private final List<Player> cooldown = new ArrayList<Player>();

    private static final HashMap<Player, Long> ironPlateResetDenyCooldown = new HashMap<>();

    @EventHandler
    public void Plate(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        Action action = e.getAction();

        if (action == Action.PHYSICAL) {
            if (e.getClickedBlock().getType() == XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE.parseMaterial()) {

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

                        if (main.getConfig().getBoolean("FastRestart.Enabled")) {
                            if (!ironPlateResetDenyCooldown.containsKey(p)) {
                                ironPlateResetDenyCooldown.put(p, System.currentTimeMillis() + 1500);

                                if (main.getTimerManager().hasPlayerTimer(p)) {
                                    String resetting = main.getLanguageHandler().getMessage("RestartMessage.Message");
                                    if(resetting.length() > 0)
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', resetting));

                                    main.getParkourHandler().resetPlayer(p);
                                }

                                ParkourHandler.startParkour(null, p, parkour, false);
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
    }
}