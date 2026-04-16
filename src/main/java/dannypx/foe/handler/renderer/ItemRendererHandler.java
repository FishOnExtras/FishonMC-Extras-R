package dannypx.foe.handler.renderer;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.SearchHandler;
import dannypx.foe.handler.store.ConstantDataHandler;
import dannypx.foe.helper.DrawHelper;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.item.*;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.*;

public class ItemRendererHandler extends Handler {
    private static ItemRendererHandler INSTANCE = new ItemRendererHandler();

    public static ItemRendererHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemRendererHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final Identifier petItemMarker = Identifier.of(FishOnMCExtras.MOD_ID, "icons/pet_item");
    //endregion

    //region Methods
    public void drawRarityMarker(DrawContext drawContext, TextRenderer textRenderer, ItemStack stack, int x, int y) {
        if(!Configs.rendererConfig.showRarityMarker.get()) {
            return;
        }

        Pair<Boolean, NbtObject> validateItem = ValidateItem.isServerItem(stack);

        if(!this.checkIfBlacklisted(validateItem.value2())
                && !validateItem.value2().getRarity().isBlank()
        ) {
            Text rarityText = Text.literal(ConstantDataHandler.instance().getConstantData().fishData
                    .getOrDefault(FishNbtObject.RARITY, new HashMap<>())
                    .getOrDefault(validateItem.value2().getRarity().toLowerCase(Locale.US), Text.empty()).getString().trim());
            if(rarityText.getString().isBlank()) rarityText = Text.literal(validateItem.value2().getRarityText().getString());

            if(!Objects.equals(rarityText, Text.empty())) {
                int markerX = x;
                int markerY = y - 1;

                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0.0F, 0.0F, 200.0F);

                //TOP
                int bgX = markerX;
                int bgY = markerY - 1;
                drawContext.enableScissor(bgX, bgY + 2, bgX + 2, bgY + 4);
                drawContext.drawText(textRenderer, rarityText, bgX, bgY, 0xAAAAAA, false);
                drawContext.disableScissor();

                //BOTTOM
                bgY = markerY + 1;
                drawContext.enableScissor(bgX, bgY + 2, bgX + 2, bgY + 4);
                drawContext.drawText(textRenderer, rarityText, bgX, bgY, 0xAAAAAA, false);
                drawContext.disableScissor();

                //LEFT
                bgX = markerX - 1;
                bgY = markerY;
                drawContext.enableScissor(bgX, bgY + 2, bgX + 2, bgY + 4);
                drawContext.drawText(textRenderer, rarityText, bgX, bgY, 0xAAAAAA, false);
                drawContext.disableScissor();

                //RIGHT
                bgX = markerX + 1;
                drawContext.enableScissor(bgX, bgY + 2, bgX + 2, bgY + 4);
                drawContext.drawText(textRenderer, rarityText, bgX, bgY, 0xAAAAAA, false);
                drawContext.disableScissor();

                drawContext.enableScissor(markerX, bgY + 2, markerX + 2, bgY + 4);
                drawContext.drawText(textRenderer, rarityText, markerX, markerY, 0xFFFFFF, false);
                drawContext.disableScissor();

                drawContext.getMatrices().pop();
            }
        }
    }

    private boolean checkIfBlacklisted(NbtObject nbtObject) {
        if(!Configs.rendererConfig.blackListItems.get().isBlank()) {
            List<String> blacklistedItems = Arrays.stream(Configs.rendererConfig.blackListItems.get().split(",")).map(String::trim).toList();
            return blacklistedItems.contains(nbtObject.getType());
        }
        return false;
    }

    public void drawStackCount(DrawContext drawContext, TextRenderer textRenderer, ItemStack stack, int x, int y) {
        Pair<Boolean, NbtObject> validatedItem = ValidateItem.isServerItem(stack);

        int count = validatedItem.value2().getCount();
        Text countText = TextHelper.literal(TextHelper.smallText(TextHelper.shortenNumber(count, 0)));
        int countWidth = textRenderer.getWidth(countText);

        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(0.0F, 0.0F, 200.0F);
        if(count > 1) DrawHelper.drawText(drawContext, textRenderer, countText,
                x + 19 - 2 - countWidth, y + 6 + 4,
                true,
                true,
                false,
                true
        );
        drawContext.getMatrices().pop();
    }

    public void drawSearchItem(DrawContext drawContext, ItemStack stack, int x, int y) {
        if(SearchHandler.instance().isOnScreen()
                && SearchHandler.instance().filterItem(stack)) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0.0F, 0.0F, 180.0F);

            drawContext.drawBorder(x, y, 16, 16, Colors.RED);

            drawContext.getMatrices().pop();
        }
    }


    public void drawPetItemEquipped(DrawContext drawContext, ItemStack stack, int x, int y) {
        if(!Configs.rendererConfig.showPetEquippedMarker.get()) {
            return;
        }

        Pair<Boolean, PetNbtObject> validatedPet = ValidateItem.isPet(stack);

        if(validatedPet.value1() && (validatedPet.value2().contains(PetNbtObject.ITEM) || validatedPet.value2().contains(PetNbtObject.SKIN))) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0.0F, 0.0F, 200.0F);
            drawContext.drawGuiTexture(RenderLayer::getGuiTextured, petItemMarker, x, y, 16, 16, 0xFFFFFFFF);
            drawContext.getMatrices().pop();
        }
    }

    public void drawFishSize(DrawContext drawContext, TextRenderer textRenderer, ItemStack stack, int x, int y) {
        Pair<Boolean, FishNbtObject> validatedFish = ValidateItem.isFish(stack);

        if(validatedFish.value1()
                && !validatedFish.value2().getFishSize().isBlank()
        ) {
            Text sizeText = ConstantDataHandler.instance().getConstantData().fishData
                    .getOrDefault(FishNbtObject.FISH_SIZE, new HashMap<>())
                    .getOrDefault(validatedFish.value2().getFishSize().toLowerCase(Locale.US), Text.empty());
            if(sizeText.getString().isBlank()) sizeText = validatedFish.value2().getFishSizeText();

            if(!sizeText.getString().isEmpty()) {
                sizeText = TextHelper.substring(sizeText, 0, 1);

                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0.0F, 0.0F, 200.0F);

                drawContext.drawText(textRenderer, sizeText, x + 17 - textRenderer.getWidth(sizeText), y + 18 - textRenderer.fontHeight, 0xFFFFFF, true);

                drawContext.getMatrices().pop();
            }
        }
    }

    public void drawPetRating(DrawContext drawContext, TextRenderer textRenderer, ItemStack stack, int x, int y) {
        Pair<Boolean, PetNbtObject> validatedPet = ValidateItem.isPet(stack);

        if(validatedPet.value1()
                && !validatedPet.value2().getRatingText().getString().isBlank()
        ) {
            Text ratingText = validatedPet.value2().getRatingText();

            if(!ratingText.getString().isEmpty()) {
                ratingText = TextHelper.substring(ratingText, 0, 1);

                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0.0F, 0.0F, 200.0F);

                drawContext.drawText(textRenderer, ratingText, x + 17 - textRenderer.getWidth(ratingText), y + 18 - textRenderer.fontHeight, 0xFFFFFF, true);

                drawContext.getMatrices().pop();
            }
        }
    }

    public void drawArmorQuality(DrawContext drawContext, TextRenderer textRenderer, ItemStack stack, int x, int y) {
        Pair<Boolean, ArmorNbtObject> validatedArmor = ValidateItem.isArmor(stack);

        if(validatedArmor.value1()
                && !validatedArmor.value2().getQualityText().getString().isBlank()
        ) {
            Text qualityArmor = validatedArmor.value2().getQualityText();
            Text qualityRaw = TextHelper.substring(qualityArmor, 0, qualityArmor.getString().length() - 1);
            Text qualityText = Text.literal(TextHelper.smallText(qualityRaw.getString())).setStyle(qualityArmor.getStyle());

            if(!qualityText.getString().isEmpty()) {
                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0.0F, 0.0F, 200.0F);

                drawContext.drawText(textRenderer, qualityText, x + 17 - textRenderer.getWidth(qualityText), y + 17 - textRenderer.fontHeight, 0xFFFFFF, true);

                drawContext.getMatrices().pop();
            }
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
        );
    }
    //endregion
}
