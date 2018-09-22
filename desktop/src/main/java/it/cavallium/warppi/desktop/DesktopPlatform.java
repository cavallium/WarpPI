package it.cavallium.warppi.desktop;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.impl.jansi24bitcolors.JAnsi24bitEngine;
import it.cavallium.warppi.gui.graphicengine.impl.jansi256colors.JAnsi256Engine;
import it.cavallium.warppi.gui.graphicengine.impl.jansi8colors.JAnsi8Engine;
import it.cavallium.warppi.gui.graphicengine.impl.jogl.JOGLEngine;
import it.cavallium.warppi.gui.graphicengine.impl.swing.SwingEngine;
import it.cavallium.warppi.util.CacheUtils;
import it.cavallium.warppi.util.Error;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class DesktopPlatform implements Platform {

	private final DesktopConsoleUtils cu;
	private final DesktopGpio gi;
	private final DesktopStorageUtils su;
	private final PngUtils pu;
	private final String on;
	private final Map<String, GraphicEngine> el;
	private final DesktopSettings settings;

	public DesktopPlatform() {
		cu = new DesktopConsoleUtils();
		gi = new DesktopGpio();
		su = new DesktopStorageUtils();
		pu = new DesktopPngUtils();
		on = System.getProperty("os.name").toLowerCase();
		el = new HashMap<>();
		el.put("CPU engine", new SwingEngine());
		el.put("GPU engine", new JOGLEngine());
		el.put("headless 24 bit engine", new JAnsi24bitEngine());
		el.put("headless 256 colors engine", new JAnsi256Engine());
		el.put("headless 8 colors engine", new JAnsi8Engine());
		settings = new DesktopSettings();
	}

	@Override
	public ConsoleUtils getConsoleUtils() {
		return cu;
	}

	@Override
	public Gpio getGpio() {
		return gi;
	}

	@Override
	public StorageUtils getStorageUtils() {
		return su;
	}

	@Override
	public PngUtils getPngUtils() {
		return pu;
	}

	@Override
	public DesktopSettings getSettings() {
		return settings;
	}

	@Override
	public void setThreadName(final Thread t, final String name) {
		t.setName(name);
	}

	@Override
	public void setThreadDaemon(final Thread t) {
		t.setDaemon(true);
	}

	@Override
	public void setThreadDaemon(final Thread t, final boolean value) {
		t.setDaemon(value);
	}

	@Override
	public void exit(final int value) {
		System.exit(value);
	}

	@Override
	public void gc() {
		System.gc();
	}

	@Override
	public boolean isJavascript() {
		return false;
	}

	@Override
	public String getOsName() {
		return on;
	}

	@Override
	public void alphaChanged(final boolean val) {
		final GraphicEngine currentEngine = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine;
		if (currentEngine instanceof SwingEngine)
			((SwingEngine) currentEngine).setAlphaChanged(val);
	}

	@Override
	public void shiftChanged(final boolean val) {
		final GraphicEngine currentEngine = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine;
		if (currentEngine instanceof SwingEngine)
			((SwingEngine) currentEngine).setShiftChanged(val);
	}

	@Override
	public Semaphore newSemaphore() {
		return new DesktopSemaphore(0);
	}

	@Override
	public Semaphore newSemaphore(final int i) {
		return new DesktopSemaphore(i);
	}

	@Override
	public URLClassLoader newURLClassLoader(final URL[] urls) {
		return new DesktopURLClassLoader(urls);
	}

	@Override
	public Map<String, GraphicEngine> getEnginesList() {
		return el;
	}

	@Override
	public GraphicEngine getEngine(final String string) throws NullPointerException {
		return el.get(string);
	}

	@Override
	public void throwNewExceptionInInitializerError(final String text) {
		throw new ExceptionInInitializerError();
	}

	@Override
	public String[] stacktraceToString(final Error e) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString().toUpperCase().replace("\t", "    ").replace("\r", "").split("\n");
	}

	@Override
	public void loadPlatformRules() {

	}

	@Override
	public void zip(final String targetPath, final String destinationFilePath, final String password) {
		try {
			final ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

			if (password.length() > 0) {
				parameters.setEncryptFiles(true);
				parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
				parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
				parameters.setPassword(password);
			}

			final ZipFile zipFile = new ZipFile(destinationFilePath);

			final File targetFile = new File(targetPath);
			if (targetFile.isFile())
				zipFile.addFile(targetFile, parameters);
			else if (targetFile.isDirectory())
				zipFile.addFolder(targetFile, parameters);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unzip(final String targetZipFilePath, final String destinationFolderPath, final String password) {
		try {
			final ZipFile zipFile = new ZipFile(targetZipFilePath);
			if (zipFile.isEncrypted())
				zipFile.setPassword(password);
			zipFile.extractAll(destinationFolderPath);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean compile(final String[] command, final PrintWriter printWriter, final PrintWriter errors) {
		return org.eclipse.jdt.internal.compiler.batch.Main.compile(command, printWriter, errors, null);
	}

	@Override
	public boolean isRunningOnRaspberry() {
		return CacheUtils.get("isRunningOnRaspberry", 24 * 60 * 60 * 1000, () -> {
			if (Engine.getPlatform().isJavascript())
				return false;
			if (Engine.getPlatform().getOsName().equals("Linux"))
				try {
					final File osRelease = new File("/etc", "os-release");
					return FileUtils.readLines(osRelease, "UTF-8").stream().map(String::toLowerCase).anyMatch(line -> line.contains("raspbian") && line.contains("name"));
				} catch (final IOException readException) {
					return false;
				}
			else
				return false;
		});
	}

}
