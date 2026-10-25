package com.otto.monstergirlcompanions.entity.monstergirl;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Zombie girl: melee bruiser that keeps calling zombie reinforcements.
 * 3 seconds after she appears (the trial starting) she calls 3 zombies, then 2-5 more every 5-10 seconds.
 */
public class ZombieGirl extends AbstractMonsterGirl {
    private static final int FIRST_WAVE_DELAY = 3 * 20;
    private static final int FIRST_WAVE_SIZE = 3;
    private static final int WAVE_MIN_SIZE = 2;
    private static final int WAVE_MAX_SIZE = 5;
    private static final int WAVE_MIN_INTERVAL = 5 * 20;
    private static final int WAVE_MAX_INTERVAL = 10 * 20;
    /** Reinforcements appear at most this many blocks above or below her. */
    private static final int REINFORCEMENT_VERTICAL_RANGE = 8;
    /** Waves wait while no player is this close, so an idle zombie girl cannot fill the world with zombies. */
    private static final double PLAYER_RANGE = 32.0;

    private int reinforcementCooldown = FIRST_WAVE_DELAY;
    private boolean firstWaveCalled;

    public ZombieGirl(EntityType<? extends ZombieGirl> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes()
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (--reinforcementCooldown > 0) {
            return;
        }
        if (level().getDifficulty() == Difficulty.PEACEFUL || nearestPlayer() == null) {
            reinforcementCooldown = 20;
            return;
        }
        int count = firstWaveCalled ? Mth.nextInt(random, WAVE_MIN_SIZE, WAVE_MAX_SIZE) : FIRST_WAVE_SIZE;
        for (int i = 0; i < count; i++) {
            callReinforcement();
        }
        firstWaveCalled = true;
        reinforcementCooldown = Mth.nextInt(random, WAVE_MIN_INTERVAL, WAVE_MAX_INTERVAL);
    }

    private Player nearestPlayer() {
        return level().getNearestPlayer(getX(), getY(), getZ(), PLAYER_RANGE, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
    }

    /**
     * Spawns one zombie the way vanilla zombie reinforcements do (Zombie#hurt on Hard): up to 50 tries at a
     * random spot 7-40 blocks away horizontally, on a valid spawn block, not within 7 blocks of a player, not
     * in liquid or blocks. Unlike vanilla, the spot stays within 8 blocks of her height and darkness is not
     * required, so the skill also works in daylight.
     */
    private void callReinforcement() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Zombie zombie = EntityType.ZOMBIE.create(serverLevel);
        if (zombie == null) {
            return;
        }
        LivingEntity target = getTarget() != null ? getTarget() : nearestPlayer();
        int x = Mth.floor(getX());
        int y = Mth.floor(getY());
        int z = Mth.floor(getZ());
        for (int attempt = 0; attempt < 50; attempt++) {
            int sx = x + Mth.nextInt(random, 7, 40) * Mth.nextInt(random, -1, 1);
            int sy = y + Mth.nextInt(random, -REINFORCEMENT_VERTICAL_RANGE, REINFORCEMENT_VERTICAL_RANGE);
            int sz = z + Mth.nextInt(random, 7, 40) * Mth.nextInt(random, -1, 1);
            BlockPos pos = new BlockPos(sx, sy, sz);
            if (!SpawnPlacements.isSpawnPositionOk(EntityType.ZOMBIE, serverLevel, pos)
                    || !Mob.checkMobSpawnRules(EntityType.ZOMBIE, serverLevel, MobSpawnType.REINFORCEMENT, pos, random)) {
                continue;
            }
            zombie.setPos(sx, sy, sz);
            if (serverLevel.hasNearbyAlivePlayer(sx, sy, sz, 7.0)
                    || !serverLevel.isUnobstructed(zombie)
                    || !serverLevel.noCollision(zombie)
                    || serverLevel.containsAnyLiquid(zombie.getBoundingBox())) {
                continue;
            }
            if (target != null) {
                zombie.setTarget(target);
            }
            ForgeEventFactory.onFinalizeSpawn(zombie, serverLevel, serverLevel.getCurrentDifficultyAt(zombie.blockPosition()),
                    MobSpawnType.REINFORCEMENT, null);
            serverLevel.addFreshEntityWithPassengers(zombie);
            return;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ReinforcementCooldown", reinforcementCooldown);
        tag.putBoolean("FirstWaveCalled", firstWaveCalled);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ReinforcementCooldown")) {
            reinforcementCooldown = tag.getInt("ReinforcementCooldown");
        }
        firstWaveCalled = tag.getBoolean("FirstWaveCalled");
    }

    // Placeholder sounds until the mod has its own.
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }
}
