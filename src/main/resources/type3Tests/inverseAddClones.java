class inverseAddClones {
    public int method1(int a, int b) {
        return (a > b) ? a : b;
    }

    public int method2(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

//    int add1() {
//        int a = 1;
//        int b = 2;
//        return a + b;
//    }
//
//    int add2() {
//        int a = 1;
//        int b = 2;
//        return a + b;
//    }


//    int methodA(int x, int y) {
//        int result = x * y;
//        if (x < y) {
//            result += y;
//        } else {
//            result += x;
//        }
//        return result;
//    }
//
//    int methodB(int a, int b) {
//        int output;
//        output = a * b;
//        if (a >= b) {
//            output = output + a;
//        } else {
//            output = output + b;
//        }
//        return output;
//    }
}
