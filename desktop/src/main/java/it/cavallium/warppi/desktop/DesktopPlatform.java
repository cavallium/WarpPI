package it.cavallium.warppi.desktop;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.deps.Platform;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.cpu.CPUEngine;
import it.cavallium.warppi.gui.graphicengine.gpu.GPUEngine;
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

	public DesktopPlatform() {
		cu = new DesktopConsoleUtils();
		gi = new DesktopGpio();
		su = new DesktopStorageUtils();
		pu = new DesktopPngUtils();
		on = System.getProperty("os.name").toLowerCase();
		el = new HashMap<>();
		el.put("CPU engine", new CPUEngine());
		el.put("GPU engine", new GPUEngine());
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
	public void setThreadName(Thread t, String name) {
		t.setName(name);
	}

	@Override
	public void setThreadDaemon(Thread t) {
		t.setDaemon(true);
	}

	@Override
	public void setThreadDaemon(Thread t, boolean value) {
		t.setDaemon(value);
	}

	@Override
	public void exit(int value) {
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
	public void alphaChanged(boolean val) {
		
	}

	@Override
	public void shiftChanged(boolean val) {
	}

	@Override
	public Semaphore newSemaphore() {
		return new DesktopSemaphore(0);
	}

	@Override
	public Semaphore newSemaphore(int i) {
		return new DesktopSemaphore(i);
	}

	@Override
	public URLClassLoader newURLClassLoader(URL[] urls) {
		return new DesktopURLClassLoader(urls);
	}

	@Override
	public Map<String, GraphicEngine> getEnginesList() {
		return el;
	}

	@Override
	public GraphicEngine getEngine(String string) throws NullPointerException {
		return el.get(string);
	}

	@Override
	public void throwNewExceptionInInitializerError(String text) {
		throw new ExceptionInInitializerError();
	}

	@Override
	public String[] stacktraceToString(Error e) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString().toUpperCase().replace("\t", "    ").replace("\r", "").split("\n");
	}

	@Override
	public void loadPlatformRules() {
		
	}

	@Override
	public void zip(String targetPath, String destinationFilePath, String password) {
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
			if (targetFile.isFile()) {
				zipFile.addFile(targetFile, parameters);
			} else if (targetFile.isDirectory()) {
				zipFile.addFolder(targetFile, parameters);
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unzip(String targetZipFilePath, String destinationFolderPath, String password) {
		try {
			final ZipFile zipFile = new ZipFile(targetZipFilePath);
			if (zipFile.isEncrypted()) {
				zipFile.setPassword(password);
			}
			zipFile.extractAll(destinationFolderPath);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean compile(String[] command, PrintWriter printWriter, PrintWriter errors) {
		return org.eclipse.jdt.internal.compiler.batch.Main.compile(command, printWriter, errors, null);
	}

}
