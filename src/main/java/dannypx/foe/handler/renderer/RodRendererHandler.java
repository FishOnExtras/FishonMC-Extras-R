package dannypx.foe.handler.renderer;

import dannypx.foe.config.Configs;
import dannypx.foe.handler.Handler;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RodRendererHandler extends Handler {
    private static RodRendererHandler INSTANCE = new RodRendererHandler();

    public static RodRendererHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new RodRendererHandler();
        }
        return INSTANCE;
    }

    //region Fields
    public static final Map<UUID, RodSettings> OTHER_PLAYER_SETTINGS = new ConcurrentHashMap<>();
    //endregion

    //region Methods
    public RodSettings get(UUID uuid) {
        if (isSelf(uuid)) return getSelf();
        return OTHER_PLAYER_SETTINGS.getOrDefault(uuid, RodSettings.DEFAULT);
    }

    private static boolean isSelf(UUID uuid) {
        LocalPlayer player = minecraft.player;
        return player != null && uuid.equals(player.getUUID());
    }

    private static RodSettings getSelf() {
        return new RodSettings(
                Configs.rendererConfig.rodScale.get(),
                Configs.rendererConfig.rodXPosition.get(),
                Configs.rendererConfig.rodYPosition.get(),
                Configs.rendererConfig.rodZPosition.get(),
                Configs.rendererConfig.lineXPosition.get(),
                Configs.rendererConfig.lineYPosition.get(),
                Configs.rendererConfig.lineZPosition.get()
        );
    }

    public void putSettings(UUID sender, RodSettings rodSettings) {
        OTHER_PLAYER_SETTINGS.put(sender, rodSettings);
    }

    public record RodSettings(float scale, float offsetRight, float offsetUp, float offsetForward, float lineOffsetRight, float lineOffsetUp, float lineOffsetForward) {
        public static final RodSettings DEFAULT = new RodSettings(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        public boolean isDefault() {
            return equals(DEFAULT);
        }
    }
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
