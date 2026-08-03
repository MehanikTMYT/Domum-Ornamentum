package com.ldtteam.domumornamentum.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ldtteam.domumornamentum.block.ModBlocks;
import com.ldtteam.domumornamentum.client.model.baked.MateriallyTexturedBlockStateModel;
import com.ldtteam.domumornamentum.util.Constants;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

@EventBusSubscriber(
    modid = Constants.MOD_ID,
    value = Dist.CLIENT
)
public final class MateriallyTexturedModelLoader
{
    private MateriallyTexturedModelLoader()
    {
    }

    @SubscribeEvent
    public static void onRegisterLoaders(
        final ModelEvent.RegisterLoaders event)
    {
        event.register(
            Constants.MATERIALLY_TEXTURED_MODEL_LOADER,
            new Loader()
        );
    }

    @SubscribeEvent
    public static void onModifyBakingResult(
        final ModelEvent.ModifyBakingResult event)
    {
        final var models =
            event.getBakingResult().blockStateModels();

        for (final Block block :
            ModBlocks.getMateriallyTexturableBlocks())
        {
            for (final BlockState state :
                block.getStateDefinition().getPossibleStates())
            {
                models.computeIfPresent(
                    state,
                    (key, model) ->
                        model instanceof
                            MateriallyTexturedBlockStateModel
                            ? model
                            : new MateriallyTexturedBlockStateModel(
                                model
                            )
                );
            }
        }
    }

    /**
     * Temporary item-side compatibility wrapper.
     *
     * It keeps the existing JSON loader valid while item material
     * replacement is migrated to the new ItemModel API.
     */
    private static final class MateriallyTexturedUnbakedModel
        extends DelegateUnbakedModel
    {
        private MateriallyTexturedUnbakedModel(
            final UnbakedModel delegate)
        {
            super(delegate);
        }
    }

    private static final class Loader
        implements UnbakedModelLoader<
            MateriallyTexturedUnbakedModel>
    {
        @Override
        public MateriallyTexturedUnbakedModel read(
            final JsonObject json,
            final JsonDeserializationContext context)
            throws JsonParseException
        {
            /*
             * Remove our loader before asking CuboidModel to parse the
             * remaining vanilla model data. Otherwise the same custom loader
             * could recursively invoke itself.
             */
            final JsonObject vanillaJson =
                json.deepCopy();

            vanillaJson.remove("loader");

            final CuboidModel delegate =
                context.deserialize(
                    vanillaJson,
                    CuboidModel.class
                );

            return new MateriallyTexturedUnbakedModel(
                delegate
            );
        }
    }
}
