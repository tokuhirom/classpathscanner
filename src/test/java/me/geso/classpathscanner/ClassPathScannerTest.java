package me.geso.classpathscanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.common.reflect.ClassPath;

public class ClassPathScannerTest {

	@Test
	public void testScanTopLevelClasses() throws IOException {
		ClassPathScanner packageScanner = new ClassPathScanner(this.getClass()
				.getClassLoader());
		long t1 = System.currentTimeMillis();
		Collection<ClassInfo> classes = packageScanner
				.scanTopLevelClasses();
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
		// classes.stream().map(k -> k.getName()).sorted()
		// .filter(k -> k.startsWith("org"))
		// .forEach(k -> System.out.println(k));
		assertTrue(classes
				.stream()
				.filter(klass -> "me.geso.classpathscanner".equals(klass
						.getPackageName()))
				.count() > 0);
		{
			List<ClassInfo> gangs = classes.stream()
					.filter(it -> it.getName().endsWith("TopLevelGang"))
					.collect(Collectors.toList());
			assertEquals(1, gangs.size());
			assertEquals("TopLevelGang", gangs.get(0).getName());
		}
	}

	@Test
	public void testScanTopLevelClassesForPackage() throws IOException {
		long t1 = System.currentTimeMillis();
		ClassPathScanner packageScanner = new ClassPathScanner(this.getClass()
				.getClassLoader());
		Collection<ClassInfo> classes = packageScanner
				.scanTopLevelClasses(Package
						.getPackage("me.geso.classpathscanner"));
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
		List<String> got = classes.stream().map(it -> it.getName()).sorted()
				.collect(Collectors.toList());
		assertEquals(
				Arrays.asList(
						"me.geso.classpathscanner.ClassInfo",
						"me.geso.classpathscanner.ClassPathScanner",
						"me.geso.classpathscanner.ClassPathScannerTest",
						"me.geso.classpathscanner.ResourceScanner",
						"me.geso.classpathscanner.ResourceScannerTest"
						).stream().sorted().collect(Collectors.toList()),
				got);
	}

	@Test
	public void testRecursive() throws IOException {
		ClassPathScanner packageScanner = new ClassPathScanner(this.getClass()
				.getClassLoader());
		Collection<ClassInfo> classes = packageScanner
				.scanTopLevelClassesRecursive(Package
						.getPackage("me.geso.classpathscanner"));
		List<String> got = classes.stream().map(it -> it.getName()).sorted()
				.collect(Collectors.toList());
		assertEquals(
				Arrays.asList(
						"me.geso.classpathscanner.ClassInfo",
						"me.geso.classpathscanner.ClassPathScanner",
						"me.geso.classpathscanner.ClassPathScannerTest",
						"me.geso.classpathscanner.child.IamAchild",
						"me.geso.classpathscanner.ResourceScanner",
						"me.geso.classpathscanner.ResourceScannerTest"
						).stream().sorted().collect(Collectors.toList()),
				got);
		classes.forEach(it -> {
			try {
				it.load();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	public void testJar() throws IOException, ClassNotFoundException {
		URLClassLoader urlClassLoader = new URLClassLoader(
				new URL[] {
						new File("src/test/resources/hello-0.0.1-SNAPSHOT.jar")
								.toURI().toURL()
				},
				this.getClass().getClassLoader());
		ClassPathScanner packageScanner = new ClassPathScanner(urlClassLoader);
		Collection<ClassInfo> classes = packageScanner
				.scanTopLevelClasses("hello");
		String got = classes.stream().map(it -> it.getName()).sorted()
				.collect(Collectors.joining(","));
		assertEquals(
				"hello.Hello",
				got);
		classes.forEach(it -> {
			try {
				it.load();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static class InnerClass {
	}

	@Test
	public void testCompareGuava() throws IOException {
		{
			List<String> guava = ClassPath
					.from(this.getClass().getClassLoader())
					.getTopLevelClasses()
					.stream().map(it -> it.getName())
					.sorted()
					.collect(Collectors.toList());
			List<String> ours = new ClassPathScanner(this.getClass()
					.getClassLoader())
					.scanTopLevelClasses()
					.stream()
					.map(it -> it.getName())
					.sorted()
					.collect(Collectors.toList());

			assertEquals(
					ours,
					guava);
		}
		{
			String pkg = this.getClass().getPackage().getName();
			List<String> guava = ClassPath
					.from(this.getClass().getClassLoader())
					.getTopLevelClasses(pkg)
					.stream().map(it -> it.getName())
					.sorted()
					.collect(Collectors.toList());
			List<String> ours = new ClassPathScanner(this.getClass()
					.getClassLoader())
					.scanTopLevelClasses(this.getClass().getPackage())
					.stream()
					.map(it -> it.getName())
					.sorted()
					.collect(Collectors.toList());

			assertEquals(guava, ours);
		}
		{
			List<String> guava = ClassPath
					.from(this.getClass().getClassLoader())
					.getTopLevelClassesRecursive(
							this.getClass().getPackage().getName())
					.stream().map(it -> it.getName())
					.sorted()
					.collect(Collectors.toList());
			List<String> ours = new ClassPathScanner(this.getClass()
					.getClassLoader())
					.scanTopLevelClassesRecursive(this.getClass().getPackage())
					.stream()
					.map(it -> it.getName())
					.sorted()
					.collect(Collectors.toList());

			assertEquals(guava, ours);
		}
		{
			Set<String> guava = ClassPath
					.from(this.getClass().getClassLoader())
					.getResources()
					.stream().map(it -> it.getResourceName())
					.sorted()
					.collect(Collectors.toSet());
			Set<String> ours = new ResourceScanner()
					.scanResources(this.getClass()
					.getClassLoader())
					.stream()
					.sorted()
					.collect(Collectors.toSet());
			assertEquals(guava, ours);
		}
	}

}
