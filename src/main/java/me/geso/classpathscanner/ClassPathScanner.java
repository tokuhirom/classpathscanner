package me.geso.classpathscanner;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ClassPathScanner {
	private final ClassLoader classLoader;

	/**
	 * Create new instance.
	 * 
	 * @param classLoader
	 */
	public ClassPathScanner(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Scan top level classes from package.
	 * 
	 * @param pkg
	 * @return
	 * @throws IOException
	 */
	public Set<ClassInfo> scanTopLevelClasses(Package pkg) throws IOException {
		Objects.requireNonNull(pkg);
		return ClassPathScanner.getClassesForPackage(this.classLoader, pkg)
				.stream()
				.filter(klass -> klass.getName().indexOf('$') == -1)
				.filter(klass -> klass.getPackageName().equals(pkg.getName()))
				.collect(Collectors.toSet());
	}

	/**
	 * Scan top level classes from package.
	 * 
	 * @param pkg
	 * @return
	 * @throws IOException
	 */
	public Set<ClassInfo> scanTopLevelClassesRecursive(Package pkg)
			throws IOException {
		Objects.requireNonNull(pkg);
		return ClassPathScanner.getClassesForPackage(this.classLoader, pkg)
				.stream()
				.filter(klass -> klass.getName().indexOf('$') == -1)
				.collect(Collectors.toSet());
	}

	private static void processDirectory(File directory, String pkgname,
			HashSet<ClassInfo> classes) {
		// Get the list of the files contained in the package
		final String[] files = directory.list();
		for (int i = 0; i < files.length; i++) {
			final String fileName = files[i];
			String className = null;
			// we are only interested in .class files
			if (fileName.endsWith(".class")) {
				// removes the .class extension
				className = pkgname + '.'
						+ fileName.substring(0, fileName.length() - 6);
			}
			if (className != null) {
				classes.add(new ClassInfo(className));
			}
			final File subdir = new File(directory, fileName);
			if (subdir.isDirectory()) {
				processDirectory(subdir, pkgname + '.' + fileName, classes);
			}
		}
	}

	private static void processJarfile(URL resource, String pkgname,
			HashSet<ClassInfo> classes) {
		final String relPath = pkgname.replace('.', '/');
		final String resPath = resource.getPath();
		final String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar")
				.replaceFirst("file:", "");
		try (JarFile jarFile = new JarFile(jarPath)) {
			final Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				final String entryName = entry.getName();
				String className = null;
				if (entryName.endsWith(".class")
						&& entryName.startsWith(relPath)
						&& entryName.length() > (relPath.length() + "/"
								.length())) {
					className = entryName.replace('/', '.').replace('\\', '.')
							.replace(".class", "");
				}
				if (className != null) {
					classes.add(new ClassInfo(className));
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(
					"Unexpected IOException reading JAR File '" + jarPath + "'",
					e);
		}
	}

	private static Set<ClassInfo> getClassesForPackage(
			ClassLoader classLoader, Package pkg) throws IOException {
		final HashSet<ClassInfo> classes = new HashSet<>();

		final String pkgname = pkg.getName();
		final String relPath = pkgname.replace('.', '/');

		// Get a File object for the package
		final Enumeration<URL> resources = classLoader.getResources(
				relPath);
		if (!resources.hasMoreElements()) {
			throw new RuntimeException("Unexpected problem: No resource for "
					+ relPath);
		}
		while (resources.hasMoreElements()) {
			final URL resource = resources.nextElement();
			resource.getPath();
			if (resource.toString().startsWith("jar:")) {
				processJarfile(resource, pkgname, classes);
			} else {
				processDirectory(new File(resource.getPath()), pkgname, classes);
			}
		}

		return classes;
	}
}
