package org.warp.picalculator.gui.graphicengine.headless256;

import java.io.IOException;
import java.util.logging.ConsoleHandler;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.internal.WindowsSupport;
import org.warp.picalculator.StaticVars;
import org.warp.picalculator.Utils;
import org.warp.picalculator.device.Keyboard;
import org.warp.picalculator.event.Key;
import org.warp.picalculator.gui.graphicengine.Renderer;
import org.warp.picalculator.gui.graphicengine.RenderingLoop;
import org.warp.picalculator.gui.graphicengine.headless24bit.Headless24bitRenderer;

import io.reactivex.Observable;

public class Headless256Engine implements org.warp.picalculator.gui.graphicengine.GraphicEngine {

	private final Headless256Renderer r = new Headless256Renderer();
	private boolean stopped = true;
	private RenderingLoop renderLoop;
	public static final int C_MUL_X = 4;//8;
	public static final int C_MUL_Y = 8;//8;
	protected static final int C_WIDTH = StaticVars.screenSize[0] / C_MUL_X;//Main.screenSize[0]/2;//;60;
	protected static final int C_HEIGHT = StaticVars.screenSize[1] / C_MUL_Y;//Main.screenSize[1]/3;//;40;
	private String title = StaticVars.calculatorName;
	private boolean win = false;
	private Key precKey = null;

	@Override
	public int[] getSize() {
		new ConsoleHandler();
		return new int[] { C_WIDTH, C_HEIGHT };
	}

	@Override
	public boolean isInitialized() {
		return !stopped;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setResizable(boolean r) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisplayMode(int ww, int wh) {
		// TODO Auto-generated method stub

	}

	@Override
	public void create() {
		this.create(null);
	}

	@Override
	public void create(Runnable onInitialized) {
		StaticVars.outputLevel = -1;
		AnsiConsole.systemInstall();
		if (Utils.isWindows() && !StaticVars.startupArguments.isMSDOSModeEnabled()) {
			win = true;
			WindowsSupport.setConsoleMode(0x0200);
			final Thread t = new Thread(() -> {
				int ch = -1;
				while (true) {
					if (precKey != null) {
						Keyboard.keyReleased(precKey);
						precKey = null;
					}
					ch = WindowsSupport.readByte();
					Key key = null;
					switch (ch) {
						case 72: { // UP
							key = Key.UP;
							break;
						}
						case 80: { // DOWN
							key = Key.DOWN;
							break;
						}
						case 77: { // RIGHT
							key = Key.RIGHT;
							break;
						}
						case 75: { // LEFT
							key = Key.LEFT;
							break;
						}
						case 49: { // 1
							key = Key.NUM1;
							break;
						}
						case 50: { // 2
							key = Key.NUM2;
							break;
						}
						case 51: { // 3
							key = Key.NUM3;
							break;
						}
						case 52: { // 4
							key = Key.NUM4;
							break;
						}
						case 53: { // 5
							key = Key.NUM5;
							break;
						}
						case 54: { // 6
							key = Key.NUM6;
							break;
						}
						default: {
							key = Key.NONE;
							break;
						}
					}
					if (key != null) {
						Keyboard.keyPressed(key);
					}

				}
			});
			t.setDaemon(true);
			t.start();
		}
		stopped = false;
		if (onInitialized != null) {
			onInitialized.run();
		}
	}

	@Override
	public Observable<Integer[]> onResize() {
		return null;
	}

	@Override
	public int getWidth() {
		return C_WIDTH * C_MUL_X;
	}

	@Override
	public int getHeight() {
		return C_HEIGHT * C_MUL_Y;
	}

	@Override
	public void destroy() {
		stopped = true;
	}

	@Override
	public void start(RenderingLoop d) {
		renderLoop = d;
		final Thread th = new Thread(() -> {
			try {
				double extratime = 0;
				while (!stopped) {
					final long start = System.currentTimeMillis();
					repaint();
					final long end = System.currentTimeMillis();
					final double delta = (end - start) / 1000d;
					final int deltaInt = (int) Math.floor(delta);
					final int extraTimeInt = (int) Math.floor(extratime);
					if (extraTimeInt + deltaInt < 200) {
						Thread.sleep(200 - (extraTimeInt + deltaInt));
						extratime = 0;
					} else {
						extratime += delta - 200d;
					}
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		});
		th.setName("Console rendering thread");
		th.setDaemon(true);
		th.start();
	}

	@Override
	public void repaint() {
		renderLoop.refresh();
		r.curColor = 0x1C;
		r.glDrawStringCenter((C_WIDTH * C_MUL_X) / 2, 0, title);
		if (win) {
			WindowsSupport.writeConsole(Headless24bitRenderer.ANSI_PREFIX + "0;0f");
			WindowsSupport.writeConsole(Headless24bitRenderer.ANSI_PREFIX + "?12l");
			WindowsSupport.writeConsole(Headless24bitRenderer.ANSI_PREFIX + "?25l");
		} else {
			AnsiConsole.out.print(Headless24bitRenderer.ANSI_PREFIX + "0;0f");
			AnsiConsole.out.print(Headless24bitRenderer.ANSI_PREFIX + "?12l");
			AnsiConsole.out.print(Headless24bitRenderer.ANSI_PREFIX + "?25l");
		}
		for (int y = 0; y < C_HEIGHT; y++) {
			int precBgColor = -1;
			int precFgColor = -1;
			int curBgColor = -1;
			int curFgColor = -1;
			for (int x = 0; x < C_WIDTH; x++) {
				curBgColor = r.bgColorMatrix[x + y * C_WIDTH];
				curFgColor = r.fgColorMatrix[x + y * C_WIDTH];
				if (precBgColor != curBgColor) {
					final String str = Headless256Renderer.ANSI_PREFIX + Headless256Renderer.ansiBgColorPrefix + curBgColor + Headless256Renderer.ansiColorSuffix;
					if (win) {
						WindowsSupport.writeConsole(str);
					} else {
						AnsiConsole.out.print(str);
					}
				}
				if (precFgColor != curFgColor) {
					final String str = Headless256Renderer.ANSI_PREFIX + Headless256Renderer.ansiFgColorPrefix + curFgColor + Headless256Renderer.ansiColorSuffix;
					if (win) {
						WindowsSupport.writeConsole(str);
					} else {
						AnsiConsole.out.print(str);
					}
				}

				final String stri = r.charmatrix[x + y * C_WIDTH] + "";
				if (win) {
					WindowsSupport.writeConsole(stri);
				} else {
					AnsiConsole.out.print(stri);
				}

				precBgColor = curBgColor;
				precFgColor = curFgColor;
			}

			if (win) {
				WindowsSupport.writeConsole("\r\n");
			} else {
				AnsiConsole.out.println();
			}
		}
	}

	@Override
	public Renderer getRenderer() {
		return r;
	}

	@Override
	public Headless256Font loadFont(String file) throws IOException {
		return new Headless256Font();
	}

	@Override
	public Headless256Font loadFont(String path, String file) throws IOException {
		return new Headless256Font();
	}

	@Override
	public Headless256Skin loadSkin(String file) throws IOException {
		return new Headless256Skin(file);
	}

	@Override
	public void waitForExit() {
		try {
			do {
				Thread.sleep(500);
			} while (stopped == false);
		} catch (final InterruptedException e) {

		}
	}

	@Override
	public boolean isSupported() {
		if (StaticVars.startupArguments.isMSDOSModeEnabled() || (StaticVars.startupArguments.isEngineForced() && StaticVars.startupArguments.isHeadless256EngineForced() == false)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}
}
