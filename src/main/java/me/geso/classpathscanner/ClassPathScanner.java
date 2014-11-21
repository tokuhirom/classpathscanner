package me.geso.classpathscanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Scanner for the class path.
 * 
 * @author tokuhirom
 *
 */
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
	 * Scan top level classes.
	 * 
	 * @param pkg
	 * @return
	 * @throws IOException
	 */
	public Collection<ClassInfo> scanTopLevelClasses() throws IOException {
		return new ResourceScanner()
				.scanResources(this.classLoader)
				.stream()
				.filter(file -> file.endsWith(".class"))
				.map(file -> file.substring(0,
						file.length() - ".class".length()))
				.map(file -> file.replaceAll("\\\\", "/"))
				.map(file -> file.replaceAll("/", "."))
				.map(file -> new ClassInfo(file, classLoader))
				.filter(klass -> klass.getName().indexOf('$') == -1)
				.collect(Collectors.toSet());
	}

	/**
	 * Scan top level classes from package.
	 * 
	 * @param pkg
	 * @return
	 * @throws IOException
	 */
	public Collection<ClassInfo> scanTopLevelClasses(Package pkg)
			throws IOException {
		Objects.requireNonNull(pkg);
		return this.scanTopLevelClasses(pkg.getName());
	}

	/**
	 * Scan top level classes from package.
	 * 
	 * @param pkg
	 * @return
	 * @throws IOException
	 */
	public Collection<ClassInfo> scanTopLevelClasses(String pkg)
			throws IOException {
		Objects.requireNonNull(pkg);
		return this.getClassesForPackage(this.classLoader, pkg)
				.stream()
				.filter(klass -> klass.getName().indexOf('$') == -1)
				.filter(klass -> klass.getPackageName().equals(pkg))
				.collect(Collectors.toSet());
	}

	/**
	 * Scan top level classes from package.
	 * 
	 * @param pkg
	 * @return
	 * @throws IOException
	 */
	public Collection<ClassInfo> scanTopLevelClassesRecursive(Package pkg)
			throws IOException {
		Objects.requireNonNull(pkg);
		return this.getClassesForPackage(this.classLoader, pkg.getName())
				.stream()
				.filter(klass -> klass.getName().indexOf('$') == -1)
				.collect(Collectors.toSet());
	}

	private void processDirectory(File directory, String pkgname,
			HashSet<ClassInfo> classes) {
		// Get the list of the files contained in the package
		final String[] files = directory.list();
		for (int i = 0; i < files.length; i++) {
			final String fileName = files[i];
			// we are only interested in .class files
			if (fileName.endsWith(".class")) {
				// removes the .class extension
				String className = pkgname + '.'
						+ fileName.substring(0, fileName.length() - 6);
				classes.add(new ClassInfo(className, classLoader));
			}
			final File subdir = new File(directory, fileName);
			if (subdir.isDirectory()) {
				processDirectory(subdir, pkgname + '.' + fileName, classes);
			}
		}
	}

	private void processJarfile(URL resource, String pkgname,
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
					classes.add(new ClassInfo(className, classLoader));
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(
					"Unexpected IOException reading JAR File '" + jarPath + "'",
					e);
		}
	}

	// This pattern was optimized for scanning classes under the package.
	// 10x faster than ResourceScanner.
	private Set<ClassInfo> getClassesForPackage(
			ClassLoader classLoader, String pkgname) throws IOException {
		final HashSet<ClassInfo> classes = new HashSet<>();

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
				this.processJarfile(resource, pkgname, classes);
			} else {
				this.processDirectory(new File(resource.getPath()), pkgname,
						classes);
			}
		}

		return classes;
	}


}
