# 정확한 답이 필요하다면 float와 double은 피하라

대부분 소수점을 표현하고자 할 때는 int나 long을 사용한다. 
float와 double 타입은 넓은 범위의 수를 빠르게 정밀한 '근사치'로 계산하도록 세심하게 설계되었다.
따라서 float와 double 타입은 특히 금융 관련 계산과 같은 정확한 결과가 필요할 때는 사용하면 안 된다.

## float와 double을 피하라

예를 들어 주머니에는 1달러가 있고, 선반에 10센트, 20센트, 30센트, ... 1달러까지의 맛있는 사탕이 놓여 있다고 가정해보자.
10센트짜리 사탕부터 하나씩 차례대로 구입한다고 가정 했을 때, 사탕을 몇 개나 살 수 있고, 잔돈은 얼마나 남을까?

```JAVA
public static void main(String[] args) {
    double funds = 1.00;
    int itemsBought = 0;
    for (double price = 0.10; funds >= price; price += 0.10) {
        funds -= price;
        itemsBought++;
    }
    System.out.println(itemsBought + "개 구입");
    System.out.println("잔돈(달러): " + funds);
}
```

위의 예시에서 사탕 3개를 구입한 후에 예상 잔돈은 0.4달러이다.
하지만 프로그램을 실행해보면 사탕 3개를 구입한 후 잔돈은 0.3999999999999999달러가 남았음을 알게 된다.
이 문제를 올바로 해결하려면 어떻게 해야 할까?

## BigDecimal, int 혹은 long을 사용하자

앞서 발생한 문제는 BigDecimal, int 혹은 long을 사용하면 해결할 수 있다.
우선 BigDecimal을 사용하면 다음과 같이 코드를 구현할 수 있다.

```JAVA
public static void main(String[] args) {
    final BigDecimal TEN_CENTS = new BigDecimal(".10");

    int itemsBought = 0;
    BigDecimal funds = new BigDecimal("1.00");
    for (BigDecimal price = TEN_CENTS; funds.compareTo(price) >= 0; price = price.add(TEN_CENTS)) {
        funds = funds.subtract(price);
        itemBought++;
    }

    System.out.println(itemBought + "개 구입");
    System.out.println("잔돈(달러): " + funds);
}
```

이 프로그램을 실행하면 사탕 4개를 구입한 후, 0달러가 남았음을 알 수 있다.
올바른 답이 나왔지만 BigDecimal은 두 가지 단점이 존재한다.
기본 타입보다 쓰기가 훨씬 느리고, 훨씬 불편하다.
단발성 계산이라면 느리다는 문제는 무시할 수 있지만, 쓰기 불편하다는 점은 무시할 수 없을 것이다.

이런 BigDecimal대안으로 숫자가 너무 크지 않다면 int나 long을 사용하면 된다.
다음은 정수 타입을 사용해 구현한 코드다.

```JAVA
public static void main(String[] args) {
    int itemsBought = 0;
    int funds = 100;
    for (int price = 10; funds >= price; price += 10) {
        funds -= price;
        itemsBought++;
    }

    System.out.println(itemBought + "개 구입");
    System.out.println("잔돈(달러): " + funds);
}
```

## 정리

정확한 답이 필요한 계산ㅁ에는 float와 double을 피하자.
소수점 추적은 BigDecimal을 사용하거나, 정수 타입을 사용하자.
