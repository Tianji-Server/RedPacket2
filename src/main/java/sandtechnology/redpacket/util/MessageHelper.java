package sandtechnology.redpacket.util;

import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;
import static sandtechnology.redpacket.RedPacketPlugin.getInstance;
import static sandtechnology.redpacket.util.JsonHelper.getGson;

/**
 * 消息工具类（基于 Adventure v5）
 */
public class MessageHelper {
    private static final Map<UUID, List<String>> massageMap = new HashMap<>();
    private static final Map<UUID, List<Component>> componentMassageMap = new HashMap<>();
    private static final Type massageMapType = new TypeToken<Map<UUID, List<String>>>() {}.getType();
    private static final GsonComponentSerializer GSON_SERIALIZER = GsonComponentSerializer.gson();

    private MessageHelper() {
    }

    public static Map<UUID, List<String>> getMassageMap() {
        return massageMap;
    }

    public static Map<UUID, List<Component>> getComponentMassageMap() {
        return componentMassageMap;
    }

    /**
     * 构造 [红包] 前缀组件
     */
    private static Component prefix() {
        return Component.text("[红包]", NamedTextColor.GREEN);
    }

    /**
     * 内部使用的离线玩家信息添加方法
     *
     * @param uuid    离线玩家UUID
     * @param massage 要发送的信息内容
     */
    private static void addMassage(UUID uuid, String massage) {
        if (massageMap.containsKey(uuid)) {
            massageMap.get(uuid).add(massage);
        } else {
            massageMap.put(uuid, new ArrayList<>(Collections.singleton(massage)));
        }
    }

    /**
     * 内部使用的离线玩家信息添加方法
     *
     * @param uuid    离线玩家UUID
     * @param massage 要发送的Adventure组件信息内容
     */
    private static void addMassage(UUID uuid, Component... massage) {
        Component component = Component.text().append(massage).build();
        if (componentMassageMap.containsKey(uuid)) {
            componentMassageMap.get(uuid).add(component);
        } else {
            componentMassageMap.put(uuid, new ArrayList<>(Collections.singleton(component)));
        }
    }

    /**
     * 发送专享红包的标题信息
     *
     * @param players  可领取该红包的玩家列表
     * @param title    标题组件
     * @param subtitle 子标题组件
     */
    public static void broadcastSelectiveRedPacket(List<OfflinePlayer> players, Component title, Component subtitle) {
        players.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).forEach(player -> {
            CompatibilityHelper.playLevelUpSound(player);
            CompatibilityHelper.sendTitle(player, title, subtitle);
        });
    }

    /**
     * 发送一般红包的标题信息
     *
     * @param title    标题组件
     * @param subtitle 子标题组件
     */
    public static void broadcastRedPacket(Component title, Component subtitle) {
        getServer().getOnlinePlayers().forEach(player -> {
            CompatibilityHelper.playLevelUpSound(player);
            CompatibilityHelper.sendTitle(player, title, subtitle);
        });
    }

    /**
     * 公告普通信息
     *
     * @param color 颜色
     * @param msg   内容
     */
    public static void broadcastMsg(TextColor color, String msg) {
        getServer().broadcast(prefix().append(Component.text(msg, color)));
    }

    /**
     * 公告Adventure组件信息
     *
     * @param msg 内容
     */
    public static void broadcastMsg(Component... msg) {
        getServer().broadcast(Component.text().append(prefix()).append(msg).build());
    }

    /**
     * 发送普通信息（需要玩家在线）
     *
     * @param sender 接收者
     * @param color  颜色
     * @param msg    内容
     */
    public static void sendSimpleMsg(CommandSender sender, TextColor color, String msg) {
        sender.sendMessage(prefix().append(Component.text(msg, color)));
    }

    /**
     * 发送Adventure组件信息（需要玩家在线）
     *
     * @param sender 接收者
     * @param msg    内容
     */
    public static void sendSimpleMsg(Player sender, Component... msg) {
        sender.sendMessage(Component.text().append(msg).build());
    }

    /**
     * 发送Adventure组件信息（不需要玩家在线）
     *
     * @param sender 接收者
     * @param msg    内容
     */
    public static void sendServiceMsg(OfflinePlayer sender, Component... msg) {
        if (sender.isOnline()) {
            sendSimpleMsg(sender.getPlayer(), msg);
        } else {
            addMassage(sender.getUniqueId(), msg);
        }
    }

    /**
     * 发送普通信息（不需要玩家在线）
     *
     * @param sender 接收者
     * @param color  颜色
     * @param msg    内容
     */
    public static void sendServiceMsg(OfflinePlayer sender, TextColor color, String msg) {
        if (sender.isOnline()) {
            sendSimpleMsg(sender.getPlayer(), color, msg);
        } else {
            addMassage(sender.getUniqueId(), msg);
        }
    }

    /**
     * 初始化方法
     *
     * @param status 设置状态。true为启动。false为禁用
     */
    public static void setStatus(boolean status) {
        Path path = getInstance().getDataFolder().toPath().resolve("PlayerData.json");
        if (status) {
            if (Files.exists(path)) {
                try {
                    FromJson(Files.readAllLines(path));
                } catch (IOException ex) {
                    throw new RuntimeException("无法加载将要发送给玩家的消息！", ex);
                }
            }
        } else {
            try {
                Files.write(path, getJson(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("无法保存将要发送给玩家的消息！", e);
            }
        }
    }

    /**
     * 离线玩家信息反序列化方法
     *
     * @param json 序列化后的内容
     */
    @SuppressWarnings("unchecked")
    private static void FromJson(List<String> json) {
        if (json.size() == 2) {
            massageMap.putAll(getGson().fromJson(json.get(0), massageMapType));
            ((Map<UUID, List<String>>) getGson().fromJson(json.get(1), massageMapType))
                    .forEach((k, v) -> componentMassageMap.put(k, v.parallelStream().map(MessageHelper::deserializeComponent).collect(Collectors.toList())));
        }
    }

    /**
     * 反序列化单个组件，旧版本（BungeeCord）数据可能无法解析，失败时返回空组件
     */
    private static Component deserializeComponent(String json) {
        try {
            return GSON_SERIALIZER.deserialize(json);
        } catch (Exception e) {
            return Component.empty();
        }
    }

    /**
     * 离线玩家信息序列化方法
     */
    private static List<String> getJson() {
        HashMap<UUID, List<String>> componentMap = new HashMap<>();
        componentMassageMap.entrySet().parallelStream().forEach(e ->
                componentMap.put(e.getKey(), e.getValue().parallelStream().map(GSON_SERIALIZER::serialize).collect(Collectors.toList())));
        return Arrays.asList(getGson().toJson(massageMap), getGson().toJson(componentMap));
    }
}
