package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.craft.craft.CraftManager;
import me.rukon0621.guardians.craft.recipes.RecipeManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.dropItem.DropManager;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.region.RegionManager;
import me.rukon0621.guardians.shop.ShopManager;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.guardians.story.StoryManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.main.pfix;

public class ReloadCommand implements CommandExecutor {
    public static String[] arguments = {"basic"};

    public ReloadCommand() {
        main.getPlugin().getCommand("reloadall").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;

        Msg.send(player, "&6서버에 존재하는 모든 외부 파일을 리로드합니다.", pfix);

        new BukkitRunnable() {
            @Override
            public void run() {
                CountDownLatch latch = new CountDownLatch(3);
                DialogQuestManager.reloadAll(latch);
                StoryManager.reloadStory(latch);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Msg.send(player, "&7 - 대화문 퀘스트 스토리 정보를 리로드하였습니다.");
            }
        }.runTaskAsynchronously(main.getPlugin());
        RegionManager.reloadRegionData();
        SkillManager.reloadAllSkills();
        main.getPlugin().getSkillTreeManager().reload();
        CraftManager.reloadCraftData();
        RecipeManager.reloadRecipes();
        DropManager.reloadAllDropData();
        AreaManger.reloadAreaData();
        ItemData.reloadItemData();
        ItemSaver.reloadItemSaver();
        ShopManager.reloadAllShops();
        Msg.send(player, "&7 - 룬 스킬 / 장착 스킬 데이터를 리로드하였습니다.");
        Msg.send(player, "&7 - 8개의 스킬트리 데이터를 리로드하였습니다.");
        Msg.send(player, "&7 - 지역, 에리어 데이터를 리로드하였습니다.");
        Msg.send(player, "&7 - 드롭 데이터를 리로드하였습니다.");
        Msg.send(player, "&7 - 제작대, 레시피 데이터를 리로드하였습니다.");
        Msg.send(player, "&7 - 아이템 데이터 부가 속성, 타입 데이터를 리로드했습니다.");
        Msg.send(player, "&7 - 아이템 세이버를 리로드했습니다.");
        Msg.send(player, "&7 - 모든 필드 던전 웨이브를 리로드했습니다.");
        Msg.send(player, "&7 - 모든 테세이온을 리로드했습니다.");
        return true;
    }
}
