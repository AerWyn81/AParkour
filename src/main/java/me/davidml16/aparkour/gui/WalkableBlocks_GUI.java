package me.davidml16.aparkour.gui;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.Pair;
import me.davidml16.aparkour.data.Parkour;
import me.davidml16.aparkour.managers.ColorManager;
import me.davidml16.aparkour.utils.ItemBuilder;
import me.davidml16.aparkour.utils.Sounds;
import me.davidml16.aparkour.utils.WalkableBlocksUtil;
import me.davidml16.aparkour.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class WalkableBlocks_GUI implements Listener {

    private final HashMap<UUID, Pair> opened;
    private final HashMap<String, Inventory> guis;
    private final List<Integer> borders;

    private final Main main;

    public WalkableBlocks_GUI(Main main) {
        this.main = main;
        this.opened = new HashMap<>();
        this.guis = new HashMap<>();
        this.borders = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 41, 42, 43, 44);
        this.main.getServer().getPluginManager().registerEvents(this, this.main);
    }

    public HashMap<UUID, Pair> getOpened() {
        return opened;
    }

    public HashMap<String, Inventory> getGuis() {
        return guis;
    }

    public void loadGUI() {
        for (File file : Objects.requireNonNull(new File(main.getDataFolder(), "parkours").listFiles())) {
            loadGUI(file.getName().toLowerCase().replace(".yml", ""));
        }
    }

    public void loadGUI(String id) {
        if (guis.containsKey(id)) return;

        Inventory gui = Bukkit.createInventory(null, 45, main.getLanguageHandler().getMessage("GUIs.WalkableBlocks.title").replaceAll("%parkour%", id));

        ItemStack edge = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName("").toItemStack();
        ItemStack back = new ItemBuilder(XMaterial.ARROW.parseItem()).setName(ColorManager.translate("&aBack to config")).toItemStack();

        for (Integer i : borders) {
            gui.setItem(i, edge);
        }

        gui.setItem(40, back);

        guis.put(id, gui);
    }

    public void reloadAllGUI() {
        for(String id : main.getParkourHandler().getParkours().keySet()) {
            reloadGUI(id);
        }
    }

    public void reloadGUI(String id) {
        for(UUID uuid : opened.keySet()) {
            if(opened.get(uuid).getParkour().equals(id)) {
                Player p = Bukkit.getPlayer(uuid);
                openPage(p, id, opened.get(uuid).getPage());
            }
        }
    }

    private void openPage(Player p, String id, int page) {
        List<XMaterial> walkable = main.getParkourHandler().getParkourById(id).getWalkableBlocks();

        if(page > 0 && walkable.size() < (page * 21) + 1) {
            openPage(p, id, page - 1);
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 45, main.getLanguageHandler().getMessage("GUIs.WalkableBlocks.title").replaceAll("%parkour%", id));
        gui.setContents(guis.get(id).getContents());

        for (int i = 10; i <= 16; i++)
            gui.setItem(i, null);
        for (int i = 19; i <= 25; i++)
            gui.setItem(i, null);
        for (int i = 28; i <= 34; i++)
            gui.setItem(i, null);

        if (page > 0) {
            gui.setItem(18, new ItemBuilder(XMaterial.ENDER_PEARL.parseItem()).setName(ColorManager.translate("&aPrevious page")).toItemStack());
        } else {
            gui.setItem(18, new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName("").toItemStack());
        }

        if (walkable.size() > (page + 1) * 21) {
            gui.setItem(26, new ItemBuilder(XMaterial.ENDER_PEARL.parseItem()).setName(ColorManager.translate("&aNext page")).toItemStack());
        } else {
            gui.setItem(26, new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName("").toItemStack());
        }

        if (walkable.size() > 21) walkable = walkable.subList(page * 21, Math.min(((page * 21) + 21), walkable.size()));

        if(walkable.size() > 0) {
            for (XMaterial block : walkable) {
                String name = block.name().replaceAll("_", " ");
                gui.addItem(new ItemBuilder(block.parseItem()).setName(ColorManager.translate("&a" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase())).setLore("", ColorManager.translate("&eClick to remove!")).toItemStack());
            }
        } else {
            gui.setItem(22, new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).setName(ColorManager.translate("&cAny walkable block selected")).setLore(
                    "",
                    ColorManager.translate(" &7You dont have any "),
                    ColorManager.translate(" &7walkable block selected. "),
                    "",
                    ColorManager.translate(" &7Now you can walk in any "),
                    ColorManager.translate(" &7block, you will not fail. "),
                    ""
            ).toItemStack());
        }

        if (!opened.containsKey(p.getUniqueId())) {
            p.openInventory(gui);
        } else {
            p.getOpenInventory().getTopInventory().setContents(gui.getContents());
        }

        Bukkit.getScheduler().runTaskLater(main, () -> opened.put(p.getUniqueId(), new Pair(id, page)), 1L);
    }

    public void open(Player p, String id) {
        p.updateInventory();
        openPage(p, id, 0);

        Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 10, 2);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getType() == XMaterial.AIR.parseMaterial()) return;

        if (opened.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            String id = opened.get(p.getUniqueId()).getParkour();
            Parkour parkour = main.getParkourHandler().getParkourById(id);
            if (slot == 18 && e.getCurrentItem().getType() == XMaterial.ENDER_PEARL.parseMaterial()) {
                Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 10, 2);
                openPage(p, id, opened.get(p.getUniqueId()).getPage() - 1);
            } else if (slot == 26 && e.getCurrentItem().getType() == XMaterial.ENDER_PEARL.parseMaterial()) {
                Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 10, 2);
                openPage(p, id, opened.get(p.getUniqueId()).getPage() + 1);
            } else if (slot == 40) {
                main.getConfigGUI().open(p, id);
            } else if (slot >= 45 && slot <= 80) {
                List<XMaterial> walkable = parkour.getWalkableBlocks();

                    if (e.getCurrentItem().getType() == XMaterial.AIR.parseMaterial()) return;

                    XMaterial xMat = XMaterial.matchXMaterial(e.getCurrentItem());

                    if (e.getCurrentItem().getType().name().contains("PLATE")) {
                        Sounds.playSound(p, p.getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
                    } else {
                        if (!WalkableBlocksUtil.containsWalkable(walkable, xMat)) {
                            p.sendMessage(ColorManager.translate(main.getLanguageHandler().getPrefix()
                                    + " &aYou added &e" + e.getCurrentItem().getType().name() + " &ato walkable blocks of parkour &e" + id));
                            walkable.add(xMat);
                            parkour.setWalkableBlocks(walkable);
                            reloadGUI(id);
                            Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 10, 2);
                        } else {
                            p.sendMessage(ColorManager.translate(main.getLanguageHandler().getPrefix()
                                    + " &cThe block &e" + e.getCurrentItem().getType().name() + " &calready exists in walkable blocks of parkour &e" + id));
                            Sounds.playSound(p, p.getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
                        }
                }
            } else if ((slot >= 10 && slot <= 16) || (slot >= 19 && slot <= 25) || (slot >= 28 && slot <= 34)) {
                if (e.getCurrentItem().getType() == XMaterial.AIR.parseMaterial()) return;

                if (parkour.getWalkableBlocks().size() == 0) return;

                XMaterial xMaterial = XMaterial.matchXMaterial(e.getCurrentItem().getType());

                p.sendMessage(ColorManager.translate(main.getLanguageHandler().getPrefix()
                        + " &aYou removed &e" + e.getCurrentItem().getType().name() + " &afrom walkable blocks of parkour &e" + id));
                List<XMaterial> walkable = parkour.getWalkableBlocks();
                walkable.remove(xMaterial);
                parkour.setWalkableBlocks(walkable);
                reloadGUI(id);
                Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 10, 2);
            }
        }
    }

    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (opened.containsKey(p.getUniqueId())) {
            main.getParkourHandler().getParkours().get(opened.get(p.getUniqueId()).getParkour()).saveParkour();
            opened.remove(p.getUniqueId());
        }
    }
}