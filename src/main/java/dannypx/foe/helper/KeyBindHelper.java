package dannypx.foe.helper;

import me.fzzyhmstrs.fzzy_config.screen.context.FzzyKeybindSimple;
import me.fzzyhmstrs.fzzy_config.screen.context.FzzyKeybindUnbound;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedKeybind;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class KeyBindHelper {
    private static final Map<String, String> KEY_UNICODE_MAP = Map.<String, String>ofEntries(
            // Letters
            Map.entry("key.keyboard.a", "\uDB80\uDC00"),
            Map.entry("key.keyboard.b", "\uDB80\uDC01"),
            Map.entry("key.keyboard.c", "\uDB80\uDC02"),
            Map.entry("key.keyboard.d", "\uDB80\uDC03"),
            Map.entry("key.keyboard.e", "\uDB80\uDC04"),
            Map.entry("key.keyboard.f", "\uDB80\uDC05"),
            Map.entry("key.keyboard.g", "\uDB80\uDC06"),
            Map.entry("key.keyboard.h", "\uDB80\uDC07"),
            Map.entry("key.keyboard.i", "\uDB80\uDC08"),
            Map.entry("key.keyboard.j", "\uDB80\uDC09"),
            Map.entry("key.keyboard.k", "\uDB80\uDC0A"),
            Map.entry("key.keyboard.l", "\uDB80\uDC0B"),
            Map.entry("key.keyboard.m", "\uDB80\uDC0C"),
            Map.entry("key.keyboard.n", "\uDB80\uDC0D"),
            Map.entry("key.keyboard.o", "\uDB80\uDC0E"),
            Map.entry("key.keyboard.p", "\uDB80\uDC0F"),
            Map.entry("key.keyboard.q", "\uDB80\uDC10"),
            Map.entry("key.keyboard.r", "\uDB80\uDC11"),
            Map.entry("key.keyboard.s", "\uDB80\uDC12"),
            Map.entry("key.keyboard.t", "\uDB80\uDC13"),
            Map.entry("key.keyboard.u", "\uDB80\uDC14"),
            Map.entry("key.keyboard.v", "\uDB80\uDC15"),
            Map.entry("key.keyboard.w", "\uDB80\uDC16"),
            Map.entry("key.keyboard.x", "\uDB80\uDC17"),
            Map.entry("key.keyboard.y", "\uDB80\uDC18"),
            Map.entry("key.keyboard.z", "\uDB80\uDC19"),

            // Numbers
            Map.entry("key.keyboard.0", "\uDB80\uDC20"),
            Map.entry("key.keyboard.1", "\uDB80\uDC21"),
            Map.entry("key.keyboard.2", "\uDB80\uDC22"),
            Map.entry("key.keyboard.3", "\uDB80\uDC23"),
            Map.entry("key.keyboard.4", "\uDB80\uDC24"),
            Map.entry("key.keyboard.5", "\uDB80\uDC25"),
            Map.entry("key.keyboard.6", "\uDB80\uDC26"),
            Map.entry("key.keyboard.7", "\uDB80\uDC27"),
            Map.entry("key.keyboard.8", "\uDB80\uDC28"),
            Map.entry("key.keyboard.9", "\uDB80\uDC29"),

            // Controls
            Map.entry("key.keyboard.space", "\uDB80\uDC54"),
            Map.entry("key.keyboard.enter", "\uDB80\uDC55"),
            Map.entry("key.keyboard.escape", "\uDB80\uDC56"),
            Map.entry("key.keyboard.tab", "\uDB80\uDC57"),
            Map.entry("key.keyboard.backspace", "\uDB80\uDC58"),

            // Symbols
            Map.entry("key.keyboard.minus", "\uDB80\uDC44"),
            Map.entry("key.keyboard.equal", "\uDB80\uDC45"),
            Map.entry("key.keyboard.left.bracket", "\uDB80\uDC46"),
            Map.entry("key.keyboard.right.bracket", "\uDB80\uDC47"),
            Map.entry("key.keyboard.backslash", "\uDB80\uDC48"),
            Map.entry("key.keyboard.semicolon", "\uDB80\uDC49"),
            Map.entry("key.keyboard.apostrophe", "\uDB80\uDC4A"),
            Map.entry("key.keyboard.grave.accent", "\uDB80\uDC4B"),
            Map.entry("key.keyboard.comma", "\uDB80\uDC4C"),
            Map.entry("key.keyboard.period", "\uDB80\uDC4D"),
            Map.entry("key.keyboard.slash", "\uDB80\uDC4E"),

            // Modifiers
            Map.entry("key.keyboard.left.shift", "\uDB80\uDC59"),
            Map.entry("key.keyboard.right.shift", "\uDB80\uDC59"),
            Map.entry("key.keyboard.left.control", "\uDB80\uDC5B"),
            Map.entry("key.keyboard.right.control", "\uDB80\uDC5B"),
            Map.entry("key.keyboard.left.alt", "\uDB80\uDC5A"),
            Map.entry("key.keyboard.right.alt", "\uDB80\uDC5A"),

            // System Keys
            Map.entry("key.keyboard.caps.lock", "\uDB80\uDC5C"),
            Map.entry("key.keyboard.insert", "\uDB80\uDC5D"),
            Map.entry("key.keyboard.delete", "\uDB80\uDC5F"),
            Map.entry("key.keyboard.home", "\uDB80\uDC60"),
            Map.entry("key.keyboard.end", "\uDB80\uDC61"),
            Map.entry("key.keyboard.page.up", "\uDB80\uDC62"),
            Map.entry("key.keyboard.page.down", "\uDB80\uDC63"),

            // Extended Navidation
            Map.entry("key.keyboard.print.screen", "\uDB80\uDC64"),
            Map.entry("key.keyboard.scroll.lock", "\uDB80\uDC65"),
            Map.entry("key.keyboard.pause", "\uDB80\uDC66"),

            // Arrows
            Map.entry("key.keyboard.up", "\uDB80\uDC40"),
            Map.entry("key.keyboard.down", "\uDB80\uDC41"),
            Map.entry("key.keyboard.left", "\uDB80\uDC42"),
            Map.entry("key.keyboard.right", "\uDB80\uDC43"),

            // Function keys
            Map.entry("key.keyboard.f1", "\uDB80\uDC30"),
            Map.entry("key.keyboard.f2", "\uDB80\uDC31"),
            Map.entry("key.keyboard.f3", "\uDB80\uDC32"),
            Map.entry("key.keyboard.f4", "\uDB80\uDC33"),
            Map.entry("key.keyboard.f5", "\uDB80\uDC34"),
            Map.entry("key.keyboard.f6", "\uDB80\uDC35"),
            Map.entry("key.keyboard.f7", "\uDB80\uDC36"),
            Map.entry("key.keyboard.f8", "\uDB80\uDC37"),
            Map.entry("key.keyboard.f9", "\uDB80\uDC38"),
            Map.entry("key.keyboard.f10", "\uDB80\uDC39"),
            Map.entry("key.keyboard.f11", "\uDB80\uDC3A"),
            Map.entry("key.keyboard.f12", "\uDB80\uDC3B"),

            // Keypad
            Map.entry("key.keyboard.keypad.0", "\uDB80\uDC20"),
            Map.entry("key.keyboard.keypad.1", "\uDB80\uDC21"),
            Map.entry("key.keyboard.keypad.2", "\uDB80\uDC22"),
            Map.entry("key.keyboard.keypad.3", "\uDB80\uDC23"),
            Map.entry("key.keyboard.keypad.4", "\uDB80\uDC24"),
            Map.entry("key.keyboard.keypad.5", "\uDB80\uDC25"),
            Map.entry("key.keyboard.keypad.6", "\uDB80\uDC26"),
            Map.entry("key.keyboard.keypad.7", "\uDB80\uDC27"),
            Map.entry("key.keyboard.keypad.8", "\uDB80\uDC28"),
            Map.entry("key.keyboard.keypad.9", "\uDB80\uDC29"),
            Map.entry("key.keyboard.keypad.add", "\uDB80\uDC4F"),
            Map.entry("key.keyboard.keypad.subtract", "\uDB80\uDC44"),
            Map.entry("key.keyboard.keypad.multiply", "\uDB80\uDC50"),
            Map.entry("key.keyboard.keypad.divide", "\uDB80\uDC4E"),
            Map.entry("key.keyboard.keypad.decimal", "\uDB80\uDC4D"),
            Map.entry("key.keyboard.keypad.enter", "\uDB80\uDC51"),
            Map.entry("key.keyboard.keypad.equal", "\uDB80\uDC45")
    );

    public static String getKeyUnicode(ValidatedKeybind validatedKeybind) {
        if(validatedKeybind.get() instanceof FzzyKeybindSimple) {
            String translatableKey = getTranslatableKey(validatedKeybind);
            String key = getKey(translatableKey);

            if(validatedKeybind.needsAlt()) key = "\uDB80\uDC5A " + key;
            if(validatedKeybind.needsShift()) key = "\uDB80\uDC59 " + key;
            if(validatedKeybind.needsCtrl()) key = "\uDB80\uDC5B " + key;

            return key;
        }  else if (validatedKeybind.get() instanceof FzzyKeybindUnbound) {
            return "Not Bound";
        }
        return "Unknown";
    }

    public static String getKeyString(ValidatedKeybind validatedKeybind) {
        String key = "Unknown";
        if(validatedKeybind.get() instanceof FzzyKeybindSimple) {
            key = getTranslatableKey(validatedKeybind);

            if(validatedKeybind.needsCtrl()) key = "control " + key;
            if(validatedKeybind.needsShift()) key = "shift " + key;
            if(validatedKeybind.needsAlt()) key = "alt " + key;

        }  else if (validatedKeybind.get() instanceof FzzyKeybindUnbound) {
            return "Not Bound";
        }

        key = key.replace("key.keyboard.", "")
                .replace("key.mouse.", "mouse ")
                .replace(".", " ");

        return key;
    }

    public static String getTranslatableKey(ValidatedKeybind validatedKeybind) {
        if(validatedKeybind.get() instanceof FzzyKeybindSimple fzzyKeybindSimple) {
            InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(fzzyKeybindSimple.getInputCode());
            return key.getName();
        } else if (validatedKeybind.get() instanceof FzzyKeybindUnbound) {
            return "Not Bound";
        }
        return "Unknown";
    }

    public static boolean isPressed(ValidatedKeybind validatedKeybind) {
        if(validatedKeybind.isPressed()) {
            return true;
        } else if(!validatedKeybind.needsAlt()
                && !validatedKeybind.needsShift()
                && !validatedKeybind.needsCtrl()
                && validatedKeybind.get() instanceof FzzyKeybindSimple fzzyKeybindSimple
        ) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), fzzyKeybindSimple.getInputCode());
        }

        return false;
    }

    public static String getKey(String key) {
        return KEY_UNICODE_MAP.getOrDefault(key, key);
    }

    public static String getKeyFromCodes(int ...codes) {
        StringBuilder keysBuilder = new StringBuilder();
        for (int code : codes) {
            String key = InputConstants.Type.KEYSYM.getOrCreate(code).getName();
            keysBuilder.append(getKey(key));
        }
        return keysBuilder.toString();
    }
}
