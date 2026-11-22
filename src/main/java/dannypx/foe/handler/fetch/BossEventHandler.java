package dannypx.foe.handler.fetch;

import com.google.gson.*;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.mixin.accessor.BossHealthOverlayAccessor;
import java.util.*;

import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

public class BossEventHandler extends Handler {
    private static BossEventHandler INSTANCE = new BossEventHandler();

    public static BossEventHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new BossEventHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private MutableComponent location = Component.empty();
    private MutableComponent weather = Component.empty();
    private MutableComponent time = Component.empty();
    private MutableComponent temperature = Component.empty();
    private MutableComponent subLocation = Component.empty();
    private MutableComponent communityGoalCurrent = Component.empty();
    private MutableComponent communityGoalMax = Component.empty();
    private String prevBossEvent = "";

    public MutableComponent getLocation() {
        return location;
    }

    public MutableComponent getWeather() {
        return weather;
    }

    public MutableComponent getTime() {
        return time;
    }

    public MutableComponent getTemperature() {
        return temperature;
    }

    public MutableComponent getSubLocation() {
        return subLocation;
    }

    public MutableComponent getCommunityGoalCurrent() {
        return communityGoalCurrent;
    }

    public MutableComponent getCommunityGoalMax() {
        return communityGoalMax;
    }
    //endregion

    //region Methods
    public void tick() {
        this.fetchFromBossBar();
    }

    private void fetchFromBossBar() {
        Map<UUID, LerpingBossEvent> bossEventMap = ((BossHealthOverlayAccessor) (minecraft.gui.hud.getBossOverlay())).getEvents();
        if(!bossEventMap.isEmpty()) {
            bossEventMap.forEach(((uuid, lerpingBossEvent) -> {
                if(lerpingBossEvent.getName().getString().contains("\uF039") && !Objects.equals(prevBossEvent, lerpingBossEvent.getName().getString())) {
                    prevBossEvent = lerpingBossEvent.getName().getString();
                    String json = TextHelper.componentToJson(lerpingBossEvent.getName());
                    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

                    if(jsonObject.get("extra") != null) {
                        JsonObject locationObject = jsonObject.get("extra").getAsJsonArray().get(0).getAsJsonObject()
                                .get("extra").getAsJsonArray().get(0).getAsJsonObject();

                        location = TextHelper.substring(TextHelper.jsonToComponent(locationObject.toString()), 4);

                        if(location.getString().contains("(")) {
                            location = TextHelper.substring(location, 0, location.getString().indexOf("(") - 1);
                        }

                        JsonObject weatherObject = jsonObject.get("extra").getAsJsonArray().get(2).getAsJsonObject()
                                .get("extra").getAsJsonArray().get(0).getAsJsonObject();
                        weather = Component.literal(weatherObject.get("text").getAsString())
                                .withColor(TextColor.parseColor(weatherObject.get("color").getAsString()).getOrThrow().getValue());

                        if(weather.getString().trim().length() != 1) {
                            weather = Component.literal(weatherObject.get("text").getAsString().substring(0, 1))
                                    .withColor(TextColor.parseColor(weatherObject.get("color").getAsString()).getOrThrow().getValue());

                            time = Component.literal(weatherObject.get("text").getAsString().substring(1).trim())
                                    .withColor(TextColor.parseColor(weatherObject.get("color").getAsString()).getOrThrow().getValue());

                            if(weatherObject.get("extra") != null) {
                                JsonObject temperatureObject = weatherObject.get("extra").getAsJsonArray().get(0).getAsJsonObject();
                                temperature = Component.literal(temperatureObject.get("text").getAsString().trim())
                                        .withColor(TextColor.parseColor(temperatureObject.get("color").getAsString()).getOrThrow().getValue());
                            }
                            return;
                        }

                        if(weatherObject.get("extra") != null) {
                            JsonObject timeObject = weatherObject.get("extra").getAsJsonArray().get(0).getAsJsonObject();
                            time = Component.literal(timeObject.get("text").getAsString().trim())
                                    .withColor(TextColor.parseColor(timeObject.get("color").getAsString()).getOrThrow().getValue());

                            if(timeObject.get("extra") != null) {
                                JsonObject temperatureObject = timeObject.get("extra").getAsJsonArray().get(0).getAsJsonObject();
                                temperature = Component.literal(temperatureObject.get("text").getAsString().trim())
                                        .withColor(TextColor.parseColor(temperatureObject.get("color").getAsString()).getOrThrow().getValue());
                            }
                        }

                        if(jsonObject.get("extra").getAsJsonArray().size() > 4 && lerpingBossEvent.getName().getString().contains("Community Goal:")) {
                            JsonObject communityGoalObject = jsonObject.get("extra").getAsJsonArray().get(4).getAsJsonObject()
                                    .get("extra").getAsJsonArray().get(0).getAsJsonObject().get("extra").getAsJsonArray().get(0).getAsJsonObject();

                            communityGoalCurrent = Component.literal(communityGoalObject.get("text").getAsString().trim())
                                    .withColor(TextColor.parseColor(communityGoalObject.get("color").getAsString()).getOrThrow().getValue());

                            JsonObject communityGoalMaxObject = communityGoalObject.get("extra").getAsJsonArray().get(0).getAsJsonObject()
                                    .get("extra").getAsJsonArray().get(0).getAsJsonObject();

                            communityGoalMax = Component.literal(communityGoalMaxObject.get("text").getAsString().trim())
                                    .withColor(TextColor.parseColor(communityGoalMaxObject.get("color").getAsString()).getOrThrow().getValue());
                        }
                    }
                } else if(lerpingBossEvent.getName().getString().contains("\uA201\uEEE1\uA208")) {
                    JsonObject jsonObject = JsonParser.parseString(TextHelper.componentToJson(lerpingBossEvent.getName())).getAsJsonObject();

                    if(jsonObject.get("extra") != null) {
                        JsonObject locationObject = jsonObject.get("extra").getAsJsonArray().get(0).getAsJsonObject();
                        subLocation = Component.literal(locationObject.get("text").getAsString())
                                .withColor(TextColor.parseColor(locationObject.get("color").getAsString()).getOrThrow().getValue());
                    }
                } else {
                    subLocation = Component.empty();
                }
            }));
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "location", Pair.of(getLocation(), Component.empty()),
                "weather", Pair.of(getWeather(), Component.empty()),
                "time", Pair.of(getTime(), Component.empty()),
                "temperature", Pair.of(getTemperature(), Component.empty()),
                "subLocation", Pair.of(getSubLocation(), Component.empty())
        );
    }
    //endregion
}
