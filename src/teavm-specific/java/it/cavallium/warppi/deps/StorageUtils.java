package it.cavallium.warppi.deps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.teavm.classlib.java.nio.charset.TCharset;
import org.teavm.jso.browser.Window;

public class StorageUtils {
	public static final String basepath;
	static {
		String fullurl = Window.current().getLocation().getFullURL();
		if (fullurl.charAt(fullurl.length()-1) == '/') {
			basepath = fullurl+"resources";
		} else {
			basepath = fullurl+"/resources";
		}
}
	
	private static Map<String, File> resourcesCache = new HashMap<String, File>();
	
	public static final boolean exists(Path f) {
		return f.toFile().exists();
	}
	
	public static final boolean exists(File f) {
		return f.exists();
	}

	public static File get(String... path) {
		return new File(join(path, File.separator));
	}
	
	private static String join(String[] list, String conjunction)
	{
	   StringBuilder sb = new StringBuilder();
	   boolean first = true;
	   for (String item : list)
	   {
	      if (first)
	         first = false;
	      else
	         sb.append(conjunction);
	      sb.append(item);
	   }
	   return sb.toString();
	}
	
	public static File getResource(String path) throws IOException, URISyntaxException {
		try {
			File targetFile;
			if (resourcesCache.containsKey(path)) {
				if ((targetFile = resourcesCache.get(path)).exists()) {
					return targetFile;
				} else {
					resourcesCache.remove(path);
				}
			}
			final URL res = new URL(basepath+path);
			InputStream initialStream = res.openStream();
			byte[] buffer = new byte[initialStream.available()];
		    initialStream.read(buffer);
		    
		    targetFile = File.createTempFile("res", ".bin");
		    targetFile.createNewFile();
		    FileOutputStream outStream = new FileOutputStream(targetFile);
		    outStream.write(buffer);
		    outStream.close();
		    resourcesCache.put(path, targetFile);
		    return targetFile;
		} catch (final java.lang.IllegalArgumentException e) {
			throw e;
		}
	}

	public static InputStream getResourceStream(String path) throws IOException, URISyntaxException {
		try {
			File targetFile;
			if (resourcesCache.containsKey(path)) {
				if ((targetFile = resourcesCache.get(path)).exists()) {
					return new FileInputStream(targetFile);
				} else {
					resourcesCache.remove(path);
				}
			}
			final URL res = new URL(basepath+path);
			InputStream initialStream = res.openStream();
			byte[] buffer = new byte[initialStream.available()];
		    initialStream.read(buffer);
		    
		    targetFile = File.createTempFile("res", ".bin");
		    targetFile.createNewFile();
		    FileOutputStream outStream = new FileOutputStream(targetFile);
		    outStream.write(buffer);
		    outStream.close();
		    resourcesCache.put(path, targetFile);
		    return new FileInputStream(targetFile);
		} catch (final java.lang.IllegalArgumentException e) {
			throw e;
		}
	}

	public static List<String> readAllLines(File file) throws IOException {
        Reader reader_ = new InputStreamReader(new FileInputStream(file), Charset.defaultCharset());
        BufferedReader reader = reader_ instanceof BufferedReader ? (BufferedReader) reader_ : new BufferedReader(reader_);
        List<String> list = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        reader.close();
        return list;
	}

	public static String read(InputStream input) throws IOException {
		return IOUtils.toString(input);
	}

	public static List<File> walk(File dir) throws IOException {
		List<File> out = new ArrayList<>();
		File[] filesList = dir.listFiles();
		if (filesList == null) {
			out.add(dir);
		} else {
			for (File f : filesList) {
				if (f.isDirectory()) {
					if (f.canRead()) {
						out.addAll(walk(dir));
					}
				} else if (f.isFile()) {
					if (f.canRead()) {
						out.add(f);
					}
				}
			}
		}
		return out;
	}

	public static File relativize(File rulesPath, File f) {
		return f;
	}

	public static File resolve(File file, String string) {
		return new File(file.getAbsolutePath() + File.separatorChar + string);
	}

	public static File getParent(File f) {
		return f.getParentFile();
	}

	public static void createDirectories(File dir) {
		dir.mkdirs();
	}

	public static void write(File f, byte[] bytes, DStandardOpenOption... options) throws IOException {
		boolean create = false;
		for (DStandardOpenOption opt : options) {
			if (opt == DStandardOpenOption.CREATE) {
				create = true;
			}
		}
		if (f.exists() == false) {
			if (create) {
				if (!f.createNewFile()) {
					throw new IOException("File doesn't exist, can't create it!");
				}
			} else {
				throw new IOException("File doesn't exist.");
			}
		}
		FileOutputStream stream = new FileOutputStream(f);
		stream.write(bytes);
		stream.close();
	}
	
	public static List<String> readAllLines(InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			String thisLine = null;
			ArrayList<String> output = new ArrayList<>();
			while ((thisLine = buffer.readLine()) != null) {
				output.add(thisLine);
	        }
			return output;
		}
	}
}
