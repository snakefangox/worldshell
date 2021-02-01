package net.snakefangox.worldshell;

import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called after all static worlds have been created on the server but before
 * anything else happens. Use to recreate dynamic worlds on server restart
 */
public interface CreateWorldsEvent {

	Event<CreateWorldsEvent> EVENT = EventFactory.createArrayBacked(CreateWorldsEvent.class, (listeners) -> (server) -> {
		for (CreateWorldsEvent listener : listeners) {
			listener.event(server);
		}
	});

	void event(MinecraftServer server);
}
