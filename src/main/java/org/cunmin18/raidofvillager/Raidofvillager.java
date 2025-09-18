package org.cunmin18.raidofvillager;

import net.fabricmc.api.ModInitializer;
import org.cunmin18.raidofvillager.effect.ModEffects;
import org.cunmin18.raidofvillager.entity.ModEntities;
import org.cunmin18.raidofvillager.events.ModEvents;
import org.cunmin18.raidofvillager.item.ModItems;

public class Raidofvillager implements ModInitializer {
    public static String modName = "raid_of_villager";
    @Override
    public void onInitialize() {
        //注册事件
        ModEvents.registerEvents();
        //注册自定义物品
        ModItems.registerItems();
        //注册效果
        ModEffects.registerEffects();
        //注册自定义实体
        ModEntities.registerEntities();
    }
}
