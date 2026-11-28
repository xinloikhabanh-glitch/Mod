package com.titanbosses.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;

import javax.annotation.Nullable;

/** Blaze Overlord: ban cau lua no lon (nhin thay ro) va thieu dot nguoi choi gan do. */
public class BlazeOverlordEntity extends BlazeEntity {
    private final ServerBossInfo bossInfo =
            new ServerBossInfo(this.getDisplayName(), BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_10);
    private int fireballTimer = 80;

    public BlazeOverlordEntity(EntityType<? extends BlazeEntity> type, World world) {
        super(type, world);
        this.xpReward = 1000;
        this.setPersistenceRequired();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.tickCount % 80 == 0) {
                BossSupport.aura(this, Effects.WEAKNESS, 100, 0, 10.0D);
            }
            if (--this.fireballTimer <= 0 && this.distanceToSqr(target) < 48.0D * 48.0D) {
                this.fireballTimer = 50;
                double dx = target.getX() - this.getX();
                double dy = target.getY(0.5D) - this.getY(0.5D);
                double dz = target.getZ() - this.getZ();
                FireballEntity fireball = new FireballEntity(this.level, this, dx, dy, dz);
                fireball.explosionPower = 4;
                fireball.setPos(this.getX(), this.getY(0.5D) + 0.5D, this.getZ());
                this.level.addFreshEntity(fireball);
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
