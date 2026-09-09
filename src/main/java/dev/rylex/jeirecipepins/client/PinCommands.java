package dev.rylex.jeirecipepins.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rylex.jeirecipepins.pin.PinBoard;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

final class PinCommands {
    private PinCommands() {}

    static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher()
                .register(Commands.literal("jeirecipepins")
                        .then(Commands.literal("edit").executes(context -> run(context, PinCommands::openEditor)))
                        .then(Commands.literal("toggle")
                                .executes(context -> run(context, board -> {
                                    board.toggleVisible();
                                    feedback(
                                            context,
                                            board.isVisible()
                                                    ? "jeirecipepins.command.shown"
                                                    : "jeirecipepins.command.hidden");
                                })))
                        .then(Commands.literal("clear")
                                .executes(context -> run(context, board -> {
                                    board.clear();
                                    feedback(context, "jeirecipepins.command.cleared");
                                })))
                        .then(Commands.literal("reset")
                                .executes(context -> run(context, board -> {
                                    board.snapAll();
                                    feedback(context, "jeirecipepins.command.reset");
                                }))));
    }

    private static int run(CommandContext<CommandSourceStack> context, Consumer<PinBoard> action) {
        PinBoard board = PinBoard.get();
        if (!board.isReady()) {
            context.getSource().sendFailure(Component.translatable("jeirecipepins.command.no_jei"));
            return 0;
        }
        action.accept(board);
        return Command.SINGLE_SUCCESS;
    }

    private static void feedback(CommandContext<CommandSourceStack> context, String key) {
        context.getSource().sendSuccess(() -> Component.translatable(key), false);
    }

    /** The chat screen closes after the command runs, so the editor has to open on the next tick or it is replaced. */
    private static void openEditor(PinBoard board) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(new PinEditorScreen(null)));
    }
}
