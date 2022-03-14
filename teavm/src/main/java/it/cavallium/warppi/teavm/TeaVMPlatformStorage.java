package it.cavallium.warppi.teavm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.teavm.jso.browser.Window;

import it.cavallium.warppi.Platform.PlatformStorage;

public class TeaVMPlatformStorage implements PlatformStorage {
	@Override
	public boolean exists(final File f) {
		return f.exists();
	}

	@Override
	public File get(final String path) {
		return new File(path);
	}

	private static String join(final String[] list, final String conjunction) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final String item : list) {
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}

	@Override
	public File get(final String... path) {
		return new File(TeaVMPlatformStorage.join(path, File.separator));
	}

	private static Map<String, File> resourcesCache = new HashMap<>();

	@Override
	public File getResource(final String path) throws IOException, URISyntaxException {
		try {
			File targetFile;
			if (TeaVMPlatformStorage.resourcesCache.containsKey(path))
				if ((targetFile = TeaVMPlatformStorage.resourcesCache.get(path)).exists())
					return targetFile;
				else
					TeaVMPlatformStorage.resourcesCache.remove(path);
			final URL res = TeaVMStorageUtils.getUrl(new File(getRootPath(), path));
			final InputStream initialStream = res.openStream();
			final byte[] buffer = new byte[initialStream.available()];
			initialStream.read(buffer);

			targetFile = File.createTempFile("res", ".bin");
			targetFile.createNewFile();
			final FileOutputStream outStream = new FileOutputStream(targetFile);
			outStream.write(buffer);
			outStream.close();
			TeaVMPlatformStorage.resourcesCache.put(path, targetFile);
			return targetFile;
		} catch (final java.lang.IllegalArgumentException e) {
			throw e;
		}
	}

	@Override
	public InputStream getResourceStream(final String path) throws IOException {
		try {
			File targetFile;
			if (TeaVMPlatformStorage.resourcesCache.containsKey(path))
				if ((targetFile = TeaVMPlatformStorage.resourcesCache.get(path)).exists())
					return new FileInputStream(targetFile);
				else
					TeaVMPlatformStorage.resourcesCache.remove(path);
			final URL res = TeaVMStorageUtils.getUrl(new File(getRootPath(), path));
			final InputStream initialStream = res.openStream();
			final byte[] buffer = new byte[initialStream.available()];
			initialStream.read(buffer);

			targetFile = File.createTempFile("res", ".bin");
			targetFile.createNewFile();
			final FileOutputStream outStream = new FileOutputStream(targetFile);
			outStream.write(buffer);
			outStream.close();
			TeaVMPlatformStorage.resourcesCache.put(path, targetFile);
			return new FileInputStream(targetFile);
		} catch (final java.lang.IllegalArgumentException e) {
			throw e;
		}
	}

	@Override
	public boolean doesResourceExist(final String path) throws IOException {
		try {
			File targetFile;
			if (TeaVMPlatformStorage.resourcesCache.containsKey(path))
				if ((targetFile = TeaVMPlatformStorage.resourcesCache.get(path)).exists())
					return true;
				else
					TeaVMPlatformStorage.resourcesCache.remove(path);
			return true;
		} catch (final java.lang.IllegalArgumentException e) {
			throw e;
		}
	}

	@Override
	public List<String> readAllLines(final File file) throws IOException {
		final Reader reader_ = new InputStreamReader(new FileInputStream(file), Charset.defaultCharset());
		final BufferedReader reader = reader_ instanceof BufferedReader ? (BufferedReader) reader_ : new BufferedReader(reader_);
		final List<String> list = new ArrayList<>();
		String line = reader.readLine();
		while (line != null) {
			list.add(line);
			line = reader.readLine();
		}
		reader.close();
		return list;
	}

	// Reading an InputStream as a String is implemented this way
	// because, on TeaVM, more convenient methods (such as IOUtils.toString) hang.
	@Override
	public String read(final InputStream input) throws IOException {
		final int BUFFER_SIZE = 1024;

		final StringBuilder builder = new StringBuilder();
		try (final Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			final char[] buffer = new char[BUFFER_SIZE];
			int readCount;
			while ((readCount = reader.read(buffer)) != -1) {
				builder.append(buffer, 0, readCount);
			}
		}
		return builder.toString();
	}

	@Override
	public List<File> walk(final File dir) throws IOException {
		final List<File> out = new ArrayList<>();
		final File[] filesList = dir.listFiles();
		if (filesList == null)
			out.add(dir);
		else
			for (final File f : filesList)
				if (f.isDirectory()) {
					if (f.canRead())
						out.addAll(walk(dir));
				} else if (f.isFile())
					if (f.canRead())
						out.add(f);
		return out;
	}

	@Override
	public File relativize(final File rulesPath, final File f) {
		return f;
	}

	@Override
	public File resolve(final File file, final String string) {
		return new File(file.getAbsolutePath() + File.separatorChar + string);
	}

	@Override
	public File getParent(final File f) {
		return f.getParentFile();
	}

	@Override
	public void createDirectories(final File dir) throws IOException {
		dir.mkdirs();
	}

	@Override
	public void write(final File f, final byte[] bytes, final int... options) throws IOException {
		boolean create = false;
		for (final int opt : options)
			if (opt == PlatformStorage.OpenOptionCreate)
				create = true;
		if (f.exists() == false)
			if (create) {
				if (!f.createNewFile())
					throw new IOException("File doesn't exist, can't create it!");
			} else
				throw new IOException("File doesn't exist.");
		final FileOutputStream stream = new FileOutputStream(f);
		stream.write(bytes);
		stream.close();
	}

	@Override
	public List<String> readAllLines(final InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			String thisLine = null;
			final ArrayList<String> output = new ArrayList<>();
			while ((thisLine = buffer.readLine()) != null)
				output.add(thisLine);
			return output;
		}
	}

	@Override
	public File getRootPath() {
		return new File("resources");
	}
}
