package me.rukon0621.guardians.skillsystem;

import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.Msg;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;

public enum SkillEquipSlot {

    RIGHT {
        @org.jetbrains.annotations.Nullable
        @Override
        public Skill getSkill(Player player) {
            String s =  SkillManager.getEquipSkill(player).get("Right");
            if (s == null) return null;
            return SkillManager.getSkill(s);
        }
    },
    SHIFT_LEFT {
        @org.jetbrains.annotations.Nullable
        @Override
        public Skill getSkill(Player player) {
            String s =  SkillManager.getEquipSkill(player).get("ShiftLeft");
            if (s == null) return null;
            return SkillManager.getSkill(s);
        }
    },
    SHIFT_RIGHT {
        @org.jetbrains.annotations.Nullable
        @Override
        public Skill getSkill(Player player) {
            String s =  SkillManager.getEquipSkill(player).get("ShiftRight");
            if (s == null) return null;
            return SkillManager.getSkill(s);
        }
    },
    DOUBLE_SHIFT {
        @org.jetbrains.annotations.Nullable
        @Override
        public Skill getSkill(Player player) {
            String s =  SkillManager.getEquipSkill(player).get("DoubleShift");
            if (s == null) return null;
            return SkillManager.getSkill(s);
        }
    },
    DROP {
        @org.jetbrains.annotations.Nullable
        @Override
        public Skill getSkill(Player player) {
            String s =  SkillManager.getEquipSkill(player).get("DropKey");
            if (s == null) return null;
            return SkillManager.getSkill(s);
        }
    },
    RUNE1 {
        @org.jetbrains.annotations.Nullable
        @Override
        public Skill getSkill(Player player) {
            HashMap<String, ItemStack> equipData = EquipmentManager.getEquipmentData(player);
            ItemStack rune = equipData.getOrDefault("룬1", new ItemStack(Material.AIR));
            if(!rune.getType().equals(Material.AIR)&&!rune.getType().equals(Material.BARRIER)) {
                if(!rune.hasItemMeta()) return null;
                if(!rune.getItemMeta().hasDisplayName()) return null;
                return SkillManager.getSkill(Msg.uncolor(rune.getItemMeta().getDisplayName()).split(":")[1].trim());
            }
            else return null;
        }
    },
    RUNE2 {
        @org.jetbrains.annotations.Nullable
        @Override
        public Skill getSkill(Player player) {
            HashMap<String, ItemStack> equipData = EquipmentManager.getEquipmentData(player);
            ItemStack rune = equipData.getOrDefault("룬2", new ItemStack(Material.AIR));
            if(!rune.getType().equals(Material.AIR)&&!rune.getType().equals(Material.BARRIER)) {
                if(!rune.hasItemMeta()) return null;
                if(!rune.getItemMeta().hasDisplayName()) return null;
                return SkillManager.getSkill(Msg.uncolor(rune.getItemMeta().getDisplayName()).split(":")[1].trim());
            }
            else return null;
        }
    },
    RUNE3 {
        @org.jetbrains.annotations.Nullable
        @Override
        public Skill getSkill(Player player) {
            HashMap<String, ItemStack> equipData = EquipmentManager.getEquipmentData(player);
            ItemStack rune = equipData.getOrDefault("룬3", new ItemStack(Material.AIR));
            if(!rune.getType().equals(Material.AIR)&&!rune.getType().equals(Material.BARRIER)) {
                if(!rune.hasItemMeta()) return null;
                if(!rune.getItemMeta().hasDisplayName()) return null;
                return SkillManager.getSkill(Msg.uncolor(rune.getItemMeta().getDisplayName()).split(":")[1].trim());
            }
            else return null;
        }
    };

    @Nullable
    public abstract Skill getSkill(Player player);

}
