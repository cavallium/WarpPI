package it.cavallium.warppi.desktop;

import java.net.URL;

import it.cavallium.warppi.Platform.URLClassLoader;

public class DesktopURLClassLoader extends java.net.URLClassLoader implements URLClassLoader {

	public DesktopURLClassLoader(final URL[] urls) {
		super(urls);
	}

}
