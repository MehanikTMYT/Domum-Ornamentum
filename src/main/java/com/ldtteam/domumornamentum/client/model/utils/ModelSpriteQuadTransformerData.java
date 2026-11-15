package com.ldtteam.domumornamentum.client.model.utils;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;

public record ModelSpriteQuadTransformerData(BakedQuad quad, BlockState state) {
}
