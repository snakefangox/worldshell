package net.snakefangox.worldshell.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.snakefangox.worldshell.WSNetworking;

@Environment(EnvType.CLIENT)
public class WSClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WSNetworking.registerClientPackets();
	}
}
