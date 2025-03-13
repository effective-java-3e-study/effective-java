## item 13

### clone 재정의는 주의해서 진행하라

---

### 왜 주의해야 하나요?

`Cloneable`은 복제해도 되는 클래스임을 명시하는 용도인 `믹스인 인터페이스`이지만 의도한 목적을 3가지의 측면에서 제대로 이루지 못했습니다.

- `clone` 메서드가 선언된 곳이 `Cloneable`이 아닌 `Object`
- `protected` 선언

따라서 그냥 구현하는 것만으로는 외부 객체에서 clone 메서드 호출이 불가능합니다.

### Cloneable 인터페이스의 하는 일

`Object`의 `protected` 메서드인 `clone`의 동작 방식을 결정합니다.

- `Cloneable` 인터페이스를 구현한 클래스라면, 객체의 필드 하나하나를 복사한 객체 반환
- `Cloneable` 인터페이스를 구현하지 않았다면, `CloneNotSupportedException` 예외 던짐

좋지는 않은 인터페이스 구현 방식입니다.
> 인터페이스를 구현한다는 의미인 "클래스가 그 인터페이스에서 정의한 기능을 제공한다"는 의미와 다르게 상위 클래스에 정의된 protected 메서드의 동작 방식을 변경하고 있기 때문입니다.

### 그럼에도 불구하고 잘 동작하게 하는 방법?

그냥 구현하고 메서드를 `public` 으로 변경하면 되지 않나요?

#### clone 메서드의 허점
이를 위해선 해당 클래스와 모든 상위 클래스는 좋지 않은 인터페이스 구현 방식에 따른 단점(클래스의 복잡성, 인터페이스의 강제성 x, 허술한 프로토콜 수호)을 감내하려다가 `생성자를 호출하지 않고도 객체를 생성할 수 있게` 되는 모순적이고 위험한 매커니즘을 탄생시킬 수 있습니다.

<details> <summary> ✅ Object 명세 중 clone 메서드 </summary>

- `x.clone() != x` 는 참
- `x.clone().equals(x)` 일반적으로 참, 필수는 X
- `x.clone().getClass() == x.getClass()` 모든 상위 클래스가 반환 객체와 원본 객체는 독립적이어야 한다는 관례를 따른다면 참
</details>

위 명세 중 단점은 `super.clone`이 아닌 `생성자를 호출해 얻은 인스턴스를 반환해도 컴파일러가 불평하지 않을 것이라는 허점`이 있다는 점이다.

이 허점은 결국 하위 클래스에서 `super.clone`을 호출 시, `잘못된 클래스 객체가 만들어져 동작에 이상이 생기는 문제`를 낳는다.

그럼 어떻게 해야 하나요?

#### 🙌 방법 ️ 1. 가변 객체가 아닌 경우

- clone을 재정의한 클래스가 final 이라면 문제되지 X
- final 메서드의 clone 메서드가 super.clone을 호출하지 않는다면 Cloneable을 구현할 필요 X
- 모든 필드가 기본 타입, 불변 객체 참조 시 아래와 같이 구현해주면 됨
```java
   @Override public PhoneNumber clone() {
        try {
            return (PhoneNumber) super.clone(); // 형변환을 통해 하위 타입 반환 가능함
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();  // 일어날 수 없는 일 > PhoneNumber가 clone을 구현하고 있기에 > 거추장스러운 코드임 > `CloneNotSupportedException`비검사 예외였어야 함
        }
    }
```

#### 🙌 방법 ️ 2. 가변 객체인 경우

`field`로 배열을 갖고 있는 경우, 원본 배열과 똑같은 주소를 참조하고 있는 문제를 발생시킨다.
즉, 복사된 객체 변동을 줬는데 원본 객체도 함게 변동되는 원치 않는 일이 일어나는 것이다.
이는 프로그램 동작 이상, NullPointerException 예외를 던지는 문제가 일어난다.

따라서 우리는 해당 클래스의 하나뿐인 생성자를 호출하여 이러한 상황을 막아야 한다.
clone 메서드는 사실상 생성자와 같은 효과를 내기에 원본 객체와 별개로 독립되어 객체의 불변성을 보장해야 한다.

##### 배열의 경우, 재귀적 호출 사용
```java
    @Override public Stack clone() {
        try {
            Stack result = (Stack) super.clone();
            result.elements = elements.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
```
배열이 아닌 경우 재귀적 호출 사용만으로는 충분하지 않을 수 있다. 깊은 복사가 이루어지지 않는 문제가 발생될 수 있기 때문이다.

##### 배열이 아닌 경우, 깊은 복사 구현 및 사용

```java
Entry deepCopy(){
    return new Entry(key, value, next == null? null : next.deepCopy());
}
```
이렇게 메서드를 구현하여 재귀적인 호출을 하여 깊은 복사를 진행할 수도 있다. 그러나, 이 방식은 리스트의 원소 수만큼 스택 프레임을 소비하기에 리스트가 길면 스택 오버플로를 일으킬 위험이 있다.
해당 문제를 피하려면 deepCopy 재귀 호출 대신 `반복자`를 사용해서 순회하는 방향으로 가는 것이 좋다.

```java
Entry deepCopy(){
    Entry result = new Entry(key, value, next);
    for(entry p = result; p.next != null; p = p.next){
        p.next = new Entry(p.next.key, p.next.value, p.next.next);
    }
    return result;
}
```
##### 배열이 아닌 경우, 원본 객체의 상태를 다시 생성하는 고수준 메서드 호출
1) super.clone 을 호출하여 얻은 객체의 모든 필드를 초기 상태로 설정
2) 원본 객체의 상태를 다시 생성하는 고수준 메서드 호출

예를 들어, 버킷 배열 초기화 후, 원본 배열의 모든 키-값 쌍 각각에 대해 복제본 테이블의 put(key, value) 메서드를 호출하여 내용의 동일성 보장해주는 것

> 좋은 방식은 아니다. 생성자에서는 재정의될 수 있는 메서드를 호출하지 않아야 하는데 clone 메서드도 마찬가지이기 때문이다.

### 주의점
- 상속용 클래스의 경우 Cloneable을 구현해서는 안됨
- 스레드 안전 클래스 작성 시 Cloneable을 구현하는 모든 클래스는 clone을 재정의해야 함(접근 제한자는 public, 반환 타입은 클래스 자신)

### 가능한 다른 선택지! 변환 생성자/ 변환 팩터리

이미 Cloneable을 구현한 클래스를 확장하는 경우가 아니라면, `복사 생성자와 복사 팩터리`라는 더 나은 객체 복사 방식을 제공할 수 있습니다.
이는 인터페이스 타입을 인수로 받아서 구현 타입에 얽매이지 않고 복제본의 타입을 적절히 선택할 수 있다는 장점도 있습니다.

