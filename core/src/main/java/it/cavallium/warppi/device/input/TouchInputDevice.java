package it.cavallium.warppi.device.input;

import java.util.concurrent.Flow.Subscriber;
import java.util.function.Consumer;

import it.cavallium.warppi.event.TouchEvent;
import it.cavallium.warppi.event.TouchEventListener;
import it.cavallium.warppi.event.TouchPoint;

public interface TouchInputDevice {
	boolean getSwappedAxes();

	boolean getInvertedX();

	boolean getInvertedY();

	default void setInvertedXY() {}

	default void setInvertedX() {}

	default void setInvertedY() {}

	void listenTouchEvents(Consumer<TouchEvent> touchEventListener);
	
	default TouchPoint makePoint(final long id, float x, float y, final int screenWidth, final int screenHeight,
			final float radiusX, final float radiusY, final float force, final float rotationAngle) {
		if (getSwappedAxes()) {
			final double oldX = x;
			final double oldY = y;
			x = (float) (oldY * screenWidth / screenHeight);
			y = (float) (oldX * screenHeight / screenWidth);
		}
		if (getInvertedX()) {
			x = screenWidth - x;
		}
		if (getInvertedY()) {
			y = screenHeight - y;
		}
		return new TouchPoint(id, x, y, radiusX, radiusY, force, rotationAngle);
	}

	void initialize();
}
