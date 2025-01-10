10장 정리.
블로그로 먼저 정리하고 -> md 파일로 옮기다보니 가독성이 블로그가 더 좋습니다.
### 블로그 링크: https://mn040820.tistory.com/136


Object에서 final이 아닌 메서드는 모두 재정의를 염두에 두고 설계된 것이라 재정의 시 지켜야 하는 일반 규약이 명확히 정의되어 있다.


equals() 는 재정의가 쉬워보이지만 함정이 도사리고 있다.. 회피하는 길은 아예 재정의를 하지 않는 것이다. 그러면 오직 본인 자신과만 같게 된다. 왜냐하면 Object의 equals()는 주소값을 비교하기 때문이다.

```java
Object.class
public boolean equals(Object obj) {
   return (this == obj);
}
```

다음과 같은 상황 중 하나에 해당하면 재정의 안하는 것이 좋다.

#### 1. 각 인스턴스가 본질적으로 고유함. -> ex)Thread처럼 값을 표현하는 것이 아닌 동작하는 개체를 표현하는 클래스. -> 동작하는 개체는 고유할 수 밖에 없지. 예를 들어 사람은 고유하잖아. 하지만 값(ex 숫자)는 고유하지 않지.
#### 2. 인스턴스의 논리적 동치성을 검사할 일이 없다. -> 값이 같은지 검사 안해도 된다.
#### 3. 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다. -> ex. 컬렉션 Set, List, Map은 Abstract~ 상속함
#### 4. 클래스가 private이거나 package-private(default)이고 equals를 호출할 일이 없다.

equals()를 재정의해야할 때 = 논리적 동치성을 확인해야 할 때 + 상위 클래스의 equals가 논리적 동치성을 비교하도록 재정의되지 않았을 때. => 주로 값 클래스들.



equals()를 재정의해서 논리적 동치성을 확인할 수 있으면 -> 값을 비교할 수 있다는 것이고 -> 이는 중복을 제거할 수 있다는 말이므로 -> Map의 키와 Set의 원소로 사용 가능하다.



값 클래스라 해도, Enum같이 값이 같은 인스턴스가 없다는 것이 보장되면 equals()를 재정의하지 않아도 된다. -> Object의 equals()가 논리적 동치성까지 확인해줌.



equals()를 재정의할 때는 반드시 일반 규약을 따라야 한다. - Object 클래스의 일반 규약임.

모두 null이 아닌 모든 참조값에 대해,
### 1.1 반사성
- `x.equals(x)`는 true다.

### 1.2 대칭성
- `x.equals(y)`가 true면 `y.equals(x)`도 true다.

### 1.3 추이성
- `x.equals(y)`가 true고 `y.equals(z)`도 true면, `x.equals(z)`도 true다.

### 1.4 일관성
- `x.equals(y)`를 반복해서 호출하면 항상 true를 반환하거나 항상 false를 반환한다.

### 1.5 null-아님
- `x.equals(null)`은 false다.

세상에 홀로 존재하는 클래스는 없다. 수많은 클래스는 전달받는 객체가 equals 규약을 지킨다고 가정하고 동작한다. 그래서 일반 규약을 어기면 큰일난다 !!

---

## 2. 동치관계란?

- 집합을 서로 같은 원소들로 이루어진 부분집합으로 나누는 연산이다.
- 이 부분집합을 동치류(동치 클래스)라고 한다.
- equals()가 쓸모 있으려면 모든 원소가 같은 동치류에 속한 어떤 원소와도 서로 교환할 수 있어야 한다.

---

## 3. 규약 설명

### 3.1 반사성
- 객체는 자기 자신과 같아야 한다.

### 3.2 대칭성
- 두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다.
- 어기는 예시: 대소문자를 구별하지 않는 문자열 비교.
- equals() 내부에 본인 객체가 아닌 다른 클래스(ex. String)과 비교 로직이 있다면 대칭성에 위반할 수 있다.
- 규약은 본인 객체와 비교하는 것임. 규약을 어기면 그 객체를 사용하는 다른 객체들이 어떻게 반응할지 알 수 없다.

### 3.3 추이성
- 구체 클래스를 확장해 새로운 값을 추가하면서 equals 규약을 만족시킬 방법은 존재하지 않는다. (Point, ColorPoint 예시)
- `instanceof` 대신 `getClass`로 하면 리스코프 치환 원칙에 위배된다. 상위 타입을 받지 못하기 때문이다.
- 상속 대신 컴포지션을 사용하면 우회할 수 있다.
- 추상 클래스의 하위 클래스에서라면 equals 규약을 지키면서도 값을 추가할 수 있다. 상위 클래스를 직접 인스턴스화하는 게 불가능하면 지금까지 이야기한 문제들은 발생하지 않는다.

### 3.4 일관성
- 클래스가 불변이든 가변이든 equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안 된다.
- `java.net.URL`의 equals는 네트워크를 통해야 하기 때문에 그 결과가 항상 같다고 보장할 수 없다.
- 이를 피하려면 equals는 항시 메모리에 존재하는 객체만을 사용한 결정적 계산만 수행해야 한다.

### 3.5 null-아님
- 보통 null 검사를 통해 null 체크를 하는데, equals는 형변환을 진행하기 앞서 `instanceof` 연산자로 입력 매개변수의 타입을 검사하기 때문에 묵시적 null 검사가 가능하다.
- `instanceof`는 첫 번째 피연산자가 null이면 false를 반환한다. 따라서 입력이 null이면 false를 반환한다.
- `if (!(o instanceof MyType)) return false;`

---

## 4. 양질의 equals 메서드 구현 방법 단계

### 1. `==` 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.
- 단순한 성능 최적화용이다.

### 2. `instanceof` 연산자로 입력이 올바른 타입인지 확인한다.
- 그 입력은 인터페이스가 될 수도 있다.
- 어떤 인터페이스는 자신을 구현한 (서로 다른) 클래스끼리도 비교할 수 있도록 equals 규약을 수정하기도 한다.
- 이런 인터페이스를 구현한 클래스라면 equals에서 해당 인터페이스를 사용해야 한다.
- 컬렉션 인터페이스들이 이에 해당한다.

### 3. 입력을 올바른 타입으로 형변환한다.
- 2번으로 인해 100% 성공.

### 4. 입력 객체와 자기 자신의 대응되는 '핵심' 필드들이 모두 일치하는지 하나씩 검사한다.
- 만약 2단계에서 인터페이스를 사용했다면, 필드값을 가져올 때도 그 인터페이스의 메서드를 사용해야 한다.

### 4.1 기본 타입 필드 비교
- `float`, `double`을 제외한 기본 타입 필드는 `==`를 사용.
- 참조 타입 필드는 `equals()`로 비교.
- `float`과 `double`은 각각 정적 메서드인 `Float.compare()`와 `Double.compare()`로 비교.
  - 특별 대우하는 이유는 부동소수를 다뤄야 하기 때문이다.

### 4.2 배열 필드 비교
- 배열 필드는 원소 각각을 앞서의 지침대로 비교한다.
- 배열의 모든 원소가 핵심 필드라면 `Arrays.equals` 메서드들 중 하나를 사용하자.

### 4.3 복잡한 필드 비교
- 비교하기 아주 복잡한 필드를 가지고 있다면 필드의 **표준형**을 저장해둔 후 표준형끼리 비교하면 경제적이다.
- 가변 객체라면 값이 바뀔 때마다 표준형을 최신 상태로 갱신해주자.

### 4.4 성능 최적화
- 최상의 성능을 바란다면 다를 가능성이 더 크거나 비교하는 비용이 싼(혹은 둘 다) 필드를 먼저 비교하자.
- 동기화용 락 필드 같이 객체의 논리적 상태와 관련 없는 필드는 비교하면 안 된다.

### 4.5 파생 필드 비교
- 파생 필드가 객체 전체의 상태를 대표하는 상황이면 파생 필드를 비교하는 쪽이 더 빠를 때도 있다.


- equals()를 다 구현했다면 세 가지만 자문하자. 대칭적인가? 추이성이 있는가? 일관적인가?



*마지막 주의사항

equals를 재정의할 땐 hashcode도 반드시 재정의하자.
너무 복잡하게 해결하려 들지 말자. 일반적으로 별칭은 비교하지 않는게 좋다.
Object 외의 타입을 매개변수로 받는 equals 메서드는 선언하지 말자. 오버라이딩 실패한다. @Override 붙이자. 컴파일 시에 잡아준다. 이 다중 정의를 하위클래스가 또 오버라이딩하면 거짓양성을 낼 수 있다.


예제 코드

대칭성 위배
```java
public class EqualsMain {
    public static void main(String[] args) {
        Rectangle rectangle = new Rectangle(2, 5);
        Square square = new Square(2, 5, 10);
        System.out.println("rectangle.equals(square): " + rectangle.equals(square)); -> true
        System.out.println("square.equals(rectangle): " + square.equals(rectangle)); -> false
    }
}

public class Rectangle {
    private final int width;
    private final int height;

    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rectangle)) return false;
        Rectangle rectangle = (Rectangle) o;
        return width == rectangle.width && height == rectangle.height;
    }
}

public class Square extends Rectangle {

    private final int area;

    public Square(int width, int height, int area) {
        super(width, height);
        this.area = area;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Square)) return false;
        return super.equals(o) && area == ((Square) o).area;
    }
}

```


추이성 위배
```java
public static void main(String[] args) {
//추이성 위반
    Square p1 = new Square(2, 5, 10);
    Rectangle p2 = new Rectangle(2, 5);
    Square p3 = new Square(2, 5, 40);
    System.out.println("p1.equals(p2): " + p1.equals(p2));
    System.out.println("p2.equals(p3): " + p2.equals(p3));
    System.out.println("p1.equals(p3): " + p1.equals(p3));
}


public class Rectangle {
    private final int width;
    private final int height;

    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rectangle)) return false;
        Rectangle rectangle = (Rectangle) o;
        return width == rectangle.width && height == rectangle.height;
    }
}

public class Square extends Rectangle {

    private final int area;

    public Square(int width, int height, int area) {
        super(width, height);
        this.area = area;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rectangle)) return false;
        if (!(o instanceof Square)) {
            return o.equals(this);
        }
        return super.equals(o) && area == ((Square) o).area;
    }
}


----결과----
p1.equals(p2): true
p2.equals(p3): true
p1.equals(p3): false
```



리스코프 치환 원칙 위배
```java
//이 코드는 입력값과 현재 클래스가 동일한 클래스여야 성립한다.
//인텔리제이로 equals 자동 생성하면 나오는 equals()이다.
@Override
public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Rectangle rectangle = (Rectangle) o;
    return width == rectangle.width && height == rectangle.height;
}
```
대부분의 Collection에서는 contains() 메서드 판별에 equals()를 이용하는데, 위처럼 equals를 구성하고 입력값으로 하위 클래스를 넘기면 false가 나온다. 그래서 리스코프 치환 원칙에 위배한다. 만약 getClass()가 아닌 instanceof로 구현했다면 제대로 동작할 것이다.




문제 해결 코드
```java
//상속보단 컴포지션을 이용해서 equals() 구현
public class Square {
    private final Rectangle rectangle;
    private final Area area;

    public Square(int width, int height, Area area) {
        rectangle = new Rectangle(width, height);
        this.area = Objects.requireNonNull(area);
    }

    public Rectangle asRectangle() {
        return this.rectangle;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Square)) return false;
        Square square = (Square) o;
        return square.rectangle.equals(this.rectangle) && square.area.equals(this.area);
    }
}
```
