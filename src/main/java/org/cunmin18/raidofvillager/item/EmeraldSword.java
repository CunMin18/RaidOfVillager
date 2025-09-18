package org.cunmin18.raidofvillager.item;
import net.minecraft.item.*;
import org.cunmin18.raidofvillager.item.materials.EmeraldToolMaterial;

public class EmeraldSword extends SwordItem {

    public EmeraldSword() {
        super(EmeraldToolMaterial.instance, 6,-2.4f,new Settings().maxCount(1));
    }
}
