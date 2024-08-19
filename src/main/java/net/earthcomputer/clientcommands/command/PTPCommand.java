package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

import static net.earthcomputer.clientcommands.command.ClientCommandHelper.sendFeedback;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class PTPCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Collection<String> checkedPlayers = new ArrayList<String>();
    private static Collection<String> uncheckedPlayers = new ArrayList<String>();
    private static ClientPacketListener packetListener;
    private static String currentPlayer;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher
                .register(literal("ptp").executes(ctx -> run(ctx.getSource()))
                        .then(literal("clear").executes(ctx -> clear(ctx.getSource()))));
    }

    private static int clear(FabricClientCommandSource source) {
        checkedPlayers.clear();
        uncheckedPlayers.clear();
        sendFeedback("Cleared player list");

        return Command.SINGLE_SUCCESS;
    }

    private static int run(FabricClientCommandSource source) {

        packetListener = Minecraft.getInstance().getConnection();

        // check if current player is set
        if (currentPlayer == null) {
            currentPlayer = source.getClient().getUser().getName();
            LOGGER.info("Set current player to {}", currentPlayer);
        }

        if (uncheckedPlayers.isEmpty()) {
            sendFeedback("No players to check, update list");
            updatePlayerList(source);
        }

        if (uncheckedPlayers.isEmpty()) {
            sendFeedback("No players to check");

            return Command.SINGLE_SUCCESS;
        }

        checkNextPlayer(source);

        return Command.SINGLE_SUCCESS;
    }

    private static void updatePlayerList(FabricClientCommandSource source) {
        Collection<PlayerInfo> players = source.getClient().getConnection().getOnlinePlayers();

        for (PlayerInfo player : players) {
            String playerName = player.getProfile().getName();

            if (checkedPlayers.contains(playerName)) {
                //LOGGER.info("Player {} is already checked", playerName);
                continue;
            }

            if (playerName.equals(currentPlayer)) {
                //LOGGER.info("Skip myself {}", playerName);
                continue;
            }

            //LOGGER.info("Add player {} to list", playerName);
            uncheckedPlayers.add(playerName);
        }
    }

    private static void checkNextPlayer(FabricClientCommandSource source) {
        String playerName = uncheckedPlayers.iterator().next();

        uncheckedPlayers.remove(playerName);
        checkedPlayers.add(playerName);

        sendFeedback("Teleport to " + playerName);
        packetListener.sendCommand("tpo " + playerName);
    }
}
