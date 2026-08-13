package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class NEWTWindowStartupConfigurationTest {
	@Test
	void raspberryUsesOnlyItsNativeHeadlessOpenGLESStack() {
		final Properties properties = new Properties();

		NEWTWindow.configureRaspberrySystemProperties(properties);

		assertEquals("true", properties.getProperty("java.awt.headless"));
		assertEquals("true", properties.getProperty("newt.disable.PointerIcon"));
		assertEquals("true", properties.getProperty("newt.disable.LinuxKeyEventTracker"));
		assertEquals("true", properties.getProperty("newt.disable.LinuxMouseTracker"));
		assertEquals(".bcm.vc.iv", properties.getProperty("nativewindow.ws.name"));
		assertEquals("true", properties.getProperty("jogl.disable.opengldesktop"));
	}

	@Test
	void raspberrySuppressesVerboseOpenGLDiagnosticsUnlessExplicitlyRequested() {
		final Properties properties = new Properties();

		assertFalse(NEWTWindow.openGLDiagnosticsEnabled(true, properties));
		assertTrue(NEWTWindow.openGLDiagnosticsEnabled(false, properties));

		properties.setProperty("warppi.jogl.diagnostics", "true");
		assertTrue(NEWTWindow.openGLDiagnosticsEnabled(true, properties));
	}
}
