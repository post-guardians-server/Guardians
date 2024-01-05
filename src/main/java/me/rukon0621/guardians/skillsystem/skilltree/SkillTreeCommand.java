package me.rukon0621.guardians.skillsystem.skilltree;

import me.rukon0621.guardians.commands.AbstractCommand;
import me.rukon0621.guardians.data.PlayerData;
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

public class SkillTreeCommand extends AbstractCommand {
    private final SkillTreeManager manager = main.getPlugin().getSkillTreeManager();
    public SkillTreeCommand() {
        super("skilltree", main.getPlugin());
        arguments.add("리로드");
        arguments.add("취소");
    }

    @Override
    protected void usage(Player player, String usage) {
        if(usage.equals("리로드")) {
            Msg.send(player, "&6/스킬트리 리로드");
        }
        else if (usage.equals("취소")) {
            Msg.send(player, "&6/스킬트리 취소 <플레이어> <값>");
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7스킬트리 리로드중...");
            main.getPlugin().getSkillTreeManager().reload();
            MsgUtil.cmdMsg(sender, "&a스킬트리 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) return false;

        if(args.length==0) {
            usages(player);
            return true;
        }
        else if(args[0].equals("리로드")) {
            manager.reload(player);
            Msg.send(player, "스킬 트리를 리로드했습니다.", pfix);
        }
        else if(args[0].equals("취소")) {
            if(args.length!=3) {
                usage(player, args[0]);
                return true;
            }
            Player target = main.getPlugin().getServer().getPlayer(args[1]);
            if(target==null) {
                Msg.warn(player, "해당 플레이어는 존재하지 않습니다.");
                return true;
            }
            try {
                int point = Integer.parseInt(args[2]);
                if(point<0) {
                    Msg.warn(player, "음수를 입력할 수 없습니다.");
                    return true;
                }
                PlayerData pdc = new PlayerData(target);
                pdc.setUnlearnChance(point);
                Msg.send(player, "성공적으로 설정했습니다.", pfix);
            } catch (NumberFormatException e) {
                Msg.warn(player, "올바른 숫자를 적어주세요.");
            }



        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length==1) return TabCompleteUtils.search(arguments, args[0]);
        if(!args[0].equals("리로드")) return null;



        return new ArrayList<>();
    }
}
