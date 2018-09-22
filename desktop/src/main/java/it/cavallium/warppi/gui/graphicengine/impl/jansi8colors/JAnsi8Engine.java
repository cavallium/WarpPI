package it.cavallium.warppi.gui.graphicengine.impl.jansi8colors;

import java.io.IOException;
import java.util.logging.ConsoleHandler;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.internal.WindowsSupport;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.flow.Observable;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.impl.jansi24bitcolors.JAnsi24bitRenderer;
import it.cavallium.warppi.util.Utils;

public class JAnsi8Engine implements it.cavallium.warppi.gui.graphicengine.GraphicEngine {

	private JAnsi8Renderer r;
	private boolean stopped = true;
	private RenderingLoop renderLoop;
	public static final int C_MUL_X = 4;//8;
	public static final int C_MUL_Y = 8;//8;
	protected static int C_WIDTH;
	protected static int C_HEIGHT;
	private String title;
	private boolean win = false;
	private Key precKey = null;

	@Override
	public int[] getSize() {
		new ConsoleHandler();
		return new int[] { JAnsi8Engine.C_WIDTH, JAnsi8Engine.C_HEIGHT };
	}

	@Override
	public boolean isInitialized() {
		return !stopped;
	}

	@Override
	public void setTitle(final String title) {
		this.title = title;
	}

	@Override
	public void setResizable(final boolean r) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisplayMode(final int ww, final int wh) {
		// TODO Auto-generated method stub

	}

	@Override
	public void create() {
		this.create(null);
	}

	@Override
	public void create(final Runnable onInitialized) {
		title = Engine.getPlatform().getSettings().getCalculatorName();
		r = new JAnsi8Renderer();
		JAnsi8Engine.C_WIDTH = StaticVars.screenSize[0] / JAnsi8Engine.C_MUL_X;//Main.screenSize[0]/2;//;60;
		JAnsi8Engine.C_HEIGHT = StaticVars.screenSize[1] / JAnsi8Engine.C_MUL_Y;//Main.screenSize[1]/3;//;40;
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
					if (key != null)
						Keyboard.keyPressed(key);

				}
			});
			t.setDaemon(true);
			t.start();
		}
		stopped = false;
		if (onInitialized != null)
			onInitialized.run();
	}

	@Override
	public Observable<Integer[]> onResize() {
		return null;
	}

	@Override
	public int getWidth() {
		return JAnsi8Engine.C_WIDTH * JAnsi8Engine.C_MUL_X;
	}

	@Override
	public int getHeight() {
		return JAnsi8Engine.C_HEIGHT * JAnsi8Engine.C_MUL_Y;
	}

	@Override
	public void destroy() {
		stopped = true;
	}

	@Override
	public void start(final RenderingLoop d) {
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
					} else
						extratime += delta - 200d;
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
		r.glDrawStringCenter(JAnsi8Engine.C_WIDTH * JAnsi8Engine.C_MUL_X / 2, 0, title);
		if (win) {
			WindowsSupport.writeConsole(JAnsi24bitRenderer.ANSI_PREFIX + "0;0f");
			WindowsSupport.writeConsole(JAnsi24bitRenderer.ANSI_PREFIX + "?12l");
			WindowsSupport.writeConsole(JAnsi24bitRenderer.ANSI_PREFIX + "?25l");
		} else {
			AnsiConsole.out.print(JAnsi24bitRenderer.ANSI_PREFIX + "0;0f");
			AnsiConsole.out.print(JAnsi24bitRenderer.ANSI_PREFIX + "?12l");
			AnsiConsole.out.print(JAnsi24bitRenderer.ANSI_PREFIX + "?25l");
		}
		for (int y = 0; y < JAnsi8Engine.C_HEIGHT; y++) {
			int precBgColor = -1;
			int precFgColor = -1;
			int curBgColor = -1;
			int curFgColor = -1;
			for (int x = 0; x < JAnsi8Engine.C_WIDTH; x++) {
				curBgColor = (r.colorMatrix[x + y * JAnsi8Engine.C_WIDTH] & 0xF0) >> 4;
				curFgColor = r.colorMatrix[x + y * JAnsi8Engine.C_WIDTH] & 0x0F;
				if (precBgColor != curBgColor) {
					final String str = JAnsi8Renderer.ANSI_PREFIX + JAnsi8Renderer.ansiBgColorPrefix + JAnsi8Renderer.colorANSI[curBgColor] + JAnsi8Renderer.ansiColorSuffix;
					if (win)
						WindowsSupport.writeConsole(str);
					else
						AnsiConsole.out.print(str);
				}
				if (precFgColor != curFgColor) {
					final String str = JAnsi8Renderer.ANSI_PREFIX + JAnsi8Renderer.ansiFgColorPrefix + JAnsi8Renderer.colorANSI[curFgColor] + JAnsi8Renderer.ansiColorSuffix;
					if (win)
						WindowsSupport.writeConsole(str);
					else
						AnsiConsole.out.print(str);
				}

				final String stri = r.charmatrix[x + y * JAnsi8Engine.C_WIDTH] + "";
				if (win)
					WindowsSupport.writeConsole(stri);
				else
					AnsiConsole.out.print(stri);

				precBgColor = curBgColor;
				precFgColor = curFgColor;
			}

			if (win)
				WindowsSupport.writeConsole("\r\n");
			else
				AnsiConsole.out.println();
		}
	}

	@Override
	public Renderer getRenderer() {
		return r;
	}

	@Override
	public JAnsi8Font loadFont(final String file) throws IOException {
		return new JAnsi8Font();
	}

	@Override
	public JAnsi8Font loadFont(final String path, final String file) throws IOException {
		return new JAnsi8Font();
	}

	@Override
	public JAnsi8Skin loadSkin(final String file) throws IOException {
		return new JAnsi8Skin(file);
	}

	@Override
	public void waitForExit() {
		try {
			do
				Thread.sleep(500);
			while (stopped == false);
		} catch (final InterruptedException e) {

		}
	}

	@Override
	public boolean isSupported() {
		if (StaticVars.startupArguments.isMSDOSModeEnabled() || StaticVars.startupArguments.isEngineForced() && StaticVars.startupArguments.isHeadless8EngineForced() == false)
			return false;
		return true;
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}
}
