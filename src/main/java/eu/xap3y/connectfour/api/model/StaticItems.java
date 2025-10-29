package eu.xap3y.connectfour.api.model;

import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.connectfour.manager.LangManager;
import eu.xap3y.connectfour.service.Texter;
import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class StaticItems {

    private StaticItems() {}

    public static final ItemStack borderPane = XMaterial.BLACK_STAINED_GLASS_PANE.parseItem() != null
            ? XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()
            : new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);

    public static final ItemStack redPane = withName(
            XMaterial.RED_STAINED_GLASS_PANE.parseItem() != null
                    ? XMaterial.RED_STAINED_GLASS_PANE.parseItem()
                    : new ItemStack(Material.RED_STAINED_GLASS, 1),
            Texter.colored(LangManager.getString("gui.red") != null ? LangManager.getString("gui.red") : "&cRed")
    );

    public static final ItemStack yellowPane = withName(
            XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem() != null
                    ? XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem()
                    : new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1),
            Texter.colored(LangManager.getString("gui.yellow") != null ? LangManager.getString("gui.yellow") : "&eYellow")
    );

    public static final ItemStack greenPane = XMaterial.LIME_STAINED_GLASS_PANE.parseItem() != null
            ? XMaterial.LIME_STAINED_GLASS_PANE.parseItem()
            : new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);

    public static final ItemStack orangePane = XMaterial.ORANGE_STAINED_GLASS_PANE.parseItem() != null
            ? XMaterial.ORANGE_STAINED_GLASS_PANE.parseItem()
            : new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1);

    public static final GuiButton redPaneGlow = makeGlow(redPane.clone());
    public static final GuiButton yellowPaneGlow = makeGlow(yellowPane.clone());
    public static final GuiButton greenPaneGlow = makeGlow(greenPane.clone());

    private static ItemStack withName(ItemStack stack, String name) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static GuiButton makeGlow(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
            stack.setItemMeta(meta);
        }
        return new GuiButton(stack);
    }
}
