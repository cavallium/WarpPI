package it.cavallium.warppi.gui.graphicengine.html;

import java.io.IOException;

import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.impl.common.RFTFont;

public class HtmlFont extends RFTFont {

	public HTMLCanvasElement imgEl;
	public CanvasRenderingContext2D imgElCtx;

	public HtmlFont(final String fontName) throws IOException {
		super(fontName);
		final HTMLDocument doc = Window.current().getDocument();
		imgEl = doc.createElement("canvas").cast();
		imgEl.setClassName("hidden");
		doc.getBody().appendChild(imgEl);
		imgElCtx = imgEl.getContext("2d").cast();
		imgEl.setWidth(charW);
		imgEl.setHeight(charH * intervalsTotalSize);

		int currentInt;
		int currentIntBitPosition;
		int bitData;
		imgElCtx.clearRect(0, 0, imgEl.getWidth(), imgEl.getHeight());
		imgElCtx.setFillStyle("#000");
		final int minBound = 0, maxBound = intervalsTotalSize - 1;
		for (int charIndex = minBound; charIndex < maxBound; charIndex++)
			for (int dy = 0; dy < charH; dy++)
				for (int dx = 0; dx < charW; dx++) {
					final int bit = dx + dy * charW;
					currentInt = (int) (Math.floor(bit) / RFTFont.intBits);
					currentIntBitPosition = bit - currentInt * RFTFont.intBits;
					final int charIdx = charIndex * charIntCount + currentInt;
					if (charIdx >= 0 && charIdx < chars32.length) {
						bitData = chars32[charIdx] >> currentIntBitPosition & 1;
						if (bitData == 1)
							imgElCtx.fillRect(dx, charIndex * charH + dy, 1, 1);
					}
				}
	}

	@Override
	public boolean isInitialized() {
		return super.isInitialized();
	}

	@Override
	public void use(final DisplayOutputDevice d) {
			((HtmlEngine) d.getGraphicEngine()).getRenderer().f = this;
	}

}