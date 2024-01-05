package me.rukon0621.guardians.skillsystem;

import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.spells.instant.DummySpell;
import com.nisovin.magicspells.spells.targeted.HealSpell;
import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import me.rukon0621.ridings.RideManager;
import me.rukon0621.ridings.RukonRiding;
import me.rukon0621.rinstance.RukonInstance;
import me.rukon0621.rpvp.instance.BattleInstance;
import me.rukon0621.teseion.Main;
import me.rukon0621.teseion.TeseionInstance;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static me.rukon0621.guardians.main.pfix;

public class SkillManager implements Listener {
    private static final main plugin = main.getPlugin();
    private static final String guiName = "&f\uF000\uF00A";
    private static final String guiName2 = "&f\uE200\uE200\uE200";
    private static Map<String, Skill> skillData;
    private static Map<Player, Map<String, String>> playerSkillData;
    private static Map<Player, String> playerSelectedSkill;
    private static Set<Player> doubleSneakCheck;
    private static final Set<String> paths = new HashSet<>();
    //private static final Set<String> blockCasting = new HashSet<>();

    private static Configure getPathConfig() {
        return new Configure("skillPath.yml", FileUtil.getOuterPluginFolder()+"/skills");
    }

    /**
     * 특정 스킬을 포함하는 config 파일을 반환
     * @param skillName 스킬 config를 반환 받고 싶은 스킬의 이름
     * @return 스킬의 config 파일을 반환, 존재하지 않는다면 null 반환
     */
    @Nullable
    private static Configure getSkillConfig(String skillName) {
        Configure config = getPathConfig();
        if(!config.getConfig().getKeys(false).contains(skillName)) return null;
        return new Configure(FileUtil.getOuterPluginFolder()+"/skills/skills/"+config.getConfig().getString(skillName));
    }

    public SkillManager() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reloadAllSkills();
    }

    public static Set<String> getPaths() {
        return paths;
    }

    public static void reloadAllSkills() {
        paths.clear();
        skillData = new HashMap<>();
        playerSkillData = new HashMap<>();
        playerSelectedSkill = new HashMap<>();
        doubleSneakCheck = new HashSet<>();
        Configure config = getPathConfig();
        for(String skillName : config.getConfig().getKeys(false)) {
            paths.add(config.getConfig().getString(skillName));
            reloadSkill(skillName);
        }
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            reloadPlayerSkill(player);
        }
    }

    /**
     * 해당 스킬을 리로드함
     * @param skillName 스킬 이름
     */
    public static void reloadSkill(String skillName) {
        skillData.put(skillName, new Skill(skillName, getSkillConfig(skillName)));
    }

    /**
     * 플레이어에게 장착된 스킬을 리로드함 존재하지 않게 된 스킬은 모두 삭제
     * @param player player
     */
    public static void reloadPlayerSkill(Player player) {
        Map<String, String> skills = loadEquipSkill(player);
        ArrayList<String> removed = new ArrayList<>();

        Set<String> bannedSkills = new PlayerData(player).getBannedSkillData();

        for(String skillKey : skills.keySet()) {
            String skillName = skills.get(skillKey);
            if (!skillData.containsKey(skillName)) {
                removed.add(skillKey);
            }
            else if(skillData.get(skillName).getLevel(player)==0) {
                removed.add(skillKey);
            }
            else if(bannedSkills.contains(skillName)) {
                removed.add(skillKey);
            }
        }
        for(String key : removed) {
            skills.remove(key);
        }
        playerSkillData.put(player, skills);
    }

    public static void createNewSkill(Player player, String skillName, String path, SkillType type) {
        if(skillData.containsKey(skillName)) {
            Msg.warn(player, "해당 스킬은 이미 존재하는 스킬입니다.");
            return;
        }

        //패스 등록
        Configure config = getPathConfig();
        config.getConfig().set(skillName, path);
        config.saveConfig();

        //새로운 객체 생성
        config = getSkillConfig(skillName);
        if(type.equals(SkillType.RUNE_SKILL)) {
            config.getConfig().set(skillName+".isRuneSkill", true);
            config.getConfig().set(skillName+".magicSpellNameWithShift", "null");
        }
        config.getConfig().set(skillName+".magicSpellName", "test");
        config.getConfig().set(skillName+".powerPerLevel", 5.0);
        config.getConfig().set(skillName+".cooldownPerLevel", 5.0);
        config.getConfig().set(skillName+".customModelData", 0);
        config.getConfig().set(skillName+".lores", new ArrayList<>());
        config.getConfig().set(skillName+".requiredSkills", new ArrayList<>());
        config.saveConfig();
        reloadSkill(skillName);
        Msg.send(player, "성공적으로 새로운 스킬을 생성했습니다.", pfix);
    }

    public static void deleteSkill(Player player, String skillName) {
        if(!skillData.containsKey(skillName)) {
            Msg.warn(player, "해당 스킬은 존재하지 않는 스킬입니다.");
            return;
        }
        Configure config = getSkillConfig(skillName);
        config.getConfig().set(skillName, null);
        config.saveConfig();
        config = getPathConfig();
        config.getConfig().set(skillName, null);
        config.saveConfig();
        reloadAllSkills();
        Msg.send(player, "성공적으로 스킬을 삭제했습니다.", pfix);
    }

    public static void showSkillList(Player player) {
        Msg.send(player, "서버에 존재하는 스킬의 목록과 경로입니다.", pfix);
        Configure config = getPathConfig();
        for(String skillName : config.getConfig().getKeys(false)) {
            Msg.send(player, skillName + " &7: &f" + config.getConfig().getString(skillName));
        }
    }

    public static Map<String, Skill> getSkillData() {
        return skillData;
    }

    public static void openSkillEquipGUI(Player player) {
        InvClass inv = new InvClass(3, guiName);
        ItemClass empty = new ItemClass(new ItemStack(Material.SCUTE), "&c[ 우클릭 ]");
        empty.setCustomModelData(29);
        empty.addLore("&f스킬을 선택하려면 클릭하십시오.");
        Map<String, String> skillData = getEquipSkill(player);

        String[] clicks = new String[]{"우클릭", "쉬프트+좌클릭", "쉬프트+우클릭", "더블 쉬프트", "버리기키"};
        String[] clicksEng = new String[]{"Right", "ShiftLeft", "ShiftRight", "DoubleShift", "DropKey"};
        int slot = 9;
        for(int i = 0 ; i < 5; i++) {
            if(skillData.containsKey(clicksEng[i])) {
                Skill skill = getSkill(skillData.get(clicksEng[i]));
                inv.setslot(slot, skill.getIcon(player));
            }
            else {
                empty.setName("&c[ "+clicks[i]+" ]");
                inv.setslot(slot, empty.getItem());
            }
            slot += 2;
        }
        player.openInventory(inv.getInv());
    }

    private static void openSkillList(Player player) {
        InvClass inv = new InvClass(3, guiName2);

        ItemClass it = new ItemClass(new ItemStack(Material.BARRIER), "&c장착 해제");
        inv.setslot(0, it.getItem());


        Set<String> bannedSkills = new PlayerData(player).getBannedSkillData();

        int slot = 1;
        for(Skill skill : skillData.values()) {
            if(skill.getLevel(player)==0) continue;
            if(bannedSkills.contains(skill.getSkillName())) continue;
            inv.setslot(slot, skill.getIcon(player));
            slot++;
        }

        player.openInventory(inv.getInv());
    }

    public static Skill getSkill(String skillName) {
        return skillData.get(skillName);
    }

    public static Map<String, String> getEquipSkill(Player player) {
        return playerSkillData.get(player);
    }

    public static Map<String, String> loadEquipSkill(Player player) {
        Map<String, String> skills = null;
        DataBase db = new DataBase();
        ResultSet set = db.executeQuery(String.format("SELECT equippedSkills FROM playerData WHERE uuid = '%s'", player.getUniqueId()));
        try {
            set.next();
            skills = (Map<String, String>) Serializer.deserialize(set.getBytes(1));
            set.close();
        } catch (SQLException e) {
            skills = new HashMap<>();
            e.printStackTrace();
        }
        db.close();
        return skills;
    }

    public static void saveEquipSkill(Player player, Map<String, String> data) {
        DataBase db = new DataBase();
        try {
            PreparedStatement statement = db.getConnection().prepareStatement(String.format("UPDATE playerData SET equippedSkills = ? WHERE uuid = '%s'", player.getUniqueId()));
            statement.setBytes(1, Serializer.serialize(data));
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db.close();
    }

    //스킬 추가 효과
    @EventHandler(ignoreCancelled = true)
    public void onCast(SpellCastEvent e) {
        if(e.getCaster() instanceof Player player) {
            if(player.getGameMode().equals(GameMode.SPECTATOR)) {
                e.setCancelled(true);
                return;
            }
        }
        if(e.getCaster() instanceof Player player) {
            if(player.getGameMode().equals(GameMode.CREATIVE)) {
                e.setCooldown(0);
            }
            else if(!(RukonInstance.inst().getInstanceManager().getPlayerInstance(player) instanceof BattleInstance) && e.getSpell().getName().equals("pvpPotion")) {
                Msg.warn(player, "이 아이템은 PVP 장에서만 사용할 수 있습니다.");
                return;
            }
        }
        if(e.getSpell().getName().equals("sword_main_attack")||e.getSpell().getName().equals("spear_main_attack")) {
            String soundName = "skill.sword.main_attack"+ Rand.randInt(1,6);
            Location loc = e.getCaster().getLocation();
            loc.getWorld().playSound(loc, soundName, 0.6f, Rand.randFloat(0.8, 1.3));
        }
        else if(e.getSpell().getName().equals("bow_main_attack")) {
            Location loc = e.getCaster().getLocation();
            loc.getWorld().playSound(loc, Sound.ITEM_CROSSBOW_SHOOT, 1, Rand.randFloat(0.8, 1.3));
        }
    }

    //어떤 클릭에 스킬을 장착할지
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals(guiName)) return;
        e.setCancelled(true);
        if(e.getRawSlot()==-999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.3f);
            new MenuWindow(player);
            return;
        }
        boolean click = false;
        if(e.getRawSlot()==9) {
            click = true;
            playerSelectedSkill.put(player, "Right");
        }
        else if(e.getRawSlot()==11) {
            click = true;
            playerSelectedSkill.put(player, "ShiftLeft");
        }
        else if(e.getRawSlot()==13) {
            click = true;
            playerSelectedSkill.put(player, "ShiftRight");
        }
        else if(e.getRawSlot()==15) {
            click = true;
            playerSelectedSkill.put(player, "DoubleShift");
        }
        else if(e.getRawSlot()==17) {
            click = true;
            playerSelectedSkill.put(player, "DropKey");
        }
        if(click) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            openSkillList(player);
        }
    }

    //스킬의 장착과 해제
    @EventHandler
    public void onInvClickSkillList(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals(guiName2)) return;
        e.setCancelled(true);
        if(e.getRawSlot()==-999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.3f);
            openSkillEquipGUI(player);
            return;
        }
        if(e.getCurrentItem()==null) return;
        if(e.getRawSlot()>=27) return;
        if(e.getRawSlot()==0) {
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, Rand.randFloat(0.8, 1.4));
            Map<String, String> data = playerSkillData.get(player);
            data.remove(playerSelectedSkill.get(player));
            playerSkillData.put(player, data);
            player.closeInventory();
            new BukkitRunnable() {
                @Override
                public void run() {
                    saveEquipSkill(player, data);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            openSkillEquipGUI(player);
                        }
                    }.runTask(plugin);
                }
            }.runTaskAsynchronously(plugin);
            return;
        }
        Map<String, String> data = playerSkillData.get(player);
        String skillName = Msg.recolor(e.getCurrentItem().getItemMeta().getDisplayName());
        if(!skillData.containsKey(skillName)) return;
        data.put(playerSelectedSkill.get(player) ,skillName);
        playerSelectedSkill.remove(player);
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, Rand.randFloat(0.8, 1.4));
        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                saveEquipSkill(player, data);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        openSkillEquipGUI(player);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onCastClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Map<String ,String> skills = playerSkillData.get(player);
        if(player.getInventory().getHeldItemSlot()!=0) return;
        if(RukonRiding.inst().getRideManager().isPlayerRiding(player)) {
            return;
        }
        if(EquipmentManager.getEquipment(player, "무기").getType().equals(Material.AIR)) return;
        if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)||e.getAction().equals(Action.LEFT_CLICK_AIR)) {
            if(player.isSneaking()) {
                if(skills.containsKey("ShiftLeft")) {
                    getSkill(skills.get("ShiftLeft")).cast(player);
                }
            }
            else {
                if(EquipmentManager.getEquipment(player, "무기").getType().equals(Material.AIR)) return;
                String type = EquipmentManager.getEquipmentItemData(player, "무기").getType();
                getSkill(type).cast(player);
            }
        }
        else if(e.getAction().equals(Action.RIGHT_CLICK_AIR)||e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if(player.isSneaking()) {
                if(skills.containsKey("ShiftRight")) {
                    getSkill(skills.get("ShiftRight")).cast(player);
                }
            }
            else {
                if(skills.containsKey("Right")) {
                    getSkill(skills.get("Right")).cast(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageToCast(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player player)) return;
        if(player.getInventory().getHeldItemSlot()!=0) return;
        if(e.getDamage() <= 0) return;
        if(RukonRiding.inst().getRideManager().isPlayerRiding(player)) {
            return;
        }
        /*
        String uuid = player.getUniqueId().toString();
        if(!blockCasting.add(uuid)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                blockCasting.remove(uuid);
            }
        }.runTaskLaterAsynchronously(plugin, 3);
         */

        if(player.isSneaking()) {
            Map<String ,String> skills = playerSkillData.get(player);
            if(skills.containsKey("ShiftLeft")) {
                getSkill(skills.get("ShiftLeft")).cast(player);
            }
        }
        else {
            ItemData itemData = EquipmentManager.getEquipmentItemData(player, "무기");
            if(itemData!=null) {
                String type = itemData.getType();
                getSkill(type).cast(player);
            }
        }
    }

    @EventHandler
    public void onCastByDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if(player.getInventory().getHeldItemSlot()!=0) return;
        if(RukonRiding.inst().getRideManager().isPlayerRiding(player)) {
            return;
        }
        Map<String ,String> skills = playerSkillData.get(player);
        if(skills.containsKey("DropKey")) {
            if(EquipmentManager.getEquipment(player, "무기").getType().equals(Material.AIR)) return;
            getSkill(skills.get("DropKey")).cast(player);
        }
    }

    @EventHandler
    public void onCastDoubleShift(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if(player.isSneaking()) return;
        if(player.getInventory().getHeldItemSlot()!=0) return;
        if(RukonRiding.inst().getRideManager().isPlayerRiding(player)) {
            return;
        }

        if(doubleSneakCheck.contains(player)) {
            Map<String ,String> skills = playerSkillData.get(player);
            if(skills.containsKey("DoubleShift")) {
                if(!EquipmentManager.getEquipment(player, "무기").getType().equals(Material.AIR)) getSkill(skills.get("DoubleShift")).cast(player);
            }
            doubleSneakCheck.remove(player);
        }
        else {
            doubleSneakCheck.add(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    doubleSneakCheck.remove(player);
                }
            }.runTaskLater(plugin, 7);
        }
    }
}
