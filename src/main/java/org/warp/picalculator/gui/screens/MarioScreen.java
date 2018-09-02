package org.warp.picalculator.gui.screens;

import java.io.IOException;

import org.warp.picalculator.StaticVars;
import org.warp.picalculator.device.HardwareDevice;
import org.warp.picalculator.device.Keyboard;
import org.warp.picalculator.extra.mario.MarioGame;
import org.warp.picalculator.extra.mario.MarioWorld;
import org.warp.picalculator.gui.graphicengine.BinaryFont;
import org.warp.picalculator.gui.graphicengine.Skin;

public class MarioScreen extends Screen {

	private MarioGame g;

	private static Skin skin;
	private static Skin groundskin;
	private static BinaryFont gpuTest2;
	private static BinaryFont gpuTest1;
	private static boolean gpuTest12;
	private static Skin gpuTest3;
	private int gpuTestNum = 0;
	private float gpuTestElapsed = 0;
	private final int gpuTestMax = 21;
	private final String[] gpuCharTest1 = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "ò" };
	private int gpuCharTest1Num = 0;
	private float gpuCharTestt1Elapsed = 0;
	private boolean errored;
//	public float[] marioPos = new float[] { 30, 0 };
//	public float[] marioForces = new float[] { 0, 0 };
//	public float jumptime = 0;
//	public boolean walking = false;
//	public boolean running = false;
//	public boolean jumping = false;
//	public boolean flipped = false;
//	public boolean onGround = true;

	public MarioScreen() {
		super();
		canBeInHistory = false;
	}

	@Override
	public void initialized() {
		try {
			if (skin == null) {
				skin = HardwareDevice.INSTANCE.getDisplayManager().engine.loadSkin("marioskin.png");
			}
			if (groundskin == null) {
				groundskin = HardwareDevice.INSTANCE.getDisplayManager().engine.loadSkin("marioground.png");
			}
			if (gpuTest2 == null) {
				try {
					gpuTest2 = HardwareDevice.INSTANCE.getDisplayManager().engine.loadFont("gputest2");
				} catch (final Exception ex) {}
			}
			if (gpuTest1 == null) {
				try {
					gpuTest1 = HardwareDevice.INSTANCE.getDisplayManager().engine.loadFont("gputest12");
					gpuTest12 = true;
				} catch (final Exception ex) {
					gpuTest12 = false;
					try {
						gpuTest1 = HardwareDevice.INSTANCE.getDisplayManager().engine.loadFont("gputest1");
					} catch (final Exception ex2) {}
				}
			}
			if (gpuTest3 == null) {
				try {
					gpuTest3 = HardwareDevice.INSTANCE.getDisplayManager().engine.loadSkin("font_gputest3.png");
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void created() throws InterruptedException {
		if (!errored) {
			g = new MarioGame();
		}
	}

	@Override
	public void beforeRender(float dt) {
		if (!errored) {
			final boolean rightPressed = Keyboard.isKeyDown(2, 5);
			final boolean leftPressed = Keyboard.isKeyDown(2, 3);
			final boolean jumpPressed = Keyboard.isKeyDown(2, 1);
			final boolean upPressed = false, downPressed = false, runPressed = false;
			g.gameTick(dt, upPressed, downPressed, leftPressed, rightPressed, jumpPressed, runPressed);

			gpuTestElapsed += dt;
			while (gpuTestElapsed >= 0.04) {
				gpuTestNum = (gpuTestNum + 1) % gpuTestMax;
				gpuTestElapsed -= 0.04;
			}
			gpuCharTestt1Elapsed += dt;
			while (gpuCharTestt1Elapsed >= 1.5) {
				gpuCharTest1Num = (gpuCharTest1Num + 1) % gpuCharTest1.length;
				gpuCharTestt1Elapsed -= 1.5;
			}

			HardwareDevice.INSTANCE.getDisplayManager().renderer.glClearColor(0xff000000);
		}
	}

	@Override
	public void render() {
		if (errored) {
			HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringLeft(0, 20, "ERROR");
		} else {
			if (groundskin != null) {
				final double playerX = g.getPlayer().getX();
				final double playerY = g.getPlayer().getY();
				groundskin.use(HardwareDevice.INSTANCE.getDisplayManager().engine);
				final MarioWorld w = g.getCurrentWorld();
				final int width = w.getWidth();
				final int height = w.getHeight();
				final float screenX = HardwareDevice.INSTANCE.getDisplayManager().engine.getWidth() / 2f - 8f;
				final float screenY = HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() / 2f - 8f;
				final float shiftX = -8 + 16 * (float) playerX;
				final float shiftY = -8 + 16 * (height - (float) playerY);
				int blue = -1;
				for (int ix = 0; ix < width; ix++) {
					for (int iy = 0; iy < height; iy++) {
						final double distX = Math.abs(playerX - ix);
						final double distY = Math.abs(playerY - iy - 1.5d);
						if ((distX * distX + distY * distY / 2d) < 25d) {
							final byte b = w.getBlockIdAt(ix, iy);
							if (b == 0) {
								if (blue != 1) {
									blue = 1;
									HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xff9290ff);
								}
								HardwareDevice.INSTANCE.getDisplayManager().renderer.glFillColor(screenX - shiftX + 16 * ix, screenY - shiftY + 16 * (height - iy), 16, 16);
							} else {
								if (blue != 0) {
									blue = 0;
									HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xffffffff);
								}
								HardwareDevice.INSTANCE.getDisplayManager().renderer.glFillRect(screenX - shiftX + 16 * ix, screenY - shiftY + 16 * (height - iy), 16, 16, 0, 0, 16, 16);
							}
						}
					}
				}
				if (blue != 0) {
					blue = 0;
					HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xffffffff);
				}

				//DRAW MARIO
				skin.use(HardwareDevice.INSTANCE.getDisplayManager().engine);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glFillRect(screenX - (g.getPlayer().flipped ? 3 : 0), screenY, 35, 27, 35 * (g.getPlayer().marioSkinPos[0] + (g.getPlayer().flipped ? 2 : 1)), 27 * g.getPlayer().marioSkinPos[1], 35 * (g.getPlayer().flipped ? -1 : 1), 27);
//				PIDisplay.renderer.glDrawSkin(getPosX() - 18, 25 + getPosY(), 35 * (marioSkinPos[0] + (flipped ? 2 : 1)), 27 * marioSkinPos[1], 35 * (marioSkinPos[0] + (flipped ? 1 : 2)), 27 * (marioSkinPos[1] + 1), true);
			}

//		GPU PERFORMANCE TEST
			if (gpuTest1 != null) {
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor3f(1, 1, 1);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glFillColor(HardwareDevice.INSTANCE.getDisplayManager().engine.getWidth() - (gpuTest12 ? 512 : 256), HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() / 2 - (gpuTest12 ? 256 : 128), gpuTest12 ? 512 : 256, gpuTest12 ? 512 : 256);
				gpuTest1.use(HardwareDevice.INSTANCE.getDisplayManager().engine);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor3f(0, 0, 0);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringRight(HardwareDevice.INSTANCE.getDisplayManager().engine.getWidth(), HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() / 2 - (gpuTest12 ? 256 : 128), gpuCharTest1[gpuCharTest1Num]);
			}
			if (gpuTest3 != null) {
				gpuTest3.use(HardwareDevice.INSTANCE.getDisplayManager().engine);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor4f(1, 1, 1, 0.7f);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glFillRect(0, StaticVars.screenSize[1] - 128, 224, 128, gpuTestNum * 224, 0, 224, 128);
			}
			if (gpuTest2 != null) {
				gpuTest2.use(HardwareDevice.INSTANCE.getDisplayManager().engine);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xFF000000);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() - gpuTest2.getCharacterHeight(), "A");
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xFF800000);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() - gpuTest2.getCharacterHeight(), "B");
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xFFeea28e);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() - gpuTest2.getCharacterHeight(), "C");
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xFFee7255);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() - gpuTest2.getCharacterHeight(), "D");
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xFFeac0b0);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() - gpuTest2.getCharacterHeight(), "E");
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xFFf3d8ce);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() - gpuTest2.getCharacterHeight(), "F");
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor(0xFFffede7);
				HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], HardwareDevice.INSTANCE.getDisplayManager().engine.getHeight() - gpuTest2.getCharacterHeight(), "G");
			}
		}
	}

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

}
