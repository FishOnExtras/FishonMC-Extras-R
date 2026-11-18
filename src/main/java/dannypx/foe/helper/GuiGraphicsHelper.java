package dannypx.foe.helper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import dannypx.foe.type.StringStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;
import org.joml.Matrix3x2f;

public class GuiGraphicsHelper {
    public static void drawString(GuiGraphics guiGraphics, Font font, Component component, int x, int y, StringStyle... stringStyles) {
        EnumSet<StringStyle> styles = stringStyles.length == 0
                ? EnumSet.noneOf(StringStyle.class)
                : EnumSet.copyOf(Arrays.asList(stringStyles));
        drawString(guiGraphics, font, component, x, y, Style.EMPTY, styles);
    }

    private static int drawString(GuiGraphics guiGraphics, Font font, Component component, int x, int y, Style parentStyle, EnumSet<StringStyle> styles) {
        Style style = component.getStyle().applyTo(parentStyle);

        String own = component.plainCopy().getString();
        if (!own.isEmpty()) {
            x = drawGlyphs(guiGraphics, font, own, x, y, style, styles);
        }

        for (Component sibling : component.getSiblings()) {
            x = drawString(guiGraphics, font, sibling, x, y, style, styles);
        }

        return x;
    }

    private static int drawGlyphs(GuiGraphics guiGraphics, Font font, String text, int x, int y, Style style, EnumSet<StringStyle> styles) {
        boolean shadow = styles.contains(StringStyle.SHADOW);
        boolean middle = styles.contains(StringStyle.MIDDLE);
        boolean hasCustomFont = styles.contains(StringStyle.HAS_CUSTOM_FONT);
        boolean smallCaps = styles.contains(StringStyle.SMALL_CAPS);

        Deque<Character> characters = text.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toCollection(ArrayDeque::new));

        while (!characters.isEmpty()) {
            String glyph = popNextGlyph(characters);
            if (smallCaps) {
                glyph = TextHelper.smallCaps(glyph);
            }

            int cWidth = font.width(Component.literal(glyph).setStyle(style));
            int yAdjust = computeYAdjustment(glyph, middle, hasCustomFont, smallCaps);

            guiGraphics.guiRenderState.submitText(
                    new GuiTextRenderState(
                            font,
                            Component.literal(glyph).setStyle(style).getVisualOrderText(),
                            new Matrix3x2f(guiGraphics.pose()),
                            x, y - yAdjust, CommonColors.WHITE,
                            0, shadow, false, guiGraphics.scissorStack.peek()
                    )
            );

            x += cWidth;
        }

        return x;
    }

    private static int computeYAdjustment(String glyph, boolean middle, boolean hasCustomFont, boolean smallCaps) {
        int translateY = middle ? -1 : 0;
        int offsetY = 0;

        if (glyph.length() == 1) {
            char c = glyph.charAt(0);
            if (TextHelper.isSmallNumber(c)) {
                offsetY = 1;
            } else if (hasCustomFont && smallCaps && TextHelper.isRank(c)) {
                offsetY = -1;
            } else if (TextHelper.isSmallLetter(c) || (hasCustomFont && TextHelper.isCustomFont(c))) {

            } else {
                translateY = 0;
            }
        }

        return offsetY - translateY;
    }

    private static String popNextGlyph(Deque<Character> characters) {
        if (characters.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        char first = characters.pollFirst();
        sb.append(first);

        if (Character.isHighSurrogate(first) && !characters.isEmpty() && Character.isLowSurrogate(characters.peekFirst())) {
            sb.append(characters.pollFirst());
        }

        while (!characters.isEmpty()) {
            char next = characters.peekFirst();
            int codePoint = (Character.isHighSurrogate(next) && characters.size() > 1 && Character.isLowSurrogate(peekSecond(characters)))
                    ? Character.toCodePoint(next, peekSecond(characters))
                    : next;

            boolean isModifier = next == '\uFE0F' || next == '\uFE0E' || next == '\u200D'
                    || (codePoint >= 0x1_F3FB && codePoint <= 0x1F3FF);

            if (!isModifier) break;

            sb.append(characters.pollFirst());
            if (Character.isSupplementaryCodePoint(codePoint)) {
                sb.append(characters.pollFirst());
            }
        }
        return sb.toString();
    }

    private static Character peekSecond(Deque<Character> characters) {
        Iterator<Character> it = characters.iterator();
        it.next();
        return it.next();
    }

    public static void drawHorizontalGradient(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int leftColor, int rightColor) {
        int width = x2 - x1;
        for (int i = 0; i < width; i++) {
            float t = i / (float) (width - 1);
            int r = (int) ( ((leftColor >> 16 & 0xFF) * (1 - t)) + ((rightColor >> 16 & 0xFF) * t) );
            int g = (int) ( ((leftColor >> 8 & 0xFF) * (1 - t)) + ((rightColor >> 8 & 0xFF) * t) );
            int b = (int) ( ((leftColor & 0xFF) * (1 - t)) + ((rightColor & 0xFF) * t) );
            int a = (int) ( ((leftColor >> 24 & 0xFF) * (1 - t)) + ((rightColor >> 24 & 0xFF) * t) );

            int color = (a << 24) | (r << 16) | (g << 8) | b;
            guiGraphics.fill(x1 + i, y1, x1 + i + 1, y2, color);
        }
    }

    public static void drawLine(GuiGraphics guiGraphics,
                                int x1, int y1,
                                int x2, int y2,
                                int color) {

        int dx = Math.abs(x2 - x1);
        int dy = -Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx + dy;

        while (true) {
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color);

            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x1 += sx; }
            if (e2 <= dx) { err += dx; y1 += sy; }
        }
    }
}

