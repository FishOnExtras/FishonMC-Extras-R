package dannypx.foe.nearby_dispatcher.types;

import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.handler.logic.RippleHandler;
import dannypx.foe.nearby_dispatcher.NearbyDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class RippleType {
    private static final int PACKET_TYPE = 3;

    public static void init() {
        NearbyDispatcher.register(PACKET_TYPE, RippleType::onReceive);
    }

    public static void sendBite() {
        if (!ConnectionHandler.instance().isOnServer()) return;
        NearbyDispatcher.queue(PACKET_TYPE, new byte[]{1});
    }

    private static void onReceive(UUID sender, byte[] payload) {
        if (payload.length != 1) return;

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.level == null) return;
            Player other = minecraft.level.getPlayerByUUID(sender);
            if (other == null || other.fishing == null) return;
            RippleHandler.instance().spawnAtBobber(other.fishing);
        });
    }
}
