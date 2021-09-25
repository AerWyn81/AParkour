package me.davidml16.aparkour.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import me.davidml16.aparkour.data.LeaderboardEntry;
import me.davidml16.aparkour.managers.ColorManager;
import me.davidml16.aparkour.utils.ItemBuilder;
import me.davidml16.aparkour.utils.Sounds;
import me.davidml16.aparkour.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.Parkour;

public class PlayerStats_GUI implements Listener {

	private final HashMap<UUID, Integer> opened;
	private final List<Integer> borders;

	private final Main main;

	public PlayerStats_GUI(Main main) {
		this.main = main;
		this.opened = new HashMap<>();
		this.borders = Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44);
		this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}

	public HashMap<UUID, Integer> getOpened() {
		return opened;
	}

	private void openPage(Player p, int page) {

		List<Parkour> parkours = new ArrayList<>(main.getParkourHandler().getParkours().values());

		if(page > 0 && parkours.size() < (page * 21) + 1) {
			openPage(p, page - 1);
			return;
		}

		Inventory gui = Bukkit.createInventory(null, 45, ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.title")));

		ItemStack edge = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName("").toItemStack();
		ItemStack book = new ItemBuilder(XMaterial.BOOK.parseItem()).setName(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.playerStats").replaceAll("%player%", p.getName()))).toItemStack();

		for (Integer i : borders) {
			gui.setItem(i, edge);
		}

		if (page > 0) {
			gui.setItem(18, new ItemBuilder(XMaterial.ENDER_PEARL.parseItem()).setName(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Layout.Previous"))).toItemStack());
		} else {
			gui.setItem(18, new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName("").toItemStack());
		}

		if (parkours.size() > (page + 1) * 21) {
			gui.setItem(26, new ItemBuilder(XMaterial.ENDER_PEARL.parseItem()).setName(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Layout.Next"))).toItemStack());
		} else {
			gui.setItem(26, new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName("").toItemStack());
		}

		gui.setItem(4, book);

		if (parkours.size() > 21) parkours = parkours.subList(page * 21, Math.min(((page * 21) + 21), parkours.size()));

		if(parkours.size() > 0) {
			for (Parkour parkour : parkours) {
				List<String> lore = new ArrayList<String>();
				lore.add(" ");

				lore.add(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.lastTime")));
				if (main.getPlayerDataHandler().getData(p).getLastTimes().get(parkour.getId()) > 0)
					lore.add(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.lastTimeValue").replaceAll("%lastTime%", main.getTimerManager().millisToString(main.getLanguageHandler().getMessage("Timer.Formats.ParkourTimer"), main.getPlayerDataHandler().getData(p).getLastTimes().get(parkour.getId())))));
				else
					lore.add(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.none")));

				lore.add(" ");

				lore.add(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.recordTime")));
				if (main.getPlayerDataHandler().getData(p).getBestTimes().get(parkour.getId()) > 0)
					lore.add(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.recordTimeValue").replaceAll("%recordTime%", main.getTimerManager().millisToString(main.getLanguageHandler().getMessage("Timer.Formats.ParkourTimer"), main.getPlayerDataHandler().getData(p).getBestTimes().get(parkour.getId())))));
				else
					lore.add(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.none")));

				List<LeaderboardEntry> leaderboard = main.getLeaderboardHandler().getLeaderboard(parkour.getId());

				if (leaderboard.size() > 0) {
					lore.add("");
					lore.add(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.worldRecords")));
					int i = 0;
					for (LeaderboardEntry entry : leaderboard) {
						lore.add(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.worldRecordsValue").replaceAll("%playerName%", entry.getName()).replaceAll("%playerTime%", main.getTimerManager().millisToString(main.getLanguageHandler().getMessage("Timer.Formats.ParkourTimer"), entry.getTime()))));
						if (i == 2) break;
						i++;
					}
				}

				lore.add("");

				gui.addItem(new ItemBuilder(parkour.getIcon()).setName(ColorManager.translate(main.getLanguageHandler().getMessage("GUIs.Stats.parkourName").replaceAll("%parkourName%", parkour.getName()))).setLore(lore).toItemStack());
			}
		} else {
			gui.setItem(22, new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).setName(ColorManager.translate("&c")).toItemStack());
		}

		Bukkit.getScheduler().runTaskLater(main, () -> opened.put(p.getUniqueId(), page), 1L);

		p.openInventory(gui);

		Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 100, 3);

	}

	public void open(Player p) {
		p.updateInventory();
		openPage(p, 0);
	}

	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();

		if (e.getCurrentItem() == null) return;
		if (e.getCurrentItem().getType() == XMaterial.AIR.parseMaterial()) return;

		if (opened.containsKey(p.getUniqueId())) {
			e.setCancelled(true);
			int slot = e.getRawSlot();
			if (slot == 18 && e.getCurrentItem().getType() == XMaterial.ENDER_PEARL.parseMaterial()) {
				openPage(p, opened.get(p.getUniqueId()) - 1);
			} else if (slot == 26 && e.getCurrentItem().getType() == XMaterial.ENDER_PEARL.parseMaterial()) {
				openPage(p, opened.get(p.getUniqueId()) + 1);
			}
		}
	}

	@EventHandler
	public void InventoryCloseEvent(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		opened.remove(p.getUniqueId());
	}
}
