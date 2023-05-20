public class ReplaceConcatenationWithStringBuilder {

    public void testMethod() {
        String s = new StringBuilder().append("Hello, ").append("world!");
        String t = new StringBuilder().append("This is a ").append("test.");
    }
}
