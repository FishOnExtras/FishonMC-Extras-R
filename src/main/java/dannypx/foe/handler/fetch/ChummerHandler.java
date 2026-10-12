package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChummerHandler extends Handler {
    private static ChummerHandler INSTANCE = new ChummerHandler();

    public static ChummerHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChummerHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final double RADIUS_SQ = 20 * 20;
    private final Map<Integer, ChummerInfo> CHUMMERS = new HashMap<>();

    public Collection<ChummerInfo> getChummers() {
        return CHUMMERS.values();
    }
    //endregion

    //region Methods
    public void onEntityUnload(Entity entity, ClientLevel clientLevel) {
        CHUMMERS.remove(entity.getId());
    }

    public void onLeave() {
        CHUMMERS.clear();
    }

    public void onTextUpdate(Display.TextDisplay entity, Component text) {
        ChummerInfo info = ChummerInfo.tryParse(entity.position(), text);
        if (info == null) {
            CHUMMERS.remove(entity.getId());
        } else {
            CHUMMERS.put(entity.getId(), info);
        }
    }

    public ChummerInfo getChummer() {
        return minecraft.player != null ? closest(minecraft.player.position()) : null;
    }

    public ChummerInfo closest(Vec3 from) {
        ChummerInfo best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (ChummerInfo info : getChummers()) {
            double dx = info.getPos().x - from.x;
            double dz = info.getPos().z - from.z;
            double distSq = dx * dx + dz * dz;

            info.setInRange(distSq <= RADIUS_SQ);

            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = info;
            }
        }
        return best;
    }
    //endregion

    //region Model
    public static final class ChummerInfo {
        private final Vec3 pos;
        private final String rarity;
        private final int minutes;
        private final int seconds;
        private final String player;
        private boolean inRange;

        private ChummerInfo(Vec3 pos, String rarity, int minutes, int seconds, String player) {
            this.pos = pos;
            this.rarity = rarity;
            this.minutes = minutes;
            this.seconds = seconds;
            this.player = player;
        }

        public static ChummerInfo tryParse(Vec3 pos, Component raw) {
            String rarity = null;
            int minutes = 0;
            int seconds = 0;
            String player = null;

            if(!raw.getString().contains("Chummer™")) return null;

            String[] lines = raw.getString().split("\n");
            if (lines.length > 1) rarity = lines[1].trim();

            for (String line : lines) {
                line = line.trim();

                if (line.startsWith("Activated by ")) {
                    player = line.substring("Activated by ".length()).trim();
                } else if (line.endsWith(" Remaining")) {
                    String[] parts = line.substring(0, line.length() - " Remaining".length()).split(" ");
                    try {
                        for (String part : parts) {
                            int value = Integer.parseInt(part.substring(0, part.length() - 1));
                            switch (part.charAt(part.length() - 1)) {
                                case 'm' -> minutes = value;
                                case 's' -> seconds = value;
                            }
                        }
                    } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {}
                }
            }

            if (rarity == null  || player == null) return null;
            return new ChummerInfo(pos, rarity, minutes, seconds, player);
        }

        public String getPlayer() {
            return player;
        }

        public String getRarity() {
            return rarity;
        }

        public int getMinutes() {
            return minutes;
        }

        public int getSeconds() {
            return seconds;
        }

        public Vec3 getPos() {
            return pos;
        }

        public boolean isInRange() {
            return inRange;
        }

        public void setInRange(boolean inRange) {
            this.inRange = inRange;
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
