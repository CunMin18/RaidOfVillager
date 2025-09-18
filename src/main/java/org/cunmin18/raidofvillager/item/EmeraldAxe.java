package org.cunmin18.raidofvillager.item;

import net.minecraft.item.AxeItem;
import org.cunmin18.raidofvillager.item.materials.EmeraldToolMaterial;

public class EmeraldAxe extends AxeItem {

    public EmeraldAxe() {
        super(EmeraldToolMaterial.instance, 2.5f,-3.2f,new Settings().maxCount(1));
    }
}
