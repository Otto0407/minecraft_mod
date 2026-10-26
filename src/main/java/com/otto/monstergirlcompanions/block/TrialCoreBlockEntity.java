package com.otto.monstergirlcompanions.block;

import com.otto.monstergirlcompanions.entity.monstergirl.AbstractMonsterGirl;
import com.otto.monstergirlcompanions.registry.ModBlockEntities;
import com.otto.monstergirlcompanions.trial.TrialStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Runs a trial like a raid: using a key on a complete altar shows a boss bar that fills up while the trial
 * charges, then the key's monster girl appears and the bar becomes her health. The trial ends when she dies.
 */
public class TrialCoreBlockEntity extends BlockEntity {
    public static final int CHARGE_TICKS = 5 * 20;
    /** Players within this many blocks of the core see the trial's boss bar. */
    private static final double BAR_RANGE = 64.0;
    /** How long the monster girl may be missing (e.g. still loading) before the trial is dropped. */
    private static final int MISSING_GRACE_TICKS = 2 * 20;

    public enum Phase { IDLE, CHARGING, ACTIVE }

    public enum StartResult { STARTED, BUSY, INCOMPLETE, TOO_HIGH }

    private Phase phase = Phase.IDLE;
    private int ticks;
    private EntityType<?> monsterGirl;
    private UUID monsterGirlId;
    private ServerBossEvent bar;

    public TrialCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRIAL_CORE.get(), pos, state);
    }

    public Phase phase() {
        return phase;
    }

    public StartResult tryStart(EntityType<? extends AbstractMonsterGirl> type) {
        if (phase != Phase.IDLE) {
            return StartResult.BUSY;
        }
        switch (TrialStructure.check(level, worldPosition)) {
            case INCOMPLETE -> {
                return StartResult.INCOMPLETE;
            }
            case TOO_HIGH -> {
                return StartResult.TOO_HIGH;
            }
            case NONE -> { }
        }
        phase = Phase.CHARGING;
        ticks = 0;
        monsterGirl = type;
        level.playSound(null, worldPosition, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
        setChanged();
        return StartResult.STARTED;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TrialCoreBlockEntity core) {
        if (core.phase == Phase.IDLE || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        core.ticks++;
        if (core.bar == null || level.getGameTime() % 20 == 0) {
            core.updateBarPlayers(serverLevel);
        }
        if (core.phase == Phase.CHARGING) {
            core.bar().setName(Component.translatable("trial.monstergirlcompanions.charging", core.monsterGirl.getDescription()));
            core.bar().setProgress(Math.min(1.0F, core.ticks / (float) CHARGE_TICKS));
            if (core.ticks >= CHARGE_TICKS) {
                core.spawnMonsterGirl(serverLevel);
            }
        } else {
            core.trackMonsterGirl(serverLevel);
        }
    }

    private void spawnMonsterGirl(ServerLevel level) {
        Entity entity = monsterGirl.spawn(level, worldPosition.above(), MobSpawnType.EVENT);
        if (!(entity instanceof AbstractMonsterGirl girl)) {
            if (entity != null) {
                entity.discard();
            }
            end();
            return;
        }
        girl.setPersistenceRequired();
        Player target = level.getNearestPlayer(girl.getX(), girl.getY(), girl.getZ(), BAR_RANGE, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        if (target != null) {
            girl.setTarget(target);
        }
        level.playSound(null, worldPosition, SoundEvents.TRIAL_SPAWNER_SPAWN_MOB, SoundSource.HOSTILE, 1.0F, 1.0F);
        monsterGirlId = girl.getUUID();
        phase = Phase.ACTIVE;
        ticks = 0;
        setChanged();
    }

    private void trackMonsterGirl(ServerLevel level) {
        Entity entity = monsterGirlId == null ? null : level.getEntity(monsterGirlId);
        if (entity instanceof AbstractMonsterGirl girl && girl.isAlive()) {
            ticks = 0;
            bar().setName(girl.getDisplayName());
            bar().setProgress(girl.getHealth() / girl.getMaxHealth());
            return;
        }
        if (entity instanceof AbstractMonsterGirl girl && girl.isDeadOrDying()) {
            level.playSound(null, worldPosition, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.0F, 1.0F);
            end();
        } else if (ticks > MISSING_GRACE_TICKS) {
            end(); // removed without dying, e.g. by a command
        }
    }

    private void end() {
        if (bar != null) {
            bar.removeAllPlayers();
            bar = null;
        }
        phase = Phase.IDLE;
        ticks = 0;
        monsterGirl = null;
        monsterGirlId = null;
        setChanged();
    }

    private ServerBossEvent bar() {
        if (bar == null) {
            bar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        }
        return bar;
    }

    private void updateBarPlayers(ServerLevel level) {
        ServerBossEvent bar = bar();
        double rangeSqr = BAR_RANGE * BAR_RANGE;
        for (ServerPlayer player : new ArrayList<>(bar.getPlayers())) {
            if (!player.isAlive() || player.level() != level || player.distanceToSqr(worldPosition.getCenter()) > rangeSqr) {
                bar.removePlayer(player);
            }
        }
        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && player.distanceToSqr(worldPosition.getCenter()) <= rangeSqr) {
                bar.addPlayer(player);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (bar != null) {
            bar.removeAllPlayers();
            bar = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Phase", phase.name());
        tag.putInt("Ticks", ticks);
        if (monsterGirl != null) {
            tag.putString("MonsterGirl", BuiltInRegistries.ENTITY_TYPE.getKey(monsterGirl).toString());
        }
        if (monsterGirlId != null) {
            tag.putUUID("MonsterGirlId", monsterGirlId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        phase = tag.contains("Phase") ? Phase.valueOf(tag.getString("Phase")) : Phase.IDLE;
        ticks = tag.getInt("Ticks");
        monsterGirl = tag.contains("MonsterGirl")
                ? BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(tag.getString("MonsterGirl")))
                : null;
        monsterGirlId = tag.hasUUID("MonsterGirlId") ? tag.getUUID("MonsterGirlId") : null;
        if (phase == Phase.CHARGING && monsterGirl == null) {
            phase = Phase.IDLE;
        }
    }
}
