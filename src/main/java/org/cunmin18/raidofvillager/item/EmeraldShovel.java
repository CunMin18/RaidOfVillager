package org.cunmin18.raidofvillager.item;

import net.minecraft.item.ShovelItem;
import org.cunmin18.raidofvillager.item.materials.EmeraldToolMaterial;

public class EmeraldShovel extends ShovelItem {

    public EmeraldShovel() {
        super(EmeraldToolMaterial.instance, 1,-3.2f,new Settings().maxCount(1));
    }
}
