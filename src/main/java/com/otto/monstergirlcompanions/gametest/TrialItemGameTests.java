package com.otto.monstergirlcompanions.gametest;

import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import com.otto.monstergirlcompanions.entity.companion.ZombieGirlCompanion;
import com.otto.monstergirlcompanions.entity.monstergirl.ZombieGirl;
import com.otto.monstergirlcompanions.registry.ModBlocks;
import com.otto.monstergirlcompanions.registry.ModEntities;
import com.otto.monstergirlcompanions.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.List;

/** Run with {@code ./gradlew runGameTestServer}. */
@GameTestHolder(MonsterGirlCompanions.MOD_ID)
public class TrialItemGameTests {
    private static final String EMPTY = "forge:empty3x3x3";
    private static final BlockPos CENTER = new BlockPos(1, 1, 1);

    private static InteractionResult useOn(GameTestHelper helper, Player player, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        return player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    @GameTest(template = EMPTY)
    public static void keyOnTrialCoreStartsTrial(GameTestHelper helper) {
        helper.setBlock(CENTER, ModBlocks.TRIAL_CORE.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.INFECTION_KEY.get()));

        InteractionResult result = useOn(helper, player, CENTER);

        helper.assertTrue(result.consumesAction(), "using the key on a trial core should succeed");
        helper.assertEntityPresent(ModEntities.ZOMBIE_GIRL.get(), CENTER.above(), 1.0);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the key should be used up");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void keyOnOtherBlockDoesNothing(GameTestHelper helper) {
        helper.setBlock(CENTER, Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.INFECTION_KEY.get()));

        InteractionResult result = useOn(helper, player, CENTER);

        helper.assertTrue(result == InteractionResult.PASS, "a key should only work on a trial core");
        helper.assertTrue(helper.getEntities(ModEntities.ZOMBIE_GIRL.get()).isEmpty(), "no zombie girl should appear");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "the key should be kept");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bellSummonsOwnedCompanion(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(CENTER));
        player.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.INFECTION_BELL.get()));

        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        List<ZombieGirlCompanion> companions = helper.getLevel().getEntitiesOfClass(ZombieGirlCompanion.class,
                player.getBoundingBox().inflate(4.0));
        helper.assertTrue(companions.size() == 1, "one companion should appear, found " + companions.size());
        // The mock player is not in the level, so compare UUIDs rather than resolving the owner.
        helper.assertTrue(player.getUUID().equals(companions.get(0).getOwnerUUID()), "the companion should belong to the bell's user");
        helper.assertTrue(companions.get(0).distanceTo(player) < 3.5F, "the companion should appear in front of the player");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the bell should be used up");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void defeatedZombieGirlDropsBell(GameTestHelper helper) {
        ZombieGirl girl = helper.spawn(ModEntities.ZOMBIE_GIRL.get(), CENTER);
        girl.kill();
        helper.assertItemEntityPresent(ModItems.INFECTION_BELL.get(), CENTER, 3.0);
        helper.succeed();
    }
}
