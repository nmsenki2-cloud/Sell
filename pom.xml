package hu.sellplugin.managers;

import hu.sellplugin.SellPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PriceManager {

    private final SellPlugin plugin;
    private final Map<Material, Double> prices = new HashMap<>();

    public PriceManager(SellPlugin plugin) {
        this.plugin = plugin;
        loadPrices();
    }

    public void loadPrices() {
        prices.clear();
        FileConfiguration config = plugin.getConfig();

        if (!config.isConfigurationSection("prices")) {
            plugin.getLogger().warning("Nincs 'prices' szekció a config.yml-ben!");
            return;
        }

        Set<String> keys = config.getConfigurationSection("prices").getKeys(false);
        for (String key : keys) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                double price = config.getDouble("prices." + key);
                prices.put(material, price);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ismeretlen material a config-ban: " + key);
            }
        }

        plugin.getLogger().info("Betöltve " + prices.size() + " item ár.");
    }

    public boolean hasPrice(Material material) {
        return prices.containsKey(material);
    }

    public double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }

    public double getStackPrice(Material material) {
        return getPrice(material) * 64;
    }

    public void setPrice(Material material, double price) {
        prices.put(material, price);
        plugin.getConfig().set("prices." + material.name(), price);
        plugin.saveConfig();
    }

    public Map<Material, Double> getAllPrices() {
        return new HashMap<>(prices);
    }

    public int getPriceCount() {
        return prices.size();
    }

    public String getCurrencySymbol() {
        return plugin.getConfig().getString("currency-symbol", "🪙");
    }

    public String getCurrencyName() {
        return plugin.getConfig().getString("currency-name", "Arany");
    }
}
