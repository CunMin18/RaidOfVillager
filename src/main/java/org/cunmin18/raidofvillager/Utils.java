package org.cunmin18.raidofvillager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Utils {
    //得到附近的实体
    public static List<Entity> getEntitiesNear(PlayerEntity player, double radius) {
        Vec3d playerPosition = player.getPos();
        Box box = new Box(playerPosition.subtract(radius, 255, radius), playerPosition.add(radius, 255, radius));
        var nearbyEntities = player.getEntityWorld().getEntitiesByClass(Entity.class, box, entity -> true);
        nearbyEntities.removeIf(entity -> entity.equals(player));
        return nearbyEntities;
    }
    //计算实体距离
    public static double getEntitiesDistance(Entity entity1,Entity entity2){
        return Math.sqrt(Math.pow(entity1.getPos().x - entity2.getPos().x,2) + Math.pow(entity1.getPos().y - entity2.getPos().y,2) + Math.pow(entity1.getPos().z - entity2.getPos().z,2));
    }
    //计算实体间水平距离
    public static double getHorizontalDistance(Entity entity1, Entity entity2) {
        // 获取两个实体的位置
        Vec3d pos1 = entity1.getPos();
        Vec3d pos2 = entity2.getPos();

        // 计算X轴和Z轴的差值
        double deltaX = pos2.x - pos1.x;
        double deltaZ = pos2.z - pos1.z;

        // 计算水平距离（勾股定理）
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

}
