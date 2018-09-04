package it.cavallium.warppi.teavm;

import java.io.IOException;
import java.net.URL;

import it.cavallium.warppi.deps.Platform.URLClassLoader;

public class TeaVMURLClassLoader implements URLClassLoader {

	public TeaVMURLClassLoader(URL[] urls) {
		
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return null;
	}

	@Override
	public void close() throws IOException {
		
	}

}
