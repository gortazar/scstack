package es.sidelab.code.installer;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import es.sidelab.code.virtualbox.ControlVBox;
import es.sidelab.code.virtualbox.CommandsUtils;

/**
 * Integration test for the installer of the SidelabCode Stack.
 * It is run during the "integration-test"
 * At the beginning of the test suite, it starts the VM.
 * When the tests end, it shuts it down. 
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class InstallerIT {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
//		if (ControlVBox.startVMAndTestConn(null))
//			fail("Unable to start testing machine.");
//		
		if (! ControlVBox.restoreCurrentSnapshotThenStartVMAndTestConn())
			fail("Unable to restore or start the testing machine.");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ControlVBox.saveVMStateAndDisconnect();
	}

	@Test
	public void testInstallation() {
		System.out.println("The environment has been set up, ready to perform installation.");
		if (! CommandsUtils.runCmd("pwd"))
			fail("Error running local command: pwd");
		if (! CommandsUtils.runCmd("ls -als"))
			fail("Error running local command: ls -als");
		
		String lsCmd = ControlVBox.getSSHCmd() + " ls -als";
		if (! CommandsUtils.runCmd(lsCmd))
			fail("Lost connection to guest!");
		
		System.out.println("Starting to copy 'ficherosInstalacion' towards the guest machine");
		if (! CommandsUtils.runCmd(
				ControlVBox.getRsyncCmd("ficherosInstalacion/", "ficherosInstalacion", "auh --exclude=.svn")))
			fail("Unable to copy installation files to guest!");
		
		System.out.println("Copying the jar file.");
		if (! CommandsUtils.runCmd(
				ControlVBox.getSCPCmd("target/installer-0.0.1-SNAPSHOT-jar-with-dependencies.jar", ".", "")))
			fail("Unable to copy installer jar file to guest!");
		
		if (! CommandsUtils.runCmd(lsCmd))
			fail("Lost connection to guest!");
		
		if (! CommandsUtils.runCmd(lsCmd + " ficherosInstalacion"))
			fail("Lost connection to guest!");
	}

}
