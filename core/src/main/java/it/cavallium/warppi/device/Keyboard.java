package it.cavallium.warppi.device;

import java.awt.event.KeyEvent;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.chip.ParallelToSerial;
import it.cavallium.warppi.device.chip.SerialToParallel;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.gui.GUIErrorMessage;
import it.cavallium.warppi.gui.screens.KeyboardDebugScreen;
import it.cavallium.warppi.gui.screens.MarioScreen;
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
			if (Engine.getPlatform().getSettings().isDebugEnabled()) {
				try {
					while (true) {
						if (Keyboard.debugKeyCode != -1) {
							Keyboard.debugKeyPressed(Keyboard.debugKeyCode);
							Keyboard.debugKeyCode = -1;
						}
						if (Keyboard.debugKeyCodeRelease != -1) {
							Keyboard.debugKeyReleased(Keyboard.debugKeyCodeRelease);
							Keyboard.debugKeyCodeRelease = -1;
						}
						Thread.sleep(50);
					}
				} catch (final InterruptedException e) {}
			} else {
				Engine.getPlatform().getGpio().pinMode(Keyboard.CLK_INH_pin, Engine.getPlatform().getGpio().valueOutput());
				Engine.getPlatform().getGpio().pinMode(Keyboard.RCK_pin, Engine.getPlatform().getGpio().valueOutput());
				Engine.getPlatform().getGpio().pinMode(Keyboard.SER_pin, Engine.getPlatform().getGpio().valueOutput());
				Engine.getPlatform().getGpio().pinMode(Keyboard.SH_LD_pin, Engine.getPlatform().getGpio().valueOutput());
				Engine.getPlatform().getGpio().pinMode(Keyboard.SCK_and_CLK_pin, Engine.getPlatform().getGpio().valueOutput());
				Engine.getPlatform().getGpio().pinMode(Keyboard.QH_pin, Engine.getPlatform().getGpio().valueInput());

				Engine.getPlatform().getGpio().digitalWrite(Keyboard.CLK_INH_pin, false);
				Engine.getPlatform().getGpio().digitalWrite(Keyboard.RCK_pin, false);
				Engine.getPlatform().getGpio().digitalWrite(Keyboard.SER_pin, false);
				Engine.getPlatform().getGpio().digitalWrite(Keyboard.SH_LD_pin, false);
				Engine.getPlatform().getGpio().digitalWrite(Keyboard.SCK_and_CLK_pin, false);
				Engine.getPlatform().getGpio().digitalWrite(Keyboard.QH_pin, false);
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
								Keyboard.keyPressedRaw(row, col);
							} else if (data[row] == false && Keyboard.precedentStates[row][col] == true) {
								Keyboard.keyReleasedRaw(row, col);
							}
//								KeyboardDebugScreen.log("Released button at " + (row + 1) + ", " + (col + 1));
							Keyboard.precedentStates[row][col] = data[row];
						}
					}
				}
			}
		});
		Engine.getPlatform().setThreadName(kt, "Keyboard thread");
		kt.setPriority(Thread.NORM_PRIORITY + 1);
		Engine.getPlatform().setThreadDaemon(kt);
		kt.start();
	}

	public static void debugKeyPressed(final int keyCode) {
		switch (keyCode) {
			case KeyEvent.VK_ESCAPE:
				Keyboard.keyPressed(Key.POWEROFF);
				break;
			case KeyEvent.VK_S:
				if (Keyboard.shift) {
					Keyboard.keyPressed(Key.ARCSINE);
				} else if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_S);
				} else {
					Keyboard.keyPressed(Key.SINE);
				}
				break;
			case KeyEvent.VK_C:
				if (Keyboard.shift) {
					Keyboard.keyPressed(Key.ARCCOSINE);
				} else if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_C);
				} else {
					Keyboard.keyPressed(Key.COSINE);
				}
				break;
			case KeyEvent.VK_T:
				if (Keyboard.shift) {
					Keyboard.keyPressed(Key.ARCTANGENT);
				} else if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_T);
				} else {
					Keyboard.keyPressed(Key.TANGENT);
				}
				break;
			case KeyEvent.VK_D:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.debug_DEG);
				} else if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_D);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_R:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.debug_RAD);
				} else if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_R);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_G:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.debug_GRA);
				} else if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_G);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_X:
				if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_X);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_P:
				if (Keyboard.shift) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_P);
				} else {
					Keyboard.keyPressed(Key.PI);
				}
				break;
			case KeyEvent.VK_E:
				if (Keyboard.shift) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_E);
				} else {
					Keyboard.keyPressed(Key.EULER_NUMBER);
				}
				break;
			case KeyEvent.VK_Y:
				if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_Y);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_B:
				if (Keyboard.shift) {
					Keyboard.keyPressed(Key.BRIGHTNESS_CYCLE_REVERSE);
				} else if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.BRIGHTNESS_CYCLE);
				} else {
					Keyboard.keyPressed(Key.LETTER_B);
				}
				break;
			case KeyEvent.VK_L:
				if (Keyboard.shift) {
					Keyboard.keyPressed(Key.LOGARITHM);
				} else if (Keyboard.alpha) {
					Keyboard.keyPressed(Key.LETTER_L);
				} else {
					Keyboard.keyPressed(Key.LOGARITHM);
				}
				break;
			case KeyboardJogampValues.VK_ENTER:
			case KeyEvent.VK_ENTER:
				if (Keyboard.shift) {
					Keyboard.keyPressed(Key.STEP);
				} else if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.SIMPLIFY);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				int row = 2;
				int col = 1;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				break;
			case KeyEvent.VK_1:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM1);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_2:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM2);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_3:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM3);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_4:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM4);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_5:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM5);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_6:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM6);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_7:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM7);
				} else if (Keyboard.shift) {
					if (Engine.getPlatform().getSettings().isDebugEnabled()) {
						Keyboard.keyPressed(Key.DIVIDE);
					}
				}
				break;
			case KeyEvent.VK_8:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM8);
				} else if (Keyboard.shift) {
					Keyboard.keyPressed(Key.PARENTHESIS_OPEN);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_9:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM9);
				} else if (Keyboard.shift) {
					Keyboard.keyPressed(Key.PARENTHESIS_CLOSE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_0:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NUM0);
				} else if (Keyboard.shift) {
					Keyboard.keyPressed(Key.EQUAL);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_M:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.SURD_MODE);
				} else if (Keyboard.shift) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.LETTER_M);
				}
				break;
			case KeyboardJogampValues.VK_ADD:
			case KeyEvent.VK_ADD:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.PLUS);
				} else if (Keyboard.shift) {
					Keyboard.keyPressed(Key.PLUS_MINUS);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_SUBTRACT:
			case KeyEvent.VK_SUBTRACT:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.MINUS);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_MULTIPLY:
			case KeyEvent.VK_MULTIPLY:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.MULTIPLY);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_DIVIDE:
			case KeyEvent.VK_DIVIDE:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.DIVIDE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_BACK_SPACE:
				Keyboard.keyPressed(Key.DELETE);
				break;
			case KeyboardJogampValues.VK_DELETE:
			case KeyEvent.VK_DELETE:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.RESET);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_LEFT:
			case KeyEvent.VK_LEFT:
				//LEFT
				row = 2;
				col = 3;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.LEFT);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_RIGHT:
			case KeyEvent.VK_RIGHT:
				//RIGHT
				row = 2;
				col = 5;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.RIGHT);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_UP:
			case KeyEvent.VK_UP:
				//UP
				row = 1;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.UP);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_DOWN:
			case KeyEvent.VK_DOWN:
				//DOWN
				row = 3;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.DOWN);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case (short) 12:
				//DOWN
				row = 2;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = true;
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.OK);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_NUMPAD4:
			case KeyEvent.VK_NUMPAD4:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.HISTORY_BACK);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_NUMPAD6:
			case KeyEvent.VK_NUMPAD6:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.HISTORY_FORWARD);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_PERIOD:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.DOT);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_A:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_A);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_F:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_F);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_H:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_H);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_I:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_I);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_J:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_J);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_K:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_K);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_N:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_N);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_O:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_O);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_Q:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_Q);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_U:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_U);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_V:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_V);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_W:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_W);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyEvent.VK_Z:
				if (!Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.NONE);
				} else if (Keyboard.alpha && !Keyboard.shift) {
					Keyboard.keyPressed(Key.LETTER_Z);
				} else if (Keyboard.shift && !Keyboard.alpha) {
					Keyboard.keyPressed(Key.ZOOM_MODE);
				} else {
					Keyboard.keyPressed(Key.NONE);
				}
				break;
			case KeyboardJogampValues.VK_SHIFT:
			case KeyEvent.VK_SHIFT:
				Keyboard.keyPressed(Key.SHIFT);
				break;
			case KeyEvent.VK_CONTROL:
				Keyboard.keyPressed(Key.ALPHA);
				break;
			case KeyboardJogampValues.VK_NUMPAD1:
			case KeyEvent.VK_NUMPAD1:
				Keyboard.keyPressed(Key.SQRT);
				break;
			case KeyboardJogampValues.VK_NUMPAD2:
			case KeyEvent.VK_NUMPAD2:
				Keyboard.keyPressed(Key.ROOT);
				break;
			case KeyboardJogampValues.VK_NUMPAD3:
			case KeyEvent.VK_NUMPAD3:
				Keyboard.keyPressed(Key.POWER_OF_2);
				break;
			case KeyboardJogampValues.VK_NUMPAD5:
			case KeyEvent.VK_NUMPAD5:
				Keyboard.keyPressed(Key.POWER_OF_x);
				break;
		}
	}

	private synchronized static void debugKeyReleased(final int keyCode) {
		switch (keyCode) {
			case KeyEvent.VK_ENTER:
				int row = 2;
				int col = 1;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				break;
			case KeyboardJogampValues.VK_LEFT:
			case KeyEvent.VK_LEFT:
				//LEFT
				row = 2;
				col = 3;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				break;
			case KeyboardJogampValues.VK_RIGHT:
			case KeyEvent.VK_RIGHT:
				//RIGHT
				row = 2;
				col = 5;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				System.out.println("RELEASE");
				break;
			case KeyboardJogampValues.VK_UP:
			case KeyEvent.VK_UP:
				//UP
				row = 1;
				col = 4;
				Keyboard.debugKeysDown[row - 1][col - 1] = false;
				break;
			case KeyboardJogampValues.VK_DOWN:
			case KeyEvent.VK_DOWN:
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

	public static boolean isKeyDown(final int row, final int col) {
		if (Engine.getPlatform().getSettings().isDebugEnabled() == false) {
			return Keyboard.precedentStates[row - 1][col - 1];
		} else {
			return Keyboard.debugKeysDown[row - 1][col - 1];
		}
	}

	public synchronized static void keyReleasedRaw(final int row, final int col) {
//		KeyboardDebugScreen.keyX = row;
//		KeyboardDebugScreen.keyY = col;
		if (row == 1 && col == 1) {
			//keyReleased(Key.BRIGHTNESS_CYCLE);
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
					{ "↤", null, null }, /* 1,5 */
					{ "↦", null, null }, /* 1,6 */
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
					{ Key.NONE, Key.NONE, Key.NONE }, /* 0,3 */
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

	public static synchronized void keyPressedRaw(final int row, final int col) {
//		KeyboardDebugScreen.keyX = row;
//		KeyboardDebugScreen.keyY = col;
		final Key k = Keyboard.keyMap[row][col][Keyboard.shift ? 1 : Keyboard.alpha ? 2 : 0];
		if (k != null) {
			Keyboard.keyPressed(k);
		} else {
			Keyboard.keyPressed(Key.NONE);
		}
	}

	public static void stopKeyboard() {
		if (Engine.getPlatform().getSettings().isDebugEnabled() == false) {
			Engine.getPlatform().getGpio().digitalWrite(33, false);
			Engine.getPlatform().getGpio().digitalWrite(35, false);
			Engine.getPlatform().getGpio().digitalWrite(36, false);
			Engine.getPlatform().getGpio().digitalWrite(37, false);
			Engine.getPlatform().getGpio().digitalWrite(38, false);
			Engine.getPlatform().getGpio().digitalWrite(40, false);
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
		if (Engine.INSTANCE.getHardwareDevice().getDisplayManager() != null) {
			final Screen scr = Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
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
						Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.destroy();
						break;
					case NONE:
						break;
					case BRIGHTNESS_CYCLE:
						Engine.INSTANCE.getHardwareDevice().getDisplayManager().cycleBrightness(false);
						refresh = true;
						break;
					case BRIGHTNESS_CYCLE_REVERSE:
						Engine.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(new MarioScreen()); //TODO: rimuovere: prova
						Engine.INSTANCE.getHardwareDevice().getDisplayManager().cycleBrightness(true);
						refresh = true;
						break;
					case ZOOM_MODE:
						final float newZoom = StaticVars.windowZoom.getLastValue() % 3 + 1;
						StaticVars.windowZoom.onNext(newZoom);
						Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "Keyboard", "Zoom: " + newZoom);
//						StaticVars.windowZoom = ((StaticVars.windowZoom - 0.5f) % 2f) + 1f;
						refresh = true;
						break;
					case HISTORY_BACK:
						Engine.INSTANCE.getHardwareDevice().getDisplayManager().goBack();
						refresh = true;
						break;
					case HISTORY_FORWARD:
						Engine.INSTANCE.getHardwareDevice().getDisplayManager().goForward();
						refresh = true;
						break;
					default:
						break;
				}
			}
			switch (k) {
				case SHIFT:
					if (Keyboard.alpha) {
						Engine.getPlatform().alphaChanged(Keyboard.alpha = false);
					}
					Engine.getPlatform().shiftChanged(Keyboard.shift = !Keyboard.shift);
					refresh = true;
					break;
				case ALPHA:
					if (Keyboard.shift) {
						Engine.getPlatform().shiftChanged(Keyboard.shift = false);
					}
					Engine.getPlatform().alphaChanged(Keyboard.alpha = !Keyboard.alpha);
					refresh = true;
					break;
				default:
					if (k != Key.NONE) {
						if (Keyboard.shift) {
							Engine.getPlatform().shiftChanged(Keyboard.shift = false);
						}
						if (Keyboard.alpha) {
							Engine.getPlatform().alphaChanged(Keyboard.alpha = false);
						}
					}
					break;
			}
			if (refresh) {
				Keyboard.refreshRequest = true;
			}
		} else if (!done) {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Key " + k.toString() + " ignored.");
		}
	}

	public synchronized static void keyReleased(final Key k) {
		boolean done = false;
		if (Keyboard.additionalListener != null) {
			done = Keyboard.additionalListener.onKeyReleased(new KeyReleasedEvent(k));
		}
		boolean refresh = false;
		if (Engine.INSTANCE.getHardwareDevice().getDisplayManager() != null) {
			final Screen scr = Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
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
			Engine.getPlatform().getConsoleUtils().out().println(1, "Key " + k.toString() + " ignored.");
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