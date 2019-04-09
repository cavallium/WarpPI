package it.cavallium.warppi.device.input;

import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.input.TouchInputDevice;
import it.cavallium.warppi.event.TouchCancelEvent;
import it.cavallium.warppi.event.TouchEndEvent;
import it.cavallium.warppi.event.TouchEvent;
import it.cavallium.warppi.event.TouchMoveEvent;
import it.cavallium.warppi.event.TouchPoint;
import it.cavallium.warppi.event.TouchStartEvent;
import it.cavallium.warppi.gui.graphicengine.impl.jogl.JOGLEngine;
import it.cavallium.warppi.gui.screens.Screen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PIHardwareTouchDevice implements TouchInputDevice {

	private final boolean invertXY, invertX, invertY;
	private final SubmissionPublisher<TouchEvent> touchEventPublisher = new SubmissionPublisher<>();
	private final JOGLEngine engine;
	private GLWindow window;
	public List<TouchPoint> touches = new ObjectArrayList<>();
	private long lastDraggedTime = 0;

	public PIHardwareTouchDevice(final boolean invertXY, final boolean invertX, final boolean invertY, JOGLEngine joglEngine) {
		this.invertXY = invertXY;
		this.invertX = invertX;
		this.invertY = invertY;
		this.engine = joglEngine;
	}

	@Override
	public void initialize() {
		this.window = engine.getGLWindow();

		window.addMouseListener(new MouseListener() {
			@Override
			public void mousePressed(final MouseEvent e) {
				final List<TouchPoint> newPoints = new ObjectArrayList<>();
				final List<TouchPoint> changedPoints = new ObjectArrayList<>();
				@SuppressWarnings("unused")
				final List<TouchPoint> oldPoints = touches;
				final int[] xs = e.getAllX();
				final int[] ys = e.getAllY();
				final float[] ps = e.getAllPressures();
				final short[] is = e.getAllPointerIDs();
				for (int i = 0; i < e.getPointerCount(); i++)
					newPoints.add(PIHardwareTouchDevice.this.makePoint(is[i], xs[i], ys[i], engine.getWidth(), engine.getHeight(), 5, 5, ps[i], 0));
				changedPoints.add(newPoints.get(0));
				touches = newPoints;
				PIHardwareTouchDevice.this.touchEventPublisher.submit(new TouchStartEvent(changedPoints, touches));
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				final List<TouchPoint> newPoints = new ObjectArrayList<>();
				final List<TouchPoint> changedPoints = new ObjectArrayList<>();
				@SuppressWarnings("unused")
				final List<TouchPoint> oldPoints = touches;
				final int[] xs = e.getAllX();
				final int[] ys = e.getAllY();
				final float[] ps = e.getAllPressures();
				final short[] is = e.getAllPointerIDs();
				for (int i = 0; i < e.getPointerCount(); i++)
					newPoints.add(PIHardwareTouchDevice.this.makePoint(is[i], xs[i], ys[i], engine.getWidth(), engine.getHeight(), 5, 5, ps[i], 0));
				changedPoints.add(newPoints.get(0));
				newPoints.remove(0);
				touches = newPoints;
				PIHardwareTouchDevice.this.touchEventPublisher.submit(new TouchEndEvent(changedPoints, touches));
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				final long curTime = System.currentTimeMillis();
				if (curTime - lastDraggedTime > 50) {
					lastDraggedTime = curTime;
					final List<TouchPoint> newPoints = new ObjectArrayList<>();
					final List<TouchPoint> changedPoints = new ObjectArrayList<>();
					final List<TouchPoint> oldPoints = touches;
					final int[] xs = e.getAllX();
					final int[] ys = e.getAllY();
					final float[] ps = e.getAllPressures();
					final short[] is = e.getAllPointerIDs();
					for (int i = 0; i < e.getPointerCount(); i++)
						newPoints.add(PIHardwareTouchDevice.this.makePoint(is[i], xs[i], ys[i], engine.getWidth(), engine.getHeight(), 5, 5, ps[i], 0));
					newPoints.forEach((newp) -> {
						oldPoints.forEach((oldp) -> {
							if (newp.getID() == oldp.getID())
								if (newp.equals(oldp) == false)
									changedPoints.add(newp);
						});
					});
					touches = newPoints;
					PIHardwareTouchDevice.this.touchEventPublisher.submit(new TouchMoveEvent(changedPoints, touches));
				}
			}

			@Override public void mouseClicked(final MouseEvent e) { }
			@Override public void mouseEntered(final MouseEvent e) { }
			@Override public void mouseExited(final MouseEvent e) { }
			@Override public void mouseMoved(final MouseEvent e) {}
			@Override public void mouseWheelMoved(final MouseEvent e) { }
		});
	}
	
	@Override
	public boolean getSwappedAxes() {
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
	public void listenTouchEvents(Subscriber<TouchEvent> touchEventListener) {
		touchEventPublisher.subscribe(touchEventListener);
	}
}
