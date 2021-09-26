package me.davidml16.aparkour.gui;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.api.events.ParkourReturnEvent;
import me.davidml16.aparkour.data.ParkourSession;
import me.davidml16.aparkour.handlers.ParkourHandler;
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

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Confirmation_GUI implements Listener {

    private final HashMap<UUID, String> opened;
    private final HashMap<String, Inventory> guis;

    private final Main main;

    public Confirmation_GUI(Main main) {
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
        if (guis.containsKey(id)) return;

        ItemStack edge = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName("").toItemStack();

        Inventory gui = Bukkit.createInventory(null, 9, main.getLanguageHandler().getMessage("GUIs.Confirmation.title").replaceAll("%parkour%", id));
        buildInventory(gui);

        for (int i = 0; i < 9; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, edge);
            }
        }

        guis.put(id, gui);
    }

    public void reloadGUI(String id) {
        Inventory gui = guis.get(id);
        gui.clear();

        buildInventory(gui);

        guis.replace(id, gui);
    }

    public void reloadAllGUI() {
        for(String id : main.getParkourHandler().getParkours().keySet()) {
            reloadGUI(id);
        }
    }

    private void buildInventory(Inventory gui) {
        String materialName = main.getConfig().getString("GUIs.ConfirmationOnReset.Confirm.MaterialName");
        String name = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Confirm.Name"));
        String lore = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Confirm.Lore"));

        ItemStack confirm;
        try {
            confirm = new ItemBuilder(XMaterial.matchXMaterial(materialName).get().parseItem()).setName(name).setLore(lore).toItemStack();
        } catch (Exception e) {
            confirm = new ItemBuilder(XMaterial.GREEN_WOOL.parseItem()).setName(name).setLore(lore).toItemStack();
            Main.log.sendMessage(ColorManager.translate("&cCannot parse " + materialName + " for confirmation GUI. Using default item."));
        }

        materialName = main.getConfig().getString("GUIs.ConfirmationOnReset.Cancel.MaterialName");
        name = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Cancel.Name"));
        lore = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Cancel.Lore"));

        ItemStack cancel;
        try {
            cancel = new ItemBuilder(XMaterial.matchXMaterial(materialName).get().parseItem()).setName(name).setLore(lore).toItemStack();
        } catch (Exception e) {
            cancel = new ItemBuilder(XMaterial.RED_WOOL.parseItem()).setName(name).setLore(lore).toItemStack();
            Main.log.sendMessage(ColorManager.translate("&cCannot parse " + materialName + " for confirmation GUI. Using default item."));
        }

        gui.setItem(main.getConfig().getInt("GUIs.ConfirmationOnReset.Confirm.InventorySlot"), confirm);
        gui.setItem(main.getConfig().getInt("GUIs.ConfirmationOnReset.Cancel.InventorySlot"), cancel);
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
            if (slot == main.getConfig().getInt("GUIs.ConfirmationOnReset.Confirm.InventorySlot")) {
                p.closeInventory();
                ParkourHandler.restartPlayerParkour(p);
            } else if (slot == main.getConfig().getInt("GUIs.ConfirmationOnReset.Cancel.InventorySlot")) {
                p.closeInventory();
            }
        }
    }

    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        opened.remove(p.getUniqueId());
    }
}
