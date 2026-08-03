package com.ldtteam.domumornamentum.client.model.baked;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.client.model.properties.ModProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Minecraft 26.1 block-side replacement for the former
 * MateriallyTexturedBakedModel.
 *
 * ModelData is obtained from the level during chunk meshing. Each source
 * BlockStateModelPart is rebuilt with sprites and render layers taken from
 * the selected material blocks.
 */
public final class MateriallyTexturedBlockStateModel
    extends DelegateBlockStateModel
{
    private static final int WHITE = 0xFFFFFFFF;

    public MateriallyTexturedBlockStateModel(
        final BlockStateModel delegate)
    {
        super(delegate);
    }

    @Override
    public void collectParts(
        final BlockAndTintGetter level,
        final BlockPos pos,
        final BlockState state,
        final RandomSource random,
        final List<BlockStateModelPart> output)
    {
        final ModelData modelData = level.getModelData(pos);

        if (!modelData.has(ModProperties.MATERIAL_TEXTURE_PROPERTY))
        {
            delegate.collectParts(level, pos, state, random, output);
            return;
        }

        final MaterialTextureData textureData =
            modelData.get(ModProperties.MATERIAL_TEXTURE_PROPERTY);

        if (textureData == null || textureData.isEmpty())
        {
            delegate.collectParts(level, pos, state, random, output);
            return;
        }

        final List<BlockStateModelPart> sourceParts =
            new ArrayList<>();

        delegate.collectParts(
            level,
            pos,
            state,
            random,
            sourceParts
        );

        for (final BlockStateModelPart sourcePart : sourceParts)
        {
            output.add(remapPart(
                sourcePart,
                textureData,
                level,
                pos
            ));
        }
    }

    private static BlockStateModelPart remapPart(
        final BlockStateModelPart sourcePart,
        final MaterialTextureData textureData,
        final BlockAndTintGetter level,
        final BlockPos pos)
    {
        final QuadCollection.Builder builder =
            new QuadCollection.Builder();

        for (final BakedQuad quad : sourcePart.getQuads(null))
        {
            builder.addUnculledFace(remapQuad(
                quad,
                null,
                textureData,
                level,
                pos
            ));
        }

        for (final Direction side : Direction.values())
        {
            for (final BakedQuad quad : sourcePart.getQuads(side))
            {
                builder.addCulledFace(
                    side,
                    remapQuad(
                        quad,
                        side,
                        textureData,
                        level,
                        pos
                    )
                );
            }
        }

        return new SimpleModelWrapper(
            builder.build(),
            sourcePart.useAmbientOcclusion(),
            remapParticleMaterial(
                sourcePart.particleMaterial(),
                textureData
            )
        );
    }

    private static BakedQuad remapQuad(
        final BakedQuad source,
        final Direction cullSide,
        final MaterialTextureData textureData,
        final BlockAndTintGetter level,
        final BlockPos pos)
    {
        final Identifier sourceTexture =
            source.materialInfo()
                .sprite()
                .contents()
                .name();

        final Block replacementBlock =
            textureData.getTexturedComponents()
                .get(sourceTexture);

        if (replacementBlock == null)
        {
            return source;
        }

        final BlockState replacementState =
            replacementBlock.defaultBlockState();

        final BakedQuad replacement =
            findReplacementQuad(
                replacementState,
                cullSide,
                source.direction()
            );

        if (replacement == null)
        {
            return source;
        }

        final BakedQuad.MaterialInfo replacementInfo =
            replacement.materialInfo();

        final MutableQuad mutable =
            new MutableQuad().setFrom(source);

        mutable.setSpriteAndMoveUv(
            replacementInfo.sprite(),
            replacementInfo.layer(),
            replacementInfo.itemRenderType()
        );

        mutable.setShade(replacementInfo.shade());
        mutable.setLightEmission(
            replacementInfo.lightEmission()
        );
        mutable.setAmbientOcclusion(
            replacementInfo.ambientOcclusion()
        );

        /*
         * The old implementation encoded the target block state into a
         * synthetic tint index. Minecraft 26.1 uses lists of BlockTintSource
         * objects instead, so the color is resolved now and written directly
         * into the quad.
         */
        final int tintIndex = replacementInfo.tintIndex();

        if (tintIndex >= 0)
        {
            final var tintSource = Minecraft.getInstance()
                .getBlockColors()
                .getTintSource(replacementState, tintIndex);

            int color = tintSource == null
                ? WHITE
                : tintSource.colorInWorld(replacementState, level, pos);

            if (color == -1)
            {
                color = WHITE;
            }
            else if ((color & 0xFF000000) == 0)
            {
                color |= 0xFF000000;
            }

            mutable.setColor(color);
        }
        else
        {
            mutable.setColor(WHITE);
        }

        /*
         * Tint has already been resolved above. Leaving a tint index on the
         * output quad would make Minecraft apply a second tint operation.
         */
        mutable.setTintIndex(-1);

        return mutable.toBakedQuad();
    }

    private static BakedQuad findReplacementQuad(
        final BlockState replacementState,
        final Direction requestedSide,
        final Direction sourceNormal)
    {
        final BlockStateModel replacementModel =
            Minecraft.getInstance()
                .getModelManager()
                .getBlockStateModelSet()
                .get(replacementState);

        final List<BlockStateModelPart> replacementParts =
            new ArrayList<>();

        /*
         * The context-free method is deprecated but still available. It is
         * appropriate here because the material block uses its default state,
         * matching the behaviour of the old builder.
         */
        replacementModel.collectParts(
            RandomSource.create(42L),
            replacementParts
        );

        BakedQuad quad = firstQuad(
            replacementParts,
            requestedSide
        );

        if (quad == null && sourceNormal != requestedSide)
        {
            quad = firstQuad(
                replacementParts,
                sourceNormal
            );
        }

        if (quad == null)
        {
            quad = firstQuad(
                replacementParts,
                null
            );
        }

        if (quad == null)
        {
            for (final Direction direction : Direction.values())
            {
                quad = firstQuad(
                    replacementParts,
                    direction
                );

                if (quad != null)
                {
                    break;
                }
            }
        }

        return quad;
    }

    private static BakedQuad firstQuad(
        final List<BlockStateModelPart> parts,
        final Direction side)
    {
        for (final BlockStateModelPart part : parts)
        {
            final List<BakedQuad> quads =
                part.getQuads(side);

            if (!quads.isEmpty())
            {
                return quads.getFirst();
            }
        }

        return null;
    }

    private static Material.Baked remapParticleMaterial(
        final Material.Baked original,
        final MaterialTextureData textureData)
    {
        final Map<Identifier, Block> replacements =
            textureData.getTexturedComponents();

        final Block replacementBlock =
            replacements.get(
                original.sprite().contents().name()
            );

        if (replacementBlock == null)
        {
            return original;
        }

        final BlockState replacementState =
            replacementBlock.defaultBlockState();

        final BlockStateModel replacementModel =
            Minecraft.getInstance()
                .getModelManager()
                .getBlockStateModelSet()
                .get(replacementState);

        return replacementModel.particleMaterial();
    }
}
