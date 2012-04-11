package es.sidelab.code.virtualbox;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.virtualbox_4_1.IEvent;
import org.virtualbox_4_1.IEventListener;
import org.virtualbox_4_1.IEventSource;
import org.virtualbox_4_1.IMachine;
import org.virtualbox_4_1.IMachineStateChangedEvent;
import org.virtualbox_4_1.IMedium;
import org.virtualbox_4_1.IProgress;
import org.virtualbox_4_1.IVirtualBox;
import org.virtualbox_4_1.IVirtualBoxErrorInfo;
import org.virtualbox_4_1.MachineState;
import org.virtualbox_4_1.VBoxEventType;
import org.virtualbox_4_1.VBoxException;
import org.virtualbox_4_1.VirtualBoxManager;

/**
 * Class that sets up the environment required for deployment and testing.
 * Can start and stop a headless VirtualBox instance. 
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class Environment {

	final static Logger LOG = Logger.getLogger(Environment.class.getName());

	private static String port = "18083";
	private static String host = "http://localhost";
	private final static String separator = ":";
	private static String user = null;
	private static String passwd = null;
	private static String vmname = "SCStackImage[final]";

	private static VirtualBoxManager mgr = null;
	private static IVirtualBox vbox = null;

	/**
	 * Connects to the vbox web server.
	 */
	public static void connectToVBoxServer() {
		mgr = VirtualBoxManager.createInstance(null);
		try {
			mgr.connect(host + separator + port, user, passwd);
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Cannot connect, start webserver first or check auth details!", e);
		}
		try
		{
			vbox = mgr.getVBox();
			if (vbox != null)
			{
				System.out.println("VirtualBox version: " + vbox.getVersion());
				System.out.println("VirtualBox API version: " + vbox.getAPIVersion());
			} else {
				System.out.println("Error geting VirtualBox instance!");
			}
		}
		catch (VBoxException e)
		{
			//System.out.println("VBox error: "+e.getMessage()+" original="+e.getWrapped());
			LOG.log(Level.SEVERE, "VBox error:", e);
		}
	}

	private static void enumerateMachines() {
		try {
		List<IMedium> hds = vbox.getHardDisks();
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
		List<IMachine> machines = vbox.getMachines();
		for(IMachine m : machines) {
			System.out.println(m.getName() + " - " + m.getState() + " - " + m.getId());
		}
	}
	/**
	 * Disconnects from server and releases all managed objects. 
	 */
	private static void disconnectVBoxManager() {
		try {
			mgr.disconnect();
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error disconnecting from webserver!", e);
		}
		mgr.cleanup();
	}
	/**
	 * Tries to find the VM specified by the vmname parameter.
	 * @return a IMachine if found, null otherwise.
	 */
	private static IMachine getMachine() {
		try {
			return vbox.findMachine(vmname);
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
	private static boolean startInstance(IMachine instance) {
		IProgress progress = null;
		try {
			progress = instance.launchVMProcess(mgr.getSessionObject(), "headless", "");
		} catch(VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error launching VM '" + vmname + "'", e);
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
//			System.out.println("Progress description: " + progress.getDescription());
//			while (! progress.getCompleted()) {
//				progress.waitForCompletion(1);
//				System.out.println("Progress: " + progress.getPercent() + "%");
//				System.out.println("Op#" + progress.getOperation() + ": " + progress.getOperationDescription());
//			}
		}
		return false;
	}
    
	/**
	 * Check if the machine has started successfully.
	 * @param instance 
	 * @return true if the machine is in a RUNNING state after a (reasonable) time, false otherwise
	 */
	private static boolean instanceHasStarted(IMachine instance) {
		if (instance.getState().compareTo(MachineState.Starting) == 0)
			System.out.print("Starting ");
		int i = 0;
		while(i < 50 && instance.getState().compareTo(MachineState.Starting) == 0) {
			System.out.print(".");
			try {
				Thread.currentThread().wait(500);
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
	/**
	 * 
	 * @param args the arguments should be:
	 * 0 - the host where the webservice server runs (e.g. http://localhost)
	 * 1 - the port (e.g. 18083)
	 * 2 - the user
	 * 3 - the password
	 * 4 - the virtual machine's name (e.g. SCStackImage[final])
	 */
	public static void main(String[] args) {
		connectToVBoxServer();
		if (null != vbox) {
			enumerateMachines();
			IMachine instance = getMachine();
			if (startInstance(instance) && instanceHasStarted(instance)) {
				//TODO now ssh to it
			}
		}

		disconnectVBoxManager();
	}
}
