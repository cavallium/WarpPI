package it.cavallium.warppi.device.input;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.event.TouchCancelEvent;
import it.cavallium.warppi.event.TouchEndEvent;
import it.cavallium.warppi.event.TouchMoveEvent;
import it.cavallium.warppi.event.TouchPoint;
import it.cavallium.warppi.event.TouchStartEvent;
import it.cavallium.warppi.gui.screens.Screen;

public class PIHardwareTouchDevice implements TouchInputDevice {

	private final boolean invertXY, invertX, invertY;

	public PIHardwareTouchDevice(final boolean invertXY, final boolean invertX, final boolean invertY) {
		this.invertXY = invertXY;
		this.invertX = invertX;
		this.invertY = invertY;
	}

	@Override
	public boolean onTouchStart(final TouchStartEvent e) {
		final Screen scr = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
		boolean refresh = false;
		if (scr != null && scr.initialized && scr.onTouchStart(e)) {
			refresh = true;
		} else {
			//Default behavior
		}
		if (refresh) {
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().forceRefresh = true;
		}
		return true;
	}

	@Override
	public boolean onTouchEnd(final TouchEndEvent e) {
		final Screen scr = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
		boolean refresh = false;
		if (scr != null && scr.initialized && scr.onTouchEnd(e)) {
			refresh = true;
		} else {
			//Default behavior
		}
		if (refresh) {
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().forceRefresh = true;
		}
		return true;
	}

	@Override
	public boolean onTouchCancel(final TouchCancelEvent e) {
		final Screen scr = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
		boolean refresh = false;
		if (scr != null && scr.initialized && scr.onTouchCancel(e)) {
			refresh = true;
		} else {
			//Default behavior
		}
		if (refresh) {
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().forceRefresh = true;
		}
		return true;
	}

	@Override
	public boolean onTouchMove(final TouchMoveEvent e) {
		final Screen scr = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
		boolean refresh = false;
		if (scr != null && scr.initialized && scr.onTouchMove(e)) {
			refresh = true;
		} else {
			//Default behavior
		}
		if (refresh) {
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().forceRefresh = true;
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
	public TouchPoint makePoint(final long id, float x, float y, final int screenWidth, final int screenHeight,
			final float radiusX, final float radiusY, final float force, final float rotationAngle) {
		if (getInvertedXY()) {
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
}
