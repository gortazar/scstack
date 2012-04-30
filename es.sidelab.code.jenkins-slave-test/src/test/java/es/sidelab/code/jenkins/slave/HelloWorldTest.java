package es.sidelab.code.jenkins.slave;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HelloWorldTest {

	HelloWorld hw;
	
	@Before
	public void setUp() {
		hw = new HelloWorld();
	}
	
	@Test
	public void testGetMsg() {
		assertEquals(hw.getMsg(), "Hello World!");
	}

}
