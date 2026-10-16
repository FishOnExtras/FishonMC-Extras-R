package dannypx.foe.nearby_dispatcher.types;

import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.handler.renderer.PlayerHatRenderHandler;
import dannypx.foe.nearby_dispatcher.ByteQuantizer;
import dannypx.foe.nearby_dispatcher.NearbyDispatcher;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class HatType {
    private static final int PACKET_TYPE = 1;

    private static final ByteQuantizer HAT_VALUES = new ByteQuantizer(0.50f, 1.50f, 100);

    private static final double NEARBY_RANGE = 24.0;
    private static final int NEWCOMER_DELAY_TICKS = 10;

    private static final Set<UUID> announcedTo = new HashSet<>();
    private static final Map<UUID, Integer> ticksInRange = new HashMap<>();

    public static void init() {
        NearbyDispatcher.register(PACKET_TYPE, HatType::onReceive);
        ClientTickEvents.END_CLIENT_TICK.register(HatType::tick);
    }

    public static void reload() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer self = minecraft.player;
        if (self == null || minecraft.level == null) return;

        announcedTo.clear();
        ticksInRange.clear();
        queueCurrent(self);

        for (Player other : minecraft.level.players()) {
            if (other != self && other.distanceToSqr(self) < NEARBY_RANGE * NEARBY_RANGE) {
                announcedTo.add(other.getUUID());
            }
        }
    }

    private static void tick(Minecraft minecraft) {
        LocalPlayer self = minecraft.player;
        if (self == null || minecraft.level == null || !ConnectionHandler.instance().isOnServer()) {
            announcedTo.clear();
            ticksInRange.clear();
            return;
        }

        Set<UUID> loaded = new HashSet<>();
        Set<UUID> readyNewcomers = new HashSet<>();
        for (Player other : minecraft.level.players()) {
            if (other == self) continue;
            UUID otherUuid = other.getUUID();
            loaded.add(otherUuid);
            if (announcedTo.contains(otherUuid)) continue;

            if (other.distanceToSqr(self) < NEARBY_RANGE * NEARBY_RANGE) {
                if (ticksInRange.merge(otherUuid, 1, Integer::sum) >= NEWCOMER_DELAY_TICKS) {
                    readyNewcomers.add(otherUuid);
                }
            } else {
                ticksInRange.remove(otherUuid);
            }
        }

        ticksInRange.keySet().retainAll(loaded);
        if (readyNewcomers.isEmpty()) return;

        announcedTo.addAll(readyNewcomers);
        readyNewcomers.forEach(ticksInRange::remove);

        float height = PlayerHatRenderHandler.instance().getHeight(self.getUUID());
        float scale = PlayerHatRenderHandler.instance().getScale(self.getUUID());
        if (height == PlayerHatRenderHandler.DEFAULT_HEIGHT && scale == PlayerHatRenderHandler.DEFAULT_SCALE) return;

        queueCurrent(self);
    }

    private static void queueCurrent(Player self) {
        byte[] payload = {
                HAT_VALUES.encode(PlayerHatRenderHandler.instance().getHeight(self.getUUID())),
                HAT_VALUES.encode(PlayerHatRenderHandler.instance().getScale(self.getUUID()))
        };
        NearbyDispatcher.queue(PACKET_TYPE, payload);
    }

    private static void onReceive(UUID sender, byte[] payload) {
        if (payload.length != 2) return;
        PlayerHatRenderHandler.instance().putHeights(sender, HAT_VALUES.decode(payload[0]));
        PlayerHatRenderHandler.instance().putScale(sender, HAT_VALUES.decode(payload[1]));
    }

    public static void onPlayerLeave(UUID playerUuid) {
        Minecraft.getInstance().execute(() -> {
            announcedTo.remove(playerUuid);
            ticksInRange.remove(playerUuid);
        });
    }
}
