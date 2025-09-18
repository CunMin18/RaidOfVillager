package org.cunmin18.raidofvillager.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.cunmin18.raidofvillager.client.entity.VindicatorVillagerEntityRenderer;
import org.cunmin18.raidofvillager.entity.ModEntities;

public class RaidofvillagerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.VINDICATOR_VILLAGER_ENTITY_ENTITY_TYPE, VindicatorVillagerEntityRenderer::new);
    }
}
