package me.rukon0621.guardians.GUI.item.enhance;

public class LegacyEnhanceGUI { //extends SingleEquipmentSelectWindow {
    /*

    private enum FAIL_STATUS {
        NO_EQUIPMENT("장비를 넣어주세요.") {
            @Override
            boolean isUnpassed(LegacyEnhanceGUI window) {
                return window.selectedEquipment == null;
            }
        },
        IS_MAX_LEVEL("이미 장비가 최대 레벨입니다.") {
            @Override
            boolean isUnpassed(LegacyEnhanceGUI window) {
                return window.selectedItemData.getEnhanceLevel().isMaxLevel();
            }
        },
        NO_MONEY("강화에 필요한 디나르가 부족합니다.") {
            @Override
            boolean isUnpassed(LegacyEnhanceGUI window) {
                return new PlayerData(window.player).getMoney() < window.selectedItemData.getEnhanceLevel().upgrade().getMoney();
            }
        },
        NO_ITEM("강화에 필요한 강화석이 부족합니다. 강화에 필요한 재료들의 레벨은 반드시 장비의 요구레벨 이상이 되어야합니다.") {
            @Override
            boolean isUnpassed(LegacyEnhanceGUI window) {
                Player player = window.player;
                EnhanceLevel level = window.selectedItemData.getEnhanceLevel().upgrade();

                int amount = 0;
                int reqLev = window.selectedItemData.getRequiredLevel();
                List<ItemStack> levelSorted = new ArrayList<>();
                List<ItemStack> selStones = new ArrayList<>();


                //storgate content가 hot bar를 감지하는가 (?)
                for(ItemStack item : player.getInventory().getStorageContents()) {
                    if(item == null) continue;
                    if(!item.hasItemMeta()) continue;
                    if(!item.getItemMeta().hasDisplayName()) continue;
                    if(!item.getItemMeta().getDisplayName().contains("강화석")) continue;
                    levelSorted.add(item);
                }

                //레벨 작은 순서대로 검증
                levelSorted.sort(Comparator.comparingInt(o -> new ItemData(o).getLevel()));
                for(ItemStack item : levelSorted) {
                    ItemData id = new ItemData(item);
                    if(id.getLevel() < reqLev) continue;
                    amount += id.getAmount();
                    selStones.add(item);
                    assert level != null;
                    if(amount >= level.getRequiredStone()) {
                        window.selectedStones = selStones;
                        return false;
                    }
                }

                return true;
            }
        },
        NO_EXTRA_ITEM("강화에 필요한 추가 재료가 부족합니다.") {
            @Override
            boolean isUnpassed(LegacyEnhanceGUI window) {
                Player player = window.player;
                EnhanceLevel level = window.selectedItemData.getEnhanceLevel().upgrade();
                assert level != null;
                if(level.getExtraItem() == null) return false;
                int amount = 0;
                String[] data = level.getExtraItem().split(":");
                String extraItemName = data[0].trim();
                int reqAmount = Integer.parseInt(data[1].trim());

                List<ItemStack> selItem = new ArrayList<>();

                //storgate content가 hot bar를 감지하는가 (?)
                for(ItemStack item : player.getInventory().getStorageContents()) {
                    if(item == null) continue;
                    if(!item.hasItemMeta()) continue;
                    if(!item.getItemMeta().hasDisplayName()) continue;
                    if(!item.getItemMeta().getDisplayName().contains(extraItemName)) continue;
                    amount += item.getAmount();
                    selItem.add(item);
                    if(amount >= reqAmount) {
                        window.selectedExtraItems = selItem;
                        return false;
                    }
                }
                return true;
            }
        },
        ;

        private final String msg;

        FAIL_STATUS(String msg) {
            this.msg = msg;
        }

        //true일시 조건을 만족하지 않는가
        abstract boolean isUnpassed(LegacyEnhanceGUI window);

        public String getMsg() {
            return msg;
        }
    }

    protected ItemData selectedItemData = null;
    protected List<ItemStack> selectedStones = new ArrayList<>();
    protected List<ItemStack> selectedExtraItems = new ArrayList<>();
    private FAIL_STATUS failStatus = FAIL_STATUS.NO_EQUIPMENT;

    public LegacyEnhanceGUI(Player player) {
        super(player, "&f\uF000\uF03C", 3);


        Button executeButton = new Button() {
            @Override
            public void execute(Player player, ClickType clickType) {
                if(failStatus != null) {
                    Msg.warn(player, failStatus.getMsg());
                    return;
                }
                //아이템 회수
                EnhanceLevel level = selectedItemData.getEnhanceLevel().upgrade();
                if(level == null) {
                    Msg.warn(player, "강화 도중 알 수 없는 오류가 발생되었습니다.");
                    return;
                }
                int errorItemAmount = takeDetectedItem(selectedStones, level.getRequiredStone());
                if(errorItemAmount > 0) {
                    Msg.send(player, "Error: " + errorItemAmount + " - 오류가 발생했습니다. 이 메세지를 찍어서 운영진에게 문의하십시오.");
                    return;
                }

                if(level.getExtraItem() != null) {
                    errorItemAmount = takeDetectedItem(selectedExtraItems, Integer.parseInt(level.getExtraItem().split(":")[1].trim()));
                    if(errorItemAmount > 0) {
                        Msg.send(player, "SpecError: " + errorItemAmount + " - 오류가 발생했습니다. 이 메세지를 찍어서 운영진에게 문의하십시오.");
                        return;
                    }
                }

                PlayerData pdc = new PlayerData(player);
                pdc.setMoney(pdc.getMoney() - level.getMoney());

                //강화 진행
                double rand = Rand.randDouble(0, 100);
                if(rand < level.getChance()) {
                    //강화 성공
                    EquipmentButton button = (EquipmentButton) map.get(getItemSlot());
                    ItemData id = selectedItemData;
                    id.setEnhanceLevel(id.getEnhanceLevel().upgrade());
                    button.setOriginalItem(ItemSaver.reloadItem(id.getItemStack()));
                    Msg.send(player, "&a강화를 성공했습니다!", pfix);
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
                }
                else if (level.getBigFailChance() > 0 && rand < (level.getBigFailChance() + level.getChance())) {
                    //대실패
                    EquipmentButton button = (EquipmentButton) map.get(getItemSlot());
                    ItemData id = selectedItemData;
                    id.setEnhanceLevel(EnhanceLevel.ZERO);
                    id.setQuality(Math.max(0, id.getQuality() - 50));
                    button.setOriginalItem(id.getItemStack());
                    Msg.send(player, "&c강화 대실패로 인해 강화 수치가 0이 되고 품질이 50 감소하였습니다.", pfix);
                    player.playSound(player, Sound.ENTITY_WITHER_DEATH, 1, 1.5f);
                }
                else {
                    // 강화 수치 감소
                    if(Rand.chanceOf(level.getFailDownChance())) {
                        EquipmentButton button = (EquipmentButton) map.get(getItemSlot());
                        ItemData id = selectedItemData;
                        id.setEnhanceLevel(EnhanceLevel.values()[id.getEnhanceLevel().ordinal() - 1]);
                        button.setOriginalItem(id.getItemStack());
                        Msg.send(player, "강화에 실패하여 강화 수치가 떨어졌습니다.", pfix);
                        player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1, 1.5f);
                    }
                    else {
                        Msg.send(player, "강화에 실패했습니다.", pfix);
                        player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1, 1.3f);
                    }
                }
                reloadGUI();
            }

            private static int takeDetectedItem(List<ItemStack> items, int amount) {
                for(ItemStack item : items) {
                    if(amount > item.getAmount()) {
                        amount -= item.getAmount();
                        item.setAmount(0);
                    }
                    else { //remain <= detectingItem
                        item.setAmount(item.getAmount() - amount);
                        return 0;
                    }
                }
                return amount;
            }

            @Override
            public ItemStack getIcon() {
                ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&c[ 강화 진행 ]");
                it.setCustomModelData(7);

                if(selectedEquipment == null) {
                    it.addLore("&c강화를 진행할 장비를 넣어주세요.");
                    return it.getItem();
                }
                else if(selectedItemData.getEnhanceLevel().isMaxLevel()) {
                    it.addLore("&c이 장비는 이미 최고의 장비입니다.");
                    return it.getItem();
                }

                EnhanceLevel level = selectedItemData.getEnhanceLevel().upgrade();
                it.addLore("#bbffbb\uE011\uE00C\uE00C필요 비용: " + level.getMoney() + "디나르");
                it.addLore("#eeeeaa\uE011\uE00C\uE00C필요 강화석: " + level.getRequiredStone() + "개");

                if(level.getExtraItem() != null) {
                    String[] data = level.getExtraItem().split(":");
                    it.addLore("&f");
                    it.addLore("&7────── 필요 아이템 ──────");
                    it.addLore("&7- " + data[0].trim() + " x" + data[1].trim());
                }
                it.addLore("&f");
                it.addLore(String.format("#bbccff\uE011\uE00C\uE00C강화 성공 확률: %.2f%%", level.getChance()));
                it.addLore(String.format("#ffaa33\uE011\uE00C\uE00C강화 실패시 절반의 확률로 강화 수치가 1 내려갑니다."));
                if(level.getBigFailChance() > 0) {
                    it.addLore(String.format("#ff7722\uE011\uE00C\uE00C강화 대실패 확률: %.2f%%", level.getBigFailChance()));
                    it.addLore("&7  \uE00C\uE00C- 대실패시 품질이 떨어지고 강화 단계가 내려갑니다.");
                }

                if(failStatus != null) {
                    it.addLore("&f");
                    for(String s : failStatus.getMsg().split("\\. ")) {
                        if(!s.endsWith(".")) {
                            s += ".";
                        }
                        it.addLore("&c" + s);
                    }
                }
                return it.getItem();
            }
        };
        map.put(21, executeButton);
        map.put(22, executeButton);
        map.put(23, executeButton);

        reloadGUI();
        open();
    }

    @Override
    public int getItemSlot() {
        return 4;
    }

    @Override
    protected void reloadGUI() {
        if(selectedEquipment == null) {
            selectedItemData = null;
        }
        else selectedItemData = new ItemData(selectedEquipment);
        map.put(13, new Icon() {
            @Override
            public ItemStack getIcon() {
                if(selectedEquipment == null) {
                    return new ItemClass(new ItemStack(Material.BARRIER), "&c장비를 넣어주세요.").getItem();
                }
                ItemData id = new ItemData(selectedEquipment.clone());
                EnhanceLevel level = id.getEnhanceLevel();
                if(level.isMaxLevel()) {
                    ItemClass it = new ItemClass(new ItemStack(Material.BARRIER), "&c이미 최대 레벨입니다.");
                    it.addLore("&f축하합니다! 말이 안되는 군요..!");
                    return it.getItem();
                }
                id.setEnhanceLevel(level.upgrade());
                ItemClass it = new ItemClass(ItemSaver.reloadItem(id.getItemStack()), "&c강화 성공시 스텟");
                return it.getItem();
            }
        });
        failStatus = null;
        for(FAIL_STATUS status : FAIL_STATUS.values()) {
            if(status.isUnpassed(this)) {
                failStatus = status;
                break;
            }
        }
        super.reloadGUI();
    }

     */

}
