package org.cunmin18.raidofvillager.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.cunmin18.raidofvillager.item.ModItems;

public class VindicatorVillagerEntity extends VindicatorEntity {
    public VindicatorVillagerEntity(EntityType<? extends VindicatorEntity> entityType, World world) {
        super(entityType, world);
        this.setHealth(2500);
        this.setMovementSpeed(0.3f);
    }
    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (this.getRaid() == null) {
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.EMERALD_AXE));
        }
    }
    @Override
    protected void initGoals() {
        this.targetSelector.clear(goal -> true);
        this.goalSelector.clear(goal -> true);
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new IllagerEntity.LongDoorInteractGoal(this));
        this.goalSelector.add(3, new RaiderEntity.PatrolApproachGoal(this, 10.0F));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0F, false));
        this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(10, new LookAtEntityGoal(this, ZombieEntity.class, 99));
        this.goalSelector.add(10, new LookAtEntityGoal(this, RaiderEntity.class, 99));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MobEntity.class, 5, false, false,
                entity -> entity instanceof Monster &&
                        !(entity instanceof CreeperEntity) &&
                        !(entity instanceof VillagerEntity) &&
                        !(entity instanceof IronGolemEntity) &&
                        !(entity instanceof VindicatorVillagerEntity)
        ));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, RaiderEntity.class, 5, true, true, entity -> !(entity instanceof VindicatorVillagerEntity)));
    }

    public static DefaultAttributeContainer.Builder createVindicatorAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, (double)0.36F).add(EntityAttributes.GENERIC_FOLLOW_RANGE, (double)99.0F).add(EntityAttributes.GENERIC_MAX_HEALTH, (double)24.0F).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, (double)6.0F);
    }
    @Override
    public boolean isTeammate(Entity other) {
        return false;
    }
    @Override
    public EntityGroup getGroup() {
        return EntityGroup.DEFAULT;
    }
    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }
}
