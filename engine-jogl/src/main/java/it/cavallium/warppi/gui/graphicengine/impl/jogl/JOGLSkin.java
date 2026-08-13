package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.graphicengine.impl.jogl.JOGLRenderer.OpenedTextureData;

public class JOGLSkin implements Skin {

	public Texture t;
	public int w;
	public int h;

	private String texturePath;
	private String packedTexturePath;
	private boolean initialized = false;
	private boolean isResource;
	private boolean packedTextureAvailable;
	private boolean usingPackedTexture;
	private boolean usingPalettedTexture;

	JOGLSkin(final String file) throws IOException {
		load(file);
	}

	@Override
	public void load(final String file) throws IOException {
		final boolean isResource = !Files.exists(Paths.get(file));
		final String packedPath = isResource ? JOGLPackedTexture.resourcePath(file) : null;
		final boolean sourceAvailable = !isResource
				|| WarpPI.getPlatform().getStorageUtils().doesResourceExist(file);
		final boolean packedAvailable = packedPath != null
				&& JOGLSkin.class.getResource(packedPath) != null;
		if (!sourceAvailable && !packedAvailable)
			throw new IOException("File '" + file + "' not found!");
		texturePath = file;
		packedTexturePath = packedPath;
		this.isResource = isResource;
		packedTextureAvailable = packedAvailable;
	}

	@Override
	public void initialize(final DisplayOutputDevice d) {
		if (initialized)
			return;
		try {
			final JOGLEngine engine = (JOGLEngine) d.getGraphicEngine();
			usingPackedTexture = shouldUsePackedTexture(engine.isRaspberryProductionMode(),
					packedTextureAvailable);
			if (usingPackedTexture)
				initializePackedTexture();
			else
				initializePngTexture();
			engine.registerTexture(t);
			initialized = true;
		} catch (final GLException | IOException e) {
			throw new GLException("Failed to initialize texture '" + texturePath + "'", e);
		}
	}

	private void initializePackedTexture() throws IOException {
		final GL gl = JOGLRenderer.gl;
		final boolean usePalette = JOGLPackedTexture.supportsNativePalettedUpload()
				&& gl.isExtensionAvailable(JOGLPackedTexture.PALETTED_TEXTURE_EXTENSION);
		try (InputStream input = JOGLSkin.class.getResourceAsStream(packedTexturePath)) {
			if (input == null)
				throw new IOException("Missing preprocessed texture '" + packedTexturePath + "'");
			final JOGLPackedTexture.Decoded decoded = JOGLPackedTexture.read(input, usePalette);
			final Texture texture = decoded.upload(gl);
			try {
				texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
				texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
			} catch (final RuntimeException exception) {
				try {
					texture.destroy(gl);
				} catch (final RuntimeException cleanupException) {
					exception.addSuppressed(cleanupException);
				}
				throw exception;
			}
			t = texture;
			w = decoded.width();
			h = decoded.height();
			usingPalettedTexture = decoded.compressed();
		}
	}

	private void initializePngTexture() throws IOException {
		final OpenedTextureData opened = JOGLRenderer.openTexture(texturePath, isResource);
		t = JOGLRenderer.importTexture(opened.f, opened.deleteOnExit);
		w = opened.w;
		h = opened.h;
	}

	@Override
	public void use(final DisplayOutputDevice d) {
		if (!initialized)
			initialize(d);
		final JOGLRenderer r = (JOGLRenderer) d.getGraphicEngine().getRenderer();
		r.useTexture(t, w, h);
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void unload(final DisplayOutputDevice d) {
		if (!initialized)
			return;
		final Texture texture = t;
		t = null;
		w = 0;
		h = 0;
		initialized = false;
		usingPackedTexture = false;
		usingPalettedTexture = false;
		((JOGLEngine) d.getGraphicEngine()).releaseTexture(texture);
	}

	boolean isUsingPackedTexture() {
		return usingPackedTexture;
	}

	boolean isUsingPalettedTexture() {
		return usingPalettedTexture;
	}

	static boolean shouldUsePackedTexture(final boolean raspberryProductionMode,
			final boolean packedTextureAvailable) {
		return raspberryProductionMode && packedTextureAvailable;
	}

	@Override
	public int getSkinWidth() {
		return w;
	}

	@Override
	public int getSkinHeight() {
		return h;
	}

}
