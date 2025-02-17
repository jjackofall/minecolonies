package com.minecolonies.coremod.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIRanger;
import net.minecraft.resources.ResourceLocation;

import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_LEVEL_NAME;
import static com.minecolonies.api.util.constant.GuardConstants.KNIGHT_HP_BONUS;

/**
 * The Ranger's Job class
 *
 * @author Asherslab
 */
public class JobRanger extends AbstractJobGuard<JobRanger>
{

    /**
     * The name associated with the job.
     */
    public static final String DESC = "com.minecolonies.coremod.job.Ranger";

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobRanger(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public EntityAIRanger generateGuardAI()
    {
        return new EntityAIRanger(this);
    }

    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.ARCHER_GUARD_ID;
    }
}
