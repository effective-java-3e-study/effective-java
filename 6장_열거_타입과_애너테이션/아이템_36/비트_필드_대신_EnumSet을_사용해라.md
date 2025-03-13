## item 36

### 비트 필드 대신 EnumSet을 사용해라

---

### 🙋‍♀️ 왜 비트 필드가 아닌 EnumSet을 사용해야 하나요?

#### 1️⃣ 비트 필드 열거 상수를 사용했던 이유

열거한 값들이 주로 집합으로 사용된 경우, 예전엔 각 상수에 서로 다른 2의 거듭제곱 값을 할당한 `정수 열거 패턴`
을 사용해왔습니다.

사용시, 비트별 연산을 사용해 합집합과 교집합과 같은 `집합 연산을 효율적으로 수행`할 수 있다는 장점이 있습니다.

✍ 비트 필드 열거 상수 클래스
```java
public class Text {
    public static final int STYLE_BOLD = 1 << 0; // 00000001
    public static final int STYLE_ITALIC = 1 << 1; // 0000010
    public static final int STYLE_UNDERLINE = 1 << 2; // 00000100
    public static final int STYLE_STRIKETHROUGH = 1 << 3; // 00001000

    public void applyStyles(int styles) {
        System.out.print("Applying styles: ");
        if ((styles & STYLE_BOLD) != 0)
            System.out.print("BOLD ");
        if ((styles & STYLE_ITALIC) != 0)
            System.out.print("ITALIC ");
        if ((styles & STYLE_UNDERLINE) != 0)
            System.out.print("UNDERLINE ");
        if ((styles & STYLE_STRIKETHROUGH) != 0)
            System.out.print("STRIKETHROUGH ");
        System.out.println();
    }

    public static void main(String[] args) {
        Text text = new Text();
        text.applyStyles(STYLE_BOLD | STYLE_ITALIC);
    }
}
```

---

#### 2️⃣ 왜 EnumSet을 사용하라고 하나요?

`비트 필드 열거 상수`의 단점을 보완해주는 `Enumset`의 장점 때문입니다.

---

##### ✔ 비트 필드 열거 상수의 단점

**1. 정수 열거 상수의 단점을 갖고 있음**

    - 비트 필드는 결국 정수형(int, long) 값을 사용하므로, 타입 안전성이 없음
    - `text.applyStyles(16); // 16 = 10000 → 정의되지 않은 값`
**2. 비트 필드 값이 그대로 출력되는 경우, 해석하기 어려움**

    - 위의 `applyStyles`메서드는 따로 print 문을 정의해줬지만, 아니라면 출력값을 한눈에 확인하기 어려움
    - `Applying styles: 3`
**3. 비트 필드 하나에 녹아 있는 모든 원소를 순회하기 까다로움**

    - 특정 값이 포함되어 있는지를 확인하려면 비트 연산을 직접 수행해야 함
    - 위의 `applyStyles` 메서드 참고!
**4. 최대 몇 비트가 필요한지 API 작성 시 미리 예측하여야 함(API 수정하지 않고 비트 수를 늘릴 수 X)**
     
    - int는 32비트, long은 64비트까지만 표현 가능하므로 플래그 개수가 많아지면 한계가 있음.

---

##### ✔ 위의 단점을 보완하는 EnumSet
`EnumSet`은 `Set` 인터페이스를 완벽히 구현합니다. 또한 내부적으로 비트 벡터로 구현하기에 위의 단점을 보완할 수 있습니다.

```java
public class Text {
    public enum Style {BOLD, ITALIC, UNDERLINE, STRIKETHROUGH}

    // 인터페이스로 받는 게 일반적으로 좋은 습관(클라이언트의 자유도 고려)
    // 그러나, EnumSet으로 넘기는 것을 추천
    public void applyStyles(Set<Style> styles) {
        System.out.printf("Applying styles %s to text%n",
                Objects.requireNonNull(styles));
    }

    public static void main(String[] args) {
        Text text = new Text();
        text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));
    }
}
```

**1. 정수값을 사용하지 않으므로 타입 안전성 보장**

    - EnumSet은 Set<Enum>을 사용하므로,컴파일 타임에서 잘못된 값이 들어가는 것을 방지 가능
    - `text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));`
**2. 출력 값 해석 쉬움**
     
    - 비트 필드는 정수 값이 출력되어 해석이 어려웠지만, `EnumSet`은 `Set`을 사용하기 때문에 출력 결과가 직관적임
    - `Applying styles: [BOLD, ITALIC]`
**3. 원소를 순회하기 쉬움**

    - Set 인터페이스를 구현했기에 `for-each` 문을 사용하여 간단하게 모든 원소를 순회 가능함
**4. 비트 수의 한계 없음**

    - 내부적으로 비트 벡터로 구현되어 있기에 동적으로 확장이 가능함 
    - 추가적으로 비트 필드에 비견되는 성능을 보여줌
    - 또한 `EnumSet`에서 난해한 작업을 다루기에, 비트를 직접 다룰 때 흔히 겪는 오류에서 해방이 가능함

---

### 🙋‍♀️ 그럼 EnumSet의 단점은 없나요?

유일한 단점은 자바 9까지는 아직 불변 EnumSet을 만들 수 없다는 것입니다. 
대체제로, 명확성과 성능이 조금 희생되지만 `Collections.unmodifiableSet()`을 활용하면 불변 `EnumSet`을 만들 수 있습니다.

