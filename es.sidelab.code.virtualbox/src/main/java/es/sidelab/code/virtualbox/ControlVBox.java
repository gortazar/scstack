package es.sidelab.code.virtualbox;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum ControlVBox {
	INSTANCE;
	
	final static Logger LOG = Logger.getLogger(ControlVBox.class.getName());
	final static VBoxUtils envRoot = new VBoxUtils(null,null,null,null,null,"root",null);
	final static String sshBaseCmd = "ssh -o BatchMode=yes -o StrictHostKeyChecking=no ";
	
	private static VBoxUtils currentEnv = envRoot;
	
	public static void setVBoxEnvironment(VBoxUtils altEnv) {
		currentEnv = (altEnv != null) ? altEnv : currentEnv;
	}
	
	public static VBoxUtils getCurrentVBoxEnv() {
		return currentEnv;
	}
	
	public static String getSSHCmd() {
		return sshBaseCmd + currentEnv.getRemoteUser() + "@" + currentEnv.getRemoteIP();
	}
	
	public static String getSCPCmd(String local, String remote, String options) {
		if (null != options && !options.trim().isEmpty())
			return "scp -" + options + " " + local + " " +
					currentEnv.getRemoteUser() + "@" +
					currentEnv.getRemoteIP() + ":" + 
					remote;
		else return "scp " + local + " " +
				currentEnv.getRemoteUser() + "@" +
				currentEnv.getRemoteIP() + ":" + 
				remote;
	}
	
	public static String getRsyncCmd(String local, String remote, String options) {
		if (null != options && !options.trim().isEmpty())
			return "rsync -" + options + " " + local + " " +
					currentEnv.getRemoteUser() + "@" +
					currentEnv.getRemoteIP() + ":" + 
					remote;
		else return "rsync " + local + " " +
				currentEnv.getRemoteUser() + "@" +
				currentEnv.getRemoteIP() + ":" + 
				remote;
	}
	
	/**
	 * 
	 * @param altEnv  
	 * @return
	 */
	public static boolean startVMAndTestConn() {
		if (currentEnv.connectToVBoxServer()) {
			String hostIfName = currentEnv.getHostOnlyInterface();
			if (null != hostIfName && currentEnv.setMachineHostInterfaceByName(hostIfName)) {
				if (currentEnv.startHeadlessInstance()) {
					String pwdCmd = getSSHCmd() + " pwd";
					if (CommandsUtils.tryCmds(pwdCmd, 15, 5)) {
						String lsCmd = getSSHCmd() + " ls -als";
						CommandsUtils.runCmd(lsCmd);
						return true;
					}
					else {
						LOG.log(Level.INFO, "Could not connect to the guest machine!");
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param altEnv  
	 * @return
	 */
	public static boolean restoreCurrentSnapshotThenStartVMAndTestConn() {
		if (currentEnv.connectToVBoxServer()) {
			String hostIfName = currentEnv.getHostOnlyInterface();
			if (null != hostIfName) {
				currentEnv.currentSnapshotInfo();
				if (!currentEnv.restoreCurrentSnapshot() 
						&& currentEnv.setMachineHostInterfaceByName(hostIfName) 
						&& currentEnv.startHeadlessInstance()) {
					String pwdCmd = getSSHCmd() + " pwd";
					if (CommandsUtils.tryCmds(pwdCmd, 15, 5)) {
						String lsCmd = getSSHCmd() + " ls -als";
						CommandsUtils.runCmd(lsCmd);
						return true;
					}
					else {
						LOG.log(Level.INFO, "Could not connect to the guest machine!");
					}
				}
			} else {
				LOG.log(Level.INFO, "Could not establish the host network interface!");
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param altEnv
	 */
	public static void saveVMStateAndDisconnect() {
		currentEnv.closeMachine();
		currentEnv.disconnectVBoxManager();
	}
	
	public static void main(String[] args) {
		if (ControlVBox.startVMAndTestConn()) {
			System.out.println("Connection achived! Now stopping it...");
			ControlVBox.saveVMStateAndDisconnect();
		}
	}
}
