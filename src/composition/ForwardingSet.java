package composition;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;

    public ForwardingSet(Set<E> s) {
        this.s = s;
    }

    @Override
    public int size() {
        return s.size();
    }

    @Override
    public boolean isEmpty() {
        return s.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return s.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return s.iterator();
    }

    @Override
    public Object[] toArray() {
        return s.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return s.toArray(a);
    }

    @Override
    public boolean add(final E e) {
        return s.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return s.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return s.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        System.out.println("ForwardingSet.addAll");
        return s.addAll(c);
    }
//    @Override
    public boolean addDoubleAll(final Collection<? extends E> c) {
        s.addAll(c);
        return s.addAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return s.retainAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return s.removeAll(c);
    }

    @Override
    public void clear() {
        s.clear();
    }

    public int getSetCount() {
        return 0;
    }
}
