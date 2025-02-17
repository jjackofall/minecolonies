package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.client.event.RenderLevelLastEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Main class for handling world rendering.
 * Also holds all possible values which may be needed during rendering.
 */
public class WorldEventContext
{
    public static final WorldEventContext INSTANCE = new WorldEventContext();

    private WorldEventContext()
    {
        // singleton
    }

    BufferSource bufferSource;
    PoseStack poseStack;
    float partialTicks;
    ClientLevel clientLevel;
    LocalPlayer clientPlayer;
    ItemStack mainHandItem;
    @Nullable
    IColonyView nearestColony;
    /**
     * In chunks
     */
    int clientRenderDist;

    public void renderWorldLastEvent(final RenderLevelLastEvent event)
    {
        bufferSource = WorldRenderMacros.getBufferSource();
        poseStack = event.getPoseStack();
        partialTicks = event.getPartialTick();
        clientLevel = Minecraft.getInstance().level;
        clientPlayer = Minecraft.getInstance().player;
        mainHandItem = clientPlayer.getMainHandItem();
        nearestColony = IColonyManager.getInstance().getClosestColonyView(clientLevel, clientPlayer.blockPosition());
        clientRenderDist = Minecraft.getInstance().options.renderDistance;

        final Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        ColonyBorderRenderer.render(this); // renders directly (not into bufferSource)

        NearColonyBuildingsRenderer.render(this);
        ColonyWaypointRenderer.render(this);
        ColonyPatrolPointRenderer.render(this);
        GuardTowerRallyBannerRenderer.render(this);
        HighlightManager.render(this);
        PathfindingDebugRenderer.render(this);

        bufferSource.endBatch();

        poseStack.popPose();
    }

    boolean hasNearestColony()
    {
        return nearestColony != null;
    }
}
