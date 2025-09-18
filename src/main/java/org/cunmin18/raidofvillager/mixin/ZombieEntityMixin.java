package org.cunmin18.raidofvillager.mixin;

import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin {
    //僵尸无法在阳光下燃烧
    @Inject(method = "burnsInDaylight",at = @At("HEAD"),cancellable = true)
    protected void burnsInDaylight(CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(false);
        ci.cancel();
    }
}
