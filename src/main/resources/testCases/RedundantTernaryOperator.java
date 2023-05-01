public class RedundantTernaryOperator {
    public void testMethod() {
        int a = 5;
        int b = a > 3 ? 5 : 5;
        int c = a < 3 ? 4 : 5;
    }
}