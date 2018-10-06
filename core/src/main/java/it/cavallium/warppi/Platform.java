package it.cavallium.warppi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.util.Error;

public interface Platform {

	ConsoleUtils getConsoleUtils();

	Gpio getGpio();

	StorageUtils getStorageUtils();

	PngUtils getPngUtils();

	Settings getSettings();

	void setThreadName(Thread t, String name);

	void setThreadDaemon(Thread t);

	void setThreadDaemon(Thread t, boolean value);

	void exit(int value);

	void gc();

	boolean isJavascript();

	void setRunningOnRaspberry(boolean b);
	
	boolean isRunningOnRaspberry();

	String getOsName();

	void alphaChanged(boolean val);

	void shiftChanged(boolean val);

	Semaphore newSemaphore();

	Semaphore newSemaphore(int i);

	URLClassLoader newURLClassLoader(URL[] urls);

	Map<String, GraphicEngine> getEnginesList();

	GraphicEngine getEngine(String string) throws NullPointerException;

	void throwNewExceptionInInitializerError(String text);

	String[] stacktraceToString(Error e);

	void loadPlatformRules();

	void zip(String targetPath, String destinationFilePath, String password);

	void unzip(String targetZipFilePath, String destinationFolderPath, String password);

	boolean compile(String[] command, PrintWriter printWriter, PrintWriter errors);

	public interface Gpio {
		int valueOutput();

		int valuePwmOutput();

		int valueInput();

		int valueHigh();

		int valueLow();

		Object valueUnknownBoardType();

		void wiringPiSetupPhys();

		void pinMode(int i, int type);

		void digitalWrite(int pin, int val);

		void digitalWrite(int pin, boolean val);

		void pwmWrite(int pin, int val);

		void delayMicroseconds(int t);

		int digitalRead(int pin);

		Object getBoardType();
	}

	public interface ConsoleUtils {
		int OUTPUTLEVEL_NODEBUG = 0;
		int OUTPUTLEVEL_DEBUG_MIN = 1;
		int OUTPUTLEVEL_DEBUG_VERBOSE = 4;

		AdvancedOutputStream out();

		int getOutputLevel();

		public interface AdvancedOutputStream {
			void println(Object str);

			void println(int level);

			void println(int level, Object str);

			void print(int level, String str);

			void println(int level, String prefix, String str);

			void println(int level, String... parts);
		}
	}

	public interface StorageUtils {
		int OpenOptionWrite = 0;
		int OpenOptionCreate = 1;

		boolean exists(File f);

		File get(String path);

		File get(String... path);

		@Deprecated()
		File getResource(String string) throws IOException, URISyntaxException;

		boolean doesResourceExist(String string) throws IOException;

		InputStream getResourceStream(String string) throws IOException;

		List<String> readAllLines(File file) throws IOException;

		String read(InputStream input) throws IOException;

		List<File> walk(File dir) throws IOException;

		File relativize(File rulesPath, File f);

		File resolve(File file, String string);

		File getParent(File f);

		void createDirectories(File dir) throws IOException;

		void write(File f, byte[] bytes, int... options) throws IOException;

		List<String> readAllLines(InputStream input) throws IOException;

		String getBasePath();
	}

	public interface Semaphore {
		void release();

		void acquire() throws InterruptedException;
	}

	public interface URLClassLoader {
		Class<?> loadClass(String name) throws ClassNotFoundException;

		void close() throws IOException;
	}

	public interface PngUtils {

		PngReader load(InputStream resourceStream);

		public interface PngReader {

			int[] getImageMatrix();

			int[] getSize();

		}

	}

	public interface Settings {

		boolean isDebugEnabled();

		void setDebugEnabled(boolean debugOn);

		default String getCalculatorName() {
			return "WarpPI";
		}

		default String getCalculatorNameLowercase() {
			return "warppi";
		}

		default String getCalculatorNameUppercase() {
			return "WARPPI";
		}

	}

}
