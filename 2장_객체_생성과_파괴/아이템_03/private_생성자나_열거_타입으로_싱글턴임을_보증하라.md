# private 생성자나 열거 타입으로 싱글턴임을 보증하라

## 싱글턴이란?
> 애플리케이션에서 특정 클래스의 인스턴스를 오직 하나만 생성하도록 보장하는 패턴

예를 들어, 무상태(stateless) 객체나 설계상 유일해야 하는 시스템 컴포넌트가 있다.
그런데, **클래스를 싱글턴으로 만들면 이를 사용하는 클라이언트를 테스트하기가 어려워진다.**

## 싱글턴을 쓰는 이유
- 리소스 관리 및 접근 제어
- 메모리 효율성
- 공유 상태의 일관성

## 싱글턴을 만드는 방법
### 1. public static final 필드 방식의 싱글턴
> 싱글턴 객체를 public static final 필드로 직접 선언하는 방법

```java
public class Singleton {
    public static final Singleton INSTANCE = new Signleton();
    
    private Singleton() {
        // private 생성자
    }
}
```

코드에서 볼 수 있듯이, public, protected 대신 **private 생성자**를 사용했기에 Singleton.INSTANCE를 초기화할 때 딱 한 번 호출된다.
-> 전체 시스템에서 Singleton 클래스의 인스턴스가 하나뿐인 것이 보장된다.

BUT, AccessibleObject.setAccessible로 private 생성자에 접근하는 예외가 발생할 수 있다.
-> 생성자 내부를 수정하여 예외를 막을 수 있다.

#### 1번 방법의 장점
- 싱글턴 클래스임이 API에 명백히 드러난다.
  - public static final로 설정했기 때문에 다른 객체를 참조할 수 없다.
- 간결하다.

### 2. 정적 팩터리 방식의 싱글턴
> 싱글턴 객체를 제공하기 위해 정적 메서드를 사용하는 방법

```java
    public class Singleton {
    private static final Singleton INSTANCE = new Singleton();
    
    private Singleton() {
        // private 생성자 -> 외부에서 객체를 생성하지 못하도록 막음
    }
    
    public static Singleton getInstance() {
        return INSTANCE;
    }
}
```

코드에서 볼 수 있듯에, public static final이 아닌 private static final로 싱글턴 인스턴스를 선언한 것을 볼 수 있다.
-> 싱글턴 인스턴스를 직접 노출하지 않고 정적 팩터리 메서드(getInstance())를 통해 접근한다.

#### 2번 방법의 장점
- API를 바꾸지 않고도 싱글턴이 아니게 변경할 수 있다.
- 정적 팩터리를 제네릭 싱글턴 팩터리로 만들 수 있다. (아이템 30)
- 정적 팩터리의 메서드 참조를 공급자(supplier)로 사용할 수 있다. (아이템 43, 44)

## 직렬화 & 역직렬화

### 직렬화
> 오브젝트를 파일 형태로 디스크에 저장하는 것

직렬화는 JVM의 힙 메모리에 있는 객체 데이터를 바이트 스트림 형태로 바꿔 외부 파일로 내보낼 수 있게 하는 기술이다.

### 역직렬화
> 직렬화와 반대로 다시 읽어들이는 것

역직렬화는 외부로 내보낸 직렬화 데이터를 다시 읽어들여 자바 객체로 재변환하는 것을 말한다.

**Serializable** 인터페이스를 구현하면 클래스의 인스턴스를 직렬화와 역직렬화에 사용할 수 있다.
하지만 역직렬화 과정에서 인스턴스를 새로 만들고, 직렬화에 사용한 인스턴스와는 전혀 다른 인스턴스가 되기 때문에 싱글턴이 깨진다.

모든 필드 변수를 **transient**로 선언하고, **readResolve() 메서드**를 제공해야 역직렬화를 할 때 두 번째 인스턴스가 생성되지 않는다.

```java
    public class Singleton implements Serializable {
    
    transient String str = "";
    transient ArrayList list = new ArrayList<>();
    
    private Singleton() {
    }
    
    private static class SingletonHolder {
        private static final Singleton INSTANCE = new Singleton();
    }
    
    public static Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    // readResolve 메서드
    private Object readResolve() {
        return INSTANCE;
    }
}

```

transient로 인스턴스 필드를 설정하면, 직렬화에서 제외된다.
readResolve() 메서드를 사용하면 기존에 역직렬화를 통해 새로 생성된 객체는 가비지 컬렉터 대상이 된다.

### 3. 열거 타입 방식의 깅글턴

대부분 상황에서는 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법이다.
(단, Enum 클래스가 다른 클래스나 인터페이스를 extends 할 경우 사용할 수 없다.)

```java
public enum Singleton {
    INSTANCE;
}

```

열거 타입 방식으로 싱글턴을 구현하면, 직렬화와 리플렉션 공격에도 안전하다.

+ **리플렉션**이란?

객체를 통해 클래스의 정보를 분석하여 런타임에 클래스의 동작을 조작하는 프로그램 기법

클래스 파일의 위치나 이름만 있다면 클래스의 정보를 통해 객체를 생성할 수 있다.

#### Reference
[싱글톤 객체가 깨져버리는 경우(역직렬화/리플렉션)](https://inpa.tistory.com/entry/JAVA-%E2%98%95-%EC%8B%B1%EA%B8%80%ED%86%A4-%EA%B0%9D%EC%B2%B4-%EA%B9%A8%EB%9C%A8%EB%A6%AC%EB%8A%94-%EB%B0%A9%EB%B2%95-%EC%97%AD%EC%A7%81%EB%A0%AC%ED%99%94-%EB%A6%AC%ED%94%8C%EB%A0%89%EC%85%98)
[자바에서 싱글톤 패턴을 적용하는 방법](https://olrlobt.tistory.com/72)