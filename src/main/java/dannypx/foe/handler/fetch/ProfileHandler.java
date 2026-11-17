package dannypx.foe.handler.fetch;

import com.mojang.authlib.GameProfile;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.type.tuple.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.player.LocalPlayerResolver;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.players.ProfileResolver;

public class ProfileHandler extends Handler {
    private static ProfileHandler INSTANCE = new ProfileHandler();

    public static ProfileHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfileHandler();
        }
        return INSTANCE;
    }

    public ProfileHandler() {
        profileResolver = new LocalPlayerResolver(minecraft, minecraft.services().profileResolver());
    }

    //region Fields
    private final Map<UUID, GameProfile> profileCache = new HashMap<>();
    private final Map<UUID, Boolean> fetchingUUIDs = new HashMap<>();
    private final ProfileResolver profileResolver;
    //endregion

    //region Methods
    public String getUsernameFromId(UUID uuid) {
        GameProfile cached = profileCache.get(uuid);
        if (cached != null) return cached.name();

        if (!fetchingUUIDs.containsKey(uuid)) {
            LoggerHandler._debug("Fetching: " + uuid.toString());

            fetchingUUIDs.put(uuid, true);

            profileResolver.fetchById(uuid).ifPresentOrElse(profile -> {
                profileCache.put(uuid, profile);
                fetchingUUIDs.remove(uuid);
            }, () -> {
                CodeExecuterHandler.runLater(4, () -> {
                    fetchingUUIDs.remove(uuid);
                });
            });
        }
        return null;
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
