package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.BlockMinecoloniesNamedGrave;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler.ActionProcessingResult;

public class NamedGravePlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof BlockMinecoloniesNamedGrave;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete,
      final BlockPos centerPos,
      final PlacementSettings settings)
    {
        if (world.getBlockState(pos).getBlock() == ModBlocks.blockNamedGrave)
        {
            return ActionProcessingResult.SUCCESS;
        }

        if (complete)
        {
            world.setBlockAndUpdate(pos, blockState);
            return ActionProcessingResult.SUCCESS;
        }

        return ActionProcessingResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getRequiredItems(
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete)
    {
        return Collections.emptyList();
    }
}
