package es.sidelab.code.virtualbox;

import static org.junit.Assert.fail;

import org.junit.Test;

public class StartVirtualMachineIT {
	@Test
	public void startVM() throws Exception{
		if (! ControlVBox.startVMAndTestConn())
			fail("Unable to start testing machine.");
	}
}
