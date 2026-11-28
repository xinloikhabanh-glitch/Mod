package com.titanbosses.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;

import javax.annotation.Nullable;

/** Crypt Titan: zombie khong lo. Danh yeu (Weakness) quanh minh va goi dong zombie. */
public class CryptTitanEntity extends ZombieEntity {
    private final ServerBossInfo bossInfo =
            new ServerBossInfo(this.getDisplayName(), BossInfo.Color.GREEN, BossInfo.Overlay.NOTCHED_10);
    private int summonTimer = 200;

    public CryptTitanEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
        this.xpReward = 1000;
        this.setPersistenceRequired();
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {
        // Boss khong bao gio la zombie con.
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.tickCount % 60 == 0) {
                BossSupport.aura(this, Effects.WEAKNESS, 120, 1, 14.0D);
            }
            if (--this.summonTimer <= 0) {
                this.summonTimer = 300;
                if (BossSupport.countNearby(this, ZombieEntity.class, 32.0D) < 20) {
                    BossSupport.summon(EntityType.ZOMBIE, this, target, 4);
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
