package es.sidelab.code.installer;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.virtualbox_4_1.IMachine;

import es.sidelab.code.virtualbox.VBoxUtils;
import es.sidelab.tools.commandline.CommandLine;

/**
 * Tests the installer of the SidelabCode Stack.
 * At the beginning of the test suite, it starts the VM.
 * When the tests end, it shuts it down. 
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class InstallerTest {
	static VBoxUtils env = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		env = new VBoxUtils();
		if (!env.connectToVBoxServer())
			fail("Unable to connect to VBox webserver");
		String hostIfName = env.getHostOnlyInterface();
		if (null == hostIfName)
			fail("Unable to obtain host-only if for the Host");
		//System.out.println("Interface name to use " + hostIfName);
		IMachine instance = env.getMachine();
		if (null == instance)
			fail("Unable to obtain instance of the guest OS");
		String guestIfName = env.setInstanceIfByName(instance, hostIfName);
		if (null == guestIfName)
			fail("Unable to set host-only if for the Guest");
		if (! env.startInstance(instance))
			fail("Unable to start guest instance");
		CommandLine console = new CommandLine();
		String sshCmd = "ssh -o BatchMode=yes -o StrictHostKeyChecking=no " + 
				env.getRemoteUser() + "@" + env.getRemoteIP();
		//String sshCmd = "ssh laforge@sidelabvm";
		String cmds = sshCmd + " pwd;ls;mkdir test;cd test;touch b;echo 'testing'>b;ls;echo 'a:';cat a;echo 'b:';cat b";
		String pwdCmd = sshCmd + " pwd";
		if (VBoxUtils.tryCmds(console, pwdCmd, 15, 5)) {//ok
			VBoxUtils.runCmd(console, cmds);
		}
		else
			fail("Could not connect to the guest machine!");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		IMachine instance = env.getMachine();
		if (null == instance)
			fail("Unable to obtain instance of the running OS");
		if (!env.shutdownInstance(instance))
			fail("Shutting down the guest OS has failed");
		env.disconnectVBoxManager();
	}

	@Test
	public void testInstallation() {
		fail("Not yet implemented");
	}

}
