package me.rukon0621.guardians.helper;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemClass {
    /*

    이 클래스에서 라인의 넘버는 0이 아닌 1에서 시작한다
    왜 그렇게 설계했을까...

     */

    public static ArrayList<String> getLores(ItemStack item) {
        if(item.getItemMeta().getLore()==null) {
            return new ArrayList<>();
        }
        return (ArrayList<String>) item.getItemMeta().getLore();
    }

    public static String getLore(ItemStack item, int line) {
        ArrayList<String> lores = getLores(item);
        if(lores.size()<line) return null;
        return lores.get(line-1);
    }

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemClass(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public ItemClass(ItemStack item, String name) {
        this.item = item;
        this.meta = item.getItemMeta();
        setName(name);
    }

    //ItemStack 자체를 반환
    public ItemStack getItem() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }

    //아이템의 이름을 설정 (Display Name)
    public void setName(String name) {
        meta.setDisplayName(Msg.color(name));
    }
    public void setName(BaseComponent[] components) {
        meta.setDisplayNameComponent(components);
    }

    public String getName() {
        return meta.getDisplayName();
    }

    //아이템의 커스텀 모델 데이터를 설정
    public void setCustomModelData(int id) {
        meta.setCustomModelData(id);
    }
    //아이템의 커스텀 모델 데이터를 설정
    public int getCustomModelData() {
        return meta.getCustomModelData();
    }

    public void addFlag(ItemFlag flag) {
        meta.addItemFlags(flag);
    }
    public void removeFlag(ItemFlag flag) {
        meta.removeItemFlags(flag);
    }

    //로어 리스트를 반환
    public List<String> getLore() {
        if(!meta.hasLore()) return new ArrayList<>();
        if (meta.getLore()==null) return new ArrayList<>();
        return meta.getLore();
    }

    //특정 줄의 로어를 반환 (1번 인덱스부터)
    public String getLore(int index) {
        if(index < 1 || index > meta.getLore().size()) return null;
        if (meta.getLore()==null) return null;
        return meta.getLore().get(index - 1);
    }

    //마지막 줄에 로어를 추가
    public void addLore(String lore) {
        if(!meta.hasLore()) {
            List<String> lores = new ArrayList<>();
            lores.add(Msg.color(lore));
            meta.setLore(lores);
        }
        else {
            List<String> lores = meta.getLore();
            lores.add(Msg.color(lore));
            meta.setLore(lores);
        }
    }

    //특정 줄의 로어를 변경 (1번 인덱스부터)
    public void setLore(int index, String lore) {
        List<String> lores = meta.getLore();
        if (lores==null) lores = new ArrayList<>();
        if(index<1) return;
        while(lores.size()<index) {
            lores.add("");
        }
        lores.set(index - 1, Msg.color(lore));
        meta.setLore(lores);
    }
    public void setLore(List<String> lores) {
        meta.setLore(lores);
    }

    public void setDurability(int value) {
        if(meta instanceof Damageable im) {
            im.setDamage(item.getType().getMaxDurability() - value);
        }
    }

    public void setAmount(int amount) {
        item.setAmount(amount);
    }

    //해당 줄의 로어를 삭제 (1번 인덱스부터)
    public void removeLore(int index) {
        List<String> lores = meta.getLore();
        if (lores==null) lores = new ArrayList<String>();
        if(lores.size()==0) return;
        else if (index < 1 || index > lores.size()) return;
        lores.remove(index-1);
    }

    //마지막 로어를 삭제
    public void removeLore() {
        removeLore(meta.getLore().size());
    }

    public void clearLore() {
        meta.setLore(null);
    }
}
