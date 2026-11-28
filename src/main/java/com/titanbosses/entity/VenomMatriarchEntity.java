package com.titanbosses.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CaveSpiderEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;

import javax.annotation.Nullable;

/** Venom Matriarch: nhen khong lo, tiet doc quanh minh va de ra nhen hang. */
public class VenomMatriarchEntity extends SpiderEntity {
    private final ServerBossInfo bossInfo =
            new ServerBossInfo(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.NOTCHED_10);
    private int summonTimer = 160;

    public VenomMatriarchEntity(EntityType<? extends SpiderEntity> type, World world) {
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
            if (this.tickCount % 40 == 0) {
                BossSupport.aura(this, Effects.POISON, 100, 1, 12.0D);
                BossSupport.aura(this, Effects.MOVEMENT_SLOWDOWN, 100, 1, 12.0D);
            }
            if (--this.summonTimer <= 0) {
                this.summonTimer = 240;
                if (BossSupport.countNearby(this, CaveSpiderEntity.class, 32.0D) < 20) {
                    BossSupport.summon(EntityType.CAVE_SPIDER, this, target, 5);
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
