package org.cunmin18.raidofvillager.events;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.cunmin18.raidofvillager.Utils;
import org.cunmin18.raidofvillager.entity.VindicatorVillagerEntity;
import org.cunmin18.raidofvillager.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//十分低端的写法，利用Event强制遍历玩家一定范围内全部村民，让他们检测附近有无僵尸，如果有僵尸 直接给村民添加速度，并且持续伤害范围内的僵尸，这样看起来，村民就像在“打“僵尸
public class VillagerAttackZombieEventHandler implements ServerTickEvents.EndWorldTick{
    @Override
    public void onEndTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            var minWorldPos = player.getBlockPos().add(-100, -50, -100);
            var maxWorldPos = player.getBlockPos().add(100, 50, 100);
            Vec3d vec1 = new Vec3d(minWorldPos.getX(), minWorldPos.getY(), minWorldPos.getZ());
            Vec3d vec2 = new Vec3d(maxWorldPos.getX(), maxWorldPos.getY(), maxWorldPos.getZ());
            //遍历村民实体
            for (VillagerEntity villager : world.getEntitiesByClass(VillagerEntity.class, new Box(vec1, vec2), entity -> true)) {
                if (villager != null) {
                    var box = new Box(villager.getBlockPos()).expand(4);
                    //不用管这里的pig，我懒得改了owo
                    var zombieEntityList = world.getEntitiesByClass(ZombieEntity.class, box, pig -> true);
                    var raiderEntityList = world.getEntitiesByClass(RaiderEntity.class, box, pig -> true);
                    List<Entity> enemyList = new ArrayList<>();
                    //敌人列表新增袭击者和僵尸
                    enemyList.addAll(zombieEntityList);
                    enemyList.addAll(raiderEntityList);
                    //判断是否有敌人
                    var hasEnemy = !enemyList.isEmpty();
                    //有敌人，开始选中敌人，我这里写的选中是选中随机敌人，不是最近敌人
                    if (hasEnemy) {

                        //其实这里可以 var random = villager.getRandom()，因为我第一次写 Event ，我不知道实体里面自带Random。。。
                        Random random = new Random();

                        var target = enemyList.get(random.nextInt(enemyList.size()));

                        if (target == null) continue;

                        if(target instanceof VindicatorVillagerEntity) continue;
                        //发现僵尸，让村民看向僵尸，并且持续给村民速度，这样村民看起来就像在移动
                        villager.lookAt(target.getCommandSource().getEntityAnchor(), target.getPos());

                        var ev = villager.getVelocity();
                        var rotation = Math.toRadians(villager.getYaw());
                        var moveSpeed = villager.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                        var dx = -Math.sin(rotation) * moveSpeed;
                        var dz = Math.cos(rotation) * moveSpeed;
                        villager.setVelocity(dx, ev.y, dz);
                        var distance = Utils.getEntitiesDistance(target, villager);
                        //距离小于2，给僵尸目标造成伤害，并且给其一个击退
                        if (distance <= 2f) {
                            RegistryEntry<DamageType> damageTypeEntry = world.getRegistryManager()
                                    .get(RegistryKeys.DAMAGE_TYPE)
                                    .getEntry(DamageTypes.MOB_ATTACK)
                                    .orElseThrow(() -> new IllegalStateException("MOB_ATTACK damage type not found"));
                            DamageSource damageSource = new DamageSource(damageTypeEntry, villager);
                            villager.setStackInHand(Hand.MAIN_HAND, ModItems.EMERALD_AXE.getDefaultStack());
                            boolean hurt = target.damage(damageSource, 4);

                            if (hurt) {
                                double knockbackStrength = 0.5;
                                ((LivingEntity)target).takeKnockback(knockbackStrength, Math.sin(rotation), -Math.cos(rotation));
                                //播放僵尸受伤音效，使村民的"假攻击"更逼真
                                target.playSound(SoundEvents.ENTITY_ZOMBIE_HURT, 1.0f, 1.0f);
                            }
                        }
                    }
                }
            }
        }
    }

}
