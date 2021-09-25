package me.davidml16.aparkour.events;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.Parkour;
import me.davidml16.aparkour.data.ParkourSession;
import me.davidml16.aparkour.handlers.ParkourHandler;
import me.davidml16.aparkour.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class Event_PlateEnd implements Listener {

	private final Main main;
	public Event_PlateEnd(Main main) {
		this.main = main;
	}

	private final List<Player> cooldown = new ArrayList<Player>();

	@EventHandler
	public void Plate(PlayerInteractEvent e) {
		final Player p = e.getPlayer();
		Action action = e.getAction();

		if (action == Action.PHYSICAL) {
			if (e.getClickedBlock().getType() == XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE.parseMaterial()) {

				Parkour parkour = main.getParkourHandler().getParkourByLocation(e.getClickedBlock().getLocation());

				if (parkour == null) return;

				e.setCancelled(true);

				ParkourSession session = main.getSessionHandler().getSession(p);

				if (e.getClickedBlock().getLocation().equals(parkour.getEnd().getLocation())) {
					if (session == null) return;
					if (parkour != session.getParkour()) return;

					if (main.getTimerManager().hasPlayerTimer(p)) {
						if(parkour.getCheckpoints().size() == 0 || session.getLastCheckpoint() == (parkour.getCheckpoints().size() - 1)) {
							ParkourHandler.finishParkour(null, p);
						} else {
							if (!cooldown.contains(p)) {
								cooldown.add(p);
								String message = main.getLanguageHandler().getMessage("Messages.NeedCheckpoint");
								if(message.length() > 0)
									p.sendMessage(message);
								Sounds.playSound(p, p.getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
								Bukkit.getScheduler().runTaskLaterAsynchronously(main, () -> cooldown.remove(p), 40);
							}
						}

					}
				}
			}
		}
	}
}
