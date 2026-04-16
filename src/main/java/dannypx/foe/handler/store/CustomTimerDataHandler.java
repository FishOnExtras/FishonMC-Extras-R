package dannypx.foe.handler.store;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class CustomTimerDataHandler extends Handler {
    private static CustomTimerDataHandler INSTANCE = new CustomTimerDataHandler();

    public static CustomTimerDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomTimerDataHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private CustomTimerDataModel customTimerData = new CustomTimerDataModel();
    private boolean needsUpdate = false;

    public CustomTimerDataModel getCustomTimerData() {
        return customTimerData;
    }

    public void setCustomTimerData(CustomTimerDataModel customTimerData) {
        this.customTimerData = customTimerData;
        this.updateCustomTimerData();
    }

    private void updateCustomTimerData() {
        if(needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.CUSTOM_TIMER_DATA);
        }
        this.needsUpdate = false;
    }


    //endregion

    //region Methods
    public void tick() {
        if(customTimerData.uuid == null && minecraftClient.player != null) {
            customTimerData.uuid = minecraftClient.player.getUuid();
        } else if(customTimerData.uuid != null && this.needsUpdate) {
            this.updateCustomTimerData();
        } else if(!CustomTimerDataModel.CUSTOM_TIMER_DATA_MODEL_VERSION.equals(customTimerData.version)) {
            customTimerData.version = CustomTimerDataModel.CUSTOM_TIMER_DATA_MODEL_VERSION;
            needsUpdate = true;
        }
    }

    public void init() {
        if(minecraftClient.player != null) this.setUUID(minecraftClient.player.getUuid());
    }

    private void setUUID(UUID uuid) {
        this.customTimerData.uuid = uuid;
    }

    public void createNewCustomTimer(String id) {
        customTimerData.timerList.put(id, new CustomTimer(id));
        needsUpdate = true;
    }

    public void createNewCustomTimer(String id, CustomTimer customTimer) {
        customTimerData.timerList.put(id, customTimer);
        needsUpdate = true;
    }

    public CustomTimer deleteCustomTimer(String id) {
        needsUpdate = true;
        return customTimerData.timerList.remove(id);
    }

    public void updateTimer(String currentSelectedTimer, String newName, int timer, int offset, String notificationToTrigger, String chatNotificationToTrigger, String cleanUpChatTrigger, boolean useTimer, boolean isPeriod) {
        updateTimer(currentSelectedTimer, newName, timer, 0, offset, notificationToTrigger, "", chatNotificationToTrigger, "",cleanUpChatTrigger, useTimer, isPeriod);
    }

    public void updateTimer(String currentSelectedTimer, String newName, int timer, int offTimer, int offset, String notificationToTrigger, String notificationToTriggerEnd, String chatNotificationToTrigger, String chatNotificationToTriggerEnd, String cleanUpChatTrigger, boolean useTimer, boolean isPeriod) {
        if(!Objects.equals(currentSelectedTimer, newName)) {
            deleteCustomTimer(currentSelectedTimer);
            currentSelectedTimer = newName;
        }

        if(isPeriod) {
            CustomTimerPeriod newCustomTimerPeriod = new CustomTimerPeriod(newName, timer, offTimer, offset, notificationToTrigger, notificationToTriggerEnd, chatNotificationToTrigger, chatNotificationToTriggerEnd, cleanUpChatTrigger, useTimer, true);

            customTimerData.timerList.put(currentSelectedTimer, newCustomTimerPeriod);
        } else {
            CustomTimer newCustomTimer = new CustomTimer(newName, timer, offset, notificationToTrigger, chatNotificationToTrigger, cleanUpChatTrigger, useTimer, false);

            customTimerData.timerList.put(currentSelectedTimer, newCustomTimer);
        }

        needsUpdate = true;
    }

    public void resetTimers() {
        customTimerData.timerList = new HashMap<>(CustomTimerDataModel.defaultTimers);

        needsUpdate = true;
    }
    //endregion

    //region Model
    public static class CustomTimerDataModel extends DataModels.DataModel {
        private static final String CUSTOM_TIMER_DATA_MODEL_VERSION = "0.2";

        private static final Map<String, CustomTimer> defaultTimers = Map.of(
                "Contest Timer", new CustomTimerPeriod(
                        "Contest Timer",
                        1800,
                        1800,
                        -5,
                        "Contest Start",
                        "Contest End",
                        "",
                        "",
                        "Contest Type, Contest Location, Contest Level, Contest 1st, Contest 2nd, Contest 3rd, Contest Placement",
                        true,
                        true
                )
        );

        //Name Chat Trigger, Chat Trigger
        public Map<String, CustomTimer> timerList = new HashMap<>(defaultTimers);

        public CustomTimerDataModel() {
            super(CUSTOM_TIMER_DATA_MODEL_VERSION, null);
        }
    }
    //endregion

    //region Notification Object
    public static class CustomTimer {
        public String name;
        public int timer;
        public int offset;
        public String notificationToTrigger;
        public String chatNotificationToTrigger;
        public String cleanUpChatTrigger;
        public boolean useTimer;
        public boolean isPeriod;

        public CustomTimer(String name, int timer, int offset, String notificationToTrigger, String chatNotificationToTrigger, String cleanUpChatTrigger, boolean useTimer, boolean isPeriod) {
            this.name = name;
            this.timer = timer;
            this.offset = offset;
            this.notificationToTrigger = notificationToTrigger;
            this.chatNotificationToTrigger = chatNotificationToTrigger;
            this.cleanUpChatTrigger = cleanUpChatTrigger;
            this.useTimer = useTimer;
            this.isPeriod = isPeriod;
        }

        public CustomTimer(String name) {
            this.name = name;
            this.timer = 60;
            this.offset = 0;
            this.notificationToTrigger = "";
            this.chatNotificationToTrigger = "";
            this.cleanUpChatTrigger = "";
            this.useTimer = true;
            this.isPeriod = false;
        }
    }

    public static class CustomTimerPeriod extends CustomTimer {
        public int offTimer;
        public String notificationToTriggerEnd;
        public String chatNotificationToTriggerEnd;

        public CustomTimerPeriod(String name, int timer, int offTimer, int offset, String notificationToTrigger, String notificationToTriggerEnd, String chatNotificationToTrigger, String chatNotificationToTriggerEnd, String cleanUpChatTrigger, boolean useTimer, boolean isPeriod) {
            super(name, timer, offset, notificationToTrigger, chatNotificationToTrigger, cleanUpChatTrigger, useTimer, isPeriod);
            this.offTimer = offTimer;
            this.notificationToTriggerEnd = notificationToTriggerEnd;
            this.chatNotificationToTriggerEnd = chatNotificationToTriggerEnd;
        }

        public CustomTimerPeriod(String name) {
            super(name);
            this.offTimer = 60;
            this.notificationToTriggerEnd = "";
            this.chatNotificationToTriggerEnd = "";
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "customTimerData", Pair.of(Text.literal("[customTimerData]"), TextHelper.literal(getCustomTimerData()))
        );
    }
    //endregion
}
