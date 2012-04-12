package es.sidelab.code.virtualbox;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.virtualbox_4_1.IMachine;
import org.virtualbox_4_1.IMedium;
import org.virtualbox_4_1.IProgress;
import org.virtualbox_4_1.IVirtualBox;
import org.virtualbox_4_1.IVirtualBoxErrorInfo;
import org.virtualbox_4_1.MachineState;
import org.virtualbox_4_1.VBoxException;
import org.virtualbox_4_1.VirtualBoxManager;

import es.sidelab.tools.commandline.CommandLine;
import es.sidelab.tools.commandline.ExecutionCommandException;

/**
 * Class that sets up the environment required for deployment and testing.
 * Can start and stop a headless VirtualBox instance. 
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class Environment {

	final static Logger LOG = Logger.getLogger(Environment.class.getName());

	private String port = "18083";
	private String host = "http://localhost";
	private final static String separator = ":";
	private String user = null;
	private String passwd = null;
	private String vmname = "SCStackImage[final]";

	private VirtualBoxManager mgr = null;
	private IVirtualBox vbox = null;

	public Environment() {}

	/**
	 * Constructor where each param can be null or empty (which case, default values will be used).
	 * @param host the hostname where the VBox webserver is running. (default: http://localhost)
	 * @param port the port (default: 18083)
	 * @param user (default: null)
	 * @param passwd (default: null)
	 * @param vmname (default: SCStackImage[final])
	 */
	public Environment(String host, String port, String user, String passwd, String vmname) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.passwd = passwd;
		if (vmname != null && !vmname.trim().isEmpty())
			this.vmname = vmname;
	}

	/**
	 * Connects to the vbox web server.
	 * @return true if everything went fine, false otherwise
	 */
	public boolean connectToVBoxServer() {
		this.mgr = VirtualBoxManager.createInstance(null);
		try {
			this.mgr.connect(this.host + Environment.separator + this.port, this.user, this.passwd);
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Cannot connect, start webserver first or check auth details!", e);
		}
		try
		{
			this.vbox = this.mgr.getVBox();
			if (this.vbox != null)
			{
				System.out.println("VirtualBox version: " + this.vbox.getVersion());
				System.out.println("VirtualBox API version: " + this.vbox.getAPIVersion());
				String [] version = this.vbox.getVersion().split("\\.",3);
				String [] APIversion = this.vbox.getAPIVersion().split("_");
				if (! version[0].equalsIgnoreCase(APIversion[0]) || 
						( (version.length > 1 && APIversion.length > 1)) && 
						! version[1].equalsIgnoreCase(APIversion[1]) )
					LOG.log(Level.WARNING, 
							"VirtualBox version and VBox API version are not the same." + 
							" This might lead to unexpected behaviour.");
				return true;
			} else {
				System.out.println("Error geting VirtualBox instance!");
			}
		}
		catch (VBoxException e)
		{
			LOG.log(Level.SEVERE, "VBox error:", e);
		}
		return false;
	}

	/**
	 * Enumerates all harddisks and machines.
	 */
	public void enumerateMachines() {
		try {
			List<IMedium> hds = this.vbox.getHardDisks();
			System.out.println("\nHarddisks:");
			for(IMedium hd : hds) {
				System.out.println(hd.getName() + " - " + hd.getId());
				if (null != hd.getChildren()) for (IMedium child : hd.getChildren()) {
					System.out.println("\tchild: " + child.getName() + " - " + child.getId());
				}
			}
		} catch (VBoxException e)
		{
			LOG.log(Level.SEVERE, "VBox error:", e);
		}
		System.out.println("\nIMachines:");
		List<IMachine> machines = this.vbox.getMachines();
		for(IMachine m : machines) {
			System.out.println(m.getName() + " - " + m.getState() + " - " + m.getId());
		}
	}
	/**
	 * Disconnects from server and releases all managed objects. 
	 */
	public void disconnectVBoxManager() {
		try {
			this.mgr.disconnect();
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error disconnecting from webserver!", e);
		}
		this.mgr.cleanup();
	}
	/**
	 * Tries to find the VM specified by the vmname parameter.
	 * @return a IMachine if found, null otherwise.
	 */
	public IMachine getMachine() {
		try {
			return this.vbox.findMachine(vmname);
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error finding machine '" + vmname + "'", e);
		}
		return null;
	}

	/**
	 * Starts the given machine with a 'headless' session type.
	 * @param instance
	 * @return true if the action was successful, false otherwise
	 */
	public boolean startInstance(IMachine instance) {
		IProgress progress = null;
		try {
			progress = instance.launchVMProcess(this.mgr.getSessionObject(), "headless", "");
		} catch(VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error launching VM '" + this.vmname + "'", e);
		}
		if (null != progress) {
			int count = 0;
			int top = 10;
			while (count < top) {
				progress.waitForCompletion(10000);
				if (progress.getCompleted()) {
					if (progress.getResultCode() == 0) {
						System.out.println(instance.getName() + " started.");
						return true;
					}
					else {
						IVirtualBoxErrorInfo errorInfo = progress.getErrorInfo();
						while (null != errorInfo) {
							System.out.println("Progress error: " +
									progress.getErrorInfo().getText());
							errorInfo = errorInfo.getNext();
						}
						System.out.println(instance.getName() + " could not be started.");
					}
					count = top;
				}
				else {
					System.out.println("Progress: " + progress.getPercent() + "%");
					count ++;
				}
			}
		}
		return false;
	}

	/**
	 * Check if the machine has started successfully.
	 * @param instance 
	 * @return true if the machine is in a RUNNING state after a (reasonable) time, false otherwise
	 */
	public boolean instanceHasStarted(IMachine instance) {
		if (instance.getState().compareTo(MachineState.Starting) == 0)
			System.out.print("Starting ");
		int i = 0;
		while(i < 50 && instance.getState().compareTo(MachineState.Starting) == 0) {
			System.out.print(".");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE,
						"Interrupted while waiting", e);
			}
			i++;
		}
		System.out.println();
		if (i == 50)
			System.out.println("VM still in STARTING state after " + ((50 * 500)/1000) + " seconds!");
		else 
			if (instance.getState().compareTo(MachineState.Running) == 0) {
				System.out.println("VM in running state.");
				return true;
			} else 
				System.out.println("VM is in " + instance.getState().toString() + " state!");
		return false;
	}

	public void shutdownInstance(IMachine instance) {
		IProgress progress = null;
		try {
			progress = this.mgr.getSessionObject().getConsole().powerDown();
		} catch(VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error powering off the VM '" + this.vmname + "'", e);
		}
		if (null != progress) {
			int count = 0;
			int top = 10;
			while (count < top) {
				progress.waitForCompletion(10000);
				if (progress.getCompleted()) {
					if (progress.getResultCode() == 0) {
						System.out.println(instance.getName() + " has shut down.");
					}
					else {
						IVirtualBoxErrorInfo errorInfo = progress.getErrorInfo();
						while (null != errorInfo) {
							System.out.println("Progress error: " +
									progress.getErrorInfo().getText());
							errorInfo = errorInfo.getNext();
						}
						System.out.println(instance.getName() + " could not be shut down.");
					}
					count = top;
				}
				else {
					System.out.println("Progress: " + progress.getPercent() + "%");
					count ++;
				}
			}
		}
	}

	/**
	 * Runs a specified command.
	 * @param console
	 * @param cmd
	 * @return true if no error were found, false otherwise
	 */
	public static boolean runCmd(CommandLine console, String cmd) {
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
	public static boolean tryCmds(CommandLine console, String cmd, int nSeconds, int nTries) {
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
			if (Environment.runCmd(console, cmd))
				return true;
		}
		return false;
	}
	
	public static void main(String[] args) {
		Environment env = new Environment();
		if (env.connectToVBoxServer()) {
			IMachine instance = env.getMachine();
			if (env.startInstance(instance)) {
				CommandLine console = new CommandLine();
				String sshCmd = "ssh -o BatchMode=yes -o StrictHostKeyChecking=no laforge@sidelabvm";
				//String sshCmd = "ssh laforge@sidelabvm";
				String cmds = sshCmd + " pwd;ls;mkdir test;cd test;touch b;echo 'testing'>b;ls;echo 'a:';cat a;echo 'b:';cat b";
				String pwdCmd = sshCmd + " pwd";
				if (Environment.tryCmds(console, pwdCmd, 10, 5)) //ok
					Environment.runCmd(console, cmds);
				else {
					LOG.log(Level.INFO, "Could not connect to the guest machine!");
				}
			}
			env.shutdownInstance(instance);
		}
		env.disconnectVBoxManager();
	}

}
