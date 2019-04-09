package it.cavallium.warppi.device.input;

import java.util.function.Consumer;

import it.cavallium.warppi.event.TouchEvent;
import it.cavallium.warppi.event.TouchPoint;

public class NullTouchInputDevice implements TouchInputDevice {

	@Override
	public boolean getSwappedAxes() {
		return false;
	}

	@Override
	public boolean getInvertedX() {
		return false;
	}

	@Override
	public boolean getInvertedY() {
		return false;
	}

	@Override
	public void listenTouchEvents(Consumer<TouchEvent> touchEventListener) {
		
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public TouchPoint makePoint(long id, float x, float y, int maxX, int maxY, float radiusX, float radiusY,
			float force, float rotationAngle) {
		return new TouchPoint(id, maxX, maxY, radiusX, radiusY, force, rotationAngle);
	}

}
