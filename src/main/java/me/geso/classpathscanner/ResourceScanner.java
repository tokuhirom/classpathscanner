package me.geso.classpathscanner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scan all resource files from class path.
 *
 */
public class ResourceScanner {
	public ResourceScanner() {
	}

	public Collection<String> scanResources(
			ClassLoader classLoader) throws IOException {
		final HashSet<String> classes = new HashSet<>();

		if (classLoader.getParent() != null) {
			classes.addAll(scanResources(classLoader.getParent()));
		}

		if (classLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
			for (URL url : urlClassLoader.getURLs()) {
				URI uri;
				try {
					uri = url.toURI();
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException(e);
				}
				if (uri.toString().endsWith(".jar")) {
					final String resPath = uri.getPath();
					final String jarPath = resPath.replaceFirst("[.]jar[!].*",
							".jar")
							.replaceFirst("file:", "");
					try (JarFile jarFile = new JarFile(jarPath)) {
						final Enumeration<JarEntry> entries = jarFile.entries();
						while (entries.hasMoreElements()) {
							final JarEntry entry = entries.nextElement();
							if (!entry.isDirectory()) {
								final String entryName = entry.getName();
								classes.add(entryName);
							}
						}
					} catch (final IOException e) {
						throw new RuntimeException(
								"Unexpected IOException reading JAR File '"
										+ jarPath + "'",
								e);
					}
				} else {
					scanDirectory(new File(uri.getPath()),
							"",
							classes);
				}
			}
		}

		// This is not a resource file!
		classes.remove("META-INF/MANIFEST.MF");

		return classes;
	}

	private void scanDirectory(File directory,
			String path,
			Set<String> classes) {
		// Get the list of the files contained in the package
		final String[] files = directory.list();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				final String fileName = files[i];
				final File subdir = new File(directory, fileName);
				if (subdir.isDirectory()) {
					scanDirectory(subdir,
							(path.isEmpty() ? "" : path + "/") + fileName,
							classes);
				} else {
					classes.add((path.isEmpty() ? "" : path + "/") + fileName);
				}
			}
		}
	}

}
