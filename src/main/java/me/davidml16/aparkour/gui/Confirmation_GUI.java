package me.davidml16.aparkour.gui;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.api.events.ParkourReturnEvent;
import me.davidml16.aparkour.data.ParkourSession;
import me.davidml16.aparkour.managers.ColorManager;
import me.davidml16.aparkour.utils.ItemBuilder;
import me.davidml16.aparkour.utils.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

    private HashMap<UUID, String> opened;
    private HashMap<String, Inventory> guis;

    private Main main;

    public Confirmation_GUI(Main main) {
        this.main = main;
        this.opened = new HashMap<UUID, String>();
        this.guis = new HashMap<String, Inventory>();
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

        Inventory gui = Bukkit.createInventory(null, 9, main.getLanguageHandler().getMessage("GUIs.Confirmation.title").replaceAll("%parkour%", id));

        ItemStack edge = new ItemBuilder(Material.STAINED_GLASS_PANE, 1).setDurability((short) 7).setName("").toItemStack();

        String materialName = main.getConfig().getString("GUIs.ConfirmationOnReset.Confirm.MaterialName");
        String name = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Confirm.Name"));
        String lore = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Confirm.Lore"));

        ItemStack confirm;
        if (materialName.contains(":")) {
            String[] materialWithData = materialName.split(":");
            confirm = new ItemBuilder(Material.getMaterial(materialWithData[0]), 1, (byte) Integer.parseInt(materialWithData[1])).setName(name).setLore(lore).toItemStack();
        } else {
            confirm = new ItemBuilder(Material.getMaterial(materialName), 1).setName(name).setLore(lore).toItemStack();
        }

        materialName = main.getConfig().getString("GUIs.ConfirmationOnReset.Cancel.MaterialName");
        name = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Cancel.Name"));
        lore = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Cancel.Lore"));

        ItemStack cancel;
        if (materialName.contains(":")) {
            String[] materialWithData = materialName.split(":");
            cancel = new ItemBuilder(Material.getMaterial(materialWithData[0]), 1, (byte) Integer.parseInt(materialWithData[1])).setName(name).setLore(lore).toItemStack();
        } else {
            cancel = new ItemBuilder(Material.getMaterial(materialName), 1).setName(name).setLore(lore).toItemStack();
        }

        gui.setItem(main.getConfig().getInt("GUIs.ConfirmationOnReset.Confirm.InventorySlot"), confirm);
        gui.setItem(main.getConfig().getInt("GUIs.ConfirmationOnReset.Cancel.InventorySlot"), cancel);

        for (int i = 0; i < 9; i++) {
            if(gui.getItem(i) == null) {
                gui.setItem(i, edge);
            }
        }

        guis.put(id, gui);
    }

    public void reloadAllGUI() {
        for(String id : main.getParkourHandler().getParkours().keySet()) {
            reloadGUI(id);
        }
    }

    public void reloadGUI(String id) {
        Inventory gui = guis.get(id);
        gui.clear();

        ItemStack edge = new ItemBuilder(Material.STAINED_GLASS_PANE, 1).setDurability((short) 7).setName("").toItemStack();

        String materialName = main.getConfig().getString("GUIs.ConfirmationOnReset.Confirm.MaterialName");
        String name = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Confirm.Name"));
        String lore = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Confirm.Lore"));

        ItemStack confirm;
        if (materialName.contains(":")) {
            String[] materialWithData = materialName.split(":");
            confirm = new ItemBuilder(Material.getMaterial(materialWithData[0]), 1, (byte) Integer.parseInt(materialWithData[1])).setName(name).setLore(lore).toItemStack();
        } else {
            confirm = new ItemBuilder(Material.getMaterial(materialName), 1).setName(name).setLore(lore).toItemStack();
        }

        materialName = main.getConfig().getString("GUIs.ConfirmationOnReset.Cancel.MaterialName");
        name = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Cancel.Name"));
        lore = ColorManager.translate(main.getConfig().getString("GUIs.ConfirmationOnReset.Cancel.Lore"));

        ItemStack cancel;
        if (materialName.contains(":")) {
            String[] materialWithData = materialName.split(":");
            cancel = new ItemBuilder(Material.getMaterial(materialWithData[0]), 1, (byte) Integer.parseInt(materialWithData[1])).setName(name).setLore(lore).toItemStack();
        } else {
            cancel = new ItemBuilder(Material.getMaterial(materialName), 1).setName(name).setLore(lore).toItemStack();
        }

        gui.setItem(main.getConfig().getInt("GUIs.ConfirmationOnReset.Confirm.InventorySlot"), confirm);
        gui.setItem(main.getConfig().getInt("GUIs.ConfirmationOnReset.Cancel.InventorySlot"), cancel);

        for (int i = 0; i < 9; i++) {
            if(gui.getItem(i) == null) {
                gui.setItem(i, edge);
            }
        }

        guis.replace(id, gui);
    }

    public void open(Player p, String id) {
        p.updateInventory();
        p.openInventory(guis.get(id));

        Sounds.playSound(p, p.getLocation(), Sounds.MySound.CLICK, 100, 3);
        Bukkit.getScheduler().runTaskLater(main, () -> opened.put(p.getUniqueId(), id), 1L);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getType() == Material.AIR) return;

        if (opened.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            if (slot == main.getConfig().getInt("GUIs.ConfirmationOnReset.Confirm.InventorySlot")) {
                p.closeInventory();
                ParkourSession session = main.getSessionHandler().getSession(p);
                p.teleport(session.getParkour().getSpawn());

                String message = main.getLanguageHandler().getMessage("Messages.Return");
                if (message.length() > 0)
                    p.sendMessage(message);

                main.getParkourHandler().resetPlayer(p);
                main.getSoundUtil().playReturn(p);

                Bukkit.getPluginManager().callEvent(new ParkourReturnEvent(p, session.getParkour()));
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
