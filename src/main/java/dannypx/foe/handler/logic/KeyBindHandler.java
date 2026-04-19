package dannypx.foe.handler.logic;

import dannypx.foe.config.Configs;
import dannypx.foe.handler.Handler;
import dannypx.foe.helper.KeyBindHelper;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.custom_text.CustomTextValue;
import dannypx.foe.type.custom_text.StringValue;
import dannypx.foe.screens.MainScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.regex.Pattern;

public class KeyBindHandler extends Handler {
    private static KeyBindHandler INSTANCE = new KeyBindHandler();

    public static KeyBindHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new KeyBindHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private boolean isPressingInspect = false;
    private boolean wasInspectKeyDown = false;

    public boolean isPressingInspect() {
        return isPressingInspect;
    }

    public Pair<Boolean, CustomTextValue> getKeyBind(String[] params) {
        if(params.length > 0) {
            Pattern fieldPattern = Pattern.compile("^(open_main_keybind|inspect_keybind)$");

            if(fieldPattern.matcher(params[0]).matches()
                    && params.length == 1
            ) {
                return switch(params[0]) {
                    case "open_main_keybind" -> PlaceholderHandler.getTextValue(new StringValue(KeyBindHelper.getKeyText(Configs.keyBindConfig.openMainKeybind)));
                    case "inspect_keybind" -> PlaceholderHandler.getTextValue(new StringValue(KeyBindHelper.getKeyText(Configs.keyBindConfig.inspectKeybind)));
                    default -> PlaceholderHandler.noResult();
                };
            }
        }
        return PlaceholderHandler.noResult();
    }
    //endregion

    //region Methods
    public void tick() {
        if(minecraftClient.currentScreen == null
                && KeyBindHelper.isPressed(Configs.keyBindConfig.openMainKeybind)
        ) {
            minecraftClient.setScreen(new MainScreen(minecraftClient.currentScreen));
        }

        switch (Configs.keyBindConfig.inspectMode.get()) {
            case HOLD -> isPressingInspect = KeyBindHelper.isPressed(Configs.keyBindConfig.inspectKeybind);
            case TOGGLE -> {
                boolean isKeyDown = KeyBindHelper.isPressed(Configs.keyBindConfig.inspectKeybind);
                if (isKeyDown && !wasInspectKeyDown) isPressingInspect = !isPressingInspect;
                wasInspectKeyDown = isKeyDown;
            }
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "openMainKeybind", Pair.of(Text.literal(KeyBindHelper.getKeyText(Configs.keyBindConfig.openMainKeybind)), Text.empty()),
                "inspectKeybind", Pair.of(Text.literal(KeyBindHelper.getKeyText(Configs.keyBindConfig.inspectKeybind)), Text.empty())
        );
    }
    //endregion
}
