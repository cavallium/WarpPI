package it.cavallium.warppi.teavm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.teavm.jso.browser.Window;

import it.cavallium.warppi.Platform.StorageUtils;

public class TeaVMStorageUtils implements StorageUtils {
	public boolean exists(File f) {
		return f.exists();
	}

	public File get(String path) {
		return new File(path);
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
	
	public File get(String... path) {
		return new File(join(path, File.separator));
	}

	private static Map<String, File> resourcesCache = new HashMap<String, File>();

	public File getResource(String path) throws IOException, URISyntaxException {
		try {
			File targetFile;
			if (resourcesCache.containsKey(path)) {
				if ((targetFile = resourcesCache.get(path)).exists()) {
					return targetFile;
				} else {
					resourcesCache.remove(path);
				}
			}
			final URL res = new URL(this.getBasePath()+path);
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

	public InputStream getResourceStream(String path) throws IOException, URISyntaxException {
		try {
			File targetFile;
			if (resourcesCache.containsKey(path)) {
				if ((targetFile = resourcesCache.get(path)).exists()) {
					return new FileInputStream(targetFile);
				} else {
					resourcesCache.remove(path);
				}
			}
			final URL res = new URL(this.getBasePath()+path);
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

	public List<String> readAllLines(File file) throws IOException {
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

	public String read(InputStream input) throws IOException {
		return IOUtils.toString(input, "UTF-8");
	}

	public List<File> walk(File dir) throws IOException {
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

	public File relativize(File rulesPath, File f) {
		return f;
	}

	public File resolve(File file, String string) {
		return new File(file.getAbsolutePath() + File.separatorChar + string);
	}

	public File getParent(File f) {
		return f.getParentFile();
	}

	public void createDirectories(File dir) throws IOException {
		dir.mkdirs();
	}

	public void write(File f, byte[] bytes, int... options) throws IOException {
		boolean create = false;
		for (int opt : options) {
			if (opt == StorageUtils.OpenOptionCreate) {
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

	public List<String> readAllLines(InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			String thisLine = null;
			ArrayList<String> output = new ArrayList<>();
			while ((thisLine = buffer.readLine()) != null) {
				output.add(thisLine);
	        }
			return output;
		}
	}

	@Override
	public String getBasePath() {
		String fullurl = Window.current().getLocation().getFullURL();
		if (fullurl.charAt(fullurl.length()-1) == '/') {
			return fullurl+"resources";
		} else {
			return fullurl+"/resources";
		}
	}
}
