package me.geso.classpathscanner;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.junit.Test;

public class ResourceScannerTest {

	@Test
	public void test() throws IOException {
		Collection<String> resources = new ResourceScanner().scanResources(this
				.getClass().getClassLoader());
		long hogecnt = resources.stream()
				.filter(it -> "hoge.txt".equals(it))
				.count();
		assertEquals(1, hogecnt);
	}

	@Test
	public void testClass() throws IOException {
		System.out.println(System.getProperty("java.class.path"));
		ClassLoader cl = this.getClass().getClassLoader();
		System.out.println(cl);
		System.out.println(new ResourceScanner().getURIs(cl));
		Collection<URI> uris = new ResourceScanner().getURIs(this.getClass()
				.getClassLoader());
		System.out.println("--------");
		uris.stream().forEach(System.out::println);
		System.out.println("--------");
		this.getClass().getTypeName();
		Collection<String> resources = new ResourceScanner().scanResources(cl);
		resources
				.stream()
				.filter(it -> it.startsWith("me"))
				.forEach(it -> System.out.println(it));
		long hogecnt = resources
				.stream()
				.filter(it -> it
						.equals("me/geso/classpathscanner/ClassInfo.class"))
				.count();
		assertEquals(1, hogecnt);
	}
}
