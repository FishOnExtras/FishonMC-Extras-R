package dannypx.foe.handler.renderer;

import dannypx.foe.config.Configs;
import dannypx.foe.handler.Handler;
import dannypx.foe.item.FishTagObject;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;

public class HoldingFishRenderHandler extends Handler {
    private static HoldingFishRenderHandler INSTANCE = new HoldingFishRenderHandler();

    public static HoldingFishRenderHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new HoldingFishRenderHandler();
        }
        return INSTANCE;
    }

    //region Fields
    public static final float FISH_ROLL = -45.0F;

    public static final float FISH_ARM_X_ROT = -1.25F;
    public static final float FISH_ARM_Y_ROT = 0.25F;
    public static final float FISH_ARM_CENTER_OFFSET = 6.0F;
    public static final float FISH_ARM_LENGTH = 10.0F;
    public static final float FISH_ARM_ROLL = 60.0F;
    public static final float FISH_CENTER_ADJUST = -(4F / 16F);
    public static final float FISH_FORWARD_OFFSET = 1.0F / 16.0F;
    public static final float FISH_DOWN_OFFSET = 3.0F / 16.0F;
    public static final float FISH_SPRITE_PIXELS = 16.0F;
    public static final float FISH_SPRITE_INCHES = 8.0F;

    public static final float FISH_FIRST_PERSON_X = 0.0F;
    public static final float FISH_FIRST_PERSON_Y = -0.32F;
    public static final float FISH_FIRST_PERSON_Z = -0.72F;
    public static final float FISH_FIRST_PERSON_BASE_SCALE = 0.15F;
    public static final float FIRST_PERSON_FISH_EQUIP_DROP = 0.6F;
    public static final float FIRST_PERSON_FISH_SPRITE_PIXELS = 48.0F;
    public static final float FIRST_PERSON_FISH_SPRITE_INCHES = 8.0F;
    public static final float FISH_ARM_FIRST_PERSON_INWARD = 0.1F;
    public static final float FISH_ARM_FIRST_PERSON_UP = -0.05F;
    public static final float FISH_ARM_FIRST_PERSON_FORWARD = -0.15F;

    public static final float FIRST_PERSON_FISH_ARM_X_ROT = -1.18F;
    public static final float FIRST_PERSON_FISH_SWAY_STRENGTH = 0.4F;
    //endregion

    //region Methods
    public static float getTotalScale(Pair<Boolean, FishTagObject> validatedFish) {
        float fishScale = 1.0f;
        float fishLength = validatedFish.value2().getLength();
        if (fishLength > 0.0f) {
            float fishPixels = FIRST_PERSON_FISH_SPRITE_PIXELS * (float) Math.pow(fishLength / FIRST_PERSON_FISH_SPRITE_INCHES, Configs.rendererConfig.firstPersonRateScaleInUnits.get());
            float clampedPixels = Math.clamp(fishPixels + Configs.rendererConfig.firstPersonMinimumScaleInUnits.get() - FIRST_PERSON_FISH_SPRITE_PIXELS, Configs.rendererConfig.firstPersonMinimumScaleInUnits.get(), Configs.rendererConfig.firstPersonMaximumScaleInUnits.get());
            fishScale = clampedPixels / FIRST_PERSON_FISH_SPRITE_PIXELS;
        }

        return FISH_FIRST_PERSON_BASE_SCALE * fishScale;
    }

    public static float getFishHandOffsetFromCenter() {
        double inward = FISH_ARM_LENGTH * Math.sin(Math.abs(FISH_ARM_X_ROT)) * Math.sin(Math.abs(FISH_ARM_Y_ROT));
        return (float) ((FISH_ARM_CENTER_OFFSET - inward) / 16.0);
    }

    public static void bobAnimation(ModelPart part, float age, float multiplier) {
        part.zRot += multiplier * ((float) Math.cos(age * 0.09F) * 0.05F + 0.05F);
        part.xRot += multiplier * (float) Math.sin(age * 0.067F) * 0.05F;
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
