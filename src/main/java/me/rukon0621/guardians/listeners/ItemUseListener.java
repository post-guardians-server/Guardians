package me.rukon0621.guardians.listeners;

import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.GUI.crafting.CraftAcceleratingWindow;
import me.rukon0621.guardians.GUI.item.QualityUpgradeWindow;
import me.rukon0621.guardians.GUI.item.SuccessionGUI;
import me.rukon0621.guardians.areawarp.Area;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.events.ItemClickEvent;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.guardians.skillsystem.skilltree.SkillTreeManager;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.gui.windows.util.ConfirmWindow;
import me.rukon0621.pay.PaymentData;
import me.rukon0621.rinstance.RukonInstance;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

import static me.rukon0621.guardians.main.getPlugin;
import static me.rukon0621.guardians.main.pfix;

public class ItemUseListener implements Listener {
    private final Set<Player> onReturning = new HashSet<>();

    public ItemUseListener() {
        main plugin = main.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onUseItem(ItemClickEvent e) {
        Player player = e.getPlayer();
        ItemData data = e.getItemData();
        if(!TypeData.getType(data.getType()).isMaterialOf("특수")) return;
        String type = e.getItemData().getType();
        String name = Msg.uncolor(e.getItemData().getName());
        String finalName = name;
        TypeData typeData = TypeData.getType(type);
        if(typeData == null) return;
        if (typeData.isMaterialOf("치장 아이템")) {
            String s = getCosmeticConfig().getConfig().getString(name);
            if (s == null) {
                Msg.warn(player, "이 치장은 등록되지 않았습니다.");
                return;
            }
            if(player.hasPermission("cosmeticscore.user.cosmetics.wear." + s)) {
                Msg.warn(player, "이미 가지고 있는 치장 아이템입니다.");
                return;
            }
            new ConfirmWindow(player) {
                @Override
                public void execute() {
                    if(!e.consume()) return;
                    main.getPlugin().getServer().dispatchCommand(main.getPlugin().getServer().getConsoleSender(), "lp user " + player.getName() + " perm set cosmeticscore.user.cosmetics.seeingui." + s);
                    main.getPlugin().getServer().dispatchCommand(main.getPlugin().getServer().getConsoleSender(), "lp user " + player.getName() + " perm set cosmeticscore.user.cosmetics.wear." + s);
                    Msg.send(player, "&e치장 아이템을 사용했습니다. 이제 루테티아의 캐쉬 상점에서 치장품을 장착할 수 있습니다!", pfix);
                    player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);
                    LogManager.log(player, "현질소모품", finalName);
                }
            };
        }
        else if (typeData.isMaterialOf("무기 스킨")) {
            PlayerData pdc = new PlayerData(player);
            name = Msg.uncolor(Msg.recolor(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName()));
            for(ItemStack item : pdc.getWeaponSkins()) {
                String skinName = Msg.uncolor(Msg.recolor(item.getItemMeta().getDisplayName()));
                if(skinName.equals(name)) {
                    Msg.warn(player, "이미 가지고 있는 무기 스킨입니다.");
                    return;
                }
            }
            new ConfirmWindow(player) {
                @Override
                public void execute() {
                    ItemStack it = e.getItemData().getItemStack().clone();
                    if(!e.consume()) return;
                    pdc.getWeaponSkins().add(it);
                    Msg.send(player,"무기 스킨을 성공적으로 등록했습니다! &e메뉴 -> 무기스킨&f에서 장착하실 수 있습니다.", pfix);
                    player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.8f);
                }
            };
        }
        else {
            switch (type) {
                case "가디언 패스" -> {
                    PaymentData pdc = new PaymentData(player);
                    if(pdc.getPremiumPassReward() >= 0) {
                        Msg.warn(player, "이미 프리미엄 가디언 패스를 가지고 있습니다.");
                        return;
                    }
                    pdc.setPremiumPassReward(0);
                    player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
                    Msg.send(player, "프리미엄 패스를 획득했습니다.", pfix);
                    e.consume();
                    LogManager.log(player, "현질소모품", name);
                }
                case "신의 가호" -> { //??신의 가호 3일
                    int day = Integer.parseInt(name.split(" ")[2].replaceAll("일", ""));
                    new ConfirmWindow(player) {
                        @Override
                        public void execute() {
                            if(!e.consume()) return;
                            PaymentData pdc = new PaymentData(player);
                            if (finalName.startsWith("작댁")) {
                                if(pdc.getRemainOfJackBlessing() > 86000L * 1000 * 50) {
                                    Msg.warn(player, "한 종류의 가호가 50일 이상 지속될 수 없습니다.");
                                    return;
                                }
                                if(pdc.getRemainOfJackBlessing() <= 0) {
                                    PlayerData pd = new PlayerData(player);
                                    pd.setUnlearnChance(pd.getUnlearnChance() + 3);
                                }
                                pdc.setBlessOfJack(Math.max(System.currentTimeMillis(), pdc.getBlessOfJack()) + (day * 86400000L));
                            }
                            else if (finalName.startsWith("루콘")) {
                                if(pdc.getRemainOfRukonBlessing() > 86000L * 1000 * 50) {
                                    Msg.warn(player, "한 종류의 가호가 50일 이상 지속될 수 없습니다.");
                                    return;
                                }
                                pdc.setBlessOfRukon(Math.max(System.currentTimeMillis(), pdc.getBlessOfRukon()) + (day * 86400000L));
                            }
                            else if (finalName.startsWith("버트")) {
                                if(pdc.getRemainOfBertBlessing() > 86000L * 1000 * 50) {
                                    Msg.warn(player, "한 종류의 가호가 50일 이상 지속될 수 없습니다.");
                                    return;
                                }
                                PlayerData pd = new PlayerData(player);
                                if(pd.getMaxEnergyCore() < 130) {
                                    pd.setEnergyCore(pd.getEnergyCore() + 30);
                                }
                                pdc.setBlessOfBert(Math.max(System.currentTimeMillis(), pdc.getBlessOfBert()) + (day * 86400000L));
                            }
                            Msg.send(player, "&a신의 가호가 당신과 함께합니다.", pfix);
                            player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, 1, 0.8f);
                            player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 0.7f);
                            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.5f);
                            LogManager.log(player, "가호구매", finalName);
                        }
                    };
                }
                case "스킬 취소 포인트" -> { //스킬 취소 포인트: 1
                    int point = Integer.parseInt(name.split(": ")[1]);

                    PlayerData pdc = new PlayerData(player);

                    if(pdc.getUnlearnChance() > 20) {
                        Msg.warn(player, "이미 가진 스킬 취소 포인트가 너무 많습니다.");
                        return;
                    }

                    pdc.setUnlearnChance(pdc.getUnlearnChance() + point);
                    e.consume();
                    Msg.send(player, "&e스킬 취소 포인트를 획득했습니다.", pfix);
                    player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.5f);
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
                    LogManager.log(player, "현질소모품", name);
                }
                case "스킬 초기화권" -> { //스킬 트리 초기화, 무기스킬 초기화, 제작스킬 초기화
                    PlayerData pdc = new PlayerData(player);
                    if(name.startsWith("무기")) {
                        SkillTreeManager manager = main.getPlugin().getSkillTreeManager();
                        pdc.getSkillData().removeIf(s -> manager.getSkillTree(s).getTreeIndex() <= 5);
                        Msg.send(player, "&e무기 스킬트리가 모두 초기화 되었습니다!", pfix);
                    }
                    else if(name.startsWith("제작")) {
                        SkillTreeManager manager = main.getPlugin().getSkillTreeManager();
                        pdc.getSkillData().removeIf(s -> manager.getSkillTree(s).getTreeIndex() == 7);
                        Msg.send(player, "&e제작 스킬트리가 모두 초기화 되었습니다!", pfix);
                    }
                    else if(name.startsWith("체술")) {
                        SkillTreeManager manager = main.getPlugin().getSkillTreeManager();
                        pdc.getSkillData().removeIf(s -> manager.getSkillTree(s).getTreeIndex() == 6);
                        Msg.send(player, "&e체술 스킬트리가 모두 초기화 되었습니다!", pfix);
                    }
                    else {
                        pdc.getSkillData().clear();
                        Msg.send(player, "&e스킬트리가 초기화 되었습니다!", pfix);
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            SkillManager.reloadPlayerSkill(player);
                        }
                    }.runTaskAsynchronously(getPlugin());
                    e.consume();
                    player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.5f);
                    player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0.8f);
                    LogManager.log(player, "현질소모품", name);
                }
                case "계승의 보주" -> { //스킬 트리 초기화, 무기스킬 초기화, 제작스킬 초기화
                    ItemData itemData = e.getItemData();
                    String itemName = Msg.uncolor(itemData.getName());
                    int decQuality;
                    int decEnhance;

                /*
                if(itemName.equals("D티어 계승의 보주")) {
                    decQuality = 30;
                    decEnhance = 4;
                }
                else if(itemName.equals("C티어 계승의 보주")) {
                    decQuality = 25;
                    decEnhance = 3;
                }
                else if(itemName.equals("B티어 계승의 보주")) {
                    decQuality = 20;
                    decEnhance = 3;
                }
                else if(itemName.equals("A티어 계승의 보주")) {
                    decQuality = 15;
                    decEnhance = 2;
                }
                //E티어
                else {
                    decQuality = 35;
                    decEnhance = 5;
                }

                 */
                    decQuality = 15;
                    decEnhance = 2;
                    //new SuccessionGUI(player, itemData.getItemStack(), decQuality, decEnhance);
                }
                case "품질 강화서" -> { //스킬 트리 초기화, 무기스킬 초기화, 제작스킬 초기화
                    ItemData itemData = e.getItemData();
                    String itemName = Msg.uncolor(itemData.getName());
                    Couple<Double, Double> range;
                    if(itemName.equals("고급 품질 강화서")) {
                        range = new Couple<>(-2.5D, 5D);
                    }
                    else if (itemName.equals("최고급 품질 강화서")) {
                        range = new Couple<>(0D, 5D);
                    }
                    else {
                        range = new Couple<>(-1D, 2D);
                    }
                    new QualityUpgradeWindow(player, itemData.getItemStack(), range);
                    player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 0.8f);
                }
                case "품질 변경서" -> { //스킬 트리 초기화, 무기스킬 초기화, 제작스킬 초기화
                    ItemData itemData = e.getItemData();
                    String itemName = Msg.uncolor(itemData.getName());
                    Msg.warn(player, "사용할 수 없는 아이템입니다.", pfix);
                    return;
                /*
                new QualityRandomWindow(player, itemData.getItemStack(), itemName.equals("고급 품질 변경서"));
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 0.8f);

                 */
                }
                case "에너지 코어" -> { //스킬 트리 초기화, 무기스킬 초기화, 제작스킬 초기화
                    PlayerData pdc = new PlayerData(player);
                    if(pdc.getEnergyCore() >= pdc.getMaxEnergyCore()) {
                        Msg.warn(player, "이미 에너지 코어가 가득 찼습니다.");
                        return;
                    }
                    ItemData itemData = e.getItemData();
                    double value = itemData.getValue();
                    pdc.setEnergyCore((int) (pdc.getEnergyCore() + value));
                    e.consume();
                    player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.5f);
                    Msg.send(player, "성공적으로 에너지 코어를 회복했습니다.", pfix);
                    LogManager.log(player, "현질소모품", name + "(" + e.getItemData().getValue() + ")");
                }
                case "피로회복제" -> {
                    PlayerData pdc = new PlayerData(player);
                    ItemData itemData = e.getItemData();
                    int value = (int) itemData.getValue();
                    if(pdc.getFatigue() - value < 0) {
                        Msg.warn(player, "이 아이템을 사용하면 피로 수치가 0 미만이 되기에 사용할 수 없습니다.");
                        return;
                    }
                    e.consume();
                    pdc.setFatigue(pdc.getFatigue() - value);
                    player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.5f);
                    Msg.send(player, String.format("피로 회복제를 사용해 피로도가 감소했습니다. &7(현재 피로도: &7(&f%d &7/ &f%d&7)", pdc.getFatigue(), pdc.getMaxFatigue()), pfix);
                    LogManager.log(player, "현질소모품", name + "(" + e.getItemData().getValue() + ")");
                }
                case "제작 가속기" -> { //스킬 트리 초기화, 무기스킬 초기화, 제작스킬 초기화
                    if(new PlayerData(player).getWaitingItems().isEmpty()) {
                        Msg.warn(player, "현재 제작 대기열에 아이템이 올라와 있지 않습니다.");
                        return;
                    }

                    new CraftAcceleratingWindow(player, e.getItemData().getItemStack(), (int) e.getItemData().getValue());
                    player.playSound(player, Sound.ITEM_ARMOR_EQUIP_GOLD, 1, 0.8f);
                    LogManager.log(player, "현질소모품", name + "(" + e.getItemData().getValue() + ")");
                }
                case "귀환석" -> {
                    if(DamagingListener.getRemainCombatTime(player) != -1) {
                        Msg.warn(player, "전투 중에는 귀환할 수 없습니다.");
                        return;
                    }
                    if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(player)||onReturning.contains(player)||StoryManager.getPlayingStory(player)!=null) {
                        Msg.warn(player, "지금은 귀환석을 사용할 수 없습니다.");
                        return;
                    }
                    String areaName = name.split(" ")[0].trim();
                    Area area = AreaManger.getArea(areaName);
                    if(area==null) {
                        Msg.warn(player, "존재하지 않는 에리어로 귀환할 수 없습니다.");
                        return;
                    }
                    if(!new PlayerData(player).getArea().equals(areaName)) {
                        Msg.warn(player, "다른 섬에서 해당 섬으로 귀환할 수 없습니다.");
                        return;
                    }
                    Msg.send(player, "&e5초 후&7에 해당 섬으로 귀환합니다. &c움직이면 귀환이 취소됩니다. 게임에서 나가면 귀환석이 증발하니 주의하십시오.", pfix);
                    player.playSound(player, Sound.ITEM_ARMOR_EQUIP_GOLD, 1, 0.8f);
                    ItemStack item = player.getInventory().getItemInMainHand().clone();
                    Location originalLocation = player.getLocation();
                    item.setAmount(1);
                    e.consume();
                    PotionManager.effectGive(player, PotionEffectType.SLOW, 100, 5);
                    onReturning.add(player);
                    new Timer(20, 1, 5) {

                        private boolean isUnmovable() {
                            return !originalLocation.getWorld().equals(player.getWorld()) || !(originalLocation.distanceSquared(player.getLocation()) < 0.5 * 0.5);
                        }

                        @Override
                        public void onEnd() {
                            if(isUnmovable()) {
                                Msg.warn(player, "자리에서 움직여 귀환이 취소되었습니다.");
                                MailBoxManager.giveOrMail(player, item, true);
                                return;
                            }

                            player.teleport(area.getReturnLoc());
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    player.spawnParticle(Particle.END_ROD, player.getLocation().add(0D, 1D ,0D), 8, 0.3, 0.3, 0.3, 0.7);
                                    player.playSound(player, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1, 1.3f);
                                    onReturning.remove(player);
                                }
                            }.runTaskLater(main.getPlugin(), 1);
                        }

                        @Override
                        public void execute() {
                            if(getRepeatNumber()%4==0) {
                                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                            }
                            if(!player.isOnline()) cancel();
                            else if(isUnmovable()) {
                                Msg.warn(player, "자리에서 움직여 귀환이 취소되었습니다.");
                                MailBoxManager.giveOrMail(player, item, true);
                                cancel();
                            }
                        }

                        @Override
                        public synchronized void cancel() throws IllegalStateException {
                            super.cancel();
                            PotionManager.effectRemove(player, PotionEffectType.SLOW);
                            onReturning.remove(player);
                        }
                    };
                }
                case "전리품 가방 확장권" -> {
                    int amount = (int) e.getItemData().getValue();
                    PlayerData pdc = new PlayerData(player);
                    if(pdc.getBackpackSlot() >= PlayerData.MAX_BACKPACK_SLOT) {
                        Msg.warn(player, "이미 전리품 가방 칸이 최대치에 도달했습니다.");
                        return;
                    }
                    int remain = 0;
                    if(pdc.getBackpackSlot() + amount > PlayerData.MAX_BACKPACK_SLOT) {
                        remain = pdc.getBackpackSlot() + amount - PlayerData.MAX_BACKPACK_SLOT;
                        pdc.setBackpackSlot(PlayerData.MAX_BACKPACK_SLOT);
                    }
                    else pdc.setBackpackSlot(pdc.getBackpackSlot() + amount);

                    Msg.send(player, String.format("&e전리품 가방이 확장되었습니다! &f현재 전리품 가방 칸: &e%d &7/ &c%d", pdc.getBackpackSlot(), PlayerData.MAX_BACKPACK_SLOT), pfix);
                    player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1.3f);
                    if(remain > 0) {
                        Msg.warn(player, String.format("전리품 가방이 최대치에 도달하여 초과된 %d칸의 확장권을 돌려받았습니다.", remain));
                        ItemStack item = player.getInventory().getItemInMainHand().clone();
                        item.setAmount(1);
                        ItemData itemData = new ItemData(item);
                        itemData.setValue(remain);
                        MailBoxManager.giveOrMail(player, itemData.getItemStack());
                    }
                    e.consume();
                    LogManager.log(player, "현질소모품", name);
                }
            }
        }
    }

    private Configure getCosmeticConfig() {
        return new Configure(FileUtil.getOuterPluginFolder().getPath() + "/cosmetic.yml");
    }
}
