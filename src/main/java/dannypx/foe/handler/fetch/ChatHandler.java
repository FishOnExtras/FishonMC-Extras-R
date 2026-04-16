package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.*;
import dannypx.foe.handler.store.CustomChatTriggerDataHandler;
import dannypx.foe.handler.store.ProfileDataHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.custom_text.CustomTextValue;
import dannypx.foe.type.custom_text.TextValue;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ChatHandler extends Handler {
    private static ChatHandler INSTANCE = new ChatHandler();

    public static ChatHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private Map<String, Text> storedChatTriggerText = new HashMap<>();

    final List<String> blacklistedTextFilters = List.of(
            "REACTIONS »"
    );

    public Pair<Boolean, CustomTextValue> getChat(String[] params) {
        if(params.length > 1
                && minecraftClient.player != null
        ) {
            Pattern fieldPattern = Pattern.compile("^(trigger)$");

            if(fieldPattern.matcher(params[0]).matches()
            ) {
                return switch(params[0]) {
                    case "trigger" -> PlaceholderHandler.getTextValue(new TextValue(storedChatTriggerText.getOrDefault(params[1], Text.empty())));
                    default -> PlaceholderHandler.noResult();
                };
            }
        }
        return PlaceholderHandler.noResult();
    }
    //endregion

    //region Methods
    public void init() {
        if(storedChatTriggerText.isEmpty()) {
            this.initChatTrigger();
        }
    }

    public void initChatTrigger() {
        storedChatTriggerText.clear();
        CustomChatTriggerDataHandler.instance().getCustomChatTriggerData().chatTriggerList.forEach((name, trigger) -> {
            storedChatTriggerText.put(name, Text.empty());
        });
    }

    public void onReceiveMessage(Text text) {
        if(this.inBlackList(text)) return;
        
        this.checkPet(text);
        this.checkChatTrigger(text);
    }

    private boolean inBlackList(Text text) {
        return blacklistedTextFilters.stream().anyMatch(filter -> text.getString().startsWith(filter));
    }

    private void checkPet(Text text) {
        if(text.getString().startsWith("PETS » Equipped your")) {
            ProfileDataHandler.instance().updatePet(true);
        } else if (text.getString().startsWith("PETS » Pet unequipped!")) {
            ProfileDataHandler.instance().updatePet(false);
        } else if(text.getString().startsWith("CREWS » Crew Chat has been enabled")) {
            ProfileDataHandler.instance().updateCrewChat(true);
        } else if(text.getString().startsWith("CREWS » Crew Chat has been disabled")) {
            ProfileDataHandler.instance().updateCrewChat(false);
        } else if(text.getString().startsWith("TOURNAMENT You have ENABLED tournament contributions")) {
            ProfileDataHandler.instance().updateTournamentContribution(true);
        } else if(text.getString().startsWith("TOURNAMENT You have DISABLED tournament contributions")) {
            ProfileDataHandler.instance().updateTournamentContribution(false);
        }
    }

    private void checkChatTrigger(Text text) {
        CustomChatTriggerDataHandler.instance().getCustomChatTriggerData().chatTriggerList.forEach((name, trigger) -> {
            if(!trigger.regex.isBlank()
                    && trigger.pattern.matcher(text.getString()).matches()
            ) {
                storedChatTriggerText.put(name, text);
                if(trigger.notificationToTrigger != null
                        && !trigger.notificationToTrigger.isBlank()
                        && trigger.useChatTrigger
                ) {
                    CodeExecuterHandler.runLater(1, () -> {
                        NotifierHandler.instance().notifyNotifier(trigger.notificationToTrigger);
                    });
                }

                if(trigger.chatNotificationToTrigger != null
                        && !trigger.chatNotificationToTrigger.isBlank()
                        && trigger.useChatTrigger
                ) {
                    CodeExecuterHandler.runLater(1, () -> {
                        ChatNotifierHandler.instance().notifyChat(trigger.chatNotificationToTrigger);
                    });
                }
            }
        });
    }

    public Text onModifyMessage(Text text) {
        text = this.modifyPetMessageWithPercentage(text);
        return text;
    }

    private Text modifyPetMessageWithPercentage(Text text) {

        String json = TextHelper.textToJson(text);
        if (json.contains("ᴘᴇᴛ ʀᴀᴛɪɴɢ")) {
            String petStr = json.substring(json.indexOf(" Pet\\n"), json.indexOf("ʀɪɢʜᴛ ᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ ᴘᴇᴛ ᴍᴇɴᴜ"));
            Pattern statNumber = Pattern.compile("(?<=\\+)(.*?)(?=\")");
            Matcher statNumberMatcher = statNumber.matcher(petStr);

            if(statNumberMatcher.find()) {
                List<String> matches = statNumberMatcher.results().map(MatchResult::group).toList();

                String petClimateLuck = matches.get(matches.size() - 7);
                String petClimateScale = matches.get(matches.size() - 5);
                String petLocationLuck = matches.get(matches.size() - 3);
                String petLocationScale = matches.getLast();

                float multiplier = findMultiplier(petStr);
                float total = Stream.of(petClimateLuck, petClimateScale, petLocationLuck, petLocationScale).mapToInt(Integer::parseInt).sum();

                StringBuilder builder = new StringBuilder(petStr);
                String petStrNew = petStr;

                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 9), " (" + TextHelper.floatToString((Float.parseFloat(petClimateLuck) * 4 / multiplier), 0) + "%)").toString();
                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 10), " (" + TextHelper.floatToString((Float.parseFloat(petClimateScale) * 4 / multiplier), 0) + "%)").toString();
                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 13), " (" + TextHelper.floatToString((Float.parseFloat(petLocationLuck) * 4 / multiplier), 0) + "%)").toString();
                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 14), " (" + TextHelper.floatToString((Float.parseFloat(petLocationScale) * 4 / multiplier), 0) + "%)").toString();
                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 16), " (" + TextHelper.floatToString((total / multiplier), 0) + "%)").toString();

                return TextHelper.jsonToText(json.replace(petStr, petStrNew));
            }
        }
        return text;
    }

    private static float findMultiplier(String petStr) {
        if (petStr.indexOf('\uf033') != -1) return 1f;
        else if (petStr.indexOf('\uf034') != -1) return 2f;
        else if (petStr.indexOf('\uf035') != -1) return 3f;
        else if (petStr.indexOf('\uf036') != -1) return 5f;
        else if (petStr.indexOf('\uf037') != -1) return 7.5f;
        return 1;
    }

    public void cleanChatTriggerStore(String[] chatTriggers) {
        for (String chatTrigger : chatTriggers) {
            if(storedChatTriggerText.containsKey(chatTrigger)) {
                CodeExecuterHandler.runLater(2, () -> {
                    storedChatTriggerText.put(chatTrigger, Text.empty());
                });
            }
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
        );
    }
    //endregion
}
