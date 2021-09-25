package me.davidml16.aparkour.utils;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.PlayerState;
import me.davidml16.aparkour.managers.ColorManager;
import org.bukkit.Bukkit;
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

        try {
            returnItem = new ItemBuilder(XMaterial.matchXMaterial(materialName).get().parseItem()).setName(name).setLore(lore).toItemStack();
        } catch (Exception e) {
            returnItem = new ItemBuilder(XMaterial.BARRIER.parseItem()).setName(name).setLore(lore).toItemStack();
            Main.log.sendMessage(ColorManager.translate("&cCannot parse " + materialName + " for return item. Using default item."));
        }
    }

    public void loadCheckpointItem() {
        String materialName = main.getConfig().getString("Items.Checkpoint.MaterialName");
        String name = ColorManager.translate(main.getConfig().getString("Items.Checkpoint.Name"));
        String lore = ColorManager.translate(main.getConfig().getString("Items.Checkpoint.Lore"));

        try {
            checkpointItem = new ItemBuilder(XMaterial.matchXMaterial(materialName).get().parseItem()).setName(name).setLore(lore).toItemStack();
        } catch (Exception e) {
            checkpointItem = new ItemBuilder(XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE.parseItem()).setName(name).setLore(lore).toItemStack();
            Main.log.sendMessage(ColorManager.translate("&cCannot parse " + materialName + " for checkpoint item. Using default item."));
        }
    }

    public void loadHideItem() {
        String materialName = main.getConfig().getString("Items.HideItem.Hide.MaterialName");
        String name = ColorManager.translate(main.getConfig().getString("Items.HideItem.Hide.Name"));
        List<String> lore = main.getConfig().getStringList("Items.HideItem.Hide.Lore").stream().map(ColorManager::translate).collect(Collectors.toList());

        try {
            hideItem = new ItemBuilder(XMaterial.matchXMaterial(materialName).get().parseItem()).setName(name).setLore(lore).toItemStack();
        } catch (Exception e) {
            hideItem = new ItemBuilder(XMaterial.GRAY_DYE.parseItem()).setName(name).setLore(lore).toItemStack();
            Main.log.sendMessage(ColorManager.translate("&cCannot parse " + materialName + " for hide item. Using default item."));
        }

        if (main.getConfig().getBoolean("Items.HideItem.Hide.Enchanted", false)) {
            hideItem.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

            ItemMeta hideItemMeta = hideItem.getItemMeta();
            hideItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            hideItem.setItemMeta(hideItemMeta);
        }
    }

    public void loadShowItem() {
        String materialName = main.getConfig().getString("Items.HideItem.Show.MaterialName");
        String name = ColorManager.translate(main.getConfig().getString("Items.HideItem.Show.Name"));
        List<String> lore = main.getConfig().getStringList("Items.HideItem.Show.Lore").stream().map(ColorManager::translate).collect(Collectors.toList());

        try {
            showItem = new ItemBuilder(XMaterial.matchXMaterial(materialName).get().parseItem()).setName(name).setLore(lore).toItemStack();
        } catch (Exception e) {
            showItem = new ItemBuilder(XMaterial.LIME_DYE.parseItem()).setName(name).setLore(lore).toItemStack();
            Main.log.sendMessage(ColorManager.translate("&cCannot parse " + materialName + " for show item. Using default item."));
        }

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
