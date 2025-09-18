package org.cunmin18.raidofvillager.item;

import net.minecraft.item.ArmorItem;
import org.cunmin18.raidofvillager.item.materials.EmeraldArmorMaterial;

public class EmeraldChestplate extends ArmorItem {

    public EmeraldChestplate() {
        super(EmeraldArmorMaterial.instance, Type.CHESTPLATE,new Settings().maxCount(1));
    }

}
