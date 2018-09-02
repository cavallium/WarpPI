package org.warp.picalculator.deps;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class DURLClassLoader extends URLClassLoader {

	public DURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
		// TODO Auto-generated constructor stub
	}

	public DURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		// TODO Auto-generated constructor stub
	}

	public DURLClassLoader(URL[] urls) {
		super(urls);
		// TODO Auto-generated constructor stub
	}
}
