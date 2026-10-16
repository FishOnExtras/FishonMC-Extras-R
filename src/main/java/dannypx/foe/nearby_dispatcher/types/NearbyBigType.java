package dannypx.foe.nearby_dispatcher.types;

import dannypx.foe.nearby_dispatcher.NearbyDispatcher;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NearbyBigType {
    public static final int FRAGMENT_TYPE = 255;
    public static final int MAX_MESSAGE_BYTES = 1024;

    private static final int HEADER = 4;
    private static final int CHUNK = NearbyDispatcher.MAX_PAYLOAD - HEADER;
    private static final int MAX_FRAGMENTS = (MAX_MESSAGE_BYTES + CHUNK - 1) / CHUNK;
    private static final long ASSEMBLY_TIMEOUT_MS = 120_000;

    public interface Handler {
        void handle(UUID sender, byte[] data);
    }

    private record Key(UUID sender, int msgType) {}

    private static final class Assembly {
        final int messageId;
        final byte[][] parts;
        int received;
        long lastMs;

        Assembly(int messageId, int total) {
            this.messageId = messageId;
            this.parts = new byte[total][];
        }
    }

    private static final Map<Integer, Handler> HANDLERS = new HashMap<>();
    private static final Map<Key, Assembly> ASSEMBLIES = new HashMap<>();
    private static int nextMessageId;

    public static void init() {
        NearbyDispatcher.register(FRAGMENT_TYPE, NearbyBigType::onFragment);
    }

    public static void register(int msgType, Handler handler) {
        if (msgType < 0 || msgType > 255) throw new IllegalArgumentException("msgType must be 0-255");
        HANDLERS.put(msgType, handler);
    }

    public static void send(int msgType, byte[] data) {
        if (msgType < 0 || msgType > 255) throw new IllegalArgumentException("msgType must be 0-255");
        if (data.length == 0 || data.length > MAX_MESSAGE_BYTES) {
            throw new IllegalArgumentException("data must be 1-" + MAX_MESSAGE_BYTES + " bytes");
        }

        int messageId = nextMessageId++ & 0xFF;
        int total = (data.length + CHUNK - 1) / CHUNK;
        for (int i = 0; i < total; i++) {
            int from = i * CHUNK;
            int length = Math.min(CHUNK, data.length - from);

            byte[] fragment = new byte[HEADER + length];
            fragment[0] = (byte) msgType;
            fragment[1] = (byte) messageId;
            fragment[2] = (byte) i;
            fragment[3] = (byte) total;
            System.arraycopy(data, from, fragment, HEADER, length);
            NearbyDispatcher.queueAppend(FRAGMENT_TYPE, fragment);
        }
    }

    private static void onFragment(UUID sender, byte[] payload) {
        if (payload.length <= HEADER) return;

        int msgType = payload[0] & 0xFF;
        int messageId = payload[1] & 0xFF;
        int index = payload[2] & 0xFF;
        int total = payload[3] & 0xFF;
        if (total == 0 || total > MAX_FRAGMENTS || index >= total) return;
        if (!HANDLERS.containsKey(msgType)) return;

        long now = System.currentTimeMillis();
        ASSEMBLIES.values().removeIf(a -> now - a.lastMs > ASSEMBLY_TIMEOUT_MS);

        Key key = new Key(sender, msgType);
        Assembly assembly = ASSEMBLIES.get(key);
        if (assembly == null || assembly.messageId != messageId || assembly.parts.length != total) {
            assembly = new Assembly(messageId, total);
            ASSEMBLIES.put(key, assembly);
        }
        assembly.lastMs = now;

        if (assembly.parts[index] == null) {
            assembly.parts[index] = Arrays.copyOfRange(payload, HEADER, payload.length);
            assembly.received++;
        }
        if (assembly.received < total) return;

        ASSEMBLIES.remove(key);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : assembly.parts) out.writeBytes(part);
        try {
            HANDLERS.get(msgType).handle(sender, out.toByteArray());
        } catch (Exception ignored) {
        }
    }
}
