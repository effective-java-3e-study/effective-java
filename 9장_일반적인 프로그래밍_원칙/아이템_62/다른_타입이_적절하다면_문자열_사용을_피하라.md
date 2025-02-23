# 다른 타입이 적절하다면 문자열 사용을 피하라

## 문자열(String)은 텍스트를 표현하도록 설계되었다.

자바에서 문자열은 텍스트를 표현하도록 설계되었지만,
원래 의도하지 않은 용도로도 쓰이는 경향이 있다.
이번 주제에서는 문자열을 쓰지 않아야 할 사례를 다룬다.

## 문자열은 다른 값 타입을 대신하기에 적합하지 않다.

많은 개발자들은 다양한 타입의 데이터를 받을 때 주로 문자열을 사용한다.
하지만, 진짜 문자열일 때만 사용하는 것이 좋다.
받은 데이터가 수치형이라면 int, float, BigInteger 등 적당한 타입을 사용해야 하고,
예/아니오와 같은 질문의 답이라면 boolean을 사용해야 한다.
즉, 기본 타입이든 참조 타입이든 적절한 값 타입이 있다면 그것을 사용하고, 없다면 새로 만들어 사용하는 것이 좋다.

## 문자열은 열거 타입을 대신하기에 적합하지 않다.

아이템 34에서 다뤘듯이, 상수를 열거할 때는 문자열보다는 열거 타입이 월등히 낫다.
열거 타입은 상수 데이터를 컴파일 타임에 알 수 있고, 타입 안전성을 제공하며, 이름을 프로그램 요소로 활용할 수 있다.
하지만 상수를 문자열로 작성하는 경우 다음과 같이 예상치 못한 결과가 발생할 수 있다.

```java
public String getFileExtension(String fileName) {
    switch (fileName) {
        case "png":
            return "PNG";
        ...
        default:
            return "UNKNOWN";
    }
}

getFileExtension("PNG"); // UNKNOWN
getFileExtension("Png"); // UNKNOWN
```

따라서 이런 상황에서는 문자열 대신 열거 타입을 정의해서 사용하는 것이 좋다.

```java
public enum FileType {
    PNG, JPG, GIF, UNKNOWN;
}

public FileType getFileExtension(FileType fileType) {
    switch (fileType) {
        case PNG:
            return FileType.PNG;
        ...
        default:
            return FileType.UNKNOWN;
    }
}

getFileExtension(png); // 컴파일 에러 발생
```

## 문자열은 혼합 타입을 대신하기에 적합하지 않다.

여러 요소가 혼합된 데이터를 하나의 문자열로 표현하는 경우도 있다.
```java
String compoundKey = className + "#" + i.next();
```
이런 경우에는 각 요소를 개별로 접근할 수 없고, 문자열을 파싱해야 한다.
이때 파싱 과정에서 느리고, 귀찮고, 오류 가능성도 커진다.
그리고 `equals`, `toString`, `compareTo` 같은 메서드를 제공할 수 없고, `String`이 제공하는 기능에만 의존해야 한다.
그래서 차라리 [아이템 24](https://github.com/effective-java-3e-study/effective-java/blob/main/4%EC%9E%A5_%ED%81%B4%EB%9E%98%EC%8A%A4%EC%99%80_%EC%9D%B8%ED%84%B0%ED%8E%98%EC%9D%B4%EC%8A%A4/%EC%95%84%EC%9D%B4%ED%85%9C_24/%EB%A9%A4%EB%B2%84_%ED%81%B4%EB%9E%98%EC%8A%A4%EB%8A%94_%EB%90%98%EB%8F%84%EB%A1%9D_static%EC%9C%BC%EB%A1%9C_%EB%A7%8C%EB%93%A4%EB%9D%BC.md)에서 언급했듯이, `private` 정적 멤버 클래스로 만드는 것이 좋다.

## 문자열은 권한을 표현하기에 적합하지 않다.

권한(capacity)을 문자열로 표현하는 경우가 있다.
여기 `ThreadLocal` 클래스에서 문자열을 사용했다고 가정해본다.

```java
public class ThreadLocal {
    private ThreadLoacl() {} // 객체 생성 불가
    
    // 현 스레드의 값을 키로 구분해 저장한다.
    public static void set(String key, Object value);
    
    // (키가 가리키는) 현 스레드의 값을 반환한다.
    public static Object get(String key);
}
```

이 방식의 문제는 스레드 구분용 키가 전역 이름공간에서 공유 된다는 점이다.
만약, 두 클라이언트가 서로 소통하지 못해 같은 키를 쓰기로 결정한다면, 의도치 않게 같은 변수를 공유하게 된다.
만일 상대가 악의적인 클라이언트라면 의도적으로 같은 키를 사용하여 다른 클라이언트 값을 가져올 수도 있다.
따라서 문자열 대신 위조할 수 없는 키를 사용해야 한다.

```java
public class ThreadLocal {
    private ThreadLocal() { } // 객체 생성 불가
    
    public static class key { // ((권한)
        Key() { }
    }
    
    // 위조 불가능한 고유 키를 생성한다.
    public static Key getKey() {
        return new Key();
    }
    
    public static void set(Key key, Object value);
    public static Object get(Key key);
}
```

여기서 `set`과 `get`은 이제 정적 메서드일 이유가 없으므로 `Key` 클래스의 인스턴스 메서드로 바꿀 수 있다.
이렇게 하면 `Key`는 더 이상 스레드 지역변수를 구분하기 위한 키가 아니라, 그 자체가 스레드 지역변수가 된다.

```java
public final class ThreadLocal<T> {
    public ThreadLocal();
    public void set(T value);
    public T get();
}
```

## 정리

이 말이 떠올라서 인용하겠다.
“만드는 사람이 수고로우면 쓰는 사람이 편하고, 만드는 사람이 편하면 쓰는 사람이 수고롭다.”
그냥 문자열을 쓰면 그때는 편할 수 있다. 하지만, 그것이 미래에 수고롭게 만들 수 있다.
만일 더 나은 데이터 타입이 있거나 새로 만들 수 있다면, 그것을 사용하고 만들어야 한다.
문자열은 잘못 사용하면 위에서 봤듯이 문제를 일으킬 수 있으므로, 주의해서 사용해야 한다.

