package com.worldbattles.nametags.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void worldbattles$hidePlayerNametag(T entity, Component displayName, PoseStack poseStack,
                                                MultiBufferSource bufferSource, int packedLight,
                                                CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (entity instanceof Player
                && minecraft.player != null
                && Level.OVERWORLD.equals(minecraft.player.level().dimension())) {
            ci.cancel();
        }
    }
}
