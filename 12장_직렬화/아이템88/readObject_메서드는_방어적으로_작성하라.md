# readObject 메서드는 방어적으로 작성하라

## 방어적 복사를 사용하는 불변 클래스

[아이템 50]([https://023-dev.github.io/item-50/](https://github.com/effective-java-3e-study/effective-java/blob/main/8%EC%9E%A5_%EB%A9%94%EC%84%9C%EB%93%9C/%EC%95%84%EC%9D%B4%ED%85%9C_50/%EC%A0%81%EC%8B%9C%EC%97%90_%EB%B0%A9%EC%96%B4%EC%A0%81_%EB%B3%B5%EC%82%AC%EB%B3%B8%EC%9D%84_%EB%A7%8C%EB%93%A4%EB%9D%BC.md))에서는 불변식을 지키고 불변을 유지한 날짜 클래스를 만들기 위해,
생성자와 접근자(getter)에서 Data 객체를 방어적으로 복사하도록 했다.
다음이 그 클래스의 모습니다.

```java
public final class Period {
    private final Date start;
    private final Date end;

    /**
     * @param  start 시작 시각
     * @param  end 종료 시각; 시작 시각보다 뒤여야 한다.
     * @throws IllegalArgumentException 시작 시각이 종료 시각보다 늦을 때 발생한다.
     * @throws NullPointerException start나 end가 null이면 발생한다.
     */
    public Period(Date start, Date end) {
        this.start = new Date(start.getTime()); // 가변인 Date 클래스의 위험을 막기 위해 새로운 객체로 방어적 복사
        this.end = new Date(end.getTime());

        if (this.start.compareTo(this.end) > 0) {
            throw new IllegalArgumentException(start + " after " + end);
        }
    }

    public Date start() { return new Date(start.getTime()); }
    public Date end() { return new Date(end.getTime()); }
    public String toString() { return start + " - " + end; }
    // ... 나머지 코드는 생략
}
```

이 클래스를 직렬화를 하고자 한다면 물리적 표현과 논리적 표현이 같기 때문에 기본 직렬화 형태를 사용해도 무방할 것 같다.
따라서 `Serializable`만 구현하면 될 것 같지만, 사실 실제로는 불변식을 보장하지 못하게 된다.

그 이유는 `readObject` 메서드가 실질적으로 또 다른 `public` 생성자이기 때문이다.
따라서 `readObject` 메서드도 다른 생성자와 똑같은 수준으로 주의를 기울여야 한다. 

## readObject 메서드

쉽게 말해, `readObject` 메서드는 매개변수로 바이트 스트림을 받는 생성자라고 할 수 있다.
보통 바이트 스트림은 정상적으로 생성된 인스턴스를 직렬화해서 만들어진다.
하지만 불변을 깨뜨릴 의도로 만들어진 바이트 스트림을 받으면 문제가 생긴다.
이는 정상적인 생성자로는 만들어낼 수 없는 객체를 생성하기 때문이다.

단순하게 앞서 살펴본 `Period` 클래스에 `Serializable` 구현을 추가했다고 가정했을 때,
아래와 같은 코드는 불변식을 깨뜨리는 공격을 할 수 있다.

```java
public class BogusPeriod {
    // 진짜 Period 인스턴스에서는 만들어질 수 없는 바이트 스트림,
    // 정상적인 Period 인스턴스를 직렬화한 후에 손수 수정한 바이트 스트림이다.
    private static final byte[] serializedForm = {
        (byte)0xac, (byte)0xed, 0x00, 0x05, 0x73, 0x72, 0x00, 0x06,
        0x50, 0x65, 0x72, 0x69, 0x6f, 0x64, 0x40, 0x7e, (byte)0xf8,
        ... 생략
    }

    // 상위 비트가 1인 바이트 값들은 byte로 형변환 했는데,
    // 이유는 자바가 바이트 리터럴을 지원하지 않고 byte 타입은 부호가 있는(signed) 타입이기 때문이다.

    public static void main(String[] args) {
        Period p = (Period) deserialize(serializedForm);
        System.out.println(p);
    }

    // 주어진 직렬화 형태(바이트 스트림)로부터 객체를 만들어 반환한다.
    static Object deserialize(byte[] sf) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sf)) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                return objectInputStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
```

```java
# 실행 결과, end가 start 보다 과거다. 즉, Period의 불변식이 깨진다.
Fri Jan 01 12:00:00 PST 1999 - Sun Jan 01 12:00:00 PST 1984
```

이렇게 `Period`를 직혈화 할 수 있도록 선언한 것만으로도 클래스의 불변식을 깨뜨리는 객체를 만들 수 있게 된 것이다.

## 어떻게 방어할 수 있을까?

이 문제를 고치려면 `Period`의 `readObject` 메서드가 `defaultReadObject`를 호출하게 한 후에 역직렬화된 객체가 유효한지 검사해야 한다.
여기서 유효성 검사에 실패한다면, `InvalidObjectException`을 던지게 해서 잘못된 역직렬화가 일어나는 것을 막을 수 있다.

```java
private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {

    // 불변식을 만족하는지 검사한다.
    if (start.compareTo(end) > 0) {
        throw new InvalidObjectException(start + "after" + end);
    }
}
```

하지만 여기서도 문제가 존재하는데, 정상적인 `Period` 인스턴스에서 시작된 바이트 스트림 끝에 `private Date` 필드 참조를 추가하면 가변적인 인스턴스를 만들어 낼 수 있다.
이때 공격자가 역직렬화를 통해 바이트 스트림 끝의 추가된 참조 값을 읽으면 `Period`의 내부 정부를 얻을 수 있다.
이렇게 되면 참조로 얻은 `Date` 인스턴스들을 수정을 할 수 있게 되어, 더는 `Period` 인스턴스가 불변이 아니게 되는 것이다. 

```java
public class MutablePeriod {
    // Period 인스턴스
    public final Period period;

    // 시작 시각 필드 - 외부에서 접근할 수 없어야 한다.
    public final Date start;

    // 종료 시각 필드 - 외부에서 접근할 수 없어야 한다.
    public final Date end;

    public MutablePeriod() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);

            // 유효한 Period 인스턴스를 직렬화한다.
            out.writeObject(new Period(new Date(), new Date()));

            /*
             * 악의적인 '이전 객체 참조', 즉 내부 Date 필드로의 참조를 추가한다.
             * 상세 내용은 자바 객체 직렬화 명세의 6.4절 참조.
             */
            byte[] ref = { 0x71, 0, 0x7e, 0, 5 }; // 참조 #5
            bos.write(ref); // 시작(start) 필드
            ref[4] = 4; // 참조 #4
            bos.write(ref); // 종료(end) 필드

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            period = (Period) in.readObject();
            start = (Date) in.readObject();
            end = (Date) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) {
        MutablePeriod mp = new MutablePeriod();
        Period p = mp.period;
        Date pEnd = mp.end;

        // 시간을 되돌린다.
        pEnd.setYear(78);
        System.out.println(p);

        // 60년대로 돌아간다.
        pEnd.setYear(69);
        System.out.println(p);
    }
}
```

```java
Wed Nov 22 00:21:29 PST 2017 - Wed Nov 22 00:21:29 PST 1978
Wed Nov 22 00:21:29 PST 2017 - Sat Nov 22 00:21:29 PST 1969
```

이 문제의 원인은 `Period`의 `readObject` 메서드가 방어적 복사를 하지 않음에 있다. 
역직렬화를 할 때는 클라이언트가 접근해서는 안 되는 객체 참조를 갖는 필드는 모두 방어적으로 복사를 해야 한다.

## 방어적 복사와 유효성 검사를 모두 수행해야 한다.

Period를 공격으로부터 보호하기 위해 방어적 복사를 유효성 검사보다 먼저 수행해야 한다. 
또한 Date의 clone 메서드는 사용되지 않았다.

```java
private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();

    // 가변 요소들을 방어적으로 복사한다.
    start = new Date(start.getTime());
    end = new Date(end.getTime());

    // 불변식을 만족하는지 검사한다.
    if (start.compareto(end) > 0) {
        throw new InvalidObjectException(start + " after " + end);
    }
}
```

```java
# MutablePeriod의 main 메서드 출력 결과. 
Fri May 31 01:01:06 KST 2019 - Fri May 31 01:01:06 KST 2019
Fri May 31 01:01:06 KST 2019 - Fri May 31 01:01:06 KST 2019
```

해당 작업에 있어서 `final` 필드는 방어적 복사가 불가능하다.
그래서 이 `readObject` 메서드를 사용하려면 `start`와 `end` 필드에서 `final` 한정자를 제거해야 한다.
이 부분에 대해서 다른 보안적인 이슈와 트레이드 오프가 있겠지만, 앞서 살펴본 공격 위험에 노출되는 것보다 나은 방향이다.

## 그럼 언제 기본 readObject를 사용해야 할까?

transient 필드를 제외한 모든 필드의 값을 매개변수로 받아 유효성 검사를 없이도 필드에 대입하는 public 생성자를 추가해도 괜찮다고 판단되면 기본 readObject 메서드를 사용해도 된다. 
아닌 경우 직접 readObject 메서드를 정의하여 생성자에서 수행했어야 할 모든 유효성 검사와 방어적 복사를 수행해야 한다. 
가장 추천되는 것은 직렬화 프록시 패턴을 사용하는 것이다. 
역직렬화를 안전하게 만드는 데 필요한 노력을 줄여준다.

final이 아닌 직렬화 가능한 클래스라면 생성자처럼 readObject 메서드도 재정의(overriding) 가능한 메서드를 호출해서는 안 된다. 
하위 클래스의 상태가 완전히 역직렬회되기 전에 하위 클래스에서 재정의된 메서드가 실행되기 때문이다.

## 정리

`readObject` 메서드를 작성할 때는 언제나 `public` 생성자를 작성하는 자세로 임해야 한다.
`readObject`는 어떤 바이트 스트림이 넘어오더라도 유효한 인스턴스를 만들어내야 한다.
이 바이트 스트림이 항상 진짜 직렬화된 인스턴스라고 가정해서는 안 된다.

해당 아이템에서는 기본 직렬화 형태를 사용해 예를 들었지만 커스텀 직렬화 또한 모든 문제가 그대로 발생할 수 있다.
그래서 다음과 같은 지침을 따르는 것을 권장한다.

- `private`이어야 하는 객체 참조 필드는 각 필드가 가리키는 객체를 방어적으로 복사하라. 불변 클래스 내의 가변 요소가 여기 속한다.
- 모든 불변식을 검사하여 어긋나는 게 발견되면 `InvalidObjectException`을 던져라. 방어적 복사 다음에는 반드시 불변식 검사가 뒤따라야 한다.
- 역직렬화 후 객체 그래프 전체의 유효성을 검사해야 한다면 `ObjectInputValidation` 인터페이스를 사용하라.
- 직접적이든 간접적이든, 재정의할 수 있는 메서드는 호출하지 말자.
