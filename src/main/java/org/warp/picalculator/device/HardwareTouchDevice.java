package org.warp.picalculator.device;

import org.warp.picalculator.event.TouchEventListener;
import org.warp.picalculator.event.TouchPoint;

public interface HardwareTouchDevice extends TouchEventListener {
	public boolean getInvertedXY();

	public boolean getInvertedX();

	public boolean getInvertedY();

	public default void setInvertedXY() {}

	public default void setInvertedX() {}

	public default void setInvertedY() {}

	public TouchPoint makePoint(long id, float x, float y, int maxX, int maxY, float radiusX, float radiusY,
			float force, float rotationAngle);
}
