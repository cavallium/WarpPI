package it.cavallium.warppi.device;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.event.TouchCancelEvent;
import it.cavallium.warppi.event.TouchEndEvent;
import it.cavallium.warppi.event.TouchMoveEvent;
import it.cavallium.warppi.event.TouchPoint;
import it.cavallium.warppi.event.TouchStartEvent;
import it.cavallium.warppi.gui.screens.Screen;

public class PIHardwareTouchDevice implements HardwareTouchDevice {

	private final boolean invertXY, invertX, invertY;

	public PIHardwareTouchDevice(boolean invertXY, boolean invertX, boolean invertY) {
		this.invertXY = invertXY;
		this.invertX = invertX;
		this.invertY = invertY;
	}

	@Override
	public boolean onTouchStart(TouchStartEvent e) {
		final Screen scr = Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
		boolean refresh = false;
		if (scr != null && scr.initialized && scr.onTouchStart(e)) {
			refresh = true;
		} else {
			//Default behavior
		}
		if (refresh) {
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().forceRefresh = true;
		}
		return true;
	}

	@Override
	public boolean onTouchEnd(TouchEndEvent e) {
		final Screen scr = Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
		boolean refresh = false;
		if (scr != null && scr.initialized && scr.onTouchEnd(e)) {
			refresh = true;
		} else {
			//Default behavior
		}
		if (refresh) {
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().forceRefresh = true;
		}
		return true;
	}

	@Override
	public boolean onTouchCancel(TouchCancelEvent e) {
		final Screen scr = Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
		boolean refresh = false;
		if (scr != null && scr.initialized && scr.onTouchCancel(e)) {
			refresh = true;
		} else {
			//Default behavior
		}
		if (refresh) {
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().forceRefresh = true;
		}
		return true;
	}

	@Override
	public boolean onTouchMove(TouchMoveEvent e) {
		final Screen scr = Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
		boolean refresh = false;
		if (scr != null && scr.initialized && scr.onTouchMove(e)) {
			refresh = true;
		} else {
			//Default behavior
		}
		if (refresh) {
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().forceRefresh = true;
		}
		return true;
	}

	@Override
	public boolean getInvertedXY() {
		return invertXY;
	}

	@Override
	public boolean getInvertedX() {
		return invertX;
	}

	@Override
	public boolean getInvertedY() {
		return invertY;
	}

	@Override
	public TouchPoint makePoint(long id, float x, float y, int screenWidth, int screenHeight, float radiusX,
			float radiusY, float force, float rotationAngle) {
		if (getInvertedXY()) {
			double oldX = x;
			double oldY = y;
			x = (float) (oldY * ((double) screenWidth) / ((double) screenHeight));
			y = (float) (oldX * ((double) screenHeight) / ((double) screenWidth));
		}
		if (getInvertedX()) {
			x = screenWidth - x;
		}
		if (getInvertedY()) {
			y = screenHeight - y;
		}
		return new TouchPoint(id, x, y, radiusX, radiusY, force, rotationAngle);
	}
}
