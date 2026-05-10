package hu.sellplugin.listeners;

import hu.sellplugin.SellPlugin;
import hu.sellplugin.managers.PriceManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class InventoryHoverListener implements Listener {

    private final SellPlugin plugin;
    private final PriceManager priceManager;
    private final NamespacedKey PRICE_LORE_KEY;

    public InventoryHoverListener(SellPlugin plugin) {
        this.plugin = plugin;
        this.priceManager = plugin.getPriceManager();
        this.PRICE_LORE_KEY = new NamespacedKey(plugin, "price_lore");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.getConfig().getBoolean("show-price-in-lore", true)) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        for (ItemStack item : player.getInventory().getContents()) {
            updateItemLore(item);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (!plugin.getConfig().getBoolean("show-price-in-lore", true)) return;
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        updateItemLore(item);
    }

    private void updateItemLore(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        if (!priceManager.hasPrice(item.getType())) return;

        Material mat = item.getType();
        double price = priceManager.getPrice(mat);
        double stackPrice = priceManager.getStackPrice(mat);
        String currency = priceManager.getCurrencySymbol();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        double existing = pdc.getOrDefault(PRICE_LORE_KEY, PersistentDataType.DOUBLE, -1.0);
        if (existing == price) return;

        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(line -> {
            String s = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(line);
            return s.contains("Eladási ár:") || s.contains("Stack (64x):");
        });

        lore.add(Component.empty());

        String loreLine = plugin.getConfig().getString("lore-format", "&7Eladási ár: &e{price} {currency} &7/ db")
            .replace("{price}", formatMoney(price))
            .replace("{stack_price}", formatMoney(stackPrice))
            .replace("{currency}", currency)
            .replace("&", "§");

        String stackLine = plugin.getConfig().getString("lore-stack-format", "&7Stack (64x): &6{stack_price} {currency}")
            .replace("{price}", formatMoney(price))
            .replace("{stack_price}", formatMoney(stackPrice))
            .replace("{currency}", currency)
            .replace("&", "§");

        lore.add(Component.text(loreLine));
        lore.add(Component.text(stackLine));

        meta.lore(lore);
        pdc.set(PRICE_LORE_KEY, PersistentDataType.DOUBLE, price);
        item.setItemMeta(meta);
    }

    private String formatMoney(double amount) {
        if (amount == (long) amount) return String.valueOf((long) amount);
        return String.format("%.2f", amount);
    }
}
