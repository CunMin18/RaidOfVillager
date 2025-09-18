package org.cunmin18.raidofvillager.events;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.apache.commons.logging.Log;
import org.jetbrains.annotations.Debug;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

//这个事件用于检测是否生成袭击 检测方式比较奇特：用与/locate指令类似的方式获取铁傀儡到前哨站的距离，然后判断距离是否在一定范围内，如果在，则生成袭击，这个范围内需要有玩家。
public class CheckIronGolemInPillagerOutpostAndStartRaidEvent implements ServerTickEvents.EndWorldTick{
    public static boolean inVillagerRaid = false;
    private final Map<Integer, VillagerRaid> villagerRaids = Maps.newHashMap();
    private int nextAvailableId;
    @Override
    public void onEndTick(ServerWorld serverWorld) {
        // Tick existing raids
        Iterator<Map.Entry<Integer, VillagerRaid>> iterator = villagerRaids.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, VillagerRaid> entry = iterator.next();
            VillagerRaid raid = entry.getValue();

            if (!raid.isActive() || raid.isFinished()) {
                iterator.remove();
                System.out.println("Removed finished/inactive raid: " + entry.getKey() + ", status: " + raid.hasWon());
                continue;
            }

            raid.tick();
        }

        if (inVillagerRaid && villagerRaids.values().stream().noneMatch(VillagerRaid::isActive)) {
            inVillagerRaid = false;
            System.out.println("All villager raids finished");
        }
        if(inVillagerRaid) return;
        for(ServerPlayerEntity player : serverWorld.getPlayers()){
            if(player == null) continue;
            if(!enableChecking(serverWorld,player.getBlockPos())) {
                continue;
            }
            var minWorldPos = player.getBlockPos().add(-50,-30,-50);
            var maxWorldPos = player.getBlockPos().add(50,30,50);
            Vec3d vec1 = new Vec3d(minWorldPos.getX(), minWorldPos.getY(), minWorldPos.getZ());
            Vec3d vec2 = new Vec3d(maxWorldPos.getX(), maxWorldPos.getY(), maxWorldPos.getZ());
            for(IronGolemEntity golemEntity : serverWorld.getEntitiesByClass(IronGolemEntity.class,new Box(vec1,vec2), entity -> true)) {
                if(golemEntity == null) continue;
                if(inPillageOutpost(serverWorld,golemEntity.getBlockPos())){
                    startVillagerRaid(serverWorld,player);
                    System.out.println("spawned raid!");
                }
                else{
                    System.out.println("cant spawn raid");
                }
            }
        }
    }
    private int nextId() {
        return ++this.nextAvailableId;
    }
    //开始袭击
    private VillagerRaid startVillagerRaid(ServerWorld serverWorld,ServerPlayerEntity player){
        inVillagerRaid = true;
        VillagerRaid raid = this.getOrCreateVillagerRaid(player.getServerWorld(), player.getBlockPos());
        boolean bl = false;
        if (!raid.hasStarted()) {
            if (!this.villagerRaids.containsKey(raid.getRaidId())) {
                this.villagerRaids.put(raid.getRaidId(), raid);
            }

            bl = true;
        } else if (raid.getBadOmenLevel() < raid.getMaxAcceptableBadOmenLevel()) {
            bl = true;
        } else {
            player.removeStatusEffect(StatusEffects.BAD_OMEN);
            player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, (byte)43));
        }

        if (bl) {
            raid.start(player);
            System.out.println("raid started");
            player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, (byte)43));
            if (!raid.hasSpawned()) {
                player.incrementStat(Stats.RAID_TRIGGER);
                Criteria.VOLUNTARY_EXILE.trigger(player);
            }
        }
        return raid;
    }
    private VillagerRaid getOrCreateVillagerRaid(ServerWorld world, BlockPos pos) {
        return new VillagerRaid(this.nextId(), world, pos);
    }

    //检测某个方块位置是否在前哨站内
    private boolean inPillageOutpost(ServerWorld serverWorld,BlockPos blockPos){
        var pillageOutpostPos = findNearestPillagerOutpostBlockPos(serverWorld,blockPos);
        return getDistance(blockPos,pillageOutpostPos) <= 140;
    }

    //是否开启袭击检查
    private boolean enableChecking(ServerWorld serverWorld,BlockPos playerPos){
        var pillageOutpostPos = findNearestPillagerOutpostBlockPos(serverWorld,playerPos);
        System.out.println(getDistance(playerPos,pillageOutpostPos));
        return getDistance(playerPos,pillageOutpostPos) <= 150;
    }

    public static RegistryEntryList<Structure> getSingleStructureEntryList(ServerWorld world, String structureId) {
        Identifier id = new Identifier(structureId);
        Registry<Structure> structureRegistry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        Structure structure = structureRegistry.get(id);
        if (structure == null) {
            throw new IllegalArgumentException("结构ID不存在: " + structureId);
        }
        RegistryEntry<Structure> structureEntry = RegistryEntry.of(structure);
        return RegistryEntryList.of(structureEntry);
    }
    public static RegistryEntryList<Structure> getPillagerOutpostEntryList(ServerWorld world) {
        return getSingleStructureEntryList(world, "minecraft:pillager_outpost");
    }
    private static BlockPos findNearestPillagerOutpostBlockPos(ServerWorld world, BlockPos playerPos)  {
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        Pair<BlockPos, RegistryEntry<Structure>> pair = world.getChunkManager().getChunkGenerator().locateStructure(world, getPillagerOutpostEntryList(world), playerPos, 100, false);
        stopwatch.stop();
        if (pair == null) {
            return playerPos;
        }
        return pair.getFirst();
    }
    private static float getDistance(BlockPos pos1, BlockPos pos2) {
        var x1 = pos1.getX();
        var y1 = pos1.getY();
        var z1 = pos1.getZ();
        var x2 = pos2.getX();
        var y2 = pos2.getY();
        var z2 = pos2.getZ();
        int i = x2 - x1;
        int j = y2 - y1;
        int k = z2 - z1;
        return MathHelper.sqrt((float)(i * i + j * j + k * k));
    }
}
