package dev.rylex.jeirecipepins.net;

import dev.rylex.jeirecipepins.JeiRecipePins;
import dev.rylex.jeirecipepins.pin.NetworkStock;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record StockReplyPayload(NetworkStock stock) implements CustomPacketPayload {
    public static final int MAX_STACKS = 1024;
    public static final Type<StockReplyPayload> TYPE = new Type<>(JeiRecipePins.id("stock_reply"));
    private static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> STACKS =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_STACKS));
    public static final StreamCodec<RegistryFriendlyByteBuf, StockReplyPayload> STREAM_CODEC = StreamCodec.composite(
            STACKS,
            payload -> payload.stock().stored(),
            STACKS,
            payload -> payload.stock().craftable(),
            (stored, craftable) -> new StockReplyPayload(new NetworkStock(stored, craftable)));

    @Override
    public Type<StockReplyPayload> type() {
        return TYPE;
    }
}
