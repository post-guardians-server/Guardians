//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.rukon0621.guardians.addspells.effect;

import com.nisovin.magicspells.spelleffects.SpellEffect;
import me.rukon0621.guardians.helper.Rand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class RandomSoundEffect extends SpellEffect {
    private final List<SoundData> sounds = new ArrayList<>();
    private SoundCategory category;

    public RandomSoundEffect() {
    }

    public void loadFromConfig(ConfigurationSection config) {
        List<String> list = config.getStringList("sounds");

        for(String s : list) {
            try {
                String[] data = s.split(":");
                sounds.add(new SoundData(data[0].trim(), Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3])));
            } catch (Exception e) {
                System.out.println(ChatColor.RED + s + " : 잘못된 Random Sound입니다.");
            }
        }

        try {
            this.category = SoundCategory.valueOf(config.getString("category", "master").toUpperCase());
        } catch (IllegalArgumentException e) {
            this.category = SoundCategory.MASTER;
        }


    }

    class SoundData {
        private final String sound;
        private final float volume;
        private final float pitch;
        private final float pitchRange;

        public SoundData(String sound, float volume, float pitch, float pitchRange) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.pitchRange = pitchRange;
        }

        private void play(Location location) {
            location.getWorld().playSound(location, this.sound, category, this.volume, Rand.randFloat(pitch - pitchRange, pitch + pitchRange));
        }
    }

    public Runnable playEffectLocation(Location location) {
        World world = location.getWorld();
        if (world != null) Rand.getRandomCollectionElement(sounds).play(location);
        return null;
    }
}
