package com.titanbosses.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/** Cac chieu thuc dung chung cua boss. Chi goi o phia server. */
final class BossSupport {

    private BossSupport() {
    }

    /** Gay hieu ung len moi nguoi choi (khong phai Creative/Spectator) trong ban kinh range. */
    static void aura(LivingEntity boss, Effect effect, int durationTicks, int amplifier, double range) {
        for (PlayerEntity p : boss.level.getEntitiesOfClass(PlayerEntity.class, boss.getBoundingBox().inflate(range))) {
            if (!p.isCreative() && !p.isSpectator()) {
                p.addEffect(new EffectInstance(effect, durationTicks, amplifier));
            }
        }
    }

    /** Trieu hoi dong minh quanh boss, nham vao muc tieu. */
    static void summon(EntityType<? extends MobEntity> type, MobEntity boss, LivingEntity target, int count) {
        World world = boss.level;
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld sw = (ServerWorld) world;
        for (int i = 0; i < count; i++) {
            MobEntity minion = type.create(sw);
            if (minion == null) {
                continue;
            }
            minion.moveTo(boss.getX() + boss.getRandom().nextInt(9) - 4, boss.getY(),
                    boss.getZ() + boss.getRandom().nextInt(9) - 4, boss.getRandom().nextFloat() * 360.0F, 0.0F);
            minion.finalizeSpawn(sw, sw.getCurrentDifficultyAt(minion.blockPosition()), SpawnReason.MOB_SUMMONED, null, null);
            if (target != null) {
                minion.setTarget(target);
            }
            sw.addFreshEntity(minion);
        }
    }

    /** So dong minh cung loai dang song gan boss (de gioi han so luong). */
    static int countNearby(MobEntity boss, Class<? extends MobEntity> type, double range) {
        return boss.level.getEntitiesOfClass(type, boss.getBoundingBox().inflate(range)).size();
    }

    /** Set danh: sam set + them sat thuong vat ly (bi giap giam binh thuong). */
    static void lightning(MobEntity boss, LivingEntity target, float extraDamage) {
        World world = boss.level;
        if (!(world instanceof ServerWorld)) {
            return;
        }
        LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.moveTo(target.getX(), target.getY(), target.getZ());
            world.addFreshEntity(bolt);
        }
        target.hurt(DamageSource.mobAttack(boss), extraDamage);
    }
}
