package checker.patpar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Assert;

import checkers.javari.JavariChecker;
import checkers.patpar.PatparChecker;

public class RunTestBase {
	private final String binDir = "bin-test";
	private final String classpath = "bin:" // + "lib/pcollections-1.0.0.jar:"
			+ "lib/asmx.jar:" + "lib/javaparser.jar:"
//			+ "jsr308-langtools/binary/javac.jar:" + "jsr308-langtools/binary/javap.jar:"
			+ "lib/jna.jar";
	private final String bclasspath = "lib/jsr308-all.jar"; //:lib/jdk.jar";

	public final File sourceDir;
	public final File javaFile;
	public final String cp;

	public RunTestBase(File sourceDir, File javaFile, String cp) {
		super();
		this.sourceDir = sourceDir;
		this.javaFile = javaFile;
		this.cp = cp;
		
		System.err.printf("RunTestBase javaFile=%s sourceDir=%s\n", sourceDir, javaFile);
	}

	public void test() throws IOException {
		JavaCompiler comp = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diag = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = comp.getStandardFileManager(diag, null, null);
		Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjects(javaFile);
		List<String> options = new ArrayList<String>();
		addCompileOptions(options);
		CompilationTask task = comp.getTask(null, fileManager, diag, options, null, compUnits);
		boolean success = task.call().booleanValue();
		List<ErrorTemplate> errors = errorTemplates(compUnits);
		System.err.printf("options=%s errors=%d diags=%d\n", options, errors.size(), diag.getDiagnostics().size());
		compareErrors(success, errors, diag);
	}

	private void compareErrors(boolean success, List<ErrorTemplate> errors, DiagnosticCollector<JavaFileObject> diag) {
		List<String> failures = new ArrayList<String>();

		boolean expectedSuccess = (errors.size() == 0);
		if (expectedSuccess != success) {
			failures.add(String.format("Expected success %s but got %s", expectedSuccess, success));
		}

		LinkedList<ErrorTemplate> unreported = new LinkedList<RunTestBase.ErrorTemplate>(errors);
		diagLoop: for (Diagnostic<? extends JavaFileObject> d : diag.getDiagnostics()) {
			ListIterator<ErrorTemplate> et = unreported.listIterator();
			while (et.hasNext()) {
				ErrorTemplate e = et.next();
				if (e.matches(d)) {
					et.remove();
					continue diagLoop;
				}
			}

			JavaFileObject src = d.getSource();
			String name = (src == null ? "?" : src.getName());
			failures.add(String.format("Unexpected error at %s:%s: %s", name, d.getLineNumber(), d.getMessage(null)));
		}

		for (ErrorTemplate et : unreported) {
			failures.add(String.format("Unreported error at %s:%s: %s", et.file.getName(), et.line, et.message));
		}

		for (String failure : failures) {
			System.err.println(failure);
		}

		Assert.assertArrayEquals(new Object[] {}, failures.toArray());
	}

	class ErrorTemplate {
		public final JavaFileObject file;
		public final int line;
		public final String message;
		public final Pattern pattern;

		public ErrorTemplate(JavaFileObject file, int line, String message) {
			super();
			this.file = file;
			this.line = line;
			this.message = message;

			String m = message;
			String quoteChars = "[" + Pattern.quote("{}?+.$()[]|\\^") + "]";
			m = m.replaceAll(quoteChars, "\\\\$0");
			m = m.replaceAll("\\*", ".*");
			m = String.format("^%s$", m.trim());
			this.pattern = Pattern.compile(m, Pattern.DOTALL);
		}

		public boolean matches(Diagnostic<? extends JavaFileObject> d) {
			if (!file.equals(d.getSource()))
				return false;
			if (d.getLineNumber() != line)
				return false;
			return messageMatches(d.getMessage(null));
		}

		public boolean messageMatches(String actualMessage) {
			return pattern.matcher(actualMessage.trim()).matches();
		}
	}

	private List<ErrorTemplate> errorTemplates(Iterable<? extends JavaFileObject> files) throws IOException {
		Pattern errorPattern = Pattern.compile("// (\\^*)ERROR (.*)");
		List<ErrorTemplate> templates = new ArrayList<ErrorTemplate>();
		for (JavaFileObject file : files) {
			String[] lines = file.getCharContent(true).toString().split("\n");
			for (int lineNum = 1; lineNum <= lines.length; lineNum++) {
				String line = lines[lineNum - 1];
				Matcher m = errorPattern.matcher(line);
				if (m.find()) {
					int adjust = m.end(1) - m.start(1);
					String msg = line.substring(m.start(2), m.end(2));
					ErrorTemplate et = new ErrorTemplate(file, lineNum - adjust, msg);
					templates.add(et);
				}
			}
		}
		return templates;
	}

	private void addCompileOptions(List<String> options) {
//		options.add("-nowarn");
//		options.add("-g");
//		options.add("-d");
//		options.add(binDir);
//		options.add("-Xlint:-unchecked");
//		options.add("-Xlint:-deprecation");
		options.add("-classpath");
		options.add(classpath + ":" + cp);
		options.add("-Xbootclasspath/p:" + bclasspath);
		options.add("-sourcepath");
		options.add(sourceDir.getAbsolutePath());
		options.add("-Astubs=stub");
		options.add("-processor");
		options.add(PatparChecker.class.getName());
//		options.add(JavariChecker.class.getName());
		options.add("-implicit:none");
	}
}
