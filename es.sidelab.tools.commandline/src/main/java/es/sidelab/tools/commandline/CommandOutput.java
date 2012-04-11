package es.sidelab.tools.commandline;

public class CommandOutput {

	private String standardOutput;
	private String errorOutput;

	public CommandOutput(String stdOutput, String errOutput) {
		this.standardOutput = stdOutput;
		this.errorOutput = errOutput;
	}
	
	public String getErrorOutput() {
		return errorOutput;
	}
	
	public String getStandardOutput() {
		return standardOutput;
	}
	
}
