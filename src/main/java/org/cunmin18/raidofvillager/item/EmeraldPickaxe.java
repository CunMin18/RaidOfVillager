package org.cunmin18.raidofvillager.item;

import net.minecraft.item.PickaxeItem;
import org.cunmin18.raidofvillager.item.materials.EmeraldToolMaterial;

public class EmeraldPickaxe extends PickaxeItem {

    public EmeraldPickaxe() {
        super(EmeraldToolMaterial.instance, 1,-3.2f,new Settings().maxCount(1));
    }
}
