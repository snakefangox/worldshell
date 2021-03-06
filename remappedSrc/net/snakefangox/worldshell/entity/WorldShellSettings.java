package net.snakefangox.worldshell.entity;

/**
 * Contains the settings for a WorldShell entity, this includes things like whether explosions can effect it,
 * if players can interact with it and most importantly whether it is a simple or complex worldshell entity.<p>
 * Calling the default constructor will give you a standard complex or simple worldshell entity.
 * For custom settings use the builder to avoid incompatible settings.<p>
 * There are Javadocs on each setting to explain it.<p>
 * If you want something really custom and know what you are doing, subclass this.
 * That's why the getter methods get extra data.
 */
public class WorldShellSettings {

	/***
	 * A complex worldshell takes up a slot in the worldshell world and is simulated.
	 * Players can interact with it and it can interact with the world.
	 * Furnaces will run and hoppers will hop.
	 * You probably want this if you're making a vehicle mod.<p>
	 * Simple worldshell's simply render and pass events along to some special blocks.
	 * They don't do much else. You probably want this if you're making machines out of these.
	 */
	private final boolean isComplex;

	public WorldShellSettings(boolean isComplex) {
		this.isComplex = isComplex;
	}

	public static class Builder {

	}
}
