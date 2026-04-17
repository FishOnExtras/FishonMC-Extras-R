package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.fetch.ScoreboardHandler;
import dannypx.foe.handler.store.CrewDataHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.custom_text.CustomTextValue;
import dannypx.foe.type.custom_text.StringValue;
import dannypx.foe.type.custom_text.TextValue;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class CrewHandler extends Handler {
    private static CrewHandler INSTANCE = new CrewHandler();

    public static CrewHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CrewHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private Map<UUID, Long> pendingLeavesList = new HashMap<>();

    List<Pair<UUID, String>> crewListOrdered = new ArrayList<>();
    List<Pair<UUID, String>> onlineMembers = new ArrayList<>();
    List<Pair<UUID, String>> offlineMembers = new ArrayList<>();

    boolean isCrewNearby = false;

    int leaveDelay = 20;

    public List<Pair<UUID, String>> getCrewListOrdered() {
        return crewListOrdered;
    }

    public List<Pair<UUID, String>> getOnlineMembers() {
        return onlineMembers;
    }

    public List<Pair<UUID, String>> getOfflineMembers() {
        return offlineMembers;
    }

    public boolean isCrewNearby() {
        return isCrewNearby;
    }

    public Pair<Boolean, CustomTextValue> getCrew(String[] params) {
        if(params.length > 0) {
            Pattern crewListPattern = Pattern.compile("^(online|offline|is_crew_nearby)$");
            Pattern intPattern = Pattern.compile("^-?\\d+$");
            Pattern crewPattern = Pattern.compile("^(id|name)$");

            if(crewListPattern.matcher(params[0]).matches()) {
                List<Pair<UUID, String>> list;

                switch (params[0]) {
                    case "online" -> list = getOnlineMembers();
                    case "offline" -> list = getOfflineMembers();
                    case "is_crew_nearby" -> {
                        return PlaceholderHandler.getTextValue(new TextValue(TextHelper.literal(isCrewNearby(), true)));
                    }
                    default -> list = new ArrayList<>();
                }

                if(params.length == 3
                        && intPattern.matcher(params[1]).matches()
                        && crewPattern.matcher(params[2]).matches()
                ) {
                    int index = Integer.parseInt(params[1]);
                    if(list.size() > index) {
                        Pair<UUID, String> crew = list.get(index);
                        return switch (params[2]) {
                            case "id" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(crew.value1())));
                            case "name" -> PlaceholderHandler.getTextValue(new StringValue(crew.value2()));
                            default -> PlaceholderHandler.noResult();
                        };
                    }
                }
            }
        }
        return PlaceholderHandler.noResult();
    }

    //endregion

    //region Method
    @Override
    public void init() {
        onlineMembers.clear();
        offlineMembers.clear();
        pendingLeavesList.clear();
        crewListOrdered.clear();
    }

    public void tick() {
        if(!ScoreboardHandler.instance().getCrew().getString().isBlank()) {
            if(crewListOrdered.isEmpty()) this.updateCrewOrderedList(CrewDataHandler.instance().getCrewData().crewList);
        }

        if(minecraftClient.player != null) {
            this.checkCrewNearby();
        }

        pendingLeavesList.forEach(((uuid, time) -> {
            if(System.currentTimeMillis() > time + (leaveDelay * 50L) + 1000L) CodeExecuterHandler.runLater(1, "CrewHandler > tick > pendingLeavesList.forEach", () -> pendingLeavesList.remove(uuid));
        }));
    }

    private void checkCrewNearby() {
        Box searchBox = minecraftClient.player.getBoundingBox().expand(10d);
        ClientWorld world = minecraftClient.player.clientWorld;

        List<PlayerEntity> playerEntities = world.getEntitiesByClass(
                PlayerEntity.class,
                searchBox,
                playerEntity -> {
                    if(playerEntity.getUuid().equals(minecraftClient.player.getUuid())) return false;

                    return CrewDataHandler.instance().getCrewData().crewList.containsKey(playerEntity.getUuid())
                            && playerEntity.getPos().distanceTo(minecraftClient.player.getPos()) < 10d;
                }
        );

        this.isCrewNearby = !playerEntities.isEmpty();
    }

    public void updateCrewOrderedList(Map<UUID, Pair<String, ItemStack>> crewMap) {
        this.crewListOrdered.clear();
        crewMap.forEach(((uuid, s) -> this.crewListOrdered.add(Pair.of(uuid, s.value1()))));


        this.fetchCrewMemberStatus();
    }

    private void fetchCrewMemberStatus() {
        onlineMembers.clear();
        offlineMembers.clear();
        crewListOrdered.forEach(crew -> {
            PlayerListEntry playerListEntry = minecraftClient.getNetworkHandler().getPlayerListEntry(crew.value1());
            if(playerListEntry != null) {
                onlineMembers.add(crew);
            } else {
                offlineMembers.add(crew);
            }
        });
    }

    public void updatePlayerToOffline(UUID id) {
        AtomicReference<Pair<UUID, String>> updatedMember = new AtomicReference<>();

        onlineMembers.removeIf(crew -> {
            if(crew.value1().equals(id)) {
                updatedMember.set(crew);
                return true;
            }
            return false;
        });

        if(updatedMember.get() != null) {
            Pair<String, ItemStack> crewMember = CrewDataHandler.instance().getCrewData().crewList.get(updatedMember.get().value1());

            LoggerHandler._debug("player " + crewMember.value1() + " left");

            offlineMembers.add(updatedMember.get());
            NotifierHandler.instance().notifyPlayerStatus(false, crewMember);
            CodeExecuterHandler.runLater(1, "updatePlayerToOffline > onCrewLeave", EventHandler.instance()::onCrewLeave);
        }
    }

    public void updatePlayerToOnline(UUID id) {
        AtomicReference<Pair<UUID, String>> updatedMember = new AtomicReference<>();

        offlineMembers.removeIf(crew -> {
            if(crew.value1().equals(id)) {
                updatedMember.set(crew);
                return true;
            }
            return false;
        });

        if(updatedMember.get() != null) {
            Pair<String, ItemStack> crewMember = CrewDataHandler.instance().getCrewData().crewList.get(updatedMember.get().value1());

            LoggerHandler._debug("player " + crewMember.value1() + " joined");

            onlineMembers.add(updatedMember.get());
            NotifierHandler.instance().notifyPlayerStatus(true, crewMember);
            CodeExecuterHandler.runLater(1, "updatePlayerToOnline > onCrewJoin", EventHandler.instance()::onCrewJoin);
        }
    }

    public void onPlayerJoin(UUID uuid) {
        if(LoadingHandler.instance().isLoadingDone()
                && !ScoreboardHandler.instance().getCrew().getString().isBlank()
                && CrewDataHandler.instance().getCrewData().crewList.containsKey(uuid)
        ) {
            pendingLeavesList.remove(uuid);

            if(onlineMembers.stream().noneMatch(m -> m.value1().equals(uuid))) {
                updatePlayerToOnline(uuid);
            }
        }
    }

    public void onPlayerLeave(UUID uuid) {
        if(LoadingHandler.instance().isLoadingDone()
                && !ScoreboardHandler.instance().getCrew().getString().isBlank()
                && CrewDataHandler.instance().getCrewData().crewList.containsKey(uuid)
        ) {
            this.pendingLeavesList.put(uuid, System.currentTimeMillis());

            // Delay leaves in case of proxy change
            CodeExecuterHandler.runLater(leaveDelay, "onPlayerLeave", () -> {
                if(this.pendingLeavesList.containsKey(uuid)) {
                    this.pendingLeavesList.remove(uuid);
                    updatePlayerToOffline(uuid);
                }
            });
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    @Override
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "key", Pair.of(Text.literal("value"), Text.empty())
        );
    }
    //endregion
}
