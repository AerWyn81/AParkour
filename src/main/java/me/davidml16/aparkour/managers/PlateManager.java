package me.davidml16.aparkour.managers;

import me.davidml16.aparkour.data.Parkour;
import me.davidml16.aparkour.utils.XMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class PlateManager {

    public void loadPlates(Parkour parkour) {
        try {
            Block start = parkour.getStart().getLocation().getWorld().getBlockAt(parkour.getStart().getLocation());
            if (start.getType() != XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE.parseMaterial()) {
                start.setType(XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE.parseMaterial());
            }
        } catch (NullPointerException ignored) {}

        try {
            Block end = parkour.getEnd().getLocation().getWorld().getBlockAt(parkour.getEnd().getLocation());
            if(end.getType() != XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE.parseMaterial()) {
                end.setType(XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE.parseMaterial());
            }
        } catch (NullPointerException ignored) {}

        for(Location checkpoint : parkour.getCheckpointLocations()) {
            Block cp = checkpoint.getWorld().getBlockAt(checkpoint);
            try {
                if(cp.getType() != XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE.parseMaterial()) {
                    cp.setType(XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE.parseMaterial());
                }
            } catch (NullPointerException ignored) {}
        }
    }

}
