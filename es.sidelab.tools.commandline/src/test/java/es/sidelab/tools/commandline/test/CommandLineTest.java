package es.sidelab.tools.commandline.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import es.sidelab.tools.commandline.CommandLine;
import es.sidelab.tools.commandline.CommandOutput;
import es.sidelab.tools.commandline.ExecutionCommandException;

public class CommandLineTest {

	@Test
	public void testSyncExec() {

		CommandLine cl = new CommandLine(new File("target/test-classes/"));
		try {
			CommandOutput co = cl.syncExec("java es.sidelab.tools.commandline.test.MainTest");
			System.out.println(MainTest.STD_OUTPUT);
			System.out.println(co.getStandardOutput());
			assertEquals(MainTest.STD_OUTPUT, co.getStandardOutput());
			assertEquals(MainTest.ERR_OUTPUT, co.getErrorOutput());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (ExecutionCommandException e) {
			fail(e.getMessage() + "Std: " + e.getStandardOutput() + "Err:" + e.getErrorOutput());
		}
		
		cl = new CommandLine(new File("target/test-classes/"));
		try {
			CommandOutput co = cl.syncExec("java es.sidelab.tools.commandline.test.MainTestError");
			fail("Should launch an exception");
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (ExecutionCommandException e) {
			assertEquals(2, e.getExitCode());
			assertEquals(MainTestError.STD_OUTPUT, e.getStandardOutput());
			assertEquals(MainTestError.ERR_OUTPUT, e.getErrorOutput());
		}
	}

}
