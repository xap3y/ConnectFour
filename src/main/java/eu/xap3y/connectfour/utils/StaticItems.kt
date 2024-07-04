package eu.xap3y.connectfour.utils

import com.cryptomorin.xseries.XEnchantment
import com.cryptomorin.xseries.XMaterial
import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.xagui.models.GuiButton
import eu.xap3y.xalib.managers.Texter
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object StaticItems {

    val borderPane = (XMaterial.BLACK_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1))
    val redPane = (XMaterial.RED_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.RED_STAINED_GLASS, 1)).apply { itemMeta = itemMeta?.apply { setDisplayName(Texter.colored("&cRed")) } }
    val yellowPane = (XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1)).apply { itemMeta = itemMeta?.apply { setDisplayName(Texter.colored("&eYellow")) } }
    //private val bluePane = XMaterial.BLUE_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1)
    val greenPane = XMaterial.LIME_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.LIME_STAINED_GLASS_PANE, 1)

    val redPaneGlow = GuiButton(redPane.clone().apply { itemMeta = itemMeta?.apply {
        addItemFlags(ItemFlag.HIDE_ENCHANTS)
        //if (!ConnectFour.useOld) try { addEnchant(Enchantment.MENDING, 1, true) } catch (_: Exception) { } // 1.8 doesn't support MENDING
        addEnchant(Enchantment.SILK_TOUCH, 1, true)
    }})

    val yellowPaneGlow = GuiButton(yellowPane.clone().apply { itemMeta = itemMeta?.apply {
        addItemFlags(ItemFlag.HIDE_ENCHANTS)
        //if (!ConnectFour.useOld) try { addEnchant(Enchantment.MENDING, 1, true) } catch (_: Exception) { } // 1.8 doesn't support MENDING
        addEnchant(Enchantment.SILK_TOUCH, 1, true)
    }})

    val greenPaneGlow = GuiButton(greenPane.clone().apply { itemMeta = itemMeta?.apply {
        addItemFlags(ItemFlag.HIDE_ENCHANTS)
        //if (!ConnectFour.useOld) try { addEnchant(Enchantment.MENDING, 1, true) } catch (_: Exception) { } // 1.8 doesn't support MENDING
        addEnchant(Enchantment.SILK_TOUCH, 1, true)
    }})
}