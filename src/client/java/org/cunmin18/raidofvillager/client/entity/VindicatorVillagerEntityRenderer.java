package org.cunmin18.raidofvillager.client.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.VindicatorEntityRenderer;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.util.Identifier;
import org.cunmin18.raidofvillager.Raidofvillager;

@Environment(EnvType.CLIENT)
public class VindicatorVillagerEntityRenderer extends VindicatorEntityRenderer {
    private static final Identifier TEXTURE = new Identifier(Raidofvillager.modName,"textures/entity/vindicator_villager/vindicator_villager.png");
    public VindicatorVillagerEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }
    public Identifier getTexture(VindicatorEntity vindicatorEntity) {
        return TEXTURE;
    }
}
