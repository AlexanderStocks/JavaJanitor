public class DuplicateCodeExample2 {

    public int method1() {
        genericMethod();
    }

    public void method2() {
        genericMethod();
    }

    public void method3() {
        genericMethod();
    }

    public int genericMethod() {
        genericMethod();
    }
}
