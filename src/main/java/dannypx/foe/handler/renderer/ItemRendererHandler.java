package dannypx.foe.handler.renderer;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.store.ConstantDataHandler;
import dannypx.foe.helper.GlyphHelper;
import dannypx.foe.helper.GuiGraphicsHelper;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.item.*;
import dannypx.foe.handler.logic.SearchHandler;
import dannypx.foe.type.StringStyle;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import java.util.*;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

public class ItemRendererHandler extends Handler {
    private static ItemRendererHandler INSTANCE = new ItemRendererHandler();

    public static ItemRendererHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemRendererHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final Identifier petItemMarker = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "icons/pet_item");
    private final Identifier rarityMarker = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "icons/rarity");
    //endregion

    //region Methods
    public void drawRarityMarker(GuiGraphicsExtractor guiGraphicsExtractor, Font font, ItemStack stack, int x, int y) {
        if(!Configs.rendererConfig.showRarityMarker.get()) {
            return;
        }

        Pair<Boolean, TagObject> validateItem = ValidateItem.isServerItem(stack, true);

        if(!this.checkIfBlacklisted(validateItem.value2())
                && !validateItem.value2().getRarity().isBlank()
        ) {
            Component rarityComponent = Component.literal(ConstantDataHandler.instance().getConstantData().fishData
                    .getOrDefault(FishTagObject.RARITY, new HashMap<>())
                    .getOrDefault(validateItem.value2().getRarity().toLowerCase(Locale.US), Component.empty()).getString().trim());
            if(rarityComponent.getString().isBlank()) rarityComponent = Component.literal(validateItem.value2().getRarityComponent().getString());

            if(!Objects.equals(rarityComponent, Component.empty())) {
                int color = GlyphHelper.getColor(rarityComponent.getString()).orElse(-1);
                if(color != -1) guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, rarityMarker, x, y, 16, 16, ARGB.color(0xFFFFFF, color));
            }
        }
    }

    private boolean checkIfBlacklisted(TagObject tagObject) {
        if(!Configs.rendererConfig.blackListItems.get().isBlank()) {
            List<String> blacklistedItems = Arrays.stream(Configs.rendererConfig.blackListItems.get().split(",")).map(String::trim).toList();
            return blacklistedItems.contains(tagObject.getType());
        }
        return false;
    }

    public void drawStackCount(GuiGraphicsExtractor guiGraphicsExtractor, Font font, ItemStack stack, int x, int y) {
        this.drawStackCount(guiGraphicsExtractor, font, stack, x, y, true);
    }

    public void drawStackCount(GuiGraphicsExtractor guiGraphicsExtractor, Font font, ItemStack stack, int x, int y, boolean isSmall) {
        Pair<Boolean, TagObject> validatedItem = ValidateItem.isServerItem(stack, true);

        int count = Configs.rendererConfig.showStackCountOnBait.get()
                ? validatedItem.value2().getCount()
                : stack.getCount();
        Component countComponent = isSmall
                ? TextHelper.literal(TextHelper.smallCaps(TextHelper.shortenNumber(count, 0)))
                : TextHelper.literal(TextHelper.shortenNumber(count, 0));
        int countWidth = font.width(countComponent);

        if(count > 1) {
            if(isSmall) {
                GuiGraphicsHelper.text(guiGraphicsExtractor, font, countComponent,
                        x + 19 - 2 - countWidth, y + 6 + 4,
                        StringStyle.SHADOW, StringStyle.MIDDLE, StringStyle.SMALL_CAPS);
            } else {
                GuiGraphicsHelper.text(guiGraphicsExtractor, font, countComponent,
                        x + 19 - 2 - countWidth, y + 6 + 3,
                        StringStyle.SHADOW);
            }
        }
    }

    public void drawSearchItem(GuiGraphicsExtractor guiGraphicsExtractor, ItemStack stack, int x, int y) {
        if(SearchHandler.instance().isOnScreen()
                && SearchHandler.instance().filterItem(stack)) {
            guiGraphicsExtractor.horizontalLine(x, x + 16, y, CommonColors.RED);
            guiGraphicsExtractor.horizontalLine(x, x + 16, y + 16, CommonColors.RED);
            guiGraphicsExtractor.verticalLine(x, y, y + 16, CommonColors.RED);
            guiGraphicsExtractor.verticalLine(x + 16, y, y + 16, CommonColors.RED);
        }
    }


    public void drawPetItemEquipped(GuiGraphicsExtractor guiGraphicsExtractor, ItemStack stack, int x, int y) {
        if(!Configs.rendererConfig.showPetEquippedMarker.get()) {
            return;
        }

        Pair<Boolean, PetTagObject> validatedPet = ValidateItem.isPet(stack);

        if(validatedPet.value1() && (
                validatedPet.value2().contains(PetTagObject.ITEM)
                || validatedPet.value2().contains(PetTagObject.SKIN)
                || validatedPet.value2().contains(PetTagObject.TRAIL)
        )) {
            guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, petItemMarker, x, y, 16, 16, CommonColors.WHITE);
        }
    }

    public void drawFishSize(GuiGraphicsExtractor guiGraphicsExtractor, Font font, ItemStack stack, int x, int y) {
        Pair<Boolean, FishTagObject> validatedFish = ValidateItem.isFish(stack);

        if(validatedFish.value1()
                && !validatedFish.value2().getFishSize().isBlank()
        ) {
            Component sizeComponent = ConstantDataHandler.instance().getConstantData().fishData
                    .getOrDefault(FishTagObject.FISH_SIZE, new HashMap<>())
                    .getOrDefault(validatedFish.value2().getFishSize().toLowerCase(Locale.US), Component.empty());
            if(sizeComponent.getString().isBlank()) sizeComponent = validatedFish.value2().getFishSizeComponent();

            if(!sizeComponent.getString().isEmpty()) {
                sizeComponent = TextHelper.substring(sizeComponent, 0, 1);

                guiGraphicsExtractor.text(font, sizeComponent, x + 17 - font.width(sizeComponent), y + 18 - font.lineHeight, CommonColors.WHITE, true);
            }
        }
    }

    public void drawPetRating(GuiGraphicsExtractor guiGraphicsExtractor, Font font, ItemStack stack, int x, int y) {
        Pair<Boolean, PetTagObject> validatedPet = ValidateItem.isPet(stack);

        if(validatedPet.value1()) {
            Component ratingComponent = validatedPet.value2().getRatingComponent();

            if(!ratingComponent.getString().isBlank()) {
                Component result = TextHelper.replace(
                        ratingComponent,
                        ratingComponent.getString().substring(1),
                        TextHelper.smallNumber(
                                TextHelper.shortenNumber(validatedPet.value2().getTotalPercent() * 100, 0)
                        )
                );

                ratingComponent = TextHelper.substring(
                        result,
                        1
                );

                guiGraphicsExtractor.text(font, ratingComponent, x + 17 - font.width(ratingComponent), y + 18 - font.lineHeight - 1, CommonColors.WHITE, true);
            } else {
                Component rating = TextHelper.literal(TextHelper.smallNumber(
                        TextHelper.shortenNumber(validatedPet.value2().getTotalPercent() * 100, 0)
                ));

                guiGraphicsExtractor.text(font, rating, x + 17 - font.width(rating), y + 18 - font.lineHeight - 1, CommonColors.WHITE, true);
            }
        }
    }

    public void drawArmorQuality(GuiGraphicsExtractor guiGraphicsExtractor, Font font, ItemStack stack, int x, int y) {
        Pair<Boolean, ArmorTagObject> validatedArmor = ValidateItem.isArmor(stack);

        if(validatedArmor.value1()
                && !validatedArmor.value2().getQualityComponent().getString().isBlank()
        ) {
            Component qualityArmor = validatedArmor.value2().getQualityComponent();
            Component qualityRaw = TextHelper.substring(qualityArmor, 0, qualityArmor.getString().length() - 1);
            Component qualityComponent = Component.literal(TextHelper.smallCaps(qualityRaw.getString())).setStyle(qualityArmor.getStyle());

            if(!qualityComponent.getString().isEmpty()) {
                guiGraphicsExtractor.text(font, qualityComponent, x + 17 - font.width(qualityComponent), y + 17 - font.lineHeight, CommonColors.WHITE, true);
            }
        }
    }

    public void drawBaitStackerMarker(GuiGraphicsExtractor guiGraphicsExtractor, Font font, ItemStack stack, int x, int y) {
        if(minecraft.player != null) {
            ItemStack cursorItem = minecraft.player.containerMenu.getCarried();

            if(!cursorItem.isEmpty()
                    && cursorItem != stack
            ) {
                Pair<Boolean, TagObject> validatedCursor = ValidateItem.isType(cursorItem);

                if(validatedCursor.value1()
                        && (
                                validatedCursor.value2().getType().equals("bait")
                                || validatedCursor.value2().getType().equals("lure")
                )) {
                    Pair<Boolean,TagObject> validatedItem = ValidateItem.isType(stack);

                    if(validatedItem.value1()
                            && (
                                    validatedItem.value2().getType().equals("bait")
                                    || (
                                            validatedItem.value2().getType().equals("lure")
                                            && validatedCursor.value2().getString("size").equals(validatedItem.value2().getString("size"))
                                            && validatedCursor.value2().getString("color")
                                                    .replaceAll("§.", "")
                                                    .replaceAll("<[^>]*>", "")
                                                    .equals(
                                                            validatedItem.value2().getString("color")
                                                                    .replaceAll("§.", "")
                                                                    .replaceAll("<[^>]*>", "")
                                                    )
                                    )
                            )
                            && validatedCursor.value2().getString("name").equals(validatedItem.value2().getString("name"))
                    ) {
                        Component baitStackIcon = Component.literal("+").withStyle(ChatFormatting.GREEN);
                        guiGraphicsExtractor.text(font, baitStackIcon, x + 17 - font.width(baitStackIcon), y - 1, CommonColors.GREEN, true);
                    }
                }
            }
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
        );
    }
    //endregion
}
