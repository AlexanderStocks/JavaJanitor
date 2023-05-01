class CollapseNestedIfStatements {
    void testMethod() {
        int a = 5;
        int b = 10;
        if (a < b) {
            if (b > 0) {
                System.out.println("b is greater than a and positive");
            }
        }
    }
}