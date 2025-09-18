package org.cunmin18.raidofvillager.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.cunmin18.raidofvillager.Raidofvillager;


public class ModEffects {
    public static final StatusEffect CHANGE_GRAVITY = new ChangeGravityEffect();
    public static StatusEffect registerEffect(String effectName, StatusEffect effect){
        return Registry.register(Registries.STATUS_EFFECT,new Identifier(Raidofvillager.modName,effectName),effect);
    }
    public static void registerEffects(){
        ModEffects.registerEffect("change_gravity",ModEffects.CHANGE_GRAVITY);
    }
}
