package me.rukon0621.guardians.commands;

import me.rukon0621.guardians.data.*;
import me.rukon0621.guardians.helper.ArgHelper;
import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.helper.ItemSaver;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.main;
import me.rukon0621.utils.util.MsgUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static me.rukon0621.guardians.main.pfix;

public class ItemDataCommands implements CommandExecutor {
    public static String[] arguments = {"기본값", "퀘스트아이템", "거래불가","영혼추출불가", "스텟설정","요구레벨설정", "아다만트석", "제작레벨설정","중요한물건","가공불가","요구무기타입","무기설정","방어구설정","행운력설정","장신구설정","지속시간","레벨", "강화", "등급","유효시즌","세이버", "속성설정", "속성목록","가공횟수","타입목록", "리로드"};

    public ItemDataCommands() {
        main.getPlugin().getCommand("itemdata").setExecutor(this);
        main.getPlugin().getCommand("itemdata").setTabCompleter(new ItemDataCommandsTabComp());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1 && args[0].startsWith("리")) {
            MsgUtil.cmdMsg(sender, "&7아이템 데이터 리로드중...");
            ItemData.reloadItemData();
            MsgUtil.cmdMsg(sender, "&a아이템 데이터 리로드 완료!");
            return true;
        }

        if(!(sender instanceof Player player)) return false;
        if(args.length==0) {
            usages(player);
            return true;
        }

        if(args[0].equals("기본값")) {
            if(args.length<2) {
                usage(player, "기본값", true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            String type = ArgHelper.sumArg(args, 1);
            if(!TypeData.getTypeMap().keySet().contains(type)) {
                Msg.warn(player, "&6"+type+"&c은 서버에 등록된 아이템 타입이 아닙니다.");
                Msg.send(player, "&f/아이템데이터 타입목록&7 명령어로 서버에 등록된 타입을 확인할 수 있습니다.", pfix);
                return true;
            }
            ItemData itemData = new ItemData(player.getInventory().getItemInMainHand());

            if(type.equals("치장 아이템")) {
                ItemClass item = new ItemClass(player.getInventory().getItemInMainHand());
                item.addLore("&7우클릭하여 치장을 획득합니다.");
                item.addLore("&7치장은 캐쉬 상점의 옷장에서 장착할 수 있습니다.");
                itemData = new ItemData(item);
            }

            itemData.setType(type);
            itemData.setLevel(0);

            TypeData typeData = TypeData.getType(itemData.getType());
            if(itemData.isEquipment()||itemData.isRiding()||typeData.isMaterialOf("버프 아이템")) {
                itemData.setGrade(ItemGrade.NORMAL);
                if(!typeData.isMaterialOf("버프 아이템")) itemData.setExp(0);
            }

            if(TypeData.getType(type).isMaterialOf("수치화 소모품")) {
                itemData.setValue(10);
            }

            player.getInventory().setItemInMainHand(itemData.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 설정하였습니다.", pfix);
            return true;
        }
        if(args[0].equals("요구무기타입")) {
            if(args.length<2) {
                usage(player, "요구무기타입", true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            String type = ArgHelper.sumArg(args, 1);
            if(!TypeData.getTypeMap().keySet().contains(type)) {
                Msg.warn(player, "&6"+type+"&c은 서버에 등록된 아이템 타입이 아닙니다.");
                Msg.send(player, "&f/아이템데이터 타입목록&7 명령어로 서버에 등록된 타입을 확인할 수 있습니다.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setRequiredWeaponType(type);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 설정하였습니다.", pfix);
            return true;
        }
        else if(args[0].equals("레벨")) {
            if(args.length<2) {
                usage(player, "레벨", true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            int level;
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c아이템의 레벨을 제대로 입력해주세요.", pfix);
                return true;
            }

            if(!idata.getType().equals("세이버") && level<0) {
                Msg.send(player, "&c레벨을 가능한 값으로 설정해주세요.", pfix);
                return true;
            }
            double per = idata.getExpPercentage();
            idata.setLevel(level);
            idata.setExp(idata.getMaxExp() * per / 100D);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 레벨을 설정하였습니다.", pfix);
        }
        else if(args[0].equals("제작레벨설정")) {
            if(args.length<2) {
                usage(player, args[0], true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            int level;
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c아이템의 레벨을 제대로 입력해주세요.", pfix);
                return true;
            }

            if(!idata.getType().equals("세이버") && level<0) {
                Msg.send(player, "&c레벨을 가능한 값으로 설정해주세요.", pfix);
                return true;
            }
            idata.setCraftLevel(level);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 레벨을 설정하였습니다.", pfix);
        }
        else if(args[0].equals("강화")) {
            if(args.length<2) {
                usage(player, args[0], true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            EnhanceLevel level;
            try {
                level = EnhanceLevel.getEnhanceLevel(Integer.parseInt(args[1]));
            } catch (Exception e) {
                Msg.send(player, "&c아이템의 강화 레벨을 제대로 입력해주세요.", pfix);
                return true;
            }
            idata.setEnhanceLevel(level);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 강화 레벨을 설정하였습니다.", pfix);
        }
        else if(args[0].equals("품질")) {
            if(args.length<2) {
                usage(player, "품질", true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            double quality;
            try {
                quality = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c아이템의 품질을 제대로 입력해주세요.", pfix);
                return true;
            }
            idata.setQuality(quality);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 품질을 설정했습니다.", pfix);
        }
        else if(args[0].equals("수치")) {
            if(args.length<2) {
                usage(player, args[0], true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            double value;
            try {
                value = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c아이템의 수치를 제대로 입력해주세요.", pfix);
                return true;
            }
            idata.setValue(value);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 수치를 설정했습니다.", pfix);
        }
        else if(args[0].equals("유효시즌")) {
            if(args.length<2) {
                usage(player, "유효시즌", true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            int season;
            try {
                season = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c아이템의 유효시즌을 제대로 입력해주세요.", pfix);
                return true;
            }
            if(season<0) {
                Msg.send(player, "&c유효시즌을 가능한 값으로 설정해주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setSeason(season);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 유효 시즌을 설정하였습니다.", pfix);
        }
        else if(args[0].equals("등급")) {
            if(args.length<2) {
                usage(player, args[0], true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData data = new ItemData(player.getInventory().getItemInMainHand());
            try {
                data.setGrade(ItemGrade.valueOf(args[1]));
            } catch (IllegalArgumentException e) {
                Msg.warn(player, "해당 등급은 존재하지 않는 등급입니다.");
                data.setGrade(ItemGrade.UNKNOWN);
            }
            player.getInventory().setItemInMainHand(data.getItemStack());
            Msg.send(player, "성공적으로 등급을 설정하였습니다.", pfix);
        }
        else if(args[0].equals("가공횟수")) {
            if(args.length<2) {
                usage(player, "가공횟수", true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            int level;
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c아이템의 가공횟수를 제대로 입력해주세요.", pfix);
                return true;
            }
            if(level<0) {
                Msg.send(player, "&c가공횟수를 가능한 값으로 설정해주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setProcessTime(level);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 설정하였습니다.", pfix);
        }
        else if(args[0].equals("지속시간")) {
            if(args.length<2) {
                usage(player, "지속시간", true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            int durationMinute;
            try {
                durationMinute = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c아이템의 지속시간을 제대로 입력해주세요.", pfix);
                return true;
            }
            if(durationMinute<0) {
                Msg.send(player, "&c지속시간을 가능한 값으로 설정해주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setDuration(durationMinute);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 설정하였습니다.", pfix);
        }
        else if(args[0].equals("사용시간")) {
            if(args.length<2) {
                usage(player, args[0], true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            double durationSec;
            try {
                durationSec = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c아이템의 시간을 제대로 입력해주세요.", pfix);
                return true;
            }
            if(durationSec<0) {
                Msg.send(player, "&c시간을 가능한 값으로 설정해주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setUsingTime(durationSec);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 설정하였습니다.", pfix);
        }
        else if(args[0].equals("요구레벨설정")) {
            if(args.length<2) {
                usage(player, "요구레벨설정", true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            int level;
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c아이템의 요구 레벨을 제대로 입력해주세요.", pfix);
                return true;
            }
            if(level < 1) {
                Msg.send(player, "&c요구 레벨을 가능한 값으로 설정해주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setRequiredLevel(level);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 설정하였습니다.", pfix);
        }
        else if (args[0].startsWith("퀘스트")) {
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setQuestItem(!idata.getDataMap().containsKey("questItem"));
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 변경하였습니다.", pfix);
        }
        else if (args[0].startsWith("중요한")) {
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setImportantItem(!idata.isImportantItem());
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 변경하였습니다.", pfix);
        }
        else if (args[0].startsWith("가공불가")) {
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setUnprecessable(!idata.isUnprecessable());
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 변경하였습니다.", pfix);
        }
        else if (args[0].startsWith("영혼추출불가")) {
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setDisassemble(!idata.isDisassemble());
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 변경하였습니다.", pfix);
        }
        else if (args[0].startsWith("거래")) {
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            idata.setUntradable(!idata.isUntradable());
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 변경하였습니다.", pfix);
        }
        else if (args[0].startsWith("무")) {
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            if(args.length < 5) {
                usage(player, "무기설정", true);
                return true;
            }

            double damage, criticalChance, criticalDamage;
            String attackSpeed;

            boolean percentDamage = false;
            if(args[1].endsWith("%")) {
                percentDamage = true;
                args[1] = args[1].replaceAll("%", "");
            }

            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            try {
                damage = Double.parseDouble(args[1]);
                attackSpeed = args[2].trim();
                criticalChance = Double.parseDouble(args[3]);
                criticalDamage = Double.parseDouble(args[4]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c제대로된 숫자를 입력해주세요.", pfix);
                return true;
            }
            if(damage!=0) {
                if(percentDamage) idata.setStat(Stat.ATTACK_DAMAGE_PER, damage);
                else idata.setStat(Stat.ATTACK_DAMAGE, damage);
            }
            if(!attackSpeed.equals("0")) idata.setAttackSpeed(attackSpeed.replaceAll("_", " "));
            if(criticalChance!=0) idata.setStat(Stat.CRT_CHANCE, criticalChance);
            if(criticalDamage!=0) idata.setStat(Stat.CRT_DAMAGE, criticalDamage);

            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 변경하였습니다.", pfix);
        }
        else if (args[0].startsWith("방")) {
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            if(args.length < 5) {
                usage(player, "방어구설정", true);
                return true;
            }
            double armor, health, movementSpeed, regen;
            boolean armorPercent = false, healthPercent = false, regenPercent = false;
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());

            if(args[1].endsWith("%")) {
                args[1] = args[1].replaceAll("%", "");
                armorPercent = true;
            }
            if(args[2].endsWith("%")) {
                args[2] = args[2].replaceAll("%", "");
                healthPercent = true;
            }
            if(args[4].endsWith("%")) {
                args[4] = args[4].replaceAll("%", "");
                regenPercent = true;
            }

            try {
                armor = Double.parseDouble(args[1]);
                health = Double.parseDouble(args[2]);
                regen = Double.parseDouble(args[4]);
                movementSpeed = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c제대로된 숫자를 입력해주세요.", pfix);
                return true;
            }
            if(armor!=0)  {
                if(armorPercent) idata.setStat(Stat.ARMOR_PER, armor);
                else idata.setStat(Stat.ARMOR, armor);
            }
            if(health!=0) {
                if(healthPercent) idata.setStat(Stat.HEALTH_PER, health);
                else idata.setStat(Stat.HEALTH, health);
            }
            if(regen!=0) {
                if(regenPercent) idata.setStat(Stat.REGEN_PER, regen);
                else idata.setStat(Stat.REGEN, regen);
            }
            if(movementSpeed!=0) idata.setStat(Stat.MOVE_SPEED, movementSpeed);

            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 변경하였습니다.", pfix);
        }
        else if (args[0].startsWith("행")) {
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            if(args.length < 2) {
                usage(player, "행운력설정", true);
                return true;
            }

            double luck;
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            try {
                luck = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c제대로된 숫자를 입력해주세요.", pfix);
                return true;
            }
            if(luck!=0) idata.setStat(Stat.LUCK, luck);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 변경하였습니다.", pfix);
        }
        else if (args[0].startsWith("장")) {
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            if(args.length < 3) {
                usage(player, "장신구설정", true);
                return true;
            }

            double armorIgnore, evade;
            ItemData idata = new ItemData(player.getInventory().getItemInMainHand());
            try {
                armorIgnore = Double.parseDouble(args[1]);
                evade = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                Msg.send(player, "&c제대로된 숫자를 입력해주세요.", pfix);
                return true;
            }
            if(armorIgnore!=0) idata.setStat(Stat.IGNORE_ARMOR_POWER, armorIgnore);
            if(evade!=0) idata.setStat(Stat.EVADE_POWER, evade);
            player.getInventory().setItemInMainHand(idata.getItemStack());
            Msg.send(player, "성공적으로 아이템 데이터를 변경하였습니다.", pfix);
        }
        else if (args[0].startsWith("세")) {
            if(args.length<2) {
                usage(player, "세이버", true);
                return true;
            }
            String name = ArgHelper.sumArg(args, 1);
            if(!ItemSaver.isItemExist(name)) {
                Msg.warn(player, "해당 아이템은 존재하지 않습니다.");
                return true;
            }
            ItemData itemData = new ItemData(new ItemClass(new ItemStack(Material.EMERALD), name).getItem());
            itemData.setType("세이버");
            itemData.setLevel(0);
            player.getInventory().addItem(itemData.getItemStack());
            Msg.send(player, "아이템을 성공적으로 지급 받았습니다.", pfix);
        }
        else if (args[0].startsWith("속성목")) {
            Msg.send(player, "&6서버에 존재하는 속성의 목록입니다.", pfix);
            for(String attr : ItemData.getGoodAttrList()) {
                Msg.send(player, "&a(GOOD) - " + attr);
            }
            for(String attr : ItemData.getBadAttrList()) {
                Msg.send(player, "&7(BAD) - " + attr);
            }
            for(String attr : ItemData.getRareAttrList()) {
                Msg.send(player, "&c(RARE) " + attr);
            }
        }
        else if (args[0].startsWith("타입목")) {
            if(args.length<2) {
                Msg.send(player, "&6서버에 존재하는 타입의 목록입니다.", pfix);
                for(String key : TypeData.getTypeMap().keySet()) {
                    Msg.send(player, key);
                }
                return true;
            }
            String type = ArgHelper.sumArg(args, 1);
            if(!TypeData.getTypeMap().containsKey(type)) {
                Msg.warn(player, "해당 속성은 서버에 등록된 속성이 아닙니다.");
                return true;
            }
            TypeData typeData = TypeData.getType(type);
            Msg.send(player, "&6"+type+"&e 속성에 관련된 세부 정보입니다.", pfix);
            Msg.send(player, "&f[ 상위 속성 ]");
            for(String key : typeData.getParents()) {
                Msg.send(player, "&7 - " + key);
            }
            Msg.send(player, "&f[ 하위 속성 ]");
            for(String key : typeData.getChild()) {
                Msg.send(player, "&7 - " + key);
            }
        }
        else if (args[0].startsWith("속")) {
            if(args.length < 3) {
                usage(player, "속성설정", true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            int level;
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Msg.warn(player, "제대로된 레벨을 입력해주세요.");
                return true;
            }
            String attr = ArgHelper.sumArg(args, 2);
            if(!ItemData.getAttrList().contains(attr)) {
                Msg.warn(player, attr + " 속성은 서버에 등록되지 않은 속성입니다. /아이템데이터 속성목록 명령어로 속성을 확인하세요.");
                return true;
            }
            ItemData itemData = new ItemData(player.getInventory().getItemInMainHand());
            itemData.setAttr(attr, level);
            player.getInventory().setItemInMainHand(itemData.getItemStack());
            Msg.send(player, "성공적으로 속성을 부여하였습니다.", pfix);
        }
        else if (args[0].startsWith("스텟")) {
            if(args.length < 3) {
                usage(player ,args[0], true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType()== Material.AIR) {
                Msg.send(player, "&c손에 아이템을 들어주세요.", pfix);
                return true;
            }
            try {
                Stat stat = Stat.valueOf(args[1]);
                double value = Double.parseDouble(args[2]);
                ItemData itemData = new ItemData(player.getInventory().getItemInMainHand());
                itemData.setStat(stat, value);
                player.getInventory().setItemInMainHand(itemData.getItemStack());
                Msg.send(player, "성공적으로 스텟을 부여하였습니다.", pfix);

            } catch (NumberFormatException e) {
                Msg.send(player, "정확한 숫자를 입력해주세요.");
            }
            catch (IllegalArgumentException e) {
                Msg.send(player, "올바른 스텟 이름을 입력해주세요.");
            }
        }
        else if (args[0].equals("아다만트석")) {
            if(args.length < 4) {
                usage(player, args[0], true);
                return true;
            }
            if(player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                Msg.warn(player, "손에 아이템을 들어주세요.");
                return true;
            }

            try {
                double value = Double.parseDouble(args[3]);
                Stat stat = Stat.valueOf(args[2]);
                ItemClass item = new ItemClass(player.getInventory().getItemInMainHand());
                if(item.getLore().isEmpty()) {
                    item.addLore("&7아다만트의 힘이 깃든 아다만트석이다.");
                    item.addLore("&f우클릭&7하여 무기를 선택하고 힘을 부여할 수 있다.");
                }
                ItemGrade grade = ItemGrade.valueOf(args[1]);
                ItemData itemData = new ItemData(item.getItem());
                itemData.addStoneData(new StoneData(grade, stat, value));
                player.getInventory().setItemInMainHand(itemData.getItemStack());
                Msg.send(player, "설정되었습니다.");
            } catch (NumberFormatException e) {
                Msg.warn(player, "제대로된 수치를 써주세요.");
            } catch (IllegalArgumentException e) {
                Msg.warn(player, "제대로된 등급을 써주세요.");
            }
        }
        else {
            usages(player);
        }

        return true;
    }

    private void usage(Player player, String arg, boolean forone) {
        if(forone) {
            Msg.send(player, "&e┌────────────────────────┐");
            Msg.send(player, " ");
        }
        if(arg.equals("기본값")) {
            Msg.send(player, "&6/아이템데이터 기본값 <유형>");
            Msg.send(player, "&7   아이템의 가장 기본적인 정보를 설정합니다.");
            Msg.send(player, "&7   레벨은 0과 1일때의 정보를 담습니다");
        }
        else if(arg.equals("레벨")) {
            Msg.send(player, "&6/아이템데이터 레벨 <레벨>");
            Msg.send(player, "&7   레벨에 0을 입력하면 실제 값은 ?로 표시됩니다.");
        }
        else if(arg.equals("제작레벨설정")) {
            Msg.send(player, "&6/아이템데이터 제작레벨설정 <레벨>");
        }
        else if(arg.equals("아다만트석")) {
            Msg.send(player, "&6/아이템데이터 아다만트석 <등급> <스텟> <수치>");
        }
        else if(arg.equals("등급")) {
            Msg.send(player, "&6/아이템데이터 등급 <등급>");
            Msg.send(player, "&7   아이템의 등급을 설정합니다.");
        }
        else if(arg.equals("유효시즌")) {
            Msg.send(player, "&6/아이템데이터 유효시즌 <시즌>");
            Msg.send(player, "&7   아이템의 유효시즌을 설정합니다.");
        }
        else if(arg.equals("가공횟수")) {
            Msg.send(player, "&6/아이템데이터 가공횟수 <값>");
            Msg.send(player, "&7   아이템의 가공 가능 횟수를 설정합니다.");
        }
        else if (arg.equals("퀘스트아이템")) {
            Msg.send(player, "&6/아이템데이터 퀘스트[아이템]");
            Msg.send(player, "&7   손에 들고 있는 아이템을 퀘스트 아이템 여부를 변경합니다.");
        }
        else if (arg.equals("요구무기타입")) {
            Msg.send(player, "&6/아이템데이터 요구무기타입 <타입>");
            Msg.send(player, "&7   손에 들고 있는 아이템의 요구 무기 타입을 설정합니다.");
        }
        else if (arg.equals("거래불가")) {
            Msg.send(player, "&6/아이템데이터 거래[불가]");
            Msg.send(player, "&7   손에 들고 있는 아이템을 거래가능 여부를 변경합니다.");
        }
        else if (arg.equals("무기설정")) {
            Msg.send(player, "&6/아이템데이터 무[기설정] <공격력> <공격 속도> <치명타 확률> <치명타 피해량>");
            Msg.send(player, "&7   손에 든 아이템의 무기 정보를 설정합니다.");
            Msg.send(player, "&7   0을 입력하면 해당 항목은 비워집니다.");
            Msg.send(player, "&7   공격 속도는 문자열로 _를 사용하여 띄어쓰기를 대체할 수 있습니다.");
        }
        else if (arg.equals("방어구설정")) {
            Msg.send(player, "&6/아이템데이터 방[어구설정] <방어력> <체력> <이동속도> <재생력>");
            Msg.send(player, "&7   손에 든 아이템의 방어구 정보를 설정합니다.");
            Msg.send(player, "&7   0을 입력하면 해당 항목은 비워집니다.");
        }
        else if (arg.equals("행운력설정")) {
            Msg.send(player, "&6/아이템데이터 행[운력설정] <수치>");
            Msg.send(player, "&7   손에 든 장비의 행운력을 설정합니다.");
        }
        else if (arg.equals("장신구설정")) {
            Msg.send(player, "&6/아이템데이터 장[신구설정] <방어관통> <회피력>");
            Msg.send(player, "&7   손에 든 아이템의 스텟을 설정합니다.");
        }
        else if (arg.equals("세이버")) {
            Msg.send(player, "&6/아이템데이터 세이버 <아이템 세이버 이름>");
            Msg.send(player, "&7   Dialog&Quest,Shop 등과 연동할 수 있는 아이템 세이버를 지급 받습니다.");
        }
        else if (arg.equals("속성설정")) {
            Msg.send(player, "&6/아이템데이터 속성설정 <레벨> <속성이름>");
            Msg.send(player, "&7   해당 아이템에 속성을 붙힙니다.");
        }
        else if (arg.equals("스텟설정")) {
            Msg.send(player, "&6/아이템데이터 스텟설정 <스텟 상수값> <값>");
        }
        else if (arg.equals("속성목록")) {
            Msg.send(player, "&6/아이템데이터 속성목록");
            Msg.send(player, "&7   서버에 존재하는 속성의 목록을 확인합니다.");
        }
        else if (arg.equals("수치")) {
            Msg.send(player, "&6/아이템데이터 수치 <값>");
            Msg.send(player, "&7   아이템의 수치를 설정합니다.");
        }
        else if (arg.equals("품질")) {
            Msg.send(player, "&6/아이템데이터 품질 <값>");
            Msg.send(player, "&7   아이템의 품질을 설정합니다.");
        }
        else if (arg.equals("타입목록")) {
            Msg.send(player, "&6/아이템데이터 타입목록 <타입이름>");
            Msg.send(player, "&7   서버에 존재하는 타입의 목록을 확인합니다.");
            Msg.send(player, "&7   타입이름을 입력하면 해당 속성의 세부 사항을 확인합니다");
        }
        else if (arg.equals("리로드")) {
            Msg.send(player, "&6/아이템데이터 리로드");
            Msg.send(player, "&7   서버에 존재하는 속성 데이터를 리로드합니다.");
        }
        Msg.send(player, " ");
        if (forone) Msg.send(player, "&e└────────────────────────┘");
    }

    private void usages(Player player) {
        Msg.send(player, "&e┌────────────────────────┐");
        Msg.send(player, " ");
        for(String s : arguments) {
            usage(player, s, false);
        }
        Msg.send(player, "&e└────────────────────────┘");
    }
}
