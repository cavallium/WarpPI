package it.cavallium.warppi.gui.graphicengine.html;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.Platform.Semaphore;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.flow.BehaviorSubject;
import it.cavallium.warppi.flow.SimpleSubject;
import it.cavallium.warppi.flow.Subject;
import it.cavallium.warppi.flow.ValueReference;
import it.cavallium.warppi.gui.graphicengine.Skin;

public class HtmlSkin implements Skin {

	private String url;

	private int[] skinSize;

	private boolean initd;

	private HTMLImageElement imgEl;

	public HtmlSkin(final String file) throws IOException {
		load(file);
	}

	@Override
	public void use(final DisplayOutputDevice d) {
		if (d instanceof HtmlEngine) {
			if (!initd)
				initialize(d);
			((HtmlEngine) d).getRenderer().currentSkin = this;
		}
	}

	@Override
	public void load(String file) throws IOException {
		url = WarpPI.getPlatform().getStorageUtils().getBasePath() + (!file.startsWith("/") ? "/" : "") + file;
	}

	@Override
	public void initialize(final DisplayOutputDevice d) {
		final HTMLDocument doc = Window.current().getDocument();
		ValueReference<Boolean> done = new ValueReference<Boolean>(false);
		imgEl = doc.createElement("img").cast();
		imgEl.addEventListener("load", (Event e) -> {
			done.value = true;
		});
		imgEl.setSrc(url);
		while (!done.value) {
			try {Thread.sleep(15);} catch (Exception e) {}
		}
		skinSize = new int[] { imgEl.getNaturalWidth(), imgEl.getNaturalHeight() };
		initd = true;
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

	public final HTMLImageElement getImgElement() {
		return imgEl;
	}
}