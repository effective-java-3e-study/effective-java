## item 89

### 인스턴스 수를 통제해야 한다면 readResolve보다는 열거 타입을 사용하라

---

### 🙋 readResolve 는 무엇인가요?

`readResolve`기능을 이용하면 `readObject`가 만들어낸 인스턴스를 다른 것으로 대체할 수 있습니다.
밑의 예시와 같은 싱글턴 패턴을 활용한다고 했을 때, `implements Serializable`을 추가하는 순간 싱글턴이 되지 않게 됩니다.
기본 직렬화를 쓰지 않더라도, 명시적인 `readObject`를 제공하더라도 소용이 없습니다.

```java
public class Elvis implements Serializable {
    public static final Elvis INSTANCE = new Elvis();
    
    private Elvis() {}
    
    private Object readResolve() {
        return INSTANCE; 
    }
}
```

하지만 역직렬화한 객체의 클래스가 `readResolve` 메서드를 적절히 정의해뒀다면, 역직렬화 후 새로 생성된 객체를 인수로 
이 메서드가 호출되고, 이 메서드가 반환한 객체 참조가 새로 생성된 객체를 대신해 반환합니다. 대부분의 경우 이때 새로 생성된 객체의 참조는 유지하지 않으므로 가비지 컬렉션 대상이 됩니다.

따라서 `implements Serializable`을 추가해도 `readResolve`를 활용한다면, 싱글턴이라는 속성을 유지할 수 있다는 것입니다. 
해당 메서드를 통해 역직렬화한 객체는 무시하고 클래스 초기화 때 만들어진 `Elvis` 인스턴스를 반환합니다.

이렇게 된다면, Elvis 인스턴스의 직렬화 형태는 아무런 실 데이터를 가질 이유가 없습니다.
따라서, 모든 인스턴스 필드를 `transient`로 선언해야 합니다. 

사실 `readResolve`를 인스턴스 통제 목적으로 사용한다면, 객체 참조 타입 인스턴스 필드는 모두 `trasient`로 선언해야 합니다.

---

### 🙋 왜 trasient로 선언해야 하나요?

그렇지 않으면 `MutablePeriod` 공격과 비슷한 방식으로 `readResolve`가 수행되기 전에 역직렬화된 객체의 참조를 공격할 여지가 남기 때문입니다.

<details><summary>✔ MutablePeriod 공격</summary>

`readResolve`를 사용하면 객체 자체는 교체되지만, 참조 필드는 역직렬화되면서 공격자가 조작할 기회를 얻게 된다는 개념으로 작동합니다.

- `readResolve`가 실행되기 전에 객체 필드가 먼저 역직렬화됨
- 공격자가 이 순간을 노려 필드 값을 조작하거나 객체 참조를 훔칠 수 있음

</details>

---

### 🙌 MutablePeriod 공격

### **0) 잘못된 싱글턴 - `transient`가 아닌 참조 필드를 갖고 있음**

```java
public class Elvis implements Serializable {
    public static final Elvis INSTANCE = new Elvis();
    private Elvis() {}
    
    private String[] favoriteSongs =
        { "Hound Dog", "Heartbreak Hotel" };
        
    public void printFavorites() {
        System.out.println(Arrays.toString(favoriteSongs));
    }
    
    private Object readResolve() {
        return INSTANCE;
    }
}
```
**문제점**: `favoriteSongs`가 `transient`가 아니므로 역직렬화 과정에서 
`readResolve` 실행 전에 공격자가 참조를 훔칠 수 있음

---

### **1) `readResolve` 메서드와 인스턴스 필드를 포함한 도둑 클래스 작성**
- 도둑 클래스는 직렬화된 싱글턴을 가로채는 역할을 수행
- 싱글턴이 역직렬화될 때 도둑의 `readResolve`가 먼저 실행되어 싱글턴의 참조를 저장할 수 있음

```java
public class ElvisStealer implements Serializable {
    static Elvis impersonator; // 역직렬화 후에도 참조됨
    private Elvis payload;

    private Object readResolve() {
        // 역직렬화된 Elvis 인스턴스의 참조를 훔침
        impersonator = payload;
        
        // Elvis의 favoriteSongs 타입(String[])과 일치하는 객체 반환
        // 이 과정 생략 시 직렬화 시스템이 도둑의 참조를 필드에 저장하려 할 때 VM 이 ClassCastException 던질 가능성 있음
        return new String[] { "A Fool Such as I" };
    }
    
    private static final long serialVersionUID = 0;
}
```
 **공격 방식**:
- `ElvisStealer`가 역직렬화될 때 `payload`에 원본 `Elvis` 인스턴스가 저장됨
- `readResolve()`가 실행되면서 `impersonator`에 `Elvis` 참조를 훔쳐 저장
- `impersonator`는 원래 `Elvis.INSTANCE`와 다른 독립적인 객체가 됨

---

### **2) 직렬화의 허점을 이용해 싱글턴 객체를 2개 생성**
- 싱글턴이 `ElvisStealer`를 포함하면, 역직렬화 과정에서 `ElvisStealer`의 `readResolve`가 먼저 실행됨
- 이 과정에서 도둑이 `Elvis`의 참조를 훔칠 수 있음

```java
public class ElvisImpersonator {
    // 직렬화된 Elvis 객체 (실제 실행 환경에서는 파일에서 로드 가능)
    private static final byte[] serializedForm = {
            -84, -19, 0, 5, 115, 114, 0, 20, 107, 114, 46, 115,
            ... // 생략된 직렬화 바이트 코드
            32, 72, 111, 116, 101, 108
    };

    public static void main(String[] args) {
        Elvis elvis = (Elvis) deserialize(serializedForm);
        Elvis impersonator = ElvisStealer.impersonator;

        elvis.printFavorites();
        impersonator.printFavorites();
    }

    private static Object deserialize(byte[] serializedData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```
**결과**:
1. `elvis.printFavorites();` → 원래의 `Elvis` 인스턴스
2. `impersonator.printFavorites();` → 가로챈 `Elvis` 인스턴스 (새로운 객체)

> **싱글턴이 깨지게 됩니다!**

---

### 🙋 그럼, transient로 선언만 하면 되는 거 아닌가요? 왜 열거 타입으로 바꾸는 게 나은가요?

```java
public enum Elvis {
    INSTANCE;
    private String[] favoriteSongs =
        { "Hound Dog", "Heartbreak Hotel" };
        
    public void printFavorites() {
        System.out.println(Arrays.toString(favoriteSongs));
    }
}
```
1) `readResolve` 메서드를 사용해 순간적으로 만들어진 역직렬화 인스턴스에 접근하지 못하게 하는 것은 깨지기 쉽고, 신경을 많이 써야함
2) 열거 타입을 사용해 구현하면 선언한 상수 외의 다른 객체는 존재하지 않음을 자바가 보장해 줌
   - 공격자가 `AccessibleObject.setAccessible` 같은 특권 메서드를 악용한다면 달라짐(모든 방어가 무력화되기 때문에)

---

### 🙌 readResolve 방식이 완전히 쓸데 없는 것은 아닙니다!

직렬화 가능 인스턴스 통제 클래스를 작성해야 하는데, `컴파일타임에는 어떤 인스턴스들이 있는 알 수 없는 상황` 이라면, 열거 타입으로 표현하는 것이 불가능하기 때문입니다.

#### 참고로 이때 메서드의 접근성을 잘 생각해서 구현해아합니다.
- `final` 클래스 > `private`
- `final`이 아닌 클래스 
  - `private`: 하위 클래스에서 사용 불가
  - `package-private`: 같은 패키지에 속한 하위 클래스에서만 사용 가능
  - `protected` / `public`: 재정의하지 않은 모든 하위 클래스에서 사용 가능
    - 주의점! 
    - 재정의하지 않았다면, 하위 클래스의 인스턴스를 역직렬화하면 상위 클래스의 인스턴스를 생성해 `ClassCastException` 발생 가능
