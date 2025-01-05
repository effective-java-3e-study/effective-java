package composition;

import java.util.Collection;
import java.util.Set;

public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;

    public InstrumentedSet(Set<E> s) {
        super(s);
    }

    //얘가 호출이 되지 않는다는 것이 포인트임. addAll()내부의 add()는 이제 new InstrumentedSet<>(여기) 얘를 생성할 때 생성자로 넘겨준 Set 구현체의 add가 최종 호출됨.
    @Override
    public boolean add(final E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    @Override
    public boolean addDoubleAll(final Collection<? extends E> c) {
        return super.addDoubleAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
