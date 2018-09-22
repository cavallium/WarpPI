package it.cavallium.warppi.device;

import it.cavallium.warppi.event.TouchEventListener;
import it.cavallium.warppi.event.TouchPoint;

public interface HardwareTouchDevice extends TouchEventListener {
	boolean getInvertedXY();

	boolean getInvertedX();

	boolean getInvertedY();

	default void setInvertedXY() {}

	default void setInvertedX() {}

	default void setInvertedY() {}

	TouchPoint makePoint(long id, float x, float y, int maxX, int maxY, float radiusX, float radiusY, float force,
			float rotationAngle);
}
