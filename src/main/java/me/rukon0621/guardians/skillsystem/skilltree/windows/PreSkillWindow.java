package me.rukon0621.guardians.skillsystem.skilltree.windows;

import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.gui.buttons.Icon;
import me.rukon0621.gui.buttons.NullIcon;
import me.rukon0621.gui.windows.Window;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class PreSkillWindow extends Window {
    public PreSkillWindow(Player player, String title) {
        super(player, "&f\uF000" + title, 6);
        setDefaultOfSkillTree(map);

        if(title.equals("\uF003")) {
            SkillWindowOpenButton button = new SkillWindowOpenButton("검", 0);
            map.put(18, button);
            map.put(19, button);
            map.put(27, button);
            map.put(28, button);
            button = new SkillWindowOpenButton("창", 1);
            map.put(21, button);
            map.put(22, button);
            map.put(30, button);
            map.put(31, button);
            button = new SkillWindowOpenButton("둔기", 2);
            map.put(24, button);
            map.put(25, button);
            map.put(33, button);
            map.put(34, button);
        }
        if(title.equals("\uF005")) {
            SkillWindowOpenButton button = new SkillWindowOpenButton("활", 3);
            map.put(18, button);
            map.put(19, button);
            map.put(27, button);
            map.put(28, button);
            button = new SkillWindowOpenButton("투척", 4);
            map.put(21, button);
            map.put(22, button);
            map.put(30, button);
            map.put(31, button);
            button = new SkillWindowOpenButton("폭탄", 5);
            map.put(24, button);
            map.put(25, button);
            map.put(33, button);
            map.put(34, button);
        }
        reloadGUI();
        open();
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) {
            new MenuWindow(player, 1);
        }
    }

    static class SkillWindowOpenButton extends Button {

        private final int treeIndex;
        private final String weaponType;
        public SkillWindowOpenButton(String weaponType, int treeIndex) {
            this.treeIndex = treeIndex;
            this.weaponType = weaponType;
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1 ,1);
            new SkillTreeWindow(player, treeIndex);
        }

        @Override
        public ItemStack getIcon() {
            ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&8『 &c" + weaponType + " &8』");
            item.setCustomModelData(7);

            TypeData typeData = TypeData.getType(weaponType);
            if(typeData.getChild().size()>0) {
                item.addLore("&f" + typeData.getName() + " 타입의 하위 타입 무기");
                for(String s : typeData.getChild()) {
                    item.addLore("&7 - " + s);
                }
            }
            return item.getItem();
        }
    }

    public static void setDefaultOfSkillTree(HashMap<Integer, Icon> map) {
        Button button = new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1 ,1);
                new PreSkillWindow(player, "\uF003");
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&8『 &c근접 전술 &8』");
                item.setCustomModelData(7);
                item.addLore("&f근접 무기(검, 창, 둔기)에 관한 스킬 트리를 확인합니다.");
                return item.getItem();
            }
        };
        map.put(0, button);
        map.put(1, button);
        button = new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1 ,1);
                new PreSkillWindow(player, "\uF005");
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&8『 &c원거리 전술 &8』");
                item.setCustomModelData(7);
                item.addLore("&f원거리 무기(활, 투척, 폭탄)에 관한 스킬 트리를 확인합니다.");
                return item.getItem();
            }
        };
        map.put(2, button);
        map.put(3, button);
        button = new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1 ,1);
                new SkillTreeWindow(player, 6);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&8『 &c체술 &8』");
                item.setCustomModelData(7);
                item.addLore("&f신체 능력(기본 스텟)에 관련된 스킬 트리를 확인합니다.");
                return item.getItem();
            }
        };
        map.put(4, button);
        map.put(5, button);
        button = new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1 ,1);
                new SkillTreeWindow(player, 7);
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&8『 &c제작 &8』");
                item.setCustomModelData(7);
                item.addLore("&f장비 제작 능력에 관련된 스킬 트리를 확인합니다.");
                return item.getItem();
            }
        };
        map.put(6, button);
        map.put(7, button);
        map.put(8, new NullIcon());
    }
}
