package ink.magma.fastminecart;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;

@Command({"fastminecart", "fm"})
public class MainCommand {
    final MiniMessage mini = MiniMessage.miniMessage();

    Component speedMultiplierInvalidErrorMessage = mini.deserialize("<gray>倍率不合法, 应当大于 0 或小于等于 4.");

    @Subcommand("debug")
    public void getCartInfo(Player sender) {
        if (FastMinecart.instance.debugPlayers.contains(sender.getUniqueId())) {
            FastMinecart.instance.debugPlayers.remove(sender.getUniqueId());
            sender.sendMessage(mini.deserialize("<gray>已关闭矿车速度显示."));
        } else {
            FastMinecart.instance.debugPlayers.add(sender.getUniqueId());
            sender.sendMessage(mini.deserialize("<gray>已开启矿车速度显示."));
        }
    }

    @Subcommand("multiplier set")
    public void multiplierSet(Player sender, @Named("速率") Double multiplier) {
        try {
            FastMinecart.speedManager.setPlayerSpeedMultiplier(sender, multiplier);
            FastMinecart.speedManager.sendPlayerControlPanel(sender);
        } catch (SpeedManager.SpeedMultiplierInvalidError e) {
            sender.sendMessage(speedMultiplierInvalidErrorMessage);
        }
    }

    @Subcommand("multiplier get")
    public void multiplierGet(Player sender) {
        FastMinecart.speedManager.sendPlayerControlPanel(sender);
    }

    @Subcommand("multiplier add")
    public void multiplierAdd(Player sender, @Named("增加的速率") Double multiplierAdd) {
        Double current = FastMinecart.speedManager.getPlayerSpeedMultiplier(sender);
        try {
            FastMinecart.speedManager.setPlayerSpeedMultiplier(sender, current + multiplierAdd);
            FastMinecart.speedManager.sendPlayerControlPanel(sender);
        } catch (SpeedManager.SpeedMultiplierInvalidError e) {
            sender.sendMessage(speedMultiplierInvalidErrorMessage);
        }
    }

    @Subcommand("multiplier reduce")
    public void multiplierReduce(Player sender, @Named("减少的速率") Double multiplierReduce) {
        Double current = FastMinecart.speedManager.getPlayerSpeedMultiplier(sender);
        try {
            FastMinecart.speedManager.setPlayerSpeedMultiplier(sender, current - multiplierReduce);
            FastMinecart.speedManager.sendPlayerControlPanel(sender);
        } catch (SpeedManager.SpeedMultiplierInvalidError e) {
            sender.sendMessage(speedMultiplierInvalidErrorMessage);
        }
    }

}
