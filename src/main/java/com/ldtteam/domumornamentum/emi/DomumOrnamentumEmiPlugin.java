package com.ldtteam.domumornamentum.emi;

import com.ldtteam.domumornamentum.IDomumOrnamentumApi;
import com.ldtteam.domumornamentum.recipe.ModRecipeTypes;
import com.ldtteam.domumornamentum.util.Constants;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
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

        registry.getRecipeMap()
            .byType(ModRecipeTypes.ARCHITECTS_CUTTER.get())
            .forEach(holder ->
                registry.addRecipe(
                    new ArchitectsCutterEmiRecipe(holder)
                )
            );
    }
}
