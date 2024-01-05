package me.rukon0621.guardians.account;

import me.rukon0621.dungeonwave.WaveData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.storage.StorageManager;
import me.rukon0621.pay.PaymentData;
import me.rukon0621.rpvp.data.RankData;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.main.getPlugin;
import static me.rukon0621.guardians.main.pfix;

public class AccountCommand implements CommandExecutor {
    public static String[] arguments = {"저장","삭제","로드", "보기"};
    private final AccountManager accountManager = getPlugin().getAccountManager();

    public AccountCommand() {
        main.getPlugin().getCommand("account").setExecutor(this);
        main.getPlugin().getCommand("account").setTabCompleter(new AccountCommandTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("리로드")) {
            MsgUtil.cmdMsg(sender, "&7계정 데이터 리로드중...");
            accountManager.reloadAccounts();
            MsgUtil.cmdMsg(sender, "&7계정 데이터 리로드 완료!");
            return true;
        }
        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }

        if(args[0].equals("저장")) {
            if(args.length<2) {
                usage(player, "저장", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            accountManager.saveAccount(player, name);
        }
        else if(args[0].equals("삭제")) {
            if(args.length<2) {
                usage(player, "삭제", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            accountManager.deleteAccount(player, name);
        }
        else if(args[0].equals("로드")) {
            if(args.length<2) {
                usage(player, "로드", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            accountManager.loadAccount(player, name);
        }
        else if(args[0].equals("보기")) {
            if(args.length<2) {
                usage(player, "보기", true);
                return true;
            }
            Msg.send(player, "계정을 불러오고 있습니다. 서버가 잠시 멈출 수 있습니다.", pfix);

            String uuid;
            if (args[1].contains("-")) uuid = args[1];
            else uuid = main.getPlugin().getServer().getOfflinePlayer(args[1]).getUniqueId().toString();

            CountDownLatch latch = new CountDownLatch(4);
            PlayerData.loadPlayerStatFromDatabase(player, latch, uuid);
            DialogQuestManager.loadPlayerDqData(player, latch, uuid);
            PaymentData.loadDataFromDataBase(player, latch, uuid);
            EquipmentManager.reloadEquipment(player, true);
            RankData.resetPvpData(player, latch);
            new WaveData(player, latch, uuid);
            StorageManager.copyStorageData(uuid, player.getUniqueId().toString());
            Msg.send(player, "계정을 불러왔습니다.", pfix);
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
        if(arg.equals("저장")) {
            Msg.send(player, "&6/계정 저장 <이름>");
            Msg.send(player, "&7   계정의 정보를 서버 파일로 저장합니다.");
        }
        else if(arg.equals("로드")) {
            Msg.send(player, "&6/계정 로드 <이름>");
            Msg.send(player, "&7   계정의 정보를 서버 파일에서 불러옵니다.");
            Msg.send(player, "&c   현재의 정보는 모두 초기화합니다.");
        }
        else if(arg.equals("보기")) {
            Msg.send(player, "&6/계정 보기 <플레이어>");
            Msg.send(player, "&7   해당 플레이어의 정보를 모두 불러옵니다.");
        }
        else if(arg.equals("삭제")) {
            Msg.send(player, "&6/계정 삭제 <이름>");
            Msg.send(player, "&7   서버에 저장된 계정 정보를 완전히 삭제합니다.");
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
