package me.davidml16.aparkour.events;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.api.events.ParkourCheckpointEvent;
import me.davidml16.aparkour.data.Parkour;
import me.davidml16.aparkour.data.ParkourSession;
import me.davidml16.aparkour.data.Plate;
import me.davidml16.aparkour.utils.Sounds;
import me.davidml16.aparkour.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class Event_PlateCheckpoint implements Listener {

	private final Main main;
	public Event_PlateCheckpoint(Main main) {
		this.main = main;
	}

	@EventHandler
	public void Plate(PlayerInteractEvent e) {
		final Player p = e.getPlayer();
		Action action = e.getAction();

		if (action == Action.PHYSICAL) {
			if (e.getClickedBlock().getType() == XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE.parseMaterial()) {

				Parkour parkour = main.getParkourHandler().getParkourByLocation(e.getClickedBlock().getLocation());

				if (parkour == null) return;

				e.setCancelled(true);

				if(parkour.getCheckpointLocations().contains(e.getClickedBlock().getLocation())) {
					if (main.getTimerManager().hasPlayerTimer(p)) {

						ParkourSession session = main.getSessionHandler().getSession(p);
						int lastCheckpointIndex = session.getLastCheckpoint() + 1;
						int nextCheckpointIndex = lastCheckpointIndex + 1;

						if (session.getLastCheckpoint() < session.getParkour().getCheckpoints().size() - 1) {
							if (e.getClickedBlock().getLocation().equals(session.getParkour().getCheckpoints().get(session.getLastCheckpoint() + 1).getLocation()) || e.getClickedBlock().getLocation().equals(session.getParkour().getCheckpoints().get(session.getNextCheckpoint()).getLocation())) {
								session.setLastCheckpoint(lastCheckpointIndex);

								if (session.getLastCheckpoint() != session.getParkour().getCheckpoints().size() - 1) {
									session.setNextCheckpoint(nextCheckpointIndex);

									Location loc = parkour.getCheckpointLocations().get(session.getNextCheckpoint()).clone();
									Plate nextCheckpoint = session.getParkour().getCheckpoints().get(session.getNextCheckpoint());

									loc.add(0.5D, 0D, 0.5D);
									loc.setPitch(nextCheckpoint.getPlayerPitch());
									loc.setYaw(nextCheckpoint.getPlayerYaw());
									session.setNextCheckpointLocation(loc);
								}

								Location loc = parkour.getCheckpointLocations().get(session.getLastCheckpoint()).clone();
								Plate lastCheckpoint = session.getParkour().getCheckpoints().get(session.getLastCheckpoint());

								loc.add(0.5D, 0D, 0.5D);
								loc.setPitch(lastCheckpoint.getPlayerPitch());
								loc.setYaw(lastCheckpoint.getPlayerYaw());
								session.setLastCheckpointLocation(loc);

								List<String> commands = lastCheckpoint.getCheckpointCommands();
								for (String command : commands) {
									main.getServer().dispatchCommand(main.getServer().getConsoleSender(), command
											.replaceAll("%player%", p.getName())
											.replaceAll("%checkpoint%", Integer.toString(lastCheckpointIndex + 1))
											.replaceAll("%time%", main.getTimerManager().millisToString(main.getLanguageHandler().getMessage("Timer.Formats.ParkourTimer"),session.getLiveTime())));
								}

								Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 10, 2);

								main.getTitleUtil().sendCheckpointTitle(p, parkour, session);

								Bukkit.getPluginManager().callEvent(new ParkourCheckpointEvent(p, parkour));
							}
						}
					}
				}
			}
		}
	}

}
