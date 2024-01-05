package me.rukon0621.guardians.craft;

import me.rukon0621.guardians.craft.recipes.RecipeManager;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.pfix;

public class RecipeCommand implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"생성", "삭제", "목록", "리로드"};

    public RecipeCommand() {
        plugin.getCommand("recipe").setExecutor(this);
        plugin.getCommand("recipe").setTabCompleter(new RecipeCommandTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7레시피 리로드중...");
            RecipeManager.reloadRecipes();
            MsgUtil.cmdMsg(sender, "&a레시피 리로드 완료!");
            return true;
        }

        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }
        if(args[0].equals("생성")) {
            if(args.length<2) {
                usage(player, "생성", true);
                return true;
            }
            String fileData = ArgHelper.sumArg(args, 1);

            if(RecipeManager.createNewRecipes(fileData)) {
                Msg.send(player, "성공적으로 레시피를 생성했습니다.", pfix);
                RecipeManager.reloadRecipes();
                return true;
            } else {
                Msg.send(player, "&c해당 레시피는 이미 존재하는 레시피입니다.", pfix);
                return true;
            }
        }
        else if(args[0].equals("삭제")) {
            if(args.length<2) {
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(RecipeManager.deleteRecipes(name)) {
                Msg.send(player, "&6성공적으로 레시피를 삭제했습니다.", pfix);
                RecipeManager.reloadRecipes();
                return true;
            } else {
                Msg.send(player, "&c해당 레시피는 존재하지 않는 레시피입니다.", pfix);
                return true;
            }
        }
        else if(args[0].equals("목록")) {
            Msg.send(player, "서버에 존재하는 레시피 목록입니다.", pfix);
            Configure config = RecipeManager.getPathData();
            for(String key : config.getConfig().getKeys(false)) {
                Msg.send(player, key + "&7 : &e" + config.getConfig().getString(key));
            }
            return true;
        }
        else {
            usages(player);
        }
        return true;
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equalsIgnoreCase("리로드")) {
            Msg.send(player, "&6/레시피 리로드");
            Msg.send(player, "&7    서버에 존재하는 모든 레시피를 리로드합니다.");
        }
        else if(arg.equalsIgnoreCase("생성")) {
            Msg.send(player, "&6/레시피 생성 <파일 데이터>");
            Msg.send(player, "&7    서버에 새로운 레시피를 생성합니다.");
            Msg.send(player, "&7    파일데이터: 경로+이름 | 확장자는 적지 마십시오.");
        }
        else if(arg.equalsIgnoreCase("삭제")) {
            Msg.send(player, "&6/레시피 삭제 <레시피 이름>");
            Msg.send(player, "&7    기존에 존재하는 레시피를 삭제합니다.");
        }
        else if(arg.equalsIgnoreCase("목록")) {
            Msg.send(player, "&6/레시피 목록");
            Msg.send(player, "&7    서버에 존재하는 레시피와 그 경로를 살펴봅니다.");
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
