package me.davidml16.aparkour.handlers;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.api.events.ParkourCheckpointEvent;
import me.davidml16.aparkour.api.events.ParkourReturnEvent;
import me.davidml16.aparkour.data.Parkour;
import me.davidml16.aparkour.data.ParkourSession;
import me.davidml16.aparkour.data.Plate;
import me.davidml16.aparkour.managers.ColorManager;
import me.davidml16.aparkour.utils.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CheckpointsHandler {

    private static Main main;

    public CheckpointsHandler(Main main) {
        this.main = main;
    }

    public void loadCheckpoints(Parkour parkour, FileConfiguration config) {
        List<Plate> checkpoints = new ArrayList<Plate>();
        List<Location> checkpointLocations = new ArrayList<Location>();
        if (config.contains("parkour.checkpoints")) {
            if (config.getConfigurationSection("parkour.checkpoints") != null) {
                for (String id : config.getConfigurationSection("parkour.checkpoints").getKeys(false)) {
                    Location loc = (Location) config.get("parkour.checkpoints." + Integer.parseInt(id) + ".location");
                    Plate plate = new Plate(loc);
                    plate.setId(Integer.parseInt(id));
                    plate.setPlayerPitch((float) config.getDouble("parkour.checkpoints." + Integer.parseInt(id) + ".playerPitch"));
                    plate.setPlayerYaw((float) config.getDouble("parkour.checkpoints." + Integer.parseInt(id) + ".playerYaw"));
                    plate.setCheckpointCommands(config.getStringList("parkour.checkpoints." + Integer.parseInt(id) + ".commands"));
                    checkpoints.add(plate);
                    checkpointLocations.add(loc);
                }
                parkour.setCheckpoints(checkpoints);
                parkour.setCheckpointLocations(checkpointLocations);
            }
        }
    }


    public static boolean teleportToPreviousCheckpoint(CommandSender sender, Player p) {
        if (main.getTimerManager().hasPlayerTimer(p)) {
            ParkourSession session = main.getSessionHandler().getSession(p);

            if (session.getLastCheckpoint() < 0) {
                String message = main.getLanguageHandler().getMessage("Messages.Return");
                if(message.length() > 0)
                    p.sendMessage(message);

                if(main.isKickParkourOnFail()) {
                    main.getParkourHandler().resetPlayer(p);
                    p.teleport(session.getParkour().getSpawn());
                } else {
                    Location loc = session.getParkour().getStart().getLocation().clone();
                    loc.add(0.5, 0, 0.5);
                    loc.setPitch(0.0f);
                    loc.setYaw(0.0f);
                    p.teleport(loc);
                }

                Bukkit.getPluginManager().callEvent(new ParkourReturnEvent(p, session.getParkour()));
            } else if (session.getLastCheckpoint() >= 0) {
                p.teleport(session.getLastCheckpointLocation());
                String message = main.getLanguageHandler().getMessage("Messages.ReturnCheckpoint");
                if(message.length() > 0)
                    p.sendMessage(message.replaceAll("%checkpoint%", Integer.toString(session.getLastCheckpoint() + 1)));

                if (sender instanceof Player && !sender.getName().equalsIgnoreCase(p.getName())) {
                    message = main.getLanguageHandler().getMessage("Messages.ReturnCheckpointForPlayer")
                            .replaceAll("%player%", p.getName())
                            .replaceAll("%checkpoint%", Integer.toString(session.getLastCheckpoint() + 1));
                    if(message.length() > 0) {
                        sender.sendMessage((message));
                    }
                }

                Bukkit.getPluginManager().callEvent(new ParkourCheckpointEvent(p, session.getParkour()));
            }

            main.getSoundUtil().playReturn(p);

            p.setNoDamageTicks(40);
            return true;
        } else {
            String message = main.getLanguageHandler().getMessage("Messages.NotInParkour");
            if(message.length() > 0)
                p.sendMessage(message);
            return false;
        }
    }

    public static boolean teleportToNextCheckpoint(CommandSender sender, Player p) {
        if (main.getTimerManager().hasPlayerTimer(p)) {
            ParkourSession session = main.getSessionHandler().getSession(p);
            List<Plate> checkpoints = session.getParkour().getCheckpoints();

            if (checkpoints.size() == 0 || session.getLastCheckpoint() == session.getParkour().getCheckpoints().size() - 1) {
                Main.log.sendMessage(ColorManager.translate("&cCannot teleport " + p.getName() + " to next checkpoint because there's not next checkpoint available !"));
                return false;
            }

            Location loc = session.getNextCheckpointLocation();
            loc.add(0.5, 0, 0.5);
            p.teleport(loc);

            String message = main.getLanguageHandler().getMessage("Messages.NextCheckpoint");
            if(message.length() > 0)
                p.sendMessage(message.replaceAll("%checkpoint%", Integer.toString(session.getLastCheckpoint() + 2))
                        .replaceAll("%time%", main.getTimerManager().millisToString(main.getLanguageHandler().getMessage("Timer.Formats.ParkourTimer"),session.getLiveTime())));

            if (sender instanceof Player && !sender.getName().equalsIgnoreCase(p.getName())) {
                message = main.getLanguageHandler().getMessage("Messages.NextCheckpointForPlayer")
                        .replaceAll("%player%", p.getName())
                        .replaceAll("%checkpoint%", Integer.toString(session.getLastCheckpoint() + 2));
                if(message.length() > 0) {
                    sender.sendMessage((message));
                }
            }

            return true;
        } else {
            String message = main.getLanguageHandler().getMessage("Messages.NotInParkour");
            if(message.length() > 0)
                p.sendMessage(message);
            return false;
        }
    }
}
