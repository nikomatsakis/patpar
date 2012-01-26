package checker.patpar;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public class ParpatSuite extends Suite {

	// //////////////////////////////
	// Public helper interfaces

	/**
	 * Annotation for a method which returns a {@link Configuration} to be
	 * injected into the test class constructor
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Config {
	}

	public static interface Configuration {
		int size();

		Object getTestValue(int index);

		String getTestName(int index);
	}

	// //////////////////////////////
	// Fields

	private final List<Runner> runners;

	// //////////////////////////////
	// Constructor

	/**
	 * Only called reflectively. Do not use programmatically.
	 * 
	 * @param c
	 *            the test class
	 * @throws Throwable
	 *             if something bad happens
	 */
	public ParpatSuite(Class<?> c) throws Throwable {
		super(c, Collections.<Runner> emptyList());
		TestClass testClass = getTestClass();
		Class<?> jTestClass = testClass.getJavaClass();
		TestDirectories directories = c.getAnnotation(TestDirectories.class);
		List<SourceFile> sourceFiles = data(directories.value());
		List<Runner> runners = new ArrayList<Runner>();
		for (int i = 0, size = sourceFiles.size(); i < size; i++) {
			SourceFile sourceFile = sourceFiles.get(i);
			SingleRunner runner = new SingleRunner(jTestClass, sourceFile, sourceFile.file.getName());
			runners.add(runner);
		}
		this.runners = runners;
	}

	// //////////////////////////////
	// Overrides

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	// //////////////////////////////
	// Gather the list of test files:

	class SourceFile {
		public final File sourceDirectory;
		public final File file;

		public SourceFile(File sourceDirectory, File file) {
			super();
			this.sourceDirectory = sourceDirectory;
			this.file = file;
		}
	}

	public List<SourceFile> data(String[] directories) {
		List<SourceFile> args = new ArrayList<SourceFile>();
		for (String directory : directories) {
			addSourceFiles(new File(directory), args);
		}
		return args;
	}

	private void addSourceFiles(File sourceDir, List<SourceFile> args) {
		List<File> files = new ArrayList<File>();
		addJavaFiles(files, sourceDir);

		for (File f : files) {
			args.add(new SourceFile(sourceDir, f));
		}
	}

	private FilenameFilter filter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".java");
		}
	};

	private void addJavaFiles(List<File> list, File dir) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory())
				addJavaFiles(list, f);
			else if (filter.accept(dir, f.getName()))
				list.add(f);
		}
	}

	// //////////////////////////////
	// Helper classes
	
	private static class SingleRunner extends BlockJUnit4ClassRunner {

		private final SourceFile sourceFile;
		private final String testName;

		SingleRunner(Class<?> testClass, SourceFile sourceFile, String testName) throws InitializationError {
			super(testClass);
			this.sourceFile = sourceFile;
			this.testName = testName;
		}

		@Override
		protected Object createTest() throws Exception {
			return getTestClass().getOnlyConstructor().newInstance(sourceFile.sourceDirectory, sourceFile.file);
		}

		@Override
		protected String getName() {
			return testName;
		}

		@Override
		protected String testName(FrameworkMethod method) {
			return testName + ": " + method.getName();
		}

		@Override
		protected void validateConstructor(List<Throwable> errors) {
			validateOnlyOneConstructor(errors);
		}

		@Override
		protected Statement classBlock(RunNotifier notifier) {
			return childrenInvoker(notifier);
		}
	}
}
