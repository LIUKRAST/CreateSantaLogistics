package net.liukrast.santa.world.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.math.VecHelper;
import net.liukrast.santa.DeployerGoggleInformation;
import net.liukrast.santa.SantaConfig;
import net.liukrast.santa.SantaConstants;
import net.liukrast.santa.SantaLang;
import net.liukrast.santa.registry.*;
import net.liukrast.santa.world.entity.ai.goal.*;
import net.liukrast.santa.world.inventory.RoboElfMenu;
import net.liukrast.santa.world.item.PresentItem;
import net.liukrast.santa.world.item.crafting.RoboElfTradingRecipe;
import net.liukrast.santa.world.level.block.ElfChargeStationBlock;
import net.liukrast.santa.world.level.block.entity.ElfChargeStationBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

import java.util.*;

@NonnullDefault
public class RoboElf extends PathfinderMob implements DeployerGoggleInformation, MenuProvider {
    private static final EntityDataAccessor<Byte> OXIDATION_ID = SynchedEntityData.defineId(RoboElf.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> CHARGE_ID = SynchedEntityData.defineId(RoboElf.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> STRESS_ID = SynchedEntityData.defineId(RoboElf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> QUEUE_ID = SynchedEntityData.defineId(RoboElf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CRAFTED_ID = SynchedEntityData.defineId(RoboElf.class, EntityDataSerializers.INT);
    public static final int MAX_QUEUE = 18;

    private static final List<QueueEntry> DATAFIXER_TRADES = List.of(
            new QueueEntry(Items.SNOW_BLOCK.getDefaultInstance(), 10, 10),
            new QueueEntry(SantaBlocks.CHRISTMAS_TREE.toStack(), 40, 40),
            new QueueEntry(SantaItems.CANDY_CANE.toStack(), 20, 10),
            new QueueEntry(new ItemStack(Items.COOKIE, 8), 10, 10),
            new QueueEntry(AllFluids.CHOCOLATE.getBucket().orElseThrow().getDefaultInstance(), 80, 60),
            new QueueEntry(new ItemStack(SantaBlocks.BUDDING_CRYOLITE), 100, 200)
    );

    @Nullable
    private ElfChargeStationBlockEntity chargeStation = null;
    private int unstressCooldown = 0;
    private final Queue<Pair<UUID, QueueEntry>> queue = new ArrayDeque<>();
    private final Queue<Pair<UUID, ItemStack>> crafted = new ArrayDeque<>();

    /* INIT */
    public RoboElf(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCharge(this.getMaxCharge());
    }

    @SuppressWarnings("deprecation")
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if(spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION) {
            //Any value but clean
            setOxidation(OxidationStage.values()[1+level.getRandom().nextInt(OxidationStage.values().length-1)]);
            setCharge(level.getRandom().nextFloat()*getMaxCharge());
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RoboElfCraftGoal(this));
        this.goalSelector.addGoal(1, new RoboElfCreatePackageGoal(this, 100));
        this.goalSelector.addGoal(1, new RoboElfFindStationGoal(this, 1.5));
        this.goalSelector.addGoal(1, new NearestNonCombatTargetGoal<>(this, Player.class, true, player -> !crafted.isEmpty() && player.getUUID().equals(Objects.requireNonNull(crafted.peek()).getFirst())));
        this.goalSelector.addGoal(1, new NearestNonCombatTargetGoal<>(this, PackageEntity.class, true, pack -> pack instanceof PackageEntity pe && !(pe.box.getItem() instanceof PresentItem)));
        this.goalSelector.addGoal(1, new RoboElfCollectPackageGoal(this, 1.25, false));
        this.goalSelector.addGoal(1, new RoboElfDeliverCraftGoal(this, 1.25, false));
        // Secondary tasks
        this.goalSelector.addGoal(2, new FloatGoal(this));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGE_ID, 1f);
        builder.define(STRESS_ID, 0);
        builder.define(OXIDATION_ID,(byte) 0);
        builder.define(QUEUE_ID, 0);
        builder.define(CRAFTED_ID, 0);
    }

    public static AttributeSupplier.Builder createRoboElfAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(SantaAttributes.MAX_CHARGE, 1024);
    }

    public static boolean checkSpawnRules(EntityType<? extends RoboElf> ignored, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource ignored1) {
        return MobSpawnType.ignoresLightRequirements(spawnType) || level.getRawBrightness(pos, 0) > 8;
    }

    /* GETTERS AND SETTERS */
    public OxidationStage getOxidation() {
        return OxidationStage.values()[entityData.get(OXIDATION_ID)];
    }
    public void setOxidation(OxidationStage value) {
        this.entityData.set(OXIDATION_ID, (byte)value.ordinal());
    }

    public float getCharge() {
        return this.entityData.get(CHARGE_ID);
    }

    public void setCharge(float charge) {
        this.entityData.set(CHARGE_ID, Mth.clamp(charge, 0, this.getMaxCharge()));
    }

    public final float getMaxCharge() {
        return (float) this.getAttributeValue(SantaAttributes.MAX_CHARGE);
    }

    public float extractCharge(float amount) {
        if(amount < 0) return insertCharge(-amount);
        float charge = this.getCharge();
        if(amount > charge) {
            setCharge(0);
            return amount - charge;
        }
        setCharge(charge - amount);
        return amount;
    }

    public float insertCharge(float amount) {
        if(amount < 0) return extractCharge(-amount);
        float charge = this.getCharge();
        if(charge + amount > getMaxCharge()) {
            setCharge(getMaxCharge());
            return charge + amount - getMaxCharge();
        }
        setCharge(charge + amount);
        return 0;
    }

    public int getStress() {
        return this.entityData.get(STRESS_ID);
    }

    public void setStress(int stress) {
        this.entityData.set(STRESS_ID, Mth.clamp(stress, 0, 100));
    }

    public void stress(int amount) {
        setStress(getStress()+amount);
    }

    public Queue<Pair<UUID, QueueEntry>> getQueue() {
        return queue;
    }

    public Queue<Pair<UUID, ItemStack>> getCrafted() {
        return crafted;
    }

    public void setCharging(@Nullable BlockPos pos) {
        if(pos == null) {
            if(chargeStation != null && !chargeStation.isRemoved())
                chargeStation.stopCharging();
            chargeStation = null;
            return;
        }
        if(!(level().getBlockEntity(pos) instanceof ElfChargeStationBlockEntity be)) return;
        chargeStation = be;
        be.startCharging();
    }

    public boolean isCharging() {
        return chargeStation != null && !chargeStation.isRemoved();
    }

    public int getQueueSize() {
        return entityData.get(QUEUE_ID);
    }

    public int getCraftedSize() {
        return entityData.get(CRAFTED_ID);
    }

    /* NBT */

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Charge", this.getCharge());
        compound.putInt("Stress", this.getStress());
        compound.putByte("Oxidation", (byte) this.getOxidation().ordinal());
        if(chargeStation != null) BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, chargeStation.getBlockPos())
                .result()
                .ifPresent(nbt -> compound.put("ChargeStation", nbt));
        ListTag queue = new ListTag();
        for (Pair<UUID, QueueEntry> pair : this.queue) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Owner", pair.getFirst());
            entry.put(
                    "Value",
                    QueueEntry.CODEC.encodeStart(registryAccess().createSerializationContext(NbtOps.INSTANCE), pair.getSecond()).getOrThrow()
            );
            queue.add(entry);
        }
        compound.put("TradesQueue", queue);

        ListTag crafted = new ListTag();
        for(Pair<UUID, ItemStack> pair : this.crafted) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Owner", pair.getFirst());
            entry.put("Item", pair.getSecond().save(this.registryAccess()));
            crafted.add(entry);
        }
        compound.put("CraftedItems", crafted);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if(compound.contains("Charge", Tag.TAG_ANY_NUMERIC))
            this.setCharge(compound.getFloat("Charge"));
        if(compound.contains("Stress", Tag.TAG_ANY_NUMERIC))
            this.setStress(compound.getInt("Stress"));
        if(compound.contains("Oxidation", Tag.TAG_BYTE))
            this.setOxidation(OxidationStage.values()[compound.getByte("Oxidation")]);
        if(compound.contains("ChargeStation")) {
            var pos = BlockPos.CODEC.parse(NbtOps.INSTANCE, compound.get("ChargeStation")).result().orElse(null);
            if(pos == null) chargeStation = null;
            else {
                var be = level().getBlockEntity(pos);
                if(be instanceof ElfChargeStationBlockEntity e) chargeStation = e;
                else chargeStation = null;
            }
        }
        queue.clear();
        for (Tag tag : compound.getList("TradesQueue", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) tag;

            UUID uuid = entry.getUUID("Owner");

            QueueEntry info;
            //DATA FIXING
            if(entry.contains("Id", Tag.TAG_INT)) {
                int index = entry.getInt("Id");
                if(index < 0 || index >= DATAFIXER_TRADES.size()) continue;
                info = DATAFIXER_TRADES.get(index);
            } else {
                Optional<QueueEntry> opt = QueueEntry.CODEC.parse(registryAccess().createSerializationContext(NbtOps.INSTANCE), entry.getCompound("Entry"))
                        .resultOrPartial((error) -> SantaConstants.LOGGER.error("Tried to load invalid item: '{}'", error));
                if(opt.isEmpty()) continue;
                info = opt.get();
            }
            queue.offer(Pair.of(uuid, info));
        }
        crafted.clear();
        var list = compound.getList("CraftedItems", Tag.TAG_COMPOUND);
        for(int i = 0; i < Math.min(list.size(), MAX_QUEUE); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID uuid = entry.getUUID("Owner");
            ItemStack itemStack = ItemStack.parseOptional(this.registryAccess(), entry.getCompound("Item"));
            crafted.offer(Pair.of(uuid, itemStack));
        }
        reloadQueueStats();
    }

    /* LOGIC */

    @Override
    public void onDamageTaken(DamageContainer damageContainer) {
        Entity source = damageContainer.getSource().getEntity();
        if(!(source instanceof Player player)) return;
        SantaAttachmentTypes.trust(player, -10);
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        if(PackageItem.isPackage(itemEntity.getItem())) {
            this.take(itemEntity, 1);
        }
        super.pickUpItem(itemEntity);
    }

    @Override
    public void tick() {
        if(!level().isClientSide) {
            if(getCharge() <= 0 && getRandom().nextInt(1000) < 1) {
                setOxidation(getOxidation().next());
            }
            /* UNSTRESS */
            if (unstressCooldown > 0) unstressCooldown -= (getCharge() == 0 || isCharging() ? 10 : 1);
            else {
                stress(-1);
                unstressCooldown = SantaConfig.ELF_UNSTRESS_COOLDOWN.getAsInt();
            }

            if(getStress() > SantaConfig.ELF_OVERSTRESS_PERCENTAGE.getAsInt() && getCharge() > 0) {
                extractCharge(0.1f);
                Vec3 m = VecHelper.offsetRandomly(new Vec3(0, .25f, 0), getRandom(), .125f);
                ((ServerLevel) level()).sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, getX(), getY(), getZ(), 1, m.x, m.y, m.z, .01f);
            }

            if (getCharge() == 0) {
                navigation.stop();
            }

            if (chargeStation != null && !isStationValid()) {
                setCharging(null);
            }
            if (this.getCharge() >= this.getMaxCharge()) setCharging(null);
            if (chargeStation != null) {
                chargeStation.update(this);
                Vec3 pos1 = chargeStation.getBlockPos().relative(chargeStation.getBlockState().getValue(ElfChargeStationBlock.HORIZONTAL_FACING), 2).getCenter();
                this.lookControl.setLookAt(pos1.x, pos1.y, pos1.z);
            }

            boolean fl = chargeStation == null && getCharge() > 0 && getOxidation().isActive();
            for (var flag : Goal.Flag.values()) {
                goalSelector.setControlFlag(flag, fl);
            }
        }
        super.tick();
    }

    public boolean isStationValid() {
        assert chargeStation != null;
        return !chargeStation.isRemoved() && this.distanceToSqr(chargeStation.getBlockPos().relative(chargeStation.getBlockState().getValue(ElfChargeStationBlock.HORIZONTAL_FACING)).getCenter()) < 0.5;
    }

    @Override
    public void travel(Vec3 travelVector) {
        super.travel(travelVector);
        double motion = travelVector.length() * ((getStress()+1)/10f);
        extractCharge((float) motion);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        SantaLang.translate("gui.robo_elf.info_header").forGoggles(tooltip);
        SantaLang.translate("gui.robo_elf.title")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        IRotate.StressImpact.getFormattedStressText(getStress()/100f)
                .forGoggles(tooltip);
        SantaLang.translate("gui.robo_elf.capacity")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        double remainingCharge = getCharge();
        LangBuilder su = CreateLang.translate("generic.unit.stress");
        LangBuilder stressTip = CreateLang.number(remainingCharge)
                .add(su)
                .style(IRotate.StressImpact.of(1-(remainingCharge/getMaxCharge())).getRelativeColor());
        stressTip.forGoggles(tooltip, 1);
        SantaLang.translate("gui.robo_elf.oxidation")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        var oxidation = getOxidation();
        SantaLang.translate("gui.robo_elf.oxidation." + oxidation.name().toLowerCase())
                .style(oxidation.format())
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        if(this.chargeStation != null) return false;
        return super.isPushable();
    }

    @Nullable
    public static BlockPos findTarget(LevelReader level, BlockPos pos) {
        if(!level.isEmptyBlock(pos)) return null;
        for(Direction direction : Direction.values()) {
            if(direction.getAxis().isVertical()) continue;
            BlockState state = level.getBlockState(pos.relative(direction));
            if(!state.is(SantaBlocks.ELF_CHARGE_STATION.get())) continue;
            if(state.getValue(ElfChargeStationBlock.OCCUPIED)) continue;
            if(!state.getValue(ElfChargeStationBlock.HORIZONTAL_FACING).getOpposite().equals(direction)) continue;
            return pos.relative(direction);
        }
        return null;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(!getOxidation().isActive() && stack.is(ItemTags.AXES)) {
            setOxidation(getOxidation().prev());
            level().playSound(player, blockPosition(), SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level().levelEvent(player, 3004, blockPosition(), 0);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            if(!level().isClientSide) {
                Vec3 m = VecHelper.offsetRandomly(new Vec3(0, 0.25f, 0), level().random, .125f);
                ((ServerLevel)level()).sendParticles(ParticleTypes.SCRAPE, getX(), getY(), getZ(), 10, m.x, m.y, m.z, 0.1);
            }
            return InteractionResult.SUCCESS;
        } else if(!getOxidation().isActive()) {
            return InteractionResult.CONSUME;
        } else if(player.isShiftKeyDown() && insertCharge(1)<=0) {
            player.causeFoodExhaustion(2);
            return InteractionResult.SUCCESS;
        } else if(stack.is(Items.COOKIE) && getCharge() == 0) {
            stress(-1);
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        } else if(stack.is(Items.ROTTEN_FLESH)) {
            stress(5);
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        player.openMenu(this, buf -> buf.writeInt(getId()));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void dropEquipment() {
        this.spawnAtLocation(getMainHandItem());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RoboElfMenu(containerId, playerInventory, this);
    }

    public void enqueueWork(int craftIndex, int amount, Player player) {
        if(queue.size()+crafted.size()+amount >= MAX_QUEUE) return;
        var list = level().getRecipeManager().getAllRecipesFor(SantaRecipeTypes.ROBO_ELF_TRADING.get());
        if(craftIndex < 0 || craftIndex >= list.size()) return;
        RoboElfTradingRecipe info = list.get(craftIndex).value();

        for(int i = 0; i < amount; i++) {
            if(!extractFromPlayer(info.getInputs(), player)) continue;
            queue.offer(Pair.of(player.getUUID(), QueueEntry.create(info)));
            SantaAttachmentTypes.trust(player, info.trustGain());
        }
        reloadQueueStats();


    }

    private boolean extractFromPlayer(SizedIngredient[] costs, Player player) {
        Inventory inventory = player.getInventory();

        // Verification
        for (SizedIngredient cost : costs) {
            if (cost == null) continue;
            int needed = cost.count();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (cost.ingredient().test(stack)) {
                    needed -= stack.getCount();
                }
                if (needed <= 0) break;
            }
            if (needed > 0) return false;
        }

        // Extraction
        for (SizedIngredient cost : costs) {
            if (cost == null) continue;
            int toRemove = cost.count();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (cost.ingredient().test(stack)) {
                    int taken = Math.min(toRemove, stack.getCount());
                    stack.shrink(taken);
                    toRemove -= taken;
                }
                if (toRemove <= 0) break;
            }
        }

        return true;
    }

    // Might want to sort this list by how much trust each user has
    public void setCrafted(UUID owner, ItemStack stack) {
        this.crafted.offer(Pair.of(owner, stack));
    }

    public void reloadQueueStats() {
        this.entityData.set(QUEUE_ID, queue.size());
        this.entityData.set(CRAFTED_ID, crafted.size());
    }

    public enum OxidationStage {
        CLEAN, EXPOSED, WEATHERED, OXIDIZED;

        public boolean isActive() {
            return this == CLEAN;
        }

        public OxidationStage next() {
            return switch (this) {
                case OXIDIZED, WEATHERED -> OXIDIZED;
                case EXPOSED -> WEATHERED;
                case CLEAN -> EXPOSED;
            };
        }

        public OxidationStage prev() {
            return switch (this) {
                case CLEAN, EXPOSED -> CLEAN;
                case WEATHERED -> EXPOSED;
                case OXIDIZED -> WEATHERED;
            };
        }

        public ChatFormatting format() {
            return switch (this) {
                case CLEAN -> ChatFormatting.GREEN;
                case EXPOSED -> ChatFormatting.YELLOW;
                case WEATHERED -> ChatFormatting.GOLD;
                case OXIDIZED -> ChatFormatting.RED;
            };
        }
    }

    public record QueueEntry(ItemStack result, int energy, int processTime) {
        public static final Codec<QueueEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("result").forGetter(QueueEntry::result),
                Codec.INT.fieldOf("energy").forGetter(QueueEntry::energy),
                Codec.INT.fieldOf("processTime").forGetter(QueueEntry::processTime)
        ).apply(instance, QueueEntry::new));

        public static QueueEntry create(RoboElfTradingRecipe info) {
            return new QueueEntry(info.result(), info.energy(), info.processTime());
        }
    }
}
