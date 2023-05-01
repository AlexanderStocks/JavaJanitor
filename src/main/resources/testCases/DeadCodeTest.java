class DeadCodeTest {

    private int unusedField;

    private int usedField;

    private void unusedMethod() {
        if (true) {
            System.out.println("This is a valid if statement");
        } else {
        }
    }

    private void usedMethod() {
        int unusedVariable;
        int usedVariable = 0;
        System.out.println(usedField);
        System.out.println(usedVariable);
    }

    public static void main(String[] args) {
        TestClass test = new TestClass();
        test.usedMethod();
    }

    public void testMethod() {
        int[] numbers = { 1, 2, 3, 4, 5 };
        for (int i = 0; i < numbers.length; i++) {
            System.out.println(numbers[i]);
        }
    }
}
