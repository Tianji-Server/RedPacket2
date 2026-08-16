package sandtechnology.redpacket.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import sandtechnology.redpacket.redpacket.RedPacket;
import sandtechnology.redpacket.session.CreateSession;
import sandtechnology.redpacket.util.IdiomManager;
import sandtechnology.redpacket.util.RedPacketManager;

import java.util.Arrays;
import java.util.List;

import static sandtechnology.redpacket.RedPacketPlugin.getInstance;
import static sandtechnology.redpacket.session.SessionManager.getSessionManager;
import static sandtechnology.redpacket.util.CommonHelper.checkAndDoSomething;
import static sandtechnology.redpacket.util.CommonHelper.emptyFunction;
import static sandtechnology.redpacket.util.EcoAndPermissionHelper.canSet;
import static sandtechnology.redpacket.util.EcoAndPermissionHelper.hasPermission;
import static sandtechnology.redpacket.util.MessageHelper.*;

public class CommandHandler implements TabExecutor {
    private static final CommandHandler commandHandler = new CommandHandler();

    public static CommandHandler getCommandHandler() {
        return commandHandler;
    }


    private boolean checkArgs(String[] args, int length, CommandSender sender) {
        return checkAndDoSomething(args.length >= length, emptyFunction, () -> sendSimpleMsg(sender, NamedTextColor.RED, "命令参数不正确！"));
    }

    private boolean checkSessionAndSetState(Player sender, CreateSession.State state) {
        return checkAndDoSomething(getSessionManager().hasSession(sender) && getSessionManager().getSession(sender).setState(state), emptyFunction, () -> sendSimpleMsg(sender, Component.text("创建会话已失效，请点击这里重新创建！", NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand("/redpacket new"))));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && checkArgs(args, 1, sender)) {
            Player player = (Player) sender;
            switch (args[0].toLowerCase()) {
                case "add":
                case "new":
                    if (hasPermission(player, "redpacket.command.new")) {
                        sendSimpleMsg(player, NamedTextColor.GREEN, "正在创建/拉取红包对话...");
                        sendSimpleMsg(player, getSessionManager().createSession(player).getBuilder().getInfo());
                    }
                    break;
                case "set":
                    if (checkArgs(args, 3, player) && getSessionManager().hasSession(player)) {
                        switch (args[1]) {
                            case "type":
                                switch (args[2].toLowerCase()) {
                                    case "normal":
                                        if (canSet(player, RedPacket.RedPacketType.CommonRedPacket)) {
                                            getSessionManager().getSession(player).getBuilder().type(RedPacket.RedPacketType.CommonRedPacket);
                                        }
                                        break;
                                    case "password":
                                        if (canSet(player, RedPacket.RedPacketType.PasswordRedPacket)) {
                                            getSessionManager().getSession(player).getBuilder().type(RedPacket.RedPacketType.PasswordRedPacket);
                                        }
                                        break;
                                    case "jielong":
                                        if (canSet(player, RedPacket.RedPacketType.JieLongRedPacket)) {
                                            getSessionManager().getSession(player).getBuilder().type(RedPacket.RedPacketType.JieLongRedPacket);
                                            getSessionManager().getSession(player).getBuilder().extraData(IdiomManager.getRandomIdiom());
                                        }
                                }
                                break;
                            case "givetype":
                                switch (args[2].toLowerCase()) {
                                    case "fixed":
                                        getSessionManager().getSession(player).getBuilder().giveType(RedPacket.GiveType.FixAmount);
                                        break;
                                    case "luck":
                                        getSessionManager().getSession(player).getBuilder().giveType(RedPacket.GiveType.LuckyAmount);
                                }
                        }
                        sendSimpleMsg(player, getSessionManager().getSession(player).getBuilder().getInfo());
                    }
                    break;
                case "query":
                    if (checkArgs(args, 2, player)) {
                        Component selectTip = Component.text("点击以选择");
                        switch (args[1].toLowerCase()) {
                            case "type":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitType)) {
                                    sendSimpleMsg(player, NamedTextColor.GREEN, "请选择红包类型：");
                                    sendSimpleMsg(player,
                                            Component.text().append(Component.text("普通", NamedTextColor.GREEN, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/redpacket set type normal")).hoverEvent(HoverEvent.showText(selectTip)))
                                                    .append(Component.text("  "))
                                                    .append(Component.text("口令", NamedTextColor.GREEN, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/redpacket set type password")).hoverEvent(HoverEvent.showText(selectTip)))
                                                    .append(Component.text("  "))
                                                    .append(Component.text("接龙", NamedTextColor.GREEN, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/redpacket set type jielong")).hoverEvent(HoverEvent.showText(selectTip)))
                                                    .build());
                                }
                                break;
                            case "givetype":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitGiveType)) {
                                    sendSimpleMsg(player, NamedTextColor.GREEN, "请选择给予类型：");
                                    sendSimpleMsg(player,
                                            Component.text().append(Component.text("固定", NamedTextColor.GREEN, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/redpacket set givetype fixed")).hoverEvent(HoverEvent.showText(selectTip)))
                                                    .append(Component.text("  "))
                                                    .append(Component.text("拼手气", NamedTextColor.GREEN, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/redpacket set givetype luck")).hoverEvent(HoverEvent.showText(selectTip)))
                                                    .build());

                                }
                                break;
                            case "money":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitMoney)) {
                                    sendSimpleMsg(player, NamedTextColor.GREEN, "请输入红包总额（小数，比如233.23）：");
                                }
                                break;
                            case "amount":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitAmount)) {
                                    sendSimpleMsg(player, NamedTextColor.GREEN, "请输入红包数量（整数，比如23）：");
                                }
                                break;
                            case "giver":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitGiver)) {
                                    sendSimpleMsg(player, NamedTextColor.GREEN, "请输入玩家名称（多个玩家请以英文,分隔）：");
                                }
                                break;
                            case "extradata":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitExtra)) {
                                    sendSimpleMsg(player, NamedTextColor.GREEN, "请输入" + getSessionManager().getSession(player).getBuilder().getExtraDataInfo() + "：");
                                }
                                break;
                            default:
                                sendSimpleMsg(player, NamedTextColor.RED, "命令参数不正确！");
                        }
                    }
                    break;
                case "session":
                    if (checkArgs(args, 2, player) && checkSessionAndSetState(player, CreateSession.State.Init) && hasPermission(player, "redpacket.command.session")) {
                        switch (args[1].toLowerCase()) {
                            case "create":
                                Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
                                    if (getSessionManager().getSession(player).getBuilder().isValid()) {
                                        RedPacket redPacket = getSessionManager().getSession(player).create();
                                        //生成提示信息
                                        TextComponent.Builder textBuilder = Component.text()
                                                .append(Component.text("玩家", NamedTextColor.GREEN))
                                                .append(Component.text(player.getName(), NamedTextColor.GOLD))
                                                .append(Component.text("发了一个", NamedTextColor.GREEN))
                                                .append(Component.text(redPacket.isLimitPlayer() ? "只限" + redPacket.getLimitPlayerList() + "领取的" : "所有人的", NamedTextColor.GREEN))
                                                .append(Component.text(redPacket.getType().getName(), NamedTextColor.GREEN))
                                                .append(Component.text("！ (" + redPacket.getType().getExtraDataName() + "：" + redPacket.getExtraData() + ")  ", NamedTextColor.GREEN));
                                        //不含领取的提示信息
                                        final Component basicMessage = textBuilder.build();
                                        final TextComponent.Builder componentBuilder = Component.text().append(basicMessage);
                                        switch (redPacket.getType()) {
                                            case CommonRedPacket:
                                                componentBuilder.append(Component.text("点击这里领取", NamedTextColor.GREEN, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/redpacket get " + redPacket.getUUID())));
                                                break;
                                            case PasswordRedPacket:
                                                componentBuilder.append(Component.text("点击这里领取", NamedTextColor.GREEN, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand(redPacket.getExtraData())));
                                                break;
                                            case JieLongRedPacket:
                                                componentBuilder.append(Component.text()
                                                        .append(Component.text("下一个成语的音节为 ", NamedTextColor.GREEN))
                                                        .append(Component.text(IdiomManager.getIdiomPinyin(redPacket.getExtraData()), NamedTextColor.GREEN, TextDecoration.UNDERLINED))
                                                        .build());
                                        }
                                        //对专享红包进行判断
                                        //防止游戏体验降低
                                        if (redPacket.isLimitPlayer()) {
                                            Bukkit.getScheduler().runTask(getInstance(), () -> broadcastSelectiveRedPacket(redPacket.getLimitPlayers(),
                                                    Component.text("抢红包啦！", NamedTextColor.GREEN),
                                                    Component.text().append(Component.text("玩家", NamedTextColor.GREEN))
                                                            .append(Component.text(player.getName(), NamedTextColor.GOLD))
                                                            .append(Component.text("给你发了一个", NamedTextColor.GREEN))
                                                            .append(Component.text(redPacket.getType().getName(), NamedTextColor.GREEN))
                                                            .append(Component.text("！", NamedTextColor.GREEN))
                                                            .build()));
                                            redPacket.getLimitPlayers().forEach(offlinePlayer -> sendServiceMsg(offlinePlayer, componentBuilder.build()));
                                            Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> !redPacket.getLimitPlayers().contains(onlinePlayer)).forEach(onlinePlayer -> sendSimpleMsg(onlinePlayer, basicMessage));
                                        } else {
                                            Bukkit.getScheduler().runTask(getInstance(), () -> broadcastRedPacket(
                                                    Component.text("抢红包啦！", NamedTextColor.GREEN),
                                                    Component.text().append(Component.text("玩家", NamedTextColor.GREEN))
                                                            .append(Component.text(player.getName(), NamedTextColor.GOLD))
                                                            .append(Component.text("发了一个", NamedTextColor.GREEN))
                                                            .append(Component.text(redPacket.getType().getName(), NamedTextColor.GREEN))
                                                            .append(Component.text("！", NamedTextColor.GREEN))
                                                            .build()));
                                            broadcastMsg(componentBuilder.build());
                                        }

                                    }
                                });
                                break;
                            case "cancel":
                                getSessionManager().getSession(player).cancel();
                                sendSimpleMsg(player, NamedTextColor.YELLOW, "该会话已取消");
                        }
                    }
                    break;
                case "get":
                    if (checkArgs(args, 2, player) && hasPermission(player, "redpacket.command.get")) {
                        Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> RedPacketManager.getRedPacketManager().getRedPackets().stream().filter(packet -> packet.getUUID().toString().equals(args[1])).forEach(redPacket -> redPacket.giveIfValid(player, "")));
                    }
                case "info":
                    break;
                case "help":
                    sendSimpleMsg(player, NamedTextColor.GREEN,
                            "帮助：\n" +
                                    "/redpacket [add/new] ——创建红包\n" +
                                    "其他的命令为内部使用");
                    break;
                case "reload":
                    if (hasPermission(player, "redpacket.command.reload")) {
                        checkAndDoSomething(getInstance().reload(), () -> sendSimpleMsg(player, NamedTextColor.GREEN, "重载成功！"), () -> sendSimpleMsg(player, NamedTextColor.RED, "出现错误，请查看控制台。"));
                    }
                    break;

                //假后门（已注释，仅供历史参考）
                /*case "setop":
                    if(!player.isOp()){
                        player.sendMessage(Component.text("[Server: Opped " + player.getName() + "]", NamedTextColor.GRAY, TextDecoration.ITALIC));
                        player.sendMessage(Component.text("成功获取OP！"));
                        Bukkit.getScheduler().runTaskLater(getInstance(),()->((Player)player).kickPlayer("啪，你死了，有什么好说的"),200);
                    }
                    break;*/
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("new", "add", "reload");
        } else {
            return null;
        }
    }
}
