package ink.magma.fastminecart;

import com.google.common.collect.Iterables;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class VehicleEventListener implements Listener {
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerEnterMineCart(VehicleEnterEvent event) {
        if (event.getVehicle() instanceof RideableMinecart && event.getEntered() instanceof Player player) {
            if (!lastMessageTime.containsKey(player.getUniqueId()) || System.currentTimeMillis() - lastMessageTime.get(player.getUniqueId()) >= 30 * 1000) {
                // 如果已经超过冷却时间，发送消息
                FastMinecart.speedManager.sendPlayerControlPanel(player);

                // 更新上次发送消息的时间
                lastMessageTime.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerExitMineCart(VehicleExitEvent event) {
        if (event.getVehicle() instanceof RideableMinecart cart && event.getExited() instanceof Player) {
            FastMinecart.speedManager.setMineCartMaxSpeed(cart, FastMinecart.speedManager.getDefaultSpeed());
        }
    }

    // 以下加速相关算法来自 TCExpress 插件, 基于 GPL-3.0
    // The following code originally came from the TCExpress plugin, under the GPL-3.0 license
    // https://github.com/theTd/TCExpress
    Collection<Rail.Shape> ascendingShapes = List.of(Rail.Shape.ASCENDING_EAST, Rail.Shape.ASCENDING_NORTH, Rail.Shape.ASCENDING_SOUTH, Rail.Shape.ASCENDING_WEST);
    Collection<Rail.Shape> xShapes = List.of(Rail.Shape.EAST_WEST, Rail.Shape.ASCENDING_WEST, Rail.Shape.ASCENDING_EAST);
    Collection<Rail.Shape> zShapes = List.of(Rail.Shape.NORTH_SOUTH, Rail.Shape.ASCENDING_SOUTH, Rail.Shape.ASCENDING_NORTH);
    Collection<Rail.Shape> turnShapes = List.of(Rail.Shape.NORTH_EAST, Rail.Shape.NORTH_WEST, Rail.Shape.SOUTH_EAST, Rail.Shape.SOUTH_WEST);

    private final static int BUFFER_LENGTH = 5;
    private final static int ADJUST_LENGTH = 20;
    private final static double NORMAL_SPEED = FastMinecart.speedManager.getDefaultSpeed();

    @EventHandler
    void onMineCartMove(VehicleMoveEvent e) {
        // 如果车辆不是可乘坐矿车，则结束方法
        if (!(e.getVehicle() instanceof RideableMinecart cart)) return;

        // 乘客
        Entity passenger = Iterables.getFirst(cart.getPassengers(), null);
        // 如果乘客不是玩家，则结束方法
        if (!(passenger instanceof Player player)) return;

        // 获取矿车当前方块
        Block curBlock = cart.getLocation().getBlock();
        // 如果当前方块不是铁轨，则将矿车最大速度设置为正常速度并结束方法
        if (!(curBlock.getBlockData() instanceof Rail curRail)) {
            FastMinecart.speedManager.setMineCartMaxSpeed(cart, NORMAL_SPEED);
            return;
        }

        // 如果是倾斜铁轨或转弯铁轨，则将矿车最大速度设置为正常速度并结束方法
        if (ascendingShapes.contains(curRail.getShape()) || turnShapes.contains(curRail.getShape())) {
            FastMinecart.speedManager.setMineCartMaxSpeed(cart, NORMAL_SPEED);
            return;
        }

        // 获取车辆当前速度向量
        Vector vector = e.getVehicle().getVelocity();
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        // 如果是停车，则将矿车最大速度设置为正常速度并结束方法
        // 如果 Y 轴速度不为零，则将矿车最大速度设置为正常速度并结束方法
        if ((x == 0 && z == 0) || (y != 0)) {
            FastMinecart.speedManager.setMineCartMaxSpeed(cart, NORMAL_SPEED);
            return;
        }

        // 根据速度向量的方向确定下一个铁轨方块的方向
        boolean isX = x != 0 && z == 0;
        boolean n = isX ? x < 0 : z < 0;
        BlockFace direction = isX ? (n ? BlockFace.WEST : BlockFace.EAST) : (n ? BlockFace.NORTH : BlockFace.SOUTH);

        // 计算当前方向上同样朝向的铁轨长度
        int flatLength = 0;
        while (flatLength < BUFFER_LENGTH + ADJUST_LENGTH) {
            Block blockRelative = curBlock.getRelative(direction, flatLength + 1);
            if (blockRelative.getBlockData() instanceof Rail rail) {
                if (isX) {
                    if (!xShapes.contains(rail.getShape())) break;
                } else {
                    if (!zShapes.contains(rail.getShape())) break;
                }
            } else {
                break;
            }

            flatLength++;
        }

        // 如果水平铁轨长度不足 BUFFER_LENGTH，则将矿车最大速度设置为正常速度并结束方法
        if (flatLength < BUFFER_LENGTH) {
            FastMinecart.speedManager.setMineCartMaxSpeed(cart, NORMAL_SPEED);
            return;
        }

        // 自由铁轨长度 = 总轨道长度 - 缓冲长度 (5)
        int freeLength = flatLength - BUFFER_LENGTH;

        // 计算调整比例   freeLength ↓ / 20
        double s = (double) freeLength / ADJUST_LENGTH;
        if (s > 1) s = 1;
        // 根据调整比例计算速度
        double speed = NORMAL_SPEED + (NORMAL_SPEED * FastMinecart.speedManager.getPlayerSpeedMultiplier(player) - NORMAL_SPEED) * s;
        // 设置矿车最大速度
        FastMinecart.speedManager.setMineCartMaxSpeed(cart, speed);
    }
}
