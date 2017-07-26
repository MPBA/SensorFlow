package eu.fbk.mpba.sensorflow;

class Log {
    private static long ord = 1_000_000_000L;
    public static boolean enabled = false;
    public static void l(Object text) {
        if (enabled) {
            StackTraceElement x = Thread.currentThread().getStackTrace()[2];
            String b =
                    String.valueOf(ord++) +
                    " " +
                    Thread.currentThread().getId() +
                    " " +
                    x.getMethodName() +
                    " (" +
                    x.getFileName() +
                    ":" +
                    x.getLineNumber() +
                    "): " +
                    text;
            System.out.println(b);
        }
    }
    public static void l() {
        l("");
    }
    public static void s() {
        System.out.println(Thread.currentThread().getId() + " " + Thread.currentThread().getName());
        new Throwable().printStackTrace();
    }
}
