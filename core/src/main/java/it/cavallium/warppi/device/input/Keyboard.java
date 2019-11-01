package it.cavallium.warppi.device.input;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.chip.ParallelToSerial;
import it.cavallium.warppi.device.chip.SerialToParallel;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.extra.mario.MarioScreen;
import it.cavallium.warppi.extra.tetris.TetrisScreen;
import it.cavallium.warppi.gui.GUIErrorMessage;
import it.cavallium.warppi.gui.screens.KeyboardDebugScreen;
import it.cavallium.warppi.gui.screens.Screen;

public class Keyboard {
	public static volatile boolean alpha = false;
	public static volatile boolean shift = false;

	//From Serial
	private static final int RCK_pin = 35;
	private static final int SCK_and_CLK_pin = 38;
	private static final int SER_pin = 36;

	//To Serial
	private static final int SH_LD_pin = 37;
	private static final int QH_pin = 40;
	private static final int CLK_INH_pin = 33;

	private static volatile boolean[][] precedentStates = new boolean[8][8];
	public static volatile boolean[][] debugKeysDown = new boolean[8][8];
	public static volatile int debugKeyCode = -1;
	public static volatile int debugKeyCodeRelease = -1;

	private static volatile boolean refreshRequest = false;

	private static KeyboardEventListener additionalListener;

	public synchronized void startKeyboard() {
		final Thread kt = new Thread(() -> {
			if (WarpPI.getPlatform().isRunningOnRaspberry() == false) {
				try {
					while (true) {
						if (Keyboard.debugKeyCode != -1) {
							Keyboard.debugKey(Keyboard.debugKeyCode, false);
							Keyboard.debugKeyCode = -1;
						}
						if (Keyboard.debugKeyCodeRelease != -1) {
							Keyboard.debugKey(Keyboard.debugKeyCodeRelease, true);
							Keyboard.debugKeyCodeRelease = -1;
						}
						Thread.sleep(50);
					}
				} catch (final InterruptedException e) {}
			} else {
				WarpPI.getPlatform().getGpio().pinMode(Keyboard.CLK_INH_pin, WarpPI.getPlatform().getGpio().valueOutput());
				WarpPI.getPlatform().getGpio().pinMode(Keyboard.RCK_pin, WarpPI.getPlatform().getGpio().valueOutput());
				WarpPI.getPlatform().getGpio().pinMode(Keyboard.SER_pin, WarpPI.getPlatform().getGpio().valueOutput());
				WarpPI.getPlatform().getGpio().pinMode(Keyboard.SH_LD_pin, WarpPI.getPlatform().getGpio().valueOutput());
				WarpPI.getPlatform().getGpio().pinMode(Keyboard.SCK_and_CLK_pin, WarpPI.getPlatform().getGpio().valueOutput());
				WarpPI.getPlatform().getGpio().pinMode(Keyboard.QH_pin, WarpPI.getPlatform().getGpio().valueInput());

				WarpPI.getPlatform().getGpio().digitalWrite(Keyboard.CLK_INH_pin, false);
				WarpPI.getPlatform().getGpio().digitalWrite(Keyboard.RCK_pin, false);
				WarpPI.getPlatform().getGpio().digitalWrite(Keyboard.SER_pin, false);
				WarpPI.getPlatform().getGpio().digitalWrite(Keyboard.SH_LD_pin, false);
				WarpPI.getPlatform().getGpio().digitalWrite(Keyboard.SCK_and_CLK_pin, false);
				WarpPI.getPlatform().getGpio().digitalWrite(Keyboard.QH_pin, false);
				final SerialToParallel chip1 = new SerialToParallel(Keyboard.RCK_pin, Keyboard.SCK_and_CLK_pin /*SCK*/, Keyboard.SER_pin);
				final ParallelToSerial chip2 = new ParallelToSerial(Keyboard.SH_LD_pin, Keyboard.CLK_INH_pin, Keyboard.QH_pin, Keyboard.SCK_and_CLK_pin/*CLK*/);

				KeyboardDebugScreen.log("Started keyboard system");

				while (true) {
					boolean[] data;
					for (int col = 0; col < 8; col++) {
						data = new boolean[8];
						data[col] = true;
						chip1.write(data);

						data = chip2.read();
//						KeyboardDebugScreen.ks[col] = data;

						for (int row = 0; row < 8; row++) {
							if (data[row] == true && Keyboard.precedentStates[row][col] == false) {
								//								System.out.println("Pressed button at " + (row + 1) + ", " + (col + 1));
//								KeyboardDebugScreen.log("Pressed button at " + (row + 1) + ", " + (col + 1));
								Keyboard.keyRaw(row, col, false);
							} else if (data[row] == false && Keyboard.precedentStates[row][col] == true) {
								Keyboard.keyRaw(row, col, true);
							}
//								KeyboardDebugScreen.log("Released button at " + (row + 1) + ", " + (col + 1));
							Keyboard.precedentStates[row][col] = data[row];
						}
					}
				}
			}
		});
		WarpPI.getPlatform().setThreadName(kt, "Keyboard thread");
		kt.setPriority(Thread.NORM_PRIORITY + 1);
		WarpPI.getPlatform().setThreadDaemon(kt);
		kt.start();
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
			case KeyboardAWTValues.VK_CONTROL:
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
		if (WarpPI.getPlatform().isRunningOnRaspberry()) {
			return Keyboard.precedentStates[row - 1][col - 1];
		} else {
			return Keyboard.debugKeysDown[row - 1][col - 1];
		}
	}

	static final String[][][] KeyLabelsMap = /* [ROW, COLUMN, (0:normal 1:shift 2:alpha)] */
			{ { /* ROW 0 */
					{ "⇪", "⇪", null }, /* 0,0 */
					{ "A", null, "A" }, /* 0,1 */
					{ "", null, null }, /* 0,2 */
					{ "⇧", null, null }, /* 0,3 */
					{ "", null, null }, /* 0,4 */
					{ "", null, null }, /* 0,5 */
					{ "☼", "☼", null }, /* 0,6 */
					{ "↩", null, null } /* 0,7 */
			}, { /* ROW 1 */
					{ "", null, null }, /* 1,0 */
					{ "", null, null }, /* 1,1 */
					{ "⇦", null, null }, /* 1,2 */
					{ "OK", null, null }, /* 1,3 */
					{ "⇨", null, null }, /* 1,4 */
					{ "≪", null, null }, /* 1,5 */
					{ "≫", null, null }, /* 1,6 */
					{ "", null, null } /* 1,7 */
			}, { /* ROW 2 */
					{ "", null, null }, /* 2,0 */
					{ "√▯", null, null }, /* 2,1 */
					{ "", null, null }, /* 2,2 */
					{ "⇩", null, null }, /* 2,3 */
					{ "↶", null, null }, /* 2,4 */
					{ "", null, null }, /* 2,5 */
					{ "", null, null }, /* 2,6 */
					{ "", null, null } /* 2,7 */
			}, { /* ROW 3 */
					{ "", null, null }, /* 3,0 */
					{ "", null, null }, /* 3,1 */
					{ "▯^▯", null, null }, /* 3,2 */
					{ "▯^2", null, null }, /* 3,3 */
					{ "", null, null }, /* 3,4 */
					{ "", null, null }, /* 3,5 */
					{ "", null, null }, /* 3,6 */
					{ ".", null, "y" } /* 3,7 */
			}, { /* ROW 4 */
					{ "", null, null }, /* 4,0 */
					{ "", null, null }, /* 4,1 */
					{ "(▯)", null, null }, /* 4,2 */
					{ ")", null, null }, /* 4,3 */
					{ "", null, null }, /* 4,4 */
					{ "S⇔D", null, null }, /* 4,5 */
					{ "", null, null }, /* 4,6 */
					{ "0", null, "x" } /* 4,7 */
			}, { /* ROW 5 */
					{ "7", null, null }, /* 5,0 */
					{ "8", null, null }, /* 5,1 */
					{ "9", null, null }, /* 5,2 */
					{ "⌫", null, null }, /* 5,3 */
					{ "RESET", null, null }, /* 5,4 */
					{ "", null, null }, /* 5,5 */
					{ "", null, null }, /* 5,6 */
					{ "", null, null } /* 5,7 */
			}, { /* ROW 6 */
					{ "4", null, null }, /* 6,0 */
					{ "5", null, null }, /* 6,1 */
					{ "6", null, null }, /* 6,2 */
					{ "*", null, null }, /* 6,3 */
					{ "/", null, null }, /* 6,4 */
					{ "", null, null }, /* 6,5 */
					{ "", null, null }, /* 6,6 */
					{ "", null, null } /* 6,7 */
			}, { /* ROW 7 */
					{ "1", null, null }, /* 7,0 */
					{ "2", null, null }, /* 7,1 */
					{ "3", null, null }, /* 7,2 */
					{ "+", null, null }, /* 7,3 */
					{ "-", null, null }, /* 7,4 */
					{ "", null, null }, /* 7,5 */
					{ "", null, null }, /* 7,6 */
					{ "", null, null } /* 7,7 */
			} };

	static final Key[][][] keyMap = /* [ROW, COLUMN, (0:normal 1:shift 2:alpha)] */
			{ { /* ROW 0 */
					{ Key.SHIFT, Key.SHIFT, Key.SHIFT }, /* 0,0 */
					{ Key.ALPHA, Key.ALPHA, Key.ALPHA }, /* 0,1 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 0,2 */
					{ Key.UP, Key.NONE, Key.NONE }, /* 0,3 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 0,4 */
					{ Key.SETTINGS, Key.NONE, Key.NONE }, /* 0,5 */
					{ Key.BRIGHTNESS_CYCLE, Key.BRIGHTNESS_CYCLE_REVERSE, Key.ZOOM_MODE }, /* 0,6 */
					{ Key.SIMPLIFY, Key.STEP, Key.NONE } /* 0,7 */
			}, { /* ROW 1 */
					{ Key.F4, Key.F4, Key.F4 }, /* 1,0 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 1,1 */
					{ Key.LEFT, Key.NONE, Key.NONE }, /* 1,2 */
					{ Key.OK, Key.NONE, Key.NONE }, /* 1,3 */
					{ Key.RIGHT, Key.NONE, Key.NONE }, /* 1,4 */
					{ Key.HISTORY_BACK, Key.NONE, Key.NONE }, /* 1,5 */
					{ Key.HISTORY_FORWARD, Key.NONE, Key.NONE }, /* 1,6 */
					{ Key.NONE, Key.PI, Key.DRG_CYCLE } /* 1,7 */
			}, { /* ROW 2 */
					{ Key.F3, Key.F3, Key.F3 }, /* 2,0 */
					{ Key.SQRT, Key.ROOT, Key.NONE }, /* 2,1 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 2,2 */
					{ Key.DOWN, Key.NONE, Key.NONE }, /* 2,3 */
					{ Key.BACK, Key.NONE, Key.NONE }, /* 2,4 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 2,5 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 2,6 */
					{ Key.NONE, Key.NONE, Key.LETTER_Z } /* 2,7 */
			}, { /* ROW 3 */
					{ Key.F2, Key.F2, Key.F2 }, /* 3,0 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 3,1 */
					{ Key.POWER_OF_x, Key.NONE, Key.NONE }, /* 3,2 */
					{ Key.POWER_OF_2, Key.NONE, Key.NONE }, /* 3,3 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 3,4 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 3,5 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 3,6 */
					{ Key.DOT, Key.NONE, Key.LETTER_Y } /* 3,7 */
			}, { /* ROW 4 */
					{ Key.F1, Key.F1, Key.F1 }, /* 4,0 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 4,1 */
					{ Key.PARENTHESIS_OPEN, Key.NONE, Key.NONE }, /* 4,2 */
					{ Key.PARENTHESIS_CLOSE, Key.NONE, Key.NONE }, /* 4,3 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 4,4 */
					{ Key.SURD_MODE, Key.NONE, Key.NONE }, /* 4,5 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 4,6 */
					{ Key.NUM0, Key.NONE, Key.LETTER_X } /* 4,7 */
			}, { /* ROW 5 */
					{ Key.NUM7, Key.NONE, Key.NONE }, /* 5,0 */
					{ Key.NUM8, Key.NONE, Key.NONE }, /* 5,1 */
					{ Key.NUM9, Key.NONE, Key.NONE }, /* 5,2 */
					{ Key.DELETE, Key.NONE, Key.NONE }, /* 5,3 */
					{ Key.RESET, Key.NONE, Key.NONE }, /* 5,4 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 5,5 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 5,6 */
					{ Key.NONE, Key.NONE, Key.NONE } /* 5,7 */
			}, { /* ROW 6 */
					{ Key.NUM4, Key.NONE, Key.NONE }, /* 6,0 */
					{ Key.NUM5, Key.NONE, Key.NONE }, /* 6,1 */
					{ Key.NUM6, Key.NONE, Key.NONE }, /* 6,2 */
					{ Key.MULTIPLY, Key.NONE, Key.NONE }, /* 6,3 */
					{ Key.DIVIDE, Key.NONE, Key.NONE }, /* 6,4 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 6,5 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 6,6 */
					{ Key.NONE, Key.NONE, Key.NONE } /* 6,7 */
			}, { /* ROW 7 */
					{ Key.NUM1, Key.NONE, Key.NONE }, /* 7,0 */
					{ Key.NUM2, Key.NONE, Key.NONE }, /* 7,1 */
					{ Key.NUM3, Key.NONE, Key.NONE }, /* 7,2 */
					{ Key.PLUS, Key.PLUS_MINUS, Key.NONE }, /* 7,3 */
					{ Key.MINUS, Key.NONE, Key.NONE }, /* 7,4 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 7,5 */
					{ Key.NONE, Key.NONE, Key.NONE }, /* 7,6 */
					{ Key.NONE, Key.NONE, Key.NONE } /* 7,7 */
			} };

	public static String getKeyName(final int row, final int col) {
		return Keyboard.getKeyName(row, col, Keyboard.shift, Keyboard.alpha);
	}

	public static String getKeyName(final int row, final int col, final boolean shift, final boolean alpha) {
		final String[] keyValues = Keyboard.KeyLabelsMap[row][col];
		if (shift) {
			if (keyValues[1] != null) {
				return keyValues[1];
			}
		} else if (alpha) {
			if (keyValues[2] != null) {
				return keyValues[2];
			}
		}
		return keyValues[0];
	}

	public static boolean hasKeyName(final int row, final int col) {
		final String[] keyValues = Keyboard.KeyLabelsMap[row][col];
		if (Keyboard.shift) {
			return keyValues[1] != null;
		} else if (Keyboard.alpha) {
			return keyValues[2] != null;
		} else {
			return true;
		}
	}

	/**
	 * 
	 * @param row
	 * @param col
	 * @param released true: released, false: pressed
	 */
	public static synchronized void keyRaw(final int row, final int col, final boolean released) {
//		KeyboardDebugScreen.keyX = row;
//		KeyboardDebugScreen.keyY = col;
		final Key k = Keyboard.keyMap[row][col][Keyboard.shift ? 1 : Keyboard.alpha ? 2 : 0];
		if (k != null) {
			if (released) {
				Keyboard.keyReleased(k);
			} else {
				Keyboard.keyPressed(k);
			}
		} else {
			if (released) {
				Keyboard.keyReleased(Key.NONE);
			} else {
				Keyboard.keyPressed(Key.NONE);
			}
		}
	}

	public static void stopKeyboard() {
		if (WarpPI.getPlatform().isRunningOnRaspberry()) {
			WarpPI.getPlatform().getGpio().digitalWrite(33, false);
			WarpPI.getPlatform().getGpio().digitalWrite(35, false);
			WarpPI.getPlatform().getGpio().digitalWrite(36, false);
			WarpPI.getPlatform().getGpio().digitalWrite(37, false);
			WarpPI.getPlatform().getGpio().digitalWrite(38, false);
			WarpPI.getPlatform().getGpio().digitalWrite(40, false);
		}
	}

	public synchronized static void keyPressed(final Key k) {
		boolean done = false;
		if (Keyboard.additionalListener != null) {
			try {
				done = Keyboard.additionalListener.onKeyPressed(new KeyPressedEvent(k));
			} catch (final Exception ex) {
				new GUIErrorMessage(ex);
			}
		}
		if (WarpPI.INSTANCE.getHardwareDevice().getDisplayManager() != null) {
			final Screen scr = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
			boolean refresh = false;
			boolean scrdone = false;
			try {
				scrdone = scr.onKeyPressed(new KeyPressedEvent(k));
			} catch (final Exception ex) {
				new GUIErrorMessage(ex);
			}
			if (scr != null && scr.initialized && scrdone) {
				refresh = true;
			} else {
				switch (k) {
					case POWEROFF:
						WarpPI.INSTANCE.getHardwareDevice().getDeviceStateDevice().powerOff();
						break;
					case NONE:
						break;
					case BRIGHTNESS_CYCLE:
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(new TetrisScreen()); //TODO: rimuovere: prova
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().cycleBrightness(false);
						refresh = true;
						break;
					case BRIGHTNESS_CYCLE_REVERSE:
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(new MarioScreen()); //TODO: rimuovere: prova
						WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().cycleBrightness(true);
						refresh = true;
						break;
					case ZOOM_MODE:
						final float newZoom = StaticVars.windowZoom.getLastValue() % 3 + 1;
						StaticVars.windowZoom.onNext(newZoom);
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
			done = Keyboard.additionalListener.onKeyReleased(new KeyReleasedEvent(k));
		}
		boolean refresh = false;
		if (WarpPI.INSTANCE.getHardwareDevice().getDisplayManager() != null) {
			final Screen scr = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
			if (scr != null && scr.initialized && scr.onKeyReleased(new KeyReleasedEvent(k))) {
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