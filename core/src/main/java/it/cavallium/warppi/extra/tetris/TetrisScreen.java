package it.cavallium.warppi.extra.tetris;

import java.io.IOException;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.screens.Screen;

public class TetrisScreen extends Screen {

	private TetrisGame g;

	private static Skin skin;
	
	public TetrisScreen() {
		super();
		historyBehavior = HistoryBehavior.ALWAYS_KEEP_IN_HISTORY;
	}

	@Override
	public void initialized() {
//		try {
//			if (TetrisScreen.skin == null) {
//				TetrisScreen.skin = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.loadSkin("/marioskin.png");
//			}
//			if (TetrisScreen.groundskin == null) {
//				TetrisScreen.groundskin = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.loadSkin("/marioground.png");
//			}
//			if (TetrisScreen.gpuTest2 == null) {
//				try {
//					TetrisScreen.gpuTest2 = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.loadFont("N:\\gputest\\gputest2");
//				} catch (final Exception ex) {}
//			}
//			if (TetrisScreen.gpuTest1 == null) {
//				try {
//					TetrisScreen.gpuTest1 = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.loadFont("N:\\gputest\\gputest12");
//					TetrisScreen.gpuTest12 = true;
//				} catch (final Exception ex) {
//					TetrisScreen.gpuTest12 = false;
//					try {
//						TetrisScreen.gpuTest1 = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.loadFont("N:\\gputest\\gputest1");
//					} catch (final Exception ex2) {}
//				}
//			}
//			if (TetrisScreen.gpuTest3 == null) {
//				try {
//					TetrisScreen.gpuTest3 = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.loadSkin("N:\\gputest\\font_gputest3.png");
//				} catch (final Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//		} catch (final IOException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void created() throws InterruptedException {
//		if (!errored) {
//			g = new MarioGame();
//		}
	}

	@Override
	public void beforeRender(final float dt) {
//		if (!errored) {
//			final boolean rightPressed = Keyboard.isKeyDown(2, 5);
//			final boolean leftPressed = Keyboard.isKeyDown(2, 3);
//			final boolean jumpPressed = Keyboard.isKeyDown(2, 1);
//			final boolean upPressed = false, downPressed = false, runPressed = false;
//			g.gameTick(dt, upPressed, downPressed, leftPressed, rightPressed, jumpPressed, runPressed);
//
//			gpuTestElapsed += dt;
//			while (gpuTestElapsed >= 0.04) {
//				gpuTestNum = (gpuTestNum + 1) % gpuTestMax;
//				gpuTestElapsed -= 0.04;
//			}
//			gpuCharTestt1Elapsed += dt;
//			while (gpuCharTestt1Elapsed >= 1.5) {
//				gpuCharTest1Num = (gpuCharTest1Num + 1) % gpuCharTest1.length;
//				gpuCharTestt1Elapsed -= 1.5;
//			}
//
//			Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glClearColor(0xff000000);
//		}
	}

	@Override
	public void render() {
//		if (errored) {
//			Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringLeft(0, 20, "ERROR");
//		} else {
//			if (TetrisScreen.groundskin != null) {
//				final double playerX = g.getPlayer().getX();
//				final double playerY = g.getPlayer().getY();
//				TetrisScreen.groundskin.use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
//				final MarioWorld w = g.getCurrentWorld();
//				final int width = w.getWidth();
//				final int height = w.getHeight();
//				final float screenX = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getWidth() / 2f - 8f;
//				final float screenY = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() / 2f - 8f;
//				final float shiftX = -8 + 16 * (float) playerX;
//				final float shiftY = -8 + 16 * (height - (float) playerY);
//				int blue = -1;
//				for (int ix = 0; ix < width; ix++) {
//					for (int iy = 0; iy < height; iy++) {
//						final double distX = Math.abs(playerX - ix);
//						final double distY = Math.abs(playerY - iy - 1.5d);
//						if (distX * distX + distY * distY / 2d < 25d) {
//							final byte b = w.getBlockIdAt(ix, iy);
//							if (b == 0) {
//								if (blue != 1) {
//									blue = 1;
//									Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xff9290ff);
//								}
//								Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillColor(screenX - shiftX + 16 * ix, screenY - shiftY + 16 * (height - iy), 16, 16);
//							} else {
//								if (blue != 0) {
//									blue = 0;
//									Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xffffffff);
//								}
//								Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(screenX - shiftX + 16 * ix, screenY - shiftY + 16 * (height - iy), 16, 16, 0, 0, 16, 16);
//							}
//						}
//					}
//				}
//				if (blue != 0) {
//					blue = 0;
//					Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xffffffff);
//				}
//
//				//DRAW MARIO
//				TetrisScreen.skin.use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(screenX - (g.getPlayer().flipped ? 3 : 0), screenY, 35, 27, 35 * (g.getPlayer().marioSkinPos[0] + (g.getPlayer().flipped ? 2 : 1)), 27 * g.getPlayer().marioSkinPos[1], 35 * (g.getPlayer().flipped ? -1 : 1), 27);
////				PIDisplay.renderer.glDrawSkin(getPosX() - 18, 25 + getPosY(), 35 * (marioSkinPos[0] + (flipped ? 2 : 1)), 27 * marioSkinPos[1], 35 * (marioSkinPos[0] + (flipped ? 1 : 2)), 27 * (marioSkinPos[1] + 1), true);
//			}
//
////		GPU PERFORMANCE TEST
//			if (TetrisScreen.gpuTest1 != null) {
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor3f(1, 1, 1);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillColor(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getWidth() - (TetrisScreen.gpuTest12 ? 512 : 256), Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() / 2 - (TetrisScreen.gpuTest12 ? 256 : 128), TetrisScreen.gpuTest12 ? 512 : 256, TetrisScreen.gpuTest12 ? 512 : 256);
//				TetrisScreen.gpuTest1.use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor3f(0, 0, 0);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringRight(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getWidth(), Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() / 2 - (TetrisScreen.gpuTest12 ? 256 : 128), gpuCharTest1[gpuCharTest1Num]);
//			}
//			if (TetrisScreen.gpuTest3 != null) {
//				TetrisScreen.gpuTest3.use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor4f(1, 1, 1, 0.7f);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(0, StaticVars.screenSize[1] - 128, 224, 128, gpuTestNum * 224, 0, 224, 128);
//			}
//			if (TetrisScreen.gpuTest2 != null) {
//				TetrisScreen.gpuTest2.use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xFF000000);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() - TetrisScreen.gpuTest2.getCharacterHeight(), "A");
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xFF800000);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() - TetrisScreen.gpuTest2.getCharacterHeight(), "B");
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xFFeea28e);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() - TetrisScreen.gpuTest2.getCharacterHeight(), "C");
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xFFee7255);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() - TetrisScreen.gpuTest2.getCharacterHeight(), "D");
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xFFeac0b0);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() - TetrisScreen.gpuTest2.getCharacterHeight(), "E");
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xFFf3d8ce);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() - TetrisScreen.gpuTest2.getCharacterHeight(), "F");
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor(0xFFffede7);
//				Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringRight(StaticVars.screenSize[0], Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.getHeight() - TetrisScreen.gpuTest2.getCharacterHeight(), "G");
//			}
//		}
	}

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

	@Override
	public String getSessionTitle() {
		return "Absolutely not Super Mario";
	}

}
