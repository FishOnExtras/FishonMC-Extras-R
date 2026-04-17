package dannypx.foe.helper;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.item.NbtObject;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.type_adapter.ItemStackAdapter;
import dannypx.foe.type.type_adapter.TextAdapter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TextHelper {
    private static final GsonBuilder gson = new GsonBuilder();

    public static MutableText concat(Text... texts) {
        MutableText text = Text.empty();
        for (Text t : texts) {
            text.append(t);
        }
        return text;
    }

    public static MutableText literal(boolean b) {
        return Text.literal(Boolean.toString(b));
    }

    public static MutableText literal(boolean b, boolean fancy) {
        return fancy ? (
                    b
                    ? Text.literal("✔").formatted(Formatting.GREEN, Formatting.BOLD)
                    : Text.literal("✖").formatted(Formatting.DARK_RED, Formatting.BOLD)
                ) : literal(b);
    }

    public static MutableText literal(DataModels.DataModel dataModel) {
        return Text.literal(gson
                .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
                .registerTypeAdapter(Text.class, new TextAdapter())
                .setPrettyPrinting()
                .create().toJson(dataModel));
    }

    public static MutableText literal(int i) {
        return Text.literal(Integer.toString(i));
    }

    public static MutableText literal(char c) {
        return Text.literal(String.valueOf(c));
    }

    public static MutableText literal(float f) {
        return Text.literal(Float.toString(f));
    }

    public static MutableText literal(Text text) {
        return concat(text);
    }

    @SuppressWarnings("unchecked")
    public static <T> MutableText literal(List<T> list) {
        try {
            if(!list.isEmpty()) {
                Object first = list.getFirst();
                if(first instanceof ItemStack) return Text.empty().append(ItemStackHelper.itemStackListToJson((List<ItemStack>) list));
                return Text.literal(
                        gson.setPrettyPrinting()
                                .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
                                .registerTypeAdapter(Text.class, new TextAdapter())
                                .create()
                                .toJson(list)
                );
            }
            return Text.empty();
        } catch (IllegalStateException e) {
            return Text.empty();
        }


    }

    public static MutableText literal(String s) {
        return Text.empty().append(Text.literal(s));
    }

    public static MutableText literal(ItemStack i) {
        return Text.empty().append(ItemStackHelper.itemStackToJson(i));
    }

    public static MutableText literal(NbtObject currentHeldItem) {
        return TextHelper.concat(
                Text.literal("name: "), currentHeldItem.getName(), Text.literal("\n"),
                Text.literal("rarity: "), Text.literal(currentHeldItem.getRarity()), Text.literal("\n"),
                Text.literal("type: "), Text.literal(currentHeldItem.getType())
        );
    }

    public static String textListToJson(List<Text> list) {
        return gson.setPrettyPrinting().create().toJson(TextCodecs.CODEC.listOf().encodeStart(JsonOps.INSTANCE, list).getOrThrow());
    }

    public static String smallText(String string) {
        return smallLetter(smallNumber(string));
    }

    public static char smallChar(char c) {
        if(isNumber(c)) {
            return smallNumber(c);
        } else if(isLetter(c)) {
            return smallLetter(c);
        } else {
            return c;
        }
    }

    public static String smallNumber(String string) {
        // based on numeric ping
        char[] characters = new char[string.length()];

        for (int index = 0; index < string.length(); index++) {
            characters[index] = string.charAt(index);

            if (isNumber(characters[index]))
                characters[index] = smallNumber(characters[index]);
        }

        return String.valueOf(characters);
    }

    public static char smallNumber(char c) {
        return (char) (c + 8272);
    }

    public static String smallLetter(String string) {
        // based on numeric ping
        char[] characters = new char[string.length()];

        for (int index = 0; index < string.length(); index++) {
            characters[index] = string.charAt(index);

            characters[index] = smallLetter(characters[index]);
        }

        return String.valueOf(characters);
    }

    public static char smallLetter(char c) {
        return switch (c) {
            case 'A', 'a' -> 'ᴀ';
            case 'B', 'b' -> 'ʙ';
            case 'C', 'c' -> 'ᴄ';
            case 'D', 'd' -> 'ᴅ';
            case 'E', 'e' -> 'ᴇ';
            case 'F', 'f' -> 'ꜰ';
            case 'G', 'g' -> 'ɢ';
            case 'H', 'h' -> 'ʜ';
            case 'I', 'i' -> 'ɪ';
            case 'J', 'j' -> 'ᴊ';
            case 'K', 'k' -> 'ᴋ';
            case 'L', 'l' -> 'ʟ';
            case 'M', 'm' -> 'ᴍ';
            case 'N', 'n' -> 'ɴ';
            case 'O', 'o' -> 'ᴏ';
            case 'P', 'p' -> 'ᴘ';
            case 'Q', 'q' -> 'ꞯ';
            case 'R', 'r' -> 'ʀ';
            case 'S', 's' -> 's';
            case 'T', 't' -> 'ᴛ';
            case 'U', 'u' -> 'ᴜ';
            case 'V', 'v' -> 'ᴠ';
            case 'W', 'w' -> 'ᴡ';
            case 'X', 'x' -> 'x';
            case 'Y', 'y' -> 'ʏ';
            case 'Z', 'z' -> 'ᴢ';
            default -> c;
        };
    }

    public static String normalLetter(String string) {
        // based on numeric ping
        char[] characters = new char[string.length()];

        for (int index = 0; index < string.length(); index++) {
            characters[index] = string.charAt(index);

            characters[index] = normalLetter(characters[index]);
        }

        return String.valueOf(characters);
    }

    public static char normalLetter(char c) {
        return switch (c) {
            case 'ᴀ' -> 'a';
            case 'ʙ' -> 'b';
            case 'ᴄ' -> 'c';
            case 'ᴅ' -> 'd';
            case 'ᴇ' -> 'e';
            case 'ꜰ' -> 'f';
            case 'ɢ' -> 'g';
            case 'ʜ' -> 'h';
            case 'ɪ' -> 'i';
            case 'ᴊ' -> 'j';
            case 'ᴋ' -> 'k';
            case 'ʟ' -> 'l';
            case 'ᴍ' -> 'm';
            case 'ɴ' -> 'n';
            case 'ᴏ' -> 'o';
            case 'ᴘ' -> 'p';
            case 'ꞯ' -> 'q';
            case 'ʀ' -> 'r';
            case 's' -> 's';
            case 'ᴛ' -> 't';
            case 'ᴜ' -> 'u';
            case 'ᴠ' -> 'v';
            case 'ᴡ' -> 'w';
            case 'x' -> 'x';
            case 'ʏ' -> 'y';
            case 'ᴢ' -> 'z';
            default -> c;
        };
    }

    public static boolean isNumber(char c) {
        return (c >= '0' && c <= '9');
    }

    public static boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    public static boolean isSmallNumber(char c) {
        return (c >= '₀' && c <= '₉');
    }

    public static boolean isSmallLetter(char c) {
        return switch (c) {
            case 'ᴀ', 'ʙ', 'ᴄ', 'ᴅ', 'ᴇ', 'ꜰ', 'ɢ', 'ʜ', 'ɪ', 'ᴊ', 'ᴋ', 'ʟ', 'ᴍ', 'ɴ', 'ᴏ', 'ᴘ', 'ꞯ', 'ʀ', 's', 'ᴛ',
                 'ᴜ', 'ᴠ', 'ᴡ', 'x', 'ʏ', 'ᴢ', '.', ',', ':', ';', '_' -> true;
            default -> false;
        };
    }

    public static boolean isCustomFont(char c) {
        return (c >= '\uF000' && c <= '\uF999');
    }

    public static String shortenNumber(float d, int decimals) {
        if(d >= 1000 && d < 1000000) {
            String s = String.format(Locale.US, "%." + decimals + "f", d / 1000);
            return (s.contains(".") ? s.replaceAll("0*$","").replaceAll("\\.$","") : s) + "K";
        } else if (d >= 1000000 && d < 1000000000 ){
            String s = String.format(Locale.US, "%." + decimals + "f", d / 1000000);
            return (s.contains(".") ? s.replaceAll("0*$","").replaceAll("\\.$","") : s) + "M";
        } else if (d >= 1000000000) {
            String s = String.format(Locale.US, "%." + decimals + "f", d / 1000000000);
            return (s.contains(".") ? s.replaceAll("0*$","").replaceAll("\\.$","") : s) + "B";
        } else {
            String s = String.format(Locale.US, "%.0f", d);
            return s.contains(".") ? s.replaceAll("0*$","").replaceAll("\\.$","") : s;
        }
    }

    public static int toIntFromString(String value) {
        value = value.trim();
        if(value.contains("K")) {
            return (int) (Float.parseFloat(value.substring(0, value.indexOf("K"))) * 1000f);
        } else if(value.contains("M")) {
            return (int) (Float.parseFloat(value.substring(0, value.indexOf("M"))) * 1000000f);
        } else {
            return Integer.parseInt(value);
        }
    }

    public static String shortenNumber(int i, int decimals) {
        return shortenNumber((float) i, decimals);
    }

    public static String floatToString(float f, int decimals) {
        return String.format(Locale.US, "%." + decimals + "f", f);
    }

    public static float lbToKg(float f) {
        return f * 0.4535924f;
    }

    public static float inchToCm(float f) {
        return f * 2.54f;
    }

    public static String capitalize(String s) {
        return StringUtils.capitalize(s);
    }

    public static String splitTitleCase(String s) {
        return String.join(" ", s.split("(?<!^)(?=[A-Z])"));
    }

    public static String convertField(String s) {
        return splitTitleCase(capitalize(s)).replace("_", " ");
    }

    public static int getWidth(TextRenderer textRenderer, Text text, boolean isSmall) {
        AtomicInteger width = new AtomicInteger(0);
        if(text.getSiblings().isEmpty()) {
            int calculatedWidth = textRenderer.getWidth(text);
            if(isSmall) {
                calculatedWidth = textRenderer.getWidth(Text.literal(TextHelper.smallText(text.getString())).setStyle(text.getStyle()));
            }
            width.set(width.get() + calculatedWidth);
        } else {
            text.getSiblings().forEach(text1 -> {
                int calculatedWidth = getWidth(textRenderer, text1, isSmall);
                width.set(width.get() + calculatedWidth);
            });
        }
        return width.get();
    }

    public static String textToJson(Text text) {
        return gson.create().toJson(TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow());
    }

    public static String textToJsonPretty(Text text) {
        return gson.setPrettyPrinting().create().toJson(TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow());
    }

    public static Pair<MutableText, Style> parseLegacyWithStyle(String input, Style startingStyle) {
        MutableText text = Text.empty();
        Pattern pattern = Pattern.compile("(§#[0-9A-Fa-f]{6}|§[0-9A-FK-ORa-fk-or])");
        Matcher matcher = pattern.matcher(input);

        int lastEnd = 0;
        Style currentStyle = startingStyle;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                text.append(Text.literal(input.substring(lastEnd, matcher.start())).setStyle(currentStyle));
            }

            String code = matcher.group();
            if (code.equalsIgnoreCase("§r")) {
                currentStyle = Style.EMPTY;
            } else if (code.startsWith("§#")) {
                int rgb = Integer.parseInt(code.substring(2), 16);
                currentStyle = currentStyle.withColor(TextColor.fromRgb(rgb));
            } else {
                Formatting fmt = Formatting.byCode(code.charAt(1));
                currentStyle = currentStyle.withFormatting(fmt);
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < input.length()) {
            text.append(Text.literal(input.substring(lastEnd)).setStyle(currentStyle));
        }

        return Pair.of(text, currentStyle);
    }

    public static Pair<MutableText, Style> parseLegacyWithStyle(String input) {
        return parseLegacyWithStyle(input, Style.EMPTY);
    }

    public static List<Text> wrapStyledText(Text text, int maxWidth, boolean smallText, TextRenderer renderer) {
        List<Text> lines = new ArrayList<>();
        AtomicReference<MutableText> currentLine = new AtomicReference<>(Text.empty());
        AtomicInteger currentLineWidth = new AtomicInteger();

        List<Text> currentWord = new ArrayList<>();
        AtomicInteger currentWordWidth = new AtomicInteger();

        text.visit((style, string) -> {
            for (char c : string.toCharArray()) {
                if (c == ' ') {
                    if (currentLineWidth.get() + currentWordWidth.get() > maxWidth && currentLineWidth.get() > 0) {
                        lines.add(currentLine.get());
                        currentLine.set(Text.empty());
                        currentLineWidth.set(0);
                    }

                    for (Text part : currentWord) {
                        currentLine.get().append(part);
                    }

                    Text space = Text.literal(" ").setStyle(style);
                    currentLine.get().append(space);

                    currentLineWidth.addAndGet(currentWordWidth.get() + TextHelper.getWidth(renderer, Text.literal(" "), smallText));

                    currentWord.clear();
                    currentWordWidth.set(0);
                    continue;
                }

                if (c == '\n') {
                    for (Text part : currentWord) {
                        currentLine.get().append(part);
                    }
                    currentWord.clear();
                    currentWordWidth.set(0);

                    lines.add(currentLine.get());
                    currentLine.set(Text.empty());
                    currentLineWidth.set(0);
                    continue;
                }

                Text charText = Text.literal(String.valueOf(c)).setStyle(style);
                currentWord.add(charText);

                currentWordWidth.addAndGet(TextHelper.getWidth(renderer, Text.literal(String.valueOf(c)), smallText));
            }
            return Optional.empty();
        }, Style.EMPTY);

        if (!currentWord.isEmpty()) {
            if (currentWordWidth.get() > maxWidth) {
                for (Text part : currentWord) {
                    int charWidth = TextHelper.getWidth(renderer, part, smallText);
                    if (currentLineWidth.get() + charWidth > maxWidth && currentLineWidth.get() > 0) {
                        lines.add(currentLine.get());
                        currentLine.set(Text.empty());
                        currentLineWidth.set(0);
                    }
                    currentLine.get().append(part);
                    currentLineWidth.addAndGet(charWidth);
                }
            } else {
                if (currentLineWidth.get() + currentWordWidth.get() > maxWidth && currentLineWidth.get() > 0) {
                    lines.add(currentLine.get());
                    currentLine.set(Text.empty());
                    currentLineWidth.set(0);
                }
                for (Text part : currentWord) currentLine.get().append(part);
                currentLineWidth.addAndGet(currentWordWidth.get());
            }
        }

        if (!currentLine.get().getString().isEmpty()) {
            lines.add(currentLine.get());
        }

        return lines;
    }

    public static Text substring(Text text, int start, int end) {
        int length = text.getString().length();

        if (start < 0 || end < 0 || start > end || end > length) {
            return Text.empty();
        }

        MutableText result = Text.empty();
        AtomicInteger index = new AtomicInteger(0);

        text.visit((style, string) -> {
            int strStart = index.get();
            int strEnd = strStart + string.length();

            if (strEnd > start && strStart < end) {
                int from = Math.max(0, start - strStart);
                int to = Math.min(string.length(), end - strStart);

                String sub = string.substring(from, to);
                result.append(Text.literal(sub).setStyle(style));
            }

            index.addAndGet(string.length());
            return Optional.empty();
        }, Style.EMPTY);

        return result;
    }

    public static Text trim(Text text) {
        String full = text.getString();
        int length = full.length();

        int start = 0;
        while (start < length && Character.isWhitespace(full.charAt(start))) {
            start++;
        }

        int end = length;
        while (end > start && Character.isWhitespace(full.charAt(end - 1))) {
            end--;
        }

        if (start >= end) {
            return Text.empty();
        }

        return substring(text, start, end);
    }

    public static byte[] compress(final String str) throws IOException {
        if ((str == null) || (str.isEmpty())) {
            return null;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.flush();
        gzip.close();
        return obj.toByteArray();
    }

    public static String decompress(final byte[] compressed) throws IOException {
        final StringBuilder outStr = new StringBuilder();
        if ((compressed == null) || (compressed.length == 0)) {
            return "";
        }
        if (isCompressed(compressed)) {
            final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outStr.append(line);
            }
        } else {
            outStr.append(compressed);
        }
        return outStr.toString();
    }

    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    public static Text jsonToText(String json) {
        return TextCodecs.CODEC
                .decode(JsonOps.INSTANCE, gson.create().fromJson(json, JsonElement.class))
                .mapOrElse((com.mojang.datafixers.util.Pair::getFirst), (pairError -> Text.empty()));

    }
}
