package dev.rylex.jeirecipepins;

import dev.rylex.jeirecipepins.client.JeiRecipePinsClient;
import dev.rylex.jeirecipepins.config.PinsConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(JeiRecipePins.MOD_ID)
public final class JeiRecipePins {
    public static final String MOD_ID = "jeirecipepins";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public JeiRecipePins(IEventBus modBus, ModContainer container, Dist dist) {
        container.registerConfig(ModConfig.Type.CLIENT, PinsConfig.SPEC);
        if (dist.isClient()) {
            JeiRecipePinsClient.init(container);
        }
        LOGGER.info("{} initialized", MOD_ID);
    }
}
