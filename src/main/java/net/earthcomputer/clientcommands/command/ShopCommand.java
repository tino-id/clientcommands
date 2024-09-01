package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ShopCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher
                .register(literal("shop")
                        .then(argument("number", string()).executes(ctx -> shopteleport(ctx.getSource(), getString(ctx, "number")))));
    }

    private static int shopteleport(FabricClientCommandSource source, String number) {
        ClientPacketListener packetListener = Minecraft.getInstance().getConnection();
        packetListener.sendCommand("as tp shop" + number);

        return Command.SINGLE_SUCCESS;
    }
}
