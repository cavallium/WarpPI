package it.cavallium.warppi.desktop;

import java.net.URL;

import it.cavallium.warppi.deps.Platform.URLClassLoader;

public class DesktopURLClassLoader extends java.net.URLClassLoader implements URLClassLoader {

	public DesktopURLClassLoader(URL[] urls) {
		super(urls);
	}

}
