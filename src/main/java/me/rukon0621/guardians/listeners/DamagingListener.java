package me.rukon0621.guardians.listeners;

import com.nisovin.magicspells.events.MagicSpellsEntityRegainHealthEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.rukon0621.buff.BuffManager;
import me.rukon0621.buff.RukonBuff;
import me.rukon0621.buff.data.Buff;
import me.rukon0621.dungeonwave.RukonWave;
import me.rukon0621.dungeonwave.wave.WaveManager;
import me.rukon0621.guardians.areawarp.AreaManger;
import me.rukon0621.guardians.bar.BarManager;
import me.rukon0621.guardians.data.ItemData;
import me.rukon0621.guardians.data.MobData;
import me.rukon0621.guardians.data.PlayerData;
import me.rukon0621.guardians.data.Stat;
import me.rukon0621.guardians.dialogquest.DialogQuestManager;
import me.rukon0621.guardians.dropItem.DropManager;
import me.rukon0621.guardians.equipment.EquipmentManager;
import me.rukon0621.guardians.events.GuardiansDamageEvent;
import me.rukon0621.guardians.helper.*;
import me.rukon0621.guardians.main;
import me.rukon0621.guardians.mobType.MobTypeManager;
import me.rukon0621.guardians.region.Region;
import me.rukon0621.guardians.region.RegionManager;
import me.rukon0621.guardians.skillsystem.SkillManager;
import me.rukon0621.guild.RukonGuild;
import me.rukon0621.ridings.RideManager;
import me.rukon0621.ridings.RukonRiding;
import me.rukon0621.rinstance.RukonInstance;
import me.rukon0621.rpvp.RukonPVP;
import me.rukon0621.teseion.TeseionInstance;
import net.playavalon.avnparty.AvNParty;
import net.playavalon.avnparty.player.AvalonPlayer;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static me.rukon0621.guardians.main.pfix;

public class DamagingListener implements Listener {

    private static final main plugin = main.getPlugin();
    private static final HashMap<UUID, Long> playersOnCombat = new HashMap<>();
    private static final Set<Player> playingCombatBgm = new HashSet<>();
    public static final Set<Player> deathVictims = new HashSet<>();
    private static final int dropCombatSecond = 15; //몇초후 전투가 풀리는가
    private static final AvNParty partyPlugin = AvNParty.plugin;
    private final MobTypeManager mobTypeManager = main.getPlugin().getMobTypeManager();

    private static final double DEATH_PENALTY_ATTACK = -75;
    private static final double DEATH_PENALTY_ARMOR = -75;
    private static final double DEATH_PENALTY_MOVEMENT = -50;
    private static final double DEATH_PENALTY_LUCK = -50;

    public static void clearCombatTime(Player player) {
        playersOnCombat.remove(player);
    }

    public DamagingListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    MobData mb = new MobData(player);
                    mb.optimize();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 600);
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    if(playingCombatBgm.contains(player)&&getRemainCombatTime(player)==-1) {
                        RegionManager.reloadBgm(player);
                        playingCombatBgm.remove(player);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 60);
    }

    /**
     * 플레이어의 무장해제까지 남은 시간
     * @param player 플레이어
     * @return 플레이어가 전투종료까지 남은 시간을 반환, 비전투중이라면 -1반환
     */
    public static double getRemainCombatTime(Player player) {
        return getRemainCombatTime(player.getUniqueId());
    }
    /**
     * 플레이어의 무장해제까지 남은 시간
     * @param uuid 플레이어 UUID
     * @return 플레이어가 전투종료까지 남은 시간을 반환, 비전투중이라면 -1반환
     */
    public static double getRemainCombatTime(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if(player != null && player.getGameMode() == GameMode.CREATIVE) return -1;
        if(playersOnCombat.containsKey(uuid)) {
            if(playersOnCombat.get(uuid)>System.currentTimeMillis()) {
                return Math.max((playersOnCombat.get(uuid) - System.currentTimeMillis()) / 1000.0, -1);
            }
        }
        return -1;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent e) {
        if(e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelled(true);
            return;
        }
        if(e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            if(e.getEntity() instanceof LivingEntity le) {
                e.setDamage(e.getDamage() + le.getMaxHealth() * 0.2);
            }
        }
        else if(e.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onHealEntity(MagicSpellsEntityRegainHealthEvent e) {
        if(!(e.getEntity() instanceof Player player)) return;
        BarManager.reloadBar(player, e.getAmount() * -1);
    }

    //매직스펠 타겟팅 관리
    @EventHandler(ignoreCancelled = true)
    public void onSpellTarget(SpellTargetEvent e) {
        if(e.getCaster().equals(e.getTarget())) return;
        else if(e.getTarget() instanceof ArmorStand) {
            e.setCancelled(true);
            return;
        }
        if(e.getCaster() instanceof Player caster) {
            //웨이브몹은 해당 웨이브를 플레이하는 사람만 타격 가능
            Entity entity = e.getTarget();
            WaveManager waveManager = RukonWave.inst().getWaveManager();
            if(waveManager.isWaveMob(entity)) {
                if(e.getTarget().getLocation().getBlock().getType().equals(Material.WATER)) {
                    Msg.warn(caster, "물에 빠진 적은 타격할 수 없습니다.");
                    e.setCancelled(true);
                    return;
                }

                Optional<ActiveMob> opMob = MythicBukkit.inst().getMobManager().getActiveMob(e.getTarget().getUniqueId());
                if(opMob.isPresent()) {
                    ActiveMob mob = opMob.get();
                    if(mob.getParent().isPresent()) {
                        if(!waveManager.getFieldWaveOfEntity(BukkitAdapter.adapt(mob.getParent().get())).getSurvivors().contains(caster)) {
                            e.setCancelled(true);
                            Msg.warn(caster, "필드 웨이브에 참여하지 않아 몬스터 사냥에 동참할 수 없습니다.");
                            return;
                        }
                    }

                }

                if(!waveManager.getFieldWaveOfEntity(e.getTarget()).getSurvivors().contains(caster)) {
                    e.setCancelled(true);
                    Msg.warn(caster, "필드 웨이브에 참여하지 않아 몬스터 사냥에 동참할 수 없습니다.");
                    return;
                }
            }
            if(e.getTarget() instanceof Player target) {
                //가상 플레이어 스킬 타겟팅 불가
                if(plugin.getServer().getPlayer(target.getName())==null) {
                    e.setCancelled(true);
                    return;
                }


                //Check PVP
                //PVP 비활성화 (타겟을 기준으로)

                if(RukonPVP.inst().getPvpManager().isPlayerInBattleInstance(caster)) return;

                //팀원 타겟 방지
                AvalonPlayer avnTarget = partyPlugin.getAvalonPlayer(target);
                AvalonPlayer avnCaster = partyPlugin.getAvalonPlayer(caster);
                if(avnCaster.getParty()!=null&&avnTarget.getParty()!=null) {
                    if(avnTarget.getParty().equals(avnCaster.getParty())) {
                        e.setCancelled(true);
                        return;
                    }
                }

                PlayerData pdc = new PlayerData(caster);
                PlayerData tdc = new PlayerData(target);
                if(pdc.getGuildID() != null && tdc.getGuildID() != null && pdc.getGuildID().equals(tdc.getGuildID())) {
                    if(!RukonGuild.inst().getGuildManager().getGuildsUsingFF().contains(pdc.getGuildID())) {
                        e.setCancelled(true);
                        return;
                    }
                }


                for(Region region : RegionManager.getRegionsOfPlayer(target)) {
                    if(region.getSpecialOptions().contains("blockPvp")) {
                        Msg.warn(caster, "이 플레이어는 PVP가 금지된 구역에 들어가있습니다.");
                        e.setCancelled(true);
                        return;
                    }
                    else if(region.getSpecialOptions().contains("enablePvp")) {
                        e.setCancelled(false);
                        return;
                    }
                }
                if(!AreaManger.getArea(pdc.getArea()).pvpEnabled()) {
                    e.setCancelled(true);
                }
                /*
                else {
                    //레벨 차이 선제 타격 제어
                    if(!new MobData(caster).getAttackers().contains(target) && pdc.getLevel() < new PlayerData(caster).getLevel() - 5) {
                        Msg.warn(caster, "레벨 차이가 5 이상 나는 플레이어를 선제 타격할 수 없습니다.");
                        e.setCancelled(true);
                    }
                }
                 */
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof ArmorStand && e.getDamager() instanceof Player) {
            e.setCancelled(true);
            return;
        }
        boolean crit = false, stun = false;
        double armorIgnore = 0.0, armor = 0, critDamage = 0, stunDuration = 0, multiply = 1;

        Player attacker = null;
        Player victim = null;
        if(!(e.getEntity() instanceof LivingEntity le)) return;
        le.setNoDamageTicks(0);

        if(e.getDamager() instanceof Player player) {
            e.setDamage(e.getDamage()* -1);
            if(e.getDamage() < 0) {
                e.setCancelled(true);
                return;
            }
            attacker = player;
            PlayerData pdc = new PlayerData(attacker);
            if(getRemainCombatTime(player)==-1&&!playingCombatBgm.contains(player)) {
                if(!RukonWave.inst().getFieldWaveManager().isPlayingFieldWave(player)&&!RukonInstance.inst().getInstanceManager().isPlayerInInstance(player)) {
                    ArrayList<String> set = AreaManger.getArea(pdc.getArea()).getBattleBgm();
                    if(set.size()>0) {
                        if(OpenAudioListener.playBgm(player, Rand.getRandomCollectionElement(set))) {
                            playingCombatBgm.add(player);
                        }
                    }
                }
            }

            playersOnCombat.put(player.getUniqueId(), System.currentTimeMillis()+dropCombatSecond*1000L);
            multiply = e.getDamage();

            player.setNoDamageTicks(0);
            e.setDamage(Stat.ATTACK_DAMAGE.getTotal(player));
            crit = Rand.chanceOf(Stat.CRT_CHANCE.getTotal(player) * 100);
            armorIgnore = Stat.IGNORE_ARMOR.getTotal(player);
            critDamage = Stat.CRT_DAMAGE.getTotal(player);

            stun = Rand.chanceOf(Stat.STUN_CHANCE.getTotal(player) * 100);
            stunDuration = Stat.STUN_DUR.getTotal(player);
        }
        if(e.getEntity() instanceof Player player) {
            RideManager rideManager = RukonRiding.inst().getRideManager();
            if(rideManager.isPlayerRiding(player)) rideManager.despawnRiding(player);

            //임시적으로 테세이온 딜 조정
            if(RukonInstance.inst().getInstanceManager().getPlayerInstance(player) instanceof TeseionInstance) {
                e.setDamage(e.getDamage()/2+1);
            }

            victim = player;

            if(getRemainCombatTime(player)==-1) {
                MobData.clearMobData(e.getEntity());
            }
            playersOnCombat.put(player.getUniqueId(), System.currentTimeMillis() + 1000L * dropCombatSecond);
            PlayerData pdc = new PlayerData(victim);

            if(Rand.chanceOf(Math.min(Stat.EVADE.getTotal(player) * 100, EquipmentManager.maxEvade))) {
                e.setCancelled(true);
                Msg.send(victim, "&a*회피");
                victim.playSound(victim.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 2, 1.5f);
                new DamageSender(attacker, victim, false, 0);
                return;
            }
            //절대 방어력
            e.setDamage(e.getDamage() * (1 - Stat.ABSOLUTE_ARMOR.getTotal(player)));

            victim.setNoDamageTicks(0);
            if(attacker!=null) {
                armor = 0;
                multiply *= (Stat.getPlayerArmor(player, e.getDamage(), armorIgnore));
                //multiply *= 1 - Math.min(0.9, Stat.ARMOR.getTotal(player) / (Stat.ATTACK_DAMAGE.getTotal(player) * Stat.PLAYER_ARMOR_ATTACK_CONST));
            }
            else armor = Stat.ARMOR.getTotal(player);
            if(attacker!=null&&AreaManger.getArea(pdc.getArea()).getConditions().contains("playerDamageHalf")) e.setDamage(e.getDamage() * 0.8);
        }
        if(e.getDamager() instanceof LivingEntity at) {
            GuardiansDamageEvent ev = new GuardiansDamageEvent(at, le);
            Bukkit.getPluginManager().callEvent(ev);
            if(ev.isCancelled()) {
                e.setCancelled(true);
                return;
            }
        }

        e.setDamage(e.getDamage()-armor);
        if(victim!=null) {
            if(LogInOutListener.getLoadingPlayers().contains(victim.getName())) {
                e.setCancelled(true);
                return;
            }
            double minDam = victim.getMaxHealth() / 50;
            if(e.getDamage() < minDam) {
                e.setDamage(minDam);
            }
        }
        if(e.getDamage() < 0) e.setDamage(0);

        if(attacker!=null) {
            e.setDamage(multiply * e.getDamage());
            if(crit) {
                e.setDamage(e.getDamage()*(1+(critDamage)));
                attacker.playSound(attacker, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, 2);
                attacker.playSound(attacker, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, 0.6f);
            }
            if(stun) {
                PotionManager.effectGive(le, PotionEffectType.SLOW, 1 + stunDuration, 10);
                le.getWorld().playSound(le.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 0.5f);
                Msg.send(attacker, "&9*둔화");
            }

            if(victim==null) {
                Couple<String, Integer> md = MobData.getMobData(e.getEntity());
                if(md.getSecond()==-1) return;
                String name = md.getFirst();

                //속성 대항력 파괴력 Lv.1 당 피해량 4% 증가 (최대 100%)
                String type = mobTypeManager.getMobType(name);
                if(type!=null) {
                    Pair attrAbility = new PlayerData(attacker).getAttributeAbility(type);
                    if(attrAbility!=null) {
                        double attack = Math.min(attrAbility.getFirst(), 25);
                        e.setDamage(e.getDamage() * (25 + attack) / 25);
                    }
                }
            }
            new DamageSender(attacker, le, crit, e.getDamage()).runTaskLater(plugin, 1);
            if(crit) {
                Location location = e.getEntity().getLocation();
                location.setY(location.getY() + 0.7);
                e.getEntity().getWorld().spawnParticle(Particle.TOTEM, location, 10, 0.4, 0.3, 0.4, 0.5);
                e.getEntity().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 10, 0.4, 0.3, 0.4, 0.5);
            }
        }
        else {
            if(victim!=null) {
                Couple<String, Integer> md = MobData.getMobData(e.getDamager());
                if(md.getSecond()==-1) return;
                String type = mobTypeManager.getMobType(md.getFirst());

                //속성 대항력 저항력 Lv.1 당 피해감소 2% 증가 (최대 80%)
                if(type!=null) {
                    Pair attrAbility = new PlayerData(victim).getAttributeAbility(type);
                    if(attrAbility!=null) {
                        double attrArmor = Math.min(attrAbility.getSecond(), 40);
                        e.setDamage(e.getDamage() * (50 - attrArmor) / 50);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFinalDamage(EntityDamageByEntityEvent e) {
        if(!(e.getEntity() instanceof LivingEntity le)) return;
        if(le instanceof Player player) {
            BarManager.reloadBar(player, e.getDamage());
        }

        if(!(e.getDamager() instanceof Player attacker)) return;
        MobData mobData = new MobData(e.getEntity());
        mobData.addContribution(attacker, Math.min(e.getDamage(), le.getHealth()));
    }

    /*
     *
     * @param player player
     * @return 해당 플레이어가 버프를 먹을 수 있는지 반환, (데스패널티에 의해 버프를 못받을 수 있음)
    public static boolean checkCanGetBuff(Player player) {
        BuffData buffData = RukonBuff.inst().getBuffManager().getPlayerBuffData(player);
        if(buffData.getValueOfBuff(Stat.ATTACK_DAMAGE_PER)==DEATH_PENALTY_ATTACK) {
            if(buffData.getValueOfBuff(Stat.ARMOR_PER)==DEATH_PENALTY_ARMOR) {
                if(buffData.getValueOfBuff(Stat.MOVE_SPEED)==DEATH_PENALTY_MOVEMENT) {
                    return buffData.getValueOfBuff(Stat.LUCK) != DEATH_PENALTY_LUCK;
                }
            }
        }
        return true;
    }
    */

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        //특수효과: 슬레이어
        Player killer = e.getEntity().getKiller();
        int slayerLevel = EquipmentManager.getEquipmentAttrLevel(killer, "슬레이어");
        if(slayerLevel > 0) {
            if(SkillManager.getSkill("슬레이어").cast(killer, slayerLevel)) {
                Msg.send(killer, " ");
                Msg.send(killer, "#ffaaaa*슬레이어 발동");
            }
        }


        if(e.getEntity() instanceof Player) return;
        MobData mobData = new MobData(e.getEntity());
        MobData.clearMobData(e.getEntity());
        Couple<String, Integer> md = MobData.getMobData(e.getEntity());
        if(md.getFirst()==null) return;

        if(md.getSecond()==-1) return;
        String name = md.getFirst();
        if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(e.getEntity().getKiller())) {
            return;
        }
        int level = md.getSecond();

        //퀘스트 갱신
        WaveManager waveManager = RukonWave.inst().getWaveManager();
        if(waveManager.isWaveMob(e.getEntity())) {
            return;
        }

        if(!DropManager.hasDrop(name)) return;
        Set<UUID> attackersID = mobData.getAttackers();
        int attackersSize = attackersID.size();
        new BukkitRunnable() {
            @Override
            public void run() {
                for(UUID uuid : attackersID) {
                    Player player = Bukkit.getPlayer(uuid);
                    if(player == null) continue;
                    double contribution = mobData.getContributionProportion(player);
                    DialogQuestManager.onKillMob(player, name, contribution);
                    playersOnCombat.put(uuid, System.currentTimeMillis() + (1000L * dropCombatSecond)/5);
                    DropManager.giveDrop(player, name, level, contribution / 100);
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, Rand.randFloat(1.3, 1.7));
                    //Msg.send(player,  "&6"+name+"&f : &e몬스터를 성공적으로 처치하였습니다.", pfix);
                    //Msg.send(player, "&7     - 처치 참여 인원 : " + attackersSize + "명");
                    //Msg.send(player, String.format("&7     - 처치 기여도 : %.2f%%", contribution));
                    Msg.send(player, String.format("&e%s &f토벌 완료! &7(기여도: &f%d명 중 %.1f%%&7)", name, attackersSize, contribution), pfix);
                    /*
                    if(contribution == 100) Msg.send(player, String.format("&e%s &f토벌 완료! &7(기여도: &f%.0f%%&7 &8| &7장비 경험치: &f%.2f)", name, contribution, LevelData.getDropExp(level)), pfix);
                    else Msg.send(player, String.format("&e%s &f토벌 완료! &7(기여도: &f%d명 중 %.1f%%&7 &8| &7장비 경험치: &f%.2f)", name, attackersSize, contribution, LevelData.getDropExp(level)), pfix);
                    List<String> levelUP = EquipmentManager.addExp(player, LevelData.getDropExp(level) * contribution / 100);
                    if(!levelUP.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for(String s : levelUP) sb.append(", ").append(s);
                        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.2f);
                        Msg.send(player, " ");
                        Msg.send(player, "&e" + sb.toString().replaceFirst(", ", "") +"&f의 레벨이 올랐습니다! 레벨에 따라 능력치와 품질이 변동됩니다.", pfix);
                        EquipmentManager.reloadEquipment(player, false);
                    }
                     */
                }
            }
        }.runTaskLater(plugin, 3);
    }

    private static class DamageSender extends BukkitRunnable {
        private final Player player;
        private final LivingEntity victim;
        private final boolean crit;
        private final double originalHealth;
        private final double damage;

        public DamageSender(Player attacker, LivingEntity victim, boolean crit, double damage) {
            this.player = attacker;
            this.victim = victim;
            this.crit = crit;
            originalHealth = victim.getHealth();
            this.damage = damage;
        }

        @Override
        public void run() {
            double health = victim.getHealth();
            String str;
            if (health==originalHealth) str = "&7                           MISS!";
            else if (crit) str = String.format("                           &c♥ &f%.2f &7/ &e%.0f &7( &c-%.2f &7)", health, victim.getMaxHealth(), damage);
            else str = String.format("                           &c♥ &f%.2f &7/ &e%.0f &7( -%.2f )", health, victim.getMaxHealth(), damage);
            Msg.sendTitle(player, "\uE200", str, 16, 0, 6);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if(deathVictims.contains(e.getPlayer())) return;
        Player player = e.getEntity();
        deathVictims.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                DamagingListener.deathVictims.remove(player);
            }
        }.runTaskLater(plugin, 20);
        if(RukonInstance.inst().getInstanceManager().isPlayerInInstance(player)) return;
        player.playSound(e.getEntity(), Sound.ENTITY_WITHER_HURT, 1, 0.5f);

        PlayerData pdc = new PlayerData(player);
        deathPenalty(player);
        playersOnCombat.remove(player);
        OpenAudioListener.stopSong(player, "bgm");
    }

    public static void deathPenalty(Player player) {
        PlayerData pdc = new PlayerData(player);
        if(pdc.getLevel() <= 20) {
            Msg.send(player, " ");
            Msg.send(player, "&e20레벨까지는 죽어도 데스 패널티를 받지 않습니다!");
            Msg.send(player, " ");
            return;
        }
        if(pdc.getDeathCount() < 3) {
            pdc.setDeathCount(pdc.getDeathCount() + 1);
            if(pdc.getDeathCount()==3) {
                Msg.send(player, " ");
                Msg.send(player, "&e오늘 하루의 데스패널티 방지 횟수가 모두 소모 되었습니다. &c다음 죽음부터는 패널티 보호 물약을 사용하거나 데스 패널티를 받게됩니다.", pfix);
            }
            else {
                Msg.send(player, " ");
                Msg.send(player, String.format("&e하루 3회 죽음까지는 데스 패널티가 방지됩니다. &6(앞으로 %d회 더 보호받을 수 있습니다.)", 3 - pdc.getDeathCount()), pfix);
            }
            return;
        }
        if(ItemData.removeItem(player, new ItemData(ItemSaver.getItem("데스패널티 보호 물약")), true)) {
            Msg.send(player, " ");
            Msg.send(player, "&a데스패널티 보호 물약에 의해 데스 패널티를 받지 않았습니다.", pfix);
            Msg.send(player, " ");
        }
        else {
            int min = (int) Math.min((float) pdc.getLevel() / 3, 10);
            BuffManager manager = RukonBuff.inst().getBuffManager();
            Map<Stat, Double> statMap = new HashMap<>();
            String penaltyName;
            if(AreaManger.getArea(pdc.getArea()).pvpEnabled()) {
                penaltyName = "PVP 데스패널티";
                statMap.put(Stat.ATTACK_DAMAGE_PER, DEATH_PENALTY_ATTACK / 100);
                statMap.put(Stat.ARMOR_PER, DEATH_PENALTY_ARMOR / 100);
                statMap.put(Stat.MOVE_SPEED, DEATH_PENALTY_MOVEMENT);
                statMap.put(Stat.LUCK, DEATH_PENALTY_LUCK);
                Msg.send(player, String.format("&c데스 패널티로 인해 %d분간 쇠약 상태에 걸렸습니다. &4(PVP 지역에서 사망하여 공격력, 방어력, 이속이 크게 감소합니다.)", min), pfix);
            }
            else {
                penaltyName = "데스패널티";
                statMap.put(Stat.LUCK, DEATH_PENALTY_LUCK);
                Msg.send(player, String.format("&6데스 패널티로 인해 %d분간 행운력 저하 상태에 걸렸습니다. 행운력이 " + -DEATH_PENALTY_LUCK + "만큼 감소합니다.", min), pfix);
            }
            manager.addBuff(player, new Buff(penaltyName, System.currentTimeMillis() + (60000L * min), statMap), true);
        }
    }
}
