package net.liukrast.santa.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.foundation.ICapabilityProvider;
import net.createmod.catnip.math.BlockFace;
import net.liukrast.santa.SantaConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Maybe a deployer API feature
 * */
@Deprecated(forRemoval = true)
@Mixin(FlowSource.FluidHandler.class)
public abstract class FlowSource$FluidHandlerMixin extends FlowSource {
    @Shadow
    @Nullable ICapabilityProvider<IFluidHandler> fluidHandlerCache;

    public FlowSource$FluidHandlerMixin(BlockFace location) {
        super(location);
    }

    @Definition(id = "blockEntity", local = @Local(type = BlockEntity.class, name = "blockEntity"))
    @Expression("blockEntity != null")
    @ModifyExpressionValue(method = "manageSource", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean manageSource(boolean original, @Local(argsOnly = true, name = "arg1") Level level, @Local(argsOnly = true, name = "arg2") BlockEntity networkBE) {
        if(!original) {
            BlockPos pos = ((FlowSourceAccessor)this).getLocation().getConnectedPos();
            if(level instanceof ServerLevel serverLevel) {
                if(SantaConstants.fluidCapabilityExtension(level.getBlockState(pos).getBlock()))
                    fluidHandlerCache = ICapabilityProvider.of(r -> BlockCapabilityCache.create(
                        Capabilities.FluidHandler.BLOCK,
                        serverLevel,
                        pos,
                        ((FlowSourceAccessor)this).getLocation().getOppositeFace(),
                        () -> !networkBE.isRemoved(),
                        () -> fluidHandlerCache = FlowSourceAccessor.getEMPTY()
                ));
            }
        }
        return original;
    }
}
