package com.ldtteam.domumornamentum.entity.block;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.ModBlocks;
import com.ldtteam.domumornamentum.util.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 */
public final class ModBlockEntityTypes
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntity>> MATERIALLY_TEXTURED = BLOCK_ENTITIES.register(Constants.BlockEntityTypes.MATERIALLY_RETEXTURABLE.getPath(),
      () -> new BlockEntityType<>((BlockEntityType.BlockEntitySupplier<BlockEntity>) MateriallyTexturedBlockEntity::new,
        getMateriallyTexturedValidBlocks()
      )
    );

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntity>> DYNAMIC_TIMBERFRAME = BLOCK_ENTITIES.register(Constants.BlockEntityTypes.DYNAMIC_TIMBERFRAME.getPath(),
        () -> new BlockEntityType<>((BlockEntityType.BlockEntitySupplier<BlockEntity>) DynamicTimberFrameBlockEntity::new,
            ModBlocks.getInstance().getDynamicTimberFrame()
        )
    );

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlockEntityTypes()
    {
    }

    /**
     * Resolves the valid blocks for the materially textured block entity from the mod's own registered blocks,
     * rather than scanning the whole {@link BuiltInRegistries#BLOCK} registry, which is not guaranteed to be
     * fully populated in registration order relative to this registry.
     */
    private static Block[] getMateriallyTexturedValidBlocks()
    {
        return ModBlocks.BLOCKS.getEntries()
            .stream()
            .map(DeferredHolder::get)
            .filter(IMateriallyTexturedBlock.class::isInstance)
            .toArray(Block[]::new);
    }
}
