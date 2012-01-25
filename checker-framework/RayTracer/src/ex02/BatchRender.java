package ex02;

import java.io.File;
import java.io.FilenameFilter;

// Utility class allowing to batch render an entire directory
public class BatchRender {	
	static RayTracer tracer;
	
	public static void renderDirectory(File inputDir) throws Exception {							
		File[] files = inputDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.indexOf(".txt") > -1;
			}			
		});		
		
		for (File file: files) {
			System.out.println("Rendering: " + file.getAbsolutePath());
			Process proc = Runtime.getRuntime().exec("javaw.exe -classpath C:\\work\\ex02\\bin;C:\\work\\ex02\\lib\\swt.jar ex02.RayTracer " + file.getAbsolutePath());
			proc.waitFor();
		}
	}
	
	public static void main(String[] args) throws Exception {
		File inputDir = new File(args[0]);						
		
		renderDirectory(inputDir);		
	}

}
