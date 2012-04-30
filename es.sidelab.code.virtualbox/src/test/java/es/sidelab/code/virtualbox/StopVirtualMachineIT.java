package es.sidelab.code.virtualbox;

import org.junit.Test;

public class StopVirtualMachineIT {
	@Test
	public void stopVM() throws Exception{
		ControlVBox.saveVMStateAndDisconnect();
	}
}
