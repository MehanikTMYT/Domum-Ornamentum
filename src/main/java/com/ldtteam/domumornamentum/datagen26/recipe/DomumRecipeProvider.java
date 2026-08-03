package com.ldtteam.domumornamentum.datagen26.recipe;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.ModBlocks;
import com.ldtteam.domumornamentum.block.decorative.FloatingCarpetBlock;
import com.ldtteam.domumornamentum.block.types.ExtraBlockType;
import com.ldtteam.domumornamentum.util.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal NeoForge 26.1 recipe datagen.
 * <p>
 * Consolidates the recipe-building logic that previously lived in the (currently excluded, pre-26.1)
 * {@code com.ldtteam.domumornamentum.datagen} providers: {@code global.GlobalRecipeProvider},
 * {@code global.MateriallyTexturedBlockRecipeProvider}, {@code extra.ExtraRecipeProvider},
 * {@code bricks.BrickRecipeProvider} and {@code floatingcarpet.FloatingCarpetRecipeProvider}. Only recipe (and,
 * transitively, recipe-unlock advancement) generation is covered here; blockstates/models/tags/lang/loot tables are
 * intentionally not part of this pass.
 */
public class DomumRecipeProvider extends RecipeProvider
{
    public DomumRecipeProvider(final HolderLookup.Provider registries, final RecipeOutput output)
    {
        super(registries, output);
    }

    @Override
    protected void buildRecipes()
    {
        buildArchitectsCutterRecipe();
        buildBarrelRecipes();
        buildExtraBlockRecipes();
        buildBrickRecipes();
        buildFloatingCarpetRecipes();
        buildMateriallyTexturedBlockRecipes();
    }

    /**
     * Formerly {@code GlobalRecipeProvider#buildCutterRecipe}.
     */
    private void buildArchitectsCutterRecipe()
    {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.TOOLS, ModBlocks.getInstance().getArchitectsCutter().asItem(), 1)
            .define('X', Items.IRON_INGOT)
            .define('S', Items.STONE_SLAB)
            .define('L', ItemTags.LOGS)
            .pattern(" X ")
            .pattern("SSS")
            .pattern("LLL")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .unlockedBy("has_stone_slab", has(Items.STONE_SLAB))
            .unlockedBy("has_log", has(ItemTags.LOGS))
            .save(output);
    }

    /**
     * Formerly {@code GlobalRecipeProvider#buildBarrelRecipe}.
     */
    private void buildBarrelRecipes()
    {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.BUILDING_BLOCKS, ModBlocks.getInstance().getStandingBarrel())
            .define('S', Items.STICK)
            .define('W', ItemTags.PLANKS)
            .pattern("SWS")
            .pattern("SWS")
            .pattern("SWS")
            .unlockedBy("has_stick", has(Items.STICK))
            .unlockedBy("has_planks", has(ItemTags.PLANKS))
            .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.BUILDING_BLOCKS, ModBlocks.getInstance().getLayingBarrel())
            .define('S', Items.STICK)
            .define('W', ItemTags.PLANKS)
            .pattern("SSS")
            .pattern("WWW")
            .pattern("SSS")
            .unlockedBy("has_stick", has(Items.STICK))
            .unlockedBy("has_planks", has(ItemTags.PLANKS))
            .save(output);
    }

    /**
     * Formerly {@code ExtraRecipeProvider}.
     */
    private void buildExtraBlockRecipes()
    {
        ModBlocks.getInstance().getExtraTopBlocks().forEach(extraBlock -> {
            final ExtraBlockType type = extraBlock.getType();
            final ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(items, RecipeCategory.TOOLS, extraBlock, 4);
            builder.pattern("X X");
            builder.pattern(" Z ");
            builder.pattern("X X");
            builder.define('X', type.getMaterial());
            if (type.getColor() == null)
            {
                builder.define('Z', type.getMaterial());
            }
            else
            {
                builder.define('Z', dyeItem(type.getColor()));
            }
            builder.unlockedBy("has_material", has(type.getMaterial()));
            if (type.getColor() != null)
            {
                builder.unlockedBy("has_dye", has(dyeItem(type.getColor())));
            }
            builder.save(output);
        });
    }

    /**
     * Formerly {@code BrickRecipeProvider}.
     */
    private void buildBrickRecipes()
    {
        ModBlocks.getInstance().getBricks().forEach(brickBlock -> {
            final ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(items, RecipeCategory.TOOLS, brickBlock, 4);
            builder.requires(brickBlock.getType().getIngredient(), 2);
            builder.requires(brickBlock.getType().getIngredient2(), 2);
            final String itemName = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(brickBlock.asItem().asItem())).toString().replace(":", "_");
            builder.unlockedBy("has_item1_" + itemName, has(brickBlock.getType().getIngredient()));
            builder.unlockedBy("has_item2_" + itemName, has(brickBlock.getType().getIngredient()));
            builder.save(output);
        });
    }

    /**
     * Formerly {@code FloatingCarpetRecipeProvider}.
     */
    private void buildFloatingCarpetRecipes()
    {
        final Map<DyeColor, Block> wools = new HashMap<>();
        wools.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
        wools.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        wools.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
        wools.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
        wools.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
        wools.put(DyeColor.RED, Blocks.RED_WOOL);
        wools.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
        wools.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
        wools.put(DyeColor.LIME, Blocks.LIME_WOOL);
        wools.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
        wools.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
        wools.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        wools.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
        wools.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
        wools.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
        wools.put(DyeColor.PINK, Blocks.PINK_WOOL);

        for (final FloatingCarpetBlock block : ModBlocks.getInstance().getFloatingCarpets())
        {
            final DyeColor color = block.getColor();
            ShapelessRecipeBuilder.shapeless(items, RecipeCategory.DECORATIONS, block, 3)
                .requires(wools.get(color), 2)
                .requires(Tags.Items.STRINGS)
                .group("floating_carpets")
                .unlockedBy("has_string", has(Tags.Items.STRINGS))
                .unlockedBy("has_wool", has(wools.get(color)))
                .save(output);
        }
    }

    /**
     * {@code DyeItem} lost its {@code byColor(DyeColor)} lookup helper; the individual dye items are still
     * registered as separate {@link Items} constants, so map directly to those.
     */
    private static Item dyeItem(final DyeColor color)
    {
        return switch (color)
        {
            case WHITE -> Items.WHITE_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case RED -> Items.RED_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }

    /**
     * Formerly {@code MateriallyTexturedBlockRecipeProvider}.
     */
    private void buildMateriallyTexturedBlockRecipes()
    {
        BuiltInRegistries.BLOCK.forEach(block -> {
            if (Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getNamespace().equals(Constants.MOD_ID)
                && block instanceof IMateriallyTexturedBlock materiallyTexturedBlock)
            {
                materiallyTexturedBlock.buildRecipes(output);
            }
        });
    }

    public static class Runner extends RecipeProvider.Runner
    {
        public Runner(final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> registries)
        {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(final HolderLookup.Provider registries, final RecipeOutput output)
        {
            return new DomumRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName()
        {
            return "Domum Ornamentum Recipes";
        }
    }
}
