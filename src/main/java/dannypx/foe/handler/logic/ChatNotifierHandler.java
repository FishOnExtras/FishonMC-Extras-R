package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.store.CustomChatNotificationDataHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.placeholder.evaluator.PlaceholderResult;
import dannypx.foe.placeholder.handler.PlaceholderHandlerV2;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
    public void notifyChatOnTrigger(String[] notificationIds) {
        for (String notificationId : notificationIds) {
            if(notificationId != null) {
                String notification = CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.getOrDefault(notificationId.trim(), "");

                if(!notification.isBlank()) {
                    PlaceholderResult result = PlaceholderHandlerV2.instance().resolve(notification);

                    if((result.success()[0] && !result.success()[1]) || !result.errors().isEmpty()) {
                        this.sendChatMessage(result.text());
                    }
                }
            }
        }
    }

    public void sendChatMessage(Component message) {
        minecraft.gui.getChat().addClientSystemMessage(
                TextHelper.concat(
                        Component.literal("FoER ").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD),
                        Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY),
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
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "key", Pair.of(Component.literal("value"), Component.empty())
        );
    }
    //endregion
}
