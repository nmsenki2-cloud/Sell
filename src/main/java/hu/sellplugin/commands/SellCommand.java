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
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class SellCommand implements CommandExecutor {

    private final SellPlugin plugin;
    private final PriceManager priceManager;

    public SellCommand(SellPlugin plugin) {
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

        if (!player.hasPermission("sellplugin.sell")) {
            player.sendMessage(MessageUtils.color(
                plugin.getConfig().getString("no-permission-message", "&cNincs jogosultságod!")));
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();

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

        if (args.length > 0 && args[0].equalsIgnoreCase("all")) {
            sellAll(player, mat);
            return true;
        }

        int amount;
        if (args.length > 0) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount <= 0) {
                    player.sendMessage(MessageUtils.color("&cÉrvényes mennyiséget adj meg!"));
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.color("&cÉrvénytelen szám: &f" + args[0]));
                return true;
            }
        } else {
            amount = held.getAmount();
        }

        int available = countItems(player.getInventory(), mat);
        if (amount > available) {
            player.sendMessage(MessageUtils.color("&cNincs elég item! Elérhető: &f" + available));
            return true;
        }

        double total = priceManager.getPrice(mat) * amount;
        removeItems(player.getInventory(), mat, amount);
        giveMoneyFeedback(player, mat, amount, total);
        return true;
    }

    private void sellAll(Player player, Material mat) {
        int amount = countItems(player.getInventory(), mat);
        if (amount == 0) {
            player.sendMessage(MessageUtils.color("&cNincs eladható itemed ebből!"));
            return;
        }
        double total = priceManager.getPrice(mat) * amount;
        removeItems(player.getInventory(), mat, amount);
        giveMoneyFeedback(player, mat, amount, total);
    }

    private void giveMoneyFeedback(Player player, Material mat, int amount, double total) {
        String msg = plugin.getConfig().getString("sell-message",
            "&aEladtad: &f{amount}x {item} &aért &e{total} {currency}&a!");
        msg = msg.replace("{amount}", String.valueOf(amount))
                 .replace("{item}", formatMaterial(mat))
                 .replace("{total}", formatMoney(total))
                 .replace("{currency}", priceManager.getCurrencySymbol());
        player.sendMessage(MessageUtils.color(msg));
    }

    private int countItems(PlayerInventory inv, Material mat) {
        int count = 0;
        for (ItemStack is : inv.getContents()) {
            if (is != null && is.getType() == mat) count += is.getAmount();
        }
        return count;
    }

    private void removeItems(PlayerInventory inv, Material mat, int amount) {
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length && amount > 0; i++) {
            ItemStack is = contents[i];
            if (is != null && is.getType() == mat) {
                if (is.getAmount() <= amount) {
                    amount -= is.getAmount();
                    contents[i] = null;
                } else {
                    is.setAmount(is.getAmount() - amount);
                    amount = 0;
                }
            }
        }
        inv.setContents(contents);
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
