package es.sidelab.tools.commandline;

public class ExecutionCommandException extends Exception {
	
	private String standardOutput;
	private String errorOutput;
	private int exitCode;

	public ExecutionCommandException(String message, int exitCode, String std, String err) {
		super(message);
		this.exitCode = exitCode;
		this.standardOutput = std;
		this.errorOutput = err;
	}

	public String getErrorOutput() {
		return errorOutput;
	}
	
	public String getStandardOutput() {
		return standardOutput;
	}
	
	public int getExitCode() {
		return exitCode;
	}
	
}
