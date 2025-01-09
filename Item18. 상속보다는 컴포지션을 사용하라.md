상위 클래스와 하위클래스를 모두 '같은 프로그래머'가 통제하는 '패키지 안'이라면 상속은 안전하다.
하지만 일반적인 구체 클래스를 패키지 경계를 넘어, 즉 다른 패키지의 구체 클래스를 상속하는 일은 위험하다. (클래스 -> 클래스 상속)



메서드 호출과 달리 상속은 캡슐화를 깨뜨린다.

상위 클래스는 릴리스마다 내부 구현이 달라질 수 있다 -> 이를 상속하는 하위 클래스에 오동작 등 영향을 줄 수 있다.



### 문제 1

HashSet(구체 클래스)을 상속한 MyHashSet(구체 클래스)
```java
public class MyHashSet<E> extends HashSet<E> {

    private int addCount = 0;

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}

public class MyHashSetMain {
    public static void main(String[] args) {
        MyHashSet<String> myHashSet = new MyHashSet<>();
        myHashSet.addAll(Arrays.asList("가", "나", "다"));
        System.out.println(myHashSet.getAddCount());  -> "6"
    }
}

AbstractCollection.class
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }   
```

위 경우,

1. addAll()을 재정의하지 않으면 해결할 수 있지만 HashSet의 addAll()이 add()를 이용해 구현했음을 가정한 해법이라는 한계를 가진다. 그래서 다음 릴리스에 HashSet의 addAll()이 변경되면 MyHashSet의 addAll()도 어떻게 바뀔지 모른다.

2. addAll()을 주어진 컬렉션을 순회하며 원소 하나당 add()를 한 번만 호출하는 식으로 재정의할 수도 있다.

```java
@Override
public boolean addAll(Collection<? extends E> c) {
    for (E e : c) {
        super.add(e);
    }
}
```
하지만 이 방법은 어렵고, 시간도 더 들고, 자칫 오류를 내거나 성능 저하가 발생할 수 있다. 그리고 만약 하위 클래스에서 접근할 수 없는 private 필드가 사용되어야 한다면 사용 자체가 불가능할 수 있다. 지금이야 super.add()가 public이니 가능하지만 private 메서드였다면 사용 못함.



### 문제 2

다음 릴리스에 새로운 메서드 추가

만약 보안 때문에 컬렉션에 추가된 모든 원소는 특정 조건을 만족해야 함. -> 원소를 추가할 때 조건 검사 후 추가하면 될 듯하다. -> 하지만 다음 릴리스에 상위 클래스에 또 다른 원소 추가 메서드가 만들어지면? => 허용되지 않은 원소를 추가할 수 있게 된다.

컬렉션 프레임워크 이전부터 존재하던 HashTable과 Vector를 컬렉션 프레임워크에 포함시키자 이와 관련한 보안 구멍들을 수정하여야 했다.





이상의 두 문제는 모두 메서드 재정의가 원인이었다. 그러면 재정의가 아닌 새로운 메서드를 추가하면 괜찮나?

하위 클래스에 새로운 매서드를 추가해서 사용중이라 가정하자. 하지만 다음 릴리스에 하필 운 없게도 상위 클래스에 하위 클래스에 직접 추가한 새로운 메서드와 시그니처가 같고 반환타입이 다른 메서드가 추가된다면? 컴파일조차 되지 않는다. 자바 컴파일러는 하위 클래스이고, 시그니처가 같다면, 재정의를 하는 것이라 판단하기 때문이다.

> attempting to use incompatible return type

만약 반환타입마저 같다면 그냥 메서드 재정의한 셈이다. 앞서 말한 똑같은 문제가 발생한다. 또한 하위 클래스를 작성할 때에는 상위 클래스 메서드는 존재하지도 않았으니 재정의가 되어버리면 상위 클래스가 요구하는 규약을 만족 못할 가능성이 매우 크다.





다행히, 이상의 문제를 모두 피해가는 묘안이 있다.

기존 클래스를 확장하는 대신, 새로운 클래스를 만들고 private 필드로 기존 클래스의 인스턴스를 참조하게 하는 것이다.

기존 클래스가 새로운 클래스의 구성요소로 쓰인다는 뜻에서 이러한 설계를 **컴포지션(composition; 구성)** 이라고 한다.



새 클래스의 인스턴스 메서드는 기존 클래스의 대응하는 메서드를 호출해서 그 결과를 반환하는 것을 '전달'이라고 하며 그 메서드들을 '전달 메서드'라 부른다. 그 결과 새로운 클래스는 기존 클래스의 내부 구현 방식의 영향에서 벗어나며, 심지어 기존 클래스에 새로운 메서드가 추가되더라도 전혀 영향받지 않는다.


```java
public class Main {
    public static void main(String[] args) {
        InstrumentedSet<String> s = new InstrumentedSet<>(new HashSet<>());
        s.addAll(List.of("틱", "택", "톡"));
        System.out.println(s.getAddCount());
    }
}

'커스텀 부가기능 Set'
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

    public int getAddCount() {
        return addCount;
    }
}

'전달 클래스'
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
        return s.addAll(c); //이제 addAll()은 main에서 넘겨준 HashSet의 addAll()이 호출된다.
        /* 즉, custom class인 InstrumentedSet과는 이제 연관이 없어진다는 것.
        => HashSet에서 필요한 기능만 가져다가 원하는 기능을 추가할 수 있다는 것.(1번 문제 해결 포함하며) */
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
}
```
심지어 유연하다. 기존 코드는 HashSet을 상속했기에 HashSet의 기능만 사용 가능했지만, 이제 생성자에 넘겨주는 값에 따라 다양한 Set의 기능을 추가, 보완할 수 있다.



컴포지션 구현 방법

1. 구체 클래스 각각을 따로 확장.

2. 지원하고 싶은 상위 클래스(ForwardingSet)의 생성자 각각에 대응하는 생성자를 별도로 정의.

이제 HashSet 뿐만 아니라 TreeSet 등 다양한 Set 구현체라도 받아서 기능을 추가할 수 있다.





다른 Set 인스턴스를 감싸고 있다는 뜻에서 InstrumentedSet 클래스를 래퍼 클래스라고 하며, 다른 Set에 계측 기능을 덧씌운다는 뜻에서 데코레이터 패턴이라고 한다. 컴포지션과 전달의 조합은 넓은 의미로 위임이라고 부른다.(엄밀히 따지면 래퍼 객체가 내부 객체에 자기 자신의 참조를 넘기는 경우만)



래퍼 클래스는 단점이 거의 없다. 래퍼 클래스가 콜백 프레임워크와는 어울리지 않는다는 점만 주의하자.



상속은 반드시 하위 클래스가 상위 클래스의 '진짜' 하위 타입인 상황에서만 쓰여야 한다. 즉, is-a 관계일 때만 해야한다. 클래스 A를 상속하는 클래스 B를 작성하려한다면, 먼저 자문해보자. "B는 정말 A인가?" 대답이 아니다라면 A를 private 인스턴스로 두고, A와는 다른 API를 제공해야 하는 상황이 대다수다. => 온전히 A의 API를 제공해야 할 때, B에서 A를 상속해서 사용해도 된다. 하지만 아닌 경우라면 A를 private 인스턴스로 가지고 A의 api를 변형해서 B에서 내보내는 경우이다. 즉, A는 B의 필수 구성요소가 아니라 구현하는 방법 중 하나일 뿐이다.



자바에서 위반한 클래스들 : 스택은 벡터가 아니므로 Stack은 Vector를 확장해서는 안됐다. 속성 목록도 해시테이블이 아니므로 Properties도 Hashtable을 확장해서는 안됐다.

컴포지션을 써야 할 상황에서 상속을 사용하는 건 내부 구현을 불필요하게 노출하는 꼴이다. 그 결과 API가 내부 구현에 묶이고 그 클래스의 성능도 영원히 제한된다. 더 심각한 문제는 클라이언트가 노출된 내부에 직접 접근할 수 있다는 점이다.

하위 클래스에 불변식이 존재해도, 상위 클래스에서 호출하면 불변식을 깨뜨릴 수 있다. 불변식이 한 번 깨지면 하위클래스의 기능들은 더 이상 사용할 수 없다.



컴포지션 대신 상속을 사용하기로 결정하기 전에 마지막으로 자문해야 할 질문

1. 확장하려는 클래스의 API에 아무런 결함이 없는가?

2. 결함이 있다면 이 결함이 여러분 클래스의 API까지 전파돼도 괜찮은가? (컴포지션은 이러한 결함을 숨기는 새로운 API를 설계할 수 있지만, 상속은 상위 클래스의 API를 그 결함까지도 그대로 승계한다.



상속은 상위 클래스와 하위 클래스가 순수한 is-a 관계일 때만 써야 한다. is-a 관계일 때도, 하위 클래스의 패키지와 상위 클래스의 패키지와 다르고(상위 클래스에서 default로 선언되어 있으면 다른 패키지에서 접근 불가능하기 때문) 상위 클래스가 확장을 고려해 설계되지 않았다면 여전히 문제가 될 수 있다. 상속 대신 컴포지션을 사용하자. 특히 래퍼 클래스로 구현할 적당한 인터페이스가 있다면 더욱 그렇다. 래퍼 클래스는 하위 클래스보다 견고하고 강력하다.
