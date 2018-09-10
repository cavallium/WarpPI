package it.cavallium.warppi.gui.graphicengine.headless24bit;

import java.io.IOException;
import java.util.logging.ConsoleHandler;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.internal.WindowsSupport;

import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.Utils;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.flow.Observable;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;

public class Headless24bitEngine implements it.cavallium.warppi.gui.graphicengine.GraphicEngine {

	private Headless24bitRenderer r;
	private boolean stopped = true;
	private RenderingLoop renderLoop;
	public static final int C_MUL_X = 4;//8;
	public static final int C_MUL_Y = 8;//8;
	protected static int C_WIDTH;
	protected static int C_HEIGHT;
	private String title = StaticVars.calculatorName;
	private boolean win = false;
	private Key precKey = null;

	@Override
	public int[] getSize() {
		new ConsoleHandler();
		return new int[] { r.size[0], r.size[1] };
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

	@SuppressWarnings("unused")
	private long outHandle;

	@Override
	public void create() {
		this.create(null);
	}

	@Override
	public void create(Runnable onInitialized) {
		r = new Headless24bitRenderer();
		C_WIDTH = StaticVars.screenSize[0] / C_MUL_X;
		C_HEIGHT = StaticVars.screenSize[1] / C_MUL_Y;
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
		return r.size[0];
	}

	@Override
	public int getHeight() {
		return r.size[1];
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
		r.curColor = new int[] { 0x00, 0x87, 0x00 };
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
		int[] precBgColor = new int[] { -1, -1, -1 };
		int[] precFgColor = new int[] { -1, -1, -1 };
		int[] curBgColor = new int[] { -1, -1, -1 };
		int[] curFgColor = new int[] { -1, -1, -1 };
		String out = "";
		char outchar = ' ';
		for (int y = 0; y < C_HEIGHT; y++) {
			for (int x = 0; x < C_WIDTH; x++) {
				//BG color
				int[][] pixs = new int[C_MUL_X * C_MUL_Y][];
				for (int paddY = 0; paddY < C_MUL_Y; paddY++) {
					for (int paddX = 0; paddX < C_MUL_X; paddX++) {
						pixs[paddX + paddY * C_MUL_X] = r.bgColorMatrixSs[(x * C_MUL_X + paddX) + (y * C_MUL_Y + paddY) * r.size[0]];
					}
				}
				int[] newpix = new int[3];
				for (final int[] pix : pixs) {
					newpix[0] += pix[0];
					newpix[1] += pix[1];
					newpix[2] += pix[2];
				}
				newpix[0] /= pixs.length;
				newpix[1] /= pixs.length;
				newpix[2] /= pixs.length;
				r.bgColorMatrix[x + y * C_WIDTH] = newpix;

				//FG color
				pixs = new int[C_MUL_X * C_MUL_Y][];
				for (int paddY = 0; paddY < C_MUL_Y; paddY++) {
					for (int paddX = 0; paddX < C_MUL_X; paddX++) {
						pixs[paddX + paddY * C_MUL_X] = r.fgColorMatrixSs[(x * C_MUL_X + paddX) + (y * C_MUL_Y + paddY) * r.size[0]];
					}
				}
				newpix = new int[3];
				for (final int[] pix : pixs) {
					newpix[0] += pix[0];
					newpix[1] += pix[1];
					newpix[2] += pix[2];
				}
				newpix[0] /= pixs.length;
				newpix[1] /= pixs.length;
				newpix[2] /= pixs.length;
				r.fgColorMatrix[x + y * C_WIDTH] = newpix;
			}
		}
		for (int y = 0; y < C_HEIGHT; y++) {
			for (int x = 0; x < C_WIDTH; x++) {
				curBgColor = r.bgColorMatrix[x + y * C_WIDTH];
				curFgColor = r.fgColorMatrix[x + y * C_WIDTH];
				if (precBgColor != curBgColor) {
					out = Headless24bitRenderer.ANSI_PREFIX + Headless24bitRenderer.ansiBgColorPrefix + curBgColor[0] + ";" + curBgColor[1] + ";" + curBgColor[2] + Headless24bitRenderer.ansiColorSuffix;
					if (win) {
						WindowsSupport.writeConsole(out);
					} else {
						AnsiConsole.out.print(out);
					}
				}
				if (precFgColor != curFgColor) {
					out = Headless24bitRenderer.ANSI_PREFIX + Headless24bitRenderer.ansiFgColorPrefix + curFgColor[0] + ";" + curFgColor[1] + ";" + curFgColor[2] + Headless24bitRenderer.ansiColorSuffix;
					if (win) {
						WindowsSupport.writeConsole(out);
					} else {
						AnsiConsole.out.print(out);
					}
				}

				outchar = r.charmatrix[x + y * C_WIDTH];
				if (win) {
					WindowsSupport.writeConsole(outchar + "");
				} else {
					AnsiConsole.out.print(outchar);
				}

				precBgColor = curBgColor;
				precFgColor = curFgColor;
			}

			if (win) {
				//System.out.println(ch);
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
	public Headless24bitFont loadFont(String file) throws IOException {
		return new Headless24bitFont();
	}

	@Override
	public Headless24bitFont loadFont(String path, String file) throws IOException {
		return new Headless24bitFont();
	}

	@Override
	public Headless24bitSkin loadSkin(String file) throws IOException {
		return new Headless24bitSkin(file);
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
		if (StaticVars.startupArguments.isMSDOSModeEnabled() || (StaticVars.startupArguments.isEngineForced() && StaticVars.startupArguments.isHeadless24bitEngineForced() == false)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}
}
