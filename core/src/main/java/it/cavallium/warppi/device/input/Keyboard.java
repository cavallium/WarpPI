package it.cavallium.warppi.device.input;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.Platform.Gpio;
import it.cavallium.warppi.Platform.Gpio.DigitalInput;
import it.cavallium.warppi.Platform.Gpio.DigitalOutput;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.chip.ParallelToSerial;
import it.cavallium.warppi.device.chip.SerialToParallel;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.gui.screens.AppsMenuScreen;
import it.cavallium.warppi.gui.screens.KeyboardDebugScreen;
import it.cavallium.warppi.gui.screens.Screen;
import it.cavallium.warppi.gui.screens.UIDemoScreen;

public class Keyboard {
	private static final int MATRIX_SIZE = Byte.SIZE;
	private static final int MATRIX_DEBOUNCE_SAMPLES = 3;
	private static final long MATRIX_SCAN_PAUSE_MILLIS = 5;

	public static volatile boolean alpha = false;
	public static volatile boolean shift = false;

	// BCM GPIO line numbers for the keypad's 74HC595 and 74HC165.
	private static final int OUTPUT_LATCH_GPIO = 19;
	private static final int SHARED_CLOCK_GPIO = 20;
	private static final int OUTPUT_DATA_GPIO = 16;
	private static final int INPUT_LOAD_GPIO = 26;
	private static final int INPUT_DATA_GPIO = 21;
	private static final int INPUT_CLOCK_INHIBIT_GPIO = 13;

	private static final Key[][] pressedRawKeys = new Key[MATRIX_SIZE][MATRIX_SIZE];
	public static volatile boolean[][] debugKeysDown = new boolean[8][8];
	public static volatile int debugKeyCode = -1;
	public static volatile int debugKeyCodeRelease = -1;

	private static volatile boolean refreshRequest = false;
	private static volatile Thread keyboardThread;

	private static KeyboardEventListener additionalListener;

	public static HostKeyboardInput createHostKeyboardInput() {
		return new HostKeyboardInput(
				Keyboard::keyPressed,
				Keyboard::keyReleased,
				() -> Keyboard.shift,
				() -> Keyboard.alpha,
				pressed -> Keyboard.debugKeysDown[1][0] = pressed);
	}

	public void startKeyboard() {
		synchronized (Keyboard.class) {
			if (keyboardThread != null && keyboardThread.isAlive())
				return;

			final Thread thread = new Thread(Keyboard::runKeyboard);
			keyboardThread = thread;
			WarpPI.getPlatform().setThreadName(thread, "Keyboard thread");
			thread.setPriority(Thread.NORM_PRIORITY);
			WarpPI.getPlatform().setThreadDaemon(thread);
			thread.start();
		}
	}

	private static void runKeyboard() {
		try {
			if (WarpPI.getPlatform().isRunningOnRaspberry())
				runRaspberryKeyboard();
			else
				runDebugKeyboard();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			synchronized (Keyboard.class) {
				if (keyboardThread == Thread.currentThread())
					keyboardThread = null;
			}
		}
	}

	private static void runDebugKeyboard() throws InterruptedException {
		while (!Thread.currentThread().isInterrupted()) {
			if (debugKeyCode != -1) {
				debugKey(debugKeyCode, false);
				debugKeyCode = -1;
			}
			if (debugKeyCodeRelease != -1) {
				debugKey(debugKeyCodeRelease, true);
				debugKeyCodeRelease = -1;
			}
			Thread.sleep(50);
		}
	}

	private static void runRaspberryKeyboard() throws InterruptedException {
		final Gpio gpio = WarpPI.getPlatform().getGpio();
		final DigitalOutput clockInhibit = gpio.output(INPUT_CLOCK_INHIBIT_GPIO, true);
		final DigitalOutput outputLatch = gpio.output(OUTPUT_LATCH_GPIO, false);
		final DigitalOutput outputData = gpio.output(OUTPUT_DATA_GPIO, false);
		final DigitalOutput inputLoad = gpio.output(INPUT_LOAD_GPIO, true);
		final DigitalOutput sharedClock = gpio.output(SHARED_CLOCK_GPIO, false);
		final DigitalInput inputData = gpio.input(INPUT_DATA_GPIO);
		final SerialToParallel columns = new SerialToParallel(outputLatch, sharedClock, outputData);
		final ParallelToSerial rows = new ParallelToSerial(inputLoad, clockInhibit, inputData, sharedClock,
				gpio::delayMicroseconds);
		final KeyboardMatrixScanner scanner = new KeyboardMatrixScanner(columns, rows, Keyboard::onMatrixTransition,
				MATRIX_DEBOUNCE_SAMPLES);

		KeyboardDebugScreen.log("Started keyboard system");
		try {
			while (!Thread.currentThread().isInterrupted()) {
				scanner.scanOnce();
				Thread.sleep(MATRIX_SCAN_PAUSE_MILLIS);
			}
		} finally {
			try {
				scanner.releaseAll();
			} finally {
				setKeyboardPinsIdle(clockInhibit, inputLoad, outputLatch, outputData, sharedClock, columns);
			}
		}
	}

	private static void setKeyboardPinsIdle(final DigitalOutput clockInhibit, final DigitalOutput inputLoad,
			final DigitalOutput outputLatch, final DigitalOutput outputData, final DigitalOutput sharedClock,
			final SerialToParallel columns) {
		clockInhibit.set(true);
		inputLoad.set(true);
		columns.writeByte(0);
		outputData.set(false);
		outputLatch.set(false);
		sharedClock.set(false);
	}

	private static void onMatrixTransition(final int row, final int column, final boolean pressed) {
		debugKeysDown[row][column] = pressed;
		keyRaw(row, column, !pressed);
	}

	/**
	 * 
	 * @param k
	 * @param released true: released, false: pressed
	 */
	private static void debugKey(Key k, boolean released) {
		if (released) {
			Keyboard.keyReleased(k);
		} else {
			Keyboard.keyPressed(k);
		}
	}
	
	/**
	 * 
	 * @param keyCode
	 * @param released true: released, false: pressed
	 */
	public static void debugKey(final int keyCode, boolean released) {
		switch (keyCode) {
			case KeyboardAWTValues.VK_ESCAPE:
				debugKey(Key.BACK, released);
				break;
			case KeyboardAWTValues.VK_S:
				if (Keyboard.shift) {
					debugKey(Key.ARCSINE, released);
				} else if (Keyboard.alpha) {
					debugKey(Key.LETTER_S, released);
				} else {
					debugKey(Key.SINE, released);
				}
				break;
			case KeyboardAWTValues.VK_C:
				if (Keyboard.shift) {
					debugKey(Key.ARCCOSINE, released);
				} else if (Keyboard.alpha) {
					debugKey(Key.LETTER_C, released);
				} else {
					debugKey(Key.COSINE, released);
				}
				break;
			case KeyboardAWTValues.VK_T:
				if (Keyboard.shift) {
					debugKey(Key.ARCTANGENT, released);
				} else if (Keyboard.alpha) {
					debugKey(Key.LETTER_T, released);
				} else {
					debugKey(Key.TANGENT, released);
				}
				break;
			case KeyboardAWTValues.VK_D:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.debug_DEG, released);
				} else if (Keyboard.alpha) {
					debugKey(Key.LETTER_D, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_R:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.debug_RAD, released);
				} else if (Keyboard.alpha) {
					debugKey(Key.LETTER_R, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_G:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.debug_GRA, released);
				} else if (Keyboard.alpha) {
					debugKey(Key.LETTER_G, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_X:
				if (Keyboard.alpha) {
					debugKey(Key.LETTER_X, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_P:
				if (Keyboard.shift) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha) {
					debugKey(Key.LETTER_P, released);
				} else {
					debugKey(Key.PI, released);
				}
				break;
			case KeyboardAWTValues.VK_E:
				if (Keyboard.shift) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha) {
					debugKey(Key.LETTER_E, released);
				} else {
					debugKey(Key.EULER_NUMBER, released);
				}
				break;
			case KeyboardAWTValues.VK_Y:
				if (Keyboard.alpha) {
					debugKey(Key.LETTER_Y, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_B:
				if (Keyboard.shift) {
					debugKey(Key.BRIGHTNESS_CYCLE_REVERSE, released);
				} else if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.BRIGHTNESS_CYCLE, released);
				} else {
					debugKey(Key.LETTER_B, released);
				}
				break;
			case KeyboardAWTValues.VK_L:
				if (Keyboard.shift) {
					debugKey(Key.LOGARITHM, released);
				} else if (Keyboard.alpha) {
					debugKey(Key.LETTER_L, released);
				} else {
					debugKey(Key.LOGARITHM, released);
				}
				break;
			case KeyboardJogampValues.VK_ENTER:
			case KeyboardAWTValues.VK_ENTER:
				if (Keyboard.shift) {
					debugKey(Key.STEP, released);
				} else if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.SIMPLIFY, released);
				} else {
					debugKey(Key.NONE, released);
				}
				int row = 2;
				int col = 1;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				break;
			case KeyboardAWTValues.VK_1:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM1, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_2:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM2, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_3:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM3, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_4:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM4, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_5:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM5, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_6:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM6, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_7:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM7, released);
				} else if (Keyboard.shift) {
					if (WarpPI.getPlatform().isRunningOnRaspberry() == false) {
						debugKey(Key.DIVIDE, released);
					}
				}
				break;
			case KeyboardAWTValues.VK_8:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM8, released);
				} else if (Keyboard.shift) {
					debugKey(Key.PARENTHESIS_OPEN, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_9:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM9, released);
				} else if (Keyboard.shift) {
					debugKey(Key.PARENTHESIS_CLOSE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_0:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NUM0, released);
				} else if (Keyboard.shift) {
					debugKey(Key.EQUAL, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_M:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.SURD_MODE, released);
				} else if (Keyboard.shift) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.LETTER_M, released);
				}
				break;
			case KeyboardJogampValues.VK_ADD:
			case KeyboardAWTValues.VK_ADD:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.PLUS, released);
				} else if (Keyboard.shift) {
					debugKey(Key.PLUS_MINUS, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_SUBTRACT:
			case KeyboardAWTValues.VK_SUBTRACT:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.MINUS, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_MULTIPLY:
			case KeyboardAWTValues.VK_MULTIPLY:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.MULTIPLY, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_DIVIDE:
			case KeyboardAWTValues.VK_DIVIDE:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.DIVIDE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_BACK_SPACE:
				debugKey(Key.DELETE, released);
				break;
			case KeyboardJogampValues.VK_DELETE:
			case KeyboardAWTValues.VK_DELETE:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.RESET, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_LEFT:
			case KeyboardAWTValues.VK_LEFT:
				//LEFT
				row = 2;
				col = 3;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.LEFT, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_RIGHT:
			case KeyboardAWTValues.VK_RIGHT:
				//RIGHT
				row = 2;
				col = 5;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.RIGHT, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_UP:
			case KeyboardAWTValues.VK_UP:
				//UP
				row = 1;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.UP, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_DOWN:
			case KeyboardAWTValues.VK_DOWN:
				//DOWN
				row = 3;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.DOWN, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case (short) 12:
				//DOWN
				row = 2;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.OK, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_NUMPAD4:
			case KeyboardAWTValues.VK_NUMPAD4:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.HISTORY_BACK, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_NUMPAD6:
			case KeyboardAWTValues.VK_NUMPAD6:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.HISTORY_FORWARD, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_PERIOD:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.DOT, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_A:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_A, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_F:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_F, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_H:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_H, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_I:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_I, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_J:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_J, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_K:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_K, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_N:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_N, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_O:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_O, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_Q:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_Q, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_U:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_U, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_V:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_V, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_W:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_W, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardAWTValues.VK_Z:
				if (!Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.NONE, released);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					debugKey(Key.LETTER_Z, released);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					debugKey(Key.ZOOM_MODE, released);
				} else {
					debugKey(Key.NONE, released);
				}
				break;
			case KeyboardJogampValues.VK_SHIFT:
			case KeyboardAWTValues.VK_SHIFT:
				debugKey(Key.SHIFT, released);
				break;
			case KeyboardAWTValues.VK_ALT:
			case KeyboardAWTValues.VK_ALT_GRAPH:
				debugKey(Key.ALPHA, released);
				break;
			case KeyboardJogampValues.VK_NUMPAD1:
			case KeyboardAWTValues.VK_NUMPAD1:
				debugKey(Key.SQRT, released);
				break;
			case KeyboardJogampValues.VK_NUMPAD2:
			case KeyboardAWTValues.VK_NUMPAD2:
				debugKey(Key.ROOT, released);
				break;
			case KeyboardJogampValues.VK_NUMPAD3:
			case KeyboardAWTValues.VK_NUMPAD3:
				debugKey(Key.POWER_OF_2, released);
				break;
			case KeyboardJogampValues.VK_NUMPAD5:
			case KeyboardAWTValues.VK_NUMPAD5:
				debugKey(Key.POWER_OF_x, released);
				break;
		}
	}

	private synchronized static void debugKeyReleased(final int keyCode) {
		switch (keyCode) {
			case KeyboardAWTValues.VK_ENTER:
				int row = 2;
				int col = 1;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				break;
			case KeyboardJogampValues.VK_LEFT:
			case KeyboardAWTValues.VK_LEFT:
				//LEFT
				row = 2;
				col = 3;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				break;
			case KeyboardJogampValues.VK_RIGHT:
			case KeyboardAWTValues.VK_RIGHT:
				//RIGHT
				row = 2;
				col = 5;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				System.out.println("RELEASE");
				break;
			case KeyboardJogampValues.VK_UP:
			case KeyboardAWTValues.VK_UP:
				//UP
				row = 1;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				break;
			case KeyboardJogampValues.VK_DOWN:
			case KeyboardAWTValues.VK_DOWN:
				//DOWN
				row = 3;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				break;
			case (short) 12:
				//DOWN
				row = 2;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				break;
		}
	}

	/**
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	@Deprecated
	public static boolean isKeyDown(final int row, final int col) {
		return Keyboard.debugKeysDown[row - 1][col - 1];
	}

	public static String getKeyName(final int row, final int col) {
		return Keyboard.getKeyName(row, col, Keyboard.shift, Keyboard.alpha);
	}

	public static String getKeyName(final int row, final int col, final boolean shift, final boolean alpha) {
		return HardwareKeyboardLayout.labelFor(HardwareKeyboardLayout.mappingAt(row, col, shift, alpha));
	}

	public static boolean hasKeyName(final int row, final int col) {
		return HardwareKeyboardLayout.mappingAt(row, col, Keyboard.shift, Keyboard.alpha) != Key.NONE;
	}

	/**
	 * 
	 * @param row
	 * @param col
	 * @param released true: released, false: pressed
	 */
	public static synchronized void keyRaw(final int row, final int col, final boolean released) {
		final Key key = updateRawKeyState(row, col, released, shift, alpha);
		if (key == null)
			return;
		if (released)
			keyReleased(key);
		else
			keyPressed(key);
	}

	static synchronized Key updateRawKeyState(final int row, final int column, final boolean released,
			final boolean shiftActive, final boolean alphaActive) {
		validateMatrixPosition(row, column);

		if (released) {
			final Key pressedKey = pressedRawKeys[row][column];
			pressedRawKeys[row][column] = null;
			return pressedKey;
		}

		if (pressedRawKeys[row][column] != null)
			return null;
		pressedRawKeys[row][column] = HardwareKeyboardLayout.mappingAt(
				row, column, shiftActive, alphaActive);
		return pressedRawKeys[row][column];
	}

	/** Returns whether the debounced physical switch at this matrix position is held. */
	public static synchronized boolean isPhysicalKeyPressed(final int row, final int column) {
		validateMatrixPosition(row, column);
		return pressedRawKeys[row][column] != null;
	}

	private static void validateMatrixPosition(final int row, final int column) {
		if (row < 0 || row >= MATRIX_SIZE || column < 0 || column >= MATRIX_SIZE)
			throw new IllegalArgumentException("Keyboard coordinates must be between 0 and 7");
	}

	static synchronized void resetRawKeyState() {
		for (int row = 0; row < MATRIX_SIZE; row++)
			for (int column = 0; column < MATRIX_SIZE; column++)
				pressedRawKeys[row][column] = null;
	}

	public static void stopKeyboard() {
		final Thread thread;
		synchronized (Keyboard.class) {
			thread = keyboardThread;
		}
		if (thread == null)
			return;

		thread.interrupt();
		if (thread == Thread.currentThread())
			return;
		try {
			thread.join(500);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	static Screen screenOpenedByGlobalKey(final Key key) {
		return key == Key.SETTINGS ? new AppsMenuScreen() : null;
	}

	public synchronized static void keyPressed(final Key k) {
		boolean done = false;
		if (Keyboard.additionalListener != null) {
			done = dispatchKeyPressed(Keyboard.additionalListener, new KeyPressedEvent(k));
		}
		if (WarpPI.INSTANCE.getHardwareDevice().getDisplayManager() != null) {
			final Screen scr = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
			boolean refresh = false;
			boolean scrdone = false;
			if (scr != null && scr.isReadyForInput()) {
				scrdone = dispatchKeyPressed(scr, new KeyPressedEvent(k));
			}
			if (scrdone) {
				refresh = true;
			} else {
				switch (k) {
					case POWEROFF:
						WarpPI.INSTANCE.getHardwareDevice().getDeviceStateDevice().powerOff();
						break;
					case SETTINGS:
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager()
								.setScreen(screenOpenedByGlobalKey(k));
						refresh = true;
						break;
					case debug5:
						if (WarpPI.getPlatform().getSettings().isDebugEnabled()) {
							WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(new UIDemoScreen());
							refresh = true;
						}
						break;
					case NONE:
						break;
					case BRIGHTNESS_CYCLE:
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().cycleBrightness(false);
						refresh = true;
						break;
					case BRIGHTNESS_CYCLE_REVERSE:
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().cycleBrightness(true);
						refresh = true;
						break;
					case ZOOM_MODE:
						final float newZoom = StaticVars.windowZoom.getLastValue() % 3 + 1;
						StaticVars.windowZoom.submit(newZoom);
						WarpPI.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "Keyboard", "Zoom: " + newZoom);
//						StaticVars.windowZoom = ((StaticVars.windowZoom - 0.5f) % 2f) + 1f;
						refresh = true;
						break;
					case HISTORY_BACK:
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().goBack();
						refresh = true;
						break;
					case HISTORY_FORWARD:
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().goForward();
						refresh = true;
						break;
					case BACK:
						WarpPI.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "Closing current screen.");
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().closeScreen();
						refresh = true;
						break;
					default:
						break;
				}
			}
			switch (k) {
				case SHIFT:
					if (Keyboard.alpha) {
						WarpPI.getPlatform().alphaChanged(Keyboard.alpha = false);
					}
					WarpPI.getPlatform().shiftChanged(Keyboard.shift = !Keyboard.shift);
					refresh = true;
					break;
				case ALPHA:
					if (Keyboard.shift) {
						WarpPI.getPlatform().shiftChanged(Keyboard.shift = false);
					}
					WarpPI.getPlatform().alphaChanged(Keyboard.alpha = !Keyboard.alpha);
					refresh = true;
					break;
				default:
					if (k != Key.NONE) {
						if (Keyboard.shift) {
							WarpPI.getPlatform().shiftChanged(Keyboard.shift = false);
						}
						if (Keyboard.alpha) {
							WarpPI.getPlatform().alphaChanged(Keyboard.alpha = false);
						}
					}
					break;
			}
			if (refresh) {
				Keyboard.refreshRequest = true;
			}
		} else if (!done) {
			WarpPI.getPlatform().getConsoleUtils().out().println(1, "Key " + k.toString() + " ignored.");
		}
	}

	public synchronized static void keyReleased(final Key k) {
		boolean done = false;
		if (Keyboard.additionalListener != null) {
			done = dispatchKeyReleased(Keyboard.additionalListener, new KeyReleasedEvent(k));
		}
		boolean refresh = false;
		if (WarpPI.INSTANCE.getHardwareDevice().getDisplayManager() != null) {
			final Screen scr = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
			if (scr != null && scr.isReadyForInput()
					&& dispatchKeyReleased(scr, new KeyReleasedEvent(k))) {
				refresh = true;
			} else {
				switch (k) {
					case NONE:
						break;
					default:
						break;
				}
			}
			if (refresh) {
				Keyboard.refreshRequest = true;
			}
		} else if (!done) {
			WarpPI.getPlatform().getConsoleUtils().out().println(1, "Key " + k.toString() + " ignored.");
		}
	}

	static boolean dispatchKeyPressed(final KeyboardEventListener listener, final KeyPressedEvent event) {
		try {
			return listener.onKeyPressed(event);
		} catch (final RuntimeException exception) {
			reportListenerFailure(exception);
			return false;
		}
	}

	static boolean dispatchKeyReleased(final KeyboardEventListener listener, final KeyReleasedEvent event) {
		try {
			return listener.onKeyReleased(event);
		} catch (final RuntimeException exception) {
			reportListenerFailure(exception);
			return false;
		}
	}

	private static void reportListenerFailure(final RuntimeException exception) {
		System.err.println("Keyboard event listener failed; input scanning will continue");
		exception.printStackTrace(System.err);
	}

	public void setAdditionalKeyboardListener(final KeyboardEventListener l) {
		Keyboard.additionalListener = l;
	}

	public static boolean popRefreshRequest() {
		if (Keyboard.refreshRequest) {
			Keyboard.refreshRequest = false;
			return true;
		}
		return false;
	}

}

/*



Keyboard:
	Example button:
	|ROW,COLUMN----|
	| NORMAL STATE |
	| SHIFT STATE  |
	| ALPHA STATE  |
	|--------------|

	Physical keyboard:
	|0,0-----|0,1-----|########|0,3-----|########|0,5-----|0,6-----|
	| SHIFT  | ALPHA  |########|  ^     |########|SETTINGS|+BRIGHT |
	| NORMAL | ALPHA  |########|        |########|        |-BRIGHT |
	| SHIFT  | NORMAL |########|        |########|        |ZOOMMODE|
	|1,0-----|1,1-----|1,2-----|1,3-----|1,4-----|1,5-----|1,6-----|
	| F_4    |        |   <    |   OK   |   >    | Back   | Fwd    |
	| F_4    |        |        |        |        |        |        |
	| F_4    |        |        |        |        |        |        |
	|2,0-----|2,1-----|--------|2,3-----|--------|2,5-----|2,6-----|
	| F_3    | SQRT   |########|  v     | BACK   |        |        |
	| F_3    | ROOT   |########|        |        |        |        |
	| F_3    |        |########|        |        |        |        |
	|3,0-----|3,1-----|3,2-----|3,3-----|3,4-----|3,5-----|3,6-----|
	| F_2    |        | POW x  | POW 2  |        |        |        |
	| F_2    |        |        |        |        |        |        |
	| F_2    |        |        |        |        |        |        |
	|4,0-----|4,1-----|4,2-----|4,3-----|4,4-----|4,5-----|4,6-----|
	| F_1    |        | (      | )      |        | S<=>D  |        |
	| F_1    |        |        |        |        |        |        |
	| F_1    |        |        |        |        |        |        |
	|5,0-----|5,1-----|5,2-----|5,3-----|5,4-----|5,5-----|5,6-----|
	| 7      | 8      | 9      | DEL    | RESET                    |
	|        |        |        |        |                          |
	|        |        |        |        |                          |
	|6,0-----|6,1-----|6,2-----|6,3-----|6,4-----------------------|
	| 4      | 5      | 6      | *      | /                        |
	|        |        |        |        |                          |
	|        |        |        |        |                          |
	|7,0-----|7,1-----|7,2-----|7,3-----|7,4-----------------------|
	| 1      | 2      | 3      |  +     | -                        |
	|        |        |        |        |                          |
	|        |        |        |        |                          |
	|4,7-----|3,7-----|2,7-----|1,7-----|0,7-----------------------|
	| 0      | .      |        |        | SIMPLIFY                 |
	|        |        |        |PI      | STEP                     |
	| X      | Y      | Z      |DRG CYCL|                          |
	|--------|--------|--------|--------|--------------------------|

SCREEN F_n:
	MathInputScreen:
		Default:
			[F_1] Normal: Solve for X			Shift: Solve for _			Alpha:
			[F_2] Normal: 						Shift: 						Alpha:
			[F_3] Normal: Variables	& constants	Shift: 						Alpha:
			[F_4] Normal: Functions f(x)		Shift: 						Alpha:
		Variable popup:
			[F_1] Normal(if constant):Set value	Shift: 						Alpha:
			[F_2] Normal: 						Shift: 						Alpha:
			[F_3] Normal: 						Shift: 						Alpha:
			[F_4] Normal: 						Shift: 						Alpha:
	MarioScreen
		[F_1] Normal: 						Shift: 						Alpha:
		[F_2] Normal: 						Shift: 						Alpha:
		[F_3] Normal: 						Shift: 						Alpha:
		[F_4] Normal: 						Shift: 						Alpha:
	ChooseVariableValueScreen
		[F_1] Normal: 						Shift: 						Alpha:
		[F_2] Normal: 						Shift: 						Alpha:
		[F_3] Normal: 						Shift: 						Alpha:
		[F_4] Normal: 						Shift: 						Alpha:
	SolveForXScreen
		[F_1] Normal: 						Shift: 						Alpha:
		[F_2] Normal: 						Shift: 						Alpha:
		[F_3] Normal: 						Shift: 						Alpha:
		[F_4] Normal: 						Shift: 						Alpha:
*/
