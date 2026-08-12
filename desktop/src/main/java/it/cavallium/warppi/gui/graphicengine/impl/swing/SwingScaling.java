package it.cavallium.warppi.gui.graphicengine.impl.swing;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;

/**
 * Converts calculator-logical dimensions to Swing user units.
 *
 * <p>Java2D normally applies the monitor scale through the graphics-device
 * transform. Some KDE Wayland/XWayland sessions expose a 1x transform despite
 * a larger desktop scale; {@code toolkitCompensation} supplies only that
 * missing part. Calculator zoom remains a separate factor.</p>
 */
final class SwingScaling {
	private SwingScaling() {}

	static float normalizeCalculatorZoom(final Float calculatorZoom) {
		return calculatorZoom != null && Float.isFinite(calculatorZoom) && calculatorZoom > 0
				? calculatorZoom : 1f;
	}

	static int toUserUnits(final int logicalUnits, final float toolkitCompensation) {
		return Math.max(1, Math.round(Math.max(1, logicalUnits)
				* requireScale(toolkitCompensation, "toolkit compensation")));
	}

	static int toLogicalUnits(final int userUnits, final float calculatorZoom,
			final float toolkitCompensation) {
		return Math.max(1, Math.round(Math.max(1, userUnits)
				/ (requireCalculatorZoom(calculatorZoom) * requireScale(toolkitCompensation, "toolkit compensation"))));
	}

	static Dimension toUserSize(final int logicalWidth, final int logicalHeight,
			final float toolkitCompensation) {
		return new Dimension(toUserUnits(logicalWidth, toolkitCompensation),
				toUserUnits(logicalHeight, toolkitCompensation));
	}

	static float toolkitScale(final GraphicsConfiguration configuration) {
		if (configuration == null)
			return 1f;
		final var transform = configuration.getDefaultTransform();
		return (float) Math.max(transform.getScaleX(), transform.getScaleY());
	}

	static float toolkitCompensation(final float desktopScale, final float toolkitScale) {
		final float checkedToolkitScale = requireScale(toolkitScale, "Swing toolkit scale");
		return Math.max(requireScale(desktopScale, "desktop scale"), checkedToolkitScale)
				/ checkedToolkitScale;
	}

	static double effectivePhysicalScale(final float calculatorZoom,
			final float toolkitCompensation, final double toolkitScale) {
		if (!Double.isFinite(toolkitScale) || toolkitScale <= 0)
			throw new IllegalArgumentException("Invalid Swing toolkit scale: " + toolkitScale);
		return requireCalculatorZoom(calculatorZoom)
				* requireScale(toolkitCompensation, "toolkit compensation") * toolkitScale;
	}

	private static float requireCalculatorZoom(final float calculatorZoom) {
		if (!Float.isFinite(calculatorZoom) || calculatorZoom <= 0)
			throw new IllegalArgumentException("Invalid calculator zoom: " + calculatorZoom);
		return calculatorZoom;
	}

	private static float requireScale(final float scale, final String name) {
		if (!Float.isFinite(scale) || scale <= 0)
			throw new IllegalArgumentException("Invalid " + name + ": " + scale);
		return scale;
	}
}
