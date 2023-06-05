package refactor.refactorings.collapseNestedIfStatements;

import java.util.List;

public class Testing {

    public static void main(String[] args) {
        List<String> names = List.of("");
        for (String name : names) {
            System.out.println(name);
        }

        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i));
        }
    }
}
