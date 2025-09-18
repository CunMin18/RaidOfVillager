package org.cunmin18.raidofvillager.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ModEvents {
    public static void registerEvents(){
        ServerTickEvents.END_WORLD_TICK.register(new CheckIronGolemInPillagerOutpostAndStartRaidEvent());
        ServerTickEvents.END_WORLD_TICK.register(new VillagerAttackZombieEventHandler());
    }
}
