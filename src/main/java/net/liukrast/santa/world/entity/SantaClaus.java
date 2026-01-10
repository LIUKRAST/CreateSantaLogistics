package net.liukrast.santa.world.entity;

import net.liukrast.santa.DeployerGoggleInformation;
import net.liukrast.santa.SantaConstants;
import net.liukrast.santa.SantaLang;
import net.liukrast.santa.registry.SantaAttachmentTypes;
import net.liukrast.santa.world.entity.ai.goal.SantaClausCollectFoodGoal;
import net.liukrast.santa.world.entity.ai.goal.SantaClausEatGoal;
import net.liukrast.santa.world.entity.ai.goal.SantaGoToBedGoal;
import net.liukrast.santa.world.entity.ai.goal.SantaGoToSleighGoal;
import net.liukrast.santa.world.level.entity.SantaState;
import net.liukrast.santa.world.level.levelgen.SantaBase;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@NonnullDefault
public class SantaClaus extends PathfinderMob implements DeployerGoggleInformation {
    public float animationTime;
    public State lastFoundState = State.IDLE;

    private static final EntityDataAccessor<Integer> STATE_ID = SynchedEntityData.defineId(SantaClaus.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Optional<UUID>> LAST_OWNER_ID = SynchedEntityData.defineId(SantaClaus.class, EntityDataSerializers.OPTIONAL_UUID);

    public SantaClaus(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createSantaAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 2000)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SantaGoToBedGoal(this, 1));
        this.goalSelector.addGoal(0, new SantaGoToSleighGoal(this, 1));
        this.goalSelector.addGoal(1, new SantaClausEatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1) {
            int cooldown = 0;

            @Override
            protected boolean shouldPanic() {
                return getAnimationState() == State.ANGRY;
            }

            @Override
            public void start() {
                super.start();
                cooldown = 0;
            }

            @Override
            public void stop() {
                super.stop();
                setAnimationState(State.IDLE);
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() || cooldown < 60;
            }

            @Override
            public void tick() {
                super.tick();
                cooldown++;
            }
        });
        this.goalSelector.addGoal(2, new SantaClausCollectFoodGoal(this));

        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE_ID, 0);
        builder.define(LAST_OWNER_ID, Optional.empty());
    }

    /* GETTERS AND SETTERS */

    public State getAnimationState() {
        return State.values()[this.entityData.get(STATE_ID)];
    }

    public void setAnimationState(State value) {
        this.entityData.set(STATE_ID, value.ordinal());
    }

    @Nullable
    public UUID getLastItemOwner() {
        return entityData.get(LAST_OWNER_ID).orElse(null);
    }

    public void setLastItemOwner(@Nullable UUID uuid) {
        entityData.set(LAST_OWNER_ID, Optional.ofNullable(uuid));
    }

    /* NBT */
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        UUID lastItemOwner = getLastItemOwner();
        if(lastItemOwner != null) {
            compound.putUUID("LastItemOwner", lastItemOwner);
        }
    }


    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if(compound.contains("LastItemOwner")) {
            setLastItemOwner(compound.getUUID("LastItemOwner"));
        } else setLastItemOwner(null);
    }

    /* LOGIC */

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public boolean shouldGoToSleigh() {
        long day = level().getDayTime() % 24000;
        return day >= SantaConstants.NIGHT_START - SantaConstants.LEAVE_DURATION - 400 && !shouldBeGone();
    }

    public boolean shouldGoToSleep() {
        long day = level().getDayTime() % 24000;
        return day > SantaConstants.NIGHT_END + SantaConstants.LEAVE_DURATION && day < SantaConstants.NIGHT_END + SantaConstants.LEAVE_DURATION + 400 && !shouldBeSleeping();
    }

    public boolean shouldBeSleeping() {
        long day = level().getDayTime() % 24000;
        return day >= SantaConstants.NIGHT_END + SantaConstants.LEAVE_DURATION + 400 || day < SantaConstants.NIGHT_START - SantaConstants.LEAVE_DURATION - 6000;
    }

    public boolean shouldBeGone() {
        long day = level().getDayTime() % 24000;
        return day < SantaConstants.NIGHT_END + SantaConstants.LEAVE_DURATION && day >= SantaConstants.NIGHT_START - SantaConstants.LEAVE_DURATION;
    }

    @Override
    public void tick() {

        if(!isNoAi() && !level().isClientSide) {
            boolean shouldSleep = shouldBeSleeping();
            boolean shouldBeGone = shouldBeGone();

            var state = (getAnimationState() == State.SLEEPING);
            if (shouldBeGone) {
                //TODO: Spawn particles
                ((ServerLevel)level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.SNOW_BLOCK.getDefaultInstance()), getX(), getY(), getZ(), 40, 1, 0, 1, 0.4);
                SantaState.setState((ServerLevel) level(), false);
                this.discard();
            } else if (state != shouldSleep) {
                setAnimationState(shouldSleep ? State.SLEEPING : State.IDLE);
                if(shouldSleep) {
                    BlockPos couch = SantaBase.getCouch((ServerLevel) level());
                    if(couch != null) {
                        ((ServerLevel)level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.SNOW_BLOCK.getDefaultInstance()), getX(), getY(), getZ(), 40, 1, 0, 1, 0.4);
                        teleportTo((ServerLevel) level(), couch.getX(), couch.getY() + 0.5, couch.getZ()-0.5, Set.of(), -90, 0);
                    }
                }
            }
            if(state) {
                this.stopInPlace();
                BlockPos pos = getOnPos();
                this.getLookControl().setLookAt(pos.getX(), pos.getY()+2.5, pos.getZ()-1);
            }
            for (var flag : Goal.Flag.values()) {
                goalSelector.setControlFlag(flag, !shouldSleep);
            }
        }
        super.tick();
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if(!(source.getEntity() instanceof Player player)) return true;
        SantaAttachmentTypes.trust(player, -40);
        if(level().isClientSide) return true;
        ((ServerLevel)level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.SNOW_BLOCK.getDefaultInstance()), getX(), getY(), getZ(), 40, 1, 0, 1, 0.4);
        //TODO: Push
        return true;
    }

    @Override
    public boolean isPushable() {
        if(getAnimationState() == State.SLEEPING) return false;
        return super.isPushable();
    }

    @Override
    public void onDamageTaken(DamageContainer damageContainer) {

    }


    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        SantaLang.translate("gui.santa_claus.info_header").forGoggles(tooltip);
        SantaLang.translate("gui.santa_claus.satisfaction")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        assert Minecraft.getInstance().player != null;
        var player = Minecraft.getInstance().player;
        var extraData = player.getData(SantaAttachmentTypes.SANTA_TRADE_PROGRESS);
        if(extraData.isPresent()) {
            var ed = extraData.get();
            SantaLang.translate("gui.santa_claus.progress", ed.progress(), ed.recipe().input().count())
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip, 1);
            SantaLang.translate("gui.santa_claus.result", ed.recipe().result().getHoverName())
                    .style(ChatFormatting.LIGHT_PURPLE)
                    .forGoggles(tooltip, 1);
        }

        return true;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if(getMainHandItem().isEmpty()) return super.mobInteract(player, hand);
        var last = getLastItemOwner();
        if(last == null) return super.mobInteract(player, hand);
        player.getInventory().add(getMainHandItem());
        if(getMainHandItem().isEmpty()) setLastItemOwner(null);
        return InteractionResult.SUCCESS;
    }

    public enum State {
        IDLE,
        CURIOUS,
        ANGRY,
        LOOTING,
        EATING,
        SLEEPING
    }

}
