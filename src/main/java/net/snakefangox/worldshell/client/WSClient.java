package net.snakefangox.worldshell.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.snakefangox.worldshell.WSNetworking;
import net.snakefangox.worldshell.WorldShell;

@Environment(EnvType.CLIENT)
public class WSClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(WorldShell.WORLD_LINK_ENTITY_TYPE, WorldLinkRenderer::new);
		WSNetworking.registerClientPackets();
	}
}
