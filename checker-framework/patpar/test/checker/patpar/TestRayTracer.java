package checker.patpar;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ParpatSuite.class)
@TestDirectories({"../RayTracer/src"})
public class TestRayTracer 
extends RunTestBase
{

	public TestRayTracer(File sourceDir, File javaFile) {
		super(sourceDir, javaFile, "../RayTracer/lib/Jama-1.0.2.jar");
	}
	
	@Test public void test() throws IOException {
		super.test();
	}

}
