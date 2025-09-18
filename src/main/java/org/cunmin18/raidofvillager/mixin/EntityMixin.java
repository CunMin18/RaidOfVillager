package org.cunmin18.raidofvillager.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    //僵尸被杀后会转变为村民
    @Inject(method = "onKilledOther",at = @At("HEAD"),cancellable = true)
    protected void onKilledOther(ServerWorld world, LivingEntity other, CallbackInfoReturnable<Boolean> ci) {
        boolean bl = true;
        var thisEntity = (Entity)(Object)this;
        if (other instanceof ZombieEntity zombieEntity && thisEntity instanceof VillagerEntity villager) {
            VillagerEntity villagerEntity = (VillagerEntity)zombieEntity.convertTo(EntityType.VILLAGER, false);
            if (villagerEntity != null) {
                villagerEntity.initialize(world, world.getLocalDifficulty(villagerEntity.getBlockPos()), SpawnReason.CONVERSION, (EntityData)null, (NbtCompound)null);
                villagerEntity.setVillagerData(villagerEntity.getVillagerData());
                if (!villager.isSilent()) {
                    world.syncWorldEvent((PlayerEntity)null, 1026, villager.getBlockPos(), 0);
                }

                bl = false;
            }
        }

        ci.setReturnValue(bl);
        ci.cancel();
    }
}
