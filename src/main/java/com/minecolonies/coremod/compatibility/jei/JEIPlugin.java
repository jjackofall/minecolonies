package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.coremod.colony.crafting.RecipeAnalyzer;
import com.minecolonies.coremod.compatibility.jei.transfer.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

@mezz.jei.api.JeiPlugin
public class JEIPlugin implements IModPlugin
{
    @NotNull
    @Override
    public ResourceLocation getPluginUid()
    {
        return new ResourceLocation(Constants.MOD_ID);
    }

    private final List<JobBasedRecipeCategory<?>> categories = new ArrayList<>();

    @Override
    public void registerCategories(@NotNull final IRecipeCategoryRegistration registration)
    {
        final IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        final IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        final IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();

        registration.addRecipeCategories(new CompostRecipeCategory(guiHelper));
        registration.addRecipeCategories(new FishermanRecipeCategory(guiHelper));

        categories.clear();
        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
            final Map<JobEntry, GenericRecipeCategory> craftingCategories = new HashMap<>();

            for (final Supplier<IBuildingModule> producer : building.getModuleProducers())
            {
                final IBuildingModule module = producer.get();

                if (module instanceof final ICraftingBuildingModule crafting)
                {
                    final IJob<?> job = crafting.getCraftingJob();
                    if (job != null)
                    {
                        GenericRecipeCategory category = craftingCategories.get(job.getJobRegistryEntry());
                        if (category == null)
                        {
                            category = new GenericRecipeCategory(building, job, crafting, guiHelper, modIdHelper);
                            craftingCategories.put(job.getJobRegistryEntry(), category);
                        }
                        else
                        {
                            category.addModule(crafting);
                        }
                    }
                }

                if (module instanceof final AnimalHerdingModule herding)
                {
                    registerCategory(registration, new HerderRecipeCategory(building, herding.getHerdingJob(), herding, guiHelper));
                }
            }

            for (final GenericRecipeCategory category : craftingCategories.values())
            {
                registerCategory(registration, category);
            }
        }
    }

    private void registerCategory(@NotNull final IRecipeCategoryRegistration registration,
                                  @NotNull final JobBasedRecipeCategory<?> category)
    {
        categories.add(category);
        registration.addRecipeCategories(category);
    }

    @Override
    public void registerRecipes(@NotNull final IRecipeRegistration registration)
    {
        registration.addIngredientInfo(new ItemStack(ModBlocks.blockHutComposter.asItem()), VanillaTypes.ITEM,
                new TranslatableComponent(TranslationConstants.PARTIAL_JEI_INFO + ModJobs.COMPOSTER_ID.getPath()));

        registration.addRecipes(CompostRecipeCategory.findRecipes(), CompostRecipe.ID);
        registration.addRecipes(FishermanRecipeCategory.findRecipes(), ModJobs.FISHERMAN_ID);

        final ClientLevel level = Minecraft.getInstance().level;
        final Map<CraftingType, List<IGenericRecipe>> vanilla = RecipeAnalyzer.buildVanillaRecipesMap(level.getRecipeManager(), level);

        for (final JobBasedRecipeCategory<?> category : this.categories)
        {
            try
            {
                registration.addRecipes(category.findRecipes(vanilla), category.getUid());
            }
            catch (Exception e)
            {
                Log.getLogger().error("Failed to process recipes for {}", category.getTitle(), e);
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull final IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockBarrel), CompostRecipe.ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockHutComposter), CompostRecipe.ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockHutFisherman), ModJobs.FISHERMAN_ID);

        for (final JobBasedRecipeCategory<?> category : this.categories)
        {
            registration.addRecipeCatalyst(category.getCatalyst(), category.getUid());
        }
    }

    @Override
    public void registerRecipeTransferHandlers(@NotNull final IRecipeTransferRegistration registration)
    {
        registration.addRecipeTransferHandler(new PrivateCraftingTeachingTransferHandler(registration.getTransferHelper()), VanillaRecipeCategoryUid.CRAFTING);
        registration.addRecipeTransferHandler(new PrivateSmeltingTeachingTransferHandler(registration.getTransferHelper()), VanillaRecipeCategoryUid.FURNACE);
        registration.addRecipeTransferHandler(new PrivateBrewingTeachingTransferHandler(registration.getTransferHelper()), VanillaRecipeCategoryUid.BREWING);
    }

    @Override
    public void registerGuiHandlers(@NotNull final IGuiHandlerRegistration registration)
    {
        new CraftingGuiHandler(this.categories).register(registration);
        new FurnaceCraftingGuiHandler(this.categories).register(registration);
        new BrewingCraftingGuiHandler(this.categories).register(registration);
    }

}
