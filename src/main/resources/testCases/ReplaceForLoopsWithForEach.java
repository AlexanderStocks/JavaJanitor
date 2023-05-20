public class ReplaceForLoopsWithForEach {

    public static void printNumbers(int[] numbers) {
        for (int i : numbers) {
            System.out.println(numbers[i]);
        }
    }

    private ReplaceForLoopsWithForEach() {
    }

    private static final ReplaceForLoopsWithForEach INSTANCE = new ReplaceForLoopsWithForEach();

    public static ReplaceForLoopsWithForEach getInstance() {
        return INSTANCE;
    }
}
