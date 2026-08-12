package it.cavallium.warppi.gui.graphicengine.impl.swing;

import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalKey;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalRow;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.util.EventSubmitter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.net.URISyntaxException;

public class SwingWindow extends JFrame {
	private static final long serialVersionUID = 2945898937634075491L;
	private final int defaultWidth;
	private final int defaultHeight;
	private final SwingRenderer renderer;
	private final Runnable destroyEngine;
	private BufferedImage graphics;
	public CustomCanvas c;
	private RenderingLoop renderingLoop;
	private int mult = 1;
	private final EventSubmitter<Integer[]> onResize;
	private final EventSubmitter<Integer[]> onResize$;
	public JPanel buttonsPanel;
	private SwingAdvancedButton[][] buttons;
	private int BTN_SIZE;
	private volatile boolean windowShown;
	private volatile boolean forceRepaint;

	public SwingWindow(final SwingRenderer renderer, final BufferedImage graphics, Runnable destroyEngine, int defaultWidth, int defaultHeight) {
		this.renderer = renderer;
		this.graphics = graphics;
		this.destroyEngine = destroyEngine;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		setLayout(new BorderLayout());
		setBackground(Color.BLACK);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		c = new CustomCanvas();
		c.setMinimumSize(new Dimension(0, 0));
		c.setPreferredSize(new Dimension(defaultWidth, defaultHeight));
		c.setSize(defaultWidth, defaultHeight);
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

		mult = StaticVars.windowZoom.getLastValue().intValue();

		if (!WarpPI.getPlatform().getSettings().isDebugEnabled()) {
			// Create a new blank cursor.
			final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

			// Set the blank cursor to the JFrame.
			getContentPane().setCursor(blankCursor);

			setResizable(false);
		}

		setTitle("WarpPI Calculator by Andrea Cavalli (@Cavallium)");

		onResize = EventSubmitter.create();
		onResize$ = onResize.map((newSize) -> {
			if (newSize[0] <= 0)
				newSize[0] = 1;
			if (newSize[1] <= 0)
				newSize[1] = 1;

			var oldSize = renderer.size;
			renderer.size = new int[]{newSize[0], newSize[1]};

			SwingRenderer.canvas2d = new int[renderer.size[0] * renderer.size[1]];
			var oldG = graphics;
			this.graphics = new BufferedImage(renderer.size[0], renderer.size[1], BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D) graphics.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setColor(Color.BLACK);
			g.clearRect(0, 0, renderer.size[0], renderer.size[1]);
			double oldRatio = (double) oldSize[0] / (double) oldSize[1];
			double newRatio = (double) newSize[0] / (double) newSize[1];
			int newFrameWidth;
			int newFrameHeight = 0;
			if ((int) (oldRatio * 100) == (int) (newRatio * 100)) {
				newFrameWidth = newSize[0];
				newFrameHeight = newSize[1];
			} else {
				newFrameWidth = oldSize[0];
				newFrameHeight = oldSize[1];
			}
			g.drawImage(oldG, 0, 0, newFrameWidth, newFrameHeight, null);
			forceRepaint = true;
			this.c.repaint();

			return newSize;
		});

		addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(final ComponentEvent e) {
				sendPowerOffSignal();
			}

			@Override
			public void componentMoved(final ComponentEvent e) {
			}

			@Override
			public void componentResized(final ComponentEvent e) {
				if (windowShown) {
					onResize.submit(new Integer[]{c.getWidth() / mult, c.getHeight() / mult});
				}
			}

			@Override
			public void componentShown(final ComponentEvent e) {
				SwingWindow.this.windowShown = true;
				if (WarpPI.getPlatform().getSettings().isDebugEnabled())
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
		StaticVars.windowZoom.subscribe((newZoomValue) -> {
			mult = (int) newZoomValue.floatValue();
			onResize.submit(new Integer[]{getWWidth(), getWHeight()});
			WarpPI.getPlatform().getConsoleUtils().out().println(3, "Engine", "CPU", "Zoom changed");
		});
	}

	private void setupButtonsPanel() throws IOException, URISyntaxException {
		BTN_SIZE = 32;
		if (StaticVars.startupArguments.isZoomed())
			BTN_SIZE *= 2;

		final BufferedImage buttonImage = ImageIO.read(
				WarpPI.getPlatform().getStorageUtils().getResourceStream("/desktop-buttons.png"));
		buttons = new SwingAdvancedButton[8][8];
		final JPanel buttonsPanelContainer = new JPanel();
		buttonsPanelContainer.setLayout(new FlowLayout());
		buttonsPanelContainer.setBackground(Color.BLACK);
		this.add(buttonsPanelContainer, BorderLayout.PAGE_END);
		buttonsPanel = new JPanel();
		buttonsPanelContainer.add(buttonsPanel, BorderLayout.CENTER);
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		buttonsPanel.setBackground(Color.BLACK);
		buttonsPanel.setDoubleBuffered(false);
		buttonsPanel.setVisible(true);
		for (final PhysicalRow physicalRow : HardwareKeyboardLayout.rows())
			createPhysicalRow(physicalRow, buttonImage);
		refreshKeyboardLayer(HardwareKeyboardLayout.activeLayer());

		var size = super.getSize();
		super.setSize(size.width, size.height + buttonsPanelContainer.getHeight());
		super.pack();
	}

	private void createPhysicalRow(final PhysicalRow physicalRow, final BufferedImage buttonImage)
			throws IOException, URISyntaxException {
		final JPanel rowPanel = new JPanel(new GridLayout(1, physicalRow.slotCount()));
		final int slotWidth = (int) (BTN_SIZE * 1.5);
		final Dimension rowSize = new Dimension(slotWidth * physicalRow.slotCount(), BTN_SIZE);
		rowPanel.setPreferredSize(rowSize);
		rowPanel.setMinimumSize(rowSize);
		rowPanel.setMaximumSize(rowSize);
		rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		rowPanel.setBackground(Color.BLACK);
		rowPanel.setDoubleBuffered(false);
		buttonsPanel.add(rowPanel);

		for (int slot = 0; slot < physicalRow.slotCount(); slot++) {
			final PhysicalKey key = physicalRow.keyAtSlot(slot);
			if (key == null)
				createBlankBox(rowPanel);
			else
				createBtn(rowPanel, key, buttonImage);
		}
	}

	private void createBlankBox(final JPanel rowPanel) {
		final JPanel l = new JPanel();
		l.setPreferredSize(new Dimension((int) (BTN_SIZE * 1.5), BTN_SIZE));
		l.setBackground(Color.BLACK);
		rowPanel.add(l);
	}

	private void createBtn(final JPanel rowPanel, final PhysicalKey key, final BufferedImage buttonImage)
			throws IOException, URISyntaxException {
		final int row = key.matrixRow();
		final int col = key.matrixColumn();
		final SwingAdvancedButton b = new SwingAdvancedButton(
				buttonImage, new Dimension((int) (BTN_SIZE * 1.5), BTN_SIZE));
		b.drawDefaultComponent = false;
		b.setText(Keyboard.getKeyName(row, col));
		b.setBasicForeground(Color.BLACK);
		Font f = b.getFont();
		f = f.deriveFont(Font.BOLD, BTN_SIZE / 3f);
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
		rowPanel.add(b);
	}

	public void setKeyboardLayer(final Layer layer) {
		refreshKeyboardLayer(layer);
	}

	private void refreshKeyboardLayer(final Layer layer) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> refreshKeyboardLayer(layer));
			return;
		}
		for (final PhysicalRow physicalRow : HardwareKeyboardLayout.rows())
			for (final PhysicalKey key : physicalRow.keys()) {
				final SwingAdvancedButton btn = buttons[key.matrixRow()][key.matrixColumn()];
				final Key active = key.mapping(layer);
				final boolean activeModifier = key.isActiveModifier(layer);
				btn.setText(key.label(layer));
				btn.state = activeModifier ? 2 : 0;
				if (activeModifier || active != Key.NONE && active != key.normal())
					btn.setBasicForeground(layer == Layer.ALT_ALPHA
							? new Color(244, 67, 54) : new Color(33, 150, 243));
				else
					btn.setBasicForeground(Color.BLACK);
				btn.repaint();
			}
	}

	public EventSubmitter<Integer[]> onResize() {
		return onResize$;
	}

	@Deprecated
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
		if (!windowShown) return defaultWidth;
		return c.getWidth() / mult;
	}

	public int getWHeight() {
		if (!windowShown) return defaultHeight;
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

	public void sendPowerOffSignal() {
		destroyEngine.run();
		this.setVisible(false);
		this.dispose();
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
				boolean forceRepaint = SwingWindow.this.forceRepaint;
				SwingWindow.this.forceRepaint = false;
				renderingLoop.refresh(forceRepaint);


				final int[] a = ((DataBufferInt) graphics.getRaster().getDataBuffer()).getData();
				SwingRenderer.canvas2d = a;
				g.clearRect(0, 0, renderer.size[0] * mult, renderer.size[1] * mult);
				g.drawImage(graphics, 0, 0, renderer.size[0] * mult, renderer.size[1] * mult, null);
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
