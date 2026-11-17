package dannypx.foe.handler.fetch;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CrewHandler;
import dannypx.foe.handler.logic.NotifierHandler;
import dannypx.foe.handler.store.CrewDataHandler;
import dannypx.foe.handler.store.ProfileDataHandler;
import dannypx.foe.type.tuple.Pair;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class CrewScreenHandler extends Handler {
    private static CrewScreenHandler INSTANCE = new CrewScreenHandler();

    public static CrewScreenHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CrewScreenHandler();
        }
        return INSTANCE;
    }

    //region Fields
    //endregion

    //region Methods
    public void checkCrewInfo(ChestMenu chestMenu) {
        ItemStack crewInfoStack = chestMenu.getSlot(13).getItem();
        if(!ScoreboardHandler.instance().getCrew().getString().isBlank()
                && crewInfoStack.get(DataComponents.LORE) != null
        ) {
            List<Component> componentList = crewInfoStack.get(DataComponents.LORE).lines();
            if(componentList.size() > 3
                    && componentList.get(2).getSiblings().size() > 4
                    && Objects.equals(componentList.get(2).getSiblings().get(3).getString(), ScoreboardHandler.instance().getCrew().getString())
            ) {
                Map<UUID, Pair<String, ItemStack>> crewMembers = new HashMap<>();

                for (int i = 28; i < 44; i++) {
                    ItemStack itemStack = chestMenu.getSlot(i).getItem();
                    if(itemStack.getItem() == Items.PLAYER_HEAD
                            && itemStack.get(DataComponents.PROFILE) != null
                    ) {
                        ResolvableProfile profile = itemStack.get(DataComponents.PROFILE);
                        UUID id = profile.partialProfile().properties().get("textures").stream()
                                .map(Property::value)
                                .map(this::parseProfileId)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null);

                        if(id != null
                                && itemStack.getCustomName() != null
                                && itemStack.getCustomName().getSiblings().size() > 1
                        ) {
                            crewMembers.put(id, Pair.of(itemStack.getCustomName().getSiblings().get(1).getString().trim(), itemStack));
                        }
                    }
                }

                if(!crewMembers.isEmpty()) {
                    CrewDataHandler.instance().updateCrewList(crewMembers);
                    CrewHandler.instance().updateCrewOrderedList(crewMembers);

                    if(!ProfileDataHandler.instance().getProfileData().hasImportedCrew) {
                        NotifierHandler.instance().notifyImportCrewCompleted();
                    }

                    if(NotifierHandler.instance().getPersistentNotifications().containsKey(NotifierHandler.CREW_UPDATE_CHANGE_KEY)) {
                        NotifierHandler.instance().removeNotification(NotifierHandler.CREW_UPDATE_CHANGE_KEY);
                        NotifierHandler.instance().notifyImportCrewCompleted();
                    }

                    ProfileDataHandler.instance().updateImportCrew(true);
                }
            }
        }
    }

    private UUID parseProfileId(String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64));
            JsonElement id = JsonParser.parseString(json).getAsJsonObject().get("profileId");
            if(id == null) return null;

            String hex = id.getAsString().replace("-", "");
            return new UUID(
                    Long.parseUnsignedLong(hex.substring(0, 16), 16),
                    Long.parseUnsignedLong(hex.substring(16), 16));
        } catch (Exception e) {
            return null;
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
