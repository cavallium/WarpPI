package it.cavallium.warppi.desktop;

import java.awt.GraphicsEnvironment;

/** Shares Java2D's resolved desktop scale with every desktop rendering engine. */
final class DesktopDisplayScale {
	private DesktopDisplayScale() {}

	static float detect() {
		final float scale;
		if (GraphicsEnvironment.isHeadless()) {
			scale = 1f;
		} else {
			final var transform = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
			scale = (float) Math.max(transform.getScaleX(), transform.getScaleY());
		}
		final float validScale = Float.isFinite(scale) && scale > 0 ? scale : 1f;
		System.out.println("Desktop UI scale reported by Java2D: " + validScale);
		return validScale;
	}
}
