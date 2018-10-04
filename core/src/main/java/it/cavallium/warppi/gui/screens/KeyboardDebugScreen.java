package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class KeyboardDebugScreen extends Screen {

	public String key;
	public String keyevent;
	public static int keyX;
	public static int keyY;
	public static String[] log = new String[] { "POWER ON", "LOADING", "LOADED", "DONE", "---" };
	public long beforetime;

	public KeyboardDebugScreen() {
		super();
		canBeInHistory = false;
	}

	@Override
	public void created() throws InterruptedException {}

	@Override
	public void initialized() throws InterruptedException {}

	@Override
	public void render() {
		final Renderer renderer = Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer;
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().fonts[2].use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
		renderer.glColor4f(0.75f, 0.0f, 0.0f, 1.0f);
		renderer.glDrawStringRight(StaticVars.screenSize[0] - 10, 30, "-" + keyevent.toUpperCase() + "-");
		if (keyevent != "NONE") {
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().fonts[2].use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
			renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			renderer.glDrawStringLeft(10, 30, "Key position");
			renderer.glDrawStringLeft(10, 45, "X: " + KeyboardDebugScreen.keyX + ", Y:" + KeyboardDebugScreen.keyY);
			renderer.glDrawStringLeft(10, 65, "Key value");
			renderer.glDrawStringLeft(10, 80, key);
		}
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().fonts[3].use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
		renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		for (int i = 0; i < 5; i++) {
			if (KeyboardDebugScreen.log[i] != null) {
				renderer.glDrawStringLeft(10, 230 + 15 * (i + 1), KeyboardDebugScreen.log[i].toUpperCase());
			}
		}

		//FROM SERIAL
		renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		renderer.glFillColor(-80 + 100 + 200, 90, 5, 5);
		renderer.glFillColor(-80 + 100, 100, 200, 70);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().fonts[2].use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
		renderer.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		renderer.glDrawStringCenter(-80 + 100 + 200 / 2, 100 + 70 / 2 - renderer.getCurrentFont().getCharacterHeight() / 2, "FROM SERIAL");
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().fonts[3].use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
		renderer.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
		for (int i = 0; i < 8; i++) {
			if (KeyboardDebugScreen.pinsA[i] == 1) {
				renderer.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			} else if (KeyboardDebugScreen.pinsA[i] == 2) {
				renderer.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
			} else if (KeyboardDebugScreen.pinsA[i] == -1) {
				renderer.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
			} else if (KeyboardDebugScreen.pinsA[i] == 0) {
				renderer.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
			}
			renderer.glFillColor(-80 + 103 + 25 * (7 - i), 80, 20, 20);
			renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			renderer.glDrawStringCenter(-80 + 113 + 25 * (7 - i), 90 - renderer.getCurrentFont().getCharacterHeight() / 2, "" + (i + 1));
		}
		for (int i = 15; i >= 8; i--) {
			if (KeyboardDebugScreen.pinsA[i] == 1) {
				renderer.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			} else if (KeyboardDebugScreen.pinsA[i] == 2) {
				renderer.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
			} else if (KeyboardDebugScreen.pinsA[i] == -1) {
				renderer.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
			} else if (KeyboardDebugScreen.pinsA[i] == 0) {
				renderer.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
			}
			renderer.glFillColor(-80 + 103 + 25 * (i - 8), 170, 20, 20);
			renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			renderer.glDrawStringCenter(-80 + 113 + 25 * (i - 8), 180 - renderer.getCurrentFont().getCharacterHeight() / 2, "" + (i + 1));
		}
		for (int i = 0; i < 8; i++) {
			if (KeyboardDebugScreen.dataA[i]) {
				renderer.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			} else {
				renderer.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
			}
			renderer.glFillColor(-80 + 160 + 10 * i, 150, 8, 8);
		}

		//TO SERIAL
		renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		renderer.glFillColor(150 + 90, 200, 5, 5);
		renderer.glFillColor(150 + 100, 100, 200, 70);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().fonts[2].use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
		renderer.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		renderer.glDrawStringCenter(150 + 100 + 200 / 2, 100 + 70 / 2 - renderer.getCurrentFont().getCharacterHeight() / 2, "TO SERIAL");
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().fonts[3].use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
		renderer.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
		for (int i = 15; i >= 8; i--) {
			if (KeyboardDebugScreen.pinsB[i] == 1) {
				renderer.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			} else if (KeyboardDebugScreen.pinsB[i] == 2) {
				renderer.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
			} else if (KeyboardDebugScreen.pinsB[i] == -1) {
				renderer.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
			} else if (KeyboardDebugScreen.pinsB[i] == 0) {
				renderer.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
			}
			renderer.glFillColor(150 + 103 + 25 * (15 - i), 80, 20, 20);
			renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			renderer.glDrawStringCenter(150 + 113 + 25 * (15 - i), 90 - renderer.getCurrentFont().getCharacterHeight() / 2, "" + (i + 1));
		}
		for (int i = 7; i >= 0; i--) {
			if (KeyboardDebugScreen.pinsB[i] == 1) {
				renderer.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			} else if (KeyboardDebugScreen.pinsB[i] == 2) {
				renderer.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
			} else if (KeyboardDebugScreen.pinsB[i] == -1) {
				renderer.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
			} else if (KeyboardDebugScreen.pinsB[i] == 0) {
				renderer.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
			}
			renderer.glFillColor(150 + 103 + 25 * i, 170, 20, 20);
			renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			renderer.glDrawStringCenter(150 + 113 + 25 * i, 180 - renderer.getCurrentFont().getCharacterHeight() / 2, "" + (i + 1));
		}
		for (int i = 0; i < 8; i++) {
			if (KeyboardDebugScreen.dataB[i]) {
				renderer.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			} else {
				renderer.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
			}
			renderer.glFillColor(150 + 160 + 10 * i, 150, 8, 8);
		}

		//GPIO
		for (int i = 0; i < 40; i++) {
			if (KeyboardDebugScreen.gpio[i] == true) {
				renderer.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			} else {
				renderer.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
			}
			if (i % 2 == 0) {
				renderer.glFillColor(53 + 15 * (i / 2), 50, 5, 5);
				renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
				renderer.glDrawStringCenter(55 + 15 * (i / 2), 60, "" + (i + 1));
			} else {
				renderer.glFillColor(53 + 15 * ((i - 1) / 2), 40, 5, 5);
				renderer.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
				renderer.glDrawStringCenter(55 + 15 * ((i - 1) / 2), 35 - renderer.getCurrentFont().getCharacterHeight(), "" + (i + 1));
			}
		}

		//KEYS
		for (int c = 0; c < 8; c++) {
			for (int r = 0; r < 8; r++) {
				if (KeyboardDebugScreen.ks[c][r]) {
					renderer.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
				} else {
					renderer.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
				}
				renderer.glFillColor(250 + 6 * c, 250 + 6 * r, 5, 5);
			}
		}
	}

	@Override
	public void beforeRender(final float dt) {
		if (System.currentTimeMillis() - beforetime >= 1000) {
			keyevent = "NONE";
			KeyboardDebugScreen.keyX = 0;
			KeyboardDebugScreen.keyY = 0;
			key = "";
		}
	}

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

	@Override
	public boolean onKeyPressed(final KeyPressedEvent k) {
		beforetime = System.currentTimeMillis();
		keyevent = "PRESSED";
		KeyboardDebugScreen.keyX = 0;
		KeyboardDebugScreen.keyY = 0;
		key = k.toString();
		return false;
	}

	@Override
	public boolean onKeyReleased(final KeyReleasedEvent k) {
		beforetime = System.currentTimeMillis();
		keyevent = "RELEASED";
		KeyboardDebugScreen.keyX = 0;
		KeyboardDebugScreen.keyY = 0;
		key = k.toString();
		return false;
	}

	public static int[] pinsA = new int[] { 2, 2, 2, 2, 2, 2, 2, 1, -1, -1, 0, 0, 0, 0, 2, -1 };
	public static int[] pinsB = new int[] { 0, 0, 2, 2, 2, 2, -1, 1, 0, -1, 2, 2, 2, 2, 0, -1 };
	public static boolean[] dataA = new boolean[8];
	public static boolean[] dataB = new boolean[8];
	public static boolean[][] ks = new boolean[8][8];
	public static boolean[] gpio = new boolean[40];

	public static void log(final String str) {
		final String[] newlog = KeyboardDebugScreen.log;
		for (int i = 1; i < 5; i++) {
			newlog[i - 1] = newlog[i];
		}
		newlog[4] = "[" + System.currentTimeMillis() + "]" + str;
		KeyboardDebugScreen.log = newlog;
	}

	@Override
	public String getSessionTitle() {
		return "Keyboard debug";
	}

}
