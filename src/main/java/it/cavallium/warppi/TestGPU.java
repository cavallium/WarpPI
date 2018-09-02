package it.cavallium.warppi;

import java.io.IOException;

import it.cavallium.warppi.deps.DEngine;
import it.cavallium.warppi.deps.DSystem;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.containers.NormalInputContainer;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.parser.MathParser;

public class TestGPU {

	public static final GraphicEngine d = DEngine.newGPUEngine();

	public static void main(String[] args) throws IOException, Error {
		StaticVars.debugOn = true;
		Utils.debugThirdScreen = false;
		d.create();

		Keyboard k = new Keyboard();
		k.startKeyboard();
		k.setAdditionalKeyboardListener(new KeyboardEventListener() {
			@Override
			public boolean onKeyPressed(KeyPressedEvent k) {
				try {
					switch (k.getKey()) {
						case LEFT:
							c.moveLeft();
							return true;
						case RIGHT:
							c.moveRight();
							return true;
						case NUM0:
							c.typeChar('0');
							return true;
						case NUM1:
							c.typeChar('1');
							return true;
						case NUM2:
							c.typeChar('2');
							return true;
						case NUM3:
							c.typeChar('3');
							return true;
						case NUM4:
							c.typeChar('4');
							return true;
						case NUM5:
							c.typeChar('5');
							return true;
						case NUM6:
							c.typeChar('6');
							return true;
						case NUM7:
							c.typeChar('7');
							return true;
						case NUM8:
							c.typeChar('8');
							return true;
						case NUM9:
							c.typeChar('9');
							return true;
						case PLUS:
							c.typeChar(MathematicalSymbols.SUM);
							return true;
						case MINUS:
							c.typeChar(MathematicalSymbols.SUBTRACTION);
							return true;
						case MULTIPLY:
							c.typeChar(MathematicalSymbols.MULTIPLICATION);
							return true;
						case DIVIDE:
							c.typeChar(MathematicalSymbols.DIVISION);
							return true;
						case SQRT:
							c.typeChar(MathematicalSymbols.SQUARE_ROOT);
							return true;
						case PARENTHESIS_OPEN:
						case SINE:
							c.typeChar(MathematicalSymbols.PARENTHESIS_OPEN);
							return true;
						case PARENTHESIS_CLOSE:
						case debug_DEG:
							c.moveRight();
							return true;
						case DELETE:
							c.del();
							return true;
						case RESET:
							c.clear();
							return true;
						case POWEROFF:
							d.destroy();
							DSystem.exit(0);
							return true;
						case EQUAL:
							Expression expr;
							try {
								expr = MathParser.parseInput(new MathContext(), c);
								System.out.println("Parsed input:" + expr.toString());
							} catch (final Error e) {
								e.printStackTrace();
							}
						default:
							break;
					}
					return false;
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
				return false;

			}

			@Override
			public boolean onKeyReleased(KeyReleasedEvent k) {
				return false;

			}
		});

		new Scene(d);
	}

	private static NormalInputContainer c = null;

	private static class Scene implements RenderingLoop {

		private final BinaryFont exampleFont;
		private final Skin exampleSkin;

		private final Renderer r;
		private final GraphicEngine d;
		private long lastTime = 0L;

		public Scene(GraphicEngine d) throws IOException, Error {
			this.d = d;
			r = d.getRenderer();

			exampleFont = d.loadFont("norm");

			exampleSkin = d.loadSkin("skin.png");

			BlockContainer.initializeFonts(d.loadFont("norm"), d.loadFont("smal"));

			//New expression framework test
			c = new NormalInputContainer(new InputContext(), false, 0, 200);
			c.typeChar(MathematicalSymbols.DIVISION);
			c.typeChar('5');
			c.typeChar(MathematicalSymbols.MULTIPLICATION);
			c.typeChar('2');
			c.moveRight();
			c.typeChar('2');
			c.moveRight();
			c.typeChar(MathematicalSymbols.MULTIPLICATION);
			c.typeChar('2');
			c.typeChar('2');
			c.recomputeDimensions();

			d.start(this);

//			fonts = new RAWFont[1];
//			textures = new int[100];
//			fonts[0] = new RAWFont();
//			fonts[0].create("big");
			new Thread(() -> {
				try {
					for (int i = 0; i < 12; i++) {
						Utils.printSystemResourcesUsage();
						Thread.sleep(5000);
					}
				} catch (final InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!StaticVars.debugOn) {
					d.destroy();
					DSystem.exit(0);
				}
			}).start();

			d.waitForExit();
		}

		@Override
		public void refresh() {
			exampleSkin.use(d);
			r.glClearColor(0xFFD0FDCF);
			r.glClear(d.getWidth(), d.getHeight());
			r.glColor3f(1.0f, 1.0f, 1.0f);
			r.glFillRect(2, 2, 160, 160, 0, 0, 16, 16);
			exampleFont.use(d);
			r.glColor3f(1, 0, 0);
			r.glDrawStringLeft(10, 170, "Prova! 123456789 222");

			//MSAA TEST
			r.glDrawStringLeft(10f, 190.5f, "Test MSAA");
			exampleSkin.use(d);
			r.glColor3f(1.0f, 1.0f, 1.0f);
			r.glFillRect(162, 2.5f, 160, 160, 0, 0, 16, 16);

			//New expression framework test
			if (lastTime == 0) {
				lastTime = System.currentTimeMillis();
			}
			final double delta = System.currentTimeMillis() - lastTime;
			lastTime = System.currentTimeMillis();
			c.beforeRender((float) (delta / 1000d));
			c.draw(d, r, 10, 220);
		}

	}
}
