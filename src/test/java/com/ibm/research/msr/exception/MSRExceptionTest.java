package com.ibm.research.msr.exception;

import com.ibm.research.msr.exception.MSRException;
import org.junit.Assert;
import org.junit.Test;

public class MSRExceptionTest {

    MSRException exception = new MSRException("Exception message");

    @Test
    public void testMsrException() {
        Assert.assertEquals("Exception message", exception.getMessage());
    }
}
