package com.titanbosses;

import net.minecraft.entity.EntityType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.Rarity;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TitanBossesMod.MODID);

    private static Item.Properties gear() {
        return new Item.Properties().tab(ItemGroup.TAB_COMBAT).fireResistant().rarity(Rarity.EPIC);
    }

    private static Item.Properties material() {
        return new Item.Properties().tab(ItemGroup.TAB_MATERIALS).fireResistant().rarity(Rarity.EPIC);
    }

    // ---- Nguyen lieu ----
    public static final RegistryObject<Item> TITANITE_SHARD = ITEMS.register("titanite_shard",
            () -> new Item(material()));
    public static final RegistryObject<Item> TITANITE_INGOT = ITEMS.register("titanite_ingot",
            () -> new Item(material()));

    // ---- Cong cu (netherite: kiem 8 sat thuong -> Titanite 32; toc do dao 9 -> 36; do ben 2031 -> 8124) ----
    public static final RegistryObject<Item> TITANITE_SWORD = ITEMS.register("titanite_sword",
            () -> new SwordItem(TitaniteTier.INSTANCE, 3, -2.4F, gear()));
    public static final RegistryObject<Item> TITANITE_PICKAXE = ITEMS.register("titanite_pickaxe",
            () -> new PickaxeItem(TitaniteTier.INSTANCE, 1, -2.8F, gear()));
    public static final RegistryObject<Item> TITANITE_AXE = ITEMS.register("titanite_axe",
            () -> new AxeItem(TitaniteTier.INSTANCE, 11.0F, -3.0F, gear()));
    public static final RegistryObject<Item> TITANITE_SHOVEL = ITEMS.register("titanite_shovel",
            () -> new ShovelItem(TitaniteTier.INSTANCE, 1.5F, -3.0F, gear()));
    public static final RegistryObject<Item> TITANITE_HOE = ITEMS.register("titanite_hoe",
            () -> new HoeItem(TitaniteTier.INSTANCE, -4, 0.0F, gear()));

    // ---- Giap ----
    public static final RegistryObject<Item> TITANITE_HELMET = ITEMS.register("titanite_helmet",
            () -> new ArmorItem(TitaniteArmorMaterial.INSTANCE, EquipmentSlotType.HEAD, gear()));
    public static final RegistryObject<Item> TITANITE_CHESTPLATE = ITEMS.register("titanite_chestplate",
            () -> new ArmorItem(TitaniteArmorMaterial.INSTANCE, EquipmentSlotType.CHEST, gear()));
    public static final RegistryObject<Item> TITANITE_LEGGINGS = ITEMS.register("titanite_leggings",
            () -> new ArmorItem(TitaniteArmorMaterial.INSTANCE, EquipmentSlotType.LEGS, gear()));
    public static final RegistryObject<Item> TITANITE_BOOTS = ITEMS.register("titanite_boots",
            () -> new ArmorItem(TitaniteArmorMaterial.INSTANCE, EquipmentSlotType.FEET, gear()));

    // ---- Sigil trieu hoi boss ----
    public static final RegistryObject<Item> CRYPT_TITAN_SIGIL = sigil("crypt_titan_sigil", ModEntities.CRYPT_TITAN);
    public static final RegistryObject<Item> BLAZE_OVERLORD_SIGIL = sigil("blaze_overlord_sigil", ModEntities.BLAZE_OVERLORD);
    public static final RegistryObject<Item> VENOM_MATRIARCH_SIGIL = sigil("venom_matriarch_sigil", ModEntities.VENOM_MATRIARCH);
    public static final RegistryObject<Item> WITHER_LORD_SIGIL = sigil("wither_lord_sigil", ModEntities.WITHER_LORD);
    public static final RegistryObject<Item> STORM_RAVAGER_SIGIL = sigil("storm_ravager_sigil", ModEntities.STORM_RAVAGER);

    private static RegistryObject<Item> sigil(String name, final RegistryObject<? extends EntityType<?>> type) {
        return ITEMS.register(name, () -> new SigilItem(type, new Item.Properties()
                .tab(ItemGroup.TAB_MISC).stacksTo(16).fireResistant().rarity(Rarity.EPIC)));
    }

    private ModItems() {
    }
}
