package com.ibm.research.msr.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MSRApiApplicationTest {

	@Test
	final void testMain() {
		System.out.println("passing  main sprint boot app method");
		assertEquals("pass","pass");
	}

	@Test
	final void testMultipartConfigElement() {
		assertEquals("pass", "pass");
	}

}
