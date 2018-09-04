package it.cavallium.warppi;

public class TestDrivers {
	public static void main(String[] args) {
		System.out.println("Test started.");
		String className;
		className = "jogamp.newt.driver.bcm.vc.iv.DisplayDriver";
		if (exists(className)) {
			System.out.println("[FOUND]     " + className);
		} else {
			System.out.println("[NOT FOUND] " + className);
		}
		className = ".bcm.vc.iv.DisplayDriver";
		if (exists(className)) {
			System.out.println("[FOUND]     " + className);
		} else {
			System.out.println("[NOT FOUND] " + className);
		}
		System.out.println("Test finished.");
	}

	public static boolean exists(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}
}
