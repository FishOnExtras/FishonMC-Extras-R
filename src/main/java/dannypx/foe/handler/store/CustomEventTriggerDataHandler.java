package dannypx.foe.handler.store;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.event.EventTrigger;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class CustomEventTriggerDataHandler extends Handler {
    private static CustomEventTriggerDataHandler INSTANCE = new CustomEventTriggerDataHandler();

    public static CustomEventTriggerDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomEventTriggerDataHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private CustomEventTriggerDataModel customEventTriggerData = new CustomEventTriggerDataModel();
    private boolean needsUpdate = false;

    public CustomEventTriggerDataModel getCustomEventTriggerData() {
        return customEventTriggerData;
    }

    public void setCustomEventTriggerData(CustomEventTriggerDataModel customEventTriggerData) {
        this.customEventTriggerData = customEventTriggerData;
        this.updateCustomEventTriggerData();
    }

    private void updateCustomEventTriggerData() {
        if(needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.CUSTOM_EVENT_TRIGGER_DATA);
        }
        this.needsUpdate = false;
    }
    //endregion

    //region Methods
    public void tick() {
        if(customEventTriggerData.uuid == null && minecraftClient.player != null) {
            customEventTriggerData.uuid = minecraftClient.player.getUuid();
        } else if(customEventTriggerData.uuid != null && this.needsUpdate) {
            this.updateCustomEventTriggerData();
        } else if(!CustomEventTriggerDataModel.CUSTOM_EVENT_TRIGGER_DATA_MODEL_VERSION.equals(customEventTriggerData.version)) {
            customEventTriggerData.version = CustomEventTriggerDataModel.CUSTOM_EVENT_TRIGGER_DATA_MODEL_VERSION;
            needsUpdate = true;
        }
    }

    public void init() {
        if(minecraftClient.player != null) this.setUUID(minecraftClient.player.getUuid());
    }

    private void setUUID(UUID uuid) {
        this.customEventTriggerData.uuid = uuid;
    }

    public void createNewCustomEventTrigger(String id) {
        customEventTriggerData.eventTriggerList.put(id, new CustomEventTrigger(id));
        needsUpdate = true;
    }

    public void createNewCustomEventTrigger(String id, CustomEventTrigger customEventTrigger) {
        customEventTriggerData.eventTriggerList.put(id, customEventTrigger);
        needsUpdate = true;
    }

    public CustomEventTrigger deleteCustomEventTrigger(String id) {
        needsUpdate = true;
        return customEventTriggerData.eventTriggerList.remove(id);
    }

    public void updateEventTrigger(String currentSelectedEventTrigger, String newName, EventTrigger eventTrigger, String notificationToTrigger, String chatNotificationToTrigger, boolean useEventTrigger) {
        CustomEventTrigger newEventTrigger = customEventTriggerData.eventTriggerList.get(currentSelectedEventTrigger);

        if(!Objects.equals(currentSelectedEventTrigger, newName)) {
            newEventTrigger = deleteCustomEventTrigger(currentSelectedEventTrigger);
            currentSelectedEventTrigger = newName;
        }

        newEventTrigger.name = newName;
        newEventTrigger.event = eventTrigger;
        newEventTrigger.notificationToTrigger = notificationToTrigger;
        newEventTrigger.chatNotificationToTrigger = chatNotificationToTrigger;
        newEventTrigger.useEventTrigger = useEventTrigger;

        customEventTriggerData.eventTriggerList.put(currentSelectedEventTrigger, newEventTrigger);
        needsUpdate = true;
    }

    public void resetEventTrigger() {
        customEventTriggerData.eventTriggerList = new HashMap<>(CustomEventTriggerDataModel.defaultEventTriggers);

        needsUpdate = true;
    }
    //endregion

    //region Model
    public static class CustomEventTriggerDataModel extends DataModels.DataModel {
        private static final String CUSTOM_EVENT_TRIGGER_DATA_MODEL_VERSION = "0.1";

        private static final Map<String, CustomEventTrigger> defaultEventTriggers = Map.of(

        );

        //Name Notification, Notification
        public Map<String, CustomEventTrigger> eventTriggerList = new HashMap<>(defaultEventTriggers);

        public CustomEventTriggerDataModel() {
            super(CUSTOM_EVENT_TRIGGER_DATA_MODEL_VERSION, null);
        }
    }
    //endregion

    //region Event Trigger Object
    public static class CustomEventTrigger {
        public String name;
        public EventTrigger event;
        public String notificationToTrigger;
        public String chatNotificationToTrigger;
        public boolean useEventTrigger;

        public CustomEventTrigger(String name, EventTrigger event, String notificationToTrigger, String chatNotificationToTrigger, boolean useEventTrigger) {
            this.name = name;
            this.event = event;
            this.notificationToTrigger = notificationToTrigger;
            this.chatNotificationToTrigger = chatNotificationToTrigger;
            this.useEventTrigger = useEventTrigger;
        }

        public CustomEventTrigger(String name) {
            this.name = name;
            this.event = EventTrigger.DEFAULT;
            this.notificationToTrigger = "";
            this.chatNotificationToTrigger = "";
            this.useEventTrigger = true;
        }
    }
    //

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "customEventTriggerData", Pair.of(Text.literal("[customEventTriggerData]"), TextHelper.literal(getCustomEventTriggerData()))
        );
    }
    //endregion
}
