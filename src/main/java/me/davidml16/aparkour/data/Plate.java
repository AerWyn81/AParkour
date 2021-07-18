package me.davidml16.aparkour.data;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Location;

import java.util.List;

public class Plate {

    private int id;
    private Location location;

    private Hologram hologram;
    private boolean hologramEnabled;
    private double hologramDistance;

    private float playerPitch;
    private float playerYaw;

    private List<String> checkpointCommands;

    public Plate(Location location) {
        this.location = location;
        this.hologram = null;
        this.hologramEnabled = false;
        this.hologramDistance = 2.5;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public boolean isHologramEnabled() {
        return hologramEnabled;
    }

    public void setHologramEnabled(boolean hologramEnabled) {
        this.hologramEnabled = hologramEnabled;
    }

    public double getHologramDistance() {
        return hologramDistance;
    }

    public void setHologramDistance(double hologramDistance) {
        this.hologramDistance = hologramDistance;
    }

    public float getPlayerPitch() {
        return playerPitch;
    }

    public void setPlayerPitch(float playerPitch) {
        this.playerPitch = playerPitch;
    }

    public float getPlayerYaw() {
        return playerYaw;
    }

    public void setPlayerYaw(float playerYaw) {
        this.playerYaw = playerYaw;
    }

    public List<String> getCheckpointCommands() { return checkpointCommands; }

    public void setCheckpointCommands(List<String> checkpointCommands) {
        this.checkpointCommands = checkpointCommands;
    }

    @Override
    public String toString() {
        return "Plate{" +
                "location=" + location +
                ", hologram=" + hologram +
                ", hologramEnabled=" + hologramEnabled +
                ", hologramDistance=" + hologramDistance +
                '}';
    }
}
