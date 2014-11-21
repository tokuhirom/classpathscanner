package me.geso.classpathscanner;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class ClassPathScannerTest {

	@Test
	public void test() throws IOException {
		ClassPathScanner packageScanner = new ClassPathScanner(this.getClass()
				.getClassLoader());
		Set<ClassInfo> classes = packageScanner
				.scanTopLevelClasses(Package
						.getPackage("me.geso.classpathscanner"));
		String got = classes.stream().map(it -> it.getName()).sorted()
				.collect(Collectors.joining(","));
		assertEquals(
				"me.geso.classpathscanner.ClassInfo,me.geso.classpathscanner.ClassPathScanner,me.geso.classpathscanner.ClassPathScannerTest",
				got);
	}

	@Test
	public void testRecursive() throws IOException {
		ClassPathScanner packageScanner = new ClassPathScanner(this.getClass()
				.getClassLoader());
		Set<ClassInfo> classes = packageScanner
				.scanTopLevelClassesRecursive(Package
						.getPackage("me.geso.classpathscanner"));
		String got = classes.stream().map(it -> it.getName()).sorted()
				.collect(Collectors.joining(","));
		assertEquals(
				"me.geso.classpathscanner.ClassInfo,me.geso.classpathscanner.ClassPathScanner,me.geso.classpathscanner.ClassPathScannerTest,me.geso.classpathscanner.child.IamAchild",
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
	public void testJar() throws IOException {
		// junit comes from jar!
		ClassPathScanner packageScanner = new ClassPathScanner(this.getClass()
				.getClassLoader());
		Set<ClassInfo> classes = packageScanner
				.scanTopLevelClasses(Package
						.getPackage("org.junit"));
		String got = classes.stream().map(it -> it.getName()).sorted()
				.collect(Collectors.joining(","));
		assertEquals(
				"org.junit.After,org.junit.AfterClass,org.junit.Assert,org.junit.Assume,org.junit.Before,org.junit.BeforeClass,org.junit.ClassRule,org.junit.ComparisonFailure,org.junit.FixMethodOrder,org.junit.Ignore,org.junit.Rule,org.junit.Test,org.junit.package-info",
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

}
