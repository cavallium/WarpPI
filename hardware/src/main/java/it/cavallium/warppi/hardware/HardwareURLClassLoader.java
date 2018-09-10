package it.cavallium.warppi.hardware;

import java.net.URL;

import it.cavallium.warppi.deps.Platform.URLClassLoader;

public class HardwareURLClassLoader extends java.net.URLClassLoader implements URLClassLoader {

	public HardwareURLClassLoader(URL[] urls) {
		super(urls);
	}

}
