package com.ldtteam.domumornamentum.client.color;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MateriallyTexturedBlockBlockColor {

    private static final int TINT_MASK = 0xff;
    private static final int TINT_BITS = 8;

    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        final int blockStateId = tintIndex >> TINT_BITS;
        final BlockState containedState = Block.stateById(blockStateId);
        int tintValue = tintIndex & TINT_MASK;
        final BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(containedState, tintValue);
        if (source == null) return -1;
        if (level == null || pos == null || state.getBlock() instanceof IMateriallyTexturedBlock block && !block.usesWorldSpecificTinting()) {
            return source.color(containedState);
        }
        return source.colorInWorld(containedState, level, pos);
    }
}
