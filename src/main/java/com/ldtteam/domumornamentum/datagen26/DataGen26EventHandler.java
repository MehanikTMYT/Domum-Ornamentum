package com.ldtteam.domumornamentum.datagen26;

import com.ldtteam.domumornamentum.datagen26.recipe.DomumRecipeProvider;
import com.ldtteam.domumornamentum.util.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Minimal NeoForge 26.1 datagen entrypoint, living outside the excluded legacy
 * {@code com.ldtteam.domumornamentum.datagen} package. Currently only wires up recipe generation; see
 * {@link DomumRecipeProvider} for what is covered.
 */
@EventBusSubscriber(modid = Constants.MOD_ID)
public class DataGen26EventHandler
{
    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent.Client event)
    {
        event.createProvider(DomumRecipeProvider.Runner::new);
    }
}
