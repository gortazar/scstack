package es.sidelab.code.virtualbox;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.virtualbox_4_1.Holder;
import org.virtualbox_4_1.HostNetworkInterfaceType;
import org.virtualbox_4_1.IConsole;
import org.virtualbox_4_1.IDHCPServer;
import org.virtualbox_4_1.IHostNetworkInterface;
import org.virtualbox_4_1.IMachine;
import org.virtualbox_4_1.IMedium;
import org.virtualbox_4_1.INetworkAdapter;
import org.virtualbox_4_1.IProgress;
import org.virtualbox_4_1.ISession;
import org.virtualbox_4_1.ISnapshot;
import org.virtualbox_4_1.IVirtualBox;
import org.virtualbox_4_1.IVirtualBoxErrorInfo;
import org.virtualbox_4_1.LockType;
import org.virtualbox_4_1.MachineState;
import org.virtualbox_4_1.NetworkAttachmentType;
import org.virtualbox_4_1.VBoxException;
import org.virtualbox_4_1.VirtualBoxManager;

/**
 * Controls the environment required for deployment and testing.
 * Can start and stop a VirtualBox instance. 
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class VBoxUtils {

	final static Logger LOG = Logger.getLogger(VBoxUtils.class.getName());

	//constants used to initiate the DHCP server that will provide an IP to the guest OS
	/** {@value} */
	public final static String DHCP_HOST_IP = "192.168.56.2";
	/** {@value} */
	public final static String DHCP_GUEST_IP = "192.168.56.99";
	/** {@value} */
	public final static String DHCP_SERVER_IP = "192.168.56.1";

	//	/** {@value} */
	//	public final static String DHCP_HOST_IP = "10.0.10.2";
	//	/** {@value} */
	//	public final static String DHCP_GUEST_IP = "10.0.10.99";
	//	/** {@value} */
	//	public final static String DHCP_SERVER_IP = "10.0.10.1";

	/** {@value} */
	public final static String DHCP_NETMASK = "255.255.255.0";

	public final static int PROGRESS_TIMEOUT = 10000;
	public final static int PROGRESS_TIMES = 10;

	private String port = "18083";
	private String host = "http://localhost";
	private final static String separator = ":";
	private String user = null;
	private String passwd = null;
	private String vmNameOrId = "SCStackImage[final]";

	private String vmname = null;

	private String guestUser = "root";//"laforge";
	private String guestIP = DHCP_GUEST_IP;

	private VirtualBoxManager mgr = null;
	private IVirtualBox vbox = null;
	private ISession session = null;
	private IMachine machine = null;

	/**
	 * Constructor where each param can be null or empty (which case, default values will be used).
	 * @param host the hostname where the VBox webserver is running. (default: http://localhost)
	 * @param port the port (default: 18083)
	 * @param user (default: null)
	 * @param passwd (default: null)
	 * @param vmNameOrId (default: SCStackImage[final])
	 * @param guestUser (default: laforge)
	 * @param guestIP (default: {@link #DHCP_GUEST_IP} 
	 */
	public VBoxUtils(String host, String port, String user, String passwd, String vmNameOrId, String guestUser,
			String guestIP) {
		if (host != null && !host.trim().isEmpty())
			this.host = host;
		if (port != null && !port.trim().isEmpty())
			this.port = port;
		if (user != null && !user.trim().isEmpty())
			this.user = user;
		if (passwd != null && !passwd.trim().isEmpty())
			this.passwd = passwd;
		if (vmNameOrId != null && !vmNameOrId.trim().isEmpty())
			this.vmNameOrId = vmNameOrId;
		if (guestUser != null && !guestUser.trim().isEmpty())
			this.guestUser = guestUser;
		if (guestIP != null && !guestIP.trim().isEmpty())
			this.guestIP = guestIP;
	}

	public VBoxUtils() {}

	/**
	 * @return the user name that has login access on the guest OS 
	 */
	public String getRemoteUser() {
		return this.guestUser;
	}
	/**
	 * @return the IP of the guest machine
	 */
	public String getRemoteIP() {
		return this.guestIP;
	}
	/**
	 * @return the virtual machine's name
	 */
	public String getVMName() {
		return this.vmname;
	}

	/**
	 * Connects to the vbox web server.
	 * @return true if everything went fine, false otherwise
	 */
	public boolean connectToVBoxServer() {
		this.mgr = VirtualBoxManager.createInstance(null);
		try {
			this.mgr.connect(this.host + VBoxUtils.separator + this.port, this.user, this.passwd);
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
				this.session = this.mgr.getSessionObject();
				System.out.println("Obtained session object: " + this.session.getState().name());
				this.machine = getMachine();
				if (null != this.machine) {
					this.vmname = this.machine.getName();
					System.out.println("Machine with name '" + this.vmname + "' has been found");
					return true;
				} else {
					System.out.println("There is no machine for id/name '" + vmname + "'");
				}
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
	 * Enumerates all the hard disks and machines.
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
			List<IMachine> machines = this.vbox.getMachines();
			System.out.println("\nIMachines:");
			for(IMachine m : machines) {
				System.out.println(m.getName() + " - " + m.getState() + " - " + m.getId());
			}
		} catch (VBoxException e)
		{
			LOG.log(Level.SEVERE, "VBox error:", e);
		}
	}
	/**
	 * Disconnects from server and releases all managed objects. 
	 */
	public void disconnectVBoxManager() {
		try {
			//			if (null != this.session.getState() && 
			//					this.session.getState().value() == SessionState.Locked.value())
			//				this.session.unlockMachine();
			this.mgr.closeMachineSession(this.session);
		} catch (VBoxException e) {
			LOG.log(Level.INFO,
					"Error unlocking session", e);
		}
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
			return this.vbox.findMachine(this.vmNameOrId);
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error finding machine with id/name '" + this.vmNameOrId + "'", e);
		}
		return null;
	}
	/**
	 * Tries to apply a {@link LockType#Write} lock on
	 * the machine for the current VirtualBox session ({@link ISession}).
	 * @return the {@link IConsole} to control the locked machine, null if the lock 
	 * operation could not be performed
	 */
	public IConsole lockMachineForWrite() {
		try {
			this.machine.lockMachine(this.session, LockType.Write);
			return this.session.getConsole();
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error locking machine '" + this.vmname + "'", e);
		}
		return null;
	}
	/**
	 * Using the VirtualBoxManager, it tries to apply a {@link LockType#Shared} lock on
	 * the machine for the current VirtualBox session ({@link ISession}).
	 * @return the {@link IConsole} to control the locked machine, null if the lock operation
	 * could not be completed
	 */
	public IConsole lockMachineForRead() {
		try {
			return this.mgr.openMachineSession(this.machine).getConsole();
		} catch (Exception e) {
			LOG.log(Level.SEVERE,
					"Error opening Shared session for machine '" + this.vmname + "'", e);
		}
		return null;
	}
	/**
	 * Unlocks the machine.
	 */
	public void unlockMachine() {
		try {
			this.session.unlockMachine();
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error unlocking machine '" + this.vmname + "'", e);
		}
	}
	
	/**
	 * Undoes any changes made to the machine since the current snapshot was taken 
	 * (or since the machine's state was reverted to that of the current snapshot). 
	 * It actually restores the 
	 * machine to the state it was in when the snapshot was taken.
	 * @return true if successful, false otherwise.
	 */
	public boolean restoreCurrentSnapshot() {
		try {
			ISnapshot snapshot = this.machine.getCurrentSnapshot();
			if (null == snapshot) {
				System.out.println("The machine " + this.vmname + " has no snapshots.");
				return false;
			}
			IConsole console = this.lockMachineForWrite();
			IProgress progress = console.restoreSnapshot(snapshot);
			if (!checkProgress(progress, PROGRESS_TIMEOUT, PROGRESS_TIMES))
				System.out.println("The snapshot might not have been restored successfully.");
			this.unlockMachine();
			System.out.println("Machine '" + this.vmname + "' has been restored successfully!");
		} catch(VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error restoring current snapshot for '" + this.vmname + "'", e);
		}
		return false;
	}

	/**
	 * Prints information on the current snapshot of the given machine:<br/>
	 * name, description, if current state is different from the snapshot's and
	 * the snapshot's timestamp.
	 */
	public void currentSnapshotInfo() {
		try {
			ISnapshot snap = this.machine.getCurrentSnapshot();
			if (null == snap) {
				System.out.println("The machine " + this.vmname + " has no snapshots.");
				return;
			}
			System.out.println("Current snapshot for " + 
					this.vmname + ": " + snap.getName() + 
					" - [desc]: " + snap.getDescription());
			if (this.machine.getCurrentStateModified())
				System.out.println("Machine's current state is different from the one stored in the current snapshot.");
			else
				System.out.println("Machine's current state is exactly the same as the one stored in the current snapshot.");
			Date time=new Date((long)snap.getTimeStamp());
			System.out.println("Snapshot was taken on " + time);
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error getting snapshot info for '" + this.vmname + "'", e);
		}
	}

	/**
	 * Starts the given machine with a 'headless' session type.
	 * @return true if the action was successful, false otherwise
	 */
	public boolean startHeadlessInstance() {
		try {
			return this.mgr.startVm(this.vmname, "headless", PROGRESS_TIMEOUT);
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error starting '" + this.vmname + "'", e);
		}
		return false;
	}

	/**
	 * Starts the given machine with a 'gui' session type.
	 * @return true if the action was successful, false otherwise
	 */
	public boolean startInstance() {
		try {
			return this.mgr.startVm(this.vmname, null, PROGRESS_TIMEOUT);
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error starting '" + this.vmname + "'", e);
		}
		return false;
	}

	/**
	 * Assures that there is at least one Host-Only network
	 * adapter on the host with DHCP enabled.
	 * If no adapter of this type is found, will try to create one.
	 * If null is returned, will be impossible to communicate with the guest OS. 
	 * @return the name of the host-only interface, null if it couldn't be created
	 */
	public String getHostOnlyInterface() {
		//get all the host-only interfaces on the host
		List<IHostNetworkInterface> hostOnlyIfs = this.vbox.getHost().
				findHostNetworkInterfacesOfType(HostNetworkInterfaceType.HostOnly);
		Holder<IHostNetworkInterface> hostIfHolder = new Holder<IHostNetworkInterface>();
		IHostNetworkInterface hostIf = null;
		String ifname = null;
		if (null == hostOnlyIfs || hostOnlyIfs.isEmpty()) {
			System.out.println("No Host-Only interface found on host. Trying to create one...");
			IProgress progress = null;
			try {
				progress = this.vbox.getHost().createHostOnlyNetworkInterface(hostIfHolder);
			} catch (VBoxException e) {
				LOG.log(Level.SEVERE,
						"Error creating host-only interface", e);
				return null;
			}
			if (null != progress) {
				progress.waitForCompletion(10000);
				if (progress.getCompleted() && progress.getResultCode() == 0) {
					System.out.println("Host-Only interface created.");
					System.out.println("Network name: " + hostIfHolder.value.getNetworkName());
					System.out.println("Interface name: " + hostIfHolder.value.getName());
					hostIf = hostIfHolder.value;
					ifname = hostIf.getName();
				}
			}
		} else {
			System.out.println(hostOnlyIfs.size() + " Host-Only interfaces found.");
			hostIf = hostOnlyIfs.get(0);
			System.out.println("Host-Only #0 network name: " + hostIf.getNetworkName());
			System.out.println("Host-Only #0 interface name: " + hostIf.getName());
			ifname = hostIf.getName();
		}
		if (null == hostIf) {
			System.out.println("No Host-Only interface will be available so communication to guest OS will not be possible");
		} else {
			//enable DCHP server for the host-only if
			IDHCPServer dhcp = null;
			String netname = hostIf.getNetworkName();
			try {
				dhcp = vbox.findDHCPServerByNetworkName(netname);
			} catch (VBoxException e) {
				LOG.log(Level.INFO,
						"Error searching DHCP server with network name '" + netname + "'", e);
			}
			if (dhcp == null) {
				System.out.println("No DHCP server for '" + netname + "'");
				try {
					dhcp = vbox.createDHCPServer(netname);
					System.out.println("DHCP Name: " + dhcp.getNetworkName() + " - IP: " 
							+ dhcp.getIPAddress());
				} catch (VBoxException e) {
					LOG.log(Level.INFO,
							"Could not create DHCP server for netname '" + netname + "'", e);
					return null;
				}
			}
			//we want to assign identical upper- and lower- IPs, that we'll be using to access the guest OS
			try {
				dhcp.setConfiguration(VBoxUtils.DHCP_SERVER_IP, VBoxUtils.DHCP_NETMASK, 
						VBoxUtils.DHCP_GUEST_IP, VBoxUtils.DHCP_GUEST_IP);
				dhcp.setEnabled(true);

				hostIf.enableStaticIpConfig(DHCP_HOST_IP, DHCP_NETMASK);
				if (! hostIf.getIPAddress().contentEquals(DHCP_HOST_IP)) {
					//should wait a bit for the change to take effect
					System.out.println("Detected an IP change for the host's adapter:");
					System.out.println("From " + hostIf.getIPAddress() + " (old) to " + 
							DHCP_HOST_IP + " (new).");
					System.out.println("Its status is " + hostIf.getStatus());
					hostIf.enableStaticIpConfig(DHCP_HOST_IP, DHCP_NETMASK);
					long wait_period = 60;
					System.out.println("\twaiting " + wait_period +
							" seconds before checking if the change has been applied.");
					try {
						Thread.sleep(wait_period * 1000);
					} catch (InterruptedException e) {
						LOG.log(Level.SEVERE,
								"Error during sleep, will not continue.", e);
						return null;
					}
					hostIf.enableStaticIpConfig(DHCP_HOST_IP, DHCP_NETMASK);
					if (! hostIf.getIPAddress().contentEquals(DHCP_HOST_IP)) {
						System.out.println("Changes were not applied, communication will not be possible!");
						return null;
					}
					System.out.println("Changes applied successfully: new host IP is " + DHCP_HOST_IP);
				}
				System.out.println("New IP for host-only " + ifname + ": " + hostIf.getIPAddress());
				System.out.println("Status: " + hostIf.getStatus().name());
			} catch (VBoxException e) {
				LOG.log(Level.SEVERE,
						"Error setting DHCP server for '" + ifname + "'", e);
				return null;
			}
		}
		return ifname;
	}


	/**
	 * It will set the second network interface card (with ID 1), or nic2, 
	 * to be of Host-Only type and have the specified interface name (on startup
	 * it will receive the IP from the corresponding DHCP server).
	 * <p>Internals:<br/>
	 * For that, it will acquire a new machine lock from the current session
	 * and apply the changes on a copy of the original machine (because the original is not
	 * mutable). Finally, it will release the lock.
	 * </p>
	 * If the machine it is in a {@link MachineState#Saved} then it will not be
	 * modified.
	 * @param ifName
	 * @return true if the interface was set correctly or if the machine is in a 
	 * Saved state, false otherwise
	 */
	public boolean setMachineHostInterfaceByName(String ifName) {
		try {
			IConsole console = this.lockMachineForWrite();
			if (null == console)
				return false;
			if (0 == console.getState().compareTo(MachineState.Saved)) {
				System.out.println("Machine '" + this.vmname + "' is in a Saved state. " +
						"No change will be done to its network interface(s).");
				unlockMachine();
				return true;
			}
			IMachine mutableVM = this.session.getMachine();
			INetworkAdapter net = mutableVM.getNetworkAdapter((long)1);
			net.setAttachmentType(NetworkAttachmentType.Null);
			System.out.println("Guest's adapter set to " + net.getAttachmentType());
			net.setAttachmentType(NetworkAttachmentType.HostOnly);
			System.out.println("Guest's adapter set to " + net.getAttachmentType());
			net.setHostOnlyInterface(ifName);
			net.setEnabled(true);
			mutableVM.saveSettings();
			unlockMachine();
			System.out.println("Guest adapter type: " + 
					machine.getNetworkAdapter((long)1).getAttachmentType() + " - name: " + 
					machine.getNetworkAdapter((long)1).getHostOnlyInterface() + 
					" - Enabled: " + machine.getNetworkAdapter((long)1).getEnabled());
			return true;
		} catch(VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error setting host-only adapter for guest", e);
		}
		return false;
	}

	/**
	 * Checks the {@link IProgress}.
	 * @param progress
	 * @param timeout the period (in milliseconds) to wait for completion
	 * @param times how many times to wait for completion
	 * @return true if the progress is found completed, false otherwise
	 */
	public boolean checkProgress(IProgress progress, int timeout, int times) {
		int count = 0;
		while (count < times) {
			progress.waitForCompletion(timeout);
			if (progress.getCompleted()) {
				if (progress.getResultCode() == 0) {
					return true;
				} else {
					IVirtualBoxErrorInfo errorInfo = progress.getErrorInfo();
					while (null != errorInfo) {
						System.out.println("Progress error: " + progress.getErrorInfo().getText());
						errorInfo = errorInfo.getNext();
					}
					count = times;
				}
			} else {
				System.out.println("Progress: " + progress.getPercent() + "%");
				count ++;
			}
		}
		return false;
	}

	/**
	 * Powers off the virtual machine.
	 * @return true if the guest OS could be shut down correctly, false otherwise
	 */
	public boolean shutdownInstance() {
		IProgress progress = null;
		try {
			progress = this.lockMachineForRead().powerDown();
		} catch(VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error powering off the VM '" + this.vmname + "'", e);
		}
		if (null == progress || !checkProgress(progress, PROGRESS_TIMEOUT, PROGRESS_TIMES)) {
			System.out.println("Machine '" + this.vmname + "' could not be stopped.");
			return false;
		} else {
			System.out.println("Machine '" + this.vmname + "' has been powered down.");
			return true;
		}
	}
	/**
	 * Stops the machine by saving its state and freezing it.
	 * @return true if the guest OS could be shut down correctly, false otherwise
	 */
	public boolean saveMachineState() {
		IProgress progress = null;
		try {
			progress = this.lockMachineForRead().saveState();
		} catch (VBoxException e) {
			LOG.log(Level.SEVERE,
					"Error saving state for '" + vmname + "'", e);
		}
		if (null == progress || !checkProgress(progress, PROGRESS_TIMEOUT, PROGRESS_TIMES)) {
			System.out.println("Machine '" + this.vmname + "' could not be stopped.");
			return false;
		} else {
			System.out.println("Machine '" + this.vmname + "' has been stopped.");
			return true;
		}
	}

	/**
	 * Closes the machine by saving the current state.
	 * If unable to perform this action, it tries to shut it down completely (powers it down).
	 */
	public void closeMachine() {
		System.out.println("Saving state...");
		if (! this.saveMachineState()) {
			System.out.println("Unable to save the state! Powering down..");
			if (this.shutdownInstance())
				System.out.println("Machine has been stopped.");
		} else {
			System.out.println("Done. The machine has been stopped and it will continue from the saved state the next time it's started.");
		}
	}
}
