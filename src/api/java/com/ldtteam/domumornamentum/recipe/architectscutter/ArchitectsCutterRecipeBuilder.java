package com.ldtteam.domumornamentum.recipe.architectscutter;

import com.ldtteam.domumornamentum.IDomumOrnamentumApi;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.util.DataComponentPatchBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inspired by {@link RecipeBuilder}.
 */
public class ArchitectsCutterRecipeBuilder
{
    private final RecipeCategory category;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private final Block result;
    private int count = 1;
    private final DataComponentPatchBuilder components = new DataComponentPatchBuilder();

    /**
     * @param result main result block of recipe
     * @param category recipe category as in {@link VanillaRecipeProvider}
     */
    public <T extends Block & IMateriallyTexturedBlock> ArchitectsCutterRecipeBuilder(
        final T result,
        final RecipeCategory category)
    {
        this.result = result;
        this.category = category;
    }

    public <T extends Comparable<T>> ArchitectsCutterRecipeBuilder resultProperty(
        final Property<T> property,
        final T value)
    {
        components.update(
            DataComponents.BLOCK_STATE,
            BlockItemStateProperties.EMPTY,
            props -> props.with(property, value));

        return this;
    }

    public ArchitectsCutterRecipeBuilder textureData(final MaterialTextureData textureData)
    {
        if (textureData.isEmpty())
        {
            return this;
        }

        components.set(
            IDomumOrnamentumApi.getInstance().getMaterialTextureComponentType(),
            textureData);

        return this;
    }

    public ArchitectsCutterRecipeBuilder count(final int count)
    {
        this.count = count;
        return this;
    }

    public ArchitectsCutterRecipeBuilder unlockedBy(
        final String criterionId,
        final Criterion<?> criterion)
    {
        this.criteria.put(criterionId, criterion);
        return this;
    }

    /**
     * Compatibility overload for existing data generators that still pass an Identifier.
     */
    public void save(final RecipeOutput output, final Identifier recipeId)
    {
        save(output, ResourceKey.create(Registries.RECIPE, recipeId));
    }

    /**
     * Minecraft 26.1 recipe output API uses recipe resource keys.
     */
    public void save(
        final RecipeOutput output,
        final ResourceKey<Recipe<?>> recipeId)
    {
        final ArchitectsCutterRecipe recipe = new ArchitectsCutterRecipe(
            BuiltInRegistries.BLOCK.getKey(result),
            count,
            components.build());

        if (criteria.isEmpty())
        {
            output.accept(recipeId, recipe, null);
            return;
        }

        final RecipeUnlockAdvancementBuilder advancement =
            new RecipeUnlockAdvancementBuilder();

        criteria.forEach(advancement::unlockedBy);

        output.accept(
            recipeId,
            recipe,
            advancement.build(output, recipeId, category));
    }

    public void saveSuffix(final RecipeOutput output, final String suffix)
    {
        save(
            output,
            BuiltInRegistries.BLOCK.getKey(result).withSuffix("_" + suffix));
    }

    public void save(final RecipeOutput output, final String name)
    {
        save(
            output,
            BuiltInRegistries.BLOCK.getKey(result).withPath(name));
    }

    public void save(final RecipeOutput output)
    {
        save(output, BuiltInRegistries.BLOCK.getKey(result));
    }
}
