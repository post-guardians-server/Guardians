package me.rukon0621.guardians.equipment;

import me.rukon0621.buff.BuffManager;
import me.rukon0621.buff.RukonBuff;
import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.GUI.WeaponSkinWindow;
import me.rukon0621.guardians.areawarp.AreaEnvironment;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import me.rukon0621.guild.element.Guild;
import me.rukon0621.guild.element.GuildPlayer;
import me.rukon0621.pay.PaymentData;
import me.rukon0621.pay.RukonPayment;
import me.rukon0621.pay.trade.TradeData;
import me.rukon0621.rpvp.RukonPVP;
import me.rukon0621.sampling.RukonSampling;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.main.pfix;

public class EquipmentManager implements Listener {
    private static final main plugin = main.getPlugin();
    private static final String GuiName = "&f\uF000\uF00E";
    private static HashMap<Player, HashMap<String, ItemStack>> equipmentData;
    private static HashMap<Player, HashMap<String, ItemData>> equipmentItemData;
    private static final Map<Player, HashMap<String, Number>> eqStatusData = new HashMap<>();
    private static final String[] expTarget = new String[]{"무기", "투구", "갑옷", "바지", "부츠", "목걸이", "벨트", "반지"};
    private static HashMap<String, Integer> equipmentSlotData;
    private static HashMap<String, String> equipmentTranslateData;
    private static HashMap<Player, Integer> playerInformationPage;
    private static final HashMap<UUID, Double> cachedTotalPower = new HashMap<>();
    private static final int[] informationIconSlots = new int[]{18,19,20,27,28,29};
    private static final int informationMaxPage = 5; //내 정보의 페이지 수
    public static final double maxEvade = 25;

    public static HashMap<UUID, Double> getCachedTotalPower() {
        return cachedTotalPower;
    }

    public EquipmentManager() {

        DataBase db = new DataBase();
        db.executeClose("CREATE TABLE IF NOT EXISTS equipmentData(uuid varchar(36) PRIMARY KEY, weapon blob, helmet blob, chest blob, leggings blob, boots blob, necklace blob, belt blob, ring blob, rune1 blob, rune2 blob, rune3 blob, riding blob);");
        db.execute("ALTER TABLE equipmentData ADD pendant blob;");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        equipmentData = new HashMap<>();
        equipmentItemData = new HashMap<>();
        equipmentSlotData = new HashMap<>();
        equipmentTranslateData = new HashMap<>();
        playerInformationPage = new HashMap<>();

        equipmentTranslateData.put("무기", "weapon");
        equipmentTranslateData.put("투구", "helmet");
        equipmentTranslateData.put("갑옷", "chest");
        equipmentTranslateData.put("바지", "leggings");
        equipmentTranslateData.put("부츠", "boots");
        equipmentTranslateData.put("목걸이", "necklace");
        equipmentTranslateData.put("벨트", "belt");
        equipmentTranslateData.put("반지", "ring");
        equipmentTranslateData.put("룬", "rune");
        equipmentTranslateData.put("라이딩", "riding");
        equipmentTranslateData.put("사증", "pendant");

        equipmentSlotData.put("무기", 16);
        equipmentSlotData.put("투구", 12);
        equipmentSlotData.put("갑옷", 21);
        equipmentSlotData.put("바지", 30);
        equipmentSlotData.put("부츠", 39);
        equipmentSlotData.put("목걸이", 34);
        equipmentSlotData.put("벨트", 43);
        equipmentSlotData.put("반지", 25);
        equipmentSlotData.put("라이딩", 37);
        equipmentSlotData.put("룬1", 49);
        equipmentSlotData.put("룬2", 50);
        equipmentSlotData.put("룬3", 51);
        equipmentSlotData.put("사증", 23);
    }

    public static List<String> addExp(Player player, double exp) {
        List<String> levelupList = new ArrayList<>();
        int levelLimit = new PlayerData(player).getLevel();
        for(String koreanKeyName : expTarget) {
            if(getItem(player, koreanKeyName).getType().equals(Material.AIR)) continue;
            ItemData itemData = new ItemData(getItem(player, koreanKeyName));
            int lv = itemData.getLevel();
            itemData.addExp(exp, false, levelLimit);
            if(lv != itemData.getLevel()) levelupList.add(koreanKeyName);
            setItem(player, koreanKeyName, itemData.getItemStack());
        }
        return levelupList;
    }

    public static void openEquipmentGUI(Player player) {
        InvClass inv = new InvClass(6, GuiName);
        HashMap<String, ItemStack> equipments = equipmentData.get(player);
        reloadEquipment(player, false);
        for(String type : equipmentSlotData.keySet()) {
            inv.setslot(equipmentSlotData.get(type), equipments.get(type));
        }
        ItemClass it = new ItemClass(new ItemStack(Material.IRON_HOE), "&7");
        it.setCustomModelData(12);
        inv.setslot(53, it.getItem());
        reloadInformationIcon(player, inv.getInv());
        player.openInventory(inv.getInv());
    }

    public static void reloadEquipment(Player player, boolean changeWeaponInventorySlot) {
        double previousHealth = player.getHealth() / player.getMaxHealth();

        //기본 스텟 날리기
        for(Stat stat : Stat.values()) {
            stat.set(player, 0);
            stat.setBase(player, 0);
            stat.setCollection(player, 0);
            stat.setEnvironment(player, 0);
            stat.setAdamantStone(player, 0);
        }
        PlayerData pdc = new PlayerData(player);
        HashMap<String, ItemStack> map = new HashMap<>();
        HashMap<String, Number> equipmentStatus = new HashMap<>();

        if(false) {
        //if(RukonPVP.inst().getPvpManager().isPlayerInBattleInstance(player)) {
            ItemStack item = ItemSaver.getItem("PVP스텟 조정기").getItem().clone();
            ItemData itemData = new ItemData(item);
            itemData.setLevel(pdc.getLevel());
            equipmentStatus = itemData.applyEquipmentStatToPlayer(player, equipmentStatus);
        }
        else {
            HashMap<String, ItemData> itemDataMap = new HashMap<>();
            setWeapon(player, getWeapon(player));
            ItemStack weapon = getWeapon(player);
            String key = "무기";
            map.put(key, weapon);
            if(!weapon.getType().equals(Material.AIR))  {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }

            key = "투구";
            setHelmet(player, getHelmet(player));
            map.put(key, getHelmet(player));
            if(!map.get(key).getType().equals(Material.AIR)) {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }

            key = "갑옷";
            setChest(player, getChest(player));
            map.put(key, getChest(player));
            if(!map.get(key).getType().equals(Material.AIR)) {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }

            key = "바지";
            setLeggings(player, getLeggings(player));
            map.put(key, getLeggings(player));
            if(!map.get(key).getType().equals(Material.AIR)) {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }

            key = "부츠";
            setBoots(player, getBoots(player));
            map.put(key, getBoots(player));
            if(!map.get(key).getType().equals(Material.AIR)) {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }

            key = "목걸이";
            setNecklace(player, getNecklace(player));
            map.put(key, getNecklace(player));
            if(!map.get(key).getType().equals(Material.AIR)) {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }

            key = "벨트";
            setBelt(player, getBelt(player));
            map.put(key, getBelt(player));
            if(!map.get(key).getType().equals(Material.AIR)) {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }

            key = "반지";
            setRing(player, getRing(player));
            map.put(key, getRing(player));
            if(!map.get(key).getType().equals(Material.AIR)) {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }

            key = "라이딩";
            setRiding(player, getRiding(player));
            map.put(key, getRiding(player));
            if(!map.get(key).getType().equals(Material.AIR)) {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }

            key = "사증";
            setPendant(player, getPendant(player));
            map.put(key, getPendant(player));
            if(!map.get(key).getType().equals(Material.AIR)) {
                itemDataMap.put(key, new ItemData(map.get(key)));
                equipmentStatus = itemDataMap.get(key).applyEquipmentStatToPlayer(player, equipmentStatus);
            }
            //라이딩 장착 장비창 만들어야함
            //        //실제 인벤토리 반영
            if(changeWeaponInventorySlot) {
                try {
                    player.getInventory().setItem(0, weapon);
                    WeaponSkinWindow.reloadWeaponSkin(player);
                } catch (Exception e) {
                    player.getInventory().setItem(0, new ItemStack(Material.AIR));
                }
            }
            setRunes(player, getRunes(player));
            ArrayList<ItemStack> items = getRunes(player);
            ItemClass empty = new ItemClass(new ItemStack(Material.SCUTE), "&7장착된 룬 없음");
            empty.setCustomModelData(92);
            empty.addLore("&f장비 창에서 룬을 장착할 수 있습니다.");
            for(int i = 0; i < 3 ;i++) {
                String keyName = "룬"+(i+1);
                map.put(keyName, items.get(i));
                if(items.get(i).getType().equals(Material.AIR)) {
                    player.getInventory().setItem(1+i, empty.getItem());
                }
                else {
                    ItemData itemData = new ItemData(items.get(i));
                    itemDataMap.put(keyName, itemData);
                    equipmentStatus = itemData.applyEquipmentStatToPlayer(player, equipmentStatus);
                    player.getInventory().setItem(1+i, items.get(i));
                }
            }
            equipmentData.put(player, map);
            equipmentItemData.put(player, itemDataMap);
        }


        HashMap<String, Pair> attributesAbility = new HashMap<>();
        //전체 속성 반영
        PlayerData.reloadStatus(player);
        pdc.getEnvironmentResistance().clear();
        for(String loopKey : equipmentStatus.keySet()) {
            double value = equipmentStatus.get(loopKey).doubleValue();
            Stat stat = Stat.getStatByCodeName(loopKey);

            if(stat != null) {
                stat.set(player, stat.get(player) + value);
                continue;
            }

            double multiply;
            if(loopKey.endsWith("감소")) {
                multiply = equipmentStatus.get(loopKey).doubleValue() / -25.0;
            }
            else {
                multiply = equipmentStatus.get(loopKey).doubleValue() / 25.0;
            }

            boolean statFound = false;
            for(Stat lpStat : Stat.values()) {
                if(!lpStat.toString().endsWith("PER")) continue;
                if(loopKey.startsWith("전체 " + lpStat.getKorName()) || loopKey.equals("전체 체력 증가") && lpStat.equals(Stat.HEALTH_PER)) {
                    lpStat.set(player, lpStat.get(player) + multiply);
                    statFound = true;
                    break;
                }
            }
            if(statFound) continue;
            //방어관통 Lv.1 당 4% 증가
            if(loopKey.startsWith("방어 관통")) {
                Stat.IGNORE_ARMOR.set(player, multiply / 2  + Stat.IGNORE_ARMOR.get(player));
            }
            else if(loopKey.endsWith(" 적응력")) {
                String type = loopKey.replaceAll(" 적응력", "");
                int level = equipmentStatus.get(loopKey).intValue();
                pdc.getEnvironmentResistance().put(type, level);
            }
            //공격 회피 Lv.1 당 2% 증가
            else if(loopKey.startsWith("공격 회피")) {
                Stat.EVADE.set(player, multiply / 4 + Stat.EVADE.get(player));
            }
            else if(loopKey.startsWith("속성 반감")) {
                String type = loopKey.split(": ")[1].trim();
                attributesAbility.putIfAbsent(type, new Pair(0, 0));
                Pair pair = attributesAbility.get(type);
                pair.setFirst(pair.getFirst() - equipmentStatus.get(loopKey).doubleValue() / 2);
                pair.setSecond(pair.getSecond() - equipmentStatus.get(loopKey).doubleValue() / 2);
            }
            else if(loopKey.startsWith("속성 저항력")) {
                String type = loopKey.split(": ")[1].trim();
                attributesAbility.putIfAbsent(type, new Pair(0, 0));
                Pair pair = attributesAbility.get(type);
                pair.setSecond(pair.getSecond() + equipmentStatus.get(loopKey).doubleValue());
            }
            else if(loopKey.startsWith("속성 파괴력")) {
                String type = loopKey.split(": ")[1].trim();
                attributesAbility.putIfAbsent(type, new Pair(0, 0));
                Pair pair = attributesAbility.get(type);
                pair.setFirst(pair.getFirst() + equipmentStatus.get(loopKey).doubleValue());
            }
        }
        pdc.setAttributeAbility(attributesAbility);

        //환경 영향
        if(AreaManger.getArea(pdc.getArea()).getEnvironment()!=null) {
            int envResLevel;
            AreaEnvironment environment = AreaManger.getArea(pdc.getArea()).getEnvironment();
            assert environment != null;
            envResLevel = (Integer) equipmentStatus.getOrDefault(environment.getAttrType() + " 적응력", 0);
            for(Stat stat : environment.getStatMap().keySet()) {
                stat.setEnvironment(player, environment.getParsedStat(stat, envResLevel));
            }
        }


        //버프
        RukonBuff.inst().getBuffManager().reloadBuffStats(player);

        //샘플링
        RukonSampling.inst().getSamplingManager().reloadPlayerStat(player);

        //방관, 회피, 쿨감
        Stat.IGNORE_ARMOR.set(player, Stat.IGNORE_ARMOR.get(player) + Math.pow(Math.sqrt(Math.sqrt(Stat.IGNORE_ARMOR_POWER.getTotal(player))), 3.0D) / 200.0D);
        Stat.COOL_DEC.set(player, Stat.COOL_DEC.get(player) + Math.pow(Math.sqrt(Math.sqrt(Stat.COOL_DEC_POWER.getTotal(player))), 3.0D) / 300.0D);
        Stat.EVADE.set(player, Stat.EVADE.get(player) + Math.pow(Math.sqrt(Math.sqrt(Stat.EVADE_POWER.getTotal(player))), 3.0D) / 300.0D);
        Stat.HEALTH.setBase(player, Stat.HEALTH.getBase(player) + LevelData.getLevelUpHealth(pdc.getLevel(), true));

        player.setMaxHealth(Math.max(10, Stat.HEALTH.getTotal(player)));
        player.setHealth(player.getMaxHealth() * previousHealth);
        player.setWalkSpeed(0.2f + (float) (Math.min((Stat.MOVE_SPEED.getTotal(player) - 100), 200) / 750.0));

        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        assert attr != null;
        attr.setBaseValue(Stat.KB_RESISTANCE.getTotal(player));
        eqStatusData.put(player, equipmentStatus);
        BarManager.reloadBar(player);
        if(true) {
        //if(!RukonPVP.inst().getPvpManager().isPlayerInBattleInstance(player)) {
            cachedTotalPower.put(player.getUniqueId(), Stat.getTotalPower(player));
            if(pdc.getGuildID() != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Guild guild = Guild.loadGuild(pdc.getGuildID());
                        if(guild == null) {
                            Bukkit.getLogger().severe(player.getName() + "님의 길드: " + pdc.getGuildID() + "는 존재하지 않는 길드입니다.");
                            return;
                        }
                        GuildPlayer gp = guild.getMember(player.getUniqueId());
                        if(gp == null) {
                            Bukkit.getLogger().severe(player.getName() + "님의 길드: " + guild.getName() + "에 속하지 않은 맴버입니다.");
                            return;
                        }
                        boolean change = false;
                        if(!gp.getName().equals(player.getName())) {
                            gp.setName(player.getName());
                            change = true;
                        }
                        double totalPower = Stat.getTotalPower(player);
                        if((int) gp.getTotalPower() != (int) totalPower) {
                            gp.setTotalPower(totalPower);
                            change = true;
                        }
                        if(change) guild.reloadMemberData();
                    }
                }.runTaskAsynchronously(main.getPlugin());
            }
        }
    }

    //장착된 장비 해제 슬롯 확인 (슬롯이 없으면 -1, 있으면 해당 슬롯을 반환
    private static int checkSlot(Player player) {
        for(int i = 3; i < 36;i++) {
            if(player.getInventory().getStorageContents()[i]==null) {
                return i;
            }
        }
        if(equipmentData.get(player).get("무기").getType().equals(Material.AIR)) {
            if(player.getInventory().getStorageContents()[0]==null) {
                return 0;
            }
        }
        return -1;
    }

    /**
     * 인벤토리의 정보 아이콘을 새로고침합니다.
     *
     * @param player 플레이어
     * @param inv 적용할 대상 인벤토리
     */
    private static void reloadInformationIcon(Player player, Inventory inv) {
        ItemStack icon = getInformationItem(player, playerInformationPage.get(player));
        for(int slot : informationIconSlots) {
            inv.setItem(slot, icon);
        }
    }

    /**
     * 해당 플레이어의 정보를 담은 해당 페이지의 아이콘을 반환 (장비 창에 띄워지는 내 정보 아이콘)
     * @param player 플레이어
     * @param page 정보 페이지 (페이지 마다 다른 정보를 띄워줌)
     * @return 해당하는 아이템 객체를 반환
     */
    private static ItemStack getInformationItem(Player player, int page) {
        ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&c[ 내정보 ] " + String.format("&7(%d / %d)", page, informationMaxPage));
        it.setCustomModelData(7);
        PlayerData pdc = new PlayerData(player);
        PaymentData pyd = new PaymentData(player);
        if(page==1){
            it.addLore("&7Lv. &f" + pdc.getLevel());
            it.addLore(String.format("&7Exp. %d / %d", pdc.getExp(), LevelData.expAtLevel.get(pdc.getLevel())));
            if(pdc.getGuildID() != null) {
                it.addLore("&7소속 길드: " + pdc.getGuildName());
            }

            it.addLore(" ");
            it.addLore("&7현재 지역: &f"+pdc.getArea());
            it.addLore(String.format("&7소지금: &f%d 디나르", pdc.getMoney()));
            it.addLore(String.format("&7가진 루나르: &f%d", pyd.getRunar()));
            it.addLore(String.format("&7가진 스킬 포인트: &f%d", pdc.getSkillPoint()));
            it.addLore(String.format("&7가진 제작 스킬 포인트: &f%d", pdc.getCraftSkillPoint()));
            it.addLore(String.format("&7가진 스킬 취소 포인트: &f%d", pdc.getUnlearnChance()));
            it.addLore(String.format("&7가진 에너지 코어: &f%d / %d", pdc.getEnergyCore(), pdc.getMaxEnergyCore()));
            it.addLore(String.format("&7피로도: &f%d / %d", pdc.getFatigue(), pdc.getMaxFatigue()));
            if(pdc.getSpiritOfHero() > 0) {
                it.addLore("&7가진 영웅의 기억: &f" + pdc.getSpiritOfHero());
            }
            //이벤트 코인
            //it.addLore(" ");
            //it.addLore("#bbccff가진 " + MONEY.EVENT_COIN.getDisplayName() + ": " + pyd.getEventCoin());

        }
        else if (page==2) {
            it.addLore(String.format("&e전투력: %.2f", Stat.getTotalPower(player)));
            it.addLore(" ");
            for(Stat stat : Stat.values()) {
                if(!stat.isShowStat()) continue;
                double value = stat.getTotal(player);
                if (value == 0 && !stat.equals(Stat.CRT_CHANCE)) continue;
                String lore = getLore(player, stat, value);
                it.addLore(lore);
                if (stat.equals(Stat.IGNORE_ARMOR)) it.addLore(" ");
            }
        }
        else if (page==3) {
            it.addLore("&e『 속성 파괴력 / 저항력 정보 』");
            HashMap<String, Pair> attrMap = pdc.getAttributeAbility();
            if(attrMap.keySet().isEmpty()) {
                it.addLore("&c아무 속성에 대한 대항력도 없습니다.");
                it.addLore("&c속성 대항력이 있는 장비를 맞춰보세요!");
            }
            else {
                for(String type : attrMap.keySet()) {
                    it.addLore(String.format("&7%s 속성 : ( %.0f%% / %.0f%% )", type, Math.min(100, attrMap.get(type).getFirst() * 4), Math.min(80, attrMap.get(type).getSecond() * 2)));
                }
            }
        }
        else if (page==4) {
            it.addLore("&e『 적용중인 버프 정보 』");
            BuffManager manager = RukonBuff.inst().getBuffManager();
            if(manager.getBuffs(player).isEmpty()) {
                it.addLore("&7현재 적용중인 버프가 없습니다.");
                it.addLore("&7버프 아이템을 이용해 버프를 받아보세요!");
            }
            else {
                manager.getBuffs(player).forEach(buff -> {
                    it.addLore("&f" + buff.getBuffName() + " &8| " + DateUtil.formatDate(buff.getRemainSec()));
                    buff.getStatMap().forEach((stat, value) -> {
                        if(stat.isUsingPercentage()) it.addLore(String.format("&7- %s %.2f%%", stat.getKorName(), value * 100));
                        else it.addLore(String.format("&7- %s %.2f", stat.getKorName(), value));
                    });
                });
            }
        }
        else if (page==5) {
            it.addLore("&e『 샘플링으로 얻은 스텟 』");
            for(Stat stat : Stat.values()) {
                if(!stat.isShowStat()) continue;
                double value = stat.getCollection(player);
                if(value == 0) continue;
                if(stat.isUsingPercentage()) it.addLore(String.format("&7%s &f+%.2f%%", stat.getKorName(), value * 100));
                else it.addLore(String.format("&7%s &f+%.2f", stat.getKorName(), value));
            }
        }
        it.addLore(" ");
        it.addLore("&e\uE011 클릭하여 페이지 넘기기");
        return it.getItem();
    }

    private static String getLore(Player player, Stat stat, double value) {
        String lore;
        if (stat.equals(Stat.HEALTH)) {
            lore = String.format("&7체력: &f%.2f / %.2f", player.getHealth(), value);
        }
        else if (stat.isUsingPercentage()) {
            lore = String.format("&7%s: &f%.2f%%", stat.getKorName(), 100.0D * value);
        }
        else {
            lore = String.format("&7%s: &f%.2f", stat.getKorName(), value);
        }
        if (stat.equals(Stat.STUN_DUR)) lore = lore + "초";
        if (stat.getMaxValue() != -1.0D && value >= stat.getMaxValue()) lore += " &c(MAX)";
        return lore;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        playerInformationPage.put(e.getPlayer(), 1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        eqStatusData.remove(e.getPlayer());
        cachedTotalPower.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals(GuiName)) return;
        e.setCancelled(true);

        //내 정보 확인
        for(int slot : informationIconSlots) {
            if(e.getRawSlot()==slot) {
                int page = playerInformationPage.get(player);
                if(e.getClick().equals(ClickType.LEFT)) {
                    page++;
                    if(page > informationMaxPage) page = 1;
                }
                else if(e.getClick().equals(ClickType.RIGHT)) {
                    page--;
                    if(page <= 0) page = informationMaxPage;
                }
                else return;
                playerInformationPage.put(player, page);
                reloadInformationIcon(player, e.getInventory());
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                return;
            }
        }
        if(RukonPVP.inst().getPvpManager().isPlayerInBattleInstance(player)) return;
        if(e.getClick().equals(ClickType.DOUBLE_CLICK)) return;
        if (e.getRawSlot()==-999) {
            new MenuWindow(player);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
            return;
        }
        if(e.getCurrentItem()==null) return;
        if(e.getRawSlot()>=82&&e.getRawSlot()<=84) return;

        //장비 해제
        if(equipmentSlotData.containsValue(e.getRawSlot())) {
            int slot = checkSlot(player);
            if(slot==-1) {
                Msg.send(player, "&c인벤토리에 공간이 부족하여 장비 장착을 해제할 수 없습니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                return;
            }
            ItemData idata = new ItemData(e.getCurrentItem());
            if(idata.isWeapon()) { //무기는 종류가 많으니 "무기"로 따로 분류
                setWeapon(player, new ItemStack(Material.AIR));
            }
            else if(idata.isRiding()) { //무기는 종류가 많으니 "무기"로 따로 분류
                Msg.warn(player, "라이딩은 라이딩 관리국에서 장착을 해제할 수 있습니다.");
                return;
            }
            else if (idata.getType().equals("룬")) { //룬은 독특한 케이스이니 따로 분류
                ArrayList<ItemStack> runes = getRunes(player);

                if(e.getRawSlot()==equipmentSlotData.get("룬1")) {
                    runes.set(0, new ItemStack(Material.AIR));
                }
                else if(e.getRawSlot()==equipmentSlotData.get("룬2")) {
                    runes.set(1, new ItemStack(Material.AIR));
                }
                else if(e.getRawSlot()==equipmentSlotData.get("룬3")) {
                    runes.set(2, new ItemStack(Material.AIR));
                }
                setRunes(player, runes);
            }
            else { //그 외 장비
                setItem(player, idata.getType(), new ItemStack(Material.AIR));
            }
            reloadEquipment(player, idata.isWeapon()); //0,1,2,3칸 리셋
            player.getInventory().setItem(slot, e.getCurrentItem());
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, Rand.randFloat(0.8, 1.3));

            openEquipmentGUI(player);
            return;
        }

        ItemData idata = new ItemData(e.getCurrentItem());
        if(!idata.isEquipment()) return;
        PlayerData pdc = new PlayerData(player);
        if(idata.getLevel()>pdc.getLevel()) {
            Msg.warn(player, "&4장비의 레벨이 플레이어보다 높아 이 장비를 사용할 수 없습니다.");
            return;
        }
        if(idata.getRequiredLevel()>pdc.getLevel()) {
            Msg.warn(player, "&4장비의 요구 레벨보다 플레이어의 레벨을 낮아 아이템을 장착할 수 없습니다.");
            return;
        }

        if(e.getRawSlot()==81&&!idata.isWeapon()) return;

        //장비 장착
        TradeData data = RukonPayment.inst().getTradeManager().getTradeData(player);
        if(data != null) {
            if(data.getItem().isSimilar(e.getCurrentItem())) {
                Msg.warn(player, "이 아이템은 현재 거래 대기 중에 있습니다. 거래 대기가 만료된 이후에 장착해주세요.");
                return;
            }
        }
        if (idata.isWeapon()) {
            if(player.getInventory().getItem(0)!=null&&e.getRawSlot()!=81) {
                if(!player.getInventory().getItem(0).equals(equipmentData.get(player).get("무기"))) {
                    Msg.send(player, "&c핫바 슬롯 1번 (무기 슬롯)을 비워야 장비를 장착할 수 있습니다.", pfix);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                    return;
                }
            }
            if(!equipmentData.get(player).get("무기").getType().equals(Material.AIR)) {
                Msg.send(player, "&c먼저 장착된 무기를 해제해주세요. 클릭하여 장착된 아이템을 해제할 수 있습니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                return;
            }
            setWeapon(player, e.getCurrentItem());
        }
        else if (idata.isRiding()) {
            Msg.warn(player, "라이딩은 라이딩 관리국에서 장착할 수 있습니다.");
            return;
        }
        else if(idata.getType().equals("룬")) {
            ArrayList<ItemStack> runes = getRunes(player);
            boolean pass = false;
            for(int i = 0; i < 3; i++) {
                if(runes.get(i).getType().equals(Material.AIR)) {
                    runes.set(i, e.getCurrentItem());
                    setRunes(player, runes);
                    pass = true;
                    break;
                }
            }
            if(!pass) {
                Msg.send(player, "&c룬은 최대 3개까지만 장착할 수 있습니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                return;
            }
        }
        else {
            if(!equipmentData.get(player).get(idata.getType()).getType().equals(Material.AIR)) {
                Msg.send(player, "&c먼저 장착된 장비를 해제해주세요. 클릭하여 장착된 아이템을 해제할 수 있습니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                return;
            }
            setItem(player, idata.getType(), e.getCurrentItem());
        }
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, Rand.randFloat(0.8, 1.5));
        e.setCurrentItem(new ItemStack(Material.AIR));
        reloadEquipment(player,idata.isWeapon());
        openEquipmentGUI(player);
    }

    public static void resetAllEquipment(Player player) {
        ItemStack air = new ItemStack(Material.AIR);
        equipmentData.put(player, new HashMap<>());
        setWeapon(player, air);
        setHelmet(player, air);
        setChest(player, air);
        setLeggings(player, air);
        setBoots(player, air);
        setRing(player, air);
        setBelt(player, air);
        setNecklace(player, air);
        ArrayList<ItemStack> runes = new ArrayList<>();
        runes.add(air);
        runes.add(air);
        runes.add(air);
        setRunes(player, runes);
        setRiding(player, air);
        setPendant(player, air);
        reloadEquipment(player, true);
        saveEquipmentsToDataBase(player, new CountDownLatch(1));
    }

    public static ItemStack getEquipment(Player player, String equipment) {
        return equipmentData.get(player).getOrDefault(equipment, new ItemStack(Material.AIR));
    }
    public static ItemData getEquipmentItemData(Player player, String equipment) {
        if(!equipmentData.get(player).containsKey(equipment)) return null;
        return equipmentItemData.get(player).get(equipment);
    }

    //Equipment getter and setter
    //동시에 itemReload를 진행
    public static HashMap<String, ItemStack> getEquipmentData(Player player) {
        return equipmentData.getOrDefault(player, new HashMap<>());
    }
    public static ItemStack getItem(Player player, String koreanKeyName) {
        return equipmentData.get(player).getOrDefault(koreanKeyName, new ItemStack(Material.AIR));
        /*
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "item."+equipmentTranslateData.get(koreanKeyName));
        if(!pdc.has(key, PersistentDataType.BYTE_ARRAY)) return new ItemStack(Material.AIR);
        return (ItemStack) Serializer.deserializeBukkitObject(pdc.get(key, PersistentDataType.BYTE_ARRAY));
         */
    }

    public static void setItem(Player player, String koreanKeyName, ItemStack item) {
        equipmentData.get(player).put(koreanKeyName, item);
        /*
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "item."+equipmentTranslateData.get(koreanKeyName));
        pdc.set(key, PersistentDataType.BYTE_ARRAY, Serializer.serializeBukkitObject(ItemSaver.reloadItem(item)));
         */
    }
    public static ItemStack getWeapon(Player player) {
        return getItem(player, "무기");
    }
    private static void setWeapon(Player player, ItemStack item) {
        setItem(player, "무기", item);
    }
    private static ItemStack getHelmet(Player player) {
        return getItem(player, "투구");
    }
    private static void setHelmet(Player player, ItemStack item) {
        setItem(player, "투구", item);
    }
    private static ItemStack getChest(Player player) {
        return getItem(player, "갑옷");
    }
    private static void setChest(Player player, ItemStack item) {
        setItem(player, "갑옷", item);
    }
    private static ItemStack getLeggings(Player player) {
        return getItem(player, "바지");
    }
    private static void setLeggings(Player player, ItemStack item) {
        setItem(player, "바지", item);
    }
    private static ItemStack getBoots(Player player) {
        return getItem(player, "부츠");
    }
    private static void setBoots(Player player, ItemStack item) {
        setItem(player, "부츠", item);
    }
    private static ItemStack getNecklace(Player player) {
        return getItem(player, "목걸이");
    }
    private static void setNecklace(Player player, ItemStack item) {
        setItem(player, "목걸이", item);
    }
    private static ItemStack getBelt(Player player) {
        return getItem(player, "벨트");
    }
    private static void setBelt(Player player, ItemStack item) {
        setItem(player, "벨트", item);
    }
    private static void setRing(Player player, ItemStack item) {
        setItem(player, "반지", item);
    }
    private static ItemStack getRing(Player player) {
        return getItem(player, "반지");
    }
    private static ArrayList<ItemStack> getRunes(Player player) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for(int i = 1; i <= 3; i++) {
            items.add(getItem(player, "룬" + i));
        }
        return items;
        /*
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "item.rune");
        if(!pdc.has(key, PersistentDataType.BYTE_ARRAY)) {
            ArrayList<ItemStack> items = new ArrayList<>();
            items.add(new ItemStack(Material.AIR));
            items.add(new ItemStack(Material.AIR));
            items.add(new ItemStack(Material.AIR));
            return items;
        }
         */
    }
    public static void setRunes(Player player, ArrayList<ItemStack> items) {
        int i = 1;
        for(ItemStack it : items) {
            setItem(player, "룬" + i, ItemSaver.reloadItem(it));
            i++;
        }
        /*
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "item.rune");
        ArrayList<ItemStack> items = new ArrayList<>();
        for(ItemStack it : orginalItems) {
            items.add(ItemSaver.reloadItem(it));
        }
        pdc.set(key, PersistentDataType.BYTE_ARRAY, Serializer.serializeBukkitObject(items));
         */
    }
    private static ItemStack getRiding(Player player) {
        return getItem(player, "라이딩");
    }
    private static void setRiding(Player player, ItemStack item) {
        setItem(player, "라이딩", item);
    }
    private static ItemStack getPendant(Player player) {
        return getItem(player, "사증");
    }
    private static void setPendant(Player player, ItemStack item) {
        setItem(player, "사증", item);
    }

    public static void loadEquipmentsFromDataBase(Player player, CountDownLatch latch, String uuid) {
        if(!equipmentData.containsKey(player)) {
            equipmentData.put(player, new HashMap<>());
        }
        try {
            DataBase dataBase = new DataBase();
            ResultSet resultSet = dataBase.executeQuery(String.format("SELECT * FROM equipmentData WHERE uuid = '%s'", uuid));
            resultSet.next();
            ItemStack weapon = (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(2)), new ItemStack(Material.AIR));
            if(!weapon.getType().equals(Material.AIR)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.getInventory().setItem(0, weapon);
                    }
                }.runTask(plugin);
            }
            setWeapon(player, weapon);
            setHelmet(player, (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(3)), new ItemStack(Material.AIR)));
            setChest(player, (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(4)), new ItemStack(Material.AIR)));
            setLeggings(player, (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(5)), new ItemStack(Material.AIR)));
            setBoots(player, (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(6)), new ItemStack(Material.AIR)));
            setNecklace(player, (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(7)), new ItemStack(Material.AIR)));
            setBelt(player, (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(8)), new ItemStack(Material.AIR)));
            setRing(player, (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(9)), new ItemStack(Material.AIR)));
            ArrayList<ItemStack> runes = new ArrayList<>();
            for(int i = 10; i < 13; i++) {
                runes.add((ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(i)), new ItemStack(Material.AIR)));
            }
            setRunes(player, runes);
            setRiding(player, (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(13)), new ItemStack(Material.AIR)));
            setPendant(player, (ItemStack) NullManager.defaultNull(Serializer.deserializeBukkitObject(resultSet.getBytes(14)), new ItemStack(Material.AIR)));
            dataBase.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(player.getName() + " - EquipmentData Successfully Loaded.");
        new BukkitRunnable() {
            @Override
            public void run() {
                reloadEquipment(player, false);
                latch.countDown();
                ItemStack item = getWeapon(player);
                if(item.getType().equals(Material.AIR)) return;
                player.getInventory().setItem(0, getWeapon(player));
                WeaponSkinWindow.reloadWeaponSkin(player);
            }
        }.runTask(plugin);
    }

    public static void saveEquipmentsToDataBase(Player player, CountDownLatch latch) {
        ItemStack weapon = getWeapon(player);
        ItemStack helmet = getHelmet(player);
        ItemStack chest = getChest(player);
        ItemStack leggings = getLeggings(player);
        ItemStack boots = getBoots(player);
        ItemStack necklace = getNecklace(player);
        ItemStack belt = getBelt(player);
        ItemStack ring = getRing(player);
        ArrayList<ItemStack> runes = getRunes(player);
        ItemStack riding = getRiding(player);
        ItemStack pendant = getPendant(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    DataBase dataBase = new DataBase();
                    PreparedStatement preparedStatement = dataBase.getConnection().prepareStatement("REPLACE INTO equipmentData VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, player.getUniqueId().toString());
                    preparedStatement.setBytes(2, Serializer.serializeBukkitObject(weapon));
                    preparedStatement.setBytes(3, Serializer.serializeBukkitObject(helmet));
                    preparedStatement.setBytes(4, Serializer.serializeBukkitObject(chest));
                    preparedStatement.setBytes(5, Serializer.serializeBukkitObject(leggings));
                    preparedStatement.setBytes(6, Serializer.serializeBukkitObject(boots));
                    preparedStatement.setBytes(7, Serializer.serializeBukkitObject(necklace));
                    preparedStatement.setBytes(8, Serializer.serializeBukkitObject(belt));
                    preparedStatement.setBytes(9, Serializer.serializeBukkitObject(ring));
                    preparedStatement.setBytes(10, Serializer.serializeBukkitObject(runes.get(0)));
                    preparedStatement.setBytes(11, Serializer.serializeBukkitObject(runes.get(1)));
                    preparedStatement.setBytes(12, Serializer.serializeBukkitObject(runes.get(2)));
                    preparedStatement.setBytes(13, Serializer.serializeBukkitObject(riding));
                    preparedStatement.setBytes(14, Serializer.serializeBukkitObject(pendant));
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    dataBase.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        }.runTaskAsynchronously(plugin);

    }

    public static int getEquipmentAttrLevel(Player player, String attrName) {
        if(!eqStatusData.containsKey(player)) return 0;
        return eqStatusData.get(player).getOrDefault(attrName, 0).intValue();
    }
}
