package me.rukon0621.guardians.skillsystem;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.Configure;
import me.rukon0621.guardians.helper.Couple;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.region.Region;
import me.rukon0621.guardians.region.RegionManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Skill {

    private final String skillName; //스킬 이름
    private final String magicSpellName; //매직스펠 연동 이름
    private ItemClass icon;
    private final double powerPerLevel; //레벨마다 파워가 몇 % 증가하는가 (0~100 백분율)
    private final double cooldownPerLevel; //레벨마다 쿨타임이 몇 % 줄어드는가 (0~100 백분율)
    private final ArrayList<String> requiredSkills; //스킬트리에서 이 스킬들이 있으면 1레벨씩 레벨업
    private final boolean sendCooldownMessage;
    private final boolean isRuneSkill;
    private final String  shiftSpell;
    private final String requiredEquipmentType;
    private final List<String> bannedSkills;
    private final String unicode;
    private final String shiftSkillUnicode;
    private final List<Couple<String, String>> changeMap = new ArrayList<>();

    public Skill(String skillName, Configure config) {
        this.skillName = skillName;
        magicSpellName = config.getConfig().getString(skillName+".magicSpellName", "test");
        powerPerLevel = config.getConfig().getDouble(skillName+".powerPerLevel",0.0);
        cooldownPerLevel = config.getConfig().getDouble(skillName+".cooldownPerLevel",0.0);
        requiredEquipmentType = config.getConfig().getString(skillName+".requiredEquipmentType", "null");
        isRuneSkill = config.getConfig().getBoolean(skillName+".isRuneSkill", false);
        shiftSpell = config.getConfig().getString(skillName+".magicSpellNameWithShift", "null");
        unicode = config.getConfig().getString(skillName+".unicode", "\\uF000");
        shiftSkillUnicode = config.getConfig().getString(skillName+".shiftUnicode", "\\uF000");
        icon = new ItemClass(new ItemStack(Material.GHAST_TEAR), skillName);
        icon.setCustomModelData(config.getConfig().getInt(skillName+".customModelData", 0));
        ArrayList<String> lores = (ArrayList<String>) config.getConfig().getList(skillName+".lores", new ArrayList<>());
        for(String lore : lores) {
            icon.addLore(lore);
        }
        requiredSkills = (ArrayList<String>) config.getConfig().getList(skillName+".requiredSkills", new ArrayList<>());
        sendCooldownMessage = config.getConfig().getBoolean(skillName+".sendCooltimeMessage", true);
        bannedSkills = config.getConfig().getStringList(skillName+".bannedSkills");
        for(String s : config.getConfig().getStringList(skillName+".changeMap")) {
            changeMap.add(new Couple<>(s.split(":")[0].trim(), s.split(":")[1].trim()));
        }
    }

    /**
     * 플레이어가 requiredSkills에 있는 스킬을 몇 개 가지고 있는지에 따라 레벨이 결정됨 (0은 미보유로 취급)
     *
     * @param player 플레이어
     * @return 플레이어의 스킬 레벨을 반환
     */
    public int getLevel(Player player) {
        int level = 0;
        PlayerData pdc = new PlayerData(player);
        for(String requiredSkill : requiredSkills) {
            if(pdc.hasSkill(requiredSkill)) level++;
        }
        return level;
    }

    /**
     * 이 스킬을 열면 해당 스킬을 사용할 수 없게됨
     * @return bannedSkill
     */
    public List<String> getBannedSkills() {
        return bannedSkills;
    }

    /**
     * 해당 플레이어의 스텟등이 반영된 스킬 아이콘을 반환
     * @param player 플레이어
     * @return 해당 플레이어의 스텟등이 반영된 스킬 아이콘을 반환
     */
    public ItemStack getIcon(Player player) {
        ItemClass item = new ItemClass(new ItemStack(icon.getItem()));
        int level = getLevel(player);
        item.addLore(" ");
        item.addLore("&7스킬 레벨: &f"+level);
        if(!requiredEquipmentType.equals("null")) {
            item.addLore("&7요구 무기 타입: &f"+requiredEquipmentType+" (또는 이 타입의 하위 타입)");
        }
        return item.getItem();
    }

    /**
     * 플레이어의 스킬 레벨을 반영하여 스킬을 캐스팅
     * @param player 플레이어
     */
    public void cast(Player player) {
        cast(player, getLevel(player));
    }

    /**
     * 특정 레벨로 스킬을 캐스팅 (0과 1은 동일함 [1레벨에서는 증폭이 없음] )
     * @param player 플레이어
     * @param level 몇 레벨로 스킬을 캐스팅할지 (0과 1은 동일함 [1레벨에서는 증폭이 없음] )
     */
    public boolean cast(Player player, int level) {
        return cast(player, level, null);
    }

    /**
     * 특정 레벨로 스킬을 캐스팅 (0과 1은 동일함 [1레벨에서는 증폭이 없음] )
     * @param player 플레이어
     * @param level 몇 레벨로 스킬을 캐스팅할지 (0과 1은 동일함 [1레벨에서는 증폭이 없음] )
     * @param runeData 룬의 아이템 데이터
     */
    public boolean cast(Player player, int level, @Nullable ItemData runeData) {
        try {
            for(Region region : RegionManager.getRegionsOfPlayer(player)) {
                if(region.getSpecialOptions().contains("blockSkill")) {
                    Msg.warn(player, "이곳에서는 스킬을 사용할 수 없습니다.");
                    return false;
                }
            }
            if(EquipmentManager.getEquipment(player, "무기").getType().equals(Material.AIR)) return false;
            if(!requiredEquipmentType.equals("null")) {
                String weaponType = EquipmentManager.getEquipmentItemData(player, "무기").getType();
                if(!TypeData.getType(weaponType).isMaterialOf(requiredEquipmentType)) {
                    Msg.warn(player,  "이 스킬을 사용하려면 장착된 무기의 타입이 " + requiredEquipmentType + "이거나 이 타입의 하위 타입이여야 합니다.");
                    return false;
                }
            }

            //파워 증폭은 2레벨부터 시작되기에 1레벨도 0으로 만들고 0과 1은 같게 처리
            Spell spell = null;
            if(!shiftSpell.equals("null")&&player.isSneaking()) {
                spell = MagicSpells.getSpellByInGameName(shiftSpell);
            }
            else {
                if(!changeMap.isEmpty()) {
                    PlayerData pdc = new PlayerData(player);
                    for(Couple<String, String> data : changeMap) {
                        if(pdc.hasSkill(data.getFirst())) {
                            spell = MagicSpells.getSpellByInGameName(data.getSecond());
                            break;
                        }
                    }
                }
                if(spell==null) spell = MagicSpells.getSpellByInGameName(magicSpellName);

            }
            if(spell==null) {
                throw new NullPointerException(skillName + "존재하지 않는 스펠을 사용하려고 합니다.");
            }

            if(spell.onCooldown(player)) {
                if(sendCooldownMessage) Msg.send(player, String.format("아직 이 스킬의 쿨타임은 &c%.1f&f초 남았습니다.", spell.getCooldown(player)), "&7[ "+ skillName +" &7] &f");
                return false;
            }
            level--;
            if(level<0) level = 0;
            float power = 1 + (float) (powerPerLevel/100*level);

            //1이면 원래 쿨타임
            float cooldown = 1 - (float) (cooldownPerLevel/100*level);
            spell.cast(player, power, new String[]{});

            //1이면 원래 쿨타임
            double coolMultiply = 1 - Stat.COOL_DEC.getTotal(player);

            //쿨타임 감소는 최대 95%까지 적용
            if(coolMultiply < 0.05) {
                coolMultiply = 0.05;
            }
            //쿨타임 재설정
            spell.setCooldown(player, spell.getCooldown()*(float) (cooldown*coolMultiply));
            return true;
        } catch (Exception e) {
            Msg.warn(player, "&c스킬 시전중 오류가 발생하였습니다. &7- "+skillName);
            return false;
        }
    }

    public boolean isRuneSkill() {
        return isRuneSkill;
    }

    public String getSkillName() {
        return skillName;
    }

    public String getMagicSpellName() {
        return magicSpellName;
    }

    public String getShiftSpell() {
        return shiftSpell;
    }

    public String getUnicode() {
        return unicode;
    }

    public String getShiftSkillUnicode() {
        return shiftSkillUnicode;
    }
}
