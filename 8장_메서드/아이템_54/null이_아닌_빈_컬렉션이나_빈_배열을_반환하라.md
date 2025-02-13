### 핵심 정리
- 배열 혹은 컬렉션을 반환하는 메서드를 구성할 때`null`을 반환하지 말고, 빈 배열 혹은 컬렉션을 반환하자.
- 이유
  1. 호출자 코드 단순화: 클라이언트가 null을 처리할 필요 없이 바로 컬렉션이나 배열 사용 가능
  2. NullPointer 예외 방지  
  3. 일관성 유지: 항상 같은 타입의 객체를 반환하므로 메서드 사용이 일관되고 예측가능


<br/>

### `null`을 반환하는 것의 단점
> 사용하는 클라이언트 측이 귀찮아진다.
```java
private final List<Cheese> cheesesInStock = new ArrayList<>();

public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? null
            : new ArrayList<>(cheesesInStock);
}

> 이 코드엔 무슨 문제가 있을까?
- 사용하는 클라이언트는 강제로 null 체크 의무
- 만약, 빈 리스트 반환했다면 null 처리 필요 x

@Test
public void getCheesesClient() {
    List<Cheese> cheeses = getCheeses();
    if(cheeses != null && cheeses.contains(Cheese.STILTON)) {
        System.out.println("좋았어, 바로 그거야.");
    }
}

```
<br/>

### `null`을 반환하는 것이 성능상 낫지 않은가?
- 물론 새로운 객체를 하나 만들지만 사실상 성능상의 커다란 손해가 없다. (미미하다.)
- 최적화 가능: 빈 컬렉션과 배열은 굳이 새로 할당하지 않고도 반환 가능
    - 빈 컬렉션을 불변으로 선언해놓고 같은 케이스에 계속 재활용해서 할당해도 된다.
    - `Collections.emptyList()`,`Collections.emptySet()`,`Collections.emptyMap()`도 있다.

<br/>

#### Collections.emptyList()`,`Collections.emptySet()`,`Collections.emptyMap()
1. 미리 정의된 불변 객체를 반환
  - 이 메서드들은 매번 새로운 객체를 생성하지 않고, 미리 정의된 불변 객체를 반환
  - Collections 클래스에 이미 정의된 불변의 빈 컬렉션 객체
    => 싱글톤 인스턴스, 메모리 효율, 수정 불가

2. 타입 안전성
    - 제네릭을 지원하여 컴파일 시점에 타입 체크가 가능
    - 반환 타입이 미리 정의된 불변 객체 타입이기 때문에 타입 안정성이 보장된다.
3. 명시적 의도
    - 명시적으로 빈 컬렉션을 반환한다는 의도를 나타낸다.

```java
// 타입 안정성
// 이는 개발자가 명시적으로 타입을 지정하지 않아도 되게 해주며, 코드의 가독성과 유지보수성을 향상
public List<String> getNames() {
    // 조건에 따라 빈 리스트 반환
    if (someCondition) {
        return Collections.emptyList(); // 컴파일러가 List<String>으로 추론
    }
    // ... 다른 로직
}

```


<br/>

### null을 반환하지 않는 실제적인 코드들

#### 1. 컬렉션 생성자 이용
```java
public List<Cheese> getCheeses2() {
    return new ArrayList<>(cheesesInStock);
}
```
- `ArrayList`의 생성자를 이용하는 방법인데, 이 방법은 사실 객체를 생성하는 방법이다.
- 사용 패턴에 따라 정말 가끔이지만 빈 컬렉션 할당이 성능을 떨어뜨릴 가능성도 있다. => 참고

<br/>

#### 2. 컬렉션 말고 배열을 쓴다면, 길이 0 짜리 배열로 나타내기

```java
public Cheese[] getCheeses3() {
    return cheesesInStock.toArray(new Cheese[0]);
}
```
- 배열을 쓸 때도`null`보다 위와 같이 길이 0 짜리 배열을 반환하는 것이 차라리 낫다.

```java
private static final Cheese[] EMPTY_CHEESE_ARRAY = new Cheese[0];

public Cheese[] getCheeses4() {
    return cheesesInStock.toArray(EMPTY_CHEESE_ARRAY);
}
```
- 불변인`EMPTY_CHEESE_ARRAY`를 통해`null`대신 빈 배열을 이용해 반환했다.
- 길이 0 짜리 배열을 미리`EMPTY_CHEESE_ARRAY`로 선언해두었기 때문에 성능상으로도 문제가 없다.

<br/>

### 참고
> 빈 컬렉션 할당이 성능을 떨어뜨릴 수 있는 상황은 주로 다음과 같은 경우

1. 매우 빈번한 호출
- 메서드가 매우 자주 호출되면, 오버헤드 누적으로 전체 시스템 성능에 영향
- 오버헤드: 빈 컬렉션을 생성하고 반환하는 데 드는 시간

2. 가비지 컬렉션 부하
- 빈 컬렉션 객체를 자주 생성하고 폐기하면 가비지 컬렉션의 부하 증가할 수도 => 실시간 시스템 같이 응답시간이 중요한 애플리케이션에 문제

> 빈 컬렉션이나 배열 반환으로 인한 성능 저하는 대부분의 애플리케이션에서 무시할 만한 수준이다.
> <br/>
> 대부분의 경우, 코드의 명확성과 안전성이 미미한 성능 이득보다 더 중요