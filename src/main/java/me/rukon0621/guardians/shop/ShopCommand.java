package me.rukon0621.guardians.shop;

import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.rukon0621.guardians.main.pfix;

public class ShopCommand implements CommandExecutor {
    private static final main plugin = main.getPlugin();
    public static String[] arguments = {"생성", "삭제", "목록", "열기", "아이템추가", "아이템삭제"};

    public ShopCommand() {
        plugin.getCommand("shop").setExecutor(this);
        plugin.getCommand("shop").setTabCompleter(new ShopCommandsTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7상점 리로드중...");
            ShopManager.reloadAllShops();
            MsgUtil.cmdMsg(sender, "&a상점 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) return false;

        if(args.length==0) {
            usages(player);
            return true;
        }
        if(args[0].equals("생성")) {
            if(args.length<3) {
                usage(player,"생성", true);
                return true;
            }
            String type = args[1];
            if(type.equals("구매")) {
                ShopManager.createNewShop(player, ArgHelper.sumArg(args, 2), false);
            }
            else if (type.equals("판매")) {
                ShopManager.createNewShop(player, ArgHelper.sumArg(args, 2), true);
            }
            else {
                Msg.send(player, "&c상점 유형에는 판매와 구매 2가지가 존재합니다.", pfix);
            }

        }
        else if(args[0].equals("삭제")) {
            if(args.length<2) {
                usage(player,"삭제", true);
                return true;
            }
            ShopManager.deleteShop(player, ArgHelper.sumArg(args, 1));
        }
        else if(args[0].equals("목록")) {
            ShopManager.sendShopList(player);
        }
        else if (args[0].equals("아이템추가")) {
            if(player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                Msg.send(player, "&c손에 등록할 아이템을 들어주세요.", pfix);
                return true;
            }
            if(args.length<3) {
                usage(player, "아이템추가", true);
                return true;
            }
            long price = Long.parseLong(args[1]);
            String name = ArgHelper.sumArg(args, 2);
            ShopManager.addShopItem(player, name, player.getInventory().getItemInMainHand() ,price);
        }
        else if (args[0].equals("아이템삭제")) {
            if(args.length<3) {
                usage(player, "아이템삭제", true);
                return true;
            }
            int id = Integer.parseInt(args[1]);
            String name = ArgHelper.sumArg(args, 2);
            ShopManager.removeShopItem(player, name, id);
        }
        else if (args[0].equals("열기")) {
            if(args.length<2) {
                usage(player, "열기", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!ShopManager.openShop(player, name)) {
                Msg.send(player, "&c해당 상점은 존재하지 않는 이름의 상점입니다.", pfix);
            }
        }

        return true;
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        switch (arg) {
            case "생성" -> {
                Msg.send(player, "&6/상점 생성 <유형> <이름>");
                Msg.send(player, "&7    새로운 상점을 생성합니다.");
                Msg.send(player, "&7    이름에 띄어쓰기를 쓸 수 있습니다.");
                Msg.send(player, "&7    유형에는 구매와 판매 2가지가 존재합니다.");
                Msg.send(player, "&7    아이템 판매에서는 모든 아이템 데이터를 무시하고 이름만 확인합니다.");
            }
            case "삭제" -> {
                Msg.send(player, "&6/상점 삭제 <이름>");
                Msg.send(player, "&7    상점을 삭제합니다.");
            }
            case "목록" -> {
                Msg.send(player, "&6/상점 목록");
                Msg.send(player, "&7    서버에 존재하는 상점의 목록을 확인합니다.");
            }
            case "열기" -> {
                Msg.send(player, "&6/상점 열기 <이름>");
                Msg.send(player, "&7    상점을 열어봅니다.");
            }
            case "아이템추가" -> {
                Msg.send(player, "&6/상점 아이템추가 <가격> <상점이름>");
                Msg.send(player, "&7    해당 가격으로 손에 들고 있는 아이템을 상점에 추가합니다.");
                Msg.send(player, "&7    판매용 아이템이라면 해당 아이템의 레벨에 맞춰서 판매 조건이 설정됩니다.");
                Msg.send(player, "&7    ex) 10레벨 슬라임의 점액을 등록 -> 10레벨 이상의 슬라임 점액만 판매 가능.");
            }
            case "아이템삭제" -> {
                Msg.send(player, "&6/상점 아이템삭제 <ID> <상점이름>");
                Msg.send(player, "&7    해당 ID에 있는 상점의 아이템을 삭제합니다.");
                Msg.send(player, "&7    아이디는 0번부터 순차적으로 시작합니다.");
            }
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
