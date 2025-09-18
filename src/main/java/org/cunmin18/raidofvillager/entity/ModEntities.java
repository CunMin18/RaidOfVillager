package org.cunmin18.raidofvillager.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.cunmin18.raidofvillager.Raidofvillager;

public class ModEntities {
    public static final EntityType<VindicatorVillagerEntity> VINDICATOR_VILLAGER_ENTITY_ENTITY_TYPE = FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, VindicatorVillagerEntity::new).dimensions(EntityDimensions.fixed(0.9F, 1.4F)).build();
    public static void registryEntity(String entityName, EntityType<?> entityType){
        Registry.register(Registries.ENTITY_TYPE, new Identifier(Raidofvillager.modName, entityName), entityType);
    }
    public static void registerEntities(){
        ModEntities.registryEntity("vindicator_villager",ModEntities.VINDICATOR_VILLAGER_ENTITY_ENTITY_TYPE);
        FabricDefaultAttributeRegistry.register(ModEntities.VINDICATOR_VILLAGER_ENTITY_ENTITY_TYPE, VindicatorVillagerEntity.createVindicatorAttributes());
    }
}
