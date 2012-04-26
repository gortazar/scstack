package es.sidelab.code.virtualbox;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.sidelab.tools.commandline.CommandLine;
import es.sidelab.tools.commandline.ExecutionCommandException;

/**
 * Utility singleton class for running console commands, especially in the VirtualBox environment.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public enum CommandsUtils {
	INSTANCE;
	
	final static Logger LOG = Logger.getLogger(CommandsUtils.class.getName());
	private final static CommandLine console = new CommandLine();
	
	/**
	 * Runs a specified command.
	 * @param console
	 * @param cmd
	 * @return true if no error were found, false otherwise
	 */
	public static boolean runCmd(String cmd) {
		System.out.println("Executing: " + cmd);
		try {
			console.syncExec(cmd);
			return true;
		} catch (IOException e) {
			LOG.log(Level.INFO,
					"IOException:", e.getMessage());
		} catch (ExecutionCommandException e) {
			LOG.log(Level.INFO,
					"ExecutionCommandException:\nExit code: " + e.getExitCode() +
					"\nStandard output: " + e.getStandardOutput() + 
					"\nError output: " + e.getErrorOutput());
		}
		return false;
	}

	/**
	 * Run a command for <b>nTries</b> iterations, sleeping <b>nSeconds</b> before each try.
	 * The first iteration, it will wait the double amount of seconds.
	 * If the command executes without error before the specified number of iterations has been
	 * reached, returns successfully. Otherwise, it returns with a <b>false</b> value.
	 * @param console a CommandLine object
	 * @param cmd the command; as it's used for testing the state of a virtual machine, 
	 * it should be a simple command. Example:
	 * <br/>&nbsp;&nbsp;&nbsp;&nbsp;{@code ssh user@quest-machine pwd}  
	 * @param nSeconds how many seconds between each try (double amount for the first time)
	 * @param nTries number of iterations
	 * @return true if the command could be executed, false otherwise
	 */
	public static boolean tryCmds(String cmd, int nSeconds, int nTries) {
		int time = nSeconds * 2;
		for (int i = 0; i < nTries; i++) {
			System.out.println("Waiting " + time + " secs before trying to execute ssh...");
			try {
				Thread.sleep(time * 1000);
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE,
						"Interrupted while waiting", e);
			}
			if (time == nSeconds * 2)
				time = nSeconds;
			if (CommandsUtils.runCmd(cmd))
				return true;
		}
		return false;
	}
}
