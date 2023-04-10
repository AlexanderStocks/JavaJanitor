public class Type2ClonesExample {
    public void method1() {
        int a = 10;
        int b = 20;
        int c = a + b;   // clone
        int d = a + b;   // clone
        int e = 30;
        int f = c + e;
        System.out.println(f);
    }

    public void method2() {
        int a = 40;
        int b = 50;
        int c = a + b;   // clone
        int d = a + b;   // clone
        int e = 60;
        int f = c + e;
        System.out.println(f);
    }
}
