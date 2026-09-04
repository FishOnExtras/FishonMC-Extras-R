package dannypx.foe.handler.store;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.handler.logic.UpdateHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.placeholder.evaluator.PlaceholderResult;
import dannypx.foe.placeholder.handler.PlaceholderHandlerV2;
import dannypx.foe.type.custom_value.*;
import dannypx.foe.type.tracker.TrackerAction;
import dannypx.foe.type.tracker.TrackerType;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.tuple.Triplet;
import dannypx.foe.type.version.Version;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

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
        Map<String, CustomTracker> trackerList = customTrackerData.trackerList;

        trackerList.forEach((key, customTracker) -> {
            if(!customTracker.isPersistent) {
                customTracker.value = customTracker.defaultValue;
                trackerList.put(key, customTracker);
            }
        });

        customTrackerData.trackerList = trackerList;

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
        if(customTrackerData.uuid == null && minecraft.player != null) {
            customTrackerData.uuid = minecraft.player.getUUID();
        } else if(customTrackerData.uuid != null && this.needsUpdate) {
            this.updateCustomTrackerData();
        } else if(!CustomTrackerDataModel.CUSTOM_TRACKER_DATA_MODEL_VERSION.equals(customTrackerData.version)) {
            this.updateDefault();
            customTrackerData.version = CustomTrackerDataModel.CUSTOM_TRACKER_DATA_MODEL_VERSION;
            needsUpdate = true;
        }
    }

    public void init() {
        if(minecraft.player != null) this.setUUID(minecraft.player.getUUID());
    }

    private void setUUID(UUID uuid) {
        this.customTrackerData.uuid = uuid;
    }

    public void createNewCustomTracker(String id) {
        customTrackerData.trackerList.put(id, new CustomTracker(id));
        needsUpdate = true;
    }

    public void createNewCustomTracker(String id, CustomTracker customTracker) {
        customTrackerData.trackerList.put(id, customTracker);
        needsUpdate = true;
    }

    public CustomTracker deleteCustomTracker(String id) {
        needsUpdate = true;
        return customTrackerData.trackerList.remove(id);
    }

    public void updateTracker(String currentSelectedTracker,
                              String newName,
                              TrackerType trackerType,
                              TrackerValue defaultValue,
                              TrackerValue value,
                              boolean isPersistent,
                              boolean useTracker,
                              Map<String, Triplet<TrackerAction, String, TrackerValue>> actions) {
        CustomTracker newTracker = customTrackerData.trackerList.get(currentSelectedTracker);

        if(!Objects.equals(currentSelectedTracker, newName)) {
            newTracker = deleteCustomTracker(currentSelectedTracker);
            currentSelectedTracker = newName;
        }

        newTracker.name = newName;
        newTracker.trackerType = trackerType;
        newTracker.defaultValue = defaultValue;
        newTracker.value = value;
        newTracker.isPersistent = isPersistent;
        newTracker.useTracker = useTracker;
        newTracker.actions = actions;

        customTrackerData.trackerList.put(currentSelectedTracker, newTracker);
        needsUpdate = true;
    }

    public void updateTracker(String[] trackerAndActionCodes) {
        for (String trackerAndActionCode : trackerAndActionCodes) {
            if(trackerAndActionCode != null && !trackerAndActionCode.isEmpty()) {
                String[] trackerAndActionSplitString = trackerAndActionCode.trim().split("\\.");

                if(trackerAndActionSplitString.length == 2 && customTrackerData.trackerList.containsKey(trackerAndActionSplitString[0])) {
                    CustomTracker tracker = customTrackerData.trackerList.get(trackerAndActionSplitString[0]);

                    if(tracker.actions.containsKey(trackerAndActionSplitString[1])) {
                        Triplet<TrackerAction, String, TrackerValue> action = tracker.actions.get(trackerAndActionSplitString[1]);

                        PlaceholderResult condition = PlaceholderHandlerV2.instance().resolve(action.value2());

                        if((condition.success()[0] && !condition.success()[1]) && (Boolean.parseBoolean(condition.text().getString()) || condition.text().getString().isBlank())) {

                            switch (tracker.trackerType) {
                                case BOOLEAN -> {
                                    BooleanValue valueToUse = action.value3() instanceof EmptyValue
                                            ? (BooleanValue) BooleanValue.getFalse()
                                            : action.value3() instanceof PlaceholderStringValue(String value)
                                              ? (BooleanValue) BooleanValue.of(Boolean.parseBoolean(PlaceholderHandlerV2.instance().resolve(value).text().getString()))
                                              : (BooleanValue) action.value3();
                                    BooleanValue value = (BooleanValue) tracker.value;

                                    switch (action.value1()) {
                                        case SET -> tracker.value = valueToUse;
                                        case TOGGLE -> tracker.value =  value.toggleValue();
                                    }
                                }
                                case INTEGER -> {
                                    NumberValue valueToUse = action.value3() instanceof PlaceholderStringValue(String value)
                                            ? (NumberValue) NumberValue.of(Float.parseFloat(PlaceholderHandlerV2.instance().resolve(value).text().getString()))
                                            : (NumberValue) action.value3();
                                    NumberValue value = (NumberValue) tracker.value;

                                    switch (action.value1()) {
                                        case SET -> tracker.value = valueToUse;
                                        case ADD -> tracker.value = value.addValue(valueToUse.value());
                                        case SUBTRACT -> tracker.value = value.subtractValue(valueToUse.value());
                                    }
                                }
                                case ITEMSTACK -> {
                                    TrackerValue valueToUse;

                                    if(action.value3() instanceof PlaceholderStringValue(String value)) {
                                        int index = Integer.parseInt(PlaceholderHandlerV2.instance().resolve(value).text().getString());

                                        if(index >= 0) {
                                            try {
                                                if(minecraft.screen instanceof ContainerScreen genericContainerScreen) {
                                                    valueToUse = ItemStackValue.of(genericContainerScreen.getMenu().slots.get(index).getItem());
                                                } else {
                                                    valueToUse = ItemStackValue.of(minecraft.player.getInventory().getItem(index));
                                                }
                                            } catch (IndexOutOfBoundsException e) {
                                                valueToUse = ItemStackValue.of(ItemStack.EMPTY);
                                            }
                                        } else {
                                            valueToUse = ItemStackValue.of(ItemStack.EMPTY);
                                        }
                                    } else if (action.value3() instanceof ItemStackValue(Pair<ItemStack, String> value)) {
                                        valueToUse = ItemStackValue.of(value.value2());
                                    } else valueToUse = action.value3();

                                    switch (action.value1()) {
                                        case SET -> tracker.value = valueToUse;
                                    }
                                }
                            }
                        }
                    }

                    customTrackerData.trackerList.put(trackerAndActionSplitString[0], tracker);

                    if(tracker.isPersistent) {
                        needsUpdate = true;
                    }
                }
            }
        }
    }

    public void updateTracker(String tracker, TrackerAction trackerAction, TrackerValue valueToUse) {
        if(customTrackerData.trackerList.containsKey(tracker)) {
            switch (trackerAction) {
                case SET -> {
                    CustomTracker newTracker = customTrackerData.trackerList.get(tracker);

                    if(valueToUse instanceof BooleanValue || valueToUse instanceof NumberValue || valueToUse instanceof ItemStackValue) {
                        newTracker.value = valueToUse;
                    } else if(valueToUse instanceof PlaceholderStringValue(String value1)) {
                        newTracker.value = BooleanValue.of(Boolean.parseBoolean(PlaceholderHandlerV2.instance().resolve(value1).text().getString()));
                    }

                    customTrackerData.trackerList.put(tracker, newTracker);
                    needsUpdate = true;
                }
                case TOGGLE -> {
                    CustomTracker newTracker = customTrackerData.trackerList.get(tracker);

                    if(newTracker.value instanceof BooleanValue currentValue) {
                        newTracker.value = currentValue.toggleValue();
                    }

                    customTrackerData.trackerList.put(tracker, newTracker);
                    needsUpdate = true;
                }
                case ADD -> {
                    CustomTracker newTracker = customTrackerData.trackerList.get(tracker);

                    if(newTracker.value instanceof NumberValue currentValue
                            && valueToUse instanceof NumberValue(Float value1)
                    ) {
                        newTracker.value = currentValue.addValue(value1);
                    } else if(newTracker.value instanceof NumberValue currentValue
                            && valueToUse instanceof PlaceholderStringValue(String value1)
                    ) {
                        newTracker.value = currentValue.addValue(Float.parseFloat(PlaceholderHandlerV2.instance().resolve(value1).text().getString()));
                    }

                    customTrackerData.trackerList.put(tracker, newTracker);
                    needsUpdate = true;
                }
                case SUBTRACT -> {
                    CustomTracker newTracker = customTrackerData.trackerList.get(tracker);

                    if(newTracker.value instanceof NumberValue currentValue
                            && valueToUse instanceof NumberValue(Float value1)
                    ) {
                        newTracker.value = currentValue.subtractValue(value1);
                    } else if(newTracker.value instanceof NumberValue currentValue
                            && valueToUse instanceof PlaceholderStringValue(String value1)
                    ) {
                        newTracker.value = currentValue.subtractValue(Float.parseFloat(PlaceholderHandlerV2.instance().resolve(value1).text().getString()));
                    }

                    customTrackerData.trackerList.put(tracker, newTracker);
                    needsUpdate = true;
                }
            }
        }
    }

    public void updateDefault() {
        CustomTrackerDataModel.defaultTrackers.forEach((key, timer) -> {
            customTrackerData.trackerList.putIfAbsent(key, timer);
        });
    }

    public void fixDefault() {
        customTrackerData.trackerList.putAll(CustomTrackerDataModel.defaultTrackers);
        needsUpdate = true;
    }

    public void resetTrackers() {
        customTrackerData.trackerList = new HashMap<>(CustomTrackerDataModel.defaultTrackers);

        needsUpdate = true;
    }
    //endregion

    //region Model
    public static class CustomTrackerDataModel extends DataModels.DataModel {
        private static final String CUSTOM_TRACKER_DATA_MODEL_VERSION = "0.3";

        private static final Map<String, CustomTracker> defaultTrackers = Map.of(
                "FabledEvent", new CustomTracker(
                        "FabledEvent",
                        TrackerType.BOOLEAN,
                        BooleanValue.getFalse(),
                        BooleanValue.getFalse(),
                        false,
                        true,
                        new HashMap<>(Map.of(
                                "SetFalse", Triplet.of(TrackerAction.SET, "", BooleanValue.getFalse()),
                                "SetTrue", Triplet.of(TrackerAction.SET, "", BooleanValue.getTrue())
                        ))
                ),
                "FabledDrystreak", new CustomTracker(
                        "FabledDrystreak",
                        TrackerType.INTEGER,
                        NumberValue.of(0f),
                        NumberValue.of(0f),
                        true,
                        true,
                        new HashMap<>(Map.of(
                                "Add", Triplet.of(TrackerAction.ADD, "%and.(<condition.(<catch.last_caught.fish.variant.fabled.id>!=fabled)>,<condition.(<tracker_data.data.FabledEvent.value>==true)>)%", NumberValue.of(1f)),
                                "Set", Triplet.of(TrackerAction.SET, "%and.(<condition.(<catch.last_caught.fish.variant.fabled.id>==fabled)>,<condition.(<tracker_data.data.FabledEvent.value>==true)>)%", NumberValue.of(0f))
                        ))
                )
        );

        //Name Tracker, Tracker
        public Map<String, CustomTracker> trackerList = new HashMap<>(defaultTrackers);

        public CustomTrackerDataModel() {
            super(CUSTOM_TRACKER_DATA_MODEL_VERSION, null);
        }
    }
    //endregion

    //region Tracker Object
    public static class CustomTracker {
        private String name;
        private TrackerType trackerType;
        private TrackerValue defaultValue;
        private TrackerValue value;
        private boolean isPersistent;
        private boolean useTracker;

        public String getName() {
            return name != null ? name : "";
        }

        public TrackerType getTrackerType() {
            return trackerType != null ? trackerType : TrackerType.BOOLEAN;
        }

        public TrackerValue getDefaultValue() {
            return defaultValue != null ? defaultValue : BooleanValue.getFalse();
        }

        public TrackerValue getValue() {
            return value != null ? value : BooleanValue.getFalse();
        }

        public boolean isPersistent() {
            return isPersistent;
        }

        public boolean isUseTracker() {
            return useTracker;
        }

        public Map<String, Triplet<TrackerAction, String, TrackerValue>> getActions() {
            return actions != null ? actions : new HashMap<>();
        }

        // ActionID, ActionType, Placeholder Condition, Placeholder Value
        public Map<String, Triplet<TrackerAction, String, TrackerValue>> actions;

        public CustomTracker(
                String name,
                TrackerType trackerType,
                TrackerValue defaultValue,
                TrackerValue value,
                boolean isPersistent,
                boolean useTracker,
                Map<String, Triplet<TrackerAction, String, TrackerValue>> actions
        ) {
            this.name = name;
            this.trackerType = trackerType;
            this.defaultValue = defaultValue;
            this.value = value;
            this.isPersistent = isPersistent;
            this.useTracker = useTracker;
            this.actions = actions;
        }

        public CustomTracker(String name) {
            this.name = name;
            this.trackerType = TrackerType.BOOLEAN;
            this.defaultValue = BooleanValue.getFalse();
            this.value = BooleanValue.getFalse();
            this.isPersistent = true;
            this.useTracker = true;
            this.actions = new HashMap<>(Map.of(
                    "Action #" + UUID.randomUUID(), Triplet.of(TrackerAction.SET, "", BooleanValue.getFalse())
            ));
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "customTrackerData", Pair.of(Component.literal("[customTrackerData]"), TextHelper.literal(getCustomTrackerData()))
        );
    }
    //endregion
}
