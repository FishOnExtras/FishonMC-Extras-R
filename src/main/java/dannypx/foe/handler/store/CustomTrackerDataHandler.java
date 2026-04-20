package dannypx.foe.handler.store;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.custom_value.BooleanValue;
import dannypx.foe.type.custom_value.TrackerValue;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.tuple.Quartet;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.*;

public class CustomTrackerDataHandler extends Handler {
    private static CustomTrackerDataHandler INSTANCE = new CustomTrackerDataHandler();

    public static CustomTrackerDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomTrackerDataHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private CustomTrackerDataModel customTrackerData = new CustomTrackerDataModel();

    private boolean needsUpdate = false;

    public CustomTrackerDataModel getCustomTrackerData() {
        return customTrackerData;
    }

    public void setCustomTrackerData(CustomTrackerDataModel customTrackerData) {
        this.customTrackerData = customTrackerData;
        this.updateCustomTrackerData();
    }

    private void updateCustomTrackerData() {
        if(needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.CUSTOM_TRACKER_DATA);
        }
        this.needsUpdate = false;
    }
    //endregion

    //region Methods
    public void tick() {
        if(customTrackerData.uuid == null && minecraftClient.player != null) {
            customTrackerData.uuid = minecraftClient.player.getUuid();
        } else if(customTrackerData.uuid != null && this.needsUpdate) {
            this.updateCustomTrackerData();
        } else if(!CustomTrackerDataModel.CUSTOM_TRACKER_DATA_MODEL_VERSION.equals(customTrackerData.version)) {
            customTrackerData.version = CustomTrackerDataModel.CUSTOM_TRACKER_DATA_MODEL_VERSION;
            needsUpdate = true;
        }
    }

    public void init() {
        if(minecraftClient.player != null) {
            this.setUUID(minecraftClient.player.getUuid());
        }
    }

    private void setUUID(UUID uuid) {
        this.customTrackerData.uuid = uuid;
    }

    public void createNewCustomTracker(String id) {
        customTrackerData.customTrackerList.put(id, new CustomTracker(id));
        needsUpdate = true;
    }

    public void createNewCustomTracker(String id, CustomTracker customTracker) {
        customTrackerData.customTrackerList.put(id, customTracker);
        needsUpdate = true;
    }

    public CustomTracker deleteCustomTracker(String id) {
        needsUpdate = true;
        return customTrackerData.customTrackerList.remove(id);
    }

    public void updateTracker(String currentSelectedTracker, String newName, TrackerType trackerType, TrackerValue defaultValue, TrackerValue value, boolean shouldReset, List<Quartet<String, TrackerAction, String, String>> actions) {
        CustomTracker newTracker = customTrackerData.customTrackerList.get(currentSelectedTracker);

        if(!Objects.equals(currentSelectedTracker, newName)) {
            newTracker = deleteCustomTracker(currentSelectedTracker);
            currentSelectedTracker = newName;
        }

        newTracker.name = newName;
        newTracker.trackerType = trackerType;
        newTracker.defaultValue = defaultValue;
        newTracker.value = value;
        newTracker.shouldReset = shouldReset;
        newTracker.actions = actions;

        customTrackerData.customTrackerList.put(currentSelectedTracker, newTracker);
        needsUpdate = true;
    }

    public void resetTrackers() {
        customTrackerData.customTrackerList = new HashMap<>(CustomTrackerDataModel.defaultTrackers);

        needsUpdate = true;
    }
    //endregion

    //region Model
    public static class CustomTrackerDataModel extends DataModels.DataModel {
        private static final String CUSTOM_TRACKER_DATA_MODEL_VERSION = "0.1";

        private static final Map<String, CustomTracker> defaultTrackers = Map.of(

        );

        public Map<String, CustomTracker> customTrackerList = new HashMap<>(defaultTrackers);

        public CustomTrackerDataModel() {
            super(CUSTOM_TRACKER_DATA_MODEL_VERSION, null);
        }
    }
    //endregion

    //region Tracker Object
    public static class CustomTracker {
        public String name;
        public TrackerType trackerType;
        public TrackerValue defaultValue;
        public TrackerValue value;
        public boolean shouldReset;
        // ActionID, ActionType, ActionCondition, Value
        public List<Quartet<String, TrackerAction, String, String>> actions;

        public CustomTracker(
                String name,
                TrackerType trackerType,
                TrackerValue defaultValue,
                TrackerValue value,
                boolean shouldReset,
                List<Quartet<String, TrackerAction, String, String>> actions
        ) {
            this.name = name;
            this.trackerType = trackerType;
            this.defaultValue = defaultValue;
            this.value = value;
            this.shouldReset = shouldReset;
            this.actions = actions;
        }

        public CustomTracker(String name) {
            this.name = name;
            this.trackerType = TrackerType.BOOLEAN;
            this.defaultValue = BooleanValue.getDefault();
            this.value = BooleanValue.getDefault();
            this.shouldReset = true;
            this.actions = new ArrayList<>(List.of(
                    Quartet.of("Example Action", TrackerAction.SET, "", "false")
            ));
        }
    }

    public enum TrackerAction {
        SET,
        TOGGLE,
        ADD,
        SUBTRACT;

        public static List<TrackerAction> getActions(TrackerType trackerType) {
            return switch (trackerType) {
                case BOOLEAN -> List.of(SET, TOGGLE);
                case INTEGER -> List.of(SET, ADD, SUBTRACT);
            };
        }
    }

    public enum TrackerType {
        BOOLEAN,
        INTEGER
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "customTrackerData", Pair.of(Text.literal("[customTrackerData]"), TextHelper.literal(getCustomTrackerData()))
        );
    }
    //endregion
}
