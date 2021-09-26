package me.davidml16.aparkour.handlers;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.davidml16.aparkour.api.events.ParkourEndEvent;
import me.davidml16.aparkour.api.events.ParkourReturnEvent;
import me.davidml16.aparkour.api.events.ParkourStartEvent;
import me.davidml16.aparkour.data.*;
import me.davidml16.aparkour.utils.ItemBuilder;
import me.davidml16.aparkour.utils.RandomFirework;
import me.davidml16.aparkour.utils.XMaterial;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.managers.ColorManager;
import org.bukkit.permissions.Permission;

public class ParkourHandler {

	private final HashMap<String, Parkour> parkours;
	private final HashMap<String, File> parkourFiles;
	private final HashMap<String, YamlConfiguration> parkourConfigs;

	private GameMode parkourGamemode;

	private static Main main;

	public ParkourHandler(Main main) {
		this.main = main;
		this.parkours = new HashMap<>();
		this.parkourFiles = new HashMap<>();
		this.parkourConfigs = new HashMap<>();
		this.parkourGamemode = GameMode.valueOf(this.main.getConfig().getString("ParkourGamemode"));
	}

	public HashMap<String, Parkour> getParkours() {
		return parkours;
	}

	public HashMap<String, File> getParkourFiles() {
		return parkourFiles;
	}

	public HashMap<String, YamlConfiguration> getParkourConfigs() {
		return parkourConfigs;
	}

	public GameMode getParkourGamemode() {
		return parkourGamemode;
	}

	public void setParkourGamemode(GameMode parkourGamemode) {
		this.parkourGamemode = parkourGamemode;
	}

	public boolean createParkour(String id) {
		File file = new File(main.getDataFolder(), "parkours/" + id + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
				parkourFiles.put(id, file);
				parkourConfigs.put(id, YamlConfiguration.loadConfiguration(file));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean removeParkour(String id) {
		if(parkourFiles.containsKey(id) && parkourConfigs.containsKey(id)) {
			File file = parkourFiles.get(id);
			file.delete();
			parkourFiles.remove(id);
			parkourConfigs.remove(id);
			main.getDatabaseHandler().deleteParkourRows(id);
			return true;
		}
		return false;
	}

	public void saveConfig(String id) {
		try {
			File file = parkourFiles.get(id);
			if(file.exists()) {
				parkourConfigs.get(id).save(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfig(String id) {
		return parkourConfigs.get(id);
	}

	public void loadParkours() {

		parkourConfigs.clear();
		parkourFiles.clear();
		parkours.clear();

		File directory = new File(main.getDataFolder(), "parkours");
		if(!directory.exists()) {
			directory.mkdir();
		}

		Main.log.sendMessage(ColorManager.translate(""));
		Main.log.sendMessage(ColorManager.translate("  &eLoading parkours:"));
		File[] allFiles = new File(main.getDataFolder(), "parkours").listFiles();
		for (File file : allFiles) {
			String id = file.getName().toLowerCase().replace(".yml", "");

			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			String name = config.getString("parkour.name");

			parkourFiles.put(id, file);
			parkourConfigs.put(id, config);

			if(!Character.isDigit(id.charAt(0))) {
				if (validParkourData(config)) {
					Location spawn = (Location) config.get("parkour.spawn");
					Location start = (Location) config.get("parkour.start");
					Location end = (Location) config.get("parkour.end");
					Location statsHologram = null;
					Location topHologram = null;

					if (main.isHologramsEnabled()) {
						if (config.get("parkour.holograms.stats") != null) {
							statsHologram = (Location) config.get("parkour.holograms.stats");
						}

						if (config.get("parkour.holograms.top") != null) {
							topHologram = (Location) config.get("parkour.holograms.top");
						}
					}

					Parkour parkour = new Parkour(main, id, name, spawn, start, end, statsHologram, topHologram);
					parkours.put(id, parkour);

					if (!config.contains("parkour.icon")) {
						config.set("parkour.icon", XMaterial.ITEM_FRAME.parseItem());
					} else {
						String itemName = config.getString("parkour.icon");
						parkour.setIcon(XMaterial.matchXMaterial(itemName).orElse(XMaterial.ITEM_FRAME).parseItem());
					}

					if (config.contains("parkour.walkableBlocks")) {
						List<XMaterial> walkable = getWalkableBlocks(id);
						parkour.setWalkableBlocks(walkable);
						saveWalkableBlocksString(id, walkable);
					}

					if (!config.contains("parkour.rewards")) {
						config.set("parkour.rewards", new ArrayList<>());
					}

					if (!config.contains("parkour.checkpoints")) {
						config.set("parkour.checkpoints", new ArrayList<>());
					}

					if (!config.contains("parkour.permissionRequired")) {
						config.set("parkour.permissionRequired.enabled", false);
						config.set("parkour.permissionRequired.permission", "aparkour.permission." + id);
						config.set("parkour.permissionRequired.message", "&cYou dont have permission to start this parkour!");
					}

					if (config.contains("parkour.permissionRequired")) {
						parkour.setPermissionRequired(config.getBoolean("parkour.permissionRequired.enabled"));
						parkour.setPermission(config.getString("parkour.permissionRequired.permission"));
						parkour.setPermissionMessage(config.getString("parkour.permissionRequired.message"));

						if (main.getServer().getPluginManager().getPermission(parkour.getPermission()) == null) {
							main.getServer().getPluginManager().addPermission(new Permission(parkour.getPermission()));
						}
					}

					if (!config.contains("parkour.plateHolograms.start")) {
						config.set("parkour.plateHolograms.start.enabled", false);
						config.set("parkour.plateHolograms.start.distanceBelowPlate", 2.5D);
					}

					if (!config.contains("parkour.plateHolograms.end")) {
						config.set("parkour.plateHolograms.end.enabled", false);
						config.set("parkour.plateHolograms.end.distanceBelowPlate", 2.5D);
					}

					if (!config.contains("parkour.plateHolograms.checkpoints")) {
						config.set("parkour.plateHolograms.checkpoints.enabled", false);
						config.set("parkour.plateHolograms.checkpoints.distanceBelowPlate", 2.5D);
					}

					if (!config.contains("parkour.titles.start")) {
						config.set("parkour.titles.start.enabled", false);
					}

					if (!config.contains("parkour.titles.end")) {
						config.set("parkour.titles.end.enabled", false);
					}

					if (!config.contains("parkour.titles.checkpoint")) {
						config.set("parkour.titles.checkpoint.enabled", false);
					}

					saveConfig(id);

					main.getCheckpointsHandler().loadCheckpoints(parkour, config);

					if (config.contains("parkour.plateHolograms")) {
						parkour.getStart().setHologramEnabled(config.getBoolean("parkour.plateHolograms.start.enabled"));
						parkour.getStart().setHologramDistance(config.getDouble("parkour.plateHolograms.start.distanceBelowPlate"));
						parkour.getEnd().setHologramEnabled(config.getBoolean("parkour.plateHolograms.end.enabled"));
						parkour.getEnd().setHologramDistance(config.getDouble("parkour.plateHolograms.end.distanceBelowPlate"));

						if(!parkour.getCheckpoints().isEmpty()) {
							boolean enabled = config.getBoolean("parkour.plateHolograms.checkpoints.enabled");
							double distance = config.getDouble("parkour.plateHolograms.checkpoints.distanceBelowPlate");
							for(Plate checkpoint : parkour.getCheckpoints()) {
								checkpoint.setHologramEnabled(enabled);
								checkpoint.setHologramDistance(distance);
							}
						}
					}

					if (config.contains("parkour.titles.start")) {
						parkour.setStartTitleEnabled(config.getBoolean("parkour.titles.start.enabled"));
					}

					if (config.contains("parkour.titles.end")) {
						parkour.setEndTitleEnabled(config.getBoolean("parkour.titles.end.enabled"));
					}

					if (config.contains("parkour.titles.checkpoint")) {
						parkour.setCheckpointTitleEnabled(config.getBoolean("parkour.titles.checkpoint.enabled"));
					}

					main.getPlateManager().loadPlates(parkour);

					Main.log.sendMessage(ColorManager.translate("    &a'" + name + "' &7- " + (parkour.getCheckpoints().size() > 0 ? "&a" : "&c") + parkour.getCheckpoints().size() + " checkpoints"));
				} else {
					Main.log.sendMessage(ColorManager.translate("    &c'" + name + "' not loaded because parkour data is not correct!"));
				}
			} else {
				Main.log.sendMessage(ColorManager.translate("    &c'" + name + "' not loaded because parkour id starts with a number!"));
			}
		}
		
		if(parkours.size() == 0)
			Main.log.sendMessage(ColorManager.translate("    &cNo parkour has been loaded!"));
		
		Main.log.sendMessage(ColorManager.translate(""));
	}

	public void loadHolograms() {
		if(main.isHologramsEnabled()) {
			for (Parkour parkour : parkours.values()) {
				if (parkour.getStart().isHologramEnabled()) {
					Hologram hologram = HologramsAPI.createHologram(main,
							parkour.getStart().getLocation().clone().add(0.5D, parkour.getStart().getHologramDistance(), 0.5D));
					hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Start.Line1"));
					hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Start.Line2"));
					parkour.getStart().setHologram(hologram);
				}
				if (parkour.getEnd().isHologramEnabled()) {
					Hologram hologram = HologramsAPI.createHologram(main,
							parkour.getEnd().getLocation().clone().add(0.5D, parkour.getEnd().getHologramDistance(), 0.5D));
					hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.End.Line1"));
					hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.End.Line2"));
					parkour.getEnd().setHologram(hologram);
				}
				if (!parkour.getCheckpoints().isEmpty()) {
					int iterator = 1;
					for (Plate checkpoint : parkour.getCheckpoints()) {
						if (checkpoint.isHologramEnabled()) {
							Hologram hologram = HologramsAPI.createHologram(main,
									checkpoint.getLocation().clone().add(0.5D, checkpoint.getHologramDistance(), 0.5D));
							hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Checkpoint.Line1")
									.replaceAll("%checkpoint%", Integer.toString(iterator)));
							hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Checkpoint.Line2")
									.replaceAll("%checkpoint%", Integer.toString(iterator)));
							checkpoint.setHologram(hologram);
							iterator++;
						}
					}
				}
			}
		}
	}

	public void loadHolograms(Parkour parkour) {
		if(main.isHologramsEnabled()) {
			if (parkour.getStart().isHologramEnabled()) {
				Hologram hologram = HologramsAPI.createHologram(main,
						parkour.getStart().getLocation().clone().add(0.5D, parkour.getStart().getHologramDistance(), 0.5D));
				hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Start.Line1"));
				hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Start.Line2"));
				parkour.getStart().setHologram(hologram);
			}
			if (parkour.getEnd().isHologramEnabled()) {
				Hologram hologram = HologramsAPI.createHologram(main,
						parkour.getEnd().getLocation().clone().add(0.5D, parkour.getEnd().getHologramDistance(), 0.5D));
				hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.End.Line1"));
				hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.End.Line2"));
				parkour.getEnd().setHologram(hologram);
			}
			if (!parkour.getCheckpoints().isEmpty()) {
				int iterator = 1;
				for (Plate checkpoint : parkour.getCheckpoints()) {
					if (checkpoint.isHologramEnabled()) {
						Hologram hologram = HologramsAPI.createHologram(main,
								checkpoint.getLocation().clone().add(0.5D, checkpoint.getHologramDistance(), 0.5D));
						hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Checkpoint.Line1")
								.replaceAll("%checkpoint%", Integer.toString(iterator)));
						hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Checkpoint.Line2")
								.replaceAll("%checkpoint%", Integer.toString(iterator)));
						checkpoint.setHologram(hologram);
						iterator++;
					}
				}
			}
		}
	}

	public void loadCheckpointHologram(Parkour parkour, Plate checkpoint) {
		if(main.isHologramsEnabled()) {
			FileConfiguration config = getConfig(parkour.getId());
			boolean enabled = config.getBoolean("parkour.plateHolograms.checkpoints.enabled");
			double distance = config.getDouble("parkour.plateHolograms.checkpoints.distanceBelowPlate");
			checkpoint.setHologramEnabled(enabled);
			checkpoint.setHologramDistance(distance);
			if (enabled) {
				Hologram hologram = HologramsAPI.createHologram(main, checkpoint.getLocation().clone().add(0.5D, checkpoint.getHologramDistance(), 0.5D));
				hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Checkpoint.Line1")
						.replaceAll("%checkpoint%", Integer.toString(parkour.getCheckpoints().size())));
				hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Checkpoint.Line2")
						.replaceAll("%checkpoint%", Integer.toString(parkour.getCheckpoints().size())));
				checkpoint.setHologram(hologram);
			}
		}
	}

	public void removeCheckpointHolograms(Parkour parkour) {
		if (main.isHologramsEnabled()) {
			for (Plate checkpoint : parkour.getCheckpoints()) {
				if (checkpoint.isHologramEnabled()) {
					checkpoint.getHologram().delete();
				}
			}
		}
	}

	public void loadCheckpointHolograms(Parkour parkour) {
		if(main.isHologramsEnabled()) {
			int iterator = 1;
			for (Plate checkpoint : parkour.getCheckpoints()) {
				if (checkpoint.isHologramEnabled()) {
					Hologram hologram = HologramsAPI.createHologram(main,
							checkpoint.getLocation().clone().add(0.5D, checkpoint.getHologramDistance(), 0.5D));
					hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Checkpoint.Line1")
							.replaceAll("%checkpoint%", Integer.toString(iterator)));
					hologram.appendTextLine(main.getLanguageHandler().getMessage("Holograms.Plates.Checkpoint.Line2")
							.replaceAll("%checkpoint%", Integer.toString(iterator)));
					checkpoint.setHologram(hologram);
					iterator++;
				}
			}
		}
	}

	public void removeHologram(String id) {
		if (main.isHologramsEnabled()) {
			Parkour parkour = parkours.get(id);
			if(parkour != null) {
				if (parkour.getStart().isHologramEnabled()) {
					parkour.getStart().getHologram().delete();
				}
				if (parkour.getEnd().isHologramEnabled()) {
					parkour.getEnd().getHologram().delete();
				}
				for (Plate checkpoint : parkour.getCheckpoints()) {
					if (checkpoint.getHologram() != null)
						checkpoint.getHologram().delete();
				}
			}
		}
	}

	public boolean parkourExists(String id) {
		return parkourFiles.containsKey(id);
	}

	public Parkour getParkourById(String id) {
		for (Parkour parkour : parkours.values()) {
			if (parkour.getId().equalsIgnoreCase(id))
				return parkour;
		}
		return null;
	}

	public Parkour getParkourByLocation(Location loc) {
		for (Parkour parkour : parkours.values()) {
			if (loc.equals(parkour.getStart().getLocation()) ||
					loc.equals(parkour.getEnd().getLocation()) ||
					parkour.getCheckpointLocations().contains(loc))
				return parkour;
		}
		return null;
	}

	public List<XMaterial> getWalkableBlocks(String id) {
		List<XMaterial> walkable = new ArrayList<>();

		Optional<XMaterial> optXMaterial;
		if(parkourConfigs.get(id).contains("parkour.walkableBlocks")) {
			for (String block : parkourConfigs.get(id).getStringList("parkour.walkableBlocks")) {
				optXMaterial = XMaterial.matchXMaterial(block);

				if (optXMaterial.isPresent()) {
					XMaterial xMat = optXMaterial.get();

					if (!walkable.contains(xMat)) {
						walkable.add(xMat);
					}
				}
			}
		}

		return walkable;
	}

	public List<String> getWalkableBlocksString(List<XMaterial> walkable) {
		List<String> list = new ArrayList<>();
		for(XMaterial block : walkable) {
			list.add(block.name());
		}
		return list;
	}

	public void saveWalkableBlocksString(String id, List<XMaterial> walkable) {
		List<String> list = new ArrayList<>();
		for(XMaterial block : walkable) {
			list.add(block.name());
		}

		parkourConfigs.get(id).set("parkour.walkableBlocks", list);
		saveConfig(id);
	}

	public List<Reward> getRewards(String id) {
		List<Reward> rewards = new ArrayList<>();
		FileConfiguration config = parkourConfigs.get(id);
		if (config.contains("parkour.rewards")) {
			if (config.getConfigurationSection("parkour.rewards") != null) {
				for (String rewardid : config.getConfigurationSection("parkour.rewards").getKeys(false)) {
					if(!config.contains("parkour.rewards." + rewardid + ".chance")) {
						config.set("parkour.rewards." + rewardid + ".chance", 100);
						saveConfig(id);
					}
					if (validRewardData(id, rewardid)) {
						String permission = config.getString("parkour.rewards." + rewardid + ".permission");
						String command = config.getString("parkour.rewards." + rewardid + ".command");
						boolean firstTime = config.getBoolean("parkour.rewards." + rewardid + ".firstTime");
						int chance = config.getInt("parkour.rewards." + rewardid + ".chance");
						rewards.add(new Reward(rewardid, permission, command, firstTime, chance));

						if (main.getServer().getPluginManager().getPermission(permission) == null) {
							main.getServer().getPluginManager().addPermission(new Permission(permission));
						}
					}
				}
			}
		}
		return rewards;
	}

	public List<Plate> getCheckpoints(String pid) {
		List<Plate> checkpoints = new ArrayList<Plate>();
		if (parkourConfigs.get(pid).contains("parkour.checkpoints")) {
			if (parkourConfigs.get(pid).getConfigurationSection("parkour.checkpoints") != null) {
				for (String id : parkourConfigs.get(pid).getConfigurationSection("parkour.checkpoints").getKeys(false)) {
					Location loc = (Location) parkourConfigs.get(id).get("parkour.checkpoints." + Integer.parseInt(id));
					checkpoints.add(new Plate(loc));
				}
			}
		}
		return checkpoints;
	}

	public boolean validParkourData(YamlConfiguration config) {
		return config.contains("parkour.spawn") && config.contains("parkour.start") && config.contains("parkour.end");
	}

	private boolean validRewardData(String parkourID, String rewardID) {
		FileConfiguration config = parkourConfigs.get(parkourID);
		return config.contains("parkour.rewards." + rewardID + ".permission")
				&& config.contains("parkour.rewards." + rewardID + ".command")
				&& config.contains("parkour.rewards." + rewardID + ".firstTime")
				&& config.contains("parkour.rewards." + rewardID + ".chance");
	}

	public void resetPlayer(Player p) {
		if(main.getSessionHandler().getSession(p) != null) {
			p.setFlying(false);
			p.setFallDistance(0);
			p.setNoDamageTicks(40);

			if (main.isParkourItemsEnabled()) {
				main.getPlayerDataHandler().restorePlayerInventory(p);
			}

			if (main.getHidePlayerManager().isEnabled())
				main.getHidePlayerManager().setPlayerState(p, PlayerState.SHOWN);

			main.getSessionHandler().getSession(p).getParkour().getPlaying().remove(p.getUniqueId());
			main.getSessionHandler().getSession(p).cancelTimer();
			main.getSessionHandler().removeSession(p);
		}
	}

	public static void startParkour(CommandSender sender, Player p, Parkour parkour, boolean forceSpawnTp) {
		String message = main.getLanguageHandler().getMessage("Messages.Started");
		if(message.length() > 0)
			p.sendMessage(message);

		if (sender instanceof Player && !sender.getName().equalsIgnoreCase(p.getName())) {
			message = main.getLanguageHandler().getMessage("Messages.StartedForPlayer").replaceAll("%player%", p.getName());
			if(message.length() > 0) {
				sender.sendMessage((message));
			}
		}

		main.getSoundUtil().playStart(p);

		if (main.isParkourItemsEnabled()) {
			main.getPlayerDataHandler().savePlayerInventory(p);

			if (main.getHidePlayerManager().isEnabled()) {
				p.getInventory().setItem(main.getConfig().getInt("Items.HideItem.InventorySlot") - 1, main.getHidePlayerManager().getItem(p));
			}

			if (parkour.getCheckpoints().size() > 0) {
				if (main.getConfig().getBoolean("Items.Restart.Display")) {
					p.getInventory().setItem(main.getConfig().getInt("Items.Restart.InventorySlot"), main.getParkourItems().getRestartItem());
				}

				if (main.getConfig().getBoolean("Items.Checkpoint.Display")) {
					p.getInventory().setItem(main.getConfig().getInt("Items.Checkpoint.InventorySlot"), main.getParkourItems().getCheckpointItem());
				}
			} else {
				if (main.getConfig().getBoolean("Items.Restart.Display")) {
					p.getInventory().setItem(main.getConfig().getInt("Items.Restart.InventorySlot"), main.getParkourItems().getRestartItem());
				}
			}
		}

		p.setFlying(false);

		main.getTitleUtil().sendStartTitle(p, parkour);

		main.getSessionHandler().createSession(p, parkour);

		if (main.getSessionHandler().getSession(p).getParkour().getCheckpoints().size() > 0) {
			main.getSessionHandler().getSession(p).setNextCheckpointLocation(main.getSessionHandler().getSession(p).getParkour().getCheckpoints().get(main.getSessionHandler().getSession(p).getLastCheckpoint() + 1).getLocation());
		}

		if (forceSpawnTp) {
			p.teleport(main.getParkourHandler().getParkourById(parkour.getId()).getStart().getLocation());
		}

		Bukkit.getPluginManager().callEvent(new ParkourStartEvent(p, parkour));
	}

	public static void finishParkour(CommandSender sender, Player p) {
		ParkourSession session = main.getSessionHandler().getSession(p);
		Profile profile = main.getPlayerDataHandler().getData(p);
		Parkour parkour = session.getParkour();

		if (parkour == null) {
			return;
		}

		long total = session.getLiveTime();

		main.getSoundUtil().playEnd(p);

		main.getTitleUtil().sendEndTitle(p, parkour);

		String end = main.getLanguageHandler().getMessage("EndMessage.Normal");
		if(end.length() > 0)
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', end)
					.replaceAll("%endTime%", main.getTimerManager().millisToString(main.getLanguageHandler().getMessage("Timer.Formats.ParkourTimer"),total)));

		if (sender instanceof Player && !sender.getName().equalsIgnoreCase(p.getName())) {
			end = main.getLanguageHandler().getMessage("EndMessage.EndForPlayer").replaceAll("%player%", p.getName());
			if(end.length() > 0) {
				sender.sendMessage((end));
			}
		}

		if (profile.getBestTimes().get(parkour.getId()) == 0 && profile.getLastTimes().get(parkour.getId()) == 0) {
			String message = main.getLanguageHandler().getMessage("EndMessage.FirstTime");
			if(message.length() > 0)
				p.sendMessage(message);
			main.getRewardHandler().giveParkourRewards(p, parkour.getId(), true);
		}

		profile.setLastTime(total, parkour.getId());
		if (profile.getBestTimes().get(parkour.getId()) == 0) {
			profile.setBestTime(total, parkour.getId());
		}

		main.getParkourHandler().resetPlayer(p);

		main.getRewardHandler().giveParkourRewards(p, parkour.getId(), false);

		if (main.getConfig().getBoolean("TpToParkourSpawn.Enabled")) {
			p.teleport(parkour.getSpawn());
		}

		if (main.getConfig().getBoolean("Firework.Enabled")) {
			RandomFirework.launchRandomFirework(p.getLocation());
		}

		if (profile.isBestTime(total, parkour.getId())) {
			long bestTotal = profile.getBestTimes().get(parkour.getId()) - total;

			String record = main.getLanguageHandler().getMessage("EndMessage.Record");

			profile.setBestTime(total, parkour.getId());

			if(record.length() > 0)
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', record)
						.replaceAll("%recordTime%", main.getTimerManager().millisToString(main.getLanguageHandler().getMessage("Timer.Formats.ParkourTimer"),bestTotal)));
		}

		profile.save(parkour.getId());

		main.getStatsHologramManager().reloadStatsHologram(p, parkour.getId());

		Bukkit.getPluginManager().callEvent(new ParkourEndEvent(p, parkour));
	}

	public static void restartPlayerParkour(Player p) {
		ParkourSession session = main.getSessionHandler().getSession(p);

		p.teleport(session.getParkour().getSpawn());

		String message = main.getLanguageHandler().getMessage("Messages.Leave").replaceAll("%parkourName%", session.getParkour().getName());
		if (message.length() > 0)
			p.sendMessage(message);

		main.getParkourHandler().resetPlayer(p);
		main.getSoundUtil().playReturn(p);

		Bukkit.getPluginManager().callEvent(new ParkourReturnEvent(p, session.getParkour()));
	}
}
