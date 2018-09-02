package org.warp.picalculator.gui.graphicengine.html;

import java.io.IOException;
import java.net.URISyntaxException;

import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.warp.picalculator.deps.StorageUtils;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.Skin;

import ar.com.hjg.pngj.PngReader;

public class HtmlSkin implements Skin {

	private String url;

	private int[] skinSize;

	private boolean initd;

	private HTMLImageElement imgEl;

	public HtmlSkin(String file) throws IOException {
		load(file);
	}

	public void use(GraphicEngine d) {
		if (d instanceof HtmlEngine) {
			if (!initd)
				initialize(d);
			((HtmlEngine) d).getRenderer().currentSkin = this;
		}
	}

	@Override
	public void load(String file) throws IOException {
		if (!file.startsWith("/")) 
			file = "/"+file;
			url = StorageUtils.basepath+file;
		try {
			PngReader r = new PngReader(StorageUtils.getResourceStream(file));
			skinSize = new int[] { r.imgInfo.cols, r.imgInfo.rows };
			r.close();
		} catch (URISyntaxException e) {
			IOException ex = new IOException();
			ex.initCause(e);
			throw ex;
		}
	}

	@Override
	public void initialize(GraphicEngine d) {
		HTMLDocument doc = Window.current().getDocument();
		imgEl = doc.createElement("img").cast();
		imgEl.setSrc(url);
		imgEl.setClassName("hidden");
		doc.getBody().appendChild(imgEl);
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
