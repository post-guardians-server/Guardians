package me.rukon0621.guardians.craft.craft;

import me.rukon0621.callback.LogManager;
import me.rukon0621.guardians.blueprint.BluePrintManager;
import me.rukon0621.guardians.craft.recipes.Recipe;
import me.rukon0621.guardians.craft.recipes.RecipeManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.mailbox.MailBoxManager;
import me.rukon0621.guardians.main;
import me.rukon0621.gui.windows.util.ConfirmWindow;
import me.rukon0621.pay.PaymentData;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

import static me.rukon0621.guardians.main.pfix;

public class CraftManager implements Listener {
    private static final main plugin = main.getPlugin();
    public static Map<String, CraftTable> craftTableData; //제작대 이름에 따른 제작대 객체
    public static Map<Player, CraftTable> playerCraftingTable; //현재 플레이어가 사용중인 제작대
    public static Map<Player, Integer> craftTableYloc; //현재 플레이어의 제작 y좌표
    //제작 슬롯 확인을 위해 사용
    private static final int[] craftEnabledSlot = new int[]{9,10,11,18,19,20,15,16,17,24,25,26};

    //9,10,11,18,19,20,15,16,17,24,25,26,13

    private static ArrayList<Integer> craftEnabledSlotList;

    //모든 craftData를 리로드
    public static void reloadCraftData() {
        craftTableData = new HashMap<>();
        playerCraftingTable = new HashMap<>();
        craftTableYloc = new HashMap<>();
        craftEnabledSlotList = new ArrayList<>();
        for(int i : craftEnabledSlot) {
            craftEnabledSlotList.add(i);
        }

        //기본 제작
        File f = getCraftFile();
        f.mkdir();
        if(f.listFiles()==null) return;
        for(File file : f.listFiles()) {
            craftTableData.put(file.getName().replace(".yml", ""), new CraftTable(new Configure(file),file.getName().replace(".yml", "")));
        }

    }

    //해당 제작대의 configure를 반환
    private static File getCraftFile() {
        return new File(FileUtil.getOuterPluginFolder()+"/craftTables");
    }

    //해당 이름의 제작대가 존재하는지 파악
    private static boolean isExist(String name) {
        return new File(getCraftFile().getPath()+"/"+name+".yml").exists();
    }

    public static void openCraftTable(Player player, String name) {
        craftTableYloc.put(player, 0);
        if(!craftTableData.containsKey(name)) {
            Msg.warn(player, "&c존재하지 않는 작업대를 불러오고 있습니다.");
            return;
        }
        playerCraftingTable.put(player, craftTableData.get(name));
        playerCraftingTable.get(player).openCraftTableGUI(player, 0);
    }

    //새로운 제작대를 만듬
    public static boolean createNewCraftTable(String name) {
        if(isExist(name)) return false;
        Configure config = new Configure(FileUtil.getOuterPluginFolder()+"/craftTables/"+name+".yml");
        config.getConfig().set("lines", 1);
        config.getConfig().set("recipes", new ArrayList<>());
        config.saveConfig();
        return true;
    }

    //기존 제작대를 삭제
    public static boolean deleteCraftTable(String name) {
        if(!isExist(name)) return false;
        Configure config = new Configure(FileUtil.getOuterPluginFolder()+"/craftTables/"+name+".yml");
        config.delete();
        return true;
    }

    private static void addWaitingItem(Player player, WaitingItem item) {
        new PlayerData(player).getWaitingItems().add(item);
    }

    private static void removeWaitingItem(Player player, int index) {
        new PlayerData(player).getWaitingItems().remove(index);
    }

    public static void completeAllWaitingItems(Player player) {
        PlayerData pdc = new PlayerData(player);
        ArrayList<WaitingItem> waitingItems = pdc.getWaitingItems();
        for(WaitingItem item : waitingItems) {
            item.setEndTime(new Date());
        }
    }

    //실제 내부 제작창 GUI (showing이 true면 제작법을 보여주는거)
    public static void craftingGUI(Player player, boolean showing) {
        InvClass inv = new InvClass(5, "&f\uF000\uF015");
        ItemClass it;

        if(showing) {
            inv = new InvClass(5, "&f\uF000\uF014");
            inv = RecipeManager.showRecipe(player, inv);
        } else {
            it = new ItemClass(new ItemStack(Material.SCUTE), "&c제작하기");
            it.setCustomModelData(7);
            for(int i = 38;i<43;i++) {
                inv.setslot(i, it.getItem());
            }
        }
        it = new ItemClass(new ItemStack(Material.SCUTE), "&e\uE002\uE200\uE200&e주재료");
        it.setCustomModelData(7);
        it.addLore("&f아래 6칸에 알맞는 주재료를 넣을 수 있습니다.");
        it.addLore(" ");
        it.addLore("&6주재료 & 부재료 레벨의 평균이 결과물의 레벨입니다.");
        it.addLore("&6주재료의 부가 속성은 결과물에 평균값으로 반영됩니다.");
        it.addLore("&6모든 값은 올림 처리하여 계산합니다.");
        inv.setslot(1, it.getItem());
        it = new ItemClass(new ItemStack(Material.SCUTE), "&e\uE002\uE200\uE200&e부재료");
        it.setCustomModelData(7);
        it.addLore("&f아래 6칸에 알맞는 부재료를 넣을 수 있습니다.");
        it.addLore(" ");
        it.addLore("&6주재료 & 부재료 레벨의 평균이 결과물의 레벨입니다.");
        inv.setslot(7, it.getItem());

        player.openInventory(inv.getInv());
    }

    //EVENTS
    public CraftManager() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reloadCraftData();
    }

    //외부 제작대 클릭
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF013")) return;
        e.setCancelled(true);
        CraftTable table = playerCraftingTable.get(player);
        int y = CraftManager.craftTableYloc.get(player);

        if(e.getRawSlot()==-999) {
            player.closeInventory();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        //페이지 스크롤
        if(e.getRawSlot()==26) {
            if(y!=0) CraftManager.craftTableYloc.put(player, y-1);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.2f);
            table.openCraftTableGUI(player, y);
            return;
        }
        else if (e.getRawSlot()==35) {
            if(y!=table.getHeight()) CraftManager.craftTableYloc.put(player, y+1);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.2f);
            table.openCraftTableGUI(player, y);
            return;
        }

        //제작 대기열 클릭
        if(e.getRawSlot()>=46&&e.getRawSlot()<46+8) {
            if(e.getCurrentItem()==null) return;
            if(e.getCurrentItem().getType().equals(Material.BARRIER)) {
                return;
            }
            //선택된 대기열 아이템 설정
            PlayerData pdc = new PlayerData(player);
            int index = e.getRawSlot() - 46;
            WaitingItem wait = pdc.getWaitingItems().get(index);

            //아이템 회수
            if(wait.getRemainTime()<=0) {
                //여분 공간 확인
                if(!InvClass.hasEnoughSpace(player.getInventory(), wait.getResult())) {
                    Msg.send(player, "&c인벤토리에 공간이 부족합니다.");
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                    return;
                }
                InvClass.giveOrDrop(player, wait.getResult());
                removeWaitingItem(player, index);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,(float) Rand.randDouble(1.2, 1.8));
                table.openCraftTableGUI(player, y);
                return;
            }
            //제작 취소
            else if (e.getClick()==ClickType.SHIFT_RIGHT) {
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1.5f);
                new ConfirmWindow(player) {
                    @Override
                    public void addLoreToExecuteButton(ItemClass it) {
                        it.addLore("&c정말로 제작중이던 아이템을 취소하시겠습니다.");
                        it.addLore("&4제작 재료를 돌려받을 수 없습니다.");
                        it.addLore(" ");
                    }

                    @Override
                    public void execute() {
                        removeWaitingItem(player, index);
                        player.playSound(player, Sound.ITEM_SHIELD_BREAK, 1, 0.8f);
                        Msg.send(player, "&6아이템의 제작을 취소하였습니다.", pfix);
                        table.openCraftTableGUI(player, y);
                    }
                };
                return;
            }
            else if (e.getClick()==ClickType.SHIFT_LEFT) {
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1.5f);
                new ConfirmWindow(player) {
                    @Override
                    public void execute() {
                        int price = wait.getInstantFinishPrice();
                        PaymentData pyd = new PaymentData(player);
                        if(pyd.getRunar() < price) {
                            Msg.warn(player, "루나르가 부족합니다.");
                            return;
                        }
                        pyd.setRunar(pyd.getRunar() - price);
                        wait.setEndTime(new Date(System.currentTimeMillis()));
                        player.playSound(player, Sound.ITEM_TOTEM_USE, 1, 2);
                        Msg.send(player, "&6아이템의 제작을 즉시 완료했습니다.", pfix);
                        table.openCraftTableGUI(player, y);
                    }
                };
            }
            else {
                Msg.send(player, "&c해당 아이템은 아직 제작중인 아이템입니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                return;
            }
        }

        try {
            if(e.getRawSlot()>=0&&e.getRawSlot()<=43) {
                if(e.getCurrentItem()==null) return;
                if(e.getCurrentItem().getType().equals(Material.BARRIER)) return;

                int id = y*8+(e.getRawSlot()/9)*8+(e.getRawSlot()%9);
                //플레이어가 현재 제작하려는 아이템의 레시피
                RecipeManager.playerCrafting.put(player, table.getRecipe(id));
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.4f);
                craftingGUI(player, e.getClick() == ClickType.RIGHT);
            }
        } catch (Exception er) {
            er.printStackTrace();
            Msg.warn(player, "레시피 로딩중 오류가 발견되었습니다.");
        }
        //레시피 선택
    }

    //내부 제작대(제작하기 + 재료 넣기, 빼기) 및 레시피 보기 클릭
    @EventHandler
    public void onInvClickCraftingGUI(InventoryClickEvent e)  {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF014")) { //레시피 보여주는 창
            e.setCancelled(true);
            if (e.getRawSlot() == -999) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.4f);
                playerCraftingTable.get(player).openCraftTableGUI(player, craftTableYloc.get(player));
            }
            return;
        }
        if (!Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF015")) return; //실제 제작창
        e.setCancelled(true);

        CraftTable table = playerCraftingTable.get(player);
        int y = craftTableYloc.get(player);

        if (e.getRawSlot() == -999) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.4f);
            table.openCraftTableGUI(player, y);
        }
        if (e.getCurrentItem() == null) return;

        //현재 플레이어가 작업중인 레시피를 설정
        Recipe recipe = RecipeManager.playerCrafting.get(player);

        //제작버튼 클릭
        if (e.getRawSlot() >= 38 && e.getRawSlot() < 43) {
            onCraft(player, e.getInventory(), recipe);
            BluePrintManager bluePrintManager = main.getPlugin().getBluePrintManager();
            PlayerData pdc = new PlayerData(player);
            for(String s : recipe.getBlueprints()) {
                if(bluePrintManager.getConsumableBlueprintData().containsKey(s)) {
                    if(pdc.getConsumableBlueprintsData().getOrDefault(s, 0) == 0) {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.4f);
                        table.openCraftTableGUI(player, y);
                        return;
                    }
                }
            }
            return;
        }

        //재료 뺴기
        if (craftEnabledSlotList.contains(e.getRawSlot())) {
            player.getInventory().addItem(e.getCurrentItem());
            e.setCurrentItem(new ItemStack(Material.AIR, 0));
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.2f, (float) Rand.randDouble(0.8, 2));
            reloadCraftButton(player, e.getInventory(), recipe);
            return;
        }

        if (main.unusableSlots.contains(e.getRawSlot() + 9)) return;

        //재료 넣기
        ItemStack item = e.getCurrentItem();
        ItemData itemData = new ItemData(e.getCurrentItem());
        if (item.getType().equals(Material.SCUTE)) return;

        if(recipe.getSpecialOptions().contains("process") && itemData.isUnprecessable()) {
            Msg.warn(player, "가공 불가인 아이템은 가공할 수 없습니다.");
            return;
        }
        //아이템이 주재료인지 확인 후 넣기
        int slot = recipe.put(item, player, e.getInventory());
        if(slot==-1) return;
        ItemStack newItem = new ItemStack(item);
        newItem.setAmount(1);
        e.getInventory().setItem(slot, newItem);
        item.setAmount(item.getAmount()-1);
        e.setCurrentItem(item);
        reloadCraftButton(player, e.getInventory(), recipe);
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.2f, (float) Rand.randDouble(0.8, 2));
    }

    /**
     * 플레이어의 제작 창 버튼을 리로드
     * @param player player
     * @param inv inventory
     * @param recipe 레시피
     */
    private void reloadCraftButton(Player player, Inventory inv, Recipe recipe) {
        ItemClass it = new ItemClass(new ItemStack(Material.SCUTE), "&c제작하기");
        it.setCustomModelData(7);
        ResultData resultData = getResult(player, inv, recipe);
        CraftResult result = resultData.getCraftResult();


        if(result.equals(CraftResult.SUCCESS)) {
            PaymentData pdc = new PaymentData(player);
            it.addLore("&6결과물 레벨: " + resultData.getResultItemData().getLevel());
            if(pdc.getRemainOfRukonBlessing() == 0) it.addLore("&6최종 제작 시간: " + DateUtil.formatDate(resultData.getCraftingTime()));
            else {
                it.addLore("&7&m최종 제작 시간: " + DateUtil.formatDate(resultData.getCraftingTime()));
                it.addLore("&e최종 제작 시간: " + DateUtil.formatDate((long) (resultData.getCraftingTime() * 0.8)) + " &7(신의 가호 제작시간 감소 적용중)");
            }
            it.addLore(String.format("&a\uE015\uE00C\uE00C최종 제작 비용: %d 디나르", resultData.getCost()));
            it.addLore(" ");
            it.addLore("&e\uE002\uE200\uE200최종 결과물의 품질에 따라 최종 제작 시간 및 비용은 크게 변동될 수 있습니다.");
            it.addLore("&7결과물의 레벨, 추가 속성등을 고려해 기본 값의 배율로 적용됩니다.");
        }
        else {
            if(result.equals(CraftResult.NOT_FILLED)) {
                it.addLore("&6아직 모든 재료가 채워지지 않았습니다.");
            }
            else if(result.equals(CraftResult.NO_COST)) {
                it.addLore("&6이 아이템을 제작하기 위한 디나르가 부족합니다.");
                it.addLore(" ");
                it.addLore(String.format("&c\uE015\uE00C\uE00C최종 제작 비용: %d 디나르", resultData.getCost()));
            }
            else if(result.equals(CraftResult.NO_SKILL)) {
                it.addLore("&6이 아이템을 제작하기 위한 모든 스킬을 가지고 있지 않습니다.");
            }
            else if(result.equals(CraftResult.NO_PROCESS_TIME)) {
                it.addLore("&6이 아이템은 모든 가공 횟수가 소진되어");
                it.addLore("&6더이상 가공을 진행할 수 없습니다.");
            }
            else if(result.equals(CraftResult.ONLY_ONE_PROCESS)) {
                it.addLore("&6기본 가공은 딱 1회만 진행할 수 있습니다.");
                it.addLore(" ");
                it.addLore("&f\uE002\uE200\uE200기본 가공이란?");
                it.addLore("&7뼈 다듬기, 가죽 건조등 가공 횟수를 증가시키는 가공을 의미합니다.");
            }
        }
        for(int i = 38;i<43;i++) {
            inv.setItem(i, it.getItem());
        }
    }

    /**
     * 결과물 데이터를 반환 (제작시간, 결과 ENUM, 최종 아이템)
     * @param player player
     * @param inv 현재 플레이어의 제작 창
     * @param recipe 레시피
     * @return 결과물 데이터를 반환 (제작시간, 결과 ENUM, 최종 아이템)
     */
    public ResultData getResult(Player player, Inventory inv, Recipe recipe) {
        //결과물의 기본 값을 새로운 아이템으로 설정
        ArrayList<ItemData> allData = new ArrayList<>();
        ArrayList<ItemData> mainMaterData = new ArrayList<>();

        PlayerData pdc = new PlayerData(player);

        //스킬 트리 확인
        for(String skill : recipe.getRequiredSkills()) {
            if(!pdc.hasSkill(skill)) return new ResultData(CraftResult.NO_SKILL);
        }

        //아이템이 모두 채워졌는가
        if(!recipe.isFilled(inv)) return new ResultData(CraftResult.NOT_FILLED);

        for(int slot : craftEnabledSlot) {
            ItemStack item = inv.getItem(slot);
            if(item==null) continue;
            //주재료
            if(slot==9||slot==10||slot==11||slot==18||slot==19||slot==20) {
                mainMaterData.add(new ItemData(new ItemClass(item)));
            }
            //부재료
            allData.add(new ItemData(new ItemClass(item)));
        }

        //특수 옵션 + 아이템 기본 제작 데이터
        int level = 0;

        //아이템의 레벨 계산
        for(ItemData iData : allData) {
            level += iData.getLevel();
        }

        boolean isProcessed = false; //가공 제작인지 확인
        ItemClass result = new ItemClass(new ItemStack(recipe.getResult().getItem()));
        ItemData resultData;
        //가공 (onlyAttributes) 결과물이 존재하지 않음) 첫 주재료가 결과물이 됨
        if(recipe.getSpecialOptions().contains("process")) {
            isProcessed = true;
            resultData = new ItemData(new ItemStack(inv.getItem(9)));
        }
        else resultData = new ItemData(result);

        for(String specialOption : recipe.getSpecialOptions()) {
            if(specialOption.toLowerCase().startsWith("addstartname")) {
                resultData.setName("&f"+specialOption.split(":")[1]+resultData.getName());
            }
            else if(specialOption.toLowerCase().startsWith("changetype")) {
                String[] data = specialOption.split(":");
                if(!specialOption.contains("?")) {
                    resultData.setType(data[1].trim());
                }
                else {
                    String newType = data[1].trim().replaceAll("\\?", "") + resultData.getType();
                    if(TypeData.getType(newType)!=null) resultData.setType(newType);
                    else resultData.setType(data[2].trim());
                }

            }
            else if(specialOption.toLowerCase().startsWith("addprocesstime")) {
                if(resultData.getProcessTime()!=-1) {
                    return new ResultData(CraftResult.ONLY_ONE_PROCESS);
                }
                int processTime = Integer.parseInt(specialOption.split(":")[1].trim());
                resultData.setProcessTime(processTime);
            }
            else if(specialOption.equalsIgnoreCase("removeProcessTime")) {
                if(resultData.getProcessTime()<=0) {
                    return new ResultData(CraftResult.NO_PROCESS_TIME);
                }
                resultData.setProcessTime(resultData.getProcessTime()-1);
            }
        }

        resultData.setLevel(level / (allData.size()));
        if(recipe.getMaxResultLevel()!=-1) {
            if(resultData.getLevel() > recipe.getMaxResultLevel()) resultData.setLevel(recipe.getMaxResultLevel());
        }

        if(resultData.isEquipment()) {
            resultData.setQuality(Rand.randDouble(45, 65));
        }

        //가공시 아이템이 그대로 이전되기에 속성 중첩을 막아야함
        //가공시 가공횟수가 낮은 쪽으로 유지됨
        if(!isProcessed) {
            //아이템 속성 확인 (주재료 속성의 평균(반올림, min 1)값)
            HashMap<String, Integer> attrMap = new HashMap<>();
            int process = 9999;
            for(ItemData itemData : mainMaterData) {
                if(itemData.getProcessTime() != -1) process = Math.min(process, itemData.getProcessTime());


                for(String attr : itemData.getAttrs()) {
                    if(!attrMap.containsKey(attr)) {
                        attrMap.put(attr, itemData.getAttrLevel(attr));
                    }
                    else {
                        attrMap.put(attr, attrMap.get(attr) + itemData.getAttrLevel(attr));
                    }
                }
            }
            int mainMaterSize = mainMaterData.size();
            for(String attr : attrMap.keySet()) {
                int attr_level = Math.max(1, (int) (Math.round((attrMap.get(attr) / (double) mainMaterSize))));
                resultData.addAttr(attr, attr_level);
            }
            if(!resultData.isEquipment() && process != 9999) resultData.setProcessTime(process);
        }

        //레시피 추가속성 계산
        Map<String, Integer> addingAttrMap = recipe.getAddingAttr();
        for(String attr : addingAttrMap.keySet()) {
            resultData.addAttr(attr, addingAttrMap.get(attr));
        }

        //최종 제작 시간 계산
        long time = recipe.getCraftingTime();
        double percent = 100; //기본 제작 시간에서 % 배율
        double finalMultiply = 1; //최종 제작시간에서 배율

        for(String attr : resultData.getAttrs()) {
            int attrLevel = resultData.getAttrLevel(attr);
            if(ItemData.getGoodAttrList().contains(attr)) {
                percent += attrLevel * 30;
            }
            else if(ItemData.getBadAttrList().contains(attr)) {
                percent -= attrLevel * 15;
            }
            else {
                finalMultiply += 1;
            }
        }

        //(제작 결과물 레벨) - (플레이어 레벨 / 2) * 10%
        if(!resultData.isRune()) percent += Math.max((resultData.getLevel() - pdc.getLevel()), -10) * 10;
        else resultData.setLevel(1);
        long cost = (long) (recipe.getCost() * percent / 100);
        if(cost < recipe.getCost() / 2) cost = recipe.getCost() / 2;

        if(pdc.getMoney() < cost) {
            return new ResultData(CraftResult.NO_COST, cost);
        }
        if(percent <= 70) percent = 70;
        time = (long) Math.max(((time * percent / 100) * finalMultiply * (100-pdc.getCraftSpeed()) / 100.0), recipe.getCraftingTime() * 0.05);

        resultData.setCraftLevel(resultData.getLevel());
        return new ResultData(resultData, CraftResult.SUCCESS, time, cost);
    }

    /**
     *
     * @param player player
     * @param inv inventory
     * @param recipe recipe
     */
    public void onCraft(Player player, Inventory inv, Recipe recipe) {

        ResultData resultData = getResult(player, inv, recipe);
        CraftResult craftResult = resultData.getCraftResult();

        PlayerData pdc = new PlayerData(player);

        if(!craftResult.equals(CraftResult.SUCCESS)) {
            if(craftResult.equals(CraftResult.NO_SKILL)) {
                Msg.warn(player, "&c이 아이템을 제작하기 위해 배워야할 스킬을 모두 배우지 않았습니다.");
                for(String skillName : recipe.getRequiredSkills()) {
                    if(pdc.hasSkill(skillName)) continue;
                    Msg.send(player, "   &7 - "+skillName);
                }
            }
            else if (craftResult.equals(CraftResult.NOT_FILLED)) {
                Msg.warn(player, "아이템이 모두 채워지지 않았습니다.");
            }
            else if (craftResult.equals(CraftResult.NO_COST)) {
                Msg.warn(player, "아이템을 제작하기 위한 돈이 부족합니다.");
            }
            else if (craftResult.equals(CraftResult.ONLY_ONE_PROCESS)) {
                Msg.warn(player, "가공 횟수는 딱 한 번 추가될 수 있습니다. 무한정 가공할 수 없습니다.");
            }
            else if (craftResult.equals(CraftResult.NO_PROCESS_TIME)) {
                Msg.warn(player, "아이템의 가공 가능 횟수가 남아있지 않아 가공할 수 없습니다.");
            }
            return;
        }

        ItemClass result = resultData.getResultItem();

        //즉시제작이라면 인벤토리 확인
        if(recipe.getCraftingTime()==0) {
            //인벤토리가 꽉 찼는지 확인
            if(!InvClass.hasEnoughSpace(inv, result.getItem())) {
                Msg.send(player, "&c인벤토리에 공간이 부족합니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                return;
            }
        }
        else {
            int line = pdc.getCraftLineSize(false);
            //아이템 대기열 확인
            if(recipe.getCraftingTime()!=0&&line==pdc.getWaitingItems().size()) {
                Msg.send(player, "&c제작 대기열이 꽉 차 아이템을 제작할 수 없습니다.", pfix);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                return;
            }
        }

        //아이템 제거
        for(int slot : craftEnabledSlot) {
            inv.setItem(slot, new ItemStack(Material.AIR,0));
        }

        //비용 계산
        pdc.setMoney(pdc.getMoney() - resultData.getCost());

        //일회성 청사진 제거
        BluePrintManager manager = main.getPlugin().getBluePrintManager();
        for(String bp : recipe.getBlueprints()) {
            if(manager.getConsumableBlueprintData().containsKey(bp)) {
                pdc.removeConsumableBlueprint(bp);
            }
        }

        //최종 결과 도출
        if(recipe.getCraftingTime()==0) { //즉시 제작
            if(!MailBoxManager.giveOrMail(player, result.getItem())) {
                Msg.warn(player, "인벤토리 공간이 부족해 아이템이 메일로 전송되었습니다.");
            }
            player.playSound(player, Sound.BLOCK_SMITHING_TABLE_USE, 1, (float) Rand.randDouble(0.8, 1.5));
            return;
        }
        else {
            //제작 대기열 추가, 제작 시간 감소 반영
            if(new PaymentData(player).getRemainOfRukonBlessing()>0) addWaitingItem(player, new WaitingItem(result, (long) (resultData.getCraftingTime() * 0.8)));
            else addWaitingItem(player, new WaitingItem(result, resultData.getCraftingTime()));
            Msg.send(player, "&e아이템 제작이 시작되었습니다. 제작이 끝나면 가져가세요.", pfix);
        }

        player.playSound(player, Sound.BLOCK_SMITHING_TABLE_USE, 1, (float) Rand.randDouble(0.8, 1.5));
        //Logging
        LogManager.log(player, "craft", resultData.getResultItemData().toString());
    }

    //제작중 인벤토리를 닫으면 아이템 회수
    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if(!(e.getPlayer() instanceof Player player)) return;
        if(!Msg.recolor(e.getView().getTitle()).equals("&f\uF000\uF015")) return;
        List<ItemStack> items = new ArrayList<>();
        for(int i : craftEnabledSlot) {
            ItemStack item = e.getInventory().getItem(i);
            if(item==null) continue;
            items.add(item);
        }
        MailBoxManager.giveAllOrMailAll(player, items);
    }

}
