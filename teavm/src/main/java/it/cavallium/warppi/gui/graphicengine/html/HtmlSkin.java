package it.cavallium.warppi.gui.graphicengine.html;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import it.cavallium.warppi.teavm.TeaVMStorageUtils;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.Skin;

public class HtmlSkin implements Skin {

	private String url;

	private int[] skinSize;

	private boolean initd;
	private CanvasImageSource imgEl;

	public HtmlSkin(final String file) throws IOException {
		load(file);
	}

	@Override
	public void use(final DisplayOutputDevice d) {
		if (!initd)
			initialize(d);
		((HtmlEngine) d.getGraphicEngine()).getRenderer().currentSkin = this;
	}

	@Override
	public void load(String file) throws IOException {
		File path = new File(WarpPI.getPlatform().getPlatformStorage().getRootPath(), file);
		this.url = TeaVMStorageUtils.getUrl(path).toString();
	}

	@Override
	public void initialize(final DisplayOutputDevice d) {
		final HTMLDocument doc = Window.current().getDocument();
		AtomicBoolean done = new AtomicBoolean(false);

		loadImage(url, (width, height, imageSource) -> {
			skinSize = new int[] {width, height};
			this.imgEl = imageSource;
			done.set(true);
		});

		while (!done.get()) {
			try {Thread.sleep(15);} catch (Exception e) {}
		}
		initd = true;
	}

	@JSBody(params = { "url", "loadHandler" }, script = "var image = new Image(); image.onload = function(evt) { loadHandler(image.naturalWidth, image.naturalHeight, image);}; image.src = url;")
	public static native void loadImage(String url, LoadHandler loadHandler);

	@JSFunctor
	public interface LoadHandler extends JSObject {
		void onLoad(int width, int height, CanvasImageSource imageSource);
	}

	@Override
	public boolean isInitialized() {
		return initd;
	}

	@Override
	public int getSkinWidth() {
		return skinSize[0];
	}

	@Override
	public int getSkinHeight() {
		return skinSize[1];
	}

	public final String getUrl() {
		return url;
	}

	public final CanvasImageSource getImgElement() {
		return imgEl;
	}
}