package dannypx.foe.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dannypx.foe.FishOnMCExtras;
import dannypx.foe.config.Configs;
import dannypx.foe.handler.fetch.ChatHandler;
import dannypx.foe.handler.fetch.StatsScreenHandler;
import dannypx.foe.handler.logic.TimerHandler;
import dannypx.foe.handler.store.*;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.screens.MainScreen;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CommandRegistry {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
    }

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                command("foe")
                .then(command("config").executes(Command.Foe::openConfig))
                .then(command("main").executes(Command.Foe::openMainScreen))
                .then(command("stats")
                        .then(command("import").executes(Command.Stats::importStats))
                        .then(command("cancel").executes(Command.Stats::cancelStats))
                        .then(command("reset").executes(Command.Stats::resetStats))
                )
                .then(command("crew")
                        .then(command("import").executes(Command.Crew::importCrew))
                        .then(command("cancel").executes(Command.Crew::cancelCrew))
                )
                .then(command("reset_to_defaults")
                        .then(command("button").executes(Command.Reset::resetButton))
                        .then(command("chat_trigger").executes(Command.Reset::resetChatTrigger))
                        .then(command("notification").executes(Command.Reset::resetNotification))
                        .then(command("chat_notification").executes(Command.Reset::resetChatNotification))
                        .then(command("timer").executes(Command.Reset::resetTimer))
                        .then(command("hud").executes(Command.Reset::resetHud))
                )
                .then(command("toggle")
                        .then(command("render")
                                .then(command("armor").executes(Command.Toggle::toggleArmor))
                                .then(command("pet_names").executes(Command.Toggle::togglePetNames))
                                .then(command("bobber_model").executes(Command.Toggle::toggleBobberModel))
                                .then(command("bait_on_bobber").executes(Command.Toggle::toggleBaitOnBobber))
                        )
                )
                .executes(Command.Foe::openMainScreen)
        );
    }

    private static class Command {
        static class Foe {
            public static int openConfig(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(() -> ConfigApiJava.INSTANCE.openScreen(FishOnMCExtras.MOD_ID));
            }

            public static int openMainScreen(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(() -> MinecraftClient.getInstance().setScreen(new MainScreen(MinecraftClient.getInstance().currentScreen)));
            }
        }

        static class Stats {
            public static int importStats(CommandContext<FabricClientCommandSource> context) {
                StatsScreenHandler.instance().setImportStats(true);
                return executeCommand(() -> {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.networkHandler.sendChatCommand("stats");
                    }
                });
            }

            public static int cancelStats(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(() -> ProfileDataHandler.instance().updateImportStats(true));
            }

            public static int resetStats(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(() -> StatsDataHandler.instance().resetStats());
            }
        }

        static class Crew {
            public static int importCrew(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(() -> {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.networkHandler.sendChatCommand("c info");
                    }
                });
            }

            public static int cancelCrew(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(() -> ProfileDataHandler.instance().updateImportCrew(true));
            }
        }

        static class Reset {
            public static int resetButton(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Reset buttons to default config").formatted(Formatting.GREEN), () -> CustomButtonDataHandler.instance().resetButtons());
            }

            public static int resetChatTrigger(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Reset chat triggers to default config").formatted(Formatting.GREEN), () -> {
                    CustomChatTriggerDataHandler.instance().resetChatTriggers();
                    ChatHandler.instance().initChatTrigger();
                });
            }

            public static int resetNotification(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Reset notifications to default config").formatted(Formatting.GREEN), () -> CustomNotificationDataHandler.instance().resetNotifications());
            }

            public static int resetTimer(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Reset timers to default config").formatted(Formatting.GREEN), () -> {
                    CustomTimerDataHandler.instance().resetTimers();
                    TimerHandler.instance().initTimers();
                });
            }

            public static int resetHud(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Reset HUDs to default config").formatted(Formatting.GREEN), () -> CustomHudDataHandler.instance().resetHuds());
            }

            public static int resetChatNotification(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Reset chat notifications to default config")
                        .formatted(Formatting.GREEN), () -> CustomChatNotificationDataHandler.instance().resetChatNotifications());
            }
        }

        static class Toggle {
            public static int toggleArmor(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Toggled Armor"), () -> {
                    Configs.rendererConfig.hideArmor.accept(!Configs.rendererConfig.hideArmor.get());
                    Configs.rendererConfig.save();
                });
            }

            public static int togglePetNames(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Toggled Pet Names"), () -> {
                    Configs.rendererConfig.showPetName.accept(!Configs.rendererConfig.showPetName.get());
                    Configs.rendererConfig.save();
                });
            }

            public static int toggleBobberModel(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Toggled Bobber Model"), () -> {
                    Configs.rendererConfig.showNewBobber.accept(!Configs.rendererConfig.showNewBobber.get());
                    Configs.rendererConfig.save();
                });
            }

            public static int toggleBaitOnBobber(CommandContext<FabricClientCommandSource> context) {
                return executeCommand(context, Text.literal("Toggled Bait on Bobber"), () -> {
                    Configs.rendererConfig.showBaitOnBobber.accept(!Configs.rendererConfig.showBaitOnBobber.get());
                    Configs.rendererConfig.save();
                });
            }
        }
    }

    //region Command Builder
    private static LiteralArgumentBuilder<FabricClientCommandSource> command(String command) {
        return ClientCommandManager.literal(command);
    }

    private static int executeCommand(CommandContext<FabricClientCommandSource> context, List<Text> feedback, ExecuteCallback executeCallback) {
        return executeCommand(context, TextHelper.concat(feedback.toArray(new Text[]{})), executeCallback);
    }

    private static int executeCommand(CommandContext<FabricClientCommandSource> context, String feedback, ExecuteCallback executeCallback) {
        return executeCommand(context, Text.literal(feedback), executeCallback);
    }

    private static int executeCommand(ExecuteCallback executeCallback) {
        MinecraftClient.getInstance().send(executeCallback::execute);
        return 1;
    }

    private static int executeCommand(CommandContext<FabricClientCommandSource> context, Text feedback, ExecuteCallback executeCallback) {
        MinecraftClient.getInstance().send(executeCallback::execute);
        return sendFeedback(context, feedback);
    }

    private static int sendFeedback(CommandContext<FabricClientCommandSource> context, Text feedback) {
        context.getSource().sendFeedback(
                TextHelper.concat(
                        Text.literal("FoER ").formatted(Formatting.DARK_GREEN, Formatting.BOLD),
                        Text.literal("» ").formatted(Formatting.DARK_GRAY),
                        feedback
                )
        );
        return 1;
    }

    private interface ExecuteCallback {
        void execute();
    }
    //endregion
}
