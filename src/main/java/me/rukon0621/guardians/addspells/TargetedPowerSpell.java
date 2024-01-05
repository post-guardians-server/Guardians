package me.rukon0621.guardians.addspells;

import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.skillsystem.skilltree.SkillTreeManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TargetedPowerSpell extends TargetedSpell {

    protected final Map<String, Double> powerMap = new HashMap<>();
    protected final List<String> exemptList;

    public TargetedPowerSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        exemptList = getConfigStringList("exemptList", new ArrayList<>());
        ConfigurationSection section = getConfigSection("powerMap");
        if(section!=null) {
            SkillTreeManager treeManager = main.getPlugin().getSkillTreeManager();
            if(treeManager==null) return;
            for(String s : section.getKeys(false)) {
                double value = section.getDouble(s);
                for(String skill : treeManager.getSkillNames()) {
                    if(!skill.startsWith(s)) continue;
                    if(exemptList.contains(skill)) continue;
                    powerMap.put(skill, value);
                }
            }
        }
    }

    protected float modifyPower(LivingEntity caster, float power) {
        if(powerMap.isEmpty()) return 1;
        if(!(caster instanceof Player player)) return 1;
        PlayerData pdc = new PlayerData(player);
        for(String skill : powerMap.keySet()) {
            if(!pdc.hasSkill(skill)) continue;
            power += powerMap.get(skill);
        }
        return power;
    }

}
