package com.ldtteam.domumornamentum.client.color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MateriallyTexturedBlockItemColor
{
    private static final int TINT_MASK = 0xff;
    private static final int TINT_BITS = 8;

    public int getColor(
            @NotNull final ItemStack stack,
            final int tint )
    {
        final BlockState state = Block.stateById (tint >> TINT_BITS);
        if(state.getBlock() instanceof LiquidBlock) {
            final var source = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                    .get(state.getFluidState()).fluidTintSource();
            return source == null ? -1 : source.color(state.getFluidState());
        }

        final ItemStack workingStack = new ItemStack(state.getBlock(), 1);
        if (workingStack.getItem() instanceof AirItem)
            return 0xffffff;

        int tintValue = tint & TINT_MASK;
        final BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(state, tintValue);
        return source == null ? -1 : source.color(state);
    }
}
