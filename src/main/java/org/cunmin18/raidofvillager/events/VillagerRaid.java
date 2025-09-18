//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.cunmin18.raidofvillager.events;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.BossBar.Color;
import net.minecraft.entity.boss.BossBar.Style;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack.TooltipSection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import org.cunmin18.raidofvillager.entity.ModEntities;
import org.cunmin18.raidofvillager.entity.VindicatorVillagerEntity;
import org.cunmin18.raidofvillager.item.ModItems;
import org.jetbrains.annotations.Nullable;
import net.minecraft.village.raid.Raid;

public class VillagerRaid extends Raid{
    private static final Text EVENT_TEXT = Text.of("村民袭击");
    private static final Text VICTORY_TITLE = Text.translatable("event.minecraft.raid.victory.full");
    private static final Text DEFEAT_TITLE = Text.translatable("event.minecraft.raid.defeat.full");
    private final Map<Integer, Entity> waveToCaptain = Maps.newHashMap();
    private final Map<Integer, Set<Entity>> waveToRaiders = Maps.newHashMap();
    private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private final ServerWorld world;
    private boolean started;
    private final int id;
    private float totalHealth;
    private int badOmenLevel;
    private boolean active;
    private int wavesSpawned;
    private final ServerBossBar bar;
    private int postRaidTicks;
    private int preRaidTicks;
    private final Random random;
    private final int waveCount;
    private Status status;
    private int finishCooldown;
    private Optional<BlockPos> preCalculatedRavagerSpawnLocation;

    public VillagerRaid(int id, ServerWorld world, BlockPos pos) {
        super(id,world,pos);
        this.bar = new ServerBossBar(EVENT_TEXT, Color.BLUE, Style.NOTCHED_10);
        this.random = Random.create();
        this.preCalculatedRavagerSpawnLocation = Optional.empty();
        this.id = id;
        this.world = world;
        this.active = true;
        this.preRaidTicks = 300;
        this.bar.setPercent(0.0F);
        this.center = pos;
        this.waveCount = this.getMaxWaves(world.getDifficulty());
        this.status = VillagerRaid.Status.ONGOING;
    }
    public VillagerRaid(ServerWorld world, NbtCompound nbt) {
        super(world,nbt);
        this.bar = new ServerBossBar(EVENT_TEXT, Color.BLUE, Style.NOTCHED_10);
        this.random = Random.create();
        this.preCalculatedRavagerSpawnLocation = Optional.empty();
        this.world = world;
        this.id = nbt.getInt("Id");
        this.started = nbt.getBoolean("Started");
        this.active = nbt.getBoolean("Active");
        this.ticksActive = nbt.getLong("TicksActive");
        this.badOmenLevel = nbt.getInt("BadOmenLevel");
        this.wavesSpawned = nbt.getInt("GroupsSpawned");
        this.preRaidTicks = nbt.getInt("PreRaidTicks");
        this.postRaidTicks = nbt.getInt("PostRaidTicks");
        this.totalHealth = nbt.getFloat("TotalHealth");
        this.center = new BlockPos(nbt.getInt("CX"), nbt.getInt("CY"), nbt.getInt("CZ"));
        this.waveCount = nbt.getInt("NumGroups");
        this.status = VillagerRaid.Status.fromName(nbt.getString("Status"));
        this.heroesOfTheVillage.clear();
        if (nbt.contains("HeroesOfTheVillage", 9)) {
            for(NbtElement nbtElement : nbt.getList("HeroesOfTheVillage", 11)) {
                this.heroesOfTheVillage.add(NbtHelper.toUuid(nbtElement));
            }
        }

    }

    public boolean isFinished() {
        return this.hasWon() || this.hasLost();
    }

    public boolean isPreRaid() {
        return this.hasSpawned() && this.getRaiderCount() == 0 && this.preRaidTicks > 0;
    }

    public boolean hasSpawned() {
        return this.wavesSpawned > 0;
    }

    public boolean hasStopped() {
        return this.status == VillagerRaid.Status.STOPPED;
    }

    public boolean hasWon() {
        return this.status == VillagerRaid.Status.VICTORY;
    }

    public boolean hasLost() {
        return this.status == VillagerRaid.Status.LOSS;
    }

    public float getTotalHealth() {
        return this.totalHealth;
    }

    @Override
    public Set<RaiderEntity> getAllRaiders() {
        Set<RaiderEntity> set = Sets.newHashSet();

        for(Set<Entity> set2 : this.waveToRaiders.values()) {
            for(Entity entity : set2) {
                if (entity instanceof RaiderEntity) {
                    set.add((RaiderEntity) entity);
                }
            }
        }

        return set;
    }

    public World getWorld() {
        return this.world;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return this.wavesSpawned;
    }

    private Predicate<ServerPlayerEntity> isInRaidDistance() {
        return (player) -> {
            BlockPos blockPos = player.getBlockPos();
            return player.isAlive() && blockPos.getSquaredDistance(this.center) <= 96.0 * 96.0;
        };
    }

    private void updateBarToPlayers() {
        Set<ServerPlayerEntity> set = Sets.newHashSet(this.bar.getPlayers());
        List<ServerPlayerEntity> list = this.world.getPlayers(this.isInRaidDistance());

        System.out.println("Updating raid bar for " + list.size() + " nearby players");

        for(ServerPlayerEntity serverPlayerEntity : list) {
            if (!set.contains(serverPlayerEntity)) {
                this.bar.addPlayer(serverPlayerEntity);
                System.out.println("Added player " + serverPlayerEntity.getName().getString() + " to raid bar");
            }
        }

        for(ServerPlayerEntity serverPlayerEntity : set) {
            if (!list.contains(serverPlayerEntity)) {
                this.bar.removePlayer(serverPlayerEntity);
                System.out.println("Removed player " + serverPlayerEntity.getName().getString() + " from raid bar");
            }
        }
    }
    @Override
    public void start(PlayerEntity player) {
        System.out.println("Starting villager raid at: " + this.center);
        this.started = true;
        this.badOmenLevel = 1;
        this.preRaidTicks = 300;

        this.bar.setVisible(true);
        this.bar.setPercent(0.0F);
        this.updateBarToPlayers();

        if (player instanceof ServerPlayerEntity) {
            this.bar.addPlayer((ServerPlayerEntity) player);
        }

        this.markDirty();
    }

    @Override
    public void invalidate() {
        this.active = false;
        this.bar.clearPlayers();
        this.status = VillagerRaid.Status.STOPPED;
    }

    @Override
    public void tick() {
        //这里不是很会写，所以我求助了KIMI同学
        //袭击的状态每tick更新一次
        System.out.println("raid ticking, status: " + this.status + ", wavesSpawned: " + this.wavesSpawned + "/" + this.waveCount + ", raiders: " + this.getRaiderCount());
        if (!this.hasStopped()) {
            System.out.println("raid not stopped");
            if (this.status == VillagerRaid.Status.ONGOING) {
                boolean bl = this.active;
                this.active = this.world.isChunkLoaded(this.center);
                if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
                    this.invalidate();
                    return;
                }

                if (bl != this.active) {
                    this.bar.setVisible(this.active);
                }

                if (!this.active) {
                    return;
                }


                ++this.ticksActive;
                if (this.ticksActive >= 48000L) {
                    this.invalidate();
                    return;
                }
                int i = this.getRaiderCount();
                if (i == 0 && this.shouldSpawnMoreGroups()) {
                    if (this.preRaidTicks <= 0) {
                        if (this.preRaidTicks == 0 && this.wavesSpawned > 0) {
                            this.preRaidTicks = 300;
                            this.bar.setName(EVENT_TEXT);
                            return;
                        }
                    } else {
                        boolean bl2 = this.preCalculatedRavagerSpawnLocation.isPresent();
                        boolean bl3 = !bl2 && this.preRaidTicks % 5 == 0;
                        if (bl2 && !this.world.shouldTickEntity((BlockPos)this.preCalculatedRavagerSpawnLocation.get())) {
                            bl3 = true;
                        }

                        if (bl3) {
                            int j = 0;
                            if (this.preRaidTicks < 100) {
                                j = 1;
                            } else if (this.preRaidTicks < 40) {
                                j = 2;
                            }

                            this.preCalculatedRavagerSpawnLocation = this.preCalculateRavagerSpawnLocation(j);
                        }

                        if (this.preRaidTicks == 300 || this.preRaidTicks % 20 == 0) {
                            this.updateBarToPlayers();
                        }

                        --this.preRaidTicks;
                        this.bar.setPercent(MathHelper.clamp((float)(300 - this.preRaidTicks) / 300.0F, 0.0F, 1.0F));
                    }
                }

                if (this.ticksActive % 20L == 0L) {
                    this.updateBarToPlayers();
                    this.removeObsoleteRaiders();
                    if (i > 0) {
                        if (i <= 2) {
                            this.bar.setName(EVENT_TEXT.copy().append(" - ").append(Text.translatable("event.minecraft.raid.raiders_remaining", new Object[]{i})));
                        } else {
                            this.bar.setName(EVENT_TEXT);
                        }
                    } else {
                        this.bar.setName(EVENT_TEXT);
                    }
                }

                boolean bl2 = false;
                int k = 0;

                while(this.canSpawnRaiders()) {
                    System.out.println("Attempting to spawn wave " + (this.wavesSpawned + 1) + ", attempt " + k);
                    BlockPos blockPos = this.preCalculatedRavagerSpawnLocation.isPresent() ? (BlockPos)this.preCalculatedRavagerSpawnLocation.get() : this.getRavagerSpawnLocation(k, 20);
                    if (blockPos != null) {
                        System.out.println("Spawning wave " + (this.wavesSpawned + 1) + " at " + blockPos);
                        this.started = true;
                        this.spawnNextWave(blockPos);
                        if (!bl2) {
                            this.playRaidHorn(blockPos);
                            bl2 = true;
                        }
                    } else {
                        System.out.println("No spawn location found for wave " + (this.wavesSpawned + 1) + ", attempt " + k);
                        ++k;
                    }

                    if (k > 3) {
                        System.out.println("Failed to find spawn location after 3 attempts, invalidating raid");
                        this.invalidate();
                        break;
                    }
                }

                if (this.hasStarted() && !this.shouldSpawnMoreGroups() && i == 0) {
                    if (this.postRaidTicks < 40) {
                        ++this.postRaidTicks;
                    } else {
                        this.status = VillagerRaid.Status.VICTORY;

                        for(UUID uUID : this.heroesOfTheVillage) {
                            Entity entity = this.world.getEntity(uUID);
                            if (entity instanceof LivingEntity) {
                                LivingEntity livingEntity = (LivingEntity)entity;
                                if (!entity.isSpectator()) {
                                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                                    if (livingEntity instanceof ServerPlayerEntity) {
                                        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)livingEntity;
                                        serverPlayerEntity.incrementStat(Stats.RAID_WIN);
                                        Criteria.HERO_OF_THE_VILLAGE.trigger(serverPlayerEntity);
                                    }
                                }
                            }
                        }
                    }
                }

                this.markDirty();
            } else if (this.isFinished()) {
                ++this.finishCooldown;
                if (this.finishCooldown >= 600) {
                    this.invalidate();
                    return;
                }

                if (this.finishCooldown % 20 == 0) {
                    this.updateBarToPlayers();
                    this.bar.setVisible(true);
                    if (this.hasWon()) {
                        this.bar.setPercent(0.0F);
                        this.bar.setName(VICTORY_TITLE);
                    } else {
                        this.bar.setName(DEFEAT_TITLE);
                    }
                }
            }

        }
    }

    private void moveRaidCenter() {
        Stream<ChunkSectionPos> stream = ChunkSectionPos.stream(ChunkSectionPos.from(this.center), 2);
        ServerWorld var10001 = this.world;
        Objects.requireNonNull(var10001);
        stream.filter(var10001::isNearOccupiedPointOfInterest).map(ChunkSectionPos::getCenterPos).min(Comparator.comparingDouble((pos) -> pos.getSquaredDistance(this.center))).ifPresent(this::setCenter);
    }

    private Optional<BlockPos> preCalculateRavagerSpawnLocation(int proximity) {
        for(int i = 0; i < 3; ++i) {
            BlockPos blockPos = this.getRavagerSpawnLocation(proximity, 1);
            if (blockPos != null) {
                return Optional.of(blockPos);
            }
        }

        return Optional.empty();
    }

    private boolean shouldSpawnMoreGroups() {
        if (this.hasExtraWave()) {
            return !this.hasSpawnedExtraWave();
        } else {
            return !this.hasSpawnedFinalWave();
        }
    }

    private boolean hasSpawnedFinalWave() {
        return this.getGroupsSpawned() == this.waveCount;
    }

    private boolean hasExtraWave() {
        return this.badOmenLevel > 1;
    }

    private boolean hasSpawnedExtraWave() {
        return this.getGroupsSpawned() > this.waveCount;
    }

    private boolean isSpawningExtraWave() {
        return this.hasSpawnedFinalWave() && this.getRaiderCount() == 0 && this.hasExtraWave();
    }

    private void removeObsoleteRaiders() {
        Iterator<Set<Entity>> iterator = this.waveToRaiders.values().iterator();
        Set<Entity> set = Sets.newHashSet();

        while(iterator.hasNext()) {
            Set<Entity> set2 = (Set)iterator.next();

            for(Entity entity : set2) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)entity;
                    BlockPos blockPos = livingEntity.getBlockPos();
                    if (!livingEntity.isRemoved() && livingEntity.getWorld().getRegistryKey() == this.world.getRegistryKey() && !(this.center.getSquaredDistance(blockPos) >= (double)12544.0F)) {
                        if (livingEntity.age > 600) {
                            if (this.world.getEntity(livingEntity.getUuid()) == null) {
                                set.add(livingEntity);
                            }
                        }
                    } else {
                        set.add(livingEntity);
                    }
                } else {
                    set.add(entity);
                }
            }
        }

        for(Entity entity2 : set) {
            this.removeFromWave(entity2, true);
        }
    }

    private void playRaidHorn(BlockPos pos) {
        float f = 13.0F;
        int i = 64;
        Collection<ServerPlayerEntity> collection = this.bar.getPlayers();
        long l = this.random.nextLong();

        for(ServerPlayerEntity serverPlayerEntity : this.world.getPlayers()) {
            Vec3d vec3d = serverPlayerEntity.getPos();
            Vec3d vec3d2 = Vec3d.ofCenter(pos);
            double d = Math.sqrt((vec3d2.x - vec3d.x) * (vec3d2.x - vec3d.x) + (vec3d2.z - vec3d.z) * (vec3d2.z - vec3d.z));
            double e = vec3d.x + (double)13.0F / d * (vec3d2.x - vec3d.x);
            double g = vec3d.z + (double)13.0F / d * (vec3d2.z - vec3d.z);
            if (d <= (double)64.0F || collection.contains(serverPlayerEntity)) {
                serverPlayerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.EVENT_RAID_HORN, SoundCategory.NEUTRAL, e, serverPlayerEntity.getY(), g, 64.0F, 1.0F, l));
            }
        }

    }

    private void spawnNextWave(BlockPos pos) {
        int i = this.wavesSpawned + 1;
        this.totalHealth = 0.0F;
        LocalDifficulty localDifficulty = this.world.getLocalDifficulty(pos);
        boolean bl2 = this.isSpawningExtraWave();

        for(Member member : VillagerRaid.Member.VALUES) {
            int j = this.getCount(member, i, bl2) + this.getBonusCount(member, this.random, i, localDifficulty, bl2);

            for(int l = 0; l < j; ++l) {
                Entity entity = member.type.create(this.world);
                if (entity == null) {
                    break;
                }

                System.out.println("Spawning " + member.type.getName().getString() + " at " + pos);
                entity.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);

                if (entity instanceof VindicatorVillagerEntity) {
                    VindicatorVillagerEntity vindicator = (VindicatorVillagerEntity) entity;
                    vindicator.setRaid(this);
                    vindicator.setWave(i);
                    vindicator.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.EMERALD_AXE));
                    vindicator.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.085F);
                }

                // 直接生成实体
                this.world.spawnEntity(entity);

                // 如果是生物实体，添加到袭击波次
                if (entity instanceof LivingEntity) {
                    this.addToWave(i, entity, true);
                }
            }
        }

        this.preCalculatedRavagerSpawnLocation = Optional.empty();
        ++this.wavesSpawned;
        this.updateBar();
        this.markDirty();
        System.out.println("Wave " + i + " spawned successfully! Total entities: " + this.getRaiderCount());
    }

    public void addRaider(int wave, Entity raider, @Nullable BlockPos pos, boolean existing) {
        boolean bl = this.addToWave(wave, raider);
        if (bl) {
            if (!existing && pos != null) {
                raider.setPosition((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)1.0F, (double)pos.getZ() + (double)0.5F);
                raider.setOnGround(true);
                this.world.spawnEntityAndPassengers(raider);
            }
        }

    }

    public void updateBar() {
        float currentHealth = this.getCurrentRaiderHealth();
        float progress = this.totalHealth > 0.0F ? MathHelper.clamp(currentHealth / this.totalHealth, 0.0F, 1.0F) : 1.0F;
        this.bar.setPercent(progress);
        System.out.println("Raid bar updated: " + currentHealth + "/" + this.totalHealth + " = " + progress);
    }

    public float getCurrentRaiderHealth() {
        float f = 0.0F;

        for(Set<Entity> set : this.waveToRaiders.values()) {
            for(Entity entity : set) {
                if (entity instanceof LivingEntity) {
                    f += ((LivingEntity)entity).getHealth();
                }
            }
        }

        return f;
    }

    private boolean canSpawnRaiders() {
        boolean canSpawn = this.preRaidTicks == 0 && (this.wavesSpawned < this.waveCount || this.isSpawningExtraWave()) && this.getRaiderCount() == 0;
        if (canSpawn) {
            System.out.println("Can spawn raiders: preRaidTicks=" + this.preRaidTicks + ", wavesSpawned=" + this.wavesSpawned + "/" + this.waveCount + ", raiders=" + this.getRaiderCount());
        }
        return canSpawn;
    }

    public int getRaiderCount() {
        return this.waveToRaiders.values().stream().mapToInt(Set::size).sum();
    }

    public void removeFromWave(Entity entity, boolean countHealth) {
        for (Map.Entry<Integer, Set<Entity>> entry : this.waveToRaiders.entrySet()) {
            Set<Entity> set = entry.getValue();
            if (set != null && set.remove(entity)) {
                if (countHealth && entity instanceof LivingEntity) {
                    this.totalHealth -= ((LivingEntity)entity).getHealth();
                }
                this.updateBar();
                this.markDirty();
                break;
            }
        }
    }

    private void markDirty() {
        this.world.getRaidManager().markDirty();
    }

    public static ItemStack getOminousBanner() {
        ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
        NbtCompound nbtCompound = new NbtCompound();
        NbtList nbtList = (new BannerPattern.Patterns()).add(BannerPatterns.RHOMBUS, DyeColor.CYAN).add(BannerPatterns.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).add(BannerPatterns.STRIPE_CENTER, DyeColor.GRAY).add(BannerPatterns.BORDER, DyeColor.LIGHT_GRAY).add(BannerPatterns.STRIPE_MIDDLE, DyeColor.BLACK).add(BannerPatterns.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).add(BannerPatterns.CIRCLE, DyeColor.LIGHT_GRAY).add(BannerPatterns.BORDER, DyeColor.BLACK).toNbt();
        nbtCompound.put("Patterns", nbtList);
        BlockItem.setBlockEntityNbt(itemStack, BlockEntityType.BANNER, nbtCompound);
        itemStack.addHideFlag(TooltipSection.ADDITIONAL);
        itemStack.setCustomName(Text.translatable("block.minecraft.ominous_banner").formatted(Formatting.GOLD));
        return itemStack;
    }

    @Nullable
    @Override
    public RaiderEntity getCaptain(int wave) {
        Entity entity = this.waveToCaptain.get(wave);
        return entity instanceof RaiderEntity ? (RaiderEntity) entity : null;
    }

    @Nullable
    private BlockPos getRavagerSpawnLocation(int proximity, int tries) {
        int radius = 10;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int j = 0; j < tries; ++j) {
            float f = this.world.random.nextFloat() * ((float)Math.PI * 2F);
            int distance = radius + this.world.random.nextInt(4); // 10-13格
            int k = this.center.getX() + MathHelper.floor(MathHelper.cos(f) * distance);
            int l = this.center.getZ() + MathHelper.floor(MathHelper.sin(f) * distance);
            int m = this.world.getTopY(Type.WORLD_SURFACE, k, l);
            mutable.set(k, m, l);
            if (this.world.isRegionLoaded(mutable.getX() - 5, mutable.getZ() - 5, mutable.getX() + 5, mutable.getZ() + 5) &&
                    this.world.shouldTickEntity(mutable) &&
                    (SpawnHelper.canSpawn(Location.ON_GROUND, this.world, mutable, EntityType.RAVAGER) ||
                            this.world.getBlockState(mutable.down()).isOf(Blocks.SNOW) && this.world.getBlockState(mutable).isAir())) {
                System.out.println("Found spawn location at: " + mutable + " (distance: " + Math.sqrt(this.center.getSquaredDistance(mutable)) + ")");
                return mutable;
            }
        }

        return null;
    }

    private boolean addToWave(int wave, Entity entity) {
        return this.addToWave(wave, entity, true);
    }

    public boolean addToWave(int wave, Entity entity, boolean countHealth) {
        this.waveToRaiders.computeIfAbsent(wave, (wavex) -> Sets.newHashSet());
        Set<Entity> set = (Set)this.waveToRaiders.get(wave);
        Entity raiderEntity = null;

        for(Entity raiderEntity2 : set) {
            if (raiderEntity2.getUuid().equals(entity.getUuid())) {
                raiderEntity = raiderEntity2;
                break;
            }
        }

        if (raiderEntity != null) {
            set.remove(raiderEntity);
            set.add(entity);
        }

        set.add(entity);
        if (countHealth && entity instanceof LivingEntity) {
            this.totalHealth += ((LivingEntity)entity).getMaxHealth();
        }

        this.updateBar();
        this.markDirty();
        return true;
    }

    public void setWaveCaptain(int wave, Entity entity) {
        this.waveToCaptain.put(wave, entity);
        if (entity instanceof LivingEntity) {
            ((LivingEntity)entity).equipStack(EquipmentSlot.HEAD, getOminousBanner());
        }
    }

    public void removeLeader(int wave) {
        this.waveToCaptain.remove(wave);
    }

    public BlockPos getCenter() {
        return this.center;
    }

    private void setCenter(BlockPos center) {
        this.center = center;
    }

    public int getRaidId() {
        return this.id;
    }

    private int getCount(Member member, int wave, boolean extra) {
        int index = Math.min(wave - 1, member.countInWave.length - 1);
        return extra ? member.countInWave[member.countInWave.length - 1] : member.countInWave[index];
    }

    private int getBonusCount(Member member, Random random, int wave, LocalDifficulty localDifficulty, boolean extra) {
        Difficulty difficulty = localDifficulty.getGlobalDifficulty();
        boolean bl = difficulty == Difficulty.EASY;
        boolean bl2 = difficulty == Difficulty.NORMAL;
        int i;
        switch (member) {
            case VILLAGER:
                if (bl) {
                    i = random.nextInt(2);
                } else if (bl2) {
                    i = 1;
                } else {
                    i = 2;
                }
                break;
            case IRON_GOLEM:
                i = !bl && extra ? 1 : 0;
                break;
            default:
                return 0;
        }

        return i > 0 ? random.nextInt(i + 1) : 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("Id", this.id);
        nbt.putBoolean("Started", this.started);
        nbt.putBoolean("Active", this.active);
        nbt.putLong("TicksActive", this.ticksActive);
        nbt.putInt("BadOmenLevel", this.badOmenLevel);
        nbt.putInt("GroupsSpawned", this.wavesSpawned);
        nbt.putInt("PreRaidTicks", this.preRaidTicks);
        nbt.putInt("PostRaidTicks", this.postRaidTicks);
        nbt.putFloat("TotalHealth", this.totalHealth);
        nbt.putInt("NumGroups", this.waveCount);
        nbt.putString("Status", this.status.getName());
        nbt.putInt("CX", this.center.getX());
        nbt.putInt("CY", this.center.getY());
        nbt.putInt("CZ", this.center.getZ());
        NbtList nbtList = new NbtList();

        for(UUID uUID : this.heroesOfTheVillage) {
            nbtList.add(NbtHelper.fromUuid(uUID));
        }

        nbt.put("HeroesOfTheVillage", nbtList);
        return nbt;
    }

    public int getMaxWaves(Difficulty difficulty) {
        switch (difficulty) {
            case EASY -> {
                return 3;
            }
            case NORMAL -> {
                return 5;
            }
            case HARD -> {
                return 7;
            }
            default -> {
                return 0;
            }
        }
    }

    public float getEnchantmentChance() {
        int i = this.getBadOmenLevel();
        if (i == 2) {
            return 0.1F;
        } else if (i == 3) {
            return 0.25F;
        } else if (i == 4) {
            return 0.5F;
        } else {
            return i == 5 ? 0.75F : 0.0F;
        }
    }

    public void addHero(Entity entity) {
        this.heroesOfTheVillage.add(entity.getUuid());
    }

    static enum Status {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final Status[] VALUES = values();

        static Status fromName(String name) {
            for(Status status : VALUES) {
                if (name.equalsIgnoreCase(status.name())) {
                    return status;
                }
            }

            return ONGOING;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    static enum Member {//ModEntities.VINDICATOR_VILLAGER_ENTITY_ENTITY_TYPE
        VILLAGER(ModEntities.VINDICATOR_VILLAGER_ENTITY_ENTITY_TYPE, new int[]{13, 14, 15, 16, 18}),
        IRON_GOLEM(EntityType.IRON_GOLEM, new int[]{0, 1, 1, 2, 3});

        static final Member[] VALUES = values();
        final EntityType<?> type;
        final int[] countInWave;

        private Member(EntityType<?> type, int[] countInWave) {
            this.type = type;
            this.countInWave = countInWave;
        }
    }
}