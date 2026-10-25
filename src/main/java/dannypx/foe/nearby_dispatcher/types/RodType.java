package dannypx.foe.nearby_dispatcher.types;

import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.handler.renderer.RodRendererHandler;
import dannypx.foe.nearby_dispatcher.ByteQuantizer;
import dannypx.foe.nearby_dispatcher.NearbyDispatcher;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class RodType {
    private static final int PACKET_TYPE = 2;

    private static final ByteQuantizer ROD_SCALE = new ByteQuantizer(0.50f, 1.50f, 100);
    private static final ByteQuantizer ROD_OFFSET = new ByteQuantizer(-0.50f, 0.50f, 100);

    private static final Set<UUID> announcedTo = new HashSet<>();
    private static final Map<UUID, Integer> ticksInRange = new HashMap<>();

    public static void init() {
        NearbyDispatcher.register(PACKET_TYPE, RodType::onReceive);
        ClientTickEvents.END_CLIENT_TICK.register(RodType::tick);
    }

    public static void reload() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer self = minecraft.player;
        if (self == null || minecraft.level == null) return;

        announcedTo.clear();
        ticksInRange.clear();
        queueCurrent(self);

        for (Player other : minecraft.level.players()) {
            if (other != self && other.distanceToSqr(self) < NearbyDispatcher.NEARBY_RANGE * NearbyDispatcher.NEARBY_RANGE) {
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

            if (other.distanceToSqr(self) < NearbyDispatcher.NEARBY_RANGE * NearbyDispatcher.NEARBY_RANGE) {
                if (ticksInRange.merge(otherUuid, 1, Integer::sum) >= NearbyDispatcher.NEWCOMER_DELAY_TICKS) {
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

        if (RodRendererHandler.instance().get(self.getUUID()).isDefault()) return;

        queueCurrent(self);
    }

    private static void queueCurrent(Player self) {
        RodRendererHandler.RodSettings settings = RodRendererHandler.instance().get(self.getUUID());
        byte[] payload = {
                ROD_SCALE.encode(settings.scale()),
                ROD_OFFSET.encode(settings.offsetRight()),
                ROD_OFFSET.encode(settings.offsetUp()),
                ROD_OFFSET.encode(settings.offsetForward()),
                ROD_OFFSET.encode(settings.lineOffsetRight()),
                ROD_OFFSET.encode(settings.lineOffsetUp()),
                ROD_OFFSET.encode(settings.lineOffsetForward())
        };
        NearbyDispatcher.queue(PACKET_TYPE, payload);
    }

    private static void onReceive(UUID sender, byte[] payload) {
        if (payload.length != 7) return;
        RodRendererHandler.instance().putSettings(sender, new RodRendererHandler.RodSettings(
                ROD_SCALE.decode(payload[0]),
                ROD_OFFSET.decode(payload[1]),
                ROD_OFFSET.decode(payload[2]),
                ROD_OFFSET.decode(payload[3]),
                ROD_OFFSET.decode(payload[4]),
                ROD_OFFSET.decode(payload[5]),
                ROD_OFFSET.decode(payload[6])
        ));
    }

    public static void onPlayerLeave(UUID playerUuid) {
        Minecraft.getInstance().execute(() -> {
            announcedTo.remove(playerUuid);
            ticksInRange.remove(playerUuid);
        });
    }
}
