package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.fetch.ChatHandler;
import dannypx.foe.type.custom_text.CustomTextValue;
import dannypx.foe.type.custom_text.StringValue;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.handler.store.CustomTimerDataHandler;
import dannypx.foe.type.tuple.Triplet;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Pattern;

public class TimerHandler extends Handler {
    private static TimerHandler INSTANCE = new TimerHandler();

    public static TimerHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new TimerHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private List<CustomTimerDataHandler.CustomTimer> timers = new ArrayList<>();

    private Map<CustomTimerDataHandler.CustomTimer, Runnable> callbacks = new HashMap<>();
    private Map<CustomTimerDataHandler.CustomTimer, Runnable> endOfOnCallbacks = new HashMap<>();
    private Map<CustomTimerDataHandler.CustomTimer, Runnable> endOfOffCallbacks = new HashMap<>();
    private Map<CustomTimerDataHandler.CustomTimer, Long> lastTrigger = new HashMap<>();
    private Map<CustomTimerDataHandler.CustomTimer, Long> lastPos = new HashMap<>();

    public Pair<Boolean, CustomTextValue> getTimer(String[] params) {
        if(params.length > 1) {
            Pattern fieldPattern = Pattern.compile("^(timer|offset|notification_to_trigger|clean_up_chat_trigger|use_timer|is_period|off_timer|notification_to_trigger_end|time)$");

            CustomTimerDataHandler.CustomTimer timer = timers.stream().filter(t -> Objects.equals(t.name, params[0])).findFirst().orElse(null);

            if(timer != null) {
                if(fieldPattern.matcher(params[1]).matches()) {
                    return switch (params[1]) {
                        case "timer" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(timer.timer)));
                        case "offset" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(timer.offset)));
                        case "notification_to_trigger" -> PlaceholderHandler.getTextValue(new StringValue(timer.notificationToTrigger));
                        case "clean_up_chat_trigger" -> PlaceholderHandler.getTextValue(new StringValue(timer.cleanUpChatTrigger));
                        case "use_timer" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(timer.useTimer)));
                        case "is_period" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(timer.isPeriod)));
                        case "off_timer" -> {
                            if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod) {
                                yield PlaceholderHandler.getTextValue(new StringValue(String.valueOf(timerPeriod.offTimer)));
                            }
                            yield PlaceholderHandler.noResult();
                        }
                        case "notification_to_trigger_end" -> {
                            if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod) {
                                yield PlaceholderHandler.getTextValue(new StringValue(String.valueOf(timerPeriod.notificationToTriggerEnd)));
                            }
                            yield PlaceholderHandler.noResult();
                        }
                        case "time" -> {
                            if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod
                                && params.length >= 3
                            ) {
                                long cycle = timerPeriod.timer + timerPeriod.offTimer;
                                long adjusted = System.currentTimeMillis() / 1000 + timerPeriod.offset;
                                long pos = adjusted % cycle;

                                long secondsUntilNextOn;
                                long secondsUntilNextOff;
                                Triplet<Long, Long, Long> remainingTimeOn;
                                Triplet<Long, Long, Long> remainingTimeOff;
                                boolean isOn;

                                if(pos < timerPeriod.timer) {
                                    secondsUntilNextOn = timerPeriod.timer - pos;
                                    secondsUntilNextOff = cycle - pos;
                                    remainingTimeOn = getTime(secondsUntilNextOn);
                                    remainingTimeOff = getTime(secondsUntilNextOff);
                                    isOn = true;
                                } else {
                                    secondsUntilNextOn = (cycle - pos) + timerPeriod.timer;
                                    secondsUntilNextOff = cycle - pos;
                                    remainingTimeOn = getTime(secondsUntilNextOn);
                                    remainingTimeOff = getTime(secondsUntilNextOff);
                                    isOn = false;
                                }

                                yield switch (params[2]) {
                                    case "on" -> switch (params[3]) {
                                        case "second" -> PlaceholderHandler.getTextValue(new StringValue(String.format(Locale.US, "%02d", remainingTimeOn.value1())));
                                        case "minute" -> PlaceholderHandler.getTextValue(new StringValue(String.format(Locale.US, "%02d", remainingTimeOn.value2())));
                                        case "hour" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(remainingTimeOn.value3())));
                                        default -> PlaceholderHandler.noResult();
                                    };
                                    case "off" -> switch (params[3]) {
                                        case "second" -> PlaceholderHandler.getTextValue(new StringValue(String.format(Locale.US, "%02d", remainingTimeOff.value1())));
                                        case "minute" -> PlaceholderHandler.getTextValue(new StringValue(String.format(Locale.US, "%02d", remainingTimeOff.value2())));
                                        case "hour" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(remainingTimeOff.value3())));
                                        default -> PlaceholderHandler.noResult();
                                    };
                                    case "is_on" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(isOn)));
                                    case "is_off" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(!isOn)));
                                    default -> PlaceholderHandler.noResult();
                                };
                            } else if (params.length == 3) {
                                long timeSeconds = System.currentTimeMillis() / 1000;
                                long adjusted = timeSeconds + timer.offset;
                                long pos = adjusted % timer.timer;
                                long remaining = timer.timer - pos;

                                Triplet<Long, Long, Long> remainingTime = getTime(remaining);

                                yield switch (params[2]) {
                                    case "second" -> PlaceholderHandler.getTextValue(new StringValue(String.format(Locale.US, "%02d", remainingTime.value1())));
                                    case "minute" -> PlaceholderHandler.getTextValue(new StringValue(String.format(Locale.US, "%02d", remainingTime.value2())));
                                    case "hour" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(remainingTime.value3())));
                                    default -> PlaceholderHandler.noResult();
                                };
                            } else {
                                yield PlaceholderHandler.noResult();
                            }
                        }
                        default -> PlaceholderHandler.noResult();
                    };
                }
            }
        }
        return PlaceholderHandler.noResult();
    }

    private Triplet<Long, Long, Long> getTime(long seconds) {
        long hour = seconds / 3600;
        long minute = (seconds % 3600) / 60;
        long second = seconds % 60;

        return Triplet.of(second, minute, hour);
    }
    //endregion

    //region Methods
    public void tick() {
        long timeSeconds = System.currentTimeMillis() / 1000;

        timers.forEach(timer -> {
            long adjustedWithOffset = timeSeconds + timer.offset;

            if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod) {
                long cycle = timerPeriod.timer + timerPeriod.offTimer;
                if(cycle > 0) {
                    long pos = adjustedWithOffset % cycle;
                    long prevPos = lastPos.getOrDefault(timerPeriod, pos);

                    if (crossed(prevPos, pos, 0)) {
                        this.triggerTimer(timerPeriod, timeSeconds, endOfOffCallbacks.get(timerPeriod));
                    }

                    if (crossed(prevPos, pos, timerPeriod.timer)) {
                        this.triggerTimer(timerPeriod, timeSeconds, endOfOnCallbacks.get(timerPeriod));

                        // Clean Chat Trigger
                        String[] chatTriggers = timer.cleanUpChatTrigger.trim().split("\\s*,\\s*");
                        ChatHandler.instance().cleanChatTriggerStore(chatTriggers);
                    }

                    lastPos.put(timerPeriod, pos);
                }
            } else {
                if(timer.timer > 0) {
                    long interval = timer.timer;
                    long pos = adjustedWithOffset % interval;
                    long prevPos = lastPos.getOrDefault(timer, pos);

                    if (crossed(prevPos, pos, 0)) {
                        this.triggerTimer(timer, timeSeconds, callbacks.get(timer));

                        // Clean Chat Trigger
                        String[] chatTriggers = timer.cleanUpChatTrigger.trim().split("\\s*,\\s*");
                        ChatHandler.instance().cleanChatTriggerStore(chatTriggers);
                    }

                    lastPos.put(timer, pos);
                }
            }
        });
    }

    public void init() {
        this.initTimers();
    }

    public void initTimers() {
        timers.clear();
        callbacks.clear();
        endOfOnCallbacks.clear();
        endOfOffCallbacks.clear();
        lastTrigger.clear();
        lastPos.clear();

        List<CustomTimerDataHandler.CustomTimer> tempTimers = new ArrayList<>();
        CustomTimerDataHandler.instance().getCustomTimerData().timerList.forEach((name, timer) -> {
            if(timer.useTimer) {
                if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod) {
                    tempTimers.add(timerPeriod);

                    this.register(timerPeriod, () -> {
                        CodeExecuterHandler.runLater(1, "initTimers(timerPeriod) > ToTrigger", () -> {
                            NotifierHandler.instance().notifyNotifier(timerPeriod.notificationToTrigger);
                            ChatNotifierHandler.instance().notifyChat(timerPeriod.chatNotificationToTrigger);
                        });
                    }, () -> {
                        CodeExecuterHandler.runLater(1, "initTimers(timerPeriod > ToTriggerEnd", () -> {
                            NotifierHandler.instance().notifyNotifier(timerPeriod.notificationToTriggerEnd);
                            ChatNotifierHandler.instance().notifyChat(timerPeriod.chatNotificationToTriggerEnd);
                        });
                    });
                } else {
                    tempTimers.add(timer);

                    this.register(timer, () -> {
                        CodeExecuterHandler.runLater(1, "initTimers(timer) > ToTriggerEnd", () -> {
                            NotifierHandler.instance().notifyNotifier(timer.notificationToTrigger);
                            ChatNotifierHandler.instance().notifyChat(timer.chatNotificationToTrigger);
                        });
                    });
                }
            }
        });

        timers = new ArrayList<>(tempTimers);
    }

    public void register(CustomTimerDataHandler.CustomTimer timer, Runnable callback) {
        callbacks.put(timer, callback);
    }

    public void register(CustomTimerDataHandler.CustomTimerPeriod timer, Runnable onCallback, Runnable offCallback) {
        endOfOnCallbacks.put(timer, offCallback);
        endOfOffCallbacks.put(timer, onCallback);
    }

    private void triggerTimer(CustomTimerDataHandler.CustomTimer timer, long time, Runnable callback) {
        if(callback != null) {
            long last = lastTrigger.getOrDefault(timer, -1L);

            if(last != time) {
                lastTrigger.put(timer, time);
                callback.run();
            }
        };
    }

    private boolean crossed(long prev, long curr, long target) {
        if (curr == prev) return false;
        if (prev < curr) {
            return prev < target && curr >= target;
        } else {
            return prev < target || curr >= target;
        }
    }
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
