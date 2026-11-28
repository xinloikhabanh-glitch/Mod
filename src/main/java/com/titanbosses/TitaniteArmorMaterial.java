package com.titanbosses;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

/**
 * Netherite: he so ben 37, giap 3/6/8/3, toughness 3, chong day 0.1.
 * Titanite gap 4 lan: he so ben 148, giap 12/24/32/12, toughness 12, chong day 0.4.
 * (Minecraft gioi han giap toi da 30 va giam sat thuong toi da 80%, nen phan vuot tran duoc bu bang
 *  ArmorEvents: moi manh Titanite giam them 12% sat thuong.)
 */
public enum TitaniteArmorMaterial implements IArmorMaterial {
    INSTANCE;

    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
    // Thu tu theo EquipmentSlotType.getIndex(): FEET, LEGS, CHEST, HEAD.
    private static final int[] NETHERITE_DEFENSE = {3, 6, 8, 3};

    @Override
    public int getDurabilityForSlot(EquipmentSlotType slot) {
        return BASE_DURABILITY[slot.getIndex()] * 148;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlotType slot) {
        return NETHERITE_DEFENSE[slot.getIndex()] * 4;
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.TITANITE_INGOT.get());
    }

    @Override
    public String getName() {
        // Dung texture giap netherite cua Minecraft (thay bang texture rieng neu muon).
        return "netherite";
    }

    @Override
    public float getToughness() {
        return 12.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.4F;
    }
}
