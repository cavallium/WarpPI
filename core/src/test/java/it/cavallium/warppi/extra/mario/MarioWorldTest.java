package it.cavallium.warppi.extra.mario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class MarioWorldTest {

	@Test
	void campaignContainsThreeDistinctCompactAuthoredCourses() {
		final MarioWorld[] levels = MarioWorld.createLevels();
		final Set<String> names = new HashSet<>();
		final Set<Integer> backgrounds = new HashSet<>();
		int storageBytes = 0;

		assertEquals(3, levels.length);
		for (final MarioWorld world : levels) {
			names.add(world.getName());
			backgrounds.add(world.getBackgroundIndex());
			storageBytes += world.storageBytes();
			assertEquals(18, world.getHeight());
			assertTrue(world.getWidth() >= 100);
			assertTrue(world.getGoalX() > world.getSpawnPointX() + 90d);
			assertTrue(world.getCheckpointCount() >= 2);
			assertTrue(world.getEnemies().length >= 4);
			assertContainsEveryGameplayBlock(world);
		}
		assertEquals(3, names.size());
		assertEquals(3, backgrounds.size());
		assertTrue(storageBytes < 16 * 1_024, "level bytes=" + storageBytes);
	}

	@Test
	void boundariesPitsAndTheLowPassageHaveCorrectCollisionGeometry() {
		final MarioWorld world = MarioWorld.createDefault();

		assertTrue(world.isSolidAt(-1, 2));
		assertTrue(world.isSolidAt(world.getWidth(), 2));
		assertTrue(world.isSolidAt(5, 0));
		assertFalse(world.isSolidAt(5, -1));
		assertFalse(world.isSolidAt(29, 0));
		assertTrue(world.isSolidAt(59, 3));
		assertFalse(world.isSolidAt(59, 2));
		assertTrue(world.isSolidAt(59, 1));
	}

	@Test
	void blocksChangeStateAndResetWithoutAllocatingAnotherMap() {
		final MarioWorld world = MarioWorld.createDefault();
		final MarioEnemy enemy = world.getEnemies()[0];

		assertEquals(MarioWorld.HitResult.COIN, world.hitBlockAt(19, 5, false));
		assertEquals(MarioWorld.BLOCK_USED, world.getBlockIdAt(19, 5));
		assertEquals(MarioWorld.HitResult.MUSHROOM, world.hitBlockAt(40, 6, false));
		assertEquals(MarioWorld.BLOCK_USED, world.getBlockIdAt(40, 6));
		assertEquals(MarioWorld.HitResult.BUMP, world.hitBlockAt(17, 5, false));
		assertEquals(MarioWorld.HitResult.BREAK, world.hitBlockAt(17, 5, true));
		assertEquals(MarioWorld.BLOCK_EMPTY, world.getBlockIdAt(17, 5));
		assertTrue(world.collectCoinAt(4, 3));
		enemy.deactivate();

		world.reset();

		assertEquals(MarioWorld.BLOCK_QUESTION_COIN, world.getBlockIdAt(19, 5));
		assertEquals(MarioWorld.BLOCK_QUESTION_MUSHROOM, world.getBlockIdAt(40, 6));
		assertEquals(MarioWorld.BLOCK_BRICK, world.getBlockIdAt(17, 5));
		assertEquals(MarioWorld.BLOCK_COIN, world.getBlockIdAt(4, 3));
		assertTrue(enemy.isActive());
	}

	private static void assertContainsEveryGameplayBlock(final MarioWorld world) {
		final boolean[] found = new boolean[MarioWorld.BLOCK_PIPE + 1];
		for (int y = 0; y < world.getHeight(); y++)
			for (int x = 0; x < world.getWidth(); x++)
				found[world.getBlockIdAt(x, y)] = true;
		for (byte id = MarioWorld.BLOCK_GROUND; id <= MarioWorld.BLOCK_PIPE; id++) {
			if (id != MarioWorld.BLOCK_USED)
				assertTrue(found[id], world.getName() + " lacks block " + id);
		}
	}
}
