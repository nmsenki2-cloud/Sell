package hu.sellplugin.commands;

import hu.sellplugin.SellPlugin;
import hu.sellplugin.managers.PriceManager;
import hu.sellplugin.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SellPriceCommand implements CommandExecutor {

    private final SellPlugin plugin;
    private final PriceManager priceManager;

    public SellPriceCommand(SellPlugin plugin) {
        this.plugin = plugin;
        this.priceManager = plugin.getPriceManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtils.color("&cCsak játékosok használhatják!"));
            return true;
        }

        var held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) {
            player.sendMessage(MessageUtils.color("&cTarts valamit a kezedben!"));
            return true;
        }

        Material mat = held.getType();
        if (!priceManager.hasPrice(mat)) {
            player.sendMessage(MessageUtils.color(
                plugin.getConfig().getString("no-price-message", "&cEnnek az itemnek nincs ára.")));
            return true;
        }

        double price = priceManager.getPrice(mat);
        double stackPrice = priceManager.getStackPrice(mat);
        String currency = priceManager.getCurrencySymbol();

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═══════════════════════").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("  Eladási ár lekérdezés").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("═══════════════════════").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("Item: ").color(NamedTextColor.GRAY)
            .append(Component.text(formatMaterial(mat)).color(NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Ár/db: ").color(NamedTextColor.GRAY)
            .append(Component.text(formatMoney(price) + " " + currency).color(NamedTextColor.YELLOW)));
        player.sendMessage(Component.text("Stack (64x): ").color(NamedTextColor.GRAY)
            .append(Component.text(formatMoney(stackPrice) + " " + currency).color(NamedTextColor.GOLD)));
        player.sendMessage(Component.text("Material: ").color(NamedTextColor.DARK_GRAY)
            .append(Component.text(mat.name()).color(TextColor.color(0x555577))));
        player.sendMessage(Component.text("═══════════════════════").color(NamedTextColor.GOLD));
        player.sendMessage(Component.empty());
        return true;
    }

    private String formatMaterial(Material mat) {
        String name = mat.name().toLowerCase().replace("_", " ");
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String formatMoney(double amount) {
        if (amount == (long) amount) return String.valueOf((long) amount);
        return String.format("%.2f", amount);
    }
}
