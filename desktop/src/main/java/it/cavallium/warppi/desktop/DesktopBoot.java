package it.cavallium.warppi.desktop;

import it.cavallium.warppi.boot.Boot;

public class DesktopBoot {

	public static void main(String[] args) throws Exception {
		Boot.boot(new DesktopPlatform(), args);
	}

}
