package com.titanbosses;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(TitanBossesMod.MODID)
public class TitanBossesMod {
    public static final String MODID = "titanbosses";

    public TitanBossesMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        modBus.addListener(this::registerAttributes);
        MinecraftForge.EVENT_BUS.register(new ArmorEvents());
    }

    /**
     * Chi so boss. Boss goc (1.16.5): Ender Dragon 200 HP, Wither 300 HP, Elder Guardian 80 HP, Ravager 100 HP.
     * Cac boss moi co mau >= 5 lan Wither (300 HP) va sat thuong >= 5 lan don danh cua boss goc.
     */
    private static MutableAttribute stats(MutableAttribute base, double hp, double damage, double armor,
                                          double speed, double follow) {
        return base.add(Attributes.MAX_HEALTH, hp)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.FOLLOW_RANGE, follow);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CRYPT_TITAN.get(),
                stats(ZombieEntity.createAttributes(), 1500, 50, 20, 0.26D, 48).build());
        event.put(ModEntities.BLAZE_OVERLORD.get(),
                stats(BlazeEntity.createAttributes(), 1600, 40, 20, 0.28D, 64).build());
        event.put(ModEntities.VENOM_MATRIARCH.get(),
                stats(SpiderEntity.createAttributes(), 1800, 55, 20, 0.34D, 48).build());
        event.put(ModEntities.WITHER_LORD.get(),
                stats(AbstractSkeletonEntity.createAttributes(), 2000, 60, 24, 0.30D, 48).build());
        event.put(ModEntities.STORM_RAVAGER.get(),
                stats(RavagerEntity.createAttributes(), 2500, 75, 28, 0.32D, 48).build());
    }
}
