package it.cavallium.warppi.hardware;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import it.cavallium.warppi.Platform;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.impl.framebuffer.FBEngine;
import it.cavallium.warppi.gui.graphicengine.impl.jogl.JOGLEngine;
import it.cavallium.warppi.util.Error;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class HardwarePlatform implements Platform {

	private final HardwareConsoleUtils cu;
	private final HardwareGpio gi;
	private final HardwareStorageUtils su;
	private final PngUtils pu;
	private final String on;
	private final Map<String, GraphicEngine> el;
	private final HardwareSettings settings;
	private Boolean runningOnRaspberryOverride = null;

	public HardwarePlatform() {
		cu = new HardwareConsoleUtils();
		gi = new HardwareGpio();
		su = new HardwareStorageUtils();
		pu = new HardwarePngUtils();
		on = System.getProperty("os.name").toLowerCase();
		el = new HashMap<>();
		el.put("GPU engine", new JOGLEngine());
		el.put("framebuffer engine", new FBEngine());
		settings = new HardwareSettings();
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
	public HardwareSettings getSettings() {
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

	}

	@Override
	public void shiftChanged(final boolean val) {}

	@Override
	public Semaphore newSemaphore() {
		return new HardwareSemaphore(0);
	}

	@Override
	public Semaphore newSemaphore(final int i) {
		return new HardwareSemaphore(i);
	}

	@Override
	public URLClassLoader newURLClassLoader(final URL[] urls) {
		return new HardwareURLClassLoader(urls);
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
	public void setRunningOnRaspberry(boolean b) {
		runningOnRaspberryOverride = b;
	}
	
	@Override
	public boolean isRunningOnRaspberry() {
		if (runningOnRaspberryOverride != null) return runningOnRaspberryOverride;
		return true;
		/*
		return CacheUtils.get("isRunningOnRaspberry", 24 * 60 * 60 * 1000, () -> {
			if (Engine.getPlatform().isJavascript())
				return false;
			try {
				// Check if it's a raspberry using pi4j
				return Engine.getPlatform().getGpio().getBoardType() != Engine.getPlatform().getGpio().valueUnknownBoardType();
			} catch (final Exception e) {
				// Check if it's a raspberry using other methods
				if (Engine.getPlatform().getOsName().equals("Linux")) {
					try {
						final File osRelease = new File("/etc", "os-release");
						return FileUtils.readLines(osRelease, "UTF-8").stream().map(String::toLowerCase).anyMatch(line -> line.contains("raspbian") && line.contains("name"));
					} catch (IOException readException) {
						return false;
					}
		
				} else {
					return false;
				}
			}
		});
		 */
	}

}
