package it.cavallium.warppi.hardware;

import it.cavallium.warppi.boot.Boot;

public class HardwareBoot {

	public static void main(String[] args) throws Exception {
		Boot.boot(new HardwarePlatform(), args);
	}

}
