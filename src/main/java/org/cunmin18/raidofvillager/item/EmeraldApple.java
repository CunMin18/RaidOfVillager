package org.cunmin18.raidofvillager.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import org.cunmin18.raidofvillager.effect.ModEffects;

public class EmeraldApple extends Item {

    public EmeraldApple() {
        super(new Settings().food((new FoodComponent.Builder()).hunger(4).alwaysEdible().statusEffect(new StatusEffectInstance(ModEffects.CHANGE_GRAVITY, 3600, 0), 1.0F).build()).maxCount(64));
    }

}
