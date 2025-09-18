package org.cunmin18.raidofvillager.item;

import net.minecraft.item.ArmorItem;
import org.cunmin18.raidofvillager.item.materials.EmeraldArmorMaterial;

public class EmeraldHelmet extends ArmorItem {

    public EmeraldHelmet() {
        super(EmeraldArmorMaterial.instance, Type.HELMET,new Settings().maxCount(1));
    }

}
