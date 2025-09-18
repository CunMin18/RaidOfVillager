package org.cunmin18.raidofvillager.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.cunmin18.raidofvillager.Raidofvillager;


public class ModItems {
    public static final Item EMERALD_APPLE = new EmeraldApple();
    public static final Item EMERALD_SWORD = new EmeraldSword();
    public static final Item EMERALD_AXE = new EmeraldAxe();
    public static final Item EMERALD_PICKAXE = new EmeraldPickaxe();
    public static final Item EMERALD_SHOVEL = new EmeraldShovel();
    public static final Item EMERALD_HOE = new EmeraldHoe();
    public static final Item EMERALD_HELMET = new EmeraldHelmet();
    public static final Item EMERALD_CHESTPLATE = new EmeraldChestplate();
    public static final Item EMERALD_LEGGINGS = new EmeraldLeggings();
    public static final Item EMERALD_BOOTS = new EmeraldBoots();

    public static Item registerItem(String itemName, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Raidofvillager.modName, itemName), item);
    }
    public static void addModItemToGroups(){
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(EMERALD_SWORD);
            content.add(EMERALD_HELMET);
            content.add(EMERALD_CHESTPLATE);
            content.add(EMERALD_LEGGINGS);
            content.add(EMERALD_BOOTS);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(EMERALD_AXE);
            content.add(EMERALD_PICKAXE);
            content.add(EMERALD_SHOVEL);
            content.add(EMERALD_HOE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(EMERALD_APPLE);
        });
    }
    public static void registerItems(){
        registerItem("emerald_apple",ModItems.EMERALD_APPLE);
        registerItem("emerald_sword",ModItems.EMERALD_SWORD);
        registerItem("emerald_axe",ModItems.EMERALD_AXE);
        registerItem("emerald_pickaxe",ModItems.EMERALD_PICKAXE);
        registerItem("emerald_shovel",ModItems.EMERALD_SHOVEL);
        registerItem("emerald_hoe",ModItems.EMERALD_HOE);
        registerItem("emerald_helmet",ModItems.EMERALD_HELMET);
        registerItem("emerald_chestplate",ModItems.EMERALD_CHESTPLATE);
        registerItem("emerald_leggings",ModItems.EMERALD_LEGGINGS);
        registerItem("emerald_boots",ModItems.EMERALD_BOOTS);
        addModItemToGroups();
    }

}