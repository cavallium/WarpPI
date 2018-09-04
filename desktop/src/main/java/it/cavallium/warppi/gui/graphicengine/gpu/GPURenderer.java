package it.cavallium.warppi.gui.graphicengine.gpu;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class GPURenderer implements Renderer {

	public static GL2ES1 gl;

	private static final int ELEMENTS_MAX_COUNT_PER_BUFFER = StaticVars.enableVBO ? 128 : 1;
	private static final int ELEMENT_VERTICES_COUNT = 6;

	private static final int vertSize = 3;

	private static final int texSize = 2;

	private static final int colSize = 4;

	private static final int vertBuffer = 0;

	private static final int texBuffer = 1;

	private static final int colBuffer = 2;

	private static final int vertMax = vertSize * ELEMENT_VERTICES_COUNT * ELEMENTS_MAX_COUNT_PER_BUFFER;

	private static final int texMax = texSize * ELEMENT_VERTICES_COUNT * ELEMENTS_MAX_COUNT_PER_BUFFER;

	private static final int colMax = colSize * ELEMENT_VERTICES_COUNT * ELEMENTS_MAX_COUNT_PER_BUFFER;

	private int[] handlers;
	private final DeallocationHelper deallocationHelper = new DeallocationHelper();
	FloatBuffer fbVertices;
	FloatBuffer fbTextures;
	FloatBuffer fbColors;
	int fbElements;

	float[] currentColor = new float[24];
	float[] currentClearColorARGBf = new float[] { 1f, 197f / 255f, 194f / 255f, 175f / 255f };
	boolean currentTexEnabled;
	Texture currentTex;
	float currentTexWidth;
	float currentTexHeight;

	GPUFont currentFont;

	@Override
	public void glColor3i(int r, int gg, int b) {
		final float red = (r) / 255f;
		final float gre = (gg) / 255f;
		final float blu = (b) / 255f;
		//currentColor = new float[] { red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, };
		currentColor = new float[] { red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, }; //OK
	}

	@Override
	public void glColor3f(float red, float gre, float blu) {
		// currentColor = new float[] { red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, };
		currentColor = new float[] { red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, red, gre, blu, 1.0f, };//OK
	}

	@Override
	public void glColor4f(float red, float gre, float blu, float alp) {
		// currentColor = new float[] { red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, };
		currentColor = new float[] { red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, };//ok

	}

	@Override
	public void glColor(int rgb) {
		final int alpha = (rgb >> 24) & 0xFF;
		final int red = (rgb >> 16) & 0xFF;
		final int green = (rgb >> 8) & 0xFF;
		final int blue = rgb & 0xFF;
		glColor4i(red, green, blue, alpha);
	}

	@Override
	public int glGetClearColor() {
		return (int) (currentClearColorARGBf[0] * 255) << 24 | (int) (currentClearColorARGBf[1] * 255) << 16 | (int) (currentClearColorARGBf[2] * 255) << 8 | (int) (currentClearColorARGBf[3] * 255);
	}

	@Override
	public void glClearColor(int rgb) {
		final float alpha = ((rgb >> 24) & 0xFF) / 255f;
		final float red = ((rgb >> 16) & 0xFF) / 255f;
		final float green = ((rgb >> 8) & 0xFF) / 255f;
		final float blue = (rgb & 0xFF) / 255f;
		glClearColor4f(red, green, blue, alpha);
	}

	@Override
	public void glColor4i(int r, int g, int b, int a) {
		final float red = (r) / 255f;
		final float gre = (g) / 255f;
		final float blu = (b) / 255f;
		final float alp = (a) / 255f;
		//currentColor = new float[] { red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, };
		currentColor = new float[] { red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, red, gre, blu, alp, };//OK
	}

	@Override
	public void glClearColor4i(int red, int green, int blue, int alpha) {
		final float ros = (red) / 255f;
		final float gre = (green) / 255f;
		final float blu = (blue) / 255f;
		final float alp = (alpha) / 255f;
		currentClearColorARGBf = new float[] { alp, ros, gre, blu };
	}

	@Override
	public void glClearColor4f(float red, float green, float blue, float alpha) {
		currentClearColorARGBf = new float[] { alpha, red, green, blue };
	}

	@Override
	public void glClear(int screenWidth, int screenHeight) {
		glColor(glGetClearColor());
		glFillColor(0, 0, screenWidth, screenHeight);
	}

	@Override
	public void glDrawLine(float x0, float y0, float x1, float y1) {
		glFillColor(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
	}

	@Override
	public void glFillRect(float x, float y, float width, float height, float uvX, float uvY, float uvWidth,
			float uvHeight) {
		enableTexture();
		if (uvWidth < 0) {
			uvX -= uvWidth;
		}
		if (uvHeight < 0) {
			uvY -= uvHeight;
		}
		uvWidth /= currentTexWidth;
		uvX /= currentTexWidth;
		uvHeight /= currentTexHeight;
		uvY = 1 - uvY / currentTexHeight - uvHeight;
//		final float[] vertices = { x, y, 0.0f, x, y + height, 0.0f, x + width, y, 0.0f, x + width, y + height, 0.0f, };
//		final float[] tex_vertices = { uvX, uvY + uvHeight, uvX, uvY, uvX + uvWidth, uvY + uvHeight, uvX + uvWidth, uvY, };
		//V0	x, y, 0.0f
		//V1	x, y + height, 0.0f
		//V2	x + width, y, 0.0f
		//V3	x + width, y + height, 0.0f

		//NV0 = V1
		//NV1 = V3
		//NV2 = V0

		//NV3 = V0
		//NV4 = V3
		//NV5 = V2

		final float[] vertices = { x, y + height, 0.0f, x + width, y + height, 0.0f, x, y, 0.0f, x, y, 0.0f, x + width, y + height, 0.0f, x + width, y, 0.0f };
		final float[] tex_vertices = { uvX, uvY, uvX + uvWidth, uvY, uvX, uvY + uvHeight, uvX, uvY + uvHeight, uvX + uvWidth, uvY, uvX + uvWidth, uvY + uvHeight };
		fbElements++;
		fbVertices.put(vertices);
		fbTextures.put(tex_vertices);
		fbColors.put(currentColor);
	}

	@Override
	public void glFillColor(float x0, float y0, float w1, float h1) {
		disableTexture();
//		final float[] vertices = { x0, y0, 0.0f, x0, y0 + h1, 0.0f, x0 + w1, y0, 0.0f, x0 + w1, y0 + h1, 0.0f, };
//		final float[] tex_vertices = { 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, };
		//V0	x0, y0, 0.0f
		//V1	x0, y0 + h1, 0.0f
		//V2	x0 + w1, y0, 0.0f
		//V3	x0 + w1, y0 + h1, 0.0f

		//NV0 = V1
		//NV1 = V3
		//NV2 = V0

		//NV3 = V0
		//NV4 = V3
		//NV5 = V2

		final float[] vertices = { x0, y0 + h1, 0.0f, x0 + w1, y0 + h1, 0.0f, x0, y0, 0.0f, x0, y0, 0.0f, x0 + w1, y0 + h1, 0.0f, x0 + w1, y0, 0.0f, };
		final float[] tex_vertices = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, };
		fbElements++;
		fbVertices.put(vertices);
		fbTextures.put(tex_vertices);
		fbColors.put(currentColor);
	}

	@Override
	public void glDrawStringLeft(float x, float y, String text) {
		final int txtLen = text.length();
		final int[] txtArray = currentFont.getCharIndexes(text);
		int tableIndexX;
		int tableIndexY;
		for (int currentCharIndex = 0; currentCharIndex < txtLen; currentCharIndex++) {
			tableIndexX = txtArray[currentCharIndex] % currentFont.memoryWidthOfEachColumn;
			tableIndexY = (txtArray[currentCharIndex] - tableIndexX) / currentFont.memoryWidthOfEachColumn;
			glFillRect(x + ((float) currentCharIndex) * ((float) (currentFont.charW)), y, currentFont.charW, currentFont.charH, tableIndexX * currentFont.charW, tableIndexY * currentFont.charH, currentFont.charW, currentFont.charH);
		}
	}

	@Override
	public void glDrawStringCenter(float x, float y, String text) {
		glDrawStringLeft(x - (currentFont.getStringWidth(text) / 2), y, text);
	}

	@Override
	public void glDrawStringRight(float x, float y, String text) {
		glDrawStringLeft(x - currentFont.getStringWidth(text), y, text);
	}

	@Override
	public void glDrawCharLeft(int x, int y, char ch) {
		final int index = currentFont.getCharIndex(ch);
		final int tableIndexX = index % currentFont.memoryWidthOfEachColumn;
		final int tableIndexY = (index - tableIndexX) / currentFont.memoryWidthOfEachColumn;
		glFillRect(x, y, currentFont.charW, currentFont.charH, tableIndexX * currentFont.charW, tableIndexY * currentFont.charH, currentFont.charW, currentFont.charH);
	}

	@Override
	public void glDrawCharCenter(int x, int y, char ch) {
		glDrawCharLeft(x - (currentFont.charW / 2), y, ch);
	}

	@Override
	public void glDrawCharRight(int x, int y, char ch) {
		glDrawCharLeft(x - currentFont.charW, y, ch);
	}

	@Override
	public BinaryFont getCurrentFont() {
		return currentFont;
	}

	@Deprecated
	static Texture importTexture(GL gl, String string) throws IOException {
		final FileInputStream f = new FileInputStream("test.png");
		final TextureData tx_dat = TextureIO.newTextureData(gl.getGLProfile(), f, false, TextureIO.PNG);
		final Texture tex = new Texture(gl, tx_dat);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
//		tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
//		tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		return tex;
	}

	static OpenedTextureData openTexture(String file, boolean isResource) throws GLException, IOException {
		BufferedImage img = ImageIO.read(isResource ? GPURenderer.class.getResource("/" + file) : new File(file).toURI().toURL());
		File f;
		if (isResource) {
			f = Files.createTempFile("texture-", ".png").toFile();
			f.deleteOnExit();
			ImageIO.write(img, "png", f);
		} else {
			f = new File(file);
		}
		final int imgW = img.getWidth();
		final int imgH = img.getHeight();
		img = null;
		Engine.getPlatform().gc();
		return new OpenedTextureData(imgW, imgH, f, isResource);
	}

	public static class OpenedTextureData {
		public final int w;
		public final int h;
		public final File f;
		public final boolean deleteOnExit;

		/**
		 * @param w
		 * @param h
		 * @param f
		 * @param deleteOnExit
		 */
		public OpenedTextureData(int w, int h, File f, boolean deleteOnExit) {
			this.w = w;
			this.h = h;
			this.f = f;
			this.deleteOnExit = deleteOnExit;
		}

	}

	static Texture importTexture(File f, boolean deleteOnExit) throws GLException, IOException {
		final Texture tex = TextureIO.newTexture(f, false);
		if (deleteOnExit && f.exists()) {
			try {
				if (StaticVars.debugOn) {
					throw new IOException("Delete on exit!");
				}
				f.delete();
			} catch (final Exception ex) {
				f.deleteOnExit();
			}
		}
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		f = null;
		return tex;
	}

	public void initDrawCycle() {
		final boolean textureChange = precTexEnabled != currentTexEnabled || precTex != currentTex;
		if (fbVertices == null) {
			fbVertices = Buffers.newDirectFloatBuffer(vertMax);
			fbTextures = Buffers.newDirectFloatBuffer(texMax);
			fbColors = Buffers.newDirectFloatBuffer(colMax);
			handlers = new int[3];
			gl.glGenBuffers(3, handlers, 0);
		}
		startDrawSegment(false);
		if (textureChange) {
			changeTexture();
		}
	}

	public void endDrawCycle() {
		final boolean textureChange = precTexEnabled != currentTexEnabled || precTex != currentTex;
		if (textureChange) {
			if (fbElements > 0) {
				endDrawSegment();
			}
			changeTexture();
		} else {
			if (fbElements > 0) {
				endDrawSegment();
			}
		}
	}

	private void changeTexture() {
		precTexEnabled = currentTexEnabled;
		precTex = currentTex;
		if (currentTexEnabled) {
			gl.glEnable(GL.GL_TEXTURE_2D);
			currentTex.bind(gl);
		} else {
			gl.glDisable(GL.GL_TEXTURE_2D);
		}
		firstBufferTexDataCall = true;
	}

	public void startDrawSegment(boolean continuation) {
		if (!continuation || cycleEnded) {
			fbElements = 0;
		}
		cycleEnded = false;
	}

	private boolean precTexEnabled;
	private Texture precTex;
	private boolean cycleEnded = true;

	public void doDrawSegment() {
		final boolean textureChange = precTexEnabled != currentTexEnabled || precTex != currentTex;
		final boolean changeRequired = fbElements >= ELEMENTS_MAX_COUNT_PER_BUFFER;
		if (textureChange) {
			if (fbElements > 0) {
				endDrawSegment();
				startDrawSegment(false);
			}
			changeTexture();
		} else {
			if (fbElements > 0 && changeRequired) {
				endDrawSegment();
				startDrawSegment(true);
			}
		}
	}

	boolean firstBufferDataCall = true;
	boolean firstBufferTexDataCall = true;

	public void endDrawSegment() {
		fbVertices.flip();
		fbTextures.flip();
		fbColors.flip();

//		gl.glVertexPointer(vertSize, GL.GL_FLOAT, 0, fbVertices);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, handlers[vertBuffer]);
		if (firstBufferTexDataCall) {
			gl.glBufferData(GL.GL_ARRAY_BUFFER, fbVertices.limit() * Buffers.SIZEOF_FLOAT, fbVertices, GL.GL_STATIC_DRAW);
		} else {
			gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, fbVertices.limit() * Buffers.SIZEOF_FLOAT, fbVertices);
		}
		gl.glVertexPointer(vertSize, GL.GL_FLOAT, 0, 0l);
//		gl.glTexCoordPointer(texSize, GL.GL_FLOAT, 0, fbTextures);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, handlers[texBuffer]);
		if (firstBufferTexDataCall) {
			gl.glBufferData(GL.GL_ARRAY_BUFFER, fbTextures.limit() * Buffers.SIZEOF_FLOAT, fbTextures, GL.GL_STATIC_DRAW);
		} else {
			gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, fbTextures.limit() * Buffers.SIZEOF_FLOAT, fbTextures);
		}
		gl.glTexCoordPointer(texSize, GL.GL_FLOAT, 0, 0l);
//		gl.glColorPointer(colSize, GL.GL_FLOAT, 0, fbColors);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, handlers[colBuffer]);
		if (firstBufferTexDataCall) {
			gl.glBufferData(GL.GL_ARRAY_BUFFER, fbColors.limit() * Buffers.SIZEOF_FLOAT, fbColors, GL.GL_STATIC_DRAW);
		} else {
			gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, fbColors.limit() * Buffers.SIZEOF_FLOAT, fbColors);
		}
		gl.glColorPointer(colSize, GL.GL_FLOAT, 0, 0l);

		fbVertices.limit(vertMax);
		fbTextures.limit(texMax);
		fbColors.limit(colMax);
		gl.glDrawArrays(GL.GL_TRIANGLES, 0, fbElements * ELEMENT_VERTICES_COUNT);
		//gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, fbElements * ELEMENT_VERTICES_COUNT);
		firstBufferDataCall = false;
		firstBufferTexDataCall = false;
		cycleEnded = true;

//		deleteBuffer(fbVertices);
//		deleteBuffer(txVertices);
//		deleteBuffer(colVertices);
//		fbVertices = null;
//		txVertices = null;
//		colVertices = null;
	}

	public void deleteBuffer(final Buffer realNioBuffer) {
		if (deallocationHelper != null) {
			deallocationHelper.deallocate(realNioBuffer);
		}
	}

	@Override
	public void glClearSkin() {
		if (currentTex != null) {
			currentTex = null;
			doDrawSegment();
		}
	}

	void disableTexture() {
		currentTexEnabled = false;
		doDrawSegment();
	}

	void enableTexture() {
		currentTexEnabled = true;
		doDrawSegment();
	}

	void useTexture(Texture t, float w, float h) {
		currentTex = t;
		currentTexWidth = w;
		currentTexHeight = h;
		enableTexture();
	}
}