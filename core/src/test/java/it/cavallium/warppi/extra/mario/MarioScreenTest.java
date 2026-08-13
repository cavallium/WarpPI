package it.cavallium.warppi.extra.mario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class MarioScreenTest {

	@Test
	void renderingBoundsCullTheLevelToTheViewport() {
		assertEquals(0, MarioScreen.firstVisibleTile(0d, 120));
		assertEquals(29, MarioScreen.lastVisibleTile(0d, 480, 120));
		assertEquals(50, MarioScreen.firstVisibleTile(50d, 120));
		assertEquals(79, MarioScreen.lastVisibleTile(50d, 480, 120));
		assertEquals(50, MarioScreen.firstVisibleTile(50.25d, 120));
		assertEquals(80, MarioScreen.lastVisibleTile(50.25d, 480, 120));
		assertEquals(119, MarioScreen.lastVisibleTile(110d, 480, 120));
	}

	@Test
	void runtimeAtlasContainsOnlyTheCompactGameplayAndBackgroundSurface() throws IOException {
		final BufferedImage atlas = loadAtlas();
		assertEquals(MarioScreen.ATLAS_SIZE, atlas.getWidth());
		assertEquals(MarioScreen.ATLAS_SIZE, atlas.getHeight());
		assertTrue(atlas.getColorModel() instanceof IndexColorModel);
		assertTrue(((IndexColorModel) atlas.getColorModel()).getMapSize() <= 256);
		assertEquals(0, atlas.getRGB(511, 511) >>> 24);

		assertOpaqueRegion(atlas, 0, 0, 256, 216, "mountain background");
		assertOpaqueRegion(atlas, 256, 0, 256, 216, "forest background");
		assertOpaqueRegion(atlas, 0, 216, 256, 216, "hill background");
		assertOpaqueRegion(atlas, MarioScreen.QUESTION_X, MarioScreen.BLOCK_ROW_Y, 16, 16,
				"question block");
		assertOpaqueRegion(atlas, MarioScreen.BRICK_X, MarioScreen.BLOCK_ROW_Y, 16, 16,
				"brick");
		assertOpaqueRegion(atlas, MarioScreen.MUSHROOM_X, MarioScreen.MUSHROOM_Y, 16, 16,
				"mushroom");
		assertOpaqueRegion(atlas, MarioScreen.GOAL_POST_X, MarioScreen.GOAL_POST_Y, 16, 144,
				"goal post");
	}

	@Test
	void interactiveObjectsUseTheirIntendedSmwCells() throws IOException {
		final BufferedImage atlas = loadAtlas();
		final IntPredicate yellow = color -> (color >> 16 & 0xFF) > 200
				&& (color >> 8 & 0xFF) > 140 && (color & 0xFF) < 90;
		final IntPredicate brown = color -> (color >> 16 & 0xFF) >= 80
				&& (color >> 16 & 0xFF) <= 190 && (color >> 8 & 0xFF) < 130
				&& (color & 0xFF) < 90;
		final IntPredicate red = color -> (color >> 16 & 0xFF) > 190
				&& (color >> 8 & 0xFF) < 120 && (color & 0xFF) < 120;
		final IntPredicate white = color -> (color >> 16 & 0xFF) > 225
				&& (color >> 8 & 0xFF) > 225 && (color & 0xFF) > 225;
		final IntPredicate green = color -> (color >> 8 & 0xFF) > 100
				&& (color >> 16 & 0xFF) < 100 && (color & 0xFF) < 100;

		assertTrue(countPixels(atlas, MarioScreen.QUESTION_X, MarioScreen.BLOCK_ROW_Y,
				16, 16, yellow) > 45, "question block must be yellow, not a flower");
		assertEquals(0, countPixels(atlas, MarioScreen.QUESTION_X, MarioScreen.BLOCK_ROW_Y,
				16, 16, green));
		assertTrue(countPixels(atlas, MarioScreen.BRICK_X, MarioScreen.BLOCK_ROW_Y,
				16, 16, yellow) > 70, "breakable SMW turn block");
		assertTrue(countPixels(atlas, MarioScreen.USED_X, MarioScreen.BLOCK_ROW_Y,
				16, 16, brown) > 70, "used brown block");
		assertTrue(countPixels(atlas, MarioScreen.MUSHROOM_X, MarioScreen.MUSHROOM_Y,
				16, 16, red) > 20, "red mushroom cap");
		assertTrue(countPixels(atlas, MarioScreen.MUSHROOM_X, MarioScreen.MUSHROOM_Y,
				16, 16, white) > 20, "white mushroom body");
		assertEquals(0xFF000000, atlas.getRGB(MarioScreen.MUSHROOM_X + 5,
				MarioScreen.MUSHROOM_Y), "mushroom cap must start on its own top row");
		assertEquals(0, atlas.getRGB(MarioScreen.MUSHROOM_X,
				MarioScreen.MUSHROOM_Y) >>> 24, "mushroom top-left must stay transparent");
		assertEquals(0xFF000000, atlas.getRGB(MarioScreen.BRICK_X + 3,
				MarioScreen.BLOCK_ROW_Y), "brick top border must not contain the prior row");
		assertEquals(0xFF000000, atlas.getRGB(MarioScreen.BRICK_X + 3,
				MarioScreen.BLOCK_ROW_Y + 15), "brick bottom border must not be cropped");
		for (int row = 0; row < 2; row++)
			for (int column = 0; column < 2; column++)
				assertTrue(countPixels(atlas, MarioScreen.PIPE_X + column * 16,
						row == 0 ? MarioScreen.PIPE_TOP_Y : MarioScreen.PIPE_BODY_Y,
						16, 16, green) > 75, "green pipe cell " + column + "," + row);
		assertTrue((atlas.getRGB(MarioScreen.PIPE_X + 16,
				MarioScreen.PIPE_TOP_Y + 2) >>> 24) > 0,
				"pipe seam must skip the source-sheet gutter");
		assertTrue((atlas.getRGB(MarioScreen.PIPE_X + 8,
				MarioScreen.PIPE_BODY_Y) >>> 24) > 0,
				"pipe body must start after the horizontal source-sheet gutter");
		assertEquals(0, atlas.getRGB(MarioScreen.PIPE_X + 31,
				MarioScreen.PIPE_BODY_Y + 8) >>> 24,
				"pipe right silhouette must retain its outside transparent column");
	}

	@Test
	void effectsAndGoalTapeHaveTransparentBackgroundsAndNoMonochromeFourthStar()
			throws IOException {
		final BufferedImage atlas = loadAtlas();
		for (int frame = 0; frame < 3; frame++) {
			assertEquals(0, atlas.getRGB(MarioScreen.SPARKLE_X + frame * 8,
					MarioScreen.SPARKLE_Y) >>> 24);
			assertOpaqueRegion(atlas, MarioScreen.SPARKLE_X + frame * 8,
					MarioScreen.SPARKLE_Y, 8, 8, "sparkle " + frame);
		}
		assertFullyTransparent(atlas, MarioScreen.SPARKLE_X + 24,
				MarioScreen.SPARKLE_Y, 8, 8, "removed monochrome sparkle");
		assertEquals(0, atlas.getRGB(MarioScreen.GOAL_TAPE_X,
				MarioScreen.GOAL_TAPE_Y) >>> 24);
		assertOpaqueRegion(atlas, MarioScreen.GOAL_TAPE_X, MarioScreen.GOAL_TAPE_Y,
				24, 8, "goal tape");
	}

	@Test
	void marioAndGaloombaDirectionsAreExplicitExactMirrorFrames() throws IOException {
		final BufferedImage atlas = loadAtlas();
		for (int frame = 0; frame < MarioScreen.MARIO_FRAME_COUNT; frame++) {
			assertMirrored(atlas,
					MarioScreen.SMALL_MARIO_X + frame * MarioScreen.SMALL_MARIO_SIZE,
					MarioScreen.SMALL_MARIO_RIGHT_Y,
					MarioScreen.SMALL_MARIO_X + frame * MarioScreen.SMALL_MARIO_SIZE,
					MarioScreen.SMALL_MARIO_LEFT_Y,
					MarioScreen.SMALL_MARIO_SIZE, MarioScreen.SMALL_MARIO_SIZE);
			assertMirrored(atlas,
					MarioScreen.SUPER_MARIO_X + frame * MarioScreen.SUPER_MARIO_SIZE,
					MarioScreen.SUPER_MARIO_RIGHT_Y,
					MarioScreen.SUPER_MARIO_X + frame * MarioScreen.SUPER_MARIO_SIZE,
					MarioScreen.SUPER_MARIO_LEFT_Y,
					MarioScreen.SUPER_MARIO_SIZE, MarioScreen.SUPER_MARIO_SIZE);
		}
		for (int frame = 0; frame < 2; frame++)
			assertMirrored(atlas,
					MarioScreen.GOOMBA_LEFT_X + frame * 16, MarioScreen.GOOMBA_Y,
					MarioScreen.GOOMBA_RIGHT_X + frame * 16, MarioScreen.GOOMBA_Y, 16, 16);

		assertEquals(0xFF885818, atlas.getRGB(
				MarioScreen.SMALL_MARIO_X + 18, MarioScreen.SMALL_MARIO_RIGHT_Y + 11),
				"the right-facing nose must be on the right");
		assertEquals(0xFF885818, atlas.getRGB(
				MarioScreen.SMALL_MARIO_X + 5, MarioScreen.SMALL_MARIO_LEFT_Y + 11),
				"the left-facing nose must be on the left");
		assertEquals(0xFF885818, atlas.getRGB(
				MarioScreen.SMALL_MARIO_X + 8 * MarioScreen.SMALL_MARIO_SIZE + 19,
				MarioScreen.SMALL_MARIO_RIGHT_Y + 11),
				"the third small running pose must still face right");
		assertEquals(0xFF885818, atlas.getRGB(
				MarioScreen.SMALL_MARIO_X + 8 * MarioScreen.SMALL_MARIO_SIZE + 4,
				MarioScreen.SMALL_MARIO_LEFT_Y + 11),
				"the mirrored third small running pose must still face left");
		assertEquals(0xFF885818, atlas.getRGB(
				MarioScreen.SUPER_MARIO_X + 8 * MarioScreen.SUPER_MARIO_SIZE + 22,
				MarioScreen.SUPER_MARIO_RIGHT_Y + 12),
				"the third super running pose must still face right");
		assertEquals(0xFF885818, atlas.getRGB(
				MarioScreen.SUPER_MARIO_X + 8 * MarioScreen.SUPER_MARIO_SIZE + 9,
				MarioScreen.SUPER_MARIO_LEFT_Y + 12),
				"the mirrored third super running pose must still face left");
		assertNotEquals(regionHash(atlas,
				MarioScreen.SMALL_MARIO_X + 4 * MarioScreen.SMALL_MARIO_SIZE,
				MarioScreen.SMALL_MARIO_RIGHT_Y,
				MarioScreen.SMALL_MARIO_SIZE, MarioScreen.SMALL_MARIO_SIZE),
				regionHash(atlas,
						MarioScreen.SMALL_MARIO_X + 8 * MarioScreen.SMALL_MARIO_SIZE,
						MarioScreen.SMALL_MARIO_RIGHT_Y,
						MarioScreen.SMALL_MARIO_SIZE, MarioScreen.SMALL_MARIO_SIZE),
				"the third running pose must be a distinct sprite");
	}

	@Test
	void allNineTerrainEdgesAndCornersArePresentAndSelected() throws IOException {
		final BufferedImage atlas = loadAtlas();
		final Set<Integer> hashes = new HashSet<>();
		for (int variant = 0; variant < 9; variant++) {
			assertOpaqueRegion(atlas, MarioScreen.TERRAIN_X + variant * 16,
					MarioScreen.TERRAIN_Y, 16, 16, "terrain " + variant);
			hashes.add(regionHash(atlas, MarioScreen.TERRAIN_X + variant * 16,
					MarioScreen.TERRAIN_Y, 16, 16));
		}
		assertEquals(9, hashes.size());

		final MarioWorld world = MarioWorld.createDefault();
		assertEquals(0, MarioScreen.terrainVariant(world, 0, 1));
		assertEquals(1, MarioScreen.terrainVariant(world, 5, 1));
		assertEquals(2, MarioScreen.terrainVariant(world, 103, 1));
		assertEquals(3, MarioScreen.terrainVariant(world, 32, 2));
		assertEquals(4, MarioScreen.terrainVariant(world, 33, 2));
		assertEquals(5, MarioScreen.terrainVariant(world, 35, 2));
		assertEquals(6, MarioScreen.terrainVariant(world, 0, 0));
		assertEquals(7, MarioScreen.terrainVariant(world, 5, 0));
		assertEquals(8, MarioScreen.terrainVariant(world, 103, 0));

		final Set<Integer> reflexHashes = new HashSet<>();
		for (int reflex = 0; reflex < 4; reflex++) {
			assertOpaqueRegion(atlas, MarioScreen.REFLEX_X + reflex * 16,
					MarioScreen.REFLEX_Y, 16, 16, "reflex corner " + reflex);
			reflexHashes.add(regionHash(atlas, MarioScreen.REFLEX_X + reflex * 16,
					MarioScreen.REFLEX_Y, 16, 16));
		}
		assertEquals(4, reflexHashes.size());

		final byte[] cross = new byte[3 * 3];
		cross[1 + 1 * 3] = MarioWorld.BLOCK_GROUND;
		cross[0 + 1 * 3] = MarioWorld.BLOCK_GROUND;
		cross[2 + 1 * 3] = MarioWorld.BLOCK_GROUND;
		cross[1] = MarioWorld.BLOCK_GROUND;
		cross[1 + 2 * 3] = MarioWorld.BLOCK_GROUND;
		final MarioWorld reflexWorld = new MarioWorld(1, 1, 3, 3, cross,
				new MarioEnemy[0], 2d);
		assertEquals(15, MarioScreen.terrainReflexMask(reflexWorld, 1, 1));
	}

	@Test
	void cameraUsesANonElasticDeadZone() {
		assertEquals(10d, MarioScreen.deadZoneCamera(10d, 25d,
				30d, 100d, 0.32d, 0.58d), 0d);
		assertEquals(12.6d, MarioScreen.deadZoneCamera(10d, 30d,
				30d, 100d, 0.32d, 0.58d), 0.0001d);
		assertEquals(8.4d, MarioScreen.deadZoneCamera(12.6d, 18d,
				30d, 100d, 0.32d, 0.58d), 0.0001d);
		assertEquals(0d, MarioScreen.deadZoneCamera(0d, 2d,
				30d, 20d, 0.32d, 0.58d), 0d);
	}

	@Test
	void everyMarioFrameTouchesItsCellBaseline() throws IOException {
		final BufferedImage atlas = loadAtlas();
		for (int frame = 0; frame < MarioScreen.MARIO_FRAME_COUNT; frame++) {
			assertTrue(hasOpaquePixelOnRow(atlas,
					MarioScreen.SMALL_MARIO_X + frame * MarioScreen.SMALL_MARIO_SIZE,
					MarioScreen.SMALL_MARIO_SIZE,
					MarioScreen.SMALL_MARIO_RIGHT_Y + MarioScreen.SMALL_MARIO_SIZE - 1));
			assertTrue(hasOpaquePixelOnRow(atlas,
					MarioScreen.SUPER_MARIO_X + frame * MarioScreen.SUPER_MARIO_SIZE,
					MarioScreen.SUPER_MARIO_SIZE,
					MarioScreen.SUPER_MARIO_RIGHT_Y + MarioScreen.SUPER_MARIO_SIZE - 1));
		}
	}

	private static BufferedImage loadAtlas() throws IOException {
		try (InputStream input = MarioScreen.class.getResourceAsStream("/mario-atlas.png")) {
			assertNotNull(input);
			final BufferedImage atlas = ImageIO.read(input);
			assertNotNull(atlas);
			return atlas;
		}
	}

	private static void assertOpaqueRegion(final BufferedImage image, final int startX,
			final int startY, final int width, final int height, final String description) {
		int opaque = 0;
		for (int y = startY; y < startY + height; y++)
			for (int x = startX; x < startX + width; x++)
				if ((image.getRGB(x, y) >>> 24) > 0)
					opaque++;
		assertNotEquals(0, opaque, description);
	}

	private static void assertFullyTransparent(final BufferedImage image, final int startX,
			final int startY, final int width, final int height, final String description) {
		for (int y = startY; y < startY + height; y++)
			for (int x = startX; x < startX + width; x++)
				assertEquals(0, image.getRGB(x, y) >>> 24,
						description + " at " + x + "," + y);
	}

	private static int countPixels(final BufferedImage image, final int startX,
			final int startY, final int width, final int height,
			final IntPredicate predicate) {
		int count = 0;
		for (int y = startY; y < startY + height; y++)
			for (int x = startX; x < startX + width; x++)
				if (predicate.test(image.getRGB(x, y)))
					count++;
		return count;
	}

	private static void assertMirrored(final BufferedImage image,
			final int sourceX, final int sourceY, final int mirrorX, final int mirrorY,
			final int width, final int height) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				assertEquals(image.getRGB(sourceX + x, sourceY + y),
						image.getRGB(mirrorX + width - 1 - x, mirrorY + y),
						"mirror mismatch at " + x + "," + y);
	}

	private static int regionHash(final BufferedImage image, final int startX, final int startY,
			final int width, final int height) {
		int hash = 1;
		for (int y = startY; y < startY + height; y++)
			for (int x = startX; x < startX + width; x++)
				hash = 31 * hash + image.getRGB(x, y);
		return hash;
	}

	private static boolean hasOpaquePixelOnRow(final BufferedImage image, final int startX,
			final int width, final int y) {
		for (int x = startX; x < startX + width; x++)
			if ((image.getRGB(x, y) >>> 24) > 0)
				return true;
		return false;
	}
}
