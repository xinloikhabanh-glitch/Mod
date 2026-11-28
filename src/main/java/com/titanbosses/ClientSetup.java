package com.titanbosses;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import com.titanbosses.entity.VenomMatriarchEntity;

/** Boss dung lai model cua mob goc, phong to len (he so khop voi kich thuoc trong ModEntities). */
@Mod.EventBusSubscriber(modid = TitanBossesMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.CRYPT_TITAN.get(),
                manager -> new ZombieRenderer(manager) {
                    @Override
                    protected void scale(ZombieEntity entity, MatrixStack stack, float partialTicks) {
                        super.scale(entity, stack, partialTicks);
                        stack.scale(3.5F, 3.5F, 3.5F);
                    }
                });

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BLAZE_OVERLORD.get(),
                manager -> new BlazeRenderer(manager) {
                    @Override
                    protected void scale(BlazeEntity entity, MatrixStack stack, float partialTicks) {
                        super.scale(entity, stack, partialTicks);
                        stack.scale(3.0F, 3.0F, 3.0F);
                    }
                });

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.VENOM_MATRIARCH.get(),
                manager -> new SpiderRenderer<VenomMatriarchEntity>(manager) {
                    @Override
                    protected void scale(VenomMatriarchEntity entity, MatrixStack stack, float partialTicks) {
                        super.scale(entity, stack, partialTicks);
                        stack.scale(4.0F, 4.0F, 4.0F);
                    }
                });

        // WitherSkeletonRenderer da tu phong to 1.2 lan, nen 2.92 * 1.2 = 3.5.
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.WITHER_LORD.get(),
                manager -> new WitherSkeletonRenderer(manager) {
                    @Override
                    protected void scale(AbstractSkeletonEntity entity, MatrixStack stack, float partialTicks) {
                        super.scale(entity, stack, partialTicks);
                        stack.scale(2.92F, 2.92F, 2.92F);
                    }
                });

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.STORM_RAVAGER.get(),
                manager -> new RavagerRenderer(manager) {
                    @Override
                    protected void scale(RavagerEntity entity, MatrixStack stack, float partialTicks) {
                        super.scale(entity, stack, partialTicks);
                        stack.scale(2.5F, 2.5F, 2.5F);
                    }
                });
    }
}
