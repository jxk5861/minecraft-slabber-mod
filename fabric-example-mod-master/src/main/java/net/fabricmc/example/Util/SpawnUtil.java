package net.fabricmc.example.Util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.fabricmc.example.edited.ClientSpawnRestriction;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.SpawnSettings.SpawnEntry;

public class SpawnUtil {
	private static final List<SpawnEntry> NETHER_FORTRESS_SPAWNS = ImmutableList.of(new SpawnSettings.SpawnEntry(EntityType.BLAZE, 10, 2, 3), new SpawnSettings.SpawnEntry(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4), new SpawnSettings.SpawnEntry(EntityType.WITHER_SKELETON, 8, 5, 5), new SpawnSettings.SpawnEntry(EntityType.SKELETON, 2, 5, 5), new SpawnSettings.SpawnEntry(EntityType.MAGMA_CUBE, 3, 4, 4));
	private static boolean canSpawn(ClientWorld world, SpawnEntry spawnEntry, BlockPos pos) {
		EntityType<?> entityType = spawnEntry.type;
		if (entityType.getSpawnGroup() == SpawnGroup.MISC) {
			return false;
		} /*
			 * else if (!entityType.isSpawnableFarFromPlayer() && squaredDistance >
			 * (double)(entityType.getSpawnGroup().getImmediateDespawnRange() *
			 * entityType.getSpawnGroup().getImmediateDespawnRange())) { return false; }
			 */else if (entityType.isSummonable()) {
			SpawnRestriction.Location location = SpawnRestriction.getLocation(entityType);
			if (!SpawnHelper.canSpawn(location, world, pos, entityType)) {
				return false;
			} else if (!ClientSpawnRestriction.canSpawn(entityType, world, SpawnReason.NATURAL, pos, world.random)) {
	            return false;
	         } else {
	            return world.isSpaceEmpty(entityType.createSimpleBoundingBox((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D));
	         }
		} else {
			return false;
		}
	}
	
	public static boolean canSpawn(ClientWorld world, BlockPos pos) {
		SpawnGroup group = SpawnGroup.MONSTER;

//		Biome biome = world.getBiome(pos);
		Identifier key = world.getRegistryManager().get(Registry.BIOME_KEY).getId(world.getBiome(pos));
		Biome biome = BuiltinRegistries.BIOME.get(key);
		
//		System.out.println(biome.getSpawnSettings().getSpawnEntry(group));
//		System.out.println(biome2.getSpawnSettings().getSpawnEntry(group));
//		System.out.println(biome + " " +  biome2);
		//net.minecraft.world.biome.Biome@252dcf1e
		//net.minecraft.world.biome.Biome@7084593f
		
		List<SpawnEntry> entries = biome.getSpawnSettings().getSpawnEntry(group);//new ArrayList<>();
//		entries.addAll(biome.getSpawnSettings().getSpawnEntry(group));
//		entries.addAll(NETHER_FORTRESS_SPAWNS);
		boolean canSpawn = false;
		for (SpawnEntry entry : entries) {
//			EntityType<?> type = entry.type;
			if (canSpawn(world, entry, pos)) {
				canSpawn = true;
				break;
			}
		}

		return canSpawn;
	}
}
