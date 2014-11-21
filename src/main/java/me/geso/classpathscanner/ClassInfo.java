package me.geso.classpathscanner;

/**
 * This class represents the class information without loading.
 * 
 * @author tokuhirom
 *
 */
public class ClassInfo {
	@Override
	public String toString() {
		return "ClassInfo [name=" + name + "]";
	}

	private final String name;

	/**
	 * Internal use only.
	 *
	 * @param name
	 */
	public ClassInfo(String name) {
		this.name = name;
	}

	/**
	 * Get a full name.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the package name of {@code classFullName} according to the Java
	 * Language Specification (section 6.7). Unlike {@link Class#getPackage},
	 * this method only parses the class name, without attempting to define the
	 * {@link Package} and hence load files.
	 */
	public String getPackageName() {
		int lastDot = this.name.lastIndexOf('.');
		return (lastDot < 0) ? "" : this.name.substring(0, lastDot);
	}

	/**
	 * Load a class.
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> load() throws ClassNotFoundException {
		return Class.forName(this.name);
	}
}