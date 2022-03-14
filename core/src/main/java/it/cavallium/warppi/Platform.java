package it.cavallium.warppi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.device.DeviceStateDevice;
import it.cavallium.warppi.device.display.BacklightOutputDevice;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.device.input.KeyboardInputDevice;
import it.cavallium.warppi.device.input.TouchInputDevice;
import it.cavallium.warppi.util.Error;

public interface Platform {

	ConsoleUtils getConsoleUtils();

	Gpio getGpio();

	PlatformStorage getPlatformStorage();

	ImageUtils getImageUtils();

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

	TouchInputDevice getTouchInputDevice();
	
	KeyboardInputDevice getKeyboardInputDevice();
	
	DisplayOutputDevice getDisplayOutputDevice();

	BacklightOutputDevice getBacklightOutputDevice();

	DeviceStateDevice getDeviceStateDevice();
	
	void throwNewExceptionInInitializerError(String text);

	String[] stacktraceToString(Error e);

	/**
	 * Determines the list of files containing DSL rules to load.
	 *
	 * @return a <code>List</code> of paths of files which contain DSL rules.
	 * 		   Each <code>String</code> in the returned <code>List</code> can be passed as an argument to
	 * 		   {@link PlatformStorage#getResourceStream(String)} to access the corresponding file's contents.
	 * @throws IOException if an IO error occurs while getting the list of rule file paths.
	 */
	List<String> getRuleFilePaths() throws IOException;

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

	public interface PlatformStorage {
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

		File getRootPath();
	}

	public interface Semaphore {
		void release();

		void acquire() throws InterruptedException;
	}

	public interface URLClassLoader {
		Class<?> loadClass(String name) throws ClassNotFoundException;

		void close() throws IOException;
	}
	
	public interface ImageUtils {
		
		ImageReader load(InputStream resourceStream) throws IOException;
		
		public interface ImageReader {
			
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

	void setArguments(StartupArguments args);

}
