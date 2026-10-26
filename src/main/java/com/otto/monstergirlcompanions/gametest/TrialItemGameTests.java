package com.otto.monstergirlcompanions.gametest;

import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import com.otto.monstergirlcompanions.block.TrialCoreBlockEntity;
import com.otto.monstergirlcompanions.entity.companion.ZombieGirlCompanion;
import com.otto.monstergirlcompanions.entity.monstergirl.ZombieGirl;
import com.otto.monstergirlcompanions.registry.ModEntities;
import com.otto.monstergirlcompanions.registry.ModItems;
import com.otto.monstergirlcompanions.trial.TrialStructure;
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
    private static final String TRIAL_AREA = "monstergirlcompanions:trial_area";
    private static final BlockPos CENTER = new BlockPos(1, 1, 1);
    /** Trial core position inside the 7x8x7 trial area, leaving room for the altar below it. */
    private static final BlockPos CORE = new BlockPos(3, 3, 3);
    private static final int CHARGE_DONE = TrialCoreBlockEntity.CHARGE_TICKS + 5;

    private static Player playerWithKey(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.INFECTION_KEY.get()));
        return player;
    }

    private static InteractionResult useKeyOn(GameTestHelper helper, Player player, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        return player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private static TrialCoreBlockEntity core(GameTestHelper helper, BlockPos relative) {
        return helper.getBlockEntity(relative);
    }

    @GameTest(template = TRIAL_AREA, timeoutTicks = 200)
    public static void keyOnAltarChargesThenSpawnsZombieGirl(GameTestHelper helper) {
        TrialStructure.build(helper.getLevel(), helper.absolutePos(CORE));
        Player player = playerWithKey(helper);

        helper.assertTrue(useKeyOn(helper, player, CORE).consumesAction(), "the key should start the trial");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the key should be used up");
        helper.assertTrue(core(helper, CORE).phase() == TrialCoreBlockEntity.Phase.CHARGING, "the trial should be charging");
        helper.assertTrue(helper.getEntities(ModEntities.ZOMBIE_GIRL.get()).isEmpty(), "she should not appear before the bar fills");

        helper.startSequence()
                .thenExecuteAfter(CHARGE_DONE, () -> {
                    helper.assertEntityPresent(ModEntities.ZOMBIE_GIRL.get(), CORE.above(), 1.5);
                    helper.assertTrue(core(helper, CORE).phase() == TrialCoreBlockEntity.Phase.ACTIVE, "the trial should be active");
                })
                .thenSucceed();
    }

    @GameTest(template = TRIAL_AREA, timeoutTicks = 200)
    public static void trialEndsWhenZombieGirlDies(GameTestHelper helper) {
        TrialStructure.build(helper.getLevel(), helper.absolutePos(CORE));
        useKeyOn(helper, playerWithKey(helper), CORE);

        helper.startSequence()
                .thenExecuteAfter(CHARGE_DONE, () -> helper.getEntities(ModEntities.ZOMBIE_GIRL.get()).forEach(ZombieGirl::kill))
                .thenExecuteAfter(2, () -> helper.assertTrue(core(helper, CORE).phase() == TrialCoreBlockEntity.Phase.IDLE,
                        "the trial should end with her"))
                .thenSucceed();
    }

    @GameTest(template = TRIAL_AREA)
    public static void busyCoreKeepsSecondKey(GameTestHelper helper) {
        TrialStructure.build(helper.getLevel(), helper.absolutePos(CORE));
        useKeyOn(helper, playerWithKey(helper), CORE);
        Player second = playerWithKey(helper);

        helper.assertTrue(useKeyOn(helper, second, CORE) == InteractionResult.FAIL, "a running trial should refuse another key");
        helper.assertTrue(second.getMainHandItem().getCount() == 1, "the second key should be kept");
        helper.succeed();
    }

    @GameTest(template = TRIAL_AREA)
    public static void incompleteAltarKeepsKey(GameTestHelper helper) {
        TrialStructure.build(helper.getLevel(), helper.absolutePos(CORE));
        helper.setBlock(CORE.offset(1, 1, 1), Blocks.AIR); // one redstone torch missing
        Player player = playerWithKey(helper);

        helper.assertTrue(useKeyOn(helper, player, CORE) == InteractionResult.FAIL, "an incomplete altar should refuse the key");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "the key should be kept");
        helper.assertTrue(core(helper, CORE).phase() == TrialCoreBlockEntity.Phase.IDLE, "no trial should start");
        helper.succeed();
    }

    @GameTest(template = TRIAL_AREA)
    public static void altarAboveY40KeepsKey(GameTestHelper helper) {
        // Test structures sit near the bottom of the world; build this altar high enough to poke above Y=40.
        BlockPos high = CORE.above(TrialStructure.MAX_Y - helper.absolutePos(CORE).getY());
        TrialStructure.build(helper.getLevel(), helper.absolutePos(high));
        Player player = playerWithKey(helper);

        helper.assertTrue(useKeyOn(helper, player, high) == InteractionResult.FAIL, "an altar reaching above Y=40 should refuse the key");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "the key should be kept");
        TrialStructure.clear(helper.getLevel(), helper.absolutePos(high));
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void keyOnOtherBlockDoesNothing(GameTestHelper helper) {
        helper.setBlock(CENTER, Blocks.STONE);
        Player player = playerWithKey(helper);

        helper.assertTrue(useKeyOn(helper, player, CENTER) == InteractionResult.PASS, "a key should only work on a trial core");
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
