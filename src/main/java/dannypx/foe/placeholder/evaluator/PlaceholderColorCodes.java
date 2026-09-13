package dannypx.foe.placeholder.evaluator;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Optional;

public class PlaceholderColorCodes {
    public static class Tracker {
        private Style currentStyle = Style.EMPTY;

        public MutableComponent consumeLiteral(String text) {
            MutableComponent result = Component.empty();
            StringBuilder segment = new StringBuilder();
            int i = 0;

            while(i < text.length()) {
                char c = text.charAt(i);

                if(c == '&' && i + 1 < text.length()) {
                    char next = text.charAt(i + 1);

                    if(next == '#' && i + 8 <= text.length() && Tracker.isHex(text, i + 2)) {
                        this.flush(result, segment);
                        int rgb = Integer.parseInt(text.substring(i + 2, i + 8), 16);
                        currentStyle = Style.EMPTY.withColor(TextColor.fromRgb(rgb));

                        i += 8;
                        continue;
                    }

                    ChatFormatting formatting = ChatFormatting.getByCode(next);
                    if(formatting != null) {
                        this.flush(result, segment);
                        if(formatting == ChatFormatting.RESET) {
                            currentStyle = Style.EMPTY;
                        } else if (formatting.isColor()) {
                            currentStyle = Style.EMPTY.applyFormat(formatting);
                        } else {
                            currentStyle = currentStyle.applyFormat(formatting);
                        }

                        i += 2;
                        continue;
                    }
                }

                segment.append(c);
                i++;
            }

            this.flush(result, segment);
            return result;
        }

        public MutableComponent applyActiveStyle(MutableComponent component) {
            if(component.getStyle().getColor() != null) {
                return component;
            }

            MutableComponent result = Component.empty();
            component.visit((style, text) -> {
                if(!text.isEmpty()) {
                    Style styled = style.getColor() != null
                            ? style
                            : style.withColor(currentStyle.getColor());
                    result.append(Component.literal(text).setStyle(styled));
                }
                return Optional.empty();
            }, Style.EMPTY);
            return result;
        }

        private void flush(MutableComponent result, StringBuilder segment) {
            if(!segment.isEmpty()) {
                result.append(Component.literal(segment.toString()).setStyle(currentStyle));
                segment.setLength(0);
            }
        }

        private static boolean isHex(String text, int start) {
            if(start + 6 > text.length()) return false;
            for (int i = 0; i < 6; i++) {
                if(Character.digit(text.charAt(start + i), 16) == -1) return false;
            }
            return true;
        }
    }
}
