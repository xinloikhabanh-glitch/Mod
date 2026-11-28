package com.titanbosses;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Giap Minecraft chi giam toi da 80% sat thuong, nen sau khi Titanite dat tran, moi manh Titanite
 * giam them 12% sat thuong (du 4 manh = giam them 48%).
 */
public class ArmorEvents {
    private static final EquipmentSlotType[] ARMOR_SLOTS = {
        EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET
    };

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) {
            return;
        }
        if (event.getSource().isBypassInvul()) {
            return;
        }
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        int pieces = 0;
        for (EquipmentSlotType slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof ArmorItem
                    && ((ArmorItem) stack.getItem()).getMaterial() == TitaniteArmorMaterial.INSTANCE) {
                pieces++;
            }
        }
        if (pieces > 0) {
            event.setAmount(event.getAmount() * (1.0F - 0.12F * pieces));
        }
    }
}
