package hu.sellplugin.commands;

import hu.sellplugin.SellPlugin;
import hu.sellplugin.managers.PriceManager;
import hu.sellplugin.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SellAdminCommand implements CommandExecutor {

    private final SellPlugin plugin;
    private final PriceManager priceManager;

    public SellAdminCommand(SellPlugin plugin) {
        this.plugin = plugin;
        this.priceManager = plugin.getPriceManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("sellplugin.admin")) {
            sender.sendMessage(MessageUtils.color("&cNincs jogosultságod!"));
            return true;
        }

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.color("&cHasználat: /selladmin set <material|hand> <ár>"));
                    return true;
                }
                Material mat = resolveMaterial(sender, args[1]);
                if (mat == null) return true;
                try {
                    double price = Double.parseDouble(args[2]);
                    if (price < 0) { sender.sendMessage(MessageUtils.color("&cAz ár nem lehet negatív!")); return true; }
                    priceManager.setPrice(mat, price);
                    sender.sendMessage(MessageUtils.color("&aÁr beállítva: &f" + mat.name() + " &a→ &e" + price + " " + priceManager.getCurrencySymbol()));
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.color("&cÉrvénytelen ár: &f" + args[2]));
                }
            }
            case "remove" -> {
                if (args.length < 2) { sender.sendMessage(MessageUtils.color("&cHasználat: /selladmin remove <material|hand>")); return true; }
                Material mat = resolveMaterial(sender, args[1]);
                if (mat == null) return true;
                plugin.getConfig().set("prices." + mat.name(), null);
                plugin.saveConfig();
                priceManager.loadPrices();
                sender.sendMessage(MessageUtils.color("&aEltávolítva: &f" + mat.name()));
            }
            case "reload" -> {
                plugin.reloadConfig();
                priceManager.loadPrices();
                sender.sendMessage(MessageUtils.color("&aConfig újratöltve! &f" + priceManager.getPriceCount() + " &aitem ár betöltve."));
            }
            case "list" -> {
                Map<Material, Double> all = priceManager.getAllPrices();
                String filter = args.length > 1 ? args[1].toUpperCase() : "";
                sender.sendMessage(MessageUtils.color("&6=== Árlista (" + all.size() + " item) ==="));
                all.entrySet().stream()
                    .filter(e -> filter.isEmpty() || e.getKey().name().contains(filter))
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sender.sendMessage(MessageUtils.color(
                        "&7- &f" + e.getKey().name() + " &7→ &e" + e.getValue() + " " + priceManager.getCurrencySymbol())));
            }
            case "check" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage(MessageUtils.color("&cCsak játékosok!")); return true; }
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held.getType() == Material.AIR) { player.sendMessage(MessageUtils.color("&cTarts valamit a kezedben!")); return true; }
                Material mat = held.getType();
                if (priceManager.hasPrice(mat)) {
                    sender.sendMessage(MessageUtils.color("&a" + mat.name() + " &7ára: &e" + priceManager.getPrice(mat) + " " + priceManager.getCurrencySymbol()));
                } else {
                    sender.sendMessage(MessageUtils.color("&c" + mat.name() + " &7nincs az árlistában."));
                }
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private Material resolveMaterial(CommandSender sender, String arg) {
        if (arg.equalsIgnoreCase("hand")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MessageUtils.color("&cA 'hand' csak játékosoknál működik!"));
                return null;
            }
            ItemStack held = player.getInventory().getItemInMainHand();
            if (held.getType() == Material.AIR) {
                sender.sendMessage(MessageUtils.color("&cTarts valamit a kezedben!"));
                return null;
            }
            return held.getType();
        }
        try {
            return Material.valueOf(arg.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MessageUtils.color("&cIsmeretlen material: &f" + arg));
            return null;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtils.color("&6=== SellPlugin Admin ==="));
        sender.sendMessage(MessageUtils.color("&e/selladmin set <material|hand> <ár>"));
        sender.sendMessage(MessageUtils.color("&e/selladmin remove <material|hand>"));
        sender.sendMessage(MessageUtils.color("&e/selladmin list [szűrő]"));
        sender.sendMessage(MessageUtils.color("&e/selladmin check"));
        sender.sendMessage(MessageUtils.color("&e/selladmin reload"));
    }
}
