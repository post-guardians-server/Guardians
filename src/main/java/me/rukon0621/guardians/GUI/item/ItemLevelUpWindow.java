package me.rukon0621.guardians.GUI.item;

import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Rand;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.gui.buttons.Button;
import me.rukon0621.pay.RukonPayment;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.rukon0621.guardians.data.LevelData.EQUIPMENT_EXP_BOOK_TYPE_NAME;
import static me.rukon0621.guardians.data.LevelData.maxLevel;
import static me.rukon0621.guardians.main.pfix;

public class ItemLevelUpWindow extends SingleEquipmentSelectWindow {

    private static final int SEASON = RukonPayment.inst().getPassManager().getSeason();

    enum STATUS {
        POSSIBLE,
        NO_EQUIPMENT,
        MAX_LEVEL,
        NO_BOOK,
        LOW_TOTEM_LEVEL,
        LOW_QUALITY,
        SEASON_OVER,
        LEVEL_OVER
    }

    enum TOTEM {
        PROTECT_BOOK_TOTEM("강화의 서 보호 토템"),
        PROTECT_QUALITY_TOTEM("품질 유지 토템"),
        CHANCE_UP_TOTEM("강화 확률 증가 토템");

        final String typeName;

        TOTEM(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }

        /**
         *
         * @param window window
         * @param item 집어넣을 아이템
         * @return 해당 GUI에 넣을 토템과 중복되는 토템이 있는지 반환
         */
        public static boolean canPutItem(ItemLevelUpWindow window, ItemStack item) {
            ItemData itemData = new ItemData(item);
            for(int slot : SUPPORTER_SLOTS) {
                if(!window.map.containsKey(slot)) continue;
                ItemData target = new ItemData(window.getItemButton(slot).getIcon());
                if(target.getType().equals(itemData.getType())) return false;
            }
            return true;
        }

        /**
         * @param type 타입
         * @return 해당 타입에 알맞는 TOTEM ENUM 반환
         */
        @Nullable
        public static TOTEM getTotemByType(String type) {
            for(TOTEM totem : TOTEM.values()) {
                if(totem.typeName.equals(type)) return totem;
            }
            return null;
        }

        /**
         *
         * @param item item
         * @return 해당 아이템이 어떤 종류의 토템인지 반환
         */
        @Nullable
        public static TOTEM getTotemByItem(ItemData item) {
            for(TOTEM totem : TOTEM.values()) {
                if(item.getType().equals(totem.getTypeName())) return totem;
            }
            return null;
        }

        @Nullable
        public static TOTEM getTotemByItem(ItemStack item) {
            return getTotemByItem(new ItemData(item));
        }

        /**
         * @param window window
         * @param totem 토템 종류
         * @return 해당 토템이 인벤토리 어디 슬롯에 있는지 반환, 없으면 -1 반환
         */
        public static int getSlotOfTotem(ItemLevelUpWindow window, TOTEM totem) {
            for(int slot : SUPPORTER_SLOTS) {
                if(!window.map.containsKey(slot)) return -1;
                TOTEM target = getTotemByItem(window.getItemButton(slot).getIcon());
                if(target==null) continue;
                if(target.equals(totem)) return slot;
            }
            return -1;
        }

        /**
         *
         * @param window window
         * @return 현재 GUI 중 각 토템이 각각 어떤 슬롯에 있는지 맵을 반환
         */
        public static Map<TOTEM, Integer> getTotemSlotMap(ItemLevelUpWindow window) {
            Map<TOTEM, Integer> map = new HashMap<>();
            for(TOTEM totem : TOTEM.values()) {
                int slot = TOTEM.getSlotOfTotem(window, totem);
                if(slot!=-1) map.put(totem, slot);
            }
            return map;
        }
    }

    private static final int EQUIPMENT_SLOT = 11;
    private static final int[] SUPPORTER_SLOTS = {8, 17, 26};
    private static final int[] BOOK_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    private STATUS window_status = STATUS.NO_EQUIPMENT;

    private static final String SUPPORTER_TYPE_NAME = "강화 보조 토템";
    private static final double CHANCE_POWER = 0.25;
    private static final double LOWEST_CHANCE = 5; //만렙 직전 최소 강화 확률

    private static double g(int level) {
        return (100 - LOWEST_CHANCE) * Math.pow(-level + maxLevel - 1, CHANCE_POWER);
    }

    /**
     *
     * @param window window
     * @param level level
     * @return 강화 확률 증가 토템을 포함한 강화 성공 확률을 계산
     */
    private static double calculateSuccessChance(ItemLevelUpWindow window, int level) {
        double chance = g(level) * ((100 - LOWEST_CHANCE) / g(0)) + LOWEST_CHANCE;

        //강화 성공 확률 증가 토템
        int chanceUpSlot = TOTEM.getSlotOfTotem(window, TOTEM.CHANCE_UP_TOTEM);
        if(chanceUpSlot != -1) {
            ItemData itemData = new ItemData(window.getItemButton(chanceUpSlot).getIcon());
            chance += itemData.getValue();
        }
        return Math.min(100, chance);
    }

    public ItemLevelUpWindow(Player player) {
        super(player, "&f\uF000\uF01C", 4);

        map.put(15, new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(window_status.equals(STATUS.NO_EQUIPMENT)) {
                    Msg.warn(player, "레벨업을 진행할 장비를 선택해주세요.");
                }
                else if (window_status.equals(STATUS.NO_BOOK)) {
                    Msg.warn(player, "레벨업에 사용할 강화의 서를 선택해주세요.");
                }
                else if (window_status.equals(STATUS.LOW_TOTEM_LEVEL)) {
                    Msg.warn(player, "강화 보조 토템의 레벨이 장비보다 낮으면 사용할 수 없습니다.");
                }
                else if (window_status.equals(STATUS.LEVEL_OVER)) {
                    Msg.warn(player, "장비의 레벨이 플레이어의 레벨보다 높아질 수 없습니다.");
                }
                else if (window_status.equals(STATUS.LOW_QUALITY)) {
                    Msg.warn(player, "장비의 품질이 5% 이하면 강화할 수 없습니다.");
                }
                else if (window_status.equals(STATUS.MAX_LEVEL)) {
                    Msg.warn(player, "현재 달성 가능한 최대레벨에 도달한 장비입니다. 장비의 레벨은 플레이어의 레벨보다 높아질 수 없습니다.");
                }
                else {

                    Map<TOTEM, Integer> totemMap = TOTEM.getTotemSlotMap(ItemLevelUpWindow.this);

                    ItemData itemData = new ItemData(inv.getSlot(EQUIPMENT_SLOT).clone());

                    //레벨업 진행
                    //boolean success = Rand.chanceOf(calculateSuccessChance(ItemLevelUpWindow.this, itemData.getLevel()));
                    boolean success = true;

                    if(!itemData.hasKey("craftLevel")) {
                        itemData.setCraftLevel(itemData.getCraftLevel());
                    }

                    int level = itemData.getLevel();
                    long remainExp = itemData.addExp(getExpInFirst(), totemMap.containsKey(TOTEM.PROTECT_QUALITY_TOTEM), new PlayerData(player).getLevel());
                    int levelAfter = itemData.getLevel();

                    if(totemMap.containsKey(TOTEM.PROTECT_QUALITY_TOTEM) && (level + 1) != levelAfter) {
                        Msg.warn(player, "&c아이템이 레벨업 하지 않으면 품질 유지 토템을 사용할 필요가 없습니다. 또한 반드시 한 번에 1레벨만 올라가야 합니다.");
                        return;
                    }

                    //실패시 강화서 보존
                    if(!success) {
                        if(totemMap.containsKey(TOTEM.PROTECT_BOOK_TOTEM)) {
                            player.playSound(player, Sound.ITEM_TOTEM_USE, 0.75f, 1.5f);
                            Msg.send(player, "&e강화에 실패했지만 토템에 의해 강화의 서가 보존되었습니다!", pfix);
                        }
                        else {
                            player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1, 0.8f);
                            Msg.send(player, "&c강화에 실패했습니다!", pfix);
                            getItemButton(BOOK_SLOTS[0]).removeButton();
                        }
                        reloadGUI();
                    }
                    else {
                        getItemButton(BOOK_SLOTS[0]).removeButton();
                        if(remainExp > 0) {
                            Msg.warn(player, "장비가 최고 레벨을 달성하여 남은 경험치를 다시 지급 받았습니다. " + String.format("&7(반환된 경험치: %d)", remainExp));
                            MailBoxManager.giveOrMail(player, LevelData.getEquipmentExpBook(remainExp));
                        }
                        //업그레이드 성공 GUI 반영 (setOriginalItem 에 reloadGUI 포함)
                        if(level==levelAfter) {
                            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2.0f);
                            Msg.send(player, "장비 경험치 부여를 성공했습니다!", pfix);
                        }
                        else {
                            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
                            Msg.send(player, "&e장비가 레벨업 되었습니다!", pfix);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    String s = itemData.toString();
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            LogManager.log(player, "equipmentLevelUp","장비 강화" + s);
                                        }
                                    }.runTask(main.getPlugin());
                                }
                            }.runTaskAsynchronously(main.getPlugin());
                        }
                    }
                    for(int slot : SUPPORTER_SLOTS) {
                        map.remove(slot);
                    }

                    if(success) {
                        EquipmentButton button = (EquipmentButton) map.get(EQUIPMENT_SLOT);
                        button.setOriginalItem(itemData.getItemStack());
                    }
                    else reloadGUI();
                }
            }

            @Override
            public ItemStack getIcon() {
                ItemClass icon = new ItemClass(new ItemStack(Material.SCUTE), "&c[ 강화 진행하기 ]");
                icon.setCustomModelData(7);
                //reload
                if(!map.containsKey(EQUIPMENT_SLOT)) {
                    window_status = STATUS.NO_EQUIPMENT;
                    icon.addLore("&6강화를 진행할 장비를 넣어주세요.");
                }
                else if (!map.containsKey(BOOK_SLOTS[0])) {
                    window_status = STATUS.NO_BOOK;
                    ItemData itemData = new ItemData(selectedEquipment.clone());
                    icon.addLore("&6강화에 사용할 강화의 서를 넣어주세요.");
                    icon.addLore(" ");
                    icon.addLore("&f현재 선택된 장비: "+ itemData.getName());
                    icon.addLore(" ");
                    icon.addLore("&7레벨: "+itemData.getLevel());
                    icon.addLore(String.format("&7경험치: %.2f / %.2f (%.2f%%)", itemData.getExp(), itemData.getMaxExp(), itemData.getExpPercentage()));
                }
                else {
                    ItemData itemData = new ItemData(selectedEquipment.clone());

                    for(int slot : SUPPORTER_SLOTS) {
                        if(!map.containsKey(slot)) break;
                        ItemData totem = new ItemData(getItemButton(slot).getIcon());
                        if(totem.getLevel() >= itemData.getLevel()) continue;
                        icon.addLore("&c강화 보조 토템의 레벨이 장비보다 낮으면 사용할 수 없습니다.");
                        window_status = STATUS.LOW_TOTEM_LEVEL;
                        return icon.getItem();
                    }
                    PlayerData pdc = new PlayerData(player);
                    if(itemData.getLevel() >= pdc.getLevel()) {
                        window_status = STATUS.MAX_LEVEL;
                        icon.addLore("&c선택된 장비는 현재 달성 가능한 최대 레벨을 달성했습니다.");
                    }
                    else if (itemData.getQuality() <= 5) {
                        icon.addLore("&c장비의 품질이 5% 이하면 강화할 수 없습니다.");
                    }

                    else {
                        window_status = STATUS.POSSIBLE;
                        icon.addLore("&f클릭하면 올려진 강화의 서를 하나씩 사용합니다.");
                        icon.addLore(" ");

                        long exp = getExpInFirst();

                        icon.addLore("&f현재 선택된 장비: "+ itemData.getName());
                        icon.addLore(" ");
                        icon.addLore("&7레벨: "+itemData.getLevel());
                        icon.addLore(String.format("&7경험치: %.2f / %.2f (%.2f%%)", itemData.getExp(), itemData.getMaxExp(), itemData.getExpPercentage()));

                        //경험치 추가 후 추후 결과를 계산
                        long remain = itemData.addExp(exp, true, new PlayerData(player).getLevel());

                        icon.addLore(" ");
                        icon.addLore("&7 ======= ↓ &f강화 결과 &7↓ ======= ");
                        icon.addLore(" ");
                        icon.addLore("&7레벨: "+itemData.getLevel());
                        icon.addLore(String.format("&7경험치: %.2f / %.2f (%.2f%%)", itemData.getExp(), itemData.getMaxExp(), itemData.getExpPercentage()));
                        //icon.addLore(" ");
                        //icon.addLore(String.format("&b\uE011\uE00C\uE00C경험치 부여 성공 확률: %.2f%%", calculateSuccessChance(ItemLevelUpWindow.this, itemData.getLevel())));
                        if(itemData.getLevel() > new PlayerData(player).getLevel()) {
                            window_status = STATUS.LEVEL_OVER;
                            icon.addLore(" ");
                            icon.addLore("&4\uE014\uE00C\uE00C장비의 레벨이 플레이어의 레벨보다 높아질 수 없습니다.");
                        }
                        else if(remain > 0) {
                            icon.addLore(" ");
                            icon.addLore("&e\uE011\uE00C\uE00C장비가 현재 가능한 최대 레벨에 도달합니다. 남은 경험치는 반환됩니다.");
                        }
                    }
                }
                return icon.getItem();
            }
        });
        reloadGUI();
        open();
    }

    private long getExpInFirst() {
        return LevelData.getExpOfBook(map.get(BOOK_SLOTS[0]).getIcon());
    }

    private QueuedItemButton getItemButton(int slot) {
        return (QueuedItemButton) map.get(slot);
    }

    @Override
    public void select(int i) {
        ItemData itemData = new ItemData(player.getOpenInventory().getItem(i));
        if(TypeData.getType(itemData.getType()).isMaterialOf("룬")) {
            return;
        }

        /*
        강화 아이템 넣기
        경험치 책, 강화의 서 선택 -> 경험치 책
        강화 아이템의 하위 속성인가 -> 강화 아이템
         */
        if(itemData.getType().equals(EQUIPMENT_EXP_BOOK_TYPE_NAME)||TypeData.getType(itemData.getType()).isMaterialOf(SUPPORTER_TYPE_NAME)) {
            boolean isSupportSlot = !itemData.getType().equals(EQUIPMENT_EXP_BOOK_TYPE_NAME);
            ItemStack item = player.getOpenInventory().getItem(i);
            int slot;
            if(isSupportSlot) {

                if(itemData.getSeason() != SEASON) {
                    Msg.warn(player, "유효 시즌이 지난 아이템을 사용할 수 없습니다.");
                }

                if(!TOTEM.canPutItem(this, item)) {
                    Msg.warn(player, "같은 종류의 토템을 중복해서 넣을 수 없습니다.");
                    return;
                }
                slot = getRemainSlot(SUPPORTER_SLOTS);
            }
            else slot = getRemainSlot(BOOK_SLOTS);

            if(slot==-1) {
                Msg.warn(player, "더이상 아이템을 넣을 수 없습니다.");
                return;
            }

            ItemStack newItem = item.clone();
            newItem.setAmount(1);
            item.setAmount(item.getAmount() - 1);
            map.put(slot, new QueuedItemButton(newItem, slot, isSupportSlot, isSupportSlot ? 9 : 1));
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 0.5f);
            reloadGUI();
            return;
        }

        //장비 선택
        super.select(i);
    }

    /**
     * @param slots 아이템이 들어갈 슬롯들
     * @return 아이템이 들어갈 수 있는 공간을 반환, 공간이 없으면 -1 반환
     */
    private int getRemainSlot(int[] slots) {
        for(int slot : slots) {
            if(inv.getSlot(slot)!=null) continue;
            return slot;
        }
        return -1;
    }

    @Override
    public int getItemSlot() {
        return EQUIPMENT_SLOT;
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) player.closeInventory();
        List<ItemStack> remain = new ArrayList<>();
        List<Integer> slots = new ArrayList<>();
        slots.add(EQUIPMENT_SLOT);
        for(int slot : BOOK_SLOTS) {
            slots.add(slot);
        }
        for(int slot : SUPPORTER_SLOTS) {
            slots.add(slot);
        }
        for(int slot : slots) {
            if(inv.getSlot(slot)==null) continue;
            remain.add(inv.getSlot(slot));
        }
        MailBoxManager.giveAllOrMailAll(player, remain);
    }

    private class QueuedItemButton extends SelectedItemButton {
        final boolean isSupportItem;
        final int slotDifference;

        protected QueuedItemButton(ItemStack item, int slot, boolean isSupportItem, int slotDifference) {
            super(item, slot);
            this.isSupportItem = isSupportItem;
            this.slotDifference = slotDifference;
        }

        /**
         * 아이템을 삭제하고 뒤에 있는 아이템을 앞으로 당김
         * @param player player
         * @param clickType ClickType
         */
        @Override
        public void execute(Player player, ClickType clickType) {
            removeButton();
            reloadGUI();
            MailBoxManager.giveOrMail(player, originalItem);
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 0.8f);
        }

        public void removeButton() {
            map.remove(slot);
            int lastSlot;
            if(isSupportItem) lastSlot = SUPPORTER_SLOTS[SUPPORTER_SLOTS.length - 1];
            else lastSlot = BOOK_SLOTS[BOOK_SLOTS.length - 1];

            for(int cnt = slot + 1; cnt <= lastSlot; cnt += slotDifference) {
                if(map.get(cnt) instanceof QueuedItemButton button) {
                    button.push();
                }
                else break;
            }
        }

        private void push() {
            setSlot(slot - slotDifference);
        }

        @Override
        public ItemStack getIcon() {
            return originalItem;
        }
    }
}
