package it.cavallium.warppi.extra.mario;

import java.io.IOException;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.screens.Screen;

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
		historyBehavior = HistoryBehavior.ALWAYS_KEEP_IN_HISTORY;
	}

	@Override
	public void graphicInitialized(ScreenContext ctx) {
		try {
			if (MarioScreen.skin == null) {
				MarioScreen.skin = d.display.getGraphicEngine().loadSkin("/marioskin.png");
			}
			if (MarioScreen.groundskin == null) {
				MarioScreen.groundskin = d.display.getGraphicEngine().loadSkin("/marioground.png");
			}
			if (MarioScreen.gpuTest2 == null) {
				try {
					MarioScreen.gpuTest2 = d.display.getGraphicEngine().loadFont("N:\\gputest", "gputest2");
				} catch (final Exception ex) {}
			}
			if (MarioScreen.gpuTest1 == null) {
				try {
					MarioScreen.gpuTest1 = d.display.getGraphicEngine().loadFont("N:\\gputest", "gputest12");
					MarioScreen.gpuTest12 = true;
				} catch (final Exception ex) {
					MarioScreen.gpuTest12 = false;
					try {
						MarioScreen.gpuTest1 = d.display.getGraphicEngine().loadFont("N:\\gputest", "gputest1");
					} catch (final Exception ex2) {}
				}
			}
			if (MarioScreen.gpuTest3 == null) {
				try {
					MarioScreen.gpuTest3 = d.display.getGraphicEngine().loadSkin("N:\\gputest\\font_gputest3.png");
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialized() {
		try {
			if (MarioScreen.skin == null) {
				MarioScreen.skin = d.display.getGraphicEngine().loadSkin("/marioskin.png");
			}
			if (MarioScreen.groundskin == null) {
				MarioScreen.groundskin = d.display.getGraphicEngine().loadSkin("/marioground.png");
			}
			if (MarioScreen.gpuTest2 == null) {
				try {
					MarioScreen.gpuTest2 = d.display.getGraphicEngine().loadFont("N:\\gputest\\gputest2");
				} catch (final Exception ex) {}
			}
			if (MarioScreen.gpuTest1 == null) {
				try {
					MarioScreen.gpuTest1 = d.display.getGraphicEngine().loadFont("N:\\gputest\\gputest12");
					MarioScreen.gpuTest12 = true;
				} catch (final Exception ex) {
					MarioScreen.gpuTest12 = false;
					try {
						MarioScreen.gpuTest1 = d.display.getGraphicEngine().loadFont("N:\\gputest\\gputest1");
					} catch (final Exception ex2) {}
				}
			}
			if (MarioScreen.gpuTest3 == null) {
				try {
					MarioScreen.gpuTest3 = d.display.getGraphicEngine().loadSkin("N:\\gputest\\font_gputest3.png");
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

	boolean rightPressed, leftPressed, jumpPressed;
	
	@Override
	public boolean onKeyPressed(KeyPressedEvent k) {
		switch(k.getKey()) {
			case OK:
			case SIMPLIFY:
			case STEP:
				jumpPressed = true;
				return true;
			case LEFT:
				leftPressed = true;
				return true;
			case RIGHT:
				rightPressed = true;
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public boolean onKeyReleased(KeyReleasedEvent k) {
		switch(k.getKey()) {
			case OK:
			case SIMPLIFY:
			case STEP:
				jumpPressed = false;
				return true;
			case LEFT:
				leftPressed = false;
				return true;
			case RIGHT:
				rightPressed = false;
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public void beforeRender(ScreenContext ctx, final float dt) {
		if (!errored) {
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

			d.renderer.glClearColor(0xff000000);
		}
	}

	@Override
	public void render(RenderContext ctx) {
		DisplayOutputDevice display = d.display;
		if (errored) {
			d.renderer.glDrawStringLeft(0, 20, "ERROR");
		} else {
			if (MarioScreen.groundskin != null) {
				final double playerX = g.getPlayer().getX();
				final double playerY = g.getPlayer().getY();
				MarioScreen.groundskin.use(d.display);
				final MarioWorld w = g.getCurrentWorld();
				final int width = w.getWidth();
				final int height = w.getHeight();
				final float screenX = ctx.getWidth() / 2f - 8f;
				final float screenY = ctx.getHeight() / 2f - 8f;
				final float shiftX = -8 + 16 * (float) playerX;
				final float shiftY = -8 + 16 * (height - (float) playerY);
				int blue = -1;
				for (int ix = 0; ix < width; ix++) {
					for (int iy = 0; iy < height; iy++) {
						final double distX = Math.abs(playerX - ix);
						final double distY = Math.abs(playerY - iy - 1.2d);
						if (distX * distX + distY * distY / 2d < 270d) {
							final byte b = w.getBlockIdAt(ix, iy);
							if (b == 0) {
								if (blue != 1) {
									blue = 1;
									d.renderer.glColor(0xff9290ff);
								}
								d.renderer.glFillColor(screenX - shiftX + 16 * ix, screenY - shiftY + 16 * (height - iy), 16, 16);
							} else {
								if (blue != 0) {
									blue = 0;
									d.renderer.glColor(0xffffffff);
								}
								d.renderer.glFillRect(screenX - shiftX + 16 * ix, screenY - shiftY + 16 * (height - iy), 16, 16, 0, 0, 16, 16);
							}
						}
					}
				}
				if (blue != 0) {
					blue = 0;
					d.renderer.glColor(0xffffffff);
				}

				//DRAW MARIO
				MarioScreen.skin.use(d.display);
				d.renderer.glFillRect(screenX - (g.getPlayer().flipped ? 3 : 0), screenY, 35, 27, 35 * (g.getPlayer().marioSkinPos[0] + (g.getPlayer().flipped ? 2 : 1)), 27 * g.getPlayer().marioSkinPos[1], 35 * (g.getPlayer().flipped ? -1 : 1), 27);
//				PIDisplay.renderer.glDrawSkin(getPosX() - 18, 25 + getPosY(), 35 * (marioSkinPos[0] + (flipped ? 2 : 1)), 27 * marioSkinPos[1], 35 * (marioSkinPos[0] + (flipped ? 1 : 2)), 27 * (marioSkinPos[1] + 1), true);
			}

//		GPU PERFORMANCE TEST
			if (MarioScreen.gpuTest1 != null) {
				d.renderer.glColor3f(1, 1, 1);
				d.renderer.glFillColor(ctx.getWidth() - (MarioScreen.gpuTest12 ? 512 : 256), ctx.getHeight() / 2 - (MarioScreen.gpuTest12 ? 256 : 128), MarioScreen.gpuTest12 ? 512 : 256, MarioScreen.gpuTest12 ? 512 : 256);
				MarioScreen.gpuTest1.use(d.display);
				d.renderer.glColor3f(0, 0, 0);
				d.renderer.glDrawStringRight(ctx.getWidth(), ctx.getHeight() / 2 - (MarioScreen.gpuTest12 ? 256 : 128), gpuCharTest1[gpuCharTest1Num]);
			}
			if (MarioScreen.gpuTest3 != null) {
				MarioScreen.gpuTest3.use(d.display);
				d.renderer.glColor4f(1, 1, 1, 0.7f);
				d.renderer.glFillRect(0, display.getDisplaySize()[1] - 128, 224, 128, gpuTestNum * 224, 0, 224, 128);
			}
			if (MarioScreen.gpuTest2 != null) {
				MarioScreen.gpuTest2.use(d.display);
				d.renderer.glColor(0xFF000000);
				d.renderer.glDrawStringRight(display.getDisplaySize()[0], ctx.getHeight() - MarioScreen.gpuTest2.getCharacterHeight(), "A");
				d.renderer.glColor(0xFF800000);
				d.renderer.glDrawStringRight(display.getDisplaySize()[0], ctx.getHeight() - MarioScreen.gpuTest2.getCharacterHeight(), "B");
				d.renderer.glColor(0xFFeea28e);
				d.renderer.glDrawStringRight(display.getDisplaySize()[0], ctx.getHeight() - MarioScreen.gpuTest2.getCharacterHeight(), "C");
				d.renderer.glColor(0xFFee7255);
				d.renderer.glDrawStringRight(display.getDisplaySize()[0], ctx.getHeight() - MarioScreen.gpuTest2.getCharacterHeight(), "D");
				d.renderer.glColor(0xFFeac0b0);
				d.renderer.glDrawStringRight(display.getDisplaySize()[0], ctx.getHeight() - MarioScreen.gpuTest2.getCharacterHeight(), "E");
				d.renderer.glColor(0xFFf3d8ce);
				d.renderer.glDrawStringRight(display.getDisplaySize()[0], ctx.getHeight() - MarioScreen.gpuTest2.getCharacterHeight(), "F");
				d.renderer.glColor(0xFFffede7);
				d.renderer.glDrawStringRight(display.getDisplaySize()[0], ctx.getHeight() - MarioScreen.gpuTest2.getCharacterHeight(), "G");
			}
		}
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
