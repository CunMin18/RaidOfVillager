package org.cunmin18.raidofvillager.item;

import net.minecraft.item.ArmorItem;
import org.cunmin18.raidofvillager.item.materials.EmeraldArmorMaterial;

public class EmeraldLeggings extends ArmorItem {

    public EmeraldLeggings() {
        super(EmeraldArmorMaterial.instance, Type.LEGGINGS,new Settings().maxCount(1));
    }

}
