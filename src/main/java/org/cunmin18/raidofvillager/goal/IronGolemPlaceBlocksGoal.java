package org.cunmin18.raidofvillager.goal;

import com.google.common.collect.Lists;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.cunmin18.raidofvillager.Utils;
import org.cunmin18.raidofvillager.mixin.IronGolemEntityAccessor;

import java.util.EnumSet;
import java.util.List;

public class IronGolemPlaceBlocksGoal extends Goal{
    private final IronGolemEntity golem;
    private final World world;
    private int cooldown = 0;
    public IronGolemPlaceBlocksGoal(IronGolemEntity golem) {
        this.golem = golem;
        this.world = golem.getWorld();
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }
    @Override
    public boolean canStart() {
        return golem.isOnGround();
    }
    @Override
    public void tick() {
        if(cooldown > 0) {
            cooldown--;
            System.out.println(cooldown);
            return;
        }

        if(golem.getAngryAt() == null) {
            System.out.println("null angryat");
            return;
        }

        var golemPos = golem.getBlockPos();

        if(golemPos == null) {
            System.out.println("null golempos");
            return;
        }

        var aimEntity = ((ServerWorld)world).getEntity(golem.getAngryAt());

        if(aimEntity == null) {
            System.out.println("null aimEntity");
            return;
        }

        var aimPos = aimEntity.getBlockPos();

        if(aimPos == null) {
            System.out.println("null aimPos");
            return;
        }
        var horizontalRange = Utils.getHorizontalDistance(aimEntity,golem);
        //可否搭高自己
        var placeSelf = aimPos.getY() > golemPos.getY() && !golem.isInAttackRange((LivingEntity) aimEntity) && horizontalRange <= 3;
        //不是玩家,直接开干
        if(!(aimEntity instanceof PlayerEntity) && placeSelf){
            world.setBlockState(golemPos, Blocks.DIRT.getDefaultState());
            world.playSound(null, golemPos, SoundEvents.BLOCK_ROOTED_DIRT_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            golem.setPos(golemPos.getX(),golemPos.getY() + 1,golemPos.getZ());
            ((IronGolemEntityAccessor) golem).setAttackTicksLeft(3);
            golem.getWorld().sendEntityStatus(golem, (byte)4);
            golem.setStackInHand(Hand.MAIN_HAND,new ItemStack(Items.DIRT));
            cooldown = 2;
        }
        //是玩家,计算战力
        else if(aimEntity instanceof PlayerEntity player){
            var fv = getPlayerFightingValue(player);
            //战力低,开干
            if(fv <= 45 && placeSelf){
                world.setBlockState(golemPos, Blocks.DIRT.getDefaultState());
                world.playSound(null, golemPos, SoundEvents.BLOCK_ROOTED_DIRT_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                golem.setPos(golemPos.getX(),golemPos.getY() + 1,golemPos.getZ());
                ((IronGolemEntityAccessor) golem).setAttackTicksLeft(3);
                golem.getWorld().sendEntityStatus(golem, (byte)4);
                golem.setStackInHand(Hand.MAIN_HAND,new ItemStack(Items.DIRT));
                cooldown = 2;
            }
            //战力高,巧妙取胜
            else if(fv > 45){
                world.setBlockState(aimPos, Blocks.DIRT.getDefaultState());
                world.playSound(null, aimPos, SoundEvents.BLOCK_ROOTED_DIRT_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                aimEntity.setPos(aimPos.getX(),aimPos.getY() + 1,aimPos.getZ());
                ((IronGolemEntityAccessor) golem).setAttackTicksLeft(3);
                golem.getWorld().sendEntityStatus(golem, (byte)4);
                golem.setStackInHand(Hand.MAIN_HAND,new ItemStack(Items.DIRT));
                cooldown = 2;
            }
        }

    }
    private float getPlayerFightingValue(PlayerEntity player){
        if(player == null) return 0;
        List<Item> swords = List.of(
                Items.WOODEN_SWORD,
                Items.STONE_SWORD,
                Items.IRON_SWORD,
                Items.DIAMOND_SWORD,
                Items.NETHERITE_SWORD
        );
        List<Item> helmets = List.of(
                Items.LEATHER_HELMET,
                Items.CHAINMAIL_HELMET,
                Items.IRON_HELMET,
                Items.DIAMOND_HELMET,
                Items.NETHERITE_HELMET

        );
        List<Item> chestplates = List.of(
                Items.LEATHER_CHESTPLATE,
                Items.CHAINMAIL_CHESTPLATE,
                Items.IRON_CHESTPLATE,
                Items.DIAMOND_CHESTPLATE,
                Items.NETHERITE_CHESTPLATE
        );
        List<Item> leggingses = List.of(
                Items.LEATHER_LEGGINGS,
                Items.CHAINMAIL_LEGGINGS,
                Items.IRON_LEGGINGS,
                Items.DIAMOND_LEGGINGS,
                Items.NETHERITE_LEGGINGS
        );
        List<Item> bootses = List.of(
                Items.LEATHER_BOOTS,
                Items.CHAINMAIL_BOOTS,
                Items.IRON_BOOTS,
                Items.DIAMOND_BOOTS,
                Items.NETHERITE_BOOTS
        );
        var value = 0f;
        var mainHandStack = player.getMainHandStack();
        var offHandStack = player.getOffHandStack();
        List<ItemStack> armorList = Lists.newArrayList(player.getArmorItems());
        var helmet = armorList.get(3);
        var chestplate = armorList.get(2);
        var leggings = armorList.get(1);
        var boots = armorList.get(0);

        var mainHandItem = mainHandStack.getItem();
        var mainHandValue = swords.contains(mainHandItem) ? swords.indexOf(mainHandItem) + 4 : 0;

        var offHandItem = offHandStack.getItem();
        var offHandValue = offHandItem == Items.SHIELD ? 10 : 0;

        var helmetItem = helmet.getItem();
        var helmetValue = helmets.contains(helmetItem) ? helmets.indexOf(helmetItem) + 4 : 0;

        var chestplateItem = chestplate.getItem();
        var chestplateValue = chestplates.contains(chestplateItem) ? chestplates.indexOf(chestplateItem) + 4 : 0;

        var leggingsItem = leggings.getItem();
        var leggingsValue = leggingses.contains(leggingsItem) ? leggingses.indexOf(leggingsItem) + 4 : 0;

        var bootsItem = boots.getItem();
        var bootsValue = bootses.contains(bootsItem) ? bootses.indexOf(bootsItem) + 4 : 0;

        value = mainHandValue + offHandValue + helmetValue + chestplateValue + leggingsValue + bootsValue;
        return value;
    }

}
