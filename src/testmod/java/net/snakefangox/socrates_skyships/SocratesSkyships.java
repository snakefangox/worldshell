package net.snakefangox.socrates_skyships;

import net.fabricmc.api.ModInitializer;
import net.snakefangox.rapidregister.RapidRegister;

public class SocratesSkyships implements ModInitializer {

	public static final String MODID = "socrates_skyships";

	@Override
	public void onInitialize() {
		RapidRegister.register(MODID, SRegister.class);
	}
}
