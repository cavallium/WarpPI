package it.cavallium.warppi.gui;

public class GraphicUtils {
	public static final float sin(final float rad) {
		return GraphicUtils.sin[(int) (rad * GraphicUtils.radToIndex) & GraphicUtils.SIN_MASK];
	}

	public static final float cos(final float rad) {
		return GraphicUtils.cos[(int) (rad * GraphicUtils.radToIndex) & GraphicUtils.SIN_MASK];
	}

	public static final float sinDeg(final float deg) {
		return GraphicUtils.sin[(int) (deg * GraphicUtils.degToIndex) & GraphicUtils.SIN_MASK];
	}

	public static final float cosDeg(final float deg) {
		return GraphicUtils.cos[(int) (deg * GraphicUtils.degToIndex) & GraphicUtils.SIN_MASK];
	}

	@SuppressWarnings("unused")
	private static final float RAD;
	@SuppressWarnings("unused")
	private static final float DEG;
	private static final int SIN_BITS;
	private static final int SIN_MASK;
	private static final int SIN_COUNT;
	private static final float radFull;
	private static final float radToIndex;
	private static final float degFull;
	private static final float degToIndex;
	private static final float[] sin;
	private static final float[] cos;

	static {
		RAD = (float) Math.PI / 180.0f;
		DEG = 180.0f / (float) Math.PI;

		SIN_BITS = 8;
		SIN_MASK = ~(-1 << GraphicUtils.SIN_BITS);
		SIN_COUNT = GraphicUtils.SIN_MASK + 1;

		radFull = (float) (Math.PI * 2.0);
		degFull = (float) 360.0;
		radToIndex = GraphicUtils.SIN_COUNT / GraphicUtils.radFull;
		degToIndex = GraphicUtils.SIN_COUNT / GraphicUtils.degFull;

		sin = new float[GraphicUtils.SIN_COUNT];
		cos = new float[GraphicUtils.SIN_COUNT];

		for (int i = 0; i < GraphicUtils.SIN_COUNT; i++) {
			GraphicUtils.sin[i] = (float) Math.sin((i + 0.5f) / GraphicUtils.SIN_COUNT * GraphicUtils.radFull);
			GraphicUtils.cos[i] = (float) Math.cos((i + 0.5f) / GraphicUtils.SIN_COUNT * GraphicUtils.radFull);
		}

		// Four cardinal directions (credits: Nate)
		for (int i = 0; i < 360; i += 90) {
			GraphicUtils.sin[(int) (i * GraphicUtils.degToIndex) & GraphicUtils.SIN_MASK] = (float) Math.sin(i * Math.PI / 180.0);
			GraphicUtils.cos[(int) (i * GraphicUtils.degToIndex) & GraphicUtils.SIN_MASK] = (float) Math.cos(i * Math.PI / 180.0);
		}
	}
}
