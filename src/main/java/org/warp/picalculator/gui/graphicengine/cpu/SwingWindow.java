package org.warp.picalculator.gui.graphicengine.cpu;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.warp.picalculator.ConsoleUtils;
import org.warp.picalculator.PlatformUtils;
import org.warp.picalculator.StaticVars;
import org.warp.picalculator.Utils;
import org.warp.picalculator.device.HardwareDevice;
import org.warp.picalculator.device.Keyboard;
import org.warp.picalculator.event.TouchEndEvent;
import org.warp.picalculator.event.TouchMoveEvent;
import org.warp.picalculator.event.TouchPoint;
import org.warp.picalculator.event.TouchStartEvent;
import org.warp.picalculator.gui.DisplayManager;
import org.warp.picalculator.gui.graphicengine.RenderingLoop;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SwingWindow extends JFrame {
	private static final long serialVersionUID = 2945898937634075491L;
	public CustomCanvas c;
	private RenderingLoop renderingLoop;
	private final CPUEngine display;
	private int mult = 1;
	private BehaviorSubject<Integer[]> onResize;
	private Observable<Integer[]> onResize$;

	public SwingWindow(CPUEngine disp) {
		display = disp;
		c = new CustomCanvas();
		c.setDoubleBuffered(false);
		this.add(c);
//		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		Toolkit.getDefaultToolkit().setDynamicLayout(false);
		// Transparent 16 x 16 pixel cursor image.
		final BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		mult = StaticVars.windowZoomValue.next().intValue();
		if (StaticVars.debugOn) {
			if (Utils.debugThirdScreen) {
				this.setLocation(2880, 900);
				setResizable(false);
				setAlwaysOnTop(true);
			}
		} else {
			// Create a new blank cursor.
			final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

			// Set the blank cursor to the JFrame.
			getContentPane().setCursor(blankCursor);

			setResizable(false);
		}

		setTitle("WarpPI Calculator by Andrea Cavalli (@Cavallium)");

		onResize = BehaviorSubject.create();
		onResize$ = onResize.doOnNext((newSize) -> {
			disp.r.size = new int[] { newSize[0], newSize[1] };
			if (disp.r.size[0] <= 0) {
				disp.r.size[0] = 1;
			}
			if (disp.r.size[1] <= 0) {
				disp.r.size[1] = 1;
			}
			CPURenderer.canvas2d = new int[disp.r.size[0] * disp.r.size[1]];
			disp.g = new BufferedImage(disp.r.size[0], disp.r.size[1], BufferedImage.TYPE_INT_RGB);
		});
		
		addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
				HardwareDevice.INSTANCE.getDisplayManager().engine.destroy();
			}

			@Override
			public void componentMoved(ComponentEvent e) {}

			@Override
			public void componentResized(ComponentEvent e) {
				onResize.onNext(new Integer[] {getWidth(), getHeight()});
			}

			@Override
			public void componentShown(ComponentEvent e) {
				if (StaticVars.debugOn && !Utils.debugThirdScreen) {
					SwingWindow.this.centerWindow();
				}
			}
		});
		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				Keyboard.debugKeyCode = arg0.getKeyCode();
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				Keyboard.debugKeyCodeRelease = arg0.getKeyCode();
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				final Insets wp = SwingWindow.this.getInsets();
				TouchPoint p = new TouchPoint(0, e.getX() - wp.left, e.getY() - wp.top, 5, 5, 1, 0);
				ObjectArrayList<TouchPoint> touches = new ObjectArrayList<>();
				ObjectArrayList<TouchPoint> changedTouches = new ObjectArrayList<>();
				touches.add(p);
				changedTouches.add(p);
				TouchMoveEvent tse = new TouchMoveEvent(changedTouches, touches);
				HardwareDevice.INSTANCE.getInputManager().getTouchDevice().onTouchMove(tse);
			}

			@Override
			public void mouseMoved(MouseEvent e) {}
		});
		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				final Insets wp = SwingWindow.this.getInsets();
				TouchPoint p = new TouchPoint(0, e.getX() - wp.left, e.getY() - wp.top, 5, 5, 1, 0);
				ObjectArrayList<TouchPoint> touches = new ObjectArrayList<>();
				ObjectArrayList<TouchPoint> changedTouches = new ObjectArrayList<>();
				touches.add(p);
				changedTouches.add(p);
				TouchStartEvent tse = new TouchStartEvent(changedTouches, touches);
				HardwareDevice.INSTANCE.getInputManager().getTouchDevice().onTouchStart(tse);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				final Insets wp = SwingWindow.this.getInsets();
				TouchPoint p = new TouchPoint(0, e.getX() - wp.left, e.getY() - wp.top, 5, 5, 1, 0);
				ObjectArrayList<TouchPoint> touches = new ObjectArrayList<>();
				ObjectArrayList<TouchPoint> changedTouches = new ObjectArrayList<>();
				changedTouches.add(p);
				TouchEndEvent tse = new TouchEndEvent(changedTouches, touches);
				HardwareDevice.INSTANCE.getInputManager().getTouchDevice().onTouchEnd(tse);
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		});
		StaticVars.windowZoom$.subscribe((newZoomValue) -> {
			if (newZoomValue != mult) {
				mult = (int) newZoomValue.floatValue();
				this.onResize.onNext(new Integer[] {getWidth(), getHeight()});
				ConsoleUtils.out.println(3, "Engine", "CPU", "Zoom changed");
			}
		});
	}
	

	public Observable<Integer[]> onResize() {
		return onResize$;
	}

	@Override
	public void setSize(int width, int height) {
		c.setSize(new Dimension(width * mult, height * mult));
		c.setPreferredSize(new Dimension(width * mult, height * mult));
		super.getContentPane().setSize(new Dimension(width * mult, height * mult));
		super.getContentPane().setPreferredSize(new Dimension(width * mult, height * mult));
		super.pack();
	}

	@Override
	public Dimension getSize() {
		return new Dimension(getWidth(), getHeight());
	}

	@Override
	public int getWidth() {
		return c.getWidth() / mult;
	}

	@Override
	public int getHeight() {
		return c.getHeight() / mult;
	}

	public void setRenderingLoop(RenderingLoop renderingLoop) {
		this.renderingLoop = renderingLoop;
	}

	public void centerWindow() {
		final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		final int x = (int) ((dimension.getWidth() - super.getWidth()) / 2);
		final int y = (int) ((dimension.getHeight() - super.getHeight()) / 2);
		super.setLocation(x, y);
	}

//	private static ObjectArrayList<Double> mediaValori = new ObjectArrayList<Double>();

	public class CustomCanvas extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 605243927485370885L;

		@Override
		public void paintComponent(Graphics g) {
//			long time1 = System.nanoTime();
			if (renderingLoop != null) {
				renderingLoop.refresh();

				final int[] a = ((DataBufferInt) display.g.getRaster().getDataBuffer()).getData();
				CPURenderer.canvas2d = a;
				g.clearRect(0, 0, display.r.size[0] * mult, display.r.size[1] * mult);
				g.drawImage(display.g, 0, 0, display.r.size[0] * mult, display.r.size[1] * mult, null);
				//			long time2 = System.nanoTime();
				//			double timeDelta = ((double)(time2-time1))/1000000000d;
				//			double mediaAttuale = timeDelta;
				//			mediaValori.add(mediaAttuale);
				//			double somma = 0;
				//			for (Double val : mediaValori) {
				//				somma+=val;
				//			}
				//			System.out.println(somma/((double)mediaValori.size()));
			}
		}
	}
}
