package org.cunmin18.raidofvillager.item;

import net.minecraft.item.ArmorItem;
import org.cunmin18.raidofvillager.item.materials.EmeraldArmorMaterial;

public class EmeraldBoots extends ArmorItem {

    public EmeraldBoots() {
        super(EmeraldArmorMaterial.instance, Type.BOOTS,new Settings().maxCount(1));
    }

}
