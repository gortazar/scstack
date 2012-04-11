package es.sidelab.code.virtualbox;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.virtualbox_4_1.IMachine;
import org.virtualbox_4_1.IMedium;
import org.virtualbox_4_1.IProgress;
import org.virtualbox_4_1.IVirtualBox;
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
		System.out.println("\nHarddisks:");
		List<IMedium> hds = vbox.getHardDisks();
    	for(IMedium hd : hds) {
    		System.out.println(hd.getName() + " - " + hd.getId());
    		if (null != hd.getChildren()) for (IMedium child : hd.getChildren()) {
    			System.out.println("\tchild: " + child.getName() + " - " + child.getId());
    		}
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
	private static void disconnectVBox() {
		try {
			mgr.disconnect();
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error disconnecting from webserver!", e);
		}
	}
	private static IMachine getMachine() {
		try {
			return vbox.findMachine(vmname);
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error finding machine '" + vmname + "'", e);
		}
		return null;
	}
	
	private static void startInstance(IMachine instance) {
		IProgress progress = instance.launchVMProcess(mgr.getSessionObject(), "headless", null);
		progress.waitForCompletion(-1);
		System.out.println(instance.getName() + " started.");
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		connectToVBoxServer();
		enumerateMachines();
		IMachine instance = getMachine();
		startInstance(instance);
		disconnectVBox();
	}
}
