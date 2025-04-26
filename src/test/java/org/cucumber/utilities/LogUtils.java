package org.cucumber.utilities;

import org.cucumber.step_definitions.ERAAutomation_Defs;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class LogUtils {


    private static final Logger logger = LoggerFactory.getLogger(ERAAutomation_Defs.class);

    public static void assertEquals(String errorLogMessage, Object expected, Object actual ) {
        try {
            Assert.assertEquals(expected, actual);
        } catch (AssertionError e) {
            System.out.println("errorLogMessage = " + errorLogMessage);
            e.printStackTrace();
            logger.error(errorLogMessage+" --> " + Arrays.toString(e.getStackTrace()), expected, actual,  e);
            throw e;
        }
    }

    public static void assertNotNull(Object expected, String errorLogMessage) {
        try {
            Assert.assertNotNull(expected);
        } catch (AssertionError e) {
            System.out.println("errorLogMessage = " + errorLogMessage);
            e.printStackTrace();
            logger.error(errorLogMessage+" --> "+ Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }

    public static void assertTrue(boolean expected, String errorLogMessage) {
        try {
            Assert.assertTrue(expected);
        } catch (AssertionError e) {
            System.out.println("errorLogMessage = " + errorLogMessage);
            e.printStackTrace();
            logger.error(errorLogMessage+" --> "+ Arrays.toString(e.getStackTrace()));;
            throw e;
        }
    }

    public static void assertFalse(boolean expected, String errorLogMessage) {
        try {
            Assert.assertFalse(expected);
        } catch (AssertionError e) {
            e.printStackTrace();
            System.out.println("errorLogMessage = " + errorLogMessage);
            logger.error(errorLogMessage+" --> "+ Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }

    public static void logError(StackTraceElement[] stackTrace, Exception e) throws Exception {
        if (stackTrace.length > 0) {
            StackTraceElement element = stackTrace[0];
            logger.error("Exception in method: {} at line: {} - {}", element.getMethodName(), element.getLineNumber(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            throw e;
        } else {
            logger.error("An error occurred: {}", e.getMessage(),  Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }

}


