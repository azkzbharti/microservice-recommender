package com.ibm.research.msr.utils;

import com.ibm.research.msr.utils.Commons;
import org.junit.Assert;
import org.junit.Test;

public class CommonsTest {

    @Test
    public void getMSRBaseDirTest() {
        System.setProperty("MSR_HOME", "/msr/home");
        String msrBaseDir = Commons.getMSRBaseDir();
        Assert.assertEquals("/msr/home", msrBaseDir);
    }
}
