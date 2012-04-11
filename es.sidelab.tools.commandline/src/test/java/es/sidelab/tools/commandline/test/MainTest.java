package es.sidelab.tools.commandline.test;

public class MainTest {

	public static final String STD_OUTPUT = "Writing on the standard output\n";
	public static final String ERR_OUTPUT = "Writing on the error output\n";
	
	public static void main(String[] args) {
		
		System.out.println("Writing on the standard output");
		
		System.err.println("Writing on the error output");

	}
}