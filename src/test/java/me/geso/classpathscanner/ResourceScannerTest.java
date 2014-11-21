package me.geso.classpathscanner;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
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
}
