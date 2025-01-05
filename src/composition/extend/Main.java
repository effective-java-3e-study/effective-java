package composition.extend;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
        s.addAll(List.of("틱", "택", "톡"));
        System.out.println(s.getAddCount());
    }
}
