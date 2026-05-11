package ink.magma.fastminecart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import com.google.common.collect.Iterables;

public class VehicleEventListener implements Listener {
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerEnterMineCart(VehicleEnterEvent event) {
        if (event.getVehicle() instanceof RideableMinecart && event.getEntered() instanceof Player player) {
            if (!lastMessageTime.containsKey(player.getUniqueId())
                    || System.currentTimeMillis() - lastMessageTime.get(player.getUniqueId()) >= 30 * 1000) {
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

    @EventHandler
    void onMineCartMove(VehicleMoveEvent e) {
        if (!(e.getVehicle() instanceof RideableMinecart cart))
            return;

        Entity passenger = Iterables.getFirst(cart.getPassengers(), null);
        if (!(passenger instanceof Player player))
            return;

        // 检查矿车是否在铁轨上
        Block blockOn = cart.getLocation().getBlock();
        if (!(blockOn.getBlockData() instanceof Rail)) {
            // 如果不在铁轨上，恢复默认速度
            FastMinecart.speedManager.setMineCartMaxSpeed(cart, FastMinecart.speedManager.getDefaultSpeed());
            return;
        }

        // 应用速度
        double playerSpeedMultiplier = FastMinecart.speedManager.getPlayerSpeedMultiplier(player);
        double newSpeed = FastMinecart.speedManager.getDefaultSpeed() * playerSpeedMultiplier;
        FastMinecart.speedManager.setMineCartMaxSpeed(cart, newSpeed);
    }
}
