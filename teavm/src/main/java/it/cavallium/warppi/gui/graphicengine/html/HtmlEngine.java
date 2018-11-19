package it.cavallium.warppi.gui.graphicengine.html;

import java.io.IOException;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventTarget;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLButtonElement;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;
import org.teavm.jso.dom.xml.NodeList;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.Platform.Semaphore;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.flow.BehaviorSubject;
import it.cavallium.warppi.flow.Observable;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;

public class HtmlEngine implements GraphicEngine {

	private boolean initialized;
	public Semaphore exitSemaphore;
	private static final HTMLDocument document = Window.current().getDocument();
	private HTMLCanvasElement canvas;
	private CanvasRenderingContext2D g;
	private RenderingLoop renderingLoop;
	private HtmlRenderer renderer;
	private int width, height;
	public int mult = 1;
	private final int frameTime = (int) (1000d / 10d);
	private final BehaviorSubject<Integer[]> onResize = BehaviorSubject.create();
	private final BehaviorSubject<Float> onZoom = BehaviorSubject.create();

	@Override
	public int[] getSize() {
		return new int[] { getWidth(), getHeight() };
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setTitle(final String title) {
		HtmlEngine.setHTMLTitle(title);
	}

	@JSBody(params = { "wndTitle" }, script = "document.title = wndTitle")
	private static native void setHTMLTitle(String wndTitle);

	@Override
	public void setResizable(final boolean r) {}

	@Override
	public void setDisplayMode(final int ww, final int wh) {
		canvas.setWidth(ww);
		width = ww;
		canvas.setHeight(wh);
		height = wh;
	}

	private String previousValue = "";
	private static final Object2IntArrayMap<String> keyNames = new Object2IntArrayMap<>();
	
	static {
		keyNames.put(" ", 32);
		keyNames.put("ArrowUp", 38);
		keyNames.put("ArrowDown", 40);
		keyNames.put("ArrowLeft", 37);
		keyNames.put("ArrowRight", 39);
	}

	@JSBody(params = { "ctx", "enabled" }, script = "" + "ctx.mozImageSmoothingEnabled = enabled;" + "ctx.oImageSmoothingEnabled = enabled;" + "ctx.webkitImageSmoothingEnabled = enabled;" + "ctx.msImageSmoothingEnabled = enabled;" + "ctx.imageSmoothingEnabled = enabled;")
	public static native void setImageSmoothingEnabled(CanvasRenderingContext2D ctx, boolean enabled);

	@Override
	public void create(final Runnable onInitialized) {
		exitSemaphore = Engine.getPlatform().newSemaphore(0);
		width = -1;
		height = -1;
		canvas = (HTMLCanvasElement) HtmlEngine.document.createElement("canvas");
		g = (CanvasRenderingContext2D) canvas.getContext("2d");
		final HTMLInputElement keyInput = (HTMLInputElement) HtmlEngine.document.createElement("input");
		StaticVars.windowZoom$.subscribe((zoom) -> {
			onZoom.onNext(zoom);
		});
		onZoom.subscribe((windowZoom) -> {
			if (windowZoom != 0) {
				if (suppportsZoom()) {
					canvas.setWidth((int) (480 / 1));
					canvas.setHeight((int) (320 / 1));
					canvas.getStyle().setProperty("zoom", "" + (1 + 1));
				} else {
					canvas.setWidth((int) (480 * 2));
					canvas.setHeight((int) (320 * 2));
				}
				canvas.getStyle().setProperty("max-height", (int) (44 / (1 + 1)) + "vh");
				width = 480 / windowZoom.intValue();
				height = 320 / windowZoom.intValue();
				this.mult = windowZoom.intValue();
				StaticVars.screenSize[0] = width;
				StaticVars.screenSize[1] = height;
			}
		});
		keyInput.setType("text");
		keyInput.getStyle().setProperty("opacity", "0.1");
		setDisplayMode(480, 320);
		HtmlEngine.document.getElementById("container").appendChild(canvas);
		HtmlEngine.document.getBody().appendChild(keyInput);
		keyInput.setTabIndex(0);
		keyInput.setValue("");
		HtmlEngine.document.addEventListener("keydown", (final KeyboardEvent evt) -> {
			evt.preventDefault();
			new Thread(() -> {
				Keyboard.debugKey(keyNames .getOrDefault(evt.getKey(), evt.getKeyCode()), false);
			}).start();
		});
		HtmlEngine.document.addEventListener("keyup", (final KeyboardEvent evt) -> {
			evt.preventDefault();
			new Thread(() -> {
				Keyboard.debugKey(keyNames .getOrDefault(evt.getKey(), evt.getKeyCode()), true);
			}).start();
		});
		keyInput.addEventListener("input", (final Event evt) -> {
			evt.preventDefault();
			final String previousValue = this.previousValue;
			final String newValue = keyInput.getValue();
			final int prevLen = previousValue.length();
			final int newLen = newValue.length();

			new Thread(() -> {
				if (newLen == prevLen) {

				} else if (newLen - prevLen == 1) {
					Keyboard.debugKey(newValue.toUpperCase().charAt(newLen - 1), false);
					Keyboard.debugKey(newValue.toUpperCase().charAt(newLen - 1), true);}
				else if (newLen - prevLen > 1)
					for (int i = 0; i < newLen - prevLen; i++) {
						Keyboard.debugKey(newValue.toUpperCase().charAt(prevLen + i), false);
						Keyboard.debugKey(newValue.toUpperCase().charAt(prevLen + i), true);}
				else if (newLen - prevLen < 1)
					for (int i = 0; i < prevLen - newLen; i++) {
						Keyboard.debugKey(8, false);
						Keyboard.debugKey(8, true);}
			}).start();
		});
		canvas.addEventListener("click", (final Event evt) -> {
			keyInput.focus();
		});
		HtmlEngine.document.addEventListener("DOMContentLoaded", (final Event e) -> {
			keyInput.focus();
		});
		final NodeList<? extends HTMLElement> buttons = HtmlEngine.document.getBody().getElementsByTagName("button");
		for (int i = 0; i < buttons.getLength(); i++)
			if (buttons.item(i).hasAttribute("keycode")) {
				buttons.item(i).addEventListener("touchstart", (final Event evt) -> {
					buttonEvent(evt, false);
				});
				buttons.item(i).addEventListener("touchend", (final Event evt) -> {
					buttonEvent(evt, true);
				});
				buttons.item(i).addEventListener("mousedown", (final Event evt) -> {
					buttonEvent(evt, false);
				});
				buttons.item(i).addEventListener("mouseup", (final Event evt) -> {
					buttonEvent(evt, true);
				});
			}
		renderer = new HtmlRenderer(this, g);
		initialized = true;
		if (onInitialized != null)
			onInitialized.run();
	}

	private void buttonEvent(Event evt, boolean released) {
		evt.preventDefault();
		final EventTarget target = evt.getCurrentTarget();
		final HTMLButtonElement button = target.cast();
		new Thread(() -> {
			try {
				if (button.hasAttribute("keycode") && button.getAttribute("keycode").contains(",")) {
					final String code = button.getAttribute("keycode");
					final String[] coordinates = code.split(",", 2);
					final boolean removeshift = Keyboard.shift && Integer.parseInt(coordinates[0]) != 0 && Integer.parseInt(coordinates[1]) != 0;
					final boolean removealpha = Keyboard.alpha && Integer.parseInt(coordinates[0]) != 0 && Integer.parseInt(coordinates[1]) != 1;
					Keyboard.keyRaw(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), released);
					if (released) {
						if (removeshift)
							Keyboard.keyRaw(0, 0, false);
						if (removealpha)
							Keyboard.keyRaw(0, 1, false);
					}
				} else if (Keyboard.alpha && !Keyboard.shift) {
					if (button.hasAttribute("keycodea")) {
						Keyboard.debugKey(Integer.parseInt(button.getAttribute("keycodea")), released);
					} else {
						Keyboard.debugKey(Integer.parseInt(button.getAttribute("keycode")), released);
					}
				} else if (!Keyboard.alpha && Keyboard.shift) {
					if (button.hasAttribute("keycodes")) {
						Keyboard.debugKey(Integer.parseInt(button.getAttribute("keycodes")), released);
					} else {
						Keyboard.debugKey(Integer.parseInt(button.getAttribute("keycode")), released);
					}
				} else if (Keyboard.alpha && Keyboard.shift) {
					if (button.hasAttribute("keycodesa")) {
						Keyboard.debugKey(Integer.parseInt(button.getAttribute("keycodesa")), released);
					} else if (button.hasAttribute("keycodes")) {
						Keyboard.debugKey(Integer.parseInt(button.getAttribute("keycodes")), released);
					} else {
						Keyboard.debugKey(Integer.parseInt(button.getAttribute("keycode")), released);
					}
				} else {
					Keyboard.debugKey(Integer.parseInt(button.getAttribute("keycode")), released);
				}
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	@JSBody(params = {}, script = "return CSS.supports(\"zoom:2\")")
	private static native boolean suppportsZoom();

	@Override
	public int getWidth() {
		if (width == -1)
			width = canvas.getWidth();
		return width;
	}

	@Override
	public int getHeight() {
		if (height == -1)
			height = canvas.getHeight();
		return height;
	}

	@Override
	public void destroy() {
		HtmlEngine.document.getBody().removeChild(canvas);
		initialized = false;
		exitSemaphore.release();
	}

	@Override
	public void start(final RenderingLoop d) {
		renderingLoop = d;
		final Thread th = new Thread(() -> {
			try {
				double extratime = 0;
				while (initialized) {
					final long start = System.currentTimeMillis();
					repaint();
					final long end = System.currentTimeMillis();
					final double delta = (end - start) / 1000d;
					final int deltaInt = (int) Math.floor(delta);
					final int extraTimeInt = (int) Math.floor(extratime);
					if (extraTimeInt + deltaInt < frameTime) {
						Thread.sleep(frameTime - (extraTimeInt + deltaInt));
						extratime = 0;
					} else
						extratime += delta - frameTime;
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		});
		Engine.getPlatform().setThreadName(th, "Canvas rendering thread");
		Engine.getPlatform().setThreadDaemon(th);
		th.start();
	}

	@Override
	public void repaint() {
		renderingLoop.refresh();
	}

	@Override
	public HtmlRenderer getRenderer() {
		return renderer;
	}

	@Override
	public HtmlFont loadFont(final String fontName) throws IOException {
		return new HtmlFont(fontName);
	}

	@Override
	public HtmlFont loadFont(final String path, final String fontName) throws IOException {
		return new HtmlFont(fontName);
	}

	@Override
	public HtmlSkin loadSkin(final String file) throws IOException {
		return new HtmlSkin(file);
	}

	@Override
	public void waitForExit() {
		try {
			exitSemaphore.acquire();
		} catch (final InterruptedException e) {}
	}

	@Override
	public boolean isSupported() {
		return Engine.getPlatform().isJavascript();
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}

	@Override
	public Observable<Integer[]> onResize() {
		return onResize;
	}

}