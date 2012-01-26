package checker.patpar;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ParpatSuite.class)
@TestDirectories({"examples"})
public class TestExamples 
extends RunTestBase
{

	public TestExamples(File sourceDir, File javaFile) {
		super(sourceDir, javaFile, "");
	}
	
	@Test public void test() throws IOException {
		super.test();
	}

}
