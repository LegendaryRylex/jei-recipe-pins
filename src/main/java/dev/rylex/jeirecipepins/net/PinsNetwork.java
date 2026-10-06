package dev.rylex.jeirecipepins.net;

import dev.rylex.jeirecipepins.compat.ae2.Ae2Compat;
import dev.rylex.jeirecipepins.pin.PinBoard;
import java.util.Set;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class PinsNetwork {
    private PinsNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .optional()
                .playToServer(
                        StockRequestPayload.TYPE,
                        StockRequestPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> handleRequest(payload, context)))
                .playToClient(
                        StockReplyPayload.TYPE,
                        StockReplyPayload.STREAM_CODEC,
                        (payload, context) ->
                                context.enqueueWork(() -> PinBoard.get().acceptNetworkStock(payload.stock())));
    }

    private static void handleRequest(StockRequestPayload payload, IPayloadContext context) {
        Ae2Compat.carriedStock(context.player(), Set.copyOf(payload.wanted()), StockReplyPayload.MAX_STACKS)
                .ifPresent(stock -> context.reply(new StockReplyPayload(stock)));
    }
}
