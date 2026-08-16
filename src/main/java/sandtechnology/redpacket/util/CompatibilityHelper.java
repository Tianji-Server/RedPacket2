package sandtechnology.redpacket.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * 兼容工具类（基于 Adventure v5）。
 * 自 Paper 1.20+ 起，所有标题、消息等功能均通过官方 API / Adventure 提供，
 * 无需任何反射 / NMS 兼容代码。
 */
public class CompatibilityHelper {

    private CompatibilityHelper() {
    }

    /**
     * 初始化方法。保留以兼容旧调用，现为空操作。
     */
    public static void setup() {
        // 无需任何操作
    }

    public static void playLevelUpSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);
    }

    public static void playMeowSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 100, 1);
    }

    /**
     * 使用 Adventure 发送标题
     *
     * @param player   接收者
     * @param title    标题组件
     * @param subtitle 子标题组件
     */
    public static void sendTitle(Player player, Component title, Component subtitle) {
        player.showTitle(Title.title(title, subtitle));
    }

    /**
     * 使用 Adventure 发送消息组件
     *
     * @param player     接收者
     * @param components 消息组件
     */
    public static void sendMessage(Player player, Component... components) {
        player.sendMessage(Component.text().append(components).build());
    }
}
