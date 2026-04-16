package dannypx.foe.handler.store;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Pattern;

public class CustomChatTriggerDataHandler extends Handler {
    private static CustomChatTriggerDataHandler INSTANCE = new CustomChatTriggerDataHandler();

    public static CustomChatTriggerDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomChatTriggerDataHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private CustomChatTriggerDataModel customChatTriggerData = new CustomChatTriggerDataModel();
    private boolean needsUpdate = false;

    public CustomChatTriggerDataModel getCustomChatTriggerData() {
        return customChatTriggerData;
    }

    public void setCustomChatTriggerData(CustomChatTriggerDataModel customChatTriggerData) {
        this.customChatTriggerData = customChatTriggerData;
        this.updateCustomChatTriggerData();
    }

    private void updateCustomChatTriggerData() {
        if(needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.CUSTOM_CHAT_TRIGGER_DATA);
        }
        this.needsUpdate = false;
    }
    //endregion

    //region Methods
    public void tick() {
        if(customChatTriggerData.uuid == null && minecraftClient.player != null) {
            customChatTriggerData.uuid = minecraftClient.player.getUuid();
        } else if(customChatTriggerData.uuid != null && this.needsUpdate) {
            this.updateCustomChatTriggerData();
        } else if(!CustomChatTriggerDataModel.CUSTOM_CHAT_TRIGGER_DATA_MODEL_VERSION.equals(customChatTriggerData.version)) {
            customChatTriggerData.version = CustomChatTriggerDataModel.CUSTOM_CHAT_TRIGGER_DATA_MODEL_VERSION;
            needsUpdate = true;
        }
    }

    public void init() {
        if(minecraftClient.player != null) this.setUUID(minecraftClient.player.getUuid());
    }

    private void setUUID(UUID uuid) {
        this.customChatTriggerData.uuid = uuid;
    }

    public void createNewCustomChatTrigger(String id) {
        customChatTriggerData.chatTriggerList.put(id, new CustomChatTrigger(id));
        needsUpdate = true;
    }

    public void createNewCustomChatTrigger(String id, CustomChatTrigger customChatTrigger) {
        customChatTriggerData.chatTriggerList.put(id, customChatTrigger);
        needsUpdate = true;
    }

    public CustomChatTrigger deleteCustomChatTrigger(String id) {
        needsUpdate = true;
        return customChatTriggerData.chatTriggerList.remove(id);
    }

    public void updateChatTrigger(String currentSelectedChatTrigger, String newName, String regex, String notificationToTrigger, String chatNotificationToTrigger, boolean useChatTrigger) {
        CustomChatTrigger newChatTrigger = customChatTriggerData.chatTriggerList.get(currentSelectedChatTrigger);

        if(!Objects.equals(currentSelectedChatTrigger, newName)) {
            newChatTrigger = deleteCustomChatTrigger(currentSelectedChatTrigger);
            currentSelectedChatTrigger = newName;
        }

        newChatTrigger.name = newName;
        newChatTrigger.regex = regex;
        newChatTrigger.pattern = Pattern.compile(regex);
        newChatTrigger.notificationToTrigger = notificationToTrigger;
        newChatTrigger.chatNotificationToTrigger = chatNotificationToTrigger;
        newChatTrigger.useChatTrigger = useChatTrigger;

        customChatTriggerData.chatTriggerList.put(currentSelectedChatTrigger, newChatTrigger);
        needsUpdate = true;
    }

    public void resetChatTriggers() {
        customChatTriggerData.chatTriggerList = new HashMap<>(CustomChatTriggerDataModel.defaultChatTriggers);

        needsUpdate = true;
    }
    //endregion

    //region Model
    public static class CustomChatTriggerDataModel extends DataModels.DataModel {
        private static final String CUSTOM_CHAT_TRIGGER_DATA_MODEL_VERSION = "0.2";

        private static final Map<String, CustomChatTrigger> defaultChatTriggers = Map.of(
                "Contest Type", new CustomChatTrigger(
                        "Contest Type",
                        "^Type:.*",
                        "",
                        "",
                        true
                ),
                "Contest Location", new CustomChatTrigger(
                        "Contest Location",
                        "^Location:.*",
                        "",
                        "",
                        true
                ),
                "Contest Level", new CustomChatTrigger(
                        "Contest Level",
                        "^Level:.*",
                        "",
                        "",
                        true
                ),
                "Contest 1st", new CustomChatTrigger(
                        "Contest 1st",
                        "^\uF060.*",
                        "",
                        "",
                        true
                ),
                "Contest 2nd", new CustomChatTrigger(
                        "Contest 2nd",
                        "^\uF061.*",
                        "",
                        "",
                        true
                ),
                "Contest 3rd", new CustomChatTrigger(
                        "Contest 3rd",
                        "^\uF062.*",
                        "",
                        "",
                        true
                ),
                "Contest Placement", new CustomChatTrigger(
                        "Contest Placement",
                        "^You →.*",
                        "",
                        "",
                        true
                )
        );

        //Name Chat Trigger, Chat Trigger
        public Map<String, CustomChatTrigger> chatTriggerList = new HashMap<>(defaultChatTriggers);

        public CustomChatTriggerDataModel() {
            super(CUSTOM_CHAT_TRIGGER_DATA_MODEL_VERSION, null);
        }
    }
    //endregion

    //region Notification Object
    public static class CustomChatTrigger {
        public String name;
        public String regex;
        public Pattern pattern;
        public String notificationToTrigger;
        public String chatNotificationToTrigger;
        public boolean useChatTrigger;

        public CustomChatTrigger(String name, String regex, String notificationToTrigger, String chatNotificationToTrigger, boolean useChatTrigger) {
            this.name = name;
            this.regex = regex;
            this.pattern = Pattern.compile(regex);
            this.notificationToTrigger = notificationToTrigger;
            this.chatNotificationToTrigger = chatNotificationToTrigger;
            this.useChatTrigger = useChatTrigger;
        }

        public CustomChatTrigger(String name) {
            this.name = name;
            this.regex = "";
            this.pattern = Pattern.compile("");
            this.notificationToTrigger = "";
            this.chatNotificationToTrigger = "";
            this.useChatTrigger = true;
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "customChatTriggerData", Pair.of(Text.literal("[customChatTriggerData]"), TextHelper.literal(getCustomChatTriggerData()))
        );
    }
    //endregion
}
