1. 추상 클래스 vs 인터페이스

가장 큰 차이는 추상 클래스가 정의한 타입을 구현하는 클래스는 반드시 추상 클래스의 하위 타입이 되어야 한다는 점이다. 추상 클래스 방식은 새로운 타입을 정의하는 데 커다란 제약을 안는 셈이다. 반면 인터페이스는 제대로 규약을 지켜서 구현했다면 모두 같은 타입으로 취급된다. 즉, 인터페이스는 같은 타입으로 확장이 가능하지만, 추상 클래스는 단일 상속만 가능하기 때문에 확장에 불리하다. 즉, 상속받은 하나의 부모 클래스로밖에 변경이 불가능함. 다른 타입으로 될 수가 없음.



인터페이스는 믹스인(mixin) 정의에 안성맞춤이다.

* 믹스인이란? 클래스가 구현할 수 있는 타입(=인터페이스라고 생각하면 됨)으로, 믹스인을 구현한 클래스에 원래의 '주된 타입' 외에도 특정 선택적 행위를 제공한다고 선언하는 효과를 준다.



추상 클래스로는 믹스인을 정의할 수 없다. 기존 클래스에 덧씌울 수 없기 때문이다. 다중 상속이 안되고, 클래스 계층 구조에는 믹스인을 삽입하기에 합리적인 위치가 없기 때문이다.



단일 상속의 한계로 인해 이 추상 클래스 믹스인은 합리적인 위치가 없음.
public abstract class SwimMixin {
void swim(){}
}

```java
/**컴파일 오류*/
public class Horse extends Animal, SwimMixin {

}
```

인터페이스로는 계층구조가 없는 타입 프레임워크를 만들 수 있다.

타입을 계층적으로 정의(추상 클래스, 상속)하면 수많은 개념을 구조적으로 잘 표현할 수 있지만, 현실에서는 계층을 엄격히 구분하기 어려운 개념도 있다. (ex, Singer, Songwriter)

인터페이스로 정의한다면, 구현체가 모두 implements해도 되고, 새로운 제 3의 인터페이스를 만들어서 Singer와 Songwriter를 extends하고 추가 추상 메서드도 정의할 수 있다.

만약 이를 클래스를 이용해서만 만들려면 가능한 조합 전부를 각각의 클래스로 정의한 고도비만 계층구조가 만들어질 것이다. (즉, 계층구조가 존재하고, 너무 많은 계층 구조가 생긴다는 것. 인터페이스는 계층 구조가 생기지 않는다.)





래퍼 클래스 관용구와 함께 사용하면 인터페이스는 기능을 향상시키는 안전하고 강력한 수단이 된다.

* 래퍼 클래스 관용구란?

    - 기존 클래스를 수정하지 않고 새로운 기능을 추가할 때 사용하는 방식으로, 기존 클래스를 감싸는 새로운 클래스를 만드는 것.




디폴트 메서드 - 인터페이스의 메서드 중 구현 방법이 명백한 것이 있다면, 그 구현을 디폴트 메서드로 제공해 프로그래머들의 일감을 덜어줄 수 있다.

디폴트 메서드를 제공할 때는 상속하려는 사람을 위한 설명을 @implSpec 자바독 태그를 붙여 문서화해야 한다.

* @implSpec이란? 자바 문서화 주석(Javadoc)의 태그로, 메서드의 구현 동작을 설명하는데 사용됩니다. 특히 디폴트 메서드나 다른 상속 가능한 메서드의 구현 방식을 문서화할 때 유용.

Collection interface

```java
* @implSpec
* The default implementation calls the generator function with zero
  * and then passes the resulting array to {@link #toArray(Object[]) toArray(T[])}.
    */
        default <T> T[] toArray(IntFunction<T[]> generator) {
        return toArray(generator.apply(0));
    }

@implSpec은 정말 메서드 내부 구현 동작 방식에 대해 설명하고 있다.
```

디폴트 메서드 제약

1. 많은 인터페이스가 equals와 hashCode 같은 Object의 메서드를 정의하고 있지만, 이들은 디폴트 메서드로 제공해서는 안된다. equals와 hashCode는 구체 클래스에서 재정의해야 하니까.

2. 인터페이스는 인스턴스 필드를 가질 수 없고(인터페이스가 변하는 인스턴스 필드를 보유하면 안된다. 이를 구현하는 구현체들은 변하는 인스턴스를 구현 못함), public이 아닌 정적 멤버도 가질 수 없다.(단 private 정적 메서드는 예외다)

3. 우리가 만들지 않은 인터페이스에는 디폴트 메서드를 추가할 수 없다.


원칙 : 인터페이스는 공개용이고, 인스턴스 필드를 가질 수 없다. (java 9부터는 private 정적, 구현 메서드 가능.)

```java
    void haha213() {
      System.out.println("haha213");
    }

위 메서드 인터페이스에 선언하고 싶을 때,
--공개용--
    default void haha213() {
      System.out.println("haha213");
    }
or
--static 공개용--
    static void haha213() {
      System.out.println("haha213");
    }
or
--private--
    private void haha213() {
      System.out.println("haha213");
    }
or
--private static--
    private static void haha213() {
      System.out.println("haha213");
    }
```


-------------------------


인터페이스와 추상 골격 구현 클래스를 함께 제공하는 식으로 인터페이스와 추상 클래스의 장점을 모두 취하는 방법이 있다.

인터페이스로는 타입을 정의하고, 필요하면 디폴트 메서드도 정의한다. 그리고 골격 구현 클래스는 나머지 메서드들(인터페이스 내부 추상 메서드들)까지 구현한다.

이렇게 하면 골격 구현 클래스를 extends 하는 것만으로 이 인터페이스를 구현하는 데 필요한 일이 대부분 완료된다. = 템플릿 메서드 패턴.



골격 구현을 사용해 완성한 구체 클래스 예시




```java
    static List<Integer> intArrayAsList(int[] a) {
        Objects.requireNonNull(a);

        return new IntegerAbstractList(a);
    }

    private static class IntegerAbstractList extends AbstractList<Integer> {
        private final int[] a;

        public IntegerAbstractList(int[] a) {
            this.a = a;
        }

        @Override
        public Integer get(int index) {
            return a[index];
        }

        @Override
        public int size() {
            return a.length;
        }
    }

/**골격 구현 클래스*/
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
    protected AbstractList() {}

    public boolean add(E e) {
        add(size(), e);
        return true;
    }
```

핵심 포인트

1. 인터페이스로는 해야할 일 정의

2. 골격 구현 클래스로는 공통 코드 작성

3. 구체 클래스에서는 특별한 부분만 구현


```java
public interface Gym {
    void beginStretch();
    void finishStretch();
    void startWorkout();
}

public abstract class AbstractGym implements Gym {
    private boolean isStretching = false;

    @Override
    public void startWorkout() {
        if (!isStretching) {
            beginStretch();
            isStretching = true;
        }
        workingOut();
        finishStretch();
    }

    private void workingOut() {
        System.out.println("운동을 합니다.");
    }
}

public class FitnessGym extends AbstractGym {
    @Override
    public void beginStretch() {
        System.out.println("헬스장에서 운동 전에 몸을 스트레칭 합니다.");
    }

    @Override
    public void finishStretch() {
        System.out.println("헬스장에서 마무리 스트레칭 합니다.");
    }
}

public class YogaGYM extends AbstractGym {
    @Override
    public void beginStretch() {
        System.out.println("요가를 위해 몸을 유연하게 스트레칭합니다.");
    }

    @Override
    public void finishStretch() {
        System.out.println("요가를 마치고 몸을 편안하게 합니다.");
    }
}

public static void main(String[] args) {
    Gym gym = new YogaGYM();
      gym.startWorkout();
    }
```
단순하게, 미리 인터페이스의 기능들을 재정의하거나 중간에서 추가적인 작업을 진행해둘 수 있는 장점이 있다. List를 직접 impl 한다면 매우 많이 재정의해야 하지만, AbstractList처럼 미리 골격 구현 클래스로 대부분 공통으로 구현해두거나 추가적인 로직을 넣고, 이후 AbstractList를 상속받아서 사용하면 get()과 size()만 재정의해서 사용하면 된다.



골격 구현 작성법

1. 인터페이스를 살펴보고 다른 메서드들의 구현에 사용되는 기반 메서드를 선정한다. (기반 메서드는 골격 구현 클래스에서는 추상 메서드가 된다.)

2. 기반 메서드를 사용해 직접 구현할 수 있는 메서드를 모두 디폴트 메서드로 제공한다. (골격 구현 클래스에서)



인터페이스로는 기본 디폴트 메서드를 정의하는데 한계가 있다. Object의 equals()나 hashcode()도 구현 못하고 여러 반복 적인 코드를 미리 구현해둘 수 없다. 그래서 골격 구현 클래스를 만들어서 미리 인터페이스를 구현해서 메서드를 재정의 해두는 식이다. 그러면 클라이언트는 골격 구현 클래스를 익명 클래스로 만들어서 바로 메서드를 사용하거나 골격 구현 클래스를 확장한 클래스를 통해 그 골격 구현 클래스에 미리 정의된 인터페이스의 동작들을 바로 활용할 수 있다.
