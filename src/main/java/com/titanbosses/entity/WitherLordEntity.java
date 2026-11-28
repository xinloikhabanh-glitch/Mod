package com.titanbosses.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;

import javax.annotation.Nullable;

/** Wither Lord: chua te xuong Wither. Gay hieu ung Wither len ke dich gan do va goi linh Wither Skeleton. */
public class WitherLordEntity extends WitherSkeletonEntity {
    private final ServerBossInfo bossInfo =
            new ServerBossInfo(this.getDisplayName(), BossInfo.Color.WHITE, BossInfo.Overlay.NOTCHED_10);
    private int summonTimer = 240;

    public WitherLordEntity(EntityType<? extends WitherSkeletonEntity> type, World world) {
        super(type, world);
        this.xpReward = 1500;
        this.setPersistenceRequired();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.tickCount % 60 == 0) {
                BossSupport.aura(this, Effects.WITHER, 100, 1, 10.0D);
            }
            if (--this.summonTimer <= 0) {
                this.summonTimer = 400;
                if (BossSupport.countNearby(this, WitherSkeletonEntity.class, 32.0D) < 12) {
                    BossSupport.summon(EntityType.WITHER_SKELETON, this, target, 3);
                }
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayerEntity player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayerEntity player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable ITextComponent name) {
        super.setCustomName(name);
        this.bossInfo.setName(this.getDisplayName());
    }
}
