/**
 * Copyright 2012-2013 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.nativewindow.egl.EGLGraphicsDevice;
import com.jogamp.newt.Display;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.Texture;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.util.EventSubmitter;
import jogamp.newt.driver.bcm.vc.iv.DisplayDriver;
import jogamp.newt.driver.bcm.vc.iv.ScreenDriver;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 *
 * @author Xerxes Rånby (xranby)
 * @author Andrea Cavalli (@Cavallium)
 */

class NEWTWindow implements GLEventListener {

	private final JOGLEngine engine;
	private final JOGLRenderer renderer;
	public GLWindow window;
	public volatile float windowZoom = 1f;
	public int[] realWindowSize;
	public Runnable onInitialized;
	public volatile boolean refreshViewport;

	final EventSubmitter<Integer[]> onRealResize;
	final EventSubmitter<Integer[]> onResizeEvent = EventSubmitter.create();
	private final EventSubmitter<Float> onZoom = EventSubmitter.create();
	private final EventSubmitter<GL2ES1> onGLContext = EventSubmitter.create();

	public NEWTWindow(final JOGLEngine engine) {
		this.engine = engine;
		renderer = engine.getRenderer();
		engine.size[0] = engine.getSize()[0];
		engine.size[1] = engine.getSize()[1];
		realWindowSize = new int[] { engine.getSize()[0], engine.getSize()[1] };
		windowZoom = StaticVars.windowZoom.getLastValue();
		onRealResize = EventSubmitter.create(new Integer[] { (int) (engine.getSize()[0] * windowZoom), (int) (engine.getSize()[1] * windowZoom) });

		onRealResize.subscribe((realSize) -> {
			realWindowSize[0] = realSize[0];
			realWindowSize[1] = realSize[1];
			engine.size[0] = realSize[0] / (int) windowZoom;
			engine.size[1] = realSize[1] / (int) windowZoom;
			onResizeEvent.submit(new Integer[] { engine.size[0], engine.size[1] });
			refreshViewport = true;
		});
		StaticVars.windowZoom.subscribe(onZoom::submit);
		onZoom.subscribe((z) -> {
			if (windowZoom != 0) {
				windowZoom = z;
				engine.size[0] = (int) (realWindowSize[0] / windowZoom);
				engine.size[1] = (int) (realWindowSize[1] / windowZoom);
				engine.getSize()[0] = engine.size[0];
				engine.getSize()[1] = engine.size[1];
				refreshViewport = true;
			}
		});
	}

	public void create() {
		System.out.println("Loading OpenGL...");
		GLProfile.initSingleton();
		try {
			System.out.println(GLProfile.glAvailabilityToString());
		} catch (Exception ex) {
			System.out.println("OpenGL Capabilities are not available. " + ex.getLocalizedMessage());
		}
		if (!GLProfile.isAvailable(GLProfile.GL2ES1)) {
			System.err.println("Le OpenGL non sono presenti su questo computer!");
			return;
		}
		if (WarpPI.getPlatform().getSettings().isDebugEnabled())
			System.setProperty("jnlp.newt.window.icons", "res/icons/calculator-016.png res/icons/calculator-018.png res/icons/calculator-256.png");
		final GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2ES1));
		System.out.println("Loaded OpenGL");
		// We may at this point tweak the caps and request a translucent drawable
		caps.setHardwareAccelerated(true);
		caps.setBackgroundOpaque(true); //transparency window
//		caps.setSampleBuffers(true);
//		caps.setNumSamples(4);

		final GLWindow glWindow = GLWindow.create(caps);
		window = glWindow;

		glWindow.setTitle("WarpPI Calculator by Andrea Cavalli (@Cavallium)");

		glWindow.addWindowListener(new WindowListener() {

			@Override
			public void windowDestroyNotify(final WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDestroyed(final WindowEvent e) {
				if (engine.isInitialized())
					engine.destroy();
			}

			@Override
			public void windowGainedFocus(final WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowLostFocus(final WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowMoved(final WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowRepaint(final WindowUpdateEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowResized(final WindowEvent e) {

			}

		});
		glWindow.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(final KeyEvent arg0) {
				switch (arg0.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						int row = 2;
						int col = 1;
						Keyboard.debugKeysDown[row - 1][col - 1] = true;
						break;
					default: {
					}
				}
				Key click = convertSimpleKey(arg0);
				if (click != null) {
					Keyboard.keyPressed(click);
				}
			}

			@Override
			public void keyReleased(final KeyEvent arg0) {
				switch (arg0.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						int row = 2;
						int col = 1;
						Keyboard.debugKeysDown[row - 1][col - 1] = false;
						break;
					case 0x15:
					case KeyEvent.VK_SHIFT:
						if (Keyboard.shift) {
							Keyboard.keyPressed(Key.SHIFT);
							Keyboard.keyReleased(Key.SHIFT);
						}
						break;
					case KeyEvent.VK_CONTROL:
						if (Keyboard.alpha) {
							Keyboard.keyPressed(Key.ALPHA);
							Keyboard.keyReleased(Key.ALPHA);
						}
						break;
					default: {
					}
				}
				Key click = convertSimpleKey(arg0);
				if (click != null) {
					Keyboard.keyReleased(click);
				}
			}
		});

		glWindow.addGLEventListener(this /* GLEventListener */);
		final Animator animator = new Animator();
		animator.add(glWindow);
		animator.start();
	}

	private Key convertSimpleKey(KeyEvent event) {
		switch (event.getKeyChar()) {
			case '+': {
				if (event.isControlDown()) {
					return Key.ZOOM_MODE;
				} else {
					return Key.PLUS;
				}
			}
			case '-': return Key.MINUS;
			case '*': return Key.MULTIPLY;
			case '/': return Key.DIVIDE;
			case 'd': case 'D': return Key.debug_DEG;
			case 'r': case 'R': return Key.debug_RAD;
			case 'g': case 'G': return Key.debug_GRA;
			case 'x': case 'X': return Key.LETTER_X;
			case 'y': case 'Y': return Key.LETTER_Y;
			case 'p': case 'P': return Key.PI;
			case 'b': return Key.BRIGHTNESS_CYCLE;
			case 'B': return Key.BRIGHTNESS_CYCLE_REVERSE;
			case '=': return Key.SIMPLIFY;
			case '0': return Key.NUM0;
			case '1': return Key.NUM1;
			case '2': return Key.NUM2;
			case '3': return Key.NUM3;
			case '4': return Key.NUM4;
			case '5': return Key.NUM5;
			case '6': return Key.NUM6;
			case '7': return Key.NUM7;
			case '8': return Key.NUM8;
			case '9': return Key.NUM9;
			case 'm': case 'M': return Key.SURD_MODE;
		}
		switch (event.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				return Key.POWEROFF;
			case KeyEvent.VK_ENTER:
				if (!Keyboard.shift && !Keyboard.alpha)
					return Key.OK;
				else
					return Key.NONE;
			case KeyEvent.VK_F1:
				return Key.debug1;
			case KeyEvent.VK_F2:
				return Key.debug2;
			case KeyEvent.VK_F3:
				return Key.debug3;
			case KeyEvent.VK_F4:
				return Key.debug4;
			case KeyEvent.VK_F5:
				return Key.debug5;
			case 0x15:
			case KeyEvent.VK_SHIFT:
				return Key.SHIFT;
			case KeyEvent.VK_CONTROL:
				return Key.ALPHA;
			case KeyEvent.VK_LEFT:
				if (Keyboard.alpha) {
					return Key.HISTORY_BACK;
				} else {
					return Key.LEFT;
				}
			case KeyEvent.VK_RIGHT:
				if (Keyboard.alpha) {
					return Key.HISTORY_FORWARD;
				} else {
					return Key.RIGHT;
				}
			case KeyEvent.VK_UP:
				return Key.UP;
			case KeyEvent.VK_DOWN:
			case (short) 12:
				return Key.DOWN;
			case KeyEvent.VK_BACK_SPACE:
				return Key.DELETE;
			case KeyEvent.VK_DELETE:
				if (Keyboard.alpha) {
					return Key.RESET;
				} else {
					return null;
				}
			default: return null;
		}
	}

	@Override
	public void init(final GLAutoDrawable drawable) {
		final GL2ES1 gl = drawable.getGL().getGL2ES1();
		onGLContext.submit(gl);

		if (WarpPI.getPlatform().getSettings().isDebugEnabled())
			//Vsync
			gl.setSwapInterval(1);
		else
			//Vsync
			gl.setSwapInterval(2);

		//Textures
		gl.glEnable(GL.GL_TEXTURE_2D);

		//Transparency
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glShadeModel(GLLightingFunc.GL_FLAT);

		//Multisampling
		//gl.glEnable(GL.GL_MULTISAMPLE);

		try {
			renderer.currentTex = ((JOGLSkin) engine.loadSkin("/test.png")).t;
		} catch (final Exception e) {
			e.printStackTrace();
		}

		if (onInitialized != null) {
			onInitialized.run();
			onInitialized = null;
		}

		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("INIT GL IS: " + gl.getClass().getName());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
	}

	@Override
	public void reshape(final GLAutoDrawable glad, final int x, final int y, final int width, final int height) {
		onRealResize.submit(new Integer[] { width, height });
	}

	@Override
	public void display(final GLAutoDrawable glad) {
		final GL2ES1 gl = glad.getGL().getGL2ES1();
		JOGLRenderer.gl = gl;
		onGLContext.submit(gl);

		final boolean linear = windowZoom % (int) windowZoom != 0f;
		if (refreshViewport) {
			refreshViewport = false;
			gl.glViewport(0, 0, realWindowSize[0], realWindowSize[1]);

			gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
			gl.glLoadIdentity();

			gl.glOrtho(0.0, engine.size[0], engine.size[1], 0.0, -1, 1);

			gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
			gl.glLoadIdentity();

			for (final Texture t : engine.registeredTextures) {
				t.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, linear ? GL.GL_LINEAR : GL.GL_NEAREST);
				t.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			}
		}
		while (engine.unregisteredTextures.isEmpty() == false) {
			final Texture t = engine.unregisteredTextures.pop();
			t.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, linear ? GL.GL_LINEAR : GL.GL_NEAREST);
			t.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			engine.registeredTextures.addLast(t);
		}

		gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);

		renderer.initDrawCycle();

		engine.repaint();

		renderer.endDrawCycle();

		gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);

		JOGLRenderer.gl = null;

	}

	void setSize(final int width, final int height) {
		int zoom = (int) windowZoom;
		if (zoom == 0)
			zoom = onZoom.getLastValue().intValue();
		if (zoom == 0)
			zoom = 1;
		window.setSize(width * zoom, height * zoom);
		onRealResize.submit(new Integer[] { width * zoom, height * zoom });
	}

	@Override
	public void dispose(final GLAutoDrawable drawable) {
		System.out.println("cleanup");
//		final GL2ES1 gl = drawable.getGL().getGL2ES1();
		System.exit(0);
	}

}