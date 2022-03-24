package net.fabricmc.example.edited;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class ClientSpawnRestriction {
	private static final Map<EntityType<?>, ClientSpawnRestriction.Entry> RESTRICTIONS = Maps.newHashMap();

	private static <T extends MobEntity> void register(EntityType<T> type, ClientSpawnRestriction.Location location,
			Heightmap.Type heightmapType, ClientSpawnRestriction.MySpawnPredicate<T> predicate) {
		ClientSpawnRestriction.Entry entry = RESTRICTIONS.put(type,
				new ClientSpawnRestriction.Entry(heightmapType, location, predicate));
		if (entry != null) {
			throw new IllegalStateException("Duplicate registration for type " + Registry.ENTITY_TYPE.getId(type));
		}
	}

	public static ClientSpawnRestriction.Location getLocation(EntityType<?> type) {
		ClientSpawnRestriction.Entry entry = RESTRICTIONS.get(type);
		return entry == null ? ClientSpawnRestriction.Location.NO_RESTRICTIONS : entry.location;
	}

	public static Heightmap.Type getHeightmapType(@Nullable EntityType<?> type) {
		ClientSpawnRestriction.Entry entry = RESTRICTIONS.get(type);
		return entry == null ? Heightmap.Type.MOTION_BLOCKING_NO_LEAVES : entry.heightmapType;
	}

	public static <T extends Entity> boolean canSpawn(EntityType<T> type, WorldAccess serverWorldAccess,
			SpawnReason spawnReason, BlockPos pos, Random random) {
		ClientSpawnRestriction.Entry entry = RESTRICTIONS.get(type);
		return entry == null || entry.predicate.test(type, serverWorldAccess, spawnReason, pos, random);
	}

	public static boolean isSpawnDark(WorldAccess worldAccess, BlockPos pos) {
		if (worldAccess.getLightLevel(LightType.SKY, pos) >= 31) {
			return false;
		} else {
			int i = /*
					 * MinecraftClient.getInstance().worldRenderer. ? worldAccess.getLightLevel(pos,
					 * 10) :
					 */worldAccess.getLightLevel(pos);
			return i <= 7;
		}
	}

	public static boolean hostileEntityCanSpawnInDark(EntityType<? extends HostileEntity> type, WorldAccess worldAccess,
			SpawnReason spawnReason, BlockPos pos, Random random) {
		return worldAccess.getDifficulty() != Difficulty.PEACEFUL && isSpawnDark(worldAccess, pos)
				&& HostileEntity.canMobSpawn(type, worldAccess, spawnReason, pos, null);
	}

	public static boolean drownedEntityCanSpawn(EntityType<DrownedEntity> type, WorldAccess world,
			SpawnReason spawnReason, BlockPos pos, Random random) {
		Optional<RegistryKey<Biome>> optional = world.getBiomeKey(pos);
		boolean bl = world.getDifficulty() != Difficulty.PEACEFUL && isSpawnDark(world, pos)
				&& (spawnReason == SpawnReason.SPAWNER || world.getFluidState(pos).isIn(FluidTags.WATER));
		if (!Objects.equals(optional, Optional.of(BiomeKeys.RIVER))
				&& !Objects.equals(optional, Optional.of(BiomeKeys.FROZEN_RIVER))) {
			return /* random.nextInt(40) == 0 && */ pos.getY() < world.getSeaLevel() - 5 && bl;
		} else {
			return /* random.nextInt(15) == 0 && */ bl;
		}
	}
	
	public static boolean strayCanSpawn(EntityType<StrayEntity> type, WorldAccess worldAccess, SpawnReason spawnReason, BlockPos pos, Random random) {
	      return hostileEntityCanSpawnInDark(type, worldAccess, spawnReason, pos, random) && (spawnReason == SpawnReason.SPAWNER || worldAccess.isSkyVisible(pos));
   }
	
	public static boolean huskCanSpawn(EntityType<HuskEntity> type, WorldAccess worldAccess, SpawnReason spawnReason, BlockPos pos, Random random) {
		return hostileEntityCanSpawnInDark(type, worldAccess, spawnReason, pos, random) && (spawnReason == SpawnReason.SPAWNER || worldAccess.isSkyVisible(pos));
	}
	   
	
	public static boolean ghastCanSpawn(EntityType<GhastEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
	      return world.getDifficulty() != Difficulty.PEACEFUL && MobEntity.canMobSpawn(type, world, spawnReason, pos, random);
	}
	   static {
	      register(EntityType.COD, ClientSpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, FishEntity::canSpawn);
//	      register(EntityType.DOLPHIN, MySpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, DolphinEntity::canSpawn);
	      register(EntityType.DROWNED, ClientSpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::drownedEntityCanSpawn);
	      register(EntityType.GUARDIAN, ClientSpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, GuardianEntity::canSpawn);
//	      register(EntityType.PUFFERFISH, MySpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, FishEntity::canSpawn);
//	      register(EntityType.SALMON, MySpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, FishEntity::canSpawn);
//	      register(EntityType.SQUID, MySpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SquidEntity::canSpawn);
//	      register(EntityType.TROPICAL_FISH, MySpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, FishEntity::canSpawn);
//	      register(EntityType.BAT, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BatEntity::canSpawn);
	      register(EntityType.BLAZE, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnIgnoreLightLevel);
	      register(EntityType.CAVE_SPIDER, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
//	      register(EntityType.CHICKEN, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
//	      register(EntityType.COW, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.CREEPER, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
//	      register(EntityType.DONKEY, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.ENDERMAN, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
	      register(EntityType.ENDERMITE, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndermiteEntity::canSpawn);
	      register(EntityType.ENDER_DRAGON, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
	      register(EntityType.GHAST, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::ghastCanSpawn);
	      register(EntityType.GIANT, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
//	      register(EntityType.HORSE, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.HUSK, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::huskCanSpawn);
//	      register(EntityType.IRON_GOLEM, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
//	      register(EntityType.LLAMA, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.MAGMA_CUBE, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MagmaCubeEntity::canMagmaCubeSpawn);
//	      register(EntityType.MOOSHROOM, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MooshroomEntity::canSpawn);
//	      register(EntityType.MULE, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
//	      register(EntityType.OCELOT, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING, OcelotEntity::canSpawn);
//	      register(EntityType.PARROT, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING, ParrotEntity::canSpawn);
//	      register(EntityType.PIG, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.HOGLIN, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HoglinEntity::canSpawn);
	      register(EntityType.PIGLIN, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PiglinEntity::canSpawn);
	      register(EntityType.PILLAGER, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PatrolEntity::canSpawn);
//	      register(EntityType.POLAR_BEAR, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PolarBearEntity::canSpawn);
//	      register(EntityType.RABBIT, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, RabbitEntity::canSpawn);
//	      register(EntityType.SHEEP, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.SILVERFISH, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SilverfishEntity::canSpawn);
	      register(EntityType.SKELETON, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
	      register(EntityType.SKELETON_HORSE, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.SLIME, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SlimeEntity::canSpawn);
	      register(EntityType.SNOW_GOLEM, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
	      register(EntityType.SPIDER, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
	      register(EntityType.STRAY, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::strayCanSpawn);
	      register(EntityType.STRIDER, ClientSpawnRestriction.Location.IN_LAVA, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, StriderEntity::canSpawn);
//	      register(EntityType.TURTLE, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, TurtleEntity::canSpawn);
//	      register(EntityType.VILLAGER, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
	      register(EntityType.WITCH, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
	      register(EntityType.WITHER, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
	      register(EntityType.WITHER_SKELETON, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
//	      register(EntityType.WOLF, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.ZOMBIE, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
	      register(EntityType.ZOMBIE_HORSE, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.ZOMBIFIED_PIGLIN, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ZombifiedPiglinEntity::canSpawn);
	      register(EntityType.ZOMBIE_VILLAGER, ClientSpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
//	      register(EntityType.CAT, MySpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.ELDER_GUARDIAN, ClientSpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, GuardianEntity::canSpawn);
	      register(EntityType.EVOKER, ClientSpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
//	      register(EntityType.FOX, MySpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.ILLUSIONER, ClientSpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
//	      register(EntityType.PANDA, MySpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
//	      register(EntityType.PHANTOM, MySpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
	      register(EntityType.RAVAGER, ClientSpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
	      register(EntityType.SHULKER, ClientSpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
//	      register(EntityType.TRADER_LLAMA, MySpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
	      register(EntityType.VEX, ClientSpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
	      register(EntityType.VINDICATOR, ClientSpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ClientSpawnRestriction::hostileEntityCanSpawnInDark);
//	      register(EntityType.WANDERING_TRADER, MySpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
	   }

	   public static enum Location {
	      ON_GROUND,
	      IN_WATER,
	      NO_RESTRICTIONS,
	      IN_LAVA;
	   }

	   static class Entry {
	      private final Heightmap.Type heightmapType;
	      private final ClientSpawnRestriction.Location location;
	      private final ClientSpawnRestriction.MySpawnPredicate<?> predicate;

	      public Entry(Heightmap.Type heightmapType, ClientSpawnRestriction.Location location, ClientSpawnRestriction.MySpawnPredicate<?> predicate) {
	         this.heightmapType = heightmapType;
	         this.location = location;
	         this.predicate = predicate;
	      }
	   }

	   @FunctionalInterface
	   public interface MySpawnPredicate<T extends Entity> {
	      boolean test(@SuppressWarnings("rawtypes") EntityType type, WorldAccess worldAccess, SpawnReason spawnReason, BlockPos pos, Random random);
	   }
	}

