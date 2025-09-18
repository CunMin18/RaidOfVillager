package org.cunmin18.raidofvillager.mixin;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.raid.RaiderEntity;
import org.cunmin18.raidofvillager.entity.VindicatorVillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RaiderEntity.class)
public class RaidEntityMixin {
    @Inject(method = "initGoals",at = @At("HEAD"))
    protected void initGoals(CallbackInfo ci){
        var raiderEntity = (RaiderEntity)(Object)this;
        var targetSelector = ((MobEntityAccessor) raiderEntity).getTargetSelector();
        var goalSelector = ((MobEntityAccessor) raiderEntity).getGoalSelector();
        //袭击者新增攻击目标：卫道士村民
        if(!(raiderEntity instanceof VindicatorVillagerEntity))targetSelector.add(1, new ActiveTargetGoal<>(raiderEntity, VindicatorVillagerEntity.class, 5, true, true, entity -> true));
    }
}
