package dev.linqfy.legendary;

import dev.linqfy.bigCasares.modules.legendary.LegendaryWeapon;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;

public final class LegendaryItemRegistry {
    private static final NamespacedKey ID = new NamespacedKey("bigcasares", "item_id");

    public ItemStack createItemStack(String id, int amount) {
        LegendaryWeapon weapon = LegendaryWeapon.find(id).orElseThrow();
        if (amount != 1) throw new IllegalArgumentException("Las armas son únicas por espacio.");
        ItemStack item = new ItemStack(Material.valueOf(weapon.material), 1);
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ID, PersistentDataType.STRING, weapon.id);
        meta.displayName(Component.text(weapon.displayName));
        meta.lore(List.of(Component.text(weapon.abilityName), Component.text("Clic derecho: habilidad · 40 segundos")));
        meta.setMaxStackSize(1);
        meta.setItemModel(new NamespacedKey("bigcasares", weapon.id));
        if (weapon == LegendaryWeapon.THUNDER_SPEAR) meta.addEnchant(Enchantment.LUNGE, 3, true);
        item.setItemMeta(meta);
        return item;
    }

    public Optional<String> resolveItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Optional.empty();
        return Optional.ofNullable(item.getItemMeta().getPersistentDataContainer().get(ID, PersistentDataType.STRING));
    }
}
