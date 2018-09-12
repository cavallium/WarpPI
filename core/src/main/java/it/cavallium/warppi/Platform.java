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

	public ConsoleUtils getConsoleUtils();
	public Gpio getGpio();
	public StorageUtils getStorageUtils();
	public PngUtils getPngUtils();
	public Settings getSettings();

	public void setThreadName(Thread t, String name);
	public void setThreadDaemon(Thread t);
	public void setThreadDaemon(Thread t, boolean value);

	public void exit(int value);
	public void gc();

	public boolean isJavascript();
	public boolean isRunningOnRaspberry();
	
	public String getOsName();

	public void alphaChanged(boolean val);
	public void shiftChanged(boolean val);

	public Semaphore newSemaphore();
	public Semaphore newSemaphore(int i);
	
	public URLClassLoader newURLClassLoader(URL[] urls);

	public Map<String, GraphicEngine> getEnginesList();
	public GraphicEngine getEngine(String string) throws NullPointerException;

	public void throwNewExceptionInInitializerError(String text);
	public String[] stacktraceToString(Error e);

	public void loadPlatformRules();
	public void zip(String targetPath, String destinationFilePath, String password);
	public void unzip(String targetZipFilePath, String destinationFolderPath, String password);

	public boolean compile(String[] command, PrintWriter printWriter, PrintWriter errors);

	public interface Gpio {
		public int valueOutput();
		public int valuePwmOutput();
		public int valueInput();
		public int valueHigh();
		public int valueLow();
		public Object valueUnknownBoardType();

		public void wiringPiSetupPhys();

		public void pinMode(int i, int type);

		public void digitalWrite(int pin, int val);

		public void digitalWrite(int pin, boolean val);

		public void pwmWrite(int pin, int val);
		
		public void delayMicroseconds(int t);

		public int digitalRead(int pin);

		public Object getBoardType();
	}
	
	public interface ConsoleUtils {
		public static final int OUTPUTLEVEL_NODEBUG = 0;
		public static final int OUTPUTLEVEL_DEBUG_MIN = 1;
		public static final int OUTPUTLEVEL_DEBUG_VERBOSE = 4;
		
		public AdvancedOutputStream out();
		
		public int getOutputLevel();
		
		public interface AdvancedOutputStream {
			public void println(Object str);

			public void println(int level);

			public void println(int level, Object str);

			public void print(int level, String str);

			public void println(int level, String prefix, String str);

			public void println(int level, String... parts);
		}
	}
	
	public interface StorageUtils {
		int OpenOptionWrite = 0;
		int OpenOptionCreate = 1;

		public boolean exists(File f);

		public File get(String path);

		public File get(String... path);

		@Deprecated()
		public File getResource(String string) throws IOException, URISyntaxException;

		public InputStream getResourceStream(String string) throws IOException, URISyntaxException;

		public List<String> readAllLines(File file) throws IOException;
		
		public String read(InputStream input) throws IOException;

		public List<File> walk(File dir) throws IOException;

		public File relativize(File rulesPath, File f);

		public File resolve(File file, String string);
		
		public File getParent(File f);

		public void createDirectories(File dir) throws IOException;

		public void write(File f, byte[] bytes, int... options) throws IOException;

		public List<String> readAllLines(InputStream input) throws IOException;

		public String getBasePath();
	}
	
	public interface Semaphore {
		void release();
		void acquire() throws InterruptedException;
	}
	
	public interface URLClassLoader {
	public Class<?> loadClass(String name) throws ClassNotFoundException;
		void close() throws IOException;
	}
	
	public interface PngUtils {

		public PngReader load(InputStream resourceStream);
		
		public interface PngReader {

			int[] getImageMatrix();

			int[] getSize();
			
		}
		
	}
	
	public interface Settings {

		public boolean isDebugEnabled();

		public void setDebugEnabled(boolean debugOn);

		public default String getCalculatorName() {
			return "WarpPI";
		}
		public default String getCalculatorNameLowercase() {
			return "warppi";
		}
		public default String getCalculatorNameUppercase() {
			return "WARPPI";
		}

	}

}
