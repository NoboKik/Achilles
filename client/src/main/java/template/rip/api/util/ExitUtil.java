package template.rip.api.util;

import java.util.Arrays;

public class ExitUtil {

    public static void exit(ExitType exitType) {
        switch (exitType) {
            case EXIT: System.exit(1); break;
            case EXCEPTION: throw new ExitException();
            case HALT: Runtime.getRuntime().halt(1); break;
            case KILL_JAVA: Arrays.asList("java.exe", "javaw.exe").forEach(ExitUtil::killThread); break;
        }
    }

    private static void killThread(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) t.stop();
        }
    }

    public enum ExitType {
        EXIT, EXCEPTION, HALT, KILL_JAVA
    }

    private static class ExitException extends RuntimeException {

        public ExitException() {
            super();
            setStackTrace(new StackTraceElement[0]);
        }
    }
}
