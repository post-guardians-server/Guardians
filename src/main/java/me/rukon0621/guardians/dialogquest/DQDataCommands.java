package me.rukon0621.guardians.dialogquest;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static me.rukon0621.guardians.main.pfix;

public class DQDataCommands implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"대화문완료", "대화문미완료", "퀘스트완료", "퀘스트미완료", "퀘스트클리어",  "쿨타임설정", "목표완료", "강제포기"};

    public DQDataCommands() {
        plugin.getCommand("dqpdc").setExecutor(this);
        plugin.getCommand("dqpdc").setTabCompleter(new DQDataCommandsTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[0]);
        if(target==null) {
            Msg.send(player, "&c제대로 된 플레이어의 이름을 입력해주세요.", pfix);
            return true;
        }
        if(args.length==1) {
            usages(player);
            return true;
        }

        if(args[1].equals("대화문")) {
            if(args.length<3) {
                usages(player);
                return true;
            }
            if(args[2].equals("완료")) {
                if(args.length<4) {
                    usage(player, "대화문완료", true);
                    return true;
                }
                String name = ArgHelper.sumArg(args, 3);

                if(!DialogQuestManager.getDialogData().containsKey(name)) {
                    Msg.send(player, "&c해당 대화문은 존재하지 않는 대화문입니다.", prefix(name));
                    return true;
                }
                Set<String> list = DialogQuestManager.getReadDialogs(target);
                if(list.contains(name)) {
                    Msg.send(player, "&c해당 플레이어는 이미 대화문을 읽었습니다.", prefix(name));
                    return true;
                }
                list.add(name);
                DialogQuestManager.setReadDialogs(target, list);
                Msg.send(player, "성공적으로 데이터를 수정하였습니다.", prefix(name));
            }
            else if (args[2].equals("미완료")) {
                if(args.length<4) {
                    usage(player, "대화문미완료", true);
                    return true;
                }
                String name = ArgHelper.sumArg(args, 3);
                if(!DialogQuestManager.getDialogData().containsKey(name)) {
                    Msg.send(player, "&c해당 대화문은 존재하지 않는 대화문입니다.", prefix(name));
                    return true;
                }
                Set<String> list = DialogQuestManager.getReadDialogs(target);
                if(!list.contains(name)) {
                    Msg.send(player, "&c해당 플레이어는 아직 대화문을 읽지 않았습니다.", prefix(name));
                    return true;
                }
                list.remove(name);
                DialogQuestManager.setReadDialogs(target, list);
                Msg.send(player, "성공적으로 데이터를 수정하였습니다.", prefix(name));

            }
            else {
                usages(player);
                return true;
            }
        }
        else if(args[1].equals("퀘스트")) {
            if(args.length<3) {
                usages(player);
                return true;
            }
            if(args[2].equals("완료")) {
                if(args.length<4) {
                    usage(player, "퀘스트완료", true);
                    return true;
                }
                String name = ArgHelper.sumArg(args, 3);
                if(!DialogQuestManager.getQuestData().containsKey(name)) {
                    Msg.send(player, "&c해당 퀘스트는 존재하지 않는 퀘스트입니다.", prefix(name));
                    return true;
                }
                Set<String> list = DialogQuestManager.getCompletedQuests(target);
                if(list.contains(name)) {
                    Msg.send(player, "&c해당 플레이어는 이미 해당 퀘스트를 클리어하였습니다.", prefix(name));
                    return true;
                }
                list.add(name);
                DialogQuestManager.setCompletedQuests(target, list);
                Msg.send(player, "성공적으로 데이터를 수정하였습니다.", prefix(name));
            }
            else if(args[2].equals("미완료")) {
                if(args.length<4) {
                    usage(player, "퀘스트미완료", true);
                    return true;
                }
                String name = ArgHelper.sumArg(args, 3);
                if(!DialogQuestManager.getQuestData().containsKey(name)) {
                    Msg.send(player, "&c해당 퀘스트는 존재하지 않는 퀘스트입니다.", prefix(name));
                    return true;
                }
                Set<String> list = DialogQuestManager.getCompletedQuests(target);
                if(!list.contains(name)) {
                    Msg.send(player, "&c해당 플레이어는 아직 해당 퀘스트를 클리어하지 않았습니다.", prefix(name));
                    return true;
                }
                list.remove(name);
                DialogQuestManager.setCompletedQuests(target, list);
                Msg.send(player, "성공적으로 데이터를 수정하였습니다.", prefix(name));
            }
            else if(args[2].equals("강제포기")) {
                if(args.length<4) {
                    usage(player, "강제포기", true);
                    return true;
                }
                String name = ArgHelper.sumArg(args, 3);
                if(!DialogQuestManager.getQuestData().containsKey(name)) {
                    Msg.send(player, "&c해당 퀘스트는 존재하지 않는 퀘스트입니다.", prefix(name));
                    return true;
                }
                ArrayList<QuestInProgress> list = DialogQuestManager.getQuestsInProgress(target);
                QuestInProgress qip;
                for(QuestInProgress qips : list) {
                    if(qips.getName().equals(name)) {
                        qip = qips;
                        list.remove(qip);
                        DialogQuestManager.setQuestInProgress(player, list);
                        Msg.send(player, "&c해당 퀘스트를 중단시켰습니다.", prefix(name));
                        return true;
                    }
                }
                Msg.send(player, "&c해당 플레이어는 그 퀘스트를 진행하고 있지 않습니다.", prefix(name));
                return true;
            }
            else if(args[2].startsWith("쿨타임")) {
                if(args.length<4) {
                    DialogQuestManager.setQuestCooltime(target, new HashMap<>());
                    Msg.send(player, "&c성공적으로 해당 플레이어의 모든 퀘스트 쿨타임을 초기화했습니다.", pfix);
                    return true;
                }
                String name = ArgHelper.sumArg(args, 3);
                if(!DialogQuestManager.getQuestData().containsKey(name)) {
                    Msg.send(player, "&c해당 퀘스트는 존재하지 않는 퀘스트입니다.", prefix(name));
                    return true;
                }
                Map<String, Long> data = DialogQuestManager.getQuestCooltime(target);
                data.remove(name);
                DialogQuestManager.setQuestCooltime(target, data);
                Msg.send(player, "성공적으로 데이터를 수정하였습니다.", prefix(name));
            }
            else if(args[2].startsWith("클리어")) {
                if(args.length<4) {
                    usage(player, args[0], true);
                    return true;
                }
                String name = ArgHelper.sumArg(args, 3);
                if(!DialogQuestManager.getQuestData().containsKey(name)) {
                    Msg.send(player, "&c해당 퀘스트는 존재하지 않는 퀘스트입니다.", prefix(name));
                    return true;
                }

                for(QuestInProgress qip : DialogQuestManager.getQuestsInProgress(target)) {
                    if(qip.getName().equals(name)) {
                        qip.completeQuest(target, null, null, true);
                        Msg.send(player, "성공적으로 데이터를 수정하였습니다.", prefix(name));
                        return true;
                    }
                }
                Msg.warn(player, "&c이 플레이어는 이 퀘스트를 가지고 있지 않습니다.", prefix(name));
            }
            else if (args[2].equals("목표완료")) {
                if(args.length < 4) {
                    usage(player, "목표완료", true);
                    return true;
                }
                String name = ArgHelper.sumArg(args, 3);
                if(DialogQuestManager.completeCustomObject(target, name)) {
                    Msg.send(player, "&e" + name +" &f목표를 성공시켰습니다.", pfix);
                }
                else {
                    Msg.send(player, "&e" + name +" &f목표를 가지고 있지 않습니다.", pfix);
                }
            }
        }
        else {
            usages(player);
        }

        return true;
    }

    private String prefix(String name) {
        return "&7[ &e"+name+" &7] ";
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equals("대화문완료")) {
            Msg.send(player, "&6/dqpdc <플레이어> 대화문 완료 <대화문 이름>");
            Msg.send(player, "&7   해당 플레이어에게 대화문을 다 읽은 기록을 추가합니다.");
        }
        else if(arg.equals("대화문미완료")) {
            Msg.send(player, "&6/dqpdc <플레이어> 대화문 미완료 <대화문 이름>");
            Msg.send(player, "&7   해당 플레이어에게 대화문을 읽은 기록을 삭제합니다.");
        }
        else if(arg.equals("퀘스트완료")) {
            Msg.send(player, "&6/dqpdc <플레이어> 퀘스트 완료 <퀘스트 이름>");
            Msg.send(player, "&7   해당 플레이어에게 퀘스트를 깬 기록을 추가합니다.");
        }
        else if(arg.equals("퀘스트미완료")) {
            Msg.send(player, "&6/dqpdc <플레이어> 퀘스트 미완료 <퀘스트 이름>");
            Msg.send(player, "&7   해당 플레이어가 해당 퀘스트를 클리어한 기록을 삭제합니다.");
        }
        else if(arg.equals("쿨타임초기화")) {
            Msg.send(player, "&6/dqpdc <플레이어> 퀘스트 쿨타임[초기화] <퀘스트 이름>");
            Msg.send(player, "&7   해당 플레이어의 해당 퀘스트의 퀘스트 쿨타임을 초기화합니다.");
            Msg.send(player, "&7   퀘스트 이름을 정하지 않으면 모든 퀘스트에 적용됩니다.");
        }
        else if(arg.equals("퀘스트클리어")) {
            Msg.send(player, "&6/dqpdc <플레이어> 퀘스트 클리어 <퀘스트 이름>");
            Msg.send(player, "&7   진행중인 퀘스트를 즉시 완료시킵니다.");
        }
        else if(arg.equals("강제포기")) {
            Msg.send(player, "&6/dqpdc <플레이어> 퀘스트 강제포기 <퀘스트 이름>");
            Msg.send(player, "&7   해당 플레이어의 진행중인 퀘스트를 강제로 포기시킵니다.");
        }
        else if(arg.equals("목표완료")) {
            Msg.send(player, "&6/dqpdc <플레이어> 퀘스트 목표완료 <목표 이름>");
            Msg.send(player, "&7   해당 플레이어가 진행중인 특정 목표를 완료시킵니다.");
        }
        Msg.send(player, " ");
        if (forone) Msg.send(player, "&e└────────────────────────┘");
    }

    private void usages(Player player) {
        Msg.send(player, "&e┌────────────────────────┐");
        Msg.send(player, " ");
        for(String s : arguments) {
            usage(player, s, false);
        }
        Msg.send(player, "&e└────────────────────────┘");
    }
}
