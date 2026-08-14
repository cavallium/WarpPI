package it.cavallium.warppi.gui.graphicengine.html;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import it.cavallium.warppi.util.EventSubmitter;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLButtonElement;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalKey;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalRow;
import it.cavallium.warppi.device.input.HostKeyboardInput;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.device.input.KeyboardAWTValues;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;

public class HtmlEngine implements GraphicEngine {

	private boolean initialized;
	private static final HTMLDocument document = Window.current().getDocument();
	private HTMLCanvasElement canvas;
	private CanvasRenderingContext2D g;
	private RenderingLoop renderingLoop;
	private HtmlRenderer renderer;
	private HostKeyboardInput keyboardInput;
	private int width, height;
	public int mult = 1;
	private final int frameTime = (int) (1000d / 10d);
	private final EventSubmitter<Integer[]> onResize = EventSubmitter.create();
	private final EventSubmitter<Float> onZoom = EventSubmitter.create();
	private AtomicBoolean exitRequested = new AtomicBoolean();
	private static final String KEYBOARD_CSS = """
			#keyboard {
			  width: 100%; text-align: center; user-select: none;
			  -webkit-user-select: none;
			}
			#keyboard .keyboard-row { height: 6vh; white-space: nowrap; }
			#keyboard button, #keyboard .keyboard-gap {
			  box-sizing: border-box; width: 5vh; height: 5vh; margin: .5vh;
			  display: inline-block; vertical-align: middle;
			}
			#keyboard button {
			  padding: 0; position: relative; overflow: hidden; touch-action: none;
			  font: bold 3.5vh/4.5vh sans-serif; text-align: center;
			}
			#keyboard button.fraction-template {
			  white-space: pre; font-size: 1.3vh !important; line-height: 1.1vh;
			}
			body.alpha:not(.shift) #keyboard button.fraction-template {
			  white-space: normal; font-size: 3.5vh !important; line-height: 4.5vh;
			}
			body.shift:not(.alpha) #keyboard button.fraction-template .shift {
			  white-space: pre; font-size: 1.3vh; line-height: 1.1vh;
			}
			#keyboard .alpha, #keyboard .shift {
			  position: absolute; top: 0; padding: .25vh; font-size: 50%;
			  line-height: 50%; pointer-events: none;
			}
			#keyboard .alpha { right: 0; color: #b71c1c; }
			#keyboard .shift { left: 0; color: #0d47a1; }
			body.alpha:not(.shift) #keyboard button[a],
			body.shift:not(.alpha) #keyboard button[s] { color: transparent; }
			body.alpha #keyboard #alphabtn {
			  color: #f44336; background: #fce4ec; outline: .2vh solid #f44336;
			}
			body.shift #keyboard #shiftbtn {
			  color: #2196f3; background: #e3f2fd; outline: .2vh solid #2196f3;
			}
			body.alpha:not(.shift) #keyboard .alpha,
			body.shift:not(.alpha) #keyboard .shift {
			  top: 0; left: 0; width: 100%; height: 100%; padding: 0;
			  font-size: 100%; line-height: 4.5vh; visibility: visible;
			}
			body.alpha:not(.shift) #keyboard .alpha { color: #f44336; }
			body.shift:not(.alpha) #keyboard .shift { color: #2196f3; }
			body:not(.shift) #keyboard .shift.hidden,
			body.alpha #keyboard .shift.hidden,
			body:not(.alpha) #keyboard .alpha.hidden,
			body.shift #keyboard .alpha.hidden { visibility: hidden; }
			""";

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
	@JSBody(params = { "ctx", "enabled" }, script = "" + "ctx.mozImageSmoothingEnabled = enabled;" + "ctx.oImageSmoothingEnabled = enabled;" + "ctx.webkitImageSmoothingEnabled = enabled;" + "ctx.msImageSmoothingEnabled = enabled;" + "ctx.imageSmoothingEnabled = enabled;")
	public static native void setImageSmoothingEnabled(CanvasRenderingContext2D ctx, boolean enabled);

	@Override
	public void create(final Runnable onInitialized) {
		width = -1;
		height = -1;
		canvas = (HTMLCanvasElement) HtmlEngine.document.createElement("canvas");
		g = (CanvasRenderingContext2D) canvas.getContext("2d");
		keyboardInput = Keyboard.createHostKeyboardInput();
		final HTMLInputElement keyInput = (HTMLInputElement) HtmlEngine.document.createElement("input");
		StaticVars.windowZoom.subscribe((zoom) -> {
			onZoom.submit(zoom);
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
				onResize.submit(new Integer[] {width, height});
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
			final String key = evt.getKey();
			if (keyboardInput.press(normalizeWebKeyCode(key, evt.getKeyCode()),
					webKeyChar(key), evt.isCtrlKey()))
				evt.preventDefault();
		});
		HtmlEngine.document.addEventListener("keyup", (final KeyboardEvent evt) -> {
			if (keyboardInput.release(normalizeWebKeyCode(evt.getKey(), evt.getKeyCode())))
				evt.preventDefault();
		});
		Window.current().addEventListener("blur", (final Event evt) -> keyboardInput.releaseAll());
		keyInput.addEventListener("input", (final Event evt) -> {
			evt.preventDefault();
			final String previousValue = this.previousValue;
			final String newValue = keyInput.getValue();
			final int prevLen = previousValue.length();
			final int newLen = newValue.length();
			this.previousValue = newValue;

			if (newLen > prevLen) {
				for (int i = prevLen; i < newLen; i++) {
					final char keyChar = newValue.charAt(i);
					final int keyCode = normalizeWebKeyCode(String.valueOf(keyChar), keyChar);
					keyboardInput.press(keyCode, keyChar, false);
					keyboardInput.release(keyCode);
				}
			} else if (newLen < prevLen) {
				for (int i = 0; i < prevLen - newLen; i++) {
					keyboardInput.press(KeyboardAWTValues.VK_BACK_SPACE,
							KeyboardAWTValues.CHAR_UNDEFINED, false);
					keyboardInput.release(KeyboardAWTValues.VK_BACK_SPACE);
				}
			}
		});
		canvas.addEventListener("click", (final Event evt) -> {
			keyInput.focus();
		});
		HtmlEngine.document.addEventListener("DOMContentLoaded", (final Event e) -> {
			keyInput.focus();
		});
		createHardwareKeyboard();
		renderer = new HtmlRenderer(this, g);
		initialized = true;
		if (onInitialized != null)
			onInitialized.run();
	}

	private static int normalizeWebKeyCode(final String key, final int legacyKeyCode) {
		if (key == null)
			return legacyKeyCode;
		return switch (key) {
			case "Enter", "NumpadEnter" -> KeyboardAWTValues.VK_ENTER;
			case "Backspace" -> KeyboardAWTValues.VK_BACK_SPACE;
			case "Delete", "Del" -> KeyboardAWTValues.VK_DELETE;
			case "Escape", "Esc" -> KeyboardAWTValues.VK_ESCAPE;
			case "Shift" -> KeyboardAWTValues.VK_SHIFT;
			case "Control" -> KeyboardAWTValues.VK_CONTROL;
			case "Alt" -> KeyboardAWTValues.VK_ALT;
			case "AltGraph" -> KeyboardAWTValues.VK_ALT_GRAPH;
			case "ArrowLeft", "Left" -> KeyboardAWTValues.VK_LEFT;
			case "ArrowRight", "Right" -> KeyboardAWTValues.VK_RIGHT;
			case "ArrowUp", "Up" -> KeyboardAWTValues.VK_UP;
			case "ArrowDown", "Down" -> KeyboardAWTValues.VK_DOWN;
			case "F1" -> KeyboardAWTValues.VK_F1;
			case "F2" -> KeyboardAWTValues.VK_F2;
			case "F3" -> KeyboardAWTValues.VK_F3;
			case "F4" -> KeyboardAWTValues.VK_F4;
			case "F5" -> KeyboardAWTValues.VK_F5;
			// Unlike event.key, this remains stable if Shift is released first.
			default -> legacyKeyCode;
		};
	}

	private static char webKeyChar(final String key) {
		return key != null && key.length() == 1
				? key.charAt(0) : KeyboardAWTValues.CHAR_UNDEFINED;
	}

	private void createHardwareKeyboard() {
		ensureKeyboardStyles();
		final HTMLElement keyboard = prepareKeyboardContainer();
		for (final PhysicalRow row : HardwareKeyboardLayout.rows()) {
			final HTMLElement rowElement = HtmlEngine.document.createElement("div");
			rowElement.setClassName("keyboard-row");
			for (int slot = 0; slot < row.slotCount(); slot++) {
				final PhysicalKey key = row.keyAtSlot(slot);
				if (key == null) {
					final HTMLElement gap = HtmlEngine.document.createElement("span");
					gap.setClassName("keyboard-gap");
					gap.setAttribute("aria-hidden", "true");
					rowElement.appendChild(gap);
				} else {
					rowElement.appendChild(createKeyboardButton(key));
				}
			}
			keyboard.appendChild(rowElement);
		}
	}

	private HTMLButtonElement createKeyboardButton(final PhysicalKey key) {
		final HTMLButtonElement button = (HTMLButtonElement) HtmlEngine.document.createElement("button");
		button.setType(HTMLButtonElement.TYPE_BUTTON);
		button.setInnerText(key.label(Layer.NORMAL));
		button.setAttribute("data-matrix-row", Integer.toString(key.matrixRow()));
		button.setAttribute("data-matrix-column", Integer.toString(key.matrixColumn()));
		if (key.normal() == Key.FRACTION)
			button.setClassName("fraction-template");
		final String title = keyTitle(key);
		button.setTitle(title);
		button.setAttribute("aria-label", title);
		if (key.normal() == Key.SHIFT)
			button.setId("shiftbtn");
		else if (key.normal() == Key.ALPHA)
			button.setId("alphabtn");

		appendLayerLabel(button, key, Layer.SHIFT, "shift", "s");
		appendLayerLabel(button, key, Layer.ALT_ALPHA, "alpha", "a");
		setKeyboardButtonFontSize(button, key);
		button.addEventListener("pointerdown", evt -> buttonEvent(evt, key, false));
		button.addEventListener("pointerup", evt -> buttonEvent(evt, key, true));
		button.addEventListener("pointercancel", evt -> buttonEvent(evt, key, true));
		button.addEventListener("pointerleave", evt -> buttonEvent(evt, key, true));
		return button;
	}

	private static void appendLayerLabel(final HTMLButtonElement button, final PhysicalKey key,
			final Layer layer, final String className, final String activeAttribute) {
		final Key mapping = key.mapping(layer);
		if (mapping == key.normal())
			return;
		button.setAttribute(activeAttribute, "");
		if (mapping == Key.NONE)
			return;

		final HTMLElement label = HtmlEngine.document.createElement("div");
		final boolean duplicatesNormalLabel = key.label(layer).equals(key.label(Layer.NORMAL));
		label.setClassName(className + (duplicatesNormalLabel ? " hidden" : ""));
		label.setInnerText(key.label(layer));
		button.appendChild(label);
	}

	private static void setKeyboardButtonFontSize(final HTMLButtonElement button, final PhysicalKey key) {
		final int longestLabel = Math.max(key.label(Layer.NORMAL).length(),
				Math.max(key.label(Layer.SHIFT).length(), key.label(Layer.ALT_ALPHA).length()));
		if (longestLabel >= 5)
			button.getStyle().setProperty("font-size", "1.2vh");
		else if (longestLabel >= 4)
			button.getStyle().setProperty("font-size", "1.5vh");
		else if (longestLabel >= 2)
			button.getStyle().setProperty("font-size", "2.5vh");
	}

	private static String keyTitle(final PhysicalKey key) {
		return key.switchLabel()
				+ " — N " + key.compactLabel(Layer.NORMAL)
				+ ", S " + key.compactLabel(Layer.SHIFT)
				+ ", A " + key.compactLabel(Layer.ALT_ALPHA);
	}

	private static void ensureKeyboardStyles() {
		if (HtmlEngine.document.getElementById("warppi-keyboard-style") != null)
			return;
		final HTMLElement style = HtmlEngine.document.createElement("style");
		style.setId("warppi-keyboard-style");
		style.setTextContent(KEYBOARD_CSS);
		HtmlEngine.document.getHead().appendChild(style);
	}

	@JSBody(script = """
			var keyboard = document.getElementById('keyboard');
			if (!keyboard) {
			  keyboard = document.createElement('div');
			  keyboard.id = 'keyboard';
			  document.body.appendChild(keyboard);
			}
			var legacy = document.querySelectorAll('body > button[keycode]');
			var breaks = [];
			for (var i = 0; i < legacy.length; i++) {
			  var next = legacy[i].nextSibling;
			  while (next && next.nodeType === 3 && !next.nodeValue.trim()) next = next.nextSibling;
			  if (next && next.tagName === 'BR') breaks.push(next);
			}
			for (var i = 0; i < legacy.length; i++) legacy[i].parentNode.removeChild(legacy[i]);
			for (var i = 0; i < breaks.length; i++)
			  if (breaks[i].parentNode) breaks[i].parentNode.removeChild(breaks[i]);
			keyboard.textContent = '';
			return keyboard;
			""")
	private static native HTMLElement prepareKeyboardContainer();

	private static void buttonEvent(final Event evt, final PhysicalKey key, final boolean released) {
		evt.preventDefault();
		Keyboard.keyRaw(key.matrixRow(), key.matrixColumn(), released);
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
		sendPowerOffSignal();
	}

	private void destroyEngine() {
		if (keyboardInput != null)
			keyboardInput.releaseAll();
		HtmlEngine.document.getBody().removeChild(canvas);
		initialized = false;
		exitRequested.set(true);
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
		WarpPI.getPlatform().setThreadName(th, "Canvas rendering thread");
		WarpPI.getPlatform().setThreadDaemon(th);
		th.start();
	}

	@Override
	public void repaint() {
		renderingLoop.refresh(false);
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


	public void subscribeExit(Runnable subscriber) {
		var thr = new Thread(() -> {
			try {
				while(!exitRequested.get()) {
					Thread.sleep(1000);
				}
			} catch (final InterruptedException e) {}
			subscriber.run();
		});
		WarpPI.getPlatform().setThreadDaemon(thr, true);
		thr.start();
	}

	@Override
	public boolean isSupported() {
		return WarpPI.getPlatform().isJavascript();
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}

	@Override
	public EventSubmitter<Integer[]> onResize() {
		return onResize;
	}

	public void sendPowerOffSignal() {
		destroyEngine();
	}
}
