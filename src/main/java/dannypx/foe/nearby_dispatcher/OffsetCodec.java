package dannypx.foe.nearby_dispatcher;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class OffsetCodec {
    private static final int OFFSET_RANGE = 6;
    private static final int GRID_SIDE = OFFSET_RANGE * 2 + 1;
    private static final int SYMBOL = 256;

    private static final int[] OFFSET_X = new int[SYMBOL];
    private static final int[] OFFSET_Y = new int[SYMBOL];
    private static final int[] OFFSET_Z = new int[SYMBOL];
    private static final int[] SYMBOL_BY_GRID_INDEX = new int[GRID_SIDE * GRID_SIDE * GRID_SIDE];

    public static final int MIN_OFFSET_Y;
    public static final int MAX_OFFSET_Y;

    static {
        record Candidate(double worstCaseDistance, int offsetX, int offsetY, int offsetZ) {}

        List<Candidate> candidates = new ArrayList<>();
        for (int offsetX = -OFFSET_RANGE; offsetX <= OFFSET_RANGE; offsetX++) {
            for (int offsetY = -OFFSET_RANGE; offsetY <= OFFSET_RANGE; offsetY++) {
                for (int offsetZ = -OFFSET_RANGE; offsetZ <= OFFSET_RANGE; offsetZ++) {
                    double verticalDistance = offsetY <= 0 ? 1.62 - offsetY : offsetY == 1 ? 0.62 : offsetY - 1.27;
                    double worstCaseDistance = Math.sqrt(
                            offsetX * offsetX + offsetZ * offsetZ + verticalDistance * verticalDistance);
                    candidates.add(new Candidate(worstCaseDistance, offsetX, offsetY, offsetZ));
                }
            }
        }

        candidates.sort(Comparator.comparingDouble(Candidate::worstCaseDistance)
                .thenComparingInt(Candidate::offsetX)
                .thenComparingInt(Candidate::offsetY)
                .thenComparingInt(Candidate::offsetZ));

        Arrays.fill(SYMBOL_BY_GRID_INDEX, -1);
        int minimumOffsetY = Integer.MAX_VALUE;
        int maximumOffsetY = Integer.MIN_VALUE;
        for (int symbol = 0; symbol < SYMBOL; symbol++) {
            Candidate candidate = candidates.get(symbol);
            OFFSET_X[symbol] = candidate.offsetX();
            OFFSET_Y[symbol] = candidate.offsetY();
            OFFSET_Z[symbol] = candidate.offsetZ();
            SYMBOL_BY_GRID_INDEX[gridIndex(candidate.offsetX(), candidate.offsetY(), candidate.offsetZ())] = symbol;
            minimumOffsetY = Math.min(minimumOffsetY, candidate.offsetY());
            maximumOffsetY = Math.max(maximumOffsetY, candidate.offsetY());
        }
        MIN_OFFSET_Y = minimumOffsetY;
        MAX_OFFSET_Y = maximumOffsetY;
    }

    private OffsetCodec() {}

    private static int gridIndex(int offsetX, int offsetY, int offsetZ) {
        return ((offsetX + OFFSET_RANGE) * GRID_SIDE + (offsetY + OFFSET_RANGE)) * GRID_SIDE
                + (offsetZ + OFFSET_RANGE);
    }

    public static BlockPos encode(BlockPos origin, int symbol) {
        return origin.offset(
                OFFSET_X[symbol],
                OFFSET_Y[symbol],
                OFFSET_Z[symbol]);
    }

    public static int decode(BlockPos origin, BlockPos blockPosition) {
        int offsetX = blockPosition.getX() - origin.getX();
        int offsetY = blockPosition.getY() - origin.getY();
        int offsetZ = blockPosition.getZ() - origin.getZ();
        if (Math.abs(offsetX) > OFFSET_RANGE
                || Math.abs(offsetY) > OFFSET_RANGE
                || Math.abs(offsetZ) > OFFSET_RANGE) {
            return -1;
        }
        return SYMBOL_BY_GRID_INDEX[gridIndex(offsetX, offsetY, offsetZ)];
    }

    public static int tableChecksum() {
        int checksum = 17;
        for (int symbol = 0; symbol < SYMBOL; symbol++) {
            checksum = 31 * checksum + OFFSET_X[symbol];
            checksum = 31 * checksum + OFFSET_Y[symbol];
            checksum = 31 * checksum + OFFSET_Z[symbol];
        }
        return checksum;
    }
}
