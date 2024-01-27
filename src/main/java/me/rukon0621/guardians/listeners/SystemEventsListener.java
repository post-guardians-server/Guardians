package me.rukon0621.guardians.listeners;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.nisovin.magicspells.events.ConditionsLoadingEvent;
import com.nisovin.magicspells.events.PassiveListenersLoadingEvent;
import com.nisovin.magicspells.events.SpellEffectsLoadingEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import me.rukon0621.guardians.GUI.MenuWindow;
import me.rukon0621.guardians.GUI.TrashcanGUI;
import me.rukon0621.guardians.addspells.effect.MessageEffect;
import me.rukon0621.guardians.addspells.effect.RandomSoundEffect;
import me.rukon0621.guardians.addspells.modifier.SkillTreeModifier;
import me.rukon0621.guardians.addspells.triggers.GiveGuardiansDamageListener;
import me.rukon0621.guardians.addspells.triggers.GuardiansDamageListener;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.LevelData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.TypeData;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.events.ItemClickEvent;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.PotionManager;
import me.rukon0621.guardians.helper.Rand;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.region.Region;
import me.rukon0621.guardians.region.RegionManager;
import me.rukon0621.guardians.story.StoryManager;
import me.rukon0621.pay.RukonPayment;
import me.rukon0621.ridings.RideManager;
import me.rukon0621.ridings.RukonRiding;
import me.rukon0621.rinstance.RukonInstance;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static me.rukon0621.guardians.commands.MoneyCommand.moneyName;
import static me.rukon0621.guardians.data.LevelData.EXP_BOOK_TYPE_NAME;
import static me.rukon0621.guardians.data.LevelData.reloadIndicator;
import static me.rukon0621.guardians.main.pfix;

public class SystemEventsListener implements Listener {
    private static final main plugin = main.getPlugin();
    private final Set<Player> blockRiding = new HashSet<>();
    private final Set<Player> interacting = new HashSet<>();
    private final Set<String> blockMenu = new HashSet<>();

    public SystemEventsListener() {
        main.getPlugin().getServer().getPluginManager().registerEvents(this, main.getPlugin());
        new BukkitRunnable() {
            @Override
            public void run() {
                interacting.clear();
            }
        }.runTaskTimerAsynchronously(plugin, 0, 600);
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        e.setCancelled(true);

        if(StoryManager.getPlayingStory(player) != null) {
            Msg.warn(player, "지금은 메뉴를 사용할 수 없습니다.");
            return;
        }

        RideManager rideManager = RukonRiding.inst().getRideManager();
        if(player.isSneaking()) {
            if(PlayerData.isPlayerStunned(player)) {
                Msg.warn(player, "지금은 아무 행동도 할 수 없습니다.", pfix);
                return;
            }
            for(Region region : RegionManager.getRegionsOfPlayer(player)) {
                if(region.getSpecialOptions().contains("blockMenu")) {
                    Msg.warn(player, "지금은 메뉴를 사용할 수 없습니다.", pfix);
                    return;
                }
            }
            if(rideManager.isPlayerRiding(player)) {
                rideManager.despawnRiding(player);
                return;
            }


            if(LogInOutListener.getLogoutEventBlocked().contains(player)) {
                new MenuWindow(player);
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, Rand.randFloat(1, 1.5));
            }
            else {
                LogInOutListener.addLogoutBlock(player);
                CountDownLatch latch = new CountDownLatch(LogInOutListener.dataCategories);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        LogInOutListener.saveAllDataToDB(player, latch);
                        try {
                            latch.await();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }
                        LogInOutListener.getLogoutEventBlocked().remove(player);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                new MenuWindow(player);
                                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 1, Rand.randFloat(1, 1.5));
                            }
                        }.runTask(main.getPlugin());
                    }
                }.runTaskAsynchronously(main.getPlugin());
            }
            return;
        }
        try {
            if(blockRiding.contains(player)) return;
            blockRiding.add(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    blockRiding.remove(player);
                }
            }.runTaskLater(plugin, 10);
            if(rideManager.isPlayerRiding(player)) {
                rideManager.despawnRiding(player);
            }
            else {
                ItemStack itemStack = EquipmentManager.getItem(player, "라이딩");
                if(itemStack.hasItemMeta()) {
                    if(player.isSprinting()) {
                        for(Region region : RegionManager.getRegionsOfPlayer(player)) {
                            if(region.getSpecialOptions().contains("blockRiding")) {
                                Msg.warn(player, "이곳에서는 라이딩이 금지되어 있습니다.");
                                return;
                            }
                        }
                        if(PlayerData.isPlayerStunned(player)) {
                            Msg.warn(player, "지금은 라이딩을 소환할 수 없습니다.");
                            return;
                        }
                        if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(player)) {
                            Msg.warn(player, "지금은 라이딩을 소환할 수 없습니다.");
                            return;
                        }
                        if(DamagingListener.getRemainCombatTime(player)!=-1) {
                            Msg.warn(player, String.format("전투가 완전히 해제되어야 라이딩을 소환할 수 있습니다. &7(전투 종료까지 %.1f초 남았습니다.)", DamagingListener.getRemainCombatTime(player)));
                            return;
                        }
                        String name = Msg.uncolor(itemStack.getItemMeta().getDisplayName()).split(":")[1].trim();
                        ItemData itemData = new ItemData(itemStack);
                        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        rideManager.summonRiding(player, name, itemData.getType().equals("라이딩"));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                reloadIndicator(player);
                            }
                        }.runTaskLater(plugin, 4);
                    }
                }
            }
        } catch (Exception er) {
            er.printStackTrace();
            Msg.warn(player, "여기에서 라이딩을 소환할 수 없습니다. 다시 시도해주세요.");
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if(player.getGameMode()== GameMode.CREATIVE) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if(player.getGameMode()== GameMode.CREATIVE) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent e) {
        if(e.getEntity() instanceof Player player) {
            if(player.getGameMode().equals(GameMode.CREATIVE)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if(player.getGameMode()==GameMode.CREATIVE) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if(!ResourcePackListener.acceptResourcePack(player)) {
            e.setCancelled(true);
            Msg.warn(player, "이 서버를 플레이하려면 반드시 서버 리소스팩을 사용해주셔야 합니다.");
            return;
        }

        if(!(PlayerData.isPlayerPerfectlyStunned(player))) {
            if(player.getGameMode().equals(GameMode.SPECTATOR)) {
                if(player.isOp()) return;
                if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(player)) return;
                e.setCancelled(true);
            }
            return;
        }
        if(player.getGameMode().equals(GameMode.SPECTATOR)||player.getGameMode().equals(GameMode.CREATIVE)) {
            e.setCancelled(true);
            return;
        }
        Location loc = player.getLocation();
        loc.setY(loc.getY()-0.01);
        if(player.getWorld().getBlockAt(loc).getType()== Material.AIR) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent e) {
        if(!(e.getAction().equals(Action.RIGHT_CLICK_AIR)||e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;
        if(interacting.contains(e.getPlayer())) return;
        interacting.add(e.getPlayer());
        new BukkitRunnable() {
            @Override
            public void run() {
                interacting.remove(e.getPlayer());
            }
        }.runTaskLater(plugin, 5);

        if(e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK)) {
            e.getPlayer().openBook(e.getPlayer().getInventory().getItemInMainHand());
            e.setCancelled(true);
            return;
        }

        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if(item.getType().equals(Material.AIR)) return;
        if(!item.hasItemMeta()) return;
        ItemData itemData = new ItemData(item);
        /*
        if(itemData.getSeason()!=RukonPayment.inst().getPassManager().getSeason()) {
            Msg.warn(e.getPlayer(), "이 아이템은 유효 시즌이 지난 아이템입니다.");
            return;
        }
         */
        if(itemData.getType()==null||itemData.getType().equals("null")||itemData.getName()==null) return;
        if(TypeData.getType(itemData.getType())==null) return;
        plugin.getServer().getPluginManager().callEvent(new ItemClickEvent(e.getPlayer(), itemData));
    }

    @EventHandler
    public void onRightClickExpBook(ItemClickEvent e) {
        ItemData itemData = e.getItemData();
        if(!itemData.getType().equals(EXP_BOOK_TYPE_NAME)) return;
        Player player = e.getPlayer();
        long exp = LevelData.getExpOfBook(e.getItemData().getName());
        LevelData.addExp(player, exp);
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
        Msg.send(player, "&f경험의 저서를 사용하여 &e"+exp+" 경험치&f를 획득하였습니다.", pfix);
        e.consume();
    }

    @EventHandler
    public void onRightClickMoney(ItemClickEvent e) {
        ItemData itemData = e.getItemData();
        String name = Msg.uncolor(itemData.getName());
        if(!itemData.getType().equals("디나르")) return;
        Player player = e.getPlayer();
        int money = Integer.parseInt(name.split(" ")[0]);
        PlayerData pdc = new PlayerData(player);
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
        Msg.send(player, "&f수표를 사용하여 &b"+money+" "+moneyName+"&f를 획득하였습니다.", pfix);
        pdc.setMoney(pdc.getMoney()+money);
        e.consume();
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player player)) return;
        //if(e.getClick().equals(ClickType.SWAP_OFFHAND) && e.getHotbarButton() < 0) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(e.getAction().equals(InventoryAction.DROP_ALL_CURSOR)||e.getAction().equals(InventoryAction.DROP_ALL_SLOT)||e.getAction().equals(InventoryAction.DROP_ONE_CURSOR)||e.getAction().equals(InventoryAction.DROP_ONE_SLOT)) {
            e.setCancelled(true);
            return;
        }
        if(e.getHotbarButton()>=0&&e.getHotbarButton()<=3) {
            e.setCancelled(true);
            return;
        }
        if(!e.getView().getTitle().equals("Crafting")) return;
        if(e.getRawSlot()==36) {
            if(!EquipmentManager.getEquipment(player, "무기").getType().equals(Material.AIR)) {
                e.setCancelled(true);
            }
            return;
        }
        if(!(e.getRawSlot()==37||e.getRawSlot()==38||e.getRawSlot()==39)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreakHangingItem(HangingBreakByEntityEvent e) {
        if(!(e.getRemover() instanceof Player player)) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreakHangingItem(HangingBreakEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(player.getGameMode()==GameMode.CREATIVE) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void onInteract(EntityInteractEvent e) {
        if(e.getEntity() instanceof Player) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if(e.getPlayer().isOp()) return;
        if(e.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if(e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 99999999, 120, false, false, false));
        if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(player)) return;
        RegionManager.getPlayerRegion(player).clear();
        PlayerData pdc = new PlayerData(player);
        Location loc = AreaManger.getArea(pdc.getArea()).getReturnLoc();
        e.setRespawnLocation(loc);
        player.teleport(loc);
        new BukkitRunnable() {
            @Override
            public void run() {
                BarManager.reloadBar(player);
            }
        }.runTaskLater(plugin, 1);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0.8f);
                Msg.send(player, "&7아쉽게 사망하셨군요.. ㅠㅠ 조금 더 강해져야겠어요!", pfix);
                ArrayList<String> msg = new ArrayList<>();
                if(pdc.getLevel() < 5) {
                    msg.add("&e포션을 구매해보시는 건 어떨까요?");
                    msg.add("&e포션을 구매해보시는 건 어떨까요?");
                    msg.add("&e스킬트리에서 새로운 스킬을 배워보는 건 어떨까요?");
                }
                msg.add("&e대장간에서 더 좋은 장비를 만들어보는 건 어떨까요?");
                msg.add("&e대장간에서 장비를 레벨업 해보는 건 어떨까요?");
                msg.add("&e스킬트리에서 새로운 스킬을 배워보는 건 어떨까요?");
                msg.add("&e친구와 함께 파티를 맺고 사냥해보는 건 어떨까요?");
                Msg.send(player,msg.get(Rand.randInt(0, msg.size()-1)), pfix);
            }
        }.runTaskLater(plugin, 100);
    }

    @EventHandler
    public void onMythicMobSpawn(MythicMobSpawnEvent e) {
        Location loc = e.getLocation();
        if(loc.getBlock().getType().equals(Material.AIR)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onSpectateStart(PlayerStartSpectatingEntityEvent e) {
        if(e.getPlayer().isOp()) return;
        if(e.getNewSpectatorTarget().getType().equals(EntityType.PLAYER)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Hanging) {
            if(e.getDamager() instanceof Player player) {
                if(player.getGameMode()==GameMode.CREATIVE) return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        e.setCancelled(true);
    }

    /*스토리 0 독서대 우클릭
    @EventHandler
    public void onInteractToLectern(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Player player = e.getPlayer();
        if(PlayerData.isPlayerStunned(player)) return;
        Location loc = new Location(player.getWorld(), -2016, 0, -4);
        if (!player.getWorld().getBlockAt(loc).equals(e.getClickedBlock())) return;
        e.setCancelled(true);
        PlayerData pdc = new PlayerData(player);
        if (pdc.getStoryCode() != 0) return;
        PlayerData.setPlayerStun(player, true);
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1.5f, 0.5f);
        PotionManager.effectGive(player, PotionEffectType.SLOW, 10, 5);
        OpenAudioListener.stopSong(player, "bgm");
        StoryManager.readStory(player, "reset");
    }
     */

    @EventHandler
    public void onLoadSpellEffects(SpellEffectsLoadingEvent e) {
        e.getSpellEffectManager().addSpellEffect("randomSound", RandomSoundEffect.class);
        e.getSpellEffectManager().addSpellEffect("message", MessageEffect.class);
    }

    @EventHandler
    public void onLoadSpellConditions(ConditionsLoadingEvent e) {
        e.getConditionManager().addCondition("hasSkill", SkillTreeModifier.class);
    }

    @EventHandler
    public void onLoadSpellPassive(PassiveListenersLoadingEvent e) {
        e.getPassiveManager().addListener(GuardiansDamageListener.class, "takeGuardiansDamage");
        e.getPassiveManager().addListener(GiveGuardiansDamageListener.class, "giveGuardiansDamage");
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if(e.getPlayer().getInventory().getHeldItemSlot() == 0 && !EquipmentManager.getWeapon(e.getPlayer()).getType().equals(Material.AIR)) {
            return;
        }
        Player player = e.getPlayer();
        if(DamagingListener.getRemainCombatTime(player) > 0) {
            Msg.warn(player, "전투 중에는 아이템을 버릴 수 없습니다.");
            return;
        }
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 1.3f);
        new TrashcanGUI(player);
    }

}
