package sandtechnology.redpacket.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import sandtechnology.redpacket.redpacket.RedPacket;
import sandtechnology.redpacket.session.CreateSession;

import java.util.Arrays;

import static sandtechnology.redpacket.session.SessionManager.getSessionManager;
import static sandtechnology.redpacket.util.RedPacketManager.getRedPacketManager;

/**
 * 聊天监听器（基于 Paper 的 AsyncChatEvent + Adventure v5）
 */
public class ChatListener implements Listener {

    private static final CreateSession.State[] inputNeededState = {CreateSession.State.WaitAmount, CreateSession.State.WaitExtra, CreateSession.State.WaitGiver, CreateSession.State.WaitMoney};
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = PLAIN_SERIALIZER.serialize(event.message());
        //判断是否在输入创建红包的数据
        if (getSessionManager().hasSession(player) && Arrays.stream(inputNeededState).anyMatch(state -> state == getSessionManager().getSession(player).getState())) {
            getSessionManager().getSession(player).parse(player, message);
            event.setCancelled(true);
        }
        //AsyncChatEvent 总是异步触发，直接进行红包判断
        checkRedPacket(player, message);
    }

    private void checkRedPacket(Player player, String message) {
        getRedPacketManager().getRedPackets().stream().filter(redPacket -> redPacket.getType().equals(RedPacket.RedPacketType.JieLongRedPacket) || redPacket.getType().equals(RedPacket.RedPacketType.PasswordRedPacket)).forEach(redPacket -> redPacket.giveIfValid(player, message));
    }

}
