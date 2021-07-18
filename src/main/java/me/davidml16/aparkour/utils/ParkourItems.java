package me.davidml16.aparkour.utils;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.PlayerState;
import me.davidml16.aparkour.managers.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParkourItems {

    private ItemStack returnItem;
    private ItemStack checkpointItem;
    private ItemStack hideItem;
    private ItemStack showItem;

    private Main main;
    public ParkourItems(Main main) {
        this.main = main;
    }

    public void loadReturnItem() {
        String materialName = main.getConfig().getString("Items.Restart.MaterialName");
        String name = ColorManager.translate(main.getConfig().getString("Items.Restart.Name"));
        String lore = ColorManager.translate(main.getConfig().getString("Items.Restart.Lore"));
        returnItem = new ItemBuilder(Material.getMaterial(materialName), 1).setName(name).setLore(lore).toItemStack();
    }

    public void loadCheckpointItem() {
        String materialName = main.getConfig().getString("Items.Checkpoint.MaterialName");
        String name = ColorManager.translate(main.getConfig().getString("Items.Checkpoint.Name"));
        String lore = ColorManager.translate(main.getConfig().getString("Items.Checkpoint.Lore"));
        checkpointItem = new ItemBuilder(Material.getMaterial(materialName), 1).setName(name).setLore(lore).toItemStack();
    }

    public void loadHideItem() {
        Material hideMaterial;
        String[] material = main.getConfig().getString("Items.HideItem.Hide.MaterialName", "INK_SACK:0").split(":");

        if (material.length == 2) {
            hideMaterial = Material.getMaterial(material[0]);
            if (hideMaterial == null) hideMaterial = Material.INK_SACK;
        } else {
            hideMaterial = Material.INK_SACK;
        }

        String name = ColorManager.translate(main.getConfig().getString("Items.HideItem.Hide.Name"));
        List<String> lore = main.getConfig().getStringList("Items.HideItem.Hide.Lore").stream().map(ColorManager::translate).collect(Collectors.toList());
        hideItem = new ItemBuilder(hideMaterial, 1, (byte) Integer.parseInt(material[1])).setName(name).setLore(lore).toItemStack();

        if (main.getConfig().getBoolean("Items.HideItem.Hide.Enchanted", false)) {
            hideItem.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

            ItemMeta hideItemMeta = hideItem.getItemMeta();
            hideItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            hideItem.setItemMeta(hideItemMeta);
        }
    }

    public void loadShowItem() {
        Material showMaterial;
        String[] material = main.getConfig().getString("Items.HideItem.Show.MaterialName", "INK_SACK:10").split(":");

        if (material.length == 2) {
            showMaterial = Material.getMaterial(material[0]);
            if (showMaterial == null) showMaterial = Material.INK_SACK;
        } else {
            showMaterial = Material.INK_SACK;
        }

        String name = ColorManager.translate(main.getConfig().getString("Items.HideItem.Show.Name"));
        List<String> lore = main.getConfig().getStringList("Items.HideItem.Show.Lore").stream().map(ColorManager::translate).collect(Collectors.toList());
        showItem = new ItemBuilder(showMaterial, 1, (byte) Integer.parseInt(material[1])).setName(name).setLore(lore).toItemStack();

        if (main.getConfig().getBoolean("Items.HideItem.Show.Enchanted", false)) {
            showItem.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

            ItemMeta showItemItemMeta = showItem.getItemMeta();
            showItemItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            showItem.setItemMeta(showItemItemMeta);
        }
    }

    public ItemStack getRestartItem() {
        return returnItem;
    }

    public ItemStack getCheckpointItem() {
        return checkpointItem;
    }

    public ItemStack getHideItem() { return hideItem; }

    public ItemStack getShowItem() { return showItem; }
}
