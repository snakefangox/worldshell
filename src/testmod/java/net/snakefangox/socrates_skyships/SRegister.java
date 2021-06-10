package net.snakefangox.socrates_skyships;

import net.minecraft.entity.EntityType;
import net.snakefangox.rapidregister.annotations.Exclude;
import net.snakefangox.rapidregister.annotations.RegisterContents;
import net.snakefangox.socrates_skyships.blocks.ShipsHelm;
import net.snakefangox.socrates_skyships.entities.AirShip;
import net.snakefangox.worldshell.entity.WorldShellEntityType;
import net.snakefangox.worldshell.entity.WorldShellSettings;
import net.snakefangox.worldshell.transfer.ConflictSolver;

@RegisterContents
public class SRegister {
	public static final ShipsHelm SHIPS_HELM = new ShipsHelm();

	public static final EntityType<AirShip> AIRSHIP_TYPE = new WorldShellEntityType<>(AirShip::new);

	@Exclude
	public static final WorldShellSettings AIRSHIP_SETTINGS = new WorldShellSettings.Builder(true).setConflictSolver(ConflictSolver.HARDNESS).build();
}
