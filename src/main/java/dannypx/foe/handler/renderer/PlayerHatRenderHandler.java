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

public class PlayerHatRenderHandler extends Handler {
    private static PlayerHatRenderHandler INSTANCE = new PlayerHatRenderHandler();

    public static PlayerHatRenderHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerHatRenderHandler();
        }
        return INSTANCE;
    }

    //region Fields
    public static final float DEFAULT_HEIGHT = 1.0f;
    public static final float DEFAULT_SCALE = 1.0f;

    public static final Map<UUID, Float> HEIGHTS = new ConcurrentHashMap<>();
    public static final Map<UUID, Float> SCALES = new ConcurrentHashMap<>();
    //endregion

    //region Methods
    private boolean isSelf(UUID uuid) {
        LocalPlayer player = minecraft.player;
        return player != null && uuid.equals(player.getUUID());
    }

    public float getHeight(UUID uuid) {
        if (isSelf(uuid)) return Configs.rendererConfig.hatHeight.get();
        return HEIGHTS.getOrDefault(uuid, DEFAULT_HEIGHT);
    }

    public float getScale(UUID uuid) {
        if (isSelf(uuid)) return Configs.rendererConfig.hatScale.get();
        return SCALES.getOrDefault(uuid, DEFAULT_SCALE);
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
