package com.titanbosses;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import com.titanbosses.entity.BlazeOverlordEntity;
import com.titanbosses.entity.CryptTitanEntity;
import com.titanbosses.entity.StormRavagerEntity;
import com.titanbosses.entity.VenomMatriarchEntity;
import com.titanbosses.entity.WitherLordEntity;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, TitanBossesMod.MODID);

    // Kich thuoc = kich thuoc mob goc nhan voi he so phong to (phai khop voi ClientSetup).
    public static final RegistryObject<EntityType<CryptTitanEntity>> CRYPT_TITAN = ENTITIES.register("crypt_titan",
            () -> EntityType.Builder.<CryptTitanEntity>of(CryptTitanEntity::new, EntityClassification.MONSTER)
                    .sized(2.1F, 6.8F).fireImmune().clientTrackingRange(16).build("crypt_titan"));

    public static final RegistryObject<EntityType<BlazeOverlordEntity>> BLAZE_OVERLORD = ENTITIES.register("blaze_overlord",
            () -> EntityType.Builder.<BlazeOverlordEntity>of(BlazeOverlordEntity::new, EntityClassification.MONSTER)
                    .sized(1.8F, 5.4F).fireImmune().clientTrackingRange(16).build("blaze_overlord"));

    public static final RegistryObject<EntityType<VenomMatriarchEntity>> VENOM_MATRIARCH = ENTITIES.register("venom_matriarch",
            () -> EntityType.Builder.<VenomMatriarchEntity>of(VenomMatriarchEntity::new, EntityClassification.MONSTER)
                    .sized(5.6F, 3.6F).fireImmune().clientTrackingRange(16).build("venom_matriarch"));

    public static final RegistryObject<EntityType<WitherLordEntity>> WITHER_LORD = ENTITIES.register("wither_lord",
            () -> EntityType.Builder.<WitherLordEntity>of(WitherLordEntity::new, EntityClassification.MONSTER)
                    .sized(2.45F, 8.4F).fireImmune().clientTrackingRange(16).build("wither_lord"));

    public static final RegistryObject<EntityType<StormRavagerEntity>> STORM_RAVAGER = ENTITIES.register("storm_ravager",
            () -> EntityType.Builder.<StormRavagerEntity>of(StormRavagerEntity::new, EntityClassification.MONSTER)
                    .sized(4.9F, 5.5F).fireImmune().clientTrackingRange(16).build("storm_ravager"));

    private ModEntities() {
    }
}
