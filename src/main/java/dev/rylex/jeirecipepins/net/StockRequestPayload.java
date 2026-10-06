package dev.rylex.jeirecipepins.net;

import dev.rylex.jeirecipepins.JeiRecipePins;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;

public record StockRequestPayload(List<Item> wanted) implements CustomPacketPayload {
    public static final int MAX_ITEMS = 1024;
    public static final Type<StockRequestPayload> TYPE = new Type<>(JeiRecipePins.id("stock_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StockRequestPayload> STREAM_CODEC = ByteBufCodecs.registry(
                    Registries.ITEM)
            .apply(ByteBufCodecs.list(MAX_ITEMS))
            .map(StockRequestPayload::new, StockRequestPayload::wanted);

    @Override
    public Type<StockRequestPayload> type() {
        return TYPE;
    }
}
