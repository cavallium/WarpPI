package it.cavallium.warppi.gui.graphicengine.impl.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
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
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.event.TouchEndEvent;
import it.cavallium.warppi.event.TouchMoveEvent;
import it.cavallium.warppi.event.TouchPoint;
import it.cavallium.warppi.event.TouchStartEvent;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.util.EventSubmitter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SwingWindow extends JFrame {
	private static final long serialVersionUID = 2945898937634075491L;
	public CustomCanvas c;
	private RenderingLoop renderingLoop;
	private final SwingEngine display;
	private int mult = 1;
	private final EventSubmitter<Integer[]> onResize;
	private final EventSubmitter<Integer[]> onResize$;
	public JPanel buttonsPanel;
	private SwingAdvancedButton[][] buttons;
	private int BTN_SIZE;

	public SwingWindow(final SwingEngine disp) {
		display = disp;
		setLayout(new BorderLayout());
		setBackground(Color.BLACK);
		c = new CustomCanvas();
		c.setDoubleBuffered(false);
		this.add(c, BorderLayout.CENTER);
		try {
			setupButtonsPanel();
		} catch (IOException | URISyntaxException e1) {
			e1.printStackTrace();
		}
//		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		Toolkit.getDefaultToolkit().setDynamicLayout(false);
		// Transparent 16 x 16 pixel cursor image.
		final BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		mult = StaticVars.windowZoomFunction.apply(StaticVars.windowZoom.getLastValue()).intValue();

		if (!Engine.getPlatform().getSettings().isDebugEnabled()) {
			// Create a new blank cursor.
			final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

			// Set the blank cursor to the JFrame.
			getContentPane().setCursor(blankCursor);

			setResizable(false);
		}

		setTitle("WarpPI Calculator by Andrea Cavalli (@Cavallium)");

		onResize = EventSubmitter.create();
		onResize$ = onResize.map((newSize) -> {
			disp.r.size = new int[] { newSize[0], newSize[1] };
			if (disp.r.size[0] <= 0)
				disp.r.size[0] = 1;
			if (disp.r.size[1] <= 0)
				disp.r.size[1] = 1;
			SwingRenderer.canvas2d = new int[disp.r.size[0] * disp.r.size[1]];
			disp.g = new BufferedImage(disp.r.size[0], disp.r.size[1], BufferedImage.TYPE_INT_RGB);

			return newSize;
		});

		addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(final ComponentEvent e) {
				Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.destroy();
			}

			@Override
			public void componentMoved(final ComponentEvent e) {}

			@Override
			public void componentResized(final ComponentEvent e) {
				onResize.submit(new Integer[] { c.getWidth() / mult, c.getHeight() / mult });
			}

			@Override
			public void componentShown(final ComponentEvent e) {
				if (Engine.getPlatform().getSettings().isDebugEnabled())
					SwingWindow.this.centerWindow();
			}
		});
		c.setFocusable(true);
		c.grabFocus();
		c.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(final KeyEvent arg0) {
				Keyboard.debugKeyCode = arg0.getKeyCode();
			}

			@Override
			public void keyReleased(final KeyEvent arg0) {
				Keyboard.debugKeyCodeRelease = arg0.getKeyCode();
			}

			@Override
			public void keyTyped(final KeyEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		c.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				final Insets wp = SwingWindow.this.getInsets();
				final TouchPoint p = new TouchPoint(0, e.getX() - wp.left, e.getY() - wp.top, 5, 5, 1, 0);
				final ObjectArrayList<TouchPoint> touches = new ObjectArrayList<>();
				final ObjectArrayList<TouchPoint> changedTouches = new ObjectArrayList<>();
				touches.add(p);
				changedTouches.add(p);
				final TouchMoveEvent tse = new TouchMoveEvent(changedTouches, touches);
				Engine.INSTANCE.getHardwareDevice().getInputManager().getTouchDevice().onTouchMove(tse);
			}

			@Override
			public void mouseMoved(final MouseEvent e) {}
		});
		c.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(final MouseEvent e) {}

			@Override
			public void mousePressed(final MouseEvent e) {
				final Insets wp = SwingWindow.this.getInsets();
				final TouchPoint p = new TouchPoint(0, e.getX() - wp.left, e.getY() - wp.top, 5, 5, 1, 0);
				final ObjectArrayList<TouchPoint> touches = new ObjectArrayList<>();
				final ObjectArrayList<TouchPoint> changedTouches = new ObjectArrayList<>();
				touches.add(p);
				changedTouches.add(p);
				final TouchStartEvent tse = new TouchStartEvent(changedTouches, touches);
				Engine.INSTANCE.getHardwareDevice().getInputManager().getTouchDevice().onTouchStart(tse);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				final Insets wp = SwingWindow.this.getInsets();
				final TouchPoint p = new TouchPoint(0, e.getX() - wp.left, e.getY() - wp.top, 5, 5, 1, 0);
				final ObjectArrayList<TouchPoint> touches = new ObjectArrayList<>();
				final ObjectArrayList<TouchPoint> changedTouches = new ObjectArrayList<>();
				changedTouches.add(p);
				final TouchEndEvent tse = new TouchEndEvent(changedTouches, touches);
				Engine.INSTANCE.getHardwareDevice().getInputManager().getTouchDevice().onTouchEnd(tse);
			}

			@Override
			public void mouseEntered(final MouseEvent e) {}

			@Override
			public void mouseExited(final MouseEvent e) {}
		});
		StaticVars.windowZoom$.subscribe((newZoomValue) -> {
			if (newZoomValue != mult) {
				mult = (int) newZoomValue.floatValue();
				onResize.submit(new Integer[] { getWWidth(), getWHeight() });
				Engine.getPlatform().getConsoleUtils().out().println(3, "Engine", "CPU", "Zoom changed");
			}
		});
	}

	private void setupButtonsPanel() throws IOException, URISyntaxException {
		BTN_SIZE = 32;
		if (StaticVars.debugWindow2x)
			BTN_SIZE *= 2;

		buttons = new SwingAdvancedButton[8][8];
		final JPanel buttonsPanelContainer = new JPanel();
		buttonsPanelContainer.setLayout(new FlowLayout());
		buttonsPanelContainer.setBackground(Color.BLACK);
		this.add(buttonsPanelContainer, BorderLayout.PAGE_END);
		buttonsPanel = new JPanel();
		buttonsPanelContainer.add(buttonsPanel, BorderLayout.CENTER);
		buttonsPanel.setLayout(new GridLayout(9, 7));
		buttonsPanel.setBackground(Color.BLACK);
		buttonsPanel.setDoubleBuffered(false);
		buttonsPanel.setVisible(true);
		for (int row = 0; row < 5; row++)
			for (int col = 0; col < 7; col++)
				if (row == 0 && col == 2 || row == 0 && col == 4 || row == 2 && col == 2) {
					createBlankBox();
				} else {
					createBtn(row, col);
				}
		for (int row = 5; row < 8; row++) {
			createBlankBox();
			for (int col = 0; col < 5; col++)
				createBtn(row, col);
			createBlankBox();
		}
		final int b = 7;
		createBlankBox();
		for (int a = 4; a >= 0; a--)
			createBtn(a, b);
		createBlankBox();
	}

	private void createBlankBox() {
		final JPanel l = new JPanel();
		l.setPreferredSize(new Dimension((int) (BTN_SIZE * 1.5), BTN_SIZE));
		l.setBackground(Color.BLACK);
		buttonsPanel.add(l);
	}

	private void createBtn(final int row, final int col) throws IOException, URISyntaxException {
		final BufferedImage img = ImageIO.read(Engine.getPlatform().getStorageUtils().getResourceStream("/desktop-buttons.png"));
		final SwingAdvancedButton b = new SwingAdvancedButton(img, new Dimension((int) (BTN_SIZE * 1.5), BTN_SIZE));
		b.drawDefaultComponent = false;
		b.setText(Keyboard.getKeyName(row, col));
		b.setBasicForeground(Color.BLACK);
		Font f = b.getFont();
		f = f.deriveFont(Font.BOLD, BTN_SIZE / 3);
		b.setFont(f);
		b.setBackground(new Color(200, 200, 200));
		b.setFocusable(true);
		b.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Keyboard.keyRaw(row, col, true);
				c.grabFocus();
			}
			@Override
			public void mousePressed(MouseEvent e) {
				Keyboard.keyRaw(row, col, false);
				c.grabFocus();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if (b.state == 2) {
					b.setForeground(b.basicForeground.darker());
				} else {
					b.setForeground(b.basicForeground);
				}
				b.hover = false;
				b.repaint();
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				if (b.state == 2) {
					b.setForeground(b.basicForeground);
				} else {
					b.setForeground(b.basicForeground.brighter());
				}
				b.hover = true;
				b.repaint();
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		buttons[row][col] = b;
		buttonsPanel.add(b);
	}

	public void setAlphaChanged(final boolean val) {
		for (int row = 0; row < buttons.length; row++)
			for (int col = 0; col < buttons[0].length; col++) {
				final SwingAdvancedButton btn = buttons[row][col];
				if (btn != null) {
					btn.setText(Keyboard.getKeyName(row, col));
					if (row == 0 && col == 1)
						if (val)
							btn.state = 2;
						else
							btn.state = 0;
					if (val && Keyboard.hasKeyName(row, col))
						btn.setBasicForeground(Color.RED);
					else
						btn.setBasicForeground(Color.BLACK);
				}
			}
	}

	public void setShiftChanged(final boolean val) {
		for (int row = 0; row < buttons.length; row++)
			for (int col = 0; col < buttons[0].length; col++) {
				final SwingAdvancedButton btn = buttons[row][col];
				if (btn != null) {
					btn.setText(Keyboard.getKeyName(row, col));
					if (row == 0 && col == 0)
						if (val)
							btn.state = 2;
						else
							btn.state = 0;
					if (val && Keyboard.hasKeyName(row, col))
						btn.setBasicForeground(new Color(255, 120, 0));
					else
						btn.setBasicForeground(Color.BLACK);
				}
			}
	}

	public EventSubmitter<Integer[]> onResize() {
		return onResize$;
	}

	@Override
	public void setSize(final int width, final int height) {
		c.setSize(new Dimension(width * mult, height * mult));
		c.setPreferredSize(new Dimension(width * mult, height * mult));
		super.pack();
	}

	public Dimension getWSize() {
		return new Dimension(getWWidth(), getWHeight());
	}

	public int getWWidth() {
		return c.getWidth() / mult;
	}

	public int getWHeight() {
		return c.getHeight() / mult;
	}

	public void setRenderingLoop(final RenderingLoop renderingLoop) {
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
		public void paintComponent(final Graphics g) {
//			long time1 = System.nanoTime();
			if (renderingLoop != null) {
				renderingLoop.refresh();


				final int[] a = ((DataBufferInt) display.g.getRaster().getDataBuffer()).getData();
				SwingRenderer.canvas2d = a;
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
