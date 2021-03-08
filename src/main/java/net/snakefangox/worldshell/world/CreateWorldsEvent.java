package net.snakefangox.worldshell.world;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;

/**
 * Called after all static worlds have been created on the server but before
 * anything else happens. Use to create and recreate dynamic worlds on server start.
 */
public interface CreateWorldsEvent {

	Event<CreateWorldsEvent> EVENT = EventFactory.createArrayBacked(CreateWorldsEvent.class, (listeners) -> (server) -> {
		for (CreateWorldsEvent listener : listeners) {
			listener.event(server);
		}
	});

	void event(MinecraftServer server);
}
