class Type1Test {

    int add1() {
        return genericMethod0();
    }

    int add2() {
        return genericMethod0();
    }

    public void test3() {
        System.out.println("test3");
    }

    int genericMethod0() {
        int a = 1;
        int b = 2;
        return a + b;
    }
}
