package net.liukrast.santa.world.level.block.entity;

import net.liukrast.santa.registry.SantaBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SantaDoorBlockEntity extends BlockEntity {
    public boolean lastState = false;
    public long lastStateTime = -1;

    public SantaDoorBlockEntity(BlockPos pos, BlockState blockState) {
        super(SantaBlockEntityTypes.SANTA_DOOR.get(), pos, blockState);
    }
}
