package com.ldtteam.domumornamentum.recipe;

import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Finished cutter recipe for data generation. Minecraft 26 serializes recipe
 * instances through their codec rather than the removed FinishedRecipe interface.
 */
public abstract class FinishedDORecipe
{
    public RecipeSerializer<?> getType()
    {
        return ModRecipeSerializers.ARCHITECTS_CUTTER.get();
    }

    public abstract ResourceKey<Recipe<?>> getId();

    public abstract ArchitectsCutterRecipe getRecipe();

    public void save(final RecipeOutput output)
    {
        output.accept(getId(), getRecipe(), null);
    }
}
