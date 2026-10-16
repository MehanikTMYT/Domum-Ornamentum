package com.ldtteam.domumornamentum.emi;

import com.ldtteam.domumornamentum.IDomumOrnamentumApi;
import com.ldtteam.domumornamentum.recipe.ModRecipeTypes;
import com.ldtteam.domumornamentum.util.Constants;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.client.screens.ArchitectsCutterScreen;
import com.ldtteam.domumornamentum.network.messages.CreativeSetArchitectCutterSlotMessage;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;

@EmiEntrypoint
public final class DomumOrnamentumEmiPlugin implements EmiPlugin
{
    public static final EmiRecipeCategory ARCHITECTS_CUTTER =
        new EmiRecipeCategory(
            Constants.resLocDO("architects_cutter"),
            EmiStack.of(new ItemStack(
                IDomumOrnamentumApi.getInstance()
                    .getBlocks()
                    .getArchitectsCutter()
            ))
        );

    @Override
    public void register(final EmiRegistry registry)
    {
        final ItemStack workstation = new ItemStack(
            IDomumOrnamentumApi.getInstance()
                .getBlocks()
                .getArchitectsCutter()
        );

        registry.addCategory(ARCHITECTS_CUTTER);
        registry.addWorkstation(
            ARCHITECTS_CUTTER,
            EmiStack.of(workstation)
        );

        // Was JEI MaterialSubtypeInterpreter: variants of these blocks differ by their "type" block state property.
        final Comparison byType = Comparison.compareData(stack -> stack.getItemStack()
            .getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY)
            .properties()
            .getOrDefault(Constants.TYPE_BLOCK_PROPERTY, ""));
        final var blocks = IDomumOrnamentumApi.getInstance().getBlocks();
        for (final ItemLike typed : new ItemLike[] {blocks.getDoor(), blocks.getTrapdoor(), blocks.getFancyDoor(), blocks.getFancyTrapdoor(), blocks.getPost(), blocks.getPanel()})
        {
            registry.setDefaultComparison(typed.asItem(), byType);
        }

        // Was JEI ArchitectsCutterGuiHandler: creative players can drop items onto the cutter's material slots.
        registry.addDragDropHandler(ArchitectsCutterScreen.class, new EmiDragDropHandler.SlotBased<>(
            DomumOrnamentumEmiPlugin::isCreativeMaterialSlot,
            (gui, slot, ingredient) -> {
                final ItemStack stack = ingredient.getEmiStacks().getFirst().getItemStack();
                if (!stack.isEmpty() && slot.mayPlace(stack))
                {
                    new CreativeSetArchitectCutterSlotMessage(slot.index, stack).sendToServer();
                }
            }));

        registry.getRecipeMap()
            .byType(ModRecipeTypes.ARCHITECTS_CUTTER.get())
            .forEach(holder ->
                registry.addRecipe(
                    new ArchitectsCutterEmiRecipe(holder)
                )
            );
    }

    private static boolean isCreativeMaterialSlot(final ArchitectsCutterScreen gui, final Slot slot)
    {
        final var player = Minecraft.getInstance().player;
        if (player == null || !player.isCreative() || !slot.isActive())
        {
            return false;
        }
        final ItemStack variant = gui.getMenu().getCurrentVariant();
        return variant != null && variant.getItem() instanceof BlockItem item
            && item.getBlock() instanceof IMateriallyTexturedBlock block
            && slot.index < block.getComponents().size();
    }
}
