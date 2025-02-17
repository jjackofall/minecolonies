package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.network.IMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client.
 */
public class ColonyViewBuildingViewMessage implements IMessage
{
    private int          colonyId;
    private BlockPos     buildingId;
    private FriendlyByteBuf buildingData;

    /**
     * Dimension of the colony.
     */
    private ResourceKey<Level> dimension;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewBuildingViewMessage()
    {
        super();
    }

    /**
     * Creates a message to handle colony views.
     *
     * @param building AbstractBuilding to add or update a view.
     */
    public ColonyViewBuildingViewMessage(@NotNull final IBuilding building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.buildingData = new FriendlyByteBuf(Unpooled.buffer());
        building.serializeToView(this.buildingData);
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
        buildingData = new FriendlyByteBuf(Unpooled.buffer(buf.readableBytes()));
        buf.readBytes(buildingData, buf.readableBytes());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeUtf(dimension.location().toString());
        buf.writeBytes(buildingData);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        IColonyManager.getInstance().handleColonyBuildingViewMessage(colonyId, buildingId, buildingData, dimension);
    }
}
