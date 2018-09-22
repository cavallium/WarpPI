package it.cavallium.warppi.teavm;

import java.io.IOException;
import java.net.URL;

import it.cavallium.warppi.Platform.URLClassLoader;

public class TeaVMURLClassLoader implements URLClassLoader {

	public TeaVMURLClassLoader(final URL[] urls) {

	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		return null;
	}

	@Override
	public void close() throws IOException {

	}

}
