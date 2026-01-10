package net.liukrast.santa.world.level.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import joptsimple.internal.Strings;
import net.liukrast.santa.SantaConfig;
import net.liukrast.santa.SantaLang;
import net.liukrast.santa.registry.SantaBlockEntityTypes;
import net.liukrast.santa.registry.SantaBlocks;
import net.liukrast.santa.registry.SantaFluids;
import net.liukrast.santa.world.level.block.entity.behaviour.OverclockScrollValueBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.lwjgl.system.NonnullDefault;

import java.util.List;

@NonnullDefault
public class FrostburnEngineBlockEntity extends GeneratingKineticBlockEntity {
    private int temperature = 0;
    @SuppressWarnings("NotNullFieldNotInitialized")
    public ScrollValueBehaviour overclock;
    private boolean creative = false;

    private final IFluidHandler handler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 0;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!resource.is(SantaFluids.CRYOLITE.getType())) return 0;
            if (temperature <= 0) return 0;
            int accepted = Math.min(temperature, resource.getAmount());
            if (action.execute()) temperature -= accepted;
            return accepted;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    };

    public FrostburnEngineBlockEntity(BlockPos pos, BlockState state) {
        super(SantaBlockEntityTypes.FROSTBURN_ENGINE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        overclock = new OverclockScrollValueBehaviour(SantaLang.translateDirect("kinetics.frostburn_engine.overclock"), this, new CenteredSideValueBoxTransform((a,b) -> b.getAxis().isHorizontal()));
        overclock.between(0, 1024);
        overclock.withCallback(i -> updateGeneratedRotation());
        behaviours.add(overclock);
    }

    public IFluidHandler getHandler() {
        return handler;
    }

    public int getTemperature() {
        return this.temperature;
    }

    @Override
    public float getGeneratedSpeed() {
        if(!getBlockState().is(SantaBlocks.FROSTBURN_ENGINE.get()))
            return 0;
        return 16;
    }

    @Override
    public float calculateAddedStressCapacity() {
        return (float) (super.calculateAddedStressCapacity() * overclock.value * SantaConfig.FROSTBURN_MULTIPLIER.getAsDouble());
    }

    @Override
    public void initialize() {
        super.initialize();
        if(!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("Temperature", temperature);
        compound.putInt("Overclock", overclock.value);
        compound.putBoolean("Creative", creative);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if(clientPacket) temperature = (temperature+ compound.getInt("Temperature"))/2;
        else temperature = compound.getInt("Temperature");
        overclock.setValue(compound.getInt("Overclock"));
        creative = compound.getBoolean("Creative");
    }

    @Override
    public void tick() {
        super.tick();
        int cfgMax = SantaConfig.FROSTBURN_EXPLODE.getAsInt();
        if(!creative) temperature = (int)Mth.clamp(temperature + overclock.value* SantaConfig.FROSTBURN_TEMPERATURE_INCREASE.getAsDouble(), 0, cfgMax);
        assert level != null;
        if(temperature >= cfgMax && !level.isClientSide && !creative) explode();
        sendData();
    }

    public void explode() {
        Vec3 pos = getBlockPos().getCenter();
        assert level != null;
        this.level.explode(null, new DamageSource(level.damageSources().damageTypes.getHolderOrThrow(DamageTypes.EXPLOSION), pos),
                null, pos,
                40, true, Level.ExplosionInteraction.BLOCK
        );
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        int cfgMax = SantaConfig.FROSTBURN_EXPLODE.getAsInt();
        SantaLang.translate("tooltip.overclock").style(ChatFormatting.GRAY).forGoggles(tooltip);
        var color = overclock.value < 256 ? ChatFormatting.GREEN : overclock.value < 512 ? ChatFormatting.YELLOW : overclock.value < 768 ? ChatFormatting.GOLD : ChatFormatting.RED;
        SantaLang.number(overclock.value).add(Component.literal("°")).style(color).add(Component.literal(" ")).add(SantaLang.translateDirect("tooltip.temperature_per_tick").withStyle(ChatFormatting.DARK_GRAY)).forGoggles(tooltip, 1);
        SantaLang.translate("tooltip.temperature").style(ChatFormatting.GRAY).forGoggles(tooltip);
        int bars = 40;
        var builder = SantaLang.builder()
                .add(bars(temperature*bars/cfgMax, ChatFormatting.RED))
                .add(bars(bars - (temperature*bars/cfgMax), ChatFormatting.GREEN));
        if(isPlayerSneaking)
            builder = builder.add(Component.literal(" (" + temperature + ")").withStyle(ChatFormatting.GRAY));
        builder.forGoggles(tooltip, 1);
        return true;
    }

    private MutableComponent bars(int level, ChatFormatting format) {
        return Component.literal(Strings.repeat('|', level))
                .withStyle(format);
    }
}
