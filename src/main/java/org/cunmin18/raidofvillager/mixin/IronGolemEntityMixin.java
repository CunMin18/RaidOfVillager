package org.cunmin18.raidofvillager.mixin;

import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.cunmin18.raidofvillager.entity.VindicatorVillagerEntity;
import org.cunmin18.raidofvillager.goal.IronGolemPlaceBlocksGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
interface MobEntityAccessor {
    @Accessor("goalSelector")
    GoalSelector getGoalSelector();
    @Accessor("targetSelector")
    GoalSelector getTargetSelector();
}

@Mixin(IronGolemEntity.class)
public abstract class IronGolemEntityMixin implements Angerable {
    //加强了一波铁傀儡
    @Inject(method = "initGoals",at = @At("HEAD"),cancellable = true)
    protected void initGoals(CallbackInfo ci){
        var golem = (IronGolemEntity)(Object)this;
        var targetSelector = ((MobEntityAccessor) golem).getTargetSelector();
        var goalSelector = ((MobEntityAccessor) golem).getGoalSelector();
        targetSelector.add(2, new IronGolemPlaceBlocksGoal(golem));
        goalSelector.add(1, new MeleeAttackGoal(golem, (double)1.0F, true));
        goalSelector.add(2, new WanderNearTargetGoal(golem, 0.9, 999f));
        goalSelector.add(2, new WanderAroundPointOfInterestGoal(golem, 0.6, false));
        goalSelector.add(4, new IronGolemWanderAroundGoal(golem, 0.6));
        goalSelector.add(5, new IronGolemLookGoal(golem));
        goalSelector.add(7, new LookAtEntityGoal(golem, PlayerEntity.class, 999f));
        goalSelector.add(8, new LookAroundGoal(golem));
        targetSelector.add(1, new TrackIronGolemTargetGoal(golem));
        targetSelector.add(2, new RevengeGoal(golem, new Class[0]));
        targetSelector.add(3, new ActiveTargetGoal<>(golem, PlayerEntity.class, 10, true, false, (entity) -> entity != null && golem.shouldAngerAt(entity) && golem.squaredDistanceTo(entity) <= 999 * 999));
        targetSelector.add(2, new ActiveTargetGoal<>(golem, MobEntity.class, 5, false, false,
                (entity) -> {
                    if (entity instanceof VindicatorVillagerEntity) {
                        return false;
                    }
                    return entity instanceof Monster && !(entity instanceof CreeperEntity);
                }
        ));
        targetSelector.add(4, new UniversalAngerGoal(golem, false));
        ci.cancel();
    }
    //重写了铁傀儡的属性
    @Inject(method = "createIronGolemAttributes",at = @At("HEAD"),cancellable = true)
    private static void createIronGolemAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> ci) {
        var v =  MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, (double)100.0F).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, (double)0.35F).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, (double)1.0F).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, (double)15.0F).add(EntityAttributes.GENERIC_FOLLOW_RANGE,(double)2047f);
        ci.setReturnValue(v);
        ci.cancel();
    }
}
