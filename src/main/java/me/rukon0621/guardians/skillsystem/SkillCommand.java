package me.rukon0621.guardians.skillsystem;

import me.rukon0621.guardians.commands.AbstractCommand;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.EnumUtils;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.TabCompleteUtils;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.rukon0621.guardians.main.pfix;

public class SkillCommand extends AbstractCommand {

    private List<String> enums = new ArrayList<>();

    public SkillCommand() {
        super("skill", main.getPlugin());
        arguments.add("생성");
        arguments.add("삭제");
        arguments.add("목록");
        arguments.add("리로드");

        enums = EnumUtils.getEnumStringList(SkillType.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7스킬 데이터 리로드중...");
            SkillManager.reloadAllSkills();
            MsgUtil.cmdMsg(sender, "&a스킬 데이터 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) return false;

        if(args.length==0) {
            usages(player);
            return true;
        }

        if(args[0].equals("생성")) {
            if(args.length<4) {
                usage(player, args[0], true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 3);
            String path = args[1];
            if(!path.endsWith(".yml")) path += ".yml";

            try {
                SkillType type = SkillType.valueOf(args[2]);
                SkillManager.createNewSkill(player, name, path, type);
            } catch (IllegalArgumentException e) {
                Msg.warn(player, "올바른 스킬 타입을 적어주세요.");
            }

        }
        else if(args[0].equals("삭제")) {
            if(args.length<2) {
                usage(player, args[0], true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            SkillManager.deleteSkill(player, name);
        }
        else if(args[0].equals("목록")) {
            SkillManager.showSkillList(player);
        }
        else if(args[0].equals("리로드")) {
            if(args.length < 2) {
                SkillManager.reloadAllSkills();
                Msg.send(player, "모든 스킬 데이터를 성공적으로 새로 고침 했습니다.", pfix);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            SkillManager.reloadSkill(name);
        }
        else {
            usages(player);
        }
        return true;
    }

    @Override
    protected void usage(Player player, String usage) {
        switch (usage) {
            case "생성" -> {
                Msg.send(player, "&6/스킬 생성 <경로> <타입> <이름>");
                Msg.send(player, "&7   새로운 스킬의 기본 포맷을 생성합니다.");
                Msg.send(player, "&c   경로에는 색깔 코드를 사용할 수 없습니다. (이름은 가능)");
            }
            case "삭제" -> {
                Msg.send(player, "&6/스킬 삭제 <이름>");
                Msg.send(player, "&7   존재하는 스킬을 삭제합니다. 기존에 장착된 스킬은 사라집니다.");
            }
            case "목록" -> {
                Msg.send(player, "&6/스킬 목록");
                Msg.send(player, "&7   존재하는 스킬 목록을 확인합니다.");
            }
            case "리로드" -> {
                Msg.send(player, "&6/스킬 리로드 [<이름>]");
                Msg.send(player, "&7   서버의 장착 스킬을 리로드합니다.");
                Msg.send(player, "&7   <이름>을 입력하지 않으면 모든 스킬을 삭제합니다.");
            }
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length==0) return new ArrayList<>();
        if(args.length==1) return TabCompleteUtils.searchAtList(arguments, args[0]);
        if(args[0].equals("생성")) {
            if(args.length==2) return TabCompleteUtils.searchAtSet(SkillManager.getPaths(), args[1]);
            if(args.length==3) return TabCompleteUtils.searchAtList(enums, args[2]);
            return TabCompleteUtils.searchAtList(arguments, args[0]);
        }
        if(args[0].equals("삭제")||args[0].equals("리로드")) {
            if(args.length==2) return TabCompleteUtils.searchAtSet(SkillManager.getSkillData().keySet(), args[1]);
        }

        return new ArrayList<>();
    }
}
