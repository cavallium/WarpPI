package it.cavallium.warppi.teavm;

import org.teavm.jso.browser.Window;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class TeaVMStorageUtils {
	public static URL getUrl(File file) throws MalformedURLException {
		String path = file.toString();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		String protocol = Window.current().getLocation().getProtocol();
		String host = Window.current().getLocation().getHost();
		String indexPath = Window.current().getLocation().getPathName();
		String websiteDir;
		if (indexPath.endsWith("/")) {
			websiteDir = indexPath;
		} else {
			String[] parts = indexPath.split("/");
			String[] dirParts = Arrays.copyOf(parts, parts.length - 1);
			StringBuilder dirBuilder = new StringBuilder("/");
			for (String dirPart : dirParts) {
				dirBuilder.append(dirPart);
				dirBuilder.append('/');
			}
			websiteDir = dirBuilder.toString();
		}
		String fullUrl = protocol + "//" + host + websiteDir;
		if (fullUrl.endsWith("/")) {
			fullUrl = fullUrl.substring(0, fullUrl.length() - 1);
		}

		String urlString = fullUrl + "/" + path;
		try {
			return new URL(urlString);
		} catch (MalformedURLException e) {
			System.out.println("Errored URL: " + fullUrl + "/" + path);
			throw e;
		}
	}
}
