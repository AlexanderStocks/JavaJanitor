public class DuplicateCodeExample2 {

    public int method1() {
        for (int i = 0; i < 5; i++) {
            System.out.println("Hello, World!");
            System.out.println("Hello, World!");
        }
        for (int i = 0; i < 3; i++) {
            System.out.println("Hello, World!");
            System.out.println("Hello, World!");
        }
        System.out.println("Hello, World!");
        System.out.println("Hello, World!");
        int num = 8;
        return num;
    }

    public void method2() {
        genericMethod();
    }

    public void method3() {
        genericMethod();
    }

    public void genericMethod() {
        // A comment
        System.out.println("Hello, World!");
        System.out.println("Hello, World!");
    }
}
