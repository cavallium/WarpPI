package org.warp.picalculator.gui.graphicengine.html;

import java.io.IOException;

import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.teavm.jso.typedarrays.Uint8ClampedArray;
import org.warp.picalculator.StaticVars;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.cpu.CPUFont;

public class HtmlFont extends CPUFont {

	public HTMLCanvasElement imgEl;
	public CanvasRenderingContext2D imgElCtx;
	
	public HtmlFont(String fontName) throws IOException {
		super(fontName);
		HTMLDocument doc = Window.current().getDocument();
		imgEl = doc.createElement("canvas").cast();
		imgEl.setClassName("hidden");
		doc.getBody().appendChild(imgEl);
		imgElCtx = imgEl.getContext("2d").cast();
		imgEl.setWidth(this.charW);
		imgEl.setHeight(this.charH * intervalsTotalSize);

		int screenPos = 0;

		int currentInt;
		int currentIntBitPosition;
		int bitData;
		int j;
		imgElCtx.clearRect(0, 0, imgEl.getWidth(), imgEl.getHeight());
		imgElCtx.setFillStyle("#000");
		int minBound = 0, maxBound = intervalsTotalSize-1;
		System.out.println("charIndex:"+0);
		System.out.println("minBound"+minBound);
		System.out.println("maxBound"+maxBound);
		for (int charIndex = minBound; charIndex < maxBound; charIndex++) {
			for (int dy = 0; dy < charH; dy++) {
				for (int dx = 0; dx < charW; dx++) {
					j = dx;
					final int bit = dx + dy * charW;
					currentInt = (int) (Math.floor(bit) / (HtmlFont.intBits));
					currentIntBitPosition = bit - (currentInt * HtmlFont.intBits);
					final int charIdx = charIndex * charIntCount + currentInt;
					if (charIdx >= 0 && charIdx < chars32.length) {
						bitData = (chars32[charIdx] >> currentIntBitPosition) & 1;
						if (bitData == 1) {
							imgElCtx.fillRect( dx, charIndex*charH+dy, 1, 1 );
						}
					}
				}
			}
		}
	}
	
	@Override
	public void use(GraphicEngine d) {
		if (d.getRenderer() instanceof HtmlRenderer) {
			((HtmlRenderer) d.getRenderer()).f = this;
		}
	}

}
