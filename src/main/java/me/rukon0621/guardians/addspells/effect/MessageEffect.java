//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.rukon0621.guardians.addspells.effect;

import com.nisovin.magicspells.spelleffects.SpellEffect;
import me.rukon0621.guardians.helper.Msg;
import me.rukon0621.guardians.helper.Rand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MessageEffect extends SpellEffect {
    private String message;
    private String title;
    private String subtitle;
    private int stay;
    private int fadein;
    private int fadeout;

    public void loadFromConfig(ConfigurationSection config) {
        message = config.getString("message");
        title = config.getString("title");
        subtitle = config.getString("subtitle");
        stay = config.getInt("stay", 20);
        fadein = config.getInt("stay", 20);
        fadeout = config.getInt("stay", 20);
    }

    @Override
    protected Runnable playEffectEntity(Entity entity) {
        if(entity == null) return null;
        if(!(entity instanceof Player player)) return null;

        if(message != null) {
            Msg.send(player, message);
        }
        if(title != null && subtitle != null) {
            Msg.sendTitle(player, title, subtitle, stay, fadein, fadeout);
        }
        else if(title != null) {
            Msg.sendTitle(player, title, stay, fadein, fadeout);
        }
        else if(subtitle != null) {
            Msg.sendTitle(player, null, subtitle, stay, fadein, fadeout);
        }
        return null;
    }
}
