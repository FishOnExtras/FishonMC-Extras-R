package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.store.CustomChatNotificationDataHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class ChatNotifierHandler extends Handler {
    private static ChatNotifierHandler INSTANCE = new ChatNotifierHandler();

    public static ChatNotifierHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatNotifierHandler();
        }
        return INSTANCE;
    }

    //region Fields
    public void notifyChat(String notificationId) {
        if(notificationId != null) {
            String notification = CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.getOrDefault(notificationId, "");

            if(!notification.isBlank()) {
                Pair<Boolean, MutableText> message = PlaceholderHandler.parsePlaceholderFromString(notification);

                if(message.value1()) {
                    this.sendChatMessage(message.value2());
                }
            }
        }
    }

    public void sendChatMessage(Text message) {
        minecraftClient.inGameHud.getChatHud().addMessage(
                TextHelper.concat(
                        Text.literal("FoER ").formatted(Formatting.DARK_GREEN, Formatting.BOLD),
                        Text.literal("» ").formatted(Formatting.DARK_GRAY),
                        message
                )
        );
    }
    //endregion

    //region Methods
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    @Override
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "key", Pair.of(Text.literal("value"), Text.empty())
        );
    }
    //endregion
}
