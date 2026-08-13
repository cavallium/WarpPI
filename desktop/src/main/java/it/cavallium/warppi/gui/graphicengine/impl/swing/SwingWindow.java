package it.cavallium.warppi.gui.graphicengine.impl.swing;

import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalKey;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalRow;
import it.cavallium.warppi.device.input.HostKeyboardInput;
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
	private static final int BASE_BUTTON_SIZE = 32;
	private static final int BASE_PANEL_GAP = 5;
	private static final int RESIZE_REDRAW_DELAY_MILLIS = 120;
	private final int defaultWidth;
	private final int defaultHeight;
	private final SwingRenderer renderer;
	private final Runnable destroyEngine;
	private final float desktopScale;
	private BufferedImage graphics;
	public CustomCanvas c;
	private RenderingLoop renderingLoop;
	private volatile float calculatorZoom;
	private volatile float toolkitScale;
	private volatile float toolkitCompensation;
	private final EventSubmitter<Integer[]> onResize;
	private final EventSubmitter<Integer[]> onResize$;
	public JPanel buttonsPanel;
	private JPanel buttonsPanelContainer;
	private SwingAdvancedButton[][] buttons;
	private int buttonSize;
	private final HostKeyboardInput keyboardInput;
	private final KeyEventDispatcher keyboardEventDispatcher;
	private final Timer resizeRedrawTimer;
	private volatile boolean windowShown;
	private volatile boolean forceRepaint;

	public SwingWindow(final SwingRenderer renderer, final BufferedImage graphics, final Runnable destroyEngine,
			final int defaultWidth, final int defaultHeight) {
		this(renderer, graphics, destroyEngine, defaultWidth, defaultHeight, 1f);
	}

	public SwingWindow(final SwingRenderer renderer, final BufferedImage graphics, final Runnable destroyEngine,
			final int defaultWidth, final int defaultHeight, final float desktopScale) {
		this.renderer = renderer;
		this.graphics = graphics;
		this.destroyEngine = destroyEngine;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		this.desktopScale = desktopScale;
		keyboardInput = Keyboard.createHostKeyboardInput();
		keyboardEventDispatcher = this::dispatchHostKeyboardEvent;
		resizeRedrawTimer = new Timer(RESIZE_REDRAW_DELAY_MILLIS, event -> repaintAfterResizeSettles());
		resizeRedrawTimer.setRepeats(false);
		calculatorZoom = SwingScaling.normalizeCalculatorZoom(StaticVars.windowZoom.getLastValue());
		toolkitScale = SwingScaling.toolkitScale(getGraphicsConfiguration());
		toolkitCompensation = SwingScaling.toolkitCompensation(desktopScale, toolkitScale);
		setLayout(new BorderLayout());
		setBackground(Color.BLACK);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		c = new CustomCanvas();
		c.setMinimumSize(new Dimension(0, 0));
		final Dimension initialCanvasSize = SwingScaling.toUserSize(
				defaultWidth, defaultHeight, toolkitCompensation);
		c.setPreferredSize(initialCanvasSize);
		c.setSize(initialCanvasSize);
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
				refreshToolkitScale();
			}

			@Override
			public void componentResized(final ComponentEvent e) {
				if (windowShown) {
					onResize.submit(new Integer[] {
							SwingScaling.toLogicalUnits(c.getWidth(), calculatorZoom, toolkitCompensation),
							SwingScaling.toLogicalUnits(c.getHeight(), calculatorZoom, toolkitCompensation) });
					resizeRedrawTimer.restart();
				}
			}

			@Override
			public void componentShown(final ComponentEvent e) {
				SwingWindow.this.windowShown = true;
				if (WarpPI.getPlatform().getSettings().isDebugEnabled())
					SwingWindow.this.centerWindow();
				SwingUtilities.invokeLater(c::requestFocusInWindow);
			}
		});
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(final WindowEvent event) {
				c.requestFocusInWindow();
			}

			@Override
			public void windowLostFocus(final WindowEvent event) {
				keyboardInput.releaseAll();
			}
		});
		c.setFocusable(true);
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(keyboardEventDispatcher);
		StaticVars.windowZoom.subscribe(this::setCalculatorZoom);
	}

	private boolean dispatchHostKeyboardEvent(final KeyEvent event) {
		if (event.isConsumed() || !belongsToThisWindow(event.getSource()))
			return false;

		final boolean handled = switch (event.getID()) {
			case KeyEvent.KEY_PRESSED -> keyboardInput.press(
					event.getKeyCode(), event.getKeyChar(), event.isControlDown());
			case KeyEvent.KEY_RELEASED -> keyboardInput.release(event.getKeyCode());
			default -> false;
		};
		if (handled)
			event.consume();
		return handled;
	}

	private boolean belongsToThisWindow(final Object source) {
		if (!(source instanceof Component component))
			return false;
		return component == this || SwingUtilities.getWindowAncestor(component) == this;
	}

	private void repaintAfterResizeSettles() {
		forceRepaint = true;
		if (c.isShowing())
			c.paintImmediately(0, 0, c.getWidth(), c.getHeight());
		else
			c.repaint();
	}

	private void setupButtonsPanel() throws IOException, URISyntaxException {
		buttonSize = SwingScaling.toUserUnits(BASE_BUTTON_SIZE, toolkitCompensation);

		final BufferedImage buttonImage = ImageIO.read(
				WarpPI.getPlatform().getStorageUtils().getResourceStream("/desktop-buttons.png"));
		buttons = new SwingAdvancedButton[8][8];
		buttonsPanelContainer = new JPanel();
		final int panelGap = SwingScaling.toUserUnits(BASE_PANEL_GAP, toolkitCompensation);
		buttonsPanelContainer.setLayout(new FlowLayout(FlowLayout.CENTER, panelGap, panelGap));
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
		final int slotWidth = Math.round(buttonSize * 1.5f);
		final Dimension rowSize = new Dimension(slotWidth * physicalRow.slotCount(), buttonSize);
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
		l.setPreferredSize(new Dimension(Math.round(buttonSize * 1.5f), buttonSize));
		l.setBackground(Color.BLACK);
		rowPanel.add(l);
	}

	private void createBtn(final JPanel rowPanel, final PhysicalKey key, final BufferedImage buttonImage)
			throws IOException, URISyntaxException {
		final int row = key.matrixRow();
		final int col = key.matrixColumn();
		final SwingAdvancedButton b = new SwingAdvancedButton(
				buttonImage, new Dimension(Math.round(buttonSize * 1.5f), buttonSize));
		b.drawDefaultComponent = false;
		b.setText(Keyboard.getKeyName(row, col));
		b.setBasicForeground(Color.BLACK);
		Font f = b.getFont();
		f = f.deriveFont(Font.BOLD, buttonSize / 3f);
		b.setFont(f);
		b.setBackground(new Color(200, 200, 200));
		b.setFocusable(false);
		b.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Keyboard.keyRaw(row, col, true);
				c.requestFocusInWindow();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				Keyboard.keyRaw(row, col, false);
				c.requestFocusInWindow();
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

	private void setCalculatorZoom(final Float requestedZoom) {
		final float newZoom = SwingScaling.normalizeCalculatorZoom(requestedZoom);
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> applyCalculatorZoom(newZoom));
			return;
		}
		applyCalculatorZoom(newZoom);
	}

	private void applyCalculatorZoom(final float newZoom) {
		if (Math.abs(newZoom - calculatorZoom) < 0.001f)
			return;
		calculatorZoom = newZoom;
		onResize.submit(new Integer[] { getWWidth(), getWHeight() });
		forceRepaint = true;
		c.repaint();
		WarpPI.getPlatform().getConsoleUtils().out().println(3, "Engine", "CPU", "Zoom changed");
	}

	private void refreshToolkitScale() {
		final float newToolkitScale = SwingScaling.toolkitScale(getGraphicsConfiguration());
		final float newCompensation = SwingScaling.toolkitCompensation(desktopScale, newToolkitScale);
		if (Math.abs(newToolkitScale - toolkitScale) < 0.001f
				&& Math.abs(newCompensation - toolkitCompensation) < 0.001f)
			return;
		final int windowWidth = SwingScaling.toLogicalUnits(c.getWidth(), 1f, toolkitCompensation);
		final int windowHeight = SwingScaling.toLogicalUnits(c.getHeight(), 1f, toolkitCompensation);
		toolkitScale = newToolkitScale;
		toolkitCompensation = newCompensation;
		applyWindowSizes(windowWidth, windowHeight);
	}

	private void applyWindowSizes(final int windowWidth, final int windowHeight) {
		final Dimension canvasSize = SwingScaling.toUserSize(
				windowWidth, windowHeight, toolkitCompensation);
		c.setPreferredSize(canvasSize);
		c.setSize(canvasSize);
		resizeButtonsPanel();
		super.pack();
		onResize.submit(new Integer[] { getWWidth(), getWHeight() });
		forceRepaint = true;
		c.repaint();
	}

	private void resizeButtonsPanel() {
		buttonSize = SwingScaling.toUserUnits(BASE_BUTTON_SIZE, toolkitCompensation);
		final int panelGap = SwingScaling.toUserUnits(BASE_PANEL_GAP, toolkitCompensation);
		buttonsPanelContainer.setLayout(new FlowLayout(FlowLayout.CENTER, panelGap, panelGap));
		for (int rowIndex = 0; rowIndex < HardwareKeyboardLayout.rows().size(); rowIndex++) {
			final PhysicalRow physicalRow = HardwareKeyboardLayout.rows().get(rowIndex);
			final JPanel rowPanel = (JPanel) buttonsPanel.getComponent(rowIndex);
			final int slotWidth = Math.round(buttonSize * 1.5f);
			final Dimension rowSize = new Dimension(slotWidth * physicalRow.slotCount(), buttonSize);
			setFixedSize(rowPanel, rowSize);
			final Dimension keySize = new Dimension(slotWidth, buttonSize);
			for (final Component component : rowPanel.getComponents()) {
				setFixedSize(component, keySize);
				if (component instanceof SwingAdvancedButton button) {
					button.backgroundSize = keySize;
					button.setFont(button.getFont().deriveFont(Font.BOLD, buttonSize / 3f));
				}
			}
		}
		buttonsPanelContainer.revalidate();
		buttonsPanelContainer.repaint();
	}

	private static void setFixedSize(final Component component, final Dimension size) {
		component.setMinimumSize(size);
		component.setMaximumSize(size);
		component.setPreferredSize(size);
		component.setSize(size);
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
		final Dimension canvasSize = SwingScaling.toUserSize(
				width, height, toolkitCompensation);
		c.setSize(canvasSize);
		c.setPreferredSize(canvasSize);
		super.pack();
	}

	public Dimension getWSize() {
		return new Dimension(getWWidth(), getWHeight());
	}

	Dimension calculatorViewportSize(final int windowWidth, final int windowHeight) {
		return new Dimension(
				Math.max(1, Math.round(windowWidth / calculatorZoom)),
				Math.max(1, Math.round(windowHeight / calculatorZoom)));
	}

	public int getWWidth() {
		if (!windowShown) return defaultWidth;
		return SwingScaling.toLogicalUnits(c.getWidth(), calculatorZoom, toolkitCompensation);
	}

	public int getWHeight() {
		if (!windowShown) return defaultHeight;
		return SwingScaling.toLogicalUnits(c.getHeight(), calculatorZoom, toolkitCompensation);
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

	@Override
	public void dispose() {
		resizeRedrawTimer.stop();
		keyboardInput.releaseAll();
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.removeKeyEventDispatcher(keyboardEventDispatcher);
		super.dispose();
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
				g.clearRect(0, 0, getWidth(), getHeight());
				g.drawImage(graphics, 0, 0, getWidth(), getHeight(), null);
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
