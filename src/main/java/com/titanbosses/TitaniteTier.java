package com.titanbosses;

import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;

/**
 * Netherite: do ben 2031, toc do dao 9, sat thuong cong them 4, harvest level 4.
 * Titanite = gap 4 lan: do ben 8124, toc do dao 36, sat thuong cong them 28 (kiem = 32 sat thuong/don, gap 4 lan 8).
 */
public enum TitaniteTier implements IItemTier {
    INSTANCE;

    @Override
    public int getUses() {
        return 8124;
    }

    @Override
    public float getSpeed() {
        return 36.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 28.0F;
    }

    @Override
    public int getLevel() {
        return 4;
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.TITANITE_INGOT.get());
    }
}
