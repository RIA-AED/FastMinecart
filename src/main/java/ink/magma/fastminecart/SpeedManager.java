package ink.magma.fastminecart;

import com.google.common.collect.Iterables;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.MessageFormat;

public class SpeedManager {
    private final ConfigurationSection playerData;
    private final FastMinecart plugin;
    private final MiniMessage mini = MiniMessage.miniMessage();

    SpeedManager(FastMinecart plugin) {
        this.playerData = plugin.getConfig().getConfigurationSection("player-data");
        this.plugin = plugin;
    }

    /**
     * 获取玩家的倍率
     */
    public double getPlayerSpeedMultiplier(Player player) {
        // 空为 0
        double speedMultiplier = playerData.getDouble("speed-multiplier." + player.getUniqueId());
        if (speedMultiplier > 0.0D && speedMultiplier <= 4.0D) {
            return speedMultiplier;
        } else {
            return 4D;
        }
    }

    /**
     * 设置玩家的倍率
     *
     * @throws SpeedMultiplierInvalidError 将在倍率范围不合法时掷出
     */
    public void setPlayerSpeedMultiplier(Player player, Double multiplier) throws SpeedMultiplierInvalidError {
        if (multiplier > 0.0D && multiplier <= 4.0D) {
            playerData.set("speed-multiplier." + player.getUniqueId(), multiplier);
            plugin.saveConfig();
        } else {
            throw new SpeedMultiplierInvalidError();
        }
    }

    public static class SpeedMultiplierInvalidError extends Exception {
    }


    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public void setMineCartMaxSpeed(Minecart cart, double speed) {
        cart.setMaxSpeed(speed);

        // for debug enabled player
        if (FastMinecart.instance.debugPlayers.isEmpty()) return;
        Entity passenger = Iterables.getFirst(cart.getPassengers(), null);
        if (passenger instanceof Player player && FastMinecart.instance.debugPlayers.contains(player.getUniqueId())) {
            player.sendActionBar(Component.text("矿车最高速度更新: " + decimalFormat.format(speed)));
        }
    }


    public void sendPlayerControlPanel(Player player) {
        player.sendMessage(mini.deserialize(
                MessageFormat.format(
                        "<gray>您乘坐矿车会应用的速度倍率: <white><hover:show_text:'点击输入倍率'><click:suggest_command:'/fastminecart multiplier set '>[ {0}x ]</click></hover></white> <color:#51cf66><click:run_command:'/fastminecart multiplier add 0.25'><hover:show_text:'增加 0.25'>[+]</hover></click></color> <color:#339af0><click:run_command:'/fastminecart multiplier reduce 0.25'><hover:show_text:'减少 0.25'>[-]</hover></click></color> <color:#adb5bd><click:run_command:'/fastminecart multiplier set 4'><hover:show_text:'设置为 4'>[重置]</hover></click></color> <color:#adb5bd><click:run_command:'/fastminecart multiplier set 1'><hover:show_text:'设置为 1'>[原版]</hover></click></color></gray>",
                        decimalFormat.format(FastMinecart.speedManager.getPlayerSpeedMultiplier(player))
                )
        ));
    }

    public double getDefaultSpeed() {
        return 0.4D;
    }

}
