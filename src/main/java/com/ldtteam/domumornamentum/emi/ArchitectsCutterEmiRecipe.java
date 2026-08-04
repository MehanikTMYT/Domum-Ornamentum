package com.ldtteam.domumornamentum.emi;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipeInput;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ArchitectsCutterEmiRecipe implements EmiRecipe
{
    private static final int WIDTH = 134;
    private static final int SLOT_SIZE = 18;

    private final RecipeHolder<ArchitectsCutterRecipe> holder;
    private final ArchitectsCutterRecipe recipe;
    private final Block generatedBlock;

    private final List<IMateriallyTexturedBlockComponent> components;

    /**
     * Конкретный список блоков, допустимых для каждого компонента.
     */
    private final List<List<Block>> validMaterials;

    /**
     * Широкие ингредиенты сохраняются для индекса EMI.
     *
     * Благодаря этому U на любом допустимом материале находит
     * соответствующие формы резчика.
     */
    private final List<EmiIngredient> indexedInputs;

    /**
     * Представительный выход нужен для обычного индекса рецептов.
     * Видимый выход строится динамически в addWidgets().
     */
    private final EmiStack fallbackOutput;

    private final int generationSeed;

    public ArchitectsCutterEmiRecipe(
        final RecipeHolder<ArchitectsCutterRecipe> holder)
    {
        this.holder = holder;
        this.recipe = holder.value();
        this.generatedBlock = recipe.getBlock();
        this.generationSeed = holder.id().hashCode();

        if (!(generatedBlock instanceof
            final IMateriallyTexturedBlock texturedBlock))
        {
            this.components = List.of();
            this.validMaterials = List.of();
            this.indexedInputs = List.of();
            this.fallbackOutput =
                EmiStack.of(new ItemStack(generatedBlock));

            return;
        }

        this.components =
            List.copyOf(texturedBlock.getComponents());

        this.validMaterials =
            createValidMaterials(components);

        this.indexedInputs =
            createIndexedInputs(validMaterials);

        this.fallbackOutput = EmiStack.of(
            createOutput(createDefaultSelection())
        );
    }

    private static List<List<Block>> createValidMaterials(
        final List<IMateriallyTexturedBlockComponent> components)
    {
        final List<List<Block>> result =
            new ArrayList<>(components.size());

        for (final IMateriallyTexturedBlockComponent component :
            components)
        {
            List<Block> materials =
                BuiltInRegistries.BLOCK
                    .getOrThrow(component.getValidSkins())
                    .stream()
                    .map(Holder::value)
                    .filter(block ->
                        !new ItemStack(block).isEmpty()
                    )
                    .distinct()
                    .toList();

            if (materials.isEmpty())
            {
                materials = List.of(component.getDefault());
            }

            result.add(materials);
        }

        return List.copyOf(result);
    }

    private static List<EmiIngredient> createIndexedInputs(
        final List<List<Block>> validMaterials)
    {
        final List<EmiIngredient> result =
            new ArrayList<>(validMaterials.size());

        for (final List<Block> materials : validMaterials)
        {
            final List<EmiStack> stacks =
                materials.stream()
                    .map(ItemStack::new)
                    .map(EmiStack::of)
                    .filter(stack -> !stack.isEmpty())
                    .toList();

            if (!stacks.isEmpty())
            {
                result.add(EmiIngredient.of(stacks));
            }
        }

        return List.copyOf(result);
    }

    private List<Block> createDefaultSelection()
    {
        final List<Block> result =
            new ArrayList<>(components.size());

        for (final IMateriallyTexturedBlockComponent component :
            components)
        {
            result.add(component.getDefault());
        }

        return result;
    }

    /**
     * Создаёт одну согласованную комбинацию для всех generated slots.
     *
     * Если EMI был открыт клавишей U на материале, материал
     * подставляется в одну из совместимых позиций.
     *
     * Когда он подходит сразу к нескольким компонентам,
     * позиция циклически меняется вместе со всем результатом.
     */
    private List<Block> createDisplaySelection(
        final Random random)
    {
        final List<Block> result =
            createDefaultSelection();

        final Block lookupBlock =
            resolveLookupBlock();

        if (lookupBlock == null)
        {
            return result;
        }

        final List<Integer> matchingComponents =
            new ArrayList<>();

        for (int index = 0;
             index < validMaterials.size();
             index++)
        {
            if (validMaterials
                .get(index)
                .contains(lookupBlock))
            {
                matchingComponents.add(index);
            }
        }

        if (matchingComponents.isEmpty())
        {
            return result;
        }

        final int selectedRole =
            matchingComponents.get(
                Math.floorMod(
                    random.nextInt(),
                    matchingComponents.size()
                )
            );

        result.set(selectedRole, lookupBlock);

        return result;
    }

    private static @Nullable Block resolveLookupBlock()
    {
        final EmiIngredient lookup =
            EmiApi.getRecipeLookupStack();

        if (lookup == null || lookup.isEmpty())
        {
            return null;
        }

        for (final EmiStack candidate :
            lookup.getEmiStacks())
        {
            final ItemStack stack =
                candidate.getItemStack();

            if (stack.isEmpty())
            {
                continue;
            }

            final Block block =
                Block.byItem(stack.getItem());

            if (block != null && block != Blocks.AIR)
            {
                return block;
            }
        }

        return null;
    }

    private ItemStack createOutput(
        final List<Block> materials)
    {
        final SimpleContainer container =
            new SimpleContainer(
                Math.max(materials.size(), 1)
            );

        for (int index = 0;
             index < materials.size();
             index++)
        {
            container.setItem(
                index,
                new ItemStack(materials.get(index))
            );
        }

        ItemStack result = recipe.assemble(
            new ArchitectsCutterRecipeInput(container)
        );

        if (result.isEmpty())
        {
            result = new ItemStack(generatedBlock);
        }

        result.setCount(
            Math.max(
                result.getCount(),
                Math.max(
                    materials.size(),
                    recipe.getCount()
                )
            )
        );

        return result;
    }

    private EmiIngredient generatedInput(
        final Random random,
        final int componentIndex)
    {
        final List<Block> selection =
            createDisplaySelection(random);

        return EmiStack.of(
            new ItemStack(
                selection.get(componentIndex)
            )
        );
    }

    private EmiIngredient generatedOutput(
        final Random random)
    {
        return EmiStack.of(
            createOutput(
                createDisplaySelection(random)
            )
        );
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return DomumOrnamentumEmiPlugin.ARCHITECTS_CUTTER;
    }

    @Override
    public @Nullable Identifier getId()
    {
        return holder.id().identifier();
    }

    @Override
    public List<EmiIngredient> getInputs()
    {
        return indexedInputs;
    }

    @Override
    public List<EmiStack> getOutputs()
    {
        return List.of(fallbackOutput);
    }

    @Override
    public int getDisplayWidth()
    {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight()
    {
        return Math.max(
            44,
            components.size() * SLOT_SIZE + 8
        );
    }

    @Override
    public void addWidgets(final WidgetHolder widgets)
    {
        final int centerY =
            Math.max(
                4,
                (widgets.getHeight() - SLOT_SIZE) / 2
            );

        if (components.isEmpty())
        {
            widgets.addFillingArrow(
                72,
                centerY + 1,
                1000
            );

            widgets.addSlot(
                fallbackOutput,
                108,
                centerY
            ).recipeContext(this);

            return;
        }

        for (int index = 0;
             index < components.size();
             index++)
        {
            final int componentIndex = index;

            widgets.addGeneratedSlot(
                random ->
                    generatedInput(
                        random,
                        componentIndex
                    ),
                generationSeed,
                5,
                4 + index * SLOT_SIZE
            );
        }

        widgets.addFillingArrow(
            72,
            centerY + 1,
            1000
        );

        widgets.addGeneratedSlot(
            this::generatedOutput,
            generationSeed,
            108,
            centerY
        ).recipeContext(this);
    }

    @Override
    public RecipeHolder<?> getBackingRecipe()
    {
        return holder;
    }
}
