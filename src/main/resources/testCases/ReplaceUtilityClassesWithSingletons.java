public class UtilityClass {

    private UtilityClass() {
        // Private constructor
    }

    public static void doSomething() {
        // Some static method
    }

    private static final UtilityClass INSTANCE = new UtilityClass();

    public static UtilityClass getInstance() {
        return INSTANCE;
    }
}
