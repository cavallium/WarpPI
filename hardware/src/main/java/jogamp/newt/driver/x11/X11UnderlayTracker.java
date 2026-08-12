/*
 * Hardware-only compatibility override for JogAmp's optional BCM X11 input
 * underlay. WarpPI renders directly through VideoCore and has no X server.
 */
package jogamp.newt.driver.x11;

import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;

import jogamp.newt.driver.KeyTracker;
import jogamp.newt.driver.MouseTracker;

/** Forces JOGL's BCM driver to select its direct Linux input fallback. */
public final class X11UnderlayTracker implements MouseTracker, KeyTracker {
	private static final String MESSAGE = "X11 underlay disabled for native Raspberry VideoCore mode";

	private X11UnderlayTracker() {
	}

	public static X11UnderlayTracker getSingleton() {
		throw new ExceptionInInitializerError(MESSAGE);
	}

	@Override
	public int getLastX() {
		return 0;
	}

	@Override
	public int getLastY() {
		return 0;
	}

	@Override
	public void windowResized(final WindowEvent event) {
	}

	@Override
	public void windowMoved(final WindowEvent event) {
	}

	@Override
	public void windowDestroyNotify(final WindowEvent event) {
	}

	@Override
	public void windowDestroyed(final WindowEvent event) {
	}

	@Override
	public void windowGainedFocus(final WindowEvent event) {
	}

	@Override
	public void windowLostFocus(final WindowEvent event) {
	}

	@Override
	public void windowRepaint(final WindowUpdateEvent event) {
	}
}
