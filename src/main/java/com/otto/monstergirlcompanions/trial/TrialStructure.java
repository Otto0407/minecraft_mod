package com.otto.monstergirlcompanions.trial;

import com.otto.monstergirlcompanions.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.BiConsumer;

/**
 * The player-built altar a trial needs, relative to its trial core:
 * <pre>
 *  y+1  torch . torch          redstone torches on the gold blocks
 *  y 0  gold core gold         core in the middle, gold blocks on the 3x3 corners
 *  y-1  3x3 amethyst blocks
 *  y-2  5x5 amethyst blocks
 * </pre>
 * The edge spots beside the core on its own layer may hold anything.
 */
public final class TrialStructure {
    /** Every block of the altar must be at or below this height. */
    public static final int MAX_Y = 40;

    public enum Problem { NONE, INCOMPLETE, TOO_HIGH }

    private TrialStructure() {}

    public static Problem check(Level level, BlockPos core) {
        boolean[] complete = {true};
        forEachBlock(core, (pos, block) -> {
            if (!level.getBlockState(pos).is(block)) {
                complete[0] = false;
            }
        });
        if (!complete[0]) {
            return Problem.INCOMPLETE;
        }
        return core.getY() + 1 > MAX_Y ? Problem.TOO_HIGH : Problem.NONE;
    }

    /** Places the whole altar around {@code core}; used by tests. Torches go last so they have support. */
    public static void build(Level level, BlockPos core) {
        forEachBlock(core, (pos, block) -> {
            if (block != Blocks.REDSTONE_TORCH) {
                level.setBlockAndUpdate(pos, block.defaultBlockState());
            }
        });
        forEachBlock(core, (pos, block) -> {
            if (block == Blocks.REDSTONE_TORCH) {
                level.setBlockAndUpdate(pos, block.defaultBlockState());
            }
        });
    }

    /** Removes the altar around {@code core}; used by tests that build outside their test area. */
    public static void clear(Level level, BlockPos core) {
        forEachBlock(core, (pos, block) -> level.removeBlock(pos, false));
    }

    private static void forEachBlock(BlockPos core, BiConsumer<BlockPos, Block> action) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                action.accept(core.offset(x, -2, z), Blocks.AMETHYST_BLOCK);
                if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    action.accept(core.offset(x, -1, z), Blocks.AMETHYST_BLOCK);
                }
            }
        }
        action.accept(core, ModBlocks.TRIAL_CORE.get());
        for (int x = -1; x <= 1; x += 2) {
            for (int z = -1; z <= 1; z += 2) {
                action.accept(core.offset(x, 0, z), Blocks.GOLD_BLOCK);
                action.accept(core.offset(x, 1, z), Blocks.REDSTONE_TORCH);
            }
        }
    }
}
