package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.type.tuple.Pair;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CodeExecuterHandler extends Handler {
    private static CodeExecuterHandler INSTANCE = new CodeExecuterHandler();

    public static CodeExecuterHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CodeExecuterHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private static final List<ScheduledTask> TASKS = new ArrayList<>();
    private static final List<ScheduledTask> PENDING = new ArrayList<>();

    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TASKS.addAll(PENDING);
            PENDING.clear();

            Iterator<ScheduledTask> iterator = TASKS.iterator();
            while (iterator.hasNext()) {
                ScheduledTask task = iterator.next();
                task.ticks--;
                if (task.ticks <= 0) {
                    try {
                        task.runnable.run();
                        iterator.remove();
                    } catch (Exception e) {
                        LoggerHandler.error(task.taskName);
                        throw e;
                    }
                }
            }
        });
    }

    public static void runLater(int ticks, String taskName, Runnable runnable) {
        PENDING.add(new ScheduledTask(ticks, taskName, runnable));
    }

    private static class ScheduledTask {
        int ticks;
        String taskName;
        Runnable runnable;

        ScheduledTask(int ticks, String taskName, Runnable runnable) {
            this.ticks = ticks;
            this.taskName = taskName;
            this.runnable = runnable;
        }
    }
    //endregion

    //region Methods
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
        );
    }
    //endregion
}
