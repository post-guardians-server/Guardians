package me.rukon0621.guardians.GUI;

import me.rukon0621.guardians.helper.ItemClass;
import me.rukon0621.guardians.listeners.ChatEventListener;
import me.rukon0621.gui.buttons.EnumSelectButton;
import me.rukon0621.gui.buttons.ToggleButton;
import me.rukon0621.gui.windows.Window;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Set;

public class ChatSettingWindow extends Window {
    private boolean all = ChatEventListener.getPlayerChatCache(player).contains(ChatEventListener.ChatChannel.ALL.getStr());
    private boolean party = ChatEventListener.getPlayerChatCache(player).contains(ChatEventListener.ChatChannel.PARTY.getStr());
    private boolean channel = ChatEventListener.getPlayerChatCache(player).contains(ChatEventListener.ChatChannel.CHANNEL.getStr());
    private boolean guild = ChatEventListener.getPlayerChatCache(player).contains(ChatEventListener.ChatChannel.GUILD.getStr());
    private boolean whisper = ChatEventListener.getPlayerChatCache(player).contains(ChatEventListener.ChatChannel.WHISPER.getStr());
    private ChatEventListener.ChatChannel chatChannel = ChatEventListener.getPlayerChatChannel(player);

    public ChatSettingWindow(Player player) throws NoSuchFieldException {
        super(player, "&f\uF000", 4);

        map.put(9, new ToggleChatButton(getClass().getDeclaredField("all"), ChatEventListener.ChatChannel.ALL));
        map.put(11, new ToggleChatButton(getClass().getDeclaredField("channel"), ChatEventListener.ChatChannel.CHANNEL));
        map.put(13, new ToggleChatButton(getClass().getDeclaredField("party"), ChatEventListener.ChatChannel.PARTY));
        map.put(15, new ToggleChatButton(getClass().getDeclaredField("guild"), ChatEventListener.ChatChannel.GUILD));
        map.put(17, new ToggleChatButton(getClass().getDeclaredField("whisper"), ChatEventListener.ChatChannel.WHISPER));
        map.put(31, new EnumSelectButton(this, getClass().getDeclaredField("chatChannel"), ChatEventListener.ChatChannel.class) {

            @Override
            public void execute(Player player, ClickType clickType) {
                do {
                    changeField(clickType);
                } while (chatChannel.isBlockChoice());
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1.5f);
                reloadGUI();
            }

            @Override
            public ItemStack getIcon() {
                ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), "&f말하기 채널 설정");
                item.addLore("&7어떤 채팅 채널에 채팅을 보낼지 설정합니다.");
                item.addLore("&e현재 설정된 채널: " + chatChannel.getChannelPrefix());
                item.addLore(" ");
                item.addLore("&e/채팅 &f명령어를 이용해서 변경할 수도 있습니다!");
                item.setCustomModelData(chatChannel.getCmd());
                return item.getItem();
            }
        });

        reloadGUI();
        open();
    }

    class ToggleChatButton extends ToggleButton {
        private final ChatEventListener.ChatChannel chatChannel;
        public ToggleChatButton(Field field, ChatEventListener.ChatChannel chatChannel) {
            super(ChatSettingWindow.this, field);
            this.chatChannel = chatChannel;
        }

        @Override
        public ItemClass getOriginalIcon() {
            ItemClass item = new ItemClass(new ItemStack(Material.SCUTE), chatChannel.getChannelPrefix());
            try {
                if(field.getBoolean(ChatSettingWindow.this)) {
                    item.addLore("&7클릭하여 이 채널의 채팅을 &c비활성화&7합니다.");
                    item.setCustomModelData(82);
                }
                else {
                    item.addLore("&7클릭하여 이 채널의 채팅을 &a활성화&7합니다.");
                    item.setCustomModelData(83);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return item;
        }

        @Override
        public void execute(Player player, ClickType clickType) {
            player.playSound(player, Sound.UI_BUTTON_CLICK,1 , 1.3f);
            changeField(player);
            reloadGUI();
        }
    }

    @Override
    public void close(boolean b) {
        disable();
        if(b) {
            new MenuWindow(player, 2);
        }
        Set<String> set = ChatEventListener.getPlayerChatCache(player);
        set.clear();
        if(all) set.add(ChatEventListener.ChatChannel.ALL.getStr());
        if(channel) set.add(ChatEventListener.ChatChannel.CHANNEL.getStr());
        if(party) set.add(ChatEventListener.ChatChannel.PARTY.getStr());
        if(guild) set.add(ChatEventListener.ChatChannel.GUILD.getStr());
        if(whisper) set.add(ChatEventListener.ChatChannel.WHISPER.getStr());
        ChatEventListener.setPlayerChatChannel(player, chatChannel);
    }
}
