package composition;

import java.util.HashSet;
import java.util.List;

public class Main {

    public static void main(String[] args) {
//        int INIT_CAPACITY = 16;
//        Set<E> s = new InstrumentedSet<>(new HashSet<>(INIT_CAPACITY));

        InstrumentedSet<String> s = new InstrumentedSet<>(new HashSet<>());
        s.addAll(List.of("틱", "택", "톡"));
        System.out.println(s.getAddCount());
    }
}
