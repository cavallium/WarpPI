package it.cavallium.warppi.hardware;

import java.net.URL;

import it.cavallium.warppi.Platform.URLClassLoader;

public class HardwareURLClassLoader extends java.net.URLClassLoader implements URLClassLoader {

	public HardwareURLClassLoader(final URL[] urls) {
		super(urls);
	}

}
