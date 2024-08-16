package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ColorChatCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher
                .register(literal("cc")
                        .then(argument("color", string())
                                .then(argument("message", greedyString())
                                        .executes(ctx -> sendColoredMessage(ctx.getSource(), getString(ctx, "color"), getString(ctx, "message")))
                                )
                        )
                );
    }

    private static int sendColoredMessage(FabricClientCommandSource source, String color, String message) {
        // check if color is gelb oder grün
        if (!color.equals("ge") &&
                !color.equals("gr") &&
                !color.equals("bl") &&
                !color.equals("ros") &&
                !color.equals("rot")

        ) {
            ClientCommandHelper.sendFeedback("allowed colors: ge(lb), gr(ün), bl(au), ros(a), rot");
            return Command.SINGLE_SUCCESS;
        }

        // gelb is default
        String startColorHex = "#FFE259";
        String endColorHex = "#FFA751";

        if (color.equals("gr")) {
            startColorHex = "#A4EF18";
            endColorHex = "#318518";
        } else if (color.equals("bl")) {
            startColorHex = "#3C5ED6";
            endColorHex = "#51B2E8";
        } else if (color.equals("ros")) {
            startColorHex = "#F4C4F3";
            endColorHex = "#FC67FA";
        } else if (color.equals("rot")) {
            startColorHex = "#FE7200";
            endColorHex = "#B30000";
        }

        Color startColor = Color.decode(startColorHex);
        Color endColor = Color.decode(endColorHex);

        // create a list of colors to use in the gradient function
        ArrayList<Color> colors = new ArrayList<>(Arrays.asList(startColor, endColor));

        String gradientMessage = gradient(colors, message);

        if (gradientMessage.length() > 255) {
            ClientCommandHelper.sendFeedback("message to long");
            return Command.SINGLE_SUCCESS;
        }

        ClientPacketListener packetListener = Minecraft.getInstance().getConnection();
        if (packetListener != null) {
            packetListener.sendChat(gradientMessage);
        }

        return Command.SINGLE_SUCCESS;
    }


    public static String gradient(ArrayList<Color> colors, String message) {
        if (colors.size() < 2)
            throw new IllegalArgumentException("Must provide at least 2 colors");

        ArrayList<TwoStopGradient> gradients = new ArrayList<>();
        int steps = message.length();
        float increment = (float) (steps - 1) / (colors.size() - 1);

        for (int i = 0; i < colors.size() - 1; i++) {
            gradients.add(
                    new TwoStopGradient(
                            colors.get(i),
                            colors.get(i + 1),
                            increment * i,
                            increment * (i + 1)
                    )
            );
        }

        String gradientMessage = "";

        for (int i = 0; i < steps; i++) {
            for (TwoStopGradient gradient : gradients) {
                Color currentColor = gradient.colorAt(i);
                String hexRed = Integer.toHexString(currentColor.getRed());
                String hexGreen = Integer.toHexString(currentColor.getGreen());
                String hexBlue = Integer.toHexString(currentColor.getBlue());

                // check if string contains two characters, else prepend a 0
                if (hexRed.length() == 1) {
                    hexRed = "0" + hexRed;
                }
                if (hexGreen.length() == 1) {
                    hexGreen = "0" + hexGreen;
                }
                if (hexBlue.length() == 1) {
                    hexBlue = "0" + hexBlue;
                }

                String hexColor = hexRed + hexGreen + hexBlue;

                // add a & between each char of the string
                hexColor = hexColor.replaceAll(".", "&$0");

                gradientMessage += "&x" + hexColor + message.charAt(i);
            }
        }

        return gradientMessage;
    }
}

class TwoStopGradient {

    private final Color startColor;
    private final Color endColor;
    private final float lowerRange;
    private final float upperRange;

    TwoStopGradient(Color startColor, Color endColor, float lowerRange, float upperRange) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
    }

    /**
     * Gets the color of this gradient at the given step
     *
     * @param step The step
     * @return The color of this gradient at the given step
     */
    public Color colorAt(int step) {
        return new Color(
                this.calculateHexPiece(step, this.startColor.getRed(), this.endColor.getRed()),
                this.calculateHexPiece(step, this.startColor.getGreen(), this.endColor.getGreen()),
                this.calculateHexPiece(step, this.startColor.getBlue(), this.endColor.getBlue())
        );
    }

    private int calculateHexPiece(int step, int channelStart, int channelEnd) {
        float range = this.upperRange - this.lowerRange;
        if (range == 0) // No range, don't divide by 0
            return channelStart;

        float interval = (channelEnd - channelStart) / range;

        return Math.min(Math.max(Math.round(interval * (step - this.lowerRange) + channelStart), 0), 255);
    }
}

