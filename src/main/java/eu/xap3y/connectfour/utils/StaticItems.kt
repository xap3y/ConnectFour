package eu.xap3y.connectfour.utils

import com.cryptomorin.xseries.XEnchantment
import com.cryptomorin.xseries.XMaterial
import eu.xap3y.xagui.models.GuiButton
import eu.xap3y.xalib.managers.Texter
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object StaticItems {

    val borderPane = (XMaterial.BLACK_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1))
    val redPane = (XMaterial.RED_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1)).apply { itemMeta = itemMeta?.apply { displayName = Texter.colored("&cRed") } }
    val yellowPane = (XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1)).apply { itemMeta = itemMeta?.apply { displayName = Texter.colored("&eYellow") } }
    //private val bluePane = XMaterial.BLUE_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1)
    val greenPane = XMaterial.LIME_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1)

    val redPaneGlow = GuiButton(redPane.clone().apply { itemMeta = itemMeta?.apply {
        addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }}).addEnchantment(XEnchantment.SILK_TOUCH.enchant!!, 1)

    val yellowPaneGlow = GuiButton(yellowPane.clone().apply { itemMeta = itemMeta?.apply {
        addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }}).addEnchantment(XEnchantment.SILK_TOUCH.enchant!!, 1)
}