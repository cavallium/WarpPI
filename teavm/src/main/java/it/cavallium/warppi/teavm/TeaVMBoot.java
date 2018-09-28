package it.cavallium.warppi.teavm;

import it.cavallium.warppi.boot.Boot;

public class TeaVMBoot {

	public static void main(final String[] args) {
		try {
			Boot.boot(new TeaVMPlatform(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
