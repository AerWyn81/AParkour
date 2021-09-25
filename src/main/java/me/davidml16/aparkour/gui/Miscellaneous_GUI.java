package me.davidml16.aparkour.gui;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.conversation.RenameMenu;
import me.davidml16.aparkour.data.Parkour;
import me.davidml16.aparkour.managers.ColorManager;
import me.davidml16.aparkour.utils.ItemBuilder;
import me.davidml16.aparkour.utils.Sounds;
import me.davidml16.aparkour.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Miscellaneous_GUI implements Listener {

    private final HashMap<UUID, String> opened;
    private final HashMap<String, Inventory> guis;

    private final Main main;

    public Miscellaneous_GUI(Main main) {
        this.main = main;
        this.opened = new HashMap<>();
        this.guis = new HashMap<>();
        this.main.getServer().getPluginManager().registerEvents(this, this.main);
    }

    public HashMap<UUID, String> getOpened() {
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
        if(guis.containsKey(id)) return;

        Inventory gui = Bukkit.createInventory(null, 45, main.getLanguageHandler().getMessage("GUIs.Misc.title").replaceAll("%parkour%", id));
        ItemStack edge = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName("").toItemStack();
        ItemStack back = new ItemBuilder(XMaterial.ARROW.parseItem()).setName(ColorManager.translate("&aBack to config")).toItemStack();

        buildInventory(id, gui);

        for (int i = 0; i < 45; i++) {
            if(gui.getItem(i) == null) {
                gui.setItem(i, edge);
            }
        }

        gui.setItem(40, back);

        guis.put(id, gui);
    }

    public void reloadGUI(String id) {
        Inventory gui = guis.get(id);

        buildInventory(id, gui);

        for(HumanEntity pl : gui.getViewers()) {
            pl.getOpenInventory().getTopInventory().setContents(gui.getContents());
        }
    }

    public void reloadAllGUI() {
        for(String id : main.getParkourHandler().getParkours().keySet()) {
            reloadGUI(id);
        }
    }

    private void buildInventory(String id, Inventory gui) {
        FileConfiguration config = main.getParkourHandler().getConfig(id);

        gui.setItem(11, new ItemBuilder(XMaterial.NAME_TAG.parseItem()).setName(ColorManager.translate("&aParkour icon"))
                .setLore(
                        "",
                        ColorManager.translate(" &7Click in a item of your "),
                        ColorManager.translate(" &7inventory to set it "),
                        ColorManager.translate(" &7to parkour icon. "),
                        "",
                        ColorManager.translate(" &7Click on the icon on gui "),
                        ColorManager.translate(" &7to set to the default icon. "),
                        ""
                )
                .toItemStack());

        String iconItemName = config.getString("parkour.icon");
        Optional<XMaterial> optIconItem = XMaterial.matchXMaterial(iconItemName);

        if (optIconItem.isPresent()) {
            XMaterial iconItem = optIconItem.get();
            String iconName = iconItem.name().replace("_", " ");

            if (iconItem != XMaterial.ITEM_FRAME) {
                gui.setItem(20, new ItemBuilder(iconItem.parseItem()).setName(ColorManager.translate("&a" + iconName.substring(0, 1).toUpperCase() + iconName.substring(1).toLowerCase())).setLore("", ColorManager.translate("&eClick to remove!")).toItemStack());
            } else {
                gui.setItem(20, new ItemBuilder(iconItem.parseItem()).setName(ColorManager.translate("&c" + iconName.substring(0, 1).toUpperCase() + iconName.substring(1).toLowerCase())).setLore("", ColorManager.translate("&eDefault parkour icon!")).toItemStack());
            }
        } else {
            XMaterial iconItem = XMaterial.ITEM_FRAME;
            String iconName = iconItem.name().replace("_", " ");

            gui.setItem(20, new ItemBuilder(iconItem.parseItem()).setName(ColorManager.translate("&c" + iconName.substring(0, 1).toUpperCase() + iconName.substring(1).toLowerCase())).setLore("", ColorManager.translate("&eDefault parkour icon!")).toItemStack());
        }

        gui.setItem(15, new ItemBuilder(XMaterial.NAME_TAG.parseItem()).setName(ColorManager.translate("&aParkour name"))
                .setLore(
                        "",
                        ColorManager.translate(" &7Click on the anvil "),
                        ColorManager.translate(" &7to start rename menu "),
                        "",
                        ColorManager.translate(" &7Choose 1 to rename parkour "),
                        ColorManager.translate(" &7Choose 2 to save and exit menu. "),
                        ""
                )
                .toItemStack());
        gui.setItem(24,  new ItemBuilder(XMaterial.ANVIL.parseItem())
                .setName(ColorManager.translate("&aRename parkour"))
                .setLore(
                        "",
                        ColorManager.translate("&eClick to rename parkour! ")
                ).toItemStack());
    }

    public void open(Player p, String id) {
        p.updateInventory();
        p.openInventory(guis.get(id));

        Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 100, 3);
        Bukkit.getScheduler().runTaskLater(main, () -> opened.put(p.getUniqueId(), id), 1L);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getType() == XMaterial.AIR.parseMaterial()) return;

        if (opened.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            String id = opened.get(p.getUniqueId());

            if (slot == 20) {
                if (e.getCurrentItem().getType() != XMaterial.ITEM_FRAME.parseMaterial()) {
                    FileConfiguration config = main.getParkourHandler().getConfig(id);
                    config.set("parkour.icon", XMaterial.ITEM_FRAME.name());
                    p.sendMessage(ColorManager.translate(main.getLanguageHandler().getPrefix()
                            + " &cChanged icon of parkour &e" + id + " &cto default!"));
                    main.getParkourHandler().getParkourById(id).setIcon(XMaterial.ITEM_FRAME.parseItem());
                    main.getParkourHandler().saveConfig(id);
                    Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 100, 3);
                    reloadGUI(id);
                } else {
                    p.sendMessage(ColorManager.translate(main.getLanguageHandler().getPrefix()
                            + " &cItem frame is the default icon of parkours!"));
                    Sounds.playSound(p, p.getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
                }
            } else if (slot == 24) {
                Parkour parkour = main.getParkourHandler().getParkourById(id);
                p.closeInventory();
                new RenameMenu(main).getConversation(p, parkour).begin();
                Sounds.playSound(p, p.getLocation(), Sounds.MySound.ANVIL_USE, 100, 3);
            } else if (slot == 40) {
                main.getConfigGUI().open(p, id);
            } else if (slot >= 45 && slot <= 80) {
                if (e.getCurrentItem().getType() == XMaterial.AIR.parseMaterial()) return;

                FileConfiguration config = main.getParkourHandler().getConfig(id);

                ItemStack item = e.getCurrentItem();

                config.set("parkour.icon", item.getType().name());
                main.getParkourHandler().getParkourById(id).setIcon(item);

                p.sendMessage(ColorManager.translate(main.getLanguageHandler().getPrefix()
                        + " &aChanged icon of parkour &e" + id + " &ato &e" + e.getCurrentItem().getType().name()));
                Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 100, 3);
                reloadGUI(id);

                main.getParkourHandler().saveConfig(id);
            }
        }
    }

    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        opened.remove(p.getUniqueId());
    }
}