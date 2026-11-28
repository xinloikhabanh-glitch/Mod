package com.titanbosses.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;

import javax.annotation.Nullable;

/** Storm Ravager (boss cuoi): ravager khong lo, goi set danh vao muc tieu. */
public class StormRavagerEntity extends RavagerEntity {
    private final ServerBossInfo bossInfo =
            new ServerBossInfo(this.getDisplayName(), BossInfo.Color.BLUE, BossInfo.Overlay.NOTCHED_20);
    private int lightningTimer = 120;

    public StormRavagerEntity(EntityType<? extends RavagerEntity> type, World world) {
        super(type, world);
        this.xpReward = 2500;
        this.setPersistenceRequired();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.tickCount % 80 == 0) {
                BossSupport.aura(this, Effects.MOVEMENT_SLOWDOWN, 80, 0, 12.0D);
            }
            if (--this.lightningTimer <= 0 && this.distanceToSqr(target) < 40.0D * 40.0D) {
                this.lightningTimer = 140;
                BossSupport.lightning(this, target, 30.0F);
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
