# 아이템 24. 멤버 클래스는 되도록 static으로 만들어라

## ✔️ 멤버 클래스(Member Class)란?
> 클래스 내부에 정의된 클래스, 인스턴스 변수나 메서드처럼 해당 클래스에 속해있다.

<hr>

## ✔️중첩 클래스(Nested Class)란?
> 다른 클래스 안에 정의된 클래스

중첩 클래스는 **자신을 감싹 바깥 클래스에서만** 사용되어야 하며, 그 외에 쓰임이 있다면 톱레벨 클래스로 만들어야 한다.

```java
public class Outer{ // 바깥 클래스
    public class Nested { // 중첩 클래스
    }
}
```

### 중첩 클래스의 종류

- 정적 멤버 클래스
- (비정적) 멤버 클래스
- 익명 클래스
- 지역 클래스

| **종류**               | **static 여부** | **외부 멤버 접근성**                       | **사용 가능 범위**        |
|------------------------|----------------|-----------------------------------------|-------------------------|
| **정적 멤버 클래스**   | `static`       | - 외부 클래스의 `static` 멤버에 바로 접근 가능<br>- 외부 클래스의 `private` 멤버에도 접근 가능 (인스턴스 필요 시 생성) | 외부 클래스와 독립적으로 사용 가능 |
| **비정적 멤버 클래스** | 비`static`     | - 외부 클래스의 모든 멤버에 접근 가능<br>- 외부 클래스의 `private` 멤버 접근 가능 | 외부 클래스의 인스턴스 필요 |
| **익명 클래스**        | 비`static`     | - 외부 클래스의 모든 멤버에 접근 가능<br>- 외부 클래스의 `private` 멤버 접근 가능 | 단일 사용 목적 (즉석 정의) |
| **지역 클래스**        | 비`static`     | - 외부 클래스의 모든 멤버에 접근 가능<br>- 메서드의 `final` 지역 변수 접근 가능 | 선언된 메서드 내부에서만 사용 가능 |

정적 멤버 클래스를 제외한 나머지는 **내부 클래스(inner class)** 라고 한다.

<hr>

## ✔️ 정적 멤버 클래스
> 클래스 내부에 **static** 으로 선언된 클래스

- **일반 클래스와의 차이점** : 다른 클래스 안에 선언되고, 바깥 클래스의 private 멤버에 접근할 수 있다.
- private으로 선언 시 바깥 클래스에서만 접근 가능하다.

```java
public class Calculator {
    
    //Calculator 클래스의 정적 멤버 클래스 Operation
    public static enum Operation {
        PLUS("+") {
            @Override
            public double apply(double x, double y) {
                return x + y;
            }
        },
        MINUS("-") {
            @Override
            public double apply(double x, double y) {
                return x - y;
            }
        },
        TIMES("*") {
            @Override
            public double apply(double x, double y) {
                return x * y;
            }
        },
        DIVIDE("/") {
            @Override
            public double apply(double x, double y) {
                if (y == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return x / y;
            }
        };
        
        private final String symbol;
        
        Operation(String symbol) {
            this.symbol = symbol;
        }
        
        @Override
        public String toString() {
            return symbol;
        }
        
        public abstract double apply(double x, double y);
    }

    public static void main(String[] args) {
        double x = 10.0;
        double y = 2.0;
        
        //Calculator.Operation을 통해 열거 타입에 접근
        System.out.printf("%f %s %f = %f%n", x, Calculator.Operation.PLUS, y, Calculator.Operation.PLUS.apply(x, y));
    }
}
```

### 사용

정적 멤버 클래스는 바깥 클래스와 함께 쓰일 때만 **유용한 public 도우미 클래스**로 쓰인다.

- **인스턴스 없이 사용 가능** : 정적 멤버 클래스는 <u>바깥 클래스의 인스턴스를 생성하지 않고도 독립적으로 사용할 수 있다.</u>
    - 위 코드에서 볼 수 있듯이, Calculator와 관련된 **수학적 연산**을 정적 멤버 클래스로 구현하면, Calculator 객체 없이도 **연산만 필요할 때 바로 호출해서 쓸 수 있다.**
- **주로 유틸리티 역할** : 바깥 클래스와 관련된 **헬퍼 클래스, 도우미 클래스**로 주로 사용된다. ex) Calculator 클래스의 Operation 열거형

<hr>

## ✔️ 비정적 멤버 클래스
> static이 붙지 않은 멤버 클래스

- 비정적 멤버 클래스의 인스턴스는 **바깥 클래스의 인스턴스와 연결**된다.
    - <u>바깥 클래스 인스턴스를 먼저 생성하고, 그 객체를 통해 비정적 멤버 클래스 객체를 생성할 수 있다.</u>
- - 비정적 멤버 클래스의 인스턴스와 바깥 인스턴스 사이의 관계는 멤버 클래스가 인스턴스화될 때 확립되며, 더 이상 변경할 수 없다.
- 참조 정보를 저장하려면 시간과 공간이 소비된다.

### 사용

- **어댑터**를 정의할 때 자주 쓰인다.
- Map 인터페이스 구현체
    - `keySet()`, `entrySet()`, `values()`가 반환하는 자신의 컬렉션 뷰를 구현할 때 활용된다.

```java
public class HashMap<K, V> extends AbstractMap<K, V>
        implements Map<K, V>, Cloneable, Serializable {

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        // size(), clear(), contains(), remove(), ...
    }

    final class KeySet extends AbstractSet<K> {
        // size(), clear(), contains(), remove(), ...
    }

    final class Values extends AbstractCollection<V> {
        // size(), clear(), contains(), remove(), ...
    }
}
```

#### 어뎁터란?
> 서로 호환되지 않는 인터페이스를 가진 클래스들이 함께 작업할 수 있도록 도와주는 디자인 패턴

```java
// 기존 시스템의 인터페이스 (구형)
interface OldSystem {
    void oldMethod();
}

// 새로운 시스템의 인터페이스 (신형)
interface NewSystem {
    void newMethod();
}

// 구형 시스템의 실제 구현
class OldSystemImpl implements OldSystem {
    @Override
    public void oldMethod() {
        System.out.println("Old system method executed.");
    }
}

class AdapterExample {
    private OldSystem oldSystem;

    public AdapterExample(OldSystem oldSystem) {
        this.oldSystem = oldSystem;
    }

    // 비정적 멤버 클래스
    class Adapter implements NewSystem {
        @Override
        public void newMethod() {
            oldSystem.oldMethod(); // 구형 시스템의 메서드 호출
        }
    }

    public NewSystem getAdapter() {
        return new Adapter();  // 비정적 멤버 클래스 객체 반환
    }
}
```

### 정적 멤버 클래스와의 차이점

정적 멤버 클래스와 비정적 멤버 클래스의 구문 상 차이는 단지 static이 붙어 있고 없고 뿐이지만, 의미상 차이는 의외로 크다.

<mark style="background-color: #FFF9C4">멤버 클래스에서 바깥 인스턴스에 접근하는 일이 없다면(독립적으로 존재한다면) 무조건 **static**을 붙여서 **정적 멤버 클래스**로 만들어야 한다</mark>

static을 생략하면 바깥 인스턴스로의 숨은 외부 참조를 갖게 된다.

참조가 눈에 보이지 않기 때문에 개발이 어렵고, GC가 외부 인스턴스를 수거하지 못하는 **메모리 누수**가 생길 수 있다. (아이템 7)

<hr>

## ✔️ 익명 클래스
> 이름이 없는 클래스, 주로 단일 인스턴스 생성에 사용된다.

- 바깥 클래스의 멤버가 아니다.
- 쓰이는 시점에 선언과 동시에 인스턴스가 만들어진다.

```java
public class Main {
    public static void main(String[] args) {
        // 익명 클래스 사용 예
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("익명 클래스 실행!");
            }
        };

        // 스레드에 전달하여 실행
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
```

### 제약 사항

- 선언한 지점에서만 인스턴스를 만들 수 있다.
- 이름이 없기 때문에 **instanceof** 검사나 클래스의 이름이 필요한 작업은 수행할 수 없다.
- 여러 인터페이스를 구현할 수 없다.
- 인터페이스 구현 X
- 다른 클래스 상속 X
- 짧지 않으면 가독성이 떨어진다.

### 사용
- 즉석에서 작은 함수 객체나 처리 객체를 만드는 데 사용
    - 람다 등장 이후 람다가 대체
- **정적 팩터리 메서드**를 구현할 때 사용

```java
interface Greeting {
    void sayHello();
}

public class GreetingFactory {
    // 정적 팩토리 메서드
    public static Greeting createGreeting() {
        // 익명 클래스를 사용하여 Greeting 인터페이스 구현
        return new Greeting() {
            @Override
            public void sayHello() {
                System.out.println("안녕하세요!");
            }
        };
    }
}
```

<hr>

## ✔️ 지역 클래스
> 메서드 내부에 선언된 클래스로, 선언된 메서드 내에서만 사용할 수 있다.

네 가지 중첩 클래스 중 가장 드물게 사용된다.

- 지역변수를 선언할 수 있는 곳이면 어디서든 선언할 수 있다.
- 멤버 클래스처럼 이름이 있고 반복해서 사용할 수 있다.
- 익명 클래스처럼 비정적 문맥에서 사용될 때만 바깥 인스턴스를 참조할 수 있다.
- 정적 멤버는 가질 수 없으며 가독성을 위해 짧게 작성해야 한다.

```java
public class Outer {
    private String outerField = "외부 클래스의 필드";

    public void method() {
        // 지역 클래스 정의
        class LocalClass {
            public void print() {
                // 바깥 클래스의 인스턴스 변수인 outerField에 접근 가능
                System.out.println(outerField);  // 외부 클래스의 인스턴스 변수를 참조
            }
        }

        // 지역 클래스 사용
        LocalClass localClass = new LocalClass();
        localClass.print();  // 출력: 외부 클래스의 필드
    }
}
```
<hr>

## 🔗 Reference

[effective-java-study 아이템 24 (1)](https://github.com/woowacourse-study/2022-effective-java/blob/main/04%EC%9E%A5/%EC%95%84%EC%9D%B4%ED%85%9C_24/%EB%A9%A4%EB%B2%84%20%ED%81%B4%EB%9E%98%EC%8A%A4%EB%8A%94%20%EB%90%98%EB%8F%84%EB%A1%9D%20static%EC%9C%BC%EB%A1%9C%20%EB%A7%8C%EB%93%A4%EC%96%B4%EB%9D%BC.md)

[effective-java-study 아이템 24 (2)](https://github.com/woowacourse-study/2024-effective-java/blob/main/04%EC%9E%A5/%EC%95%84%EC%9D%B4%ED%85%9C_24/%EB%A9%A4%EB%B2%84_%ED%81%B4%EB%9E%98%EC%8A%A4%EB%8A%94_%EB%90%98%EB%8F%84%EB%A1%9D_static%EC%9C%BC%EB%A1%9C_%EB%A7%8C%EB%93%A4%EB%9D%BC_%ED%98%B8%ED%8B%B0.md)

