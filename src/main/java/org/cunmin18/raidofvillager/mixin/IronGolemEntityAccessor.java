package org.cunmin18.raidofvillager.mixin;

import net.minecraft.entity.passive.IronGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IronGolemEntity.class)
public interface IronGolemEntityAccessor {
    @Accessor("attackTicksLeft")
    void setAttackTicksLeft(int value);
}
