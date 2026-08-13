package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class JOGLRenderer implements Renderer {

	public static GL2ES1 gl;

	private static final int ELEMENTS_MAX_COUNT_PER_BUFFER = 128;
	private static final int ELEMENT_VERTICES_COUNT = 6;

	private static final int vertSize = 3;

	private static final int texSize = 2;

	private static final int colSize = 4;

	private static final int vertBuffer = 0;

	private static final int texBuffer = 1;

	private static final int colBuffer = 2;

	private static final int vertMax = JOGLRenderer.vertSize * JOGLRenderer.ELEMENT_VERTICES_COUNT * JOGLRenderer.ELEMENTS_MAX_COUNT_PER_BUFFER;

	private static final int texMax = JOGLRenderer.texSize * JOGLRenderer.ELEMENT_VERTICES_COUNT * JOGLRenderer.ELEMENTS_MAX_COUNT_PER_BUFFER;

	private static final int colMax = JOGLRenderer.colSize * JOGLRenderer.ELEMENT_VERTICES_COUNT * JOGLRenderer.ELEMENTS_MAX_COUNT_PER_BUFFER;

	private int[] handlers;
	FloatBuffer fbVertices;
	FloatBuffer fbTextures;
	FloatBuffer fbColors;
	int fbElements;

	final float[] currentColor = new float[JOGLRenderer.colSize * JOGLRenderer.ELEMENT_VERTICES_COUNT];
	final float[] currentClearColorARGBf = { 1f, 197f / 255f, 194f / 255f, 175f / 255f };
	boolean currentTexEnabled;
	Texture currentTex;
	float currentTexWidth;
	float currentTexHeight;

	JOGLFont currentFont;

	@Override
	public void glColor3i(final int r, final int gg, final int b) {
		setColor(r / 255f, gg / 255f, b / 255f, 1.0f);
	}

	@Override
	public void glColor3f(final float red, final float gre, final float blu) {
		setColor(red, gre, blu, 1.0f);
	}

	@Override
	public void glColor4f(final float red, final float gre, final float blu, final float alp) {
		setColor(red, gre, blu, alp);
	}

	@Override
	public void glColor(final int rgb) {
		final int alpha = rgb >> 24 & 0xFF;
		final int red = rgb >> 16 & 0xFF;
		final int green = rgb >> 8 & 0xFF;
		final int blue = rgb & 0xFF;
		glColor4i(red, green, blue, alpha);
	}

	@Override
	public int glGetClearColor() {
		return (int) (currentClearColorARGBf[0] * 255) << 24 | (int) (currentClearColorARGBf[1] * 255) << 16 | (int) (currentClearColorARGBf[2] * 255) << 8 | (int) (currentClearColorARGBf[3] * 255);
	}

	@Override
	public void glClearColor(final int rgb) {
		final float alpha = (rgb >> 24 & 0xFF) / 255f;
		final float red = (rgb >> 16 & 0xFF) / 255f;
		final float green = (rgb >> 8 & 0xFF) / 255f;
		final float blue = (rgb & 0xFF) / 255f;
		glClearColor4f(red, green, blue, alpha);
	}

	@Override
	public void glColor4i(final int r, final int g, final int b, final int a) {
		setColor(r / 255f, g / 255f, b / 255f, a / 255f);
	}

	private void setColor(final float red, final float green, final float blue, final float alpha) {
		for (int vertex = 0; vertex < JOGLRenderer.ELEMENT_VERTICES_COUNT; vertex++) {
			final int offset = vertex * JOGLRenderer.colSize;
			currentColor[offset] = red;
			currentColor[offset + 1] = green;
			currentColor[offset + 2] = blue;
			currentColor[offset + 3] = alpha;
		}
	}

	@Override
	public void glClearColor4i(final int red, final int green, final int blue, final int alpha) {
		final float ros = red / 255f;
		final float gre = green / 255f;
		final float blu = blue / 255f;
		final float alp = alpha / 255f;
		setClearColor(alp, ros, gre, blu);
	}

	@Override
	public void glClearColor4f(final float red, final float green, final float blue, final float alpha) {
		setClearColor(alpha, red, green, blue);
	}

	private void setClearColor(final float alpha, final float red, final float green, final float blue) {
		currentClearColorARGBf[0] = alpha;
		currentClearColorARGBf[1] = red;
		currentClearColorARGBf[2] = green;
		currentClearColorARGBf[3] = blue;
	}

	@Override
	public void glClear(final int screenWidth, final int screenHeight) {
		glColor(glGetClearColor());
		glFillColor(0, 0, screenWidth, screenHeight);
	}

	@Override
	public void glDrawLine(final float x0, final float y0, final float x1, final float y1) {
		glFillColor(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
	}

	@Override
	public void glFillRect(final float x, final float y, final float width, final float height, float uvX, float uvY,
			float uvWidth, float uvHeight) {
		enableTexture();
		if (uvWidth < 0)
			uvX -= uvWidth;
		if (uvHeight < 0)
			uvY -= uvHeight;
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

		putQuad(x, y, x + width, y + height, uvX, uvY, uvX + uvWidth, uvY + uvHeight);
	}

	@Override
	public void glFillColor(final float x0, final float y0, final float w1, final float h1) {
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

		putQuad(x0, y0, x0 + w1, y0 + h1, 0.0f, 0.0f, 1.0f, 1.0f);
	}

	private void putQuad(final float x0, final float y0, final float x1, final float y1,
			final float u0, final float v0, final float u1, final float v1) {
		fbElements++;
		fbVertices
				.put(x0).put(y1).put(0.0f)
				.put(x1).put(y1).put(0.0f)
				.put(x0).put(y0).put(0.0f)
				.put(x0).put(y0).put(0.0f)
				.put(x1).put(y1).put(0.0f)
				.put(x1).put(y0).put(0.0f);
		fbTextures
				.put(u0).put(v0)
				.put(u1).put(v0)
				.put(u0).put(v1)
				.put(u0).put(v1)
				.put(u1).put(v0)
				.put(u1).put(v1);
		fbColors.put(currentColor);
	}

	@Override
	public void glDrawStringLeft(final float x, final float y, final String text) {
		final int txtLen = text.length();
		for (int currentCharIndex = 0; currentCharIndex < txtLen; currentCharIndex++)
			drawChar(x + (float) currentCharIndex * currentFont.charW, y, text.charAt(currentCharIndex));
	}

	@Override
	public void glDrawStringCenter(final float x, final float y, final String text) {
		glDrawStringLeft(x - currentFont.getStringWidth(text) / 2, y, text);
	}

	@Override
	public void glDrawStringRight(final float x, final float y, final String text) {
		glDrawStringLeft(x - currentFont.getStringWidth(text), y, text);
	}

	@Override
	public void glDrawCharLeft(final int x, final int y, final char ch) {
		drawChar(x, y, ch);
	}

	private void drawChar(final float x, final float y, final char ch) {
		final int index = currentFont.getCharIndex(ch);
		final int tableIndexX = index % currentFont.memoryWidthOfEachColumn;
		final int tableIndexY = (index - tableIndexX) / currentFont.memoryWidthOfEachColumn;
		glFillRect(x, y, currentFont.charW, currentFont.charH, tableIndexX * currentFont.charW, tableIndexY * currentFont.charH, currentFont.charW, currentFont.charH);
	}

	@Override
	public void glDrawCharCenter(final int x, final int y, final char ch) {
		glDrawCharLeft(x - currentFont.charW / 2, y, ch);
	}

	@Override
	public void glDrawCharRight(final int x, final int y, final char ch) {
		glDrawCharLeft(x - currentFont.charW, y, ch);
	}

	@Override
	public BinaryFont getCurrentFont() {
		return currentFont;
	}

	@Deprecated
	static Texture importTexture(final GL gl, final String string) throws IOException {
		final FileInputStream f = new FileInputStream("test.png");
		final TextureData tx_dat = TextureIO.newTextureData(gl.getGLProfile(), f, false, TextureIO.PNG);
		final Texture tex = new Texture(gl, tx_dat);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
//		tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
//		tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		return tex;
	}

	static OpenedTextureData openTexture(final String file, final boolean isResource) throws GLException, IOException {
		final URL imageUrl;
		if (isResource) {
			String resourcePath = file.replace('\\', '/');
			if (!resourcePath.startsWith("/"))
				resourcePath = "/" + resourcePath;
			imageUrl = JOGLRenderer.class.getResource(resourcePath);
			if (imageUrl == null)
				throw new IOException("Texture resource '" + file + "' not found");
		} else {
			imageUrl = new File(file).toURI().toURL();
		}
		final BufferedImage img = ImageIO.read(imageUrl);
		if (img == null)
			throw new IOException("Unsupported or invalid texture image '" + file + "'");
		File f;
		if (isResource) {
			f = Files.createTempFile("texture-", ".png").toFile();
			f.deleteOnExit();
			ImageIO.write(img, "png", f);
		} else
			f = new File(file);
		final int imgW = img.getWidth();
		final int imgH = img.getHeight();
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
		public OpenedTextureData(final int w, final int h, final File f, final boolean deleteOnExit) {
			this.w = w;
			this.h = h;
			this.f = f;
			this.deleteOnExit = deleteOnExit;
		}

	}

	static Texture importTexture(File f, final boolean deleteOnExit) throws GLException, IOException {
		final Texture tex = TextureIO.newTexture(f, false);
		if (deleteOnExit && f.exists())
			try {
				if (WarpPI.getPlatform().getSettings().isDebugEnabled())
					throw new IOException("Delete on exit!");
				f.delete();
			} catch (final Exception ex) {
				f.deleteOnExit();
			}
		tex.setTexParameteri(JOGLRenderer.gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		tex.setTexParameteri(JOGLRenderer.gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		f = null;
		return tex;
	}

	public void initDrawCycle() {
		final boolean textureChange = precTexEnabled != currentTexEnabled || precTex != currentTex;
		if (fbVertices == null) {
			fbVertices = Buffers.newDirectFloatBuffer(JOGLRenderer.vertMax);
			fbTextures = Buffers.newDirectFloatBuffer(JOGLRenderer.texMax);
			fbColors = Buffers.newDirectFloatBuffer(JOGLRenderer.colMax);
			handlers = new int[3];
			JOGLRenderer.gl.glGenBuffers(3, handlers, 0);
		}
		startDrawSegment(false);
		if (textureChange)
			changeTexture();
	}

	public void endDrawCycle() {
		final boolean textureChange = precTexEnabled != currentTexEnabled || precTex != currentTex;
		if (textureChange) {
			if (fbElements > 0)
				endDrawSegment();
			changeTexture();
		} else if (fbElements > 0)
			endDrawSegment();
	}

	private void changeTexture() {
		precTexEnabled = currentTexEnabled;
		precTex = currentTex;
		if (currentTexEnabled) {
			JOGLRenderer.gl.glEnable(GL.GL_TEXTURE_2D);
			currentTex.bind(JOGLRenderer.gl);
		} else
			JOGLRenderer.gl.glDisable(GL.GL_TEXTURE_2D);
		firstBufferTexDataCall = true;
	}

	public void startDrawSegment(final boolean continuation) {
		if (!continuation || cycleEnded)
			fbElements = 0;
		cycleEnded = false;
	}

	private boolean precTexEnabled;
	private Texture precTex;
	private boolean cycleEnded = true;

	public void doDrawSegment() {
		final boolean textureChange = precTexEnabled != currentTexEnabled || precTex != currentTex;
		final boolean changeRequired = fbElements >= JOGLRenderer.ELEMENTS_MAX_COUNT_PER_BUFFER;
		if (textureChange) {
			if (fbElements > 0) {
				endDrawSegment();
				startDrawSegment(false);
			}
			changeTexture();
		} else if (fbElements > 0 && changeRequired) {
			endDrawSegment();
			startDrawSegment(true);
		}
	}

	boolean firstBufferDataCall = true;
	boolean firstBufferTexDataCall = true;

	public void endDrawSegment() {
		fbVertices.flip();
		fbTextures.flip();
		fbColors.flip();

//		gl.glVertexPointer(vertSize, GL.GL_FLOAT, 0, fbVertices);
		JOGLRenderer.gl.glBindBuffer(GL.GL_ARRAY_BUFFER, handlers[JOGLRenderer.vertBuffer]);
		if (firstBufferTexDataCall)
			JOGLRenderer.gl.glBufferData(GL.GL_ARRAY_BUFFER, fbVertices.limit() * Buffers.SIZEOF_FLOAT, fbVertices, GL.GL_STATIC_DRAW);
		else
			JOGLRenderer.gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, fbVertices.limit() * Buffers.SIZEOF_FLOAT, fbVertices);
		JOGLRenderer.gl.glVertexPointer(JOGLRenderer.vertSize, GL.GL_FLOAT, 0, 0l);
//		gl.glTexCoordPointer(texSize, GL.GL_FLOAT, 0, fbTextures);
		JOGLRenderer.gl.glBindBuffer(GL.GL_ARRAY_BUFFER, handlers[JOGLRenderer.texBuffer]);
		if (firstBufferTexDataCall)
			JOGLRenderer.gl.glBufferData(GL.GL_ARRAY_BUFFER, fbTextures.limit() * Buffers.SIZEOF_FLOAT, fbTextures, GL.GL_STATIC_DRAW);
		else
			JOGLRenderer.gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, fbTextures.limit() * Buffers.SIZEOF_FLOAT, fbTextures);
		JOGLRenderer.gl.glTexCoordPointer(JOGLRenderer.texSize, GL.GL_FLOAT, 0, 0l);
//		gl.glColorPointer(colSize, GL.GL_FLOAT, 0, fbColors);
		JOGLRenderer.gl.glBindBuffer(GL.GL_ARRAY_BUFFER, handlers[JOGLRenderer.colBuffer]);
		if (firstBufferTexDataCall)
			JOGLRenderer.gl.glBufferData(GL.GL_ARRAY_BUFFER, fbColors.limit() * Buffers.SIZEOF_FLOAT, fbColors, GL.GL_STATIC_DRAW);
		else
			JOGLRenderer.gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, fbColors.limit() * Buffers.SIZEOF_FLOAT, fbColors);
		JOGLRenderer.gl.glColorPointer(JOGLRenderer.colSize, GL.GL_FLOAT, 0, 0l);

		fbVertices.limit(JOGLRenderer.vertMax);
		fbTextures.limit(JOGLRenderer.texMax);
		fbColors.limit(JOGLRenderer.colMax);
		JOGLRenderer.gl.glDrawArrays(GL.GL_TRIANGLES, 0, fbElements * JOGLRenderer.ELEMENT_VERTICES_COUNT);
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

	@Override
	public void glClearSkin() {
		if (currentTex != null || currentTexEnabled) {
			currentTex = null;
			currentTexEnabled = false;
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

	void useTexture(final Texture t, final float w, final float h) {
		currentTex = t;
		currentTexWidth = w;
		currentTexHeight = h;
		enableTexture();
	}
}
