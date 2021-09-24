package me.davidml16.aparkour.api.events;

import me.davidml16.aparkour.data.Parkour;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourReturnEvent extends Event {

    public static final HandlerList handlers = new HandlerList();
    private final Player p;
    private final Parkour parkour;

    public ParkourReturnEvent(Player p, Parkour parkour) {
        this.p = p;
        this.parkour = parkour;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.p;
    }

    public Parkour getParkour() {
        return parkour;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}