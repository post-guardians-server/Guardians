package me.rukon0621.guardians.skillsystem.skilltree.windows;

import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.guardians.skillsystem.skilltree.SkillTreeManager;
import me.rukon0621.guardians.skillsystem.skilltree.elements.SkillLine;
import me.rukon0621.guardians.skillsystem.skilltree.elements.SkillTree;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.buttons.Icon;
import me.rukon0621.gui.windows.ScrollableWindow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static me.rukon0621.guardians.main.getPlugin;
import static me.rukon0621.guardians.main.pfix;

public class SkillTreeWindow extends ScrollableWindow {
    private static final SkillTreeManager manager = main.getPlugin().getSkillTreeManager();

    public SkillTreeWindow(Player player, int treeIndex) {
        super(player, "&f\uF000" + manager.unicodeOfTree(treeIndex), 6, Math.max(manager.getSkillData(treeIndex).getX() - 7, 0), Math.max(manager.getSkillData(treeIndex).getY() - 3, 0));
        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                SkillWindowData data = manager.getSkillData(treeIndex);
                PreSkillWindow.setDefaultOfSkillTree(map);
                PlayerData pdc = new PlayerData(player);
                x = manager.getSkillData(treeIndex).getDefaultLocation().getFirst();
                y = manager.getSkillData(treeIndex).getDefaultLocation().getSecond();

                for(SkillLine line : data.getLines()) {
                    icons[line.y + 1][line.x] = new Icon() {
                        @Override
                        public ItemStack getIcon() {
                            return line.getIcon();
                        }
                    };
                }


                int hx = -1;
                int hy = -1;
                for(SkillTree tree : data.getSkills()) {
                    try {
                        icons[tree.y + 1][tree.x] = new SkillTreeButton(tree);
                        if(pdc.hasSkill(tree.getName())) {
                            if(hx < tree.x || hy < tree.y) {
                                hx = tree.x;
                                hy = tree.y;
                                x = tree.x - 4;
                                y = tree.y - 3;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Bukkit.getLogger().warning(tree.getName() + " : 로드를 실패했습니다.");
                    }
                }
                if (x < 0) x = 0;
                if (y < 0) y = 0;
                if (x > maxX) x = maxX;
                if (y > maxY) y = maxY;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        reloadGUI();
                        open();
                        ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&f");
                        for(int i = 0; i < 4; i++) {
                            item.setCustomModelData(88 + i);
                            player.getInventory().setItem(i, item.getItem());
                        }
                    }
                }.runTaskLater(getPlugin(), 1);

            }
        }.runTaskAsynchronously(main.getPlugin());

    }

    @Override
    public void close(boolean b) {
        disable();
        if(!player.isOnline()) return;
        EquipmentManager.reloadEquipment(player, true);
        if(b) {
            new MenuWindow(player, 1);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                SkillManager.reloadPlayerSkill(player);
            }
        }.runTaskAsynchronously(getPlugin());
    }

    @Override
    public boolean additionalClick(InventoryClickEvent e) {
        if(e.getClick().equals(ClickType.DOUBLE_CLICK)) return false;

        int move = 1;
        if(e.getClick().equals(ClickType.SHIFT_LEFT)) move = 3;

        if(e.getRawSlot()==81) y -= move;
        else if(e.getRawSlot()==82) y += move;
        else if(e.getRawSlot()==83) x -= move;
        else if(e.getRawSlot()==84) x += move;
        else if(e.getHotbarButton()==0) y -= move;
        else if(e.getHotbarButton()==1) y += move;
        else if(e.getHotbarButton()==2) x -= move;
        else if(e.getHotbarButton()==3) x += move;
        else return false;
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
        reloadGUI();
        return true;
    }

    class SkillTreeButton extends Button {
        private final SkillTree tree;
        public SkillTreeButton(SkillTree tree) {
            this.tree = tree;
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            PlayerData pdc = new PlayerData(player);
            if(pdc.hasSkill(tree.getName())) {
                if(clickType.equals(ClickType.SHIFT_RIGHT)) {
                    for(String st : tree.getChild()) {
                        if (pdc.hasSkill(st)) {
                            Msg.warn(player, "먼저 상위 스킬 트리를 취소해주세요. &7 - " + st);
                            return;
                        }
                    }
                    if(pdc.getUnlearnChance() > 0) {
                        pdc.setUnlearnChance(pdc.getUnlearnChance() - 1);
                        pdc.getSkillData().remove(tree.getName());
                        if(tree.getTreeIndex() == 7) pdc.setCraftSkillPoint(pdc.getCraftSkillPoint() + tree.getPoint());
                        else pdc.setSkillPoint(pdc.getSkillPoint() + tree.getPoint());
                        Msg.send(player, "배운 스킬을 취소시켰습니다.", pfix);
                        player.playSound(player, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1, 1);
                    }
                    else {
                        Msg.warn(player, "오늘 하루 지급된 모든 스킬 취소 횟수를 소진했습니다.");
                    }
                }
            }
            else {
                if(pdc.getLevel() < tree.getLevel()) {
                    Msg.warn(player, "이 스킬을 습득하기 위한 레벨 조건을 달성하지 못했습니다.");
                    return;
                }


                if(tree.isUsingOrCondition()) {
                    boolean pass = false;
                    for(String s : tree.getRequiredSkills()) {
                        if(pdc.hasSkill(s)) {
                            pass = true;
                            break;
                        }
                    }
                    if(!pass) {
                        Msg.warn(player, "이 스킬을 습득하기 위해서는 먼저 이 스킬의 상위 스킬 중 1개를 습득해야 합니다.");
                        return;
                    }
                }
                else {
                    for(String s : tree.getRequiredSkills()) {
                        if(pdc.hasSkill(s)) {
                            continue;
                        }
                        Msg.warn(player, "이 스킬을 습득하기 위해서는 먼저 이 스킬의 상위 스킬을 모두 습득해야 합니다.");
                        return;
                    }
                }

                if(pdc.getSkillPoint(tree.getTreeIndex()) < tree.getPoint()) {
                    Msg.warn(player, "스킬 포인트가 부족합니다.");
                    return;
                }
                for(String skillName : tree.getBannedSkills()) {
                    if(pdc.hasSkill(skillName)) {
                        Msg.warn(player, "이 스킬을 해금하려면 다음 스킬을 취소해야합니다. &e -> " + skillName);
                        return;
                    }
                }

                if(manager.isBasicSkill(tree.getName())) {
                    for(String s : manager.getBasicSkillNames()) {
                        if(pdc.hasSkill(s)) {
                            Msg.warn(player, "동시에 두 무기의 스킬 트리를 찍을 수 없습니다.");
                            return;
                        }
                    }

                    if(!StoryManager.getReadStory(player).contains("스킬 장착")) {
                        StoryManager.readStory(player, "스킬 장착");
                        player.closeInventory();
                    }
                }

                if(tree.getTreeIndex() == 7) pdc.setCraftSkillPoint(pdc.getCraftSkillPoint() - tree.getPoint());
                else pdc.setSkillPoint(pdc.getSkillPoint() - tree.getPoint());
                pdc.addSkill(tree.getName());
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
                Msg.send(player, "성공적으로 스킬을 습득했습니다.", pfix);

            }
            
        }

        @Override
        public ItemStack getIcon() {
            PlayerData pdc = new PlayerData(player);
            if(pdc.hasSkill(tree.getName())) {
                ItemClass item = new ItemClass(new ItemStack(Material.MAGMA_CREAM), "&a" + tree.getName() + " &8| &a(해금됨)");
                for(String s : tree.getLores()) {
                    item.addLore(s);
                }
                item.addLore(" ");
                item.addLore("&c\uE011\uE00C\uE00C이 스킬의 습득을 취소하려면 &4쉬프트 우클릭&c 하십시오.");
                item.addLore("&6\uE011\uE00C\uE00C남은 스킬 취소 포인트: " + pdc.getUnlearnChance());
                if(tree.getTreeIndex() == 7) item.addLore("&6\uE011\uE00C\uE00C취소시 얻는 제작 스킬 포인트: " + tree.getPoint());
                else item.addLore("&6\uE011\uE00C\uE00C취소시 얻는 스킬 포인트: " + tree.getPoint());
                item.setCustomModelData(tree.getCmd());
                return item.getItem();
            }
            else {
                ItemClass item = new ItemClass(new ItemStack(Material.SLIME_BALL), "&c" + tree.getName() + " &8| &7(미해금)");
                for(String s : tree.getLores()) {
                    item.addLore(s);
                }
                item.addLore(" ");


                if(tree.getTreeIndex() == 7) {
                    if(pdc.getCraftSkillPoint() >= tree.getPoint()) item.addLore("&6\uE011\uE00C\uE00C이 스킬을 습득하려면 &a" + tree.getPoint() + "&6만큼의 스킬 포인트가 필요합니다.");
                    else item.addLore("&6\uE011\uE00C\uE00C이 스킬을 습득하려면 &c" + tree.getPoint() + "&6만큼의 스킬 포인트가 필요합니다.");
                }
                else {
                    if(pdc.getSkillPoint() >= tree.getPoint()) item.addLore("&6\uE011\uE00C\uE00C이 스킬을 습득하려면 &a" + tree.getPoint() + "&6만큼의 스킬 포인트가 필요합니다.");
                    else item.addLore("&6\uE011\uE00C\uE00C이 스킬을 습득하려면 &c" + tree.getPoint() + "&6만큼의 스킬 포인트가 필요합니다.");
                }

                if(pdc.getLevel() >= tree.getLevel()) item.addLore("&6\uE011\uE00C\uE00C이 스킬을 습득하려면 &a" + tree.getLevel() + "&6레벨을 달성해야 합니다.");
                else item.addLore("&6\uE011\uE00C\uE00C이 스킬을 습득하려면 &c" + tree.getLevel() + "&6레벨을 달성해야 합니다.");

                if(!tree.getBannedSkills().isEmpty()) {
                    item.addLore(" ");
                    item.addLore("#f8a59f\uE014\uE00C\uE00C이 스킬을 해금하면 아래의 스킬을 해금할 수 없습니다.");
                    for(String skillName : tree.getBannedSkills()) {
                        item.addLore("&7- " + skillName);
                    }

                }

                item.addLore(" ");
                if(tree.getTreeIndex() == 7) item.addLore("&f\uE011\uE00C\uE00C&b가진 제작 스킬 포인트: &9" + pdc.getCraftSkillPoint());
                else item.addLore("&f\uE011\uE00C\uE00C&b가진 스킬 포인트: &9" + pdc.getSkillPoint());

                if(!tree.getRequiredSkills().isEmpty()) {
                    item.addLore(" ");
                    if(tree.isUsingOrCondition()) item.addLore("&f\uE011\uE00C\uE00C이 스킬을 해금하려면 먼저 &e아래 스킬 중 1개&f를 해금해야합니다.");
                    else item.addLore("&f\uE011\uE00C\uE00C이 스킬을 해금하려면 먼저 &e아래 스킬을 모두&f 해금해야합니다.");
                    for(String s : tree.getRequiredSkills()) {
                        item.addLore("&7 - " + s);
                    }
                }
                item.setCustomModelData(tree.getCmd());
                return item.getItem();
            }
        }
    }

}
