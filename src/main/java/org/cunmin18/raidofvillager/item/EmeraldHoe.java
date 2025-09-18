package org.cunmin18.raidofvillager.item;

import net.minecraft.item.HoeItem;
import org.cunmin18.raidofvillager.item.materials.EmeraldToolMaterial;

public class EmeraldHoe extends HoeItem {

    public EmeraldHoe() {
        super(EmeraldToolMaterial.instance, 1,-3.2f,new Settings().maxCount(1));
    }
}
