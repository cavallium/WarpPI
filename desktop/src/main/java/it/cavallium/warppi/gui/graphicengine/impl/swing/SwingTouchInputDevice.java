package it.cavallium.warppi.gui.graphicengine.impl.swing;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.input.TouchInputDevice;
import it.cavallium.warppi.event.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

public class SwingTouchInputDevice implements TouchInputDevice {
    private final SubmissionPublisher<TouchEvent> touchEventPublisher = new SubmissionPublisher<>();
    private final SwingEngine graphicEngine;

    public SwingTouchInputDevice(SwingEngine graphicEngine) {
        this.graphicEngine = graphicEngine;
    }

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
        touchEventPublisher.consume(touchEventListener);
    }

    @Override
    public void initialize() {

        graphicEngine.subscribeTouchDevice(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                final Insets wp = graphicEngine.getInsets();
                final TouchPoint p = new TouchPoint(0, e.getX() - wp.left, e.getY() - wp.top, 5, 5, 1, 0);
                final ObjectArrayList<TouchPoint> touches = new ObjectArrayList<>();
                final ObjectArrayList<TouchPoint> changedTouches = new ObjectArrayList<>();
                touches.add(p);
                changedTouches.add(p);
                final TouchMoveEvent tse = new TouchMoveEvent(changedTouches, touches);

                SwingTouchInputDevice.this.touchEventPublisher.submit(tse);
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        }, new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                final Insets wp = graphicEngine.getInsets();
                final TouchPoint p = new TouchPoint(0, e.getX() - wp.left, e.getY() - wp.top, 5, 5, 1, 0);
                final ObjectArrayList<TouchPoint> touches = new ObjectArrayList<>();
                final ObjectArrayList<TouchPoint> changedTouches = new ObjectArrayList<>();
                touches.add(p);
                changedTouches.add(p);
                final TouchStartEvent tse = new TouchStartEvent(changedTouches, touches);

                SwingTouchInputDevice.this.touchEventPublisher.submit(tse);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                final Insets wp = graphicEngine.getInsets();
                final TouchPoint p = new TouchPoint(0, e.getX() - wp.left, e.getY() - wp.top, 5, 5, 1, 0);
                final ObjectArrayList<TouchPoint> touches = new ObjectArrayList<>();
                final ObjectArrayList<TouchPoint> changedTouches = new ObjectArrayList<>();
                changedTouches.add(p);
                final TouchEndEvent tse = new TouchEndEvent(changedTouches, touches);

                SwingTouchInputDevice.this.touchEventPublisher.submit(tse);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }
}
