package net.snakefangox.socrates_skyships.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.socrates_skyships.BlockScan;
import net.snakefangox.socrates_skyships.SRegister;
import net.snakefangox.socrates_skyships.entities.AirShip;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.storage.ShellAwareBlock;
import net.snakefangox.worldshell.transfer.WorldShellConstructor;
import net.snakefangox.worldshell.world.Worldshell;
import org.jetbrains.annotations.Nullable;

public class ShipsHelm extends Block implements ShellAwareBlock {

    private static final BooleanProperty CONSTRUCTING = BooleanProperty.of("constructing");

    public ShipsHelm() {
        super(FabricBlockSettings.create());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        if (world instanceof Worldshell)
            return ActionResult.PASS;
        if (state.get(CONSTRUCTING))
            return ActionResult.FAIL;
        if (!(world instanceof ServerWorld))
            return ActionResult.SUCCESS;

        WorldShellConstructor<AirShip> airshipConstructor = WorldShellConstructor.create((ServerWorld) world,
                SRegister.AIRSHIP_TYPE, pos, new BlockScan(pos, world));
        world.setBlockState(pos, state.with(CONSTRUCTING, true));
        airshipConstructor.construct(result -> {
        });
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(CONSTRUCTING, false);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CONSTRUCTING);
    }

    @Override
    public void onUseInShell(World world, WorldShellEntity entity, PlayerEntity player, Hand hand, BlockHitResult hit) {
        entity.setRotation(entity.getRotation().addLocal(new Quaternion().fromAngles(Math.PI / 8.0, 0, 0)).normalizeLocal());
    }
}
