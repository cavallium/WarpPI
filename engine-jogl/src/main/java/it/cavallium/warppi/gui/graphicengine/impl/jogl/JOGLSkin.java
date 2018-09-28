package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.graphicengine.impl.jogl.JOGLRenderer.OpenedTextureData;

public class JOGLSkin implements Skin {

	public Texture t;
	public int w;
	public int h;

	private String texturePath;
	private boolean initialized = false;
	private boolean isResource;

	JOGLSkin(final GraphicEngine d, final String file) throws IOException {
		load(file);
	}

	@Override
	public void load(final String file) throws IOException {
		final boolean isResource = !Files.exists(Paths.get(file));
		if (isResource && Engine.getPlatform().getStorageUtils().getResourceStream(file) == null)
			throw new IOException("File '" + file + "' not found!");
		texturePath = file;
		this.isResource = isResource;
	}

	@Override
	public void initialize(final GraphicEngine d) {
		try {
			final OpenedTextureData i = JOGLRenderer.openTexture(texturePath, isResource);
			t = JOGLRenderer.importTexture(i.f, i.deleteOnExit);
			w = i.w;
			h = i.h;
			((JOGLEngine) d).registerTexture(t);
			initialized = true;
		} catch (GLException | IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void use(final GraphicEngine d) {
		if (!initialized)
			initialize(d);
		final JOGLRenderer r = (JOGLRenderer) d.getRenderer();
		r.useTexture(t, w, h);
	}

	@Override
	public boolean isInitialized() {
		return initialized;
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
