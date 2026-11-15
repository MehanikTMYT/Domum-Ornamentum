package com.ldtteam.domumornamentum.client.model.item;

import com.ldtteam.domumornamentum.client.model.RetexturingHandler;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.client.model.retexturing.RetexturedModelPart;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Extracts material-aware item geometry from the stack's persistent material component. */
public final class MateriallyTexturedItemModel implements ItemModel {
    private final List<BlockStateModelPart> baseParts;
    private final List<BakedQuad> overlay;
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;

    private MateriallyTexturedItemModel(List<BlockStateModelPart> baseParts, List<BakedQuad> overlay,
                                       ModelRenderProperties properties, Matrix4fc transformation) {
        this.baseParts = baseParts;
        this.overlay = overlay;
        this.properties = properties;
        this.transformation = transformation;
    }

    public static Unbaked unbaked(Identifier model) {
        return new Unbaked(model, Optional.empty());
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext display, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        final MaterialTextureData data = MaterialTextureData.readFromItemStack(stack);
        state.appendModelIdentityElement(this);
        state.appendModelIdentityElement(data);
        final var layer = state.newLayer();
        properties.applyToLayer(layer, display);
        layer.setLocalTransform(transformation);
        final List<BakedQuad> quads = layer.prepareQuadList();
        // Snapshot the material geometry during extraction; never consult a live stack during submit.
        for (var part : RetexturingHandler.retexturedParts(baseParts, data, BlockPos.ZERO, BlockAndTintGetter.EMPTY)) {
            for (var direction : net.minecraft.core.Direction.values()) quads.addAll(part.getQuads(direction));
            quads.addAll(part.getQuads(null));
        }
        quads.addAll(overlay);
        final var extents = CuboidItemModelWrapper.computeExtents(quads);
        layer.setExtents(() -> extents);
        for (var target : RetexturingHandler.targetsFor(data)) {
            final var tint = target.originalTintIndex() < 0 ? null : Minecraft.getInstance().getBlockColors()
                    .getTintSource(target.targetBlockState(), target.originalTintIndex());
            final int color = tint == null ? -1 : tint.color(target.targetBlockState());
            layer.tintLayers().add(color);
            state.appendModelIdentityElement(color);
        }
        if (stack.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            state.setAnimated();
        }
        if (quads.stream().anyMatch(q -> (q.materialInfo().flags() & BakedQuad.FLAG_ANIMATED) != 0)) state.setAnimated();
    }

    public record Unbaked(Identifier model, Optional<Identifier> overlay) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
                Identifier.CODEC.optionalFieldOf("overlay").forGetter(Unbaked::overlay)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() { return CODEC; }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(model);
            overlay.ifPresent(resolver::markDependency);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            final var baker = context.blockModelBaker();
            final var resolved = baker.getModel(model);
            final var slots = resolved.getTopTextureSlots();
            final var geometry = resolved.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY);
            final var properties = ModelRenderProperties.fromResolvedModel(baker, resolved, slots);
            final List<BlockStateModelPart> parts = List.of(new RetexturedModelPart(geometry,
                    resolved.getTopAmbientOcclusion(), properties.particleMaterial(), geometry.materialFlags()));
            final List<BakedQuad> overlayQuads = new ArrayList<>();
            overlay.ifPresent(id -> {
                final var m = baker.getModel(id);
                overlayQuads.addAll(m.bakeTopGeometry(m.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll());
            });
            return new MateriallyTexturedItemModel(parts, List.copyOf(overlayQuads), properties, transformation);
        }
    }
}
