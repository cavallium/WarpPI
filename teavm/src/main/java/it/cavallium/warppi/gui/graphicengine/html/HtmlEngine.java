package it.cavallium.warppi.gui.graphicengine.html;

import java.io.IOException;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasGradient;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.EventTarget;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLButtonElement;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;
import org.teavm.jso.dom.xml.NodeList;
import org.teavm.jso.json.JSON;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Utils;
import it.cavallium.warppi.deps.Platform.Semaphore;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.flow.BehaviorSubject;
import it.cavallium.warppi.flow.Observable;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.Skin;

public class HtmlEngine implements GraphicEngine {

	private boolean initialized;
	public Semaphore exitSemaphore = Engine.getPlatform().newSemaphore(0);
	private static final HTMLDocument document = Window.current().getDocument();
	private HTMLCanvasElement canvas;
	private CanvasRenderingContext2D g;
	private RenderingLoop renderingLoop;
	private HtmlRenderer renderer;
	private int width = -1, height = -1;
	private final int frameTime = (int) (1000d/10d);
	private final BehaviorSubject<Integer[]> onResize = BehaviorSubject.create();
	
	@Override
	public int[] getSize() {
		return new int[] { getWidth(), getHeight() };
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setTitle(String title) {
		HtmlEngine.setHTMLTitle(title);
	}

	@JSBody(params = {"wndTitle"}, script = "document.title = wndTitle")
	private static native void setHTMLTitle(String wndTitle);
	
	@Override
	public void setResizable(boolean r) {}

	@Override
	public void setDisplayMode(int ww, int wh) {
		canvas.setWidth(ww);
		width = ww;
		canvas.setHeight(wh);
		height = wh;
	}

	private String previousValue="";
	
	@Override
	public void create(Runnable onInitialized) {
		canvas = (HTMLCanvasElement) document.createElement("canvas");
		g = (CanvasRenderingContext2D ) canvas.getContext("2d");
		HTMLInputElement keyInput = (HTMLInputElement) document.createElement("input");
		keyInput.setType("text");
		keyInput.getStyle().setProperty("opacity", "0.1");
		setDisplayMode(480, 320);
		document.getElementById("container").appendChild(canvas);
		document.getBody().appendChild(keyInput);
		keyInput.setTabIndex(0);
		keyInput.addEventListener("keydown", (KeyboardEvent evt) -> {
			evt.preventDefault();
			new Thread(() -> {
				previousValue = keyInput.getValue();
				Keyboard.debugKeyPressed(evt.getKeyCode());
				System.out.println(evt.getKeyCode());
				System.out.println(""+(int) evt.getKey().charAt(0));
			}).start();
		});
		keyInput.addEventListener("input", (Event evt) -> {
			evt.preventDefault();
			final String previousValue = this.previousValue;
			final String newValue = keyInput.getValue();
			final int prevLen = previousValue.length();
			final int newLen = newValue.length();

			new Thread(() -> {
				if (newLen == prevLen) {
					
				} else if (newLen - prevLen == 1) {
					Keyboard.debugKeyPressed((int) newValue.toUpperCase().charAt(newLen-1));
				} else if (newLen - prevLen > 1) {
					for (int i = 0; i < newLen - prevLen; i++) {
						Keyboard.debugKeyPressed((int) newValue.toUpperCase().charAt(prevLen + i));
					}
				} else if (newLen - prevLen < 1) {
					for (int i = 0; i < prevLen - newLen; i++) {
						Keyboard.debugKeyPressed(8);
					}
				}
			}).start();
		});
		canvas.addEventListener("click", (Event evt) -> {
			keyInput.focus();
		});
		document.addEventListener("DOMContentLoaded", (Event e) -> {
			keyInput.focus();
		});
		NodeList<? extends HTMLElement> buttons = document.getBody().getElementsByTagName("button");
		for (int i = 0; i < buttons.getLength(); i++) {
			if (buttons.item(i).hasAttribute("keycode")) {
				buttons.item(i).addEventListener("click", (Event evt) -> {
					evt.preventDefault();
					EventTarget target = evt.getCurrentTarget();
					HTMLButtonElement button = target.cast();
					new Thread(() -> {
						try {
							if (button.hasAttribute("keycode") && button.getAttribute("keycode").contains(",")) {
								String code = button.getAttribute("keycode");
								String[] coordinates = code.split(",", 2);
								boolean removeshift = Keyboard.shift && Integer.parseInt(coordinates[0]) != 0 && Integer.parseInt(coordinates[1]) != 0;
								boolean removealpha = Keyboard.alpha && Integer.parseInt(coordinates[0]) != 0 && Integer.parseInt(coordinates[1]) != 1;
								Keyboard.keyPressedRaw(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
								if (removeshift) {
									Keyboard.keyPressedRaw(0,0);
								}
								if (removealpha) {
									Keyboard.keyPressedRaw(0,1);
								}
								Thread.sleep(100);
								Keyboard.keyReleasedRaw(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
								if (removeshift) {
									Keyboard.keyReleasedRaw(0,0);
								}
								if (removealpha) {
									Keyboard.keyReleasedRaw(0,1);
								}
							} else {
								if (Keyboard.alpha && !Keyboard.shift) {
									if (button.hasAttribute("keycodea")) {
										Keyboard.debugKeyPressed(Integer.parseInt(button.getAttribute("keycodea")));
									} else {
										Keyboard.debugKeyPressed(Integer.parseInt(button.getAttribute("keycode")));
									}
								} else if (!Keyboard.alpha && Keyboard.shift) {
									if (button.hasAttribute("keycodes")) {
										Keyboard.debugKeyPressed(Integer.parseInt(button.getAttribute("keycodes")));
									} else {
										Keyboard.debugKeyPressed(Integer.parseInt(button.getAttribute("keycode")));
									}
								} else if (Keyboard.alpha && Keyboard.shift) {
									if (button.hasAttribute("keycodesa")) {
										Keyboard.debugKeyPressed(Integer.parseInt(button.getAttribute("keycodesa")));
									} else {
										if (button.hasAttribute("keycodes")) {
											Keyboard.debugKeyPressed(Integer.parseInt(button.getAttribute("keycodes")));
										} else {
											Keyboard.debugKeyPressed(Integer.parseInt(button.getAttribute("keycode")));
										}
									}
								} else {
									Keyboard.debugKeyPressed(Integer.parseInt(button.getAttribute("keycode")));
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}).start();
				});
			}
		}
		renderer = new HtmlRenderer(this, g);
		initialized = true;
		if (onInitialized != null) {
			onInitialized.run();
		}
	}

	@Override
	public int getWidth() {
		if (width == -1) {
			width = canvas.getWidth();
		}
		return width;
	}

	@Override
	public int getHeight() {
		if (height == -1) {
			height = canvas.getHeight();
		}
		return height;
	}

	@Override
	public void destroy() {
		document.getBody().removeChild(canvas);
		initialized = false;
		exitSemaphore.release();
	}

	@Override
	public void start(RenderingLoop d) {
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
					} else {
						extratime += delta - frameTime;
					}
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
	public HtmlFont loadFont(String fontName) throws IOException {
		return new HtmlFont(fontName);
	}

	@Override
	public HtmlFont loadFont(String path, String fontName) throws IOException {
		return new HtmlFont(fontName);
	}

	@Override
	public HtmlSkin loadSkin(String file) throws IOException {
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