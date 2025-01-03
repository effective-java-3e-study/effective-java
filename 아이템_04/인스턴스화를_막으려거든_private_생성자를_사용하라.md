인스턴스화를 막으려거든 private 생성자를 사용하라
=============

### 인스턴스화를 지양하는 케이스
보통 Util,Helper와 같이 "도와주는" 성격을 가지는 클래스들은 static한 메서드들만 가지고 있다.
스태틱한 메서드들은 인스턴스 없이 클래스를 통해 바로 호출이 가능하다.

```java
public class UtilClass {
    public  static String hello(){
        return "hello";
    }
  public static void main(String[] args) {
   UtilClass.hello();
   /*UtilClass utilClass = new UtilClass();
   utilClass.hello();*/ 
  }
  
}
```
주석 처리된 부분처럼 인스턴스를 만든 다음에 호출하는 코드는 문법 적 오류는 아니다. 하지만 인스턴스화를 만들 필요가 없고 이에 더해서 오히려 메서드가 static 메서드인지 
인스턴스 메서드 인지 헷갈리게 만든다.

이러한 내용을 바탕으로 "인스턴스회를 방지하는" 방법에 대해 알아보자.

### abstract의 사용
UtilClasslClass();를 못만들게 하는 반향으로 가보자.
추상 클래스는 인스턴스를 만들지 못하지 않을까? 결론적으로 말하면 추상클래스로 만들어도 인스턴스는 만들어질 수 있다.

```java
public class UtilClass {
    //아무 생성자를 작성하지 않으면 Default Constructor가 자동적으로 생긴다.
    //만약 abstractor로 인스턴스화가 막힌다면 "Default Constructor"라는 부분이 호출되지 않아야한다.
    public UtilClass(){
        System.out.println("Default Constructor");
    }
    public  static String hello(){
        return "hello";
    }
  public static void main(String[] args) {
 String hello = UtilClass.hello();
  }
  
}
```
```java
public class Subclass extends UtilClass {
    //하지만 subclass를 만들게 된다면 인스턴스를 만들 수 있게 된다. 
    //자식 클래스의 인스턴스를 만들 뗴 기본 생성자에서 상위 클래스의 생성잘 기본으로 출력한다.
    public static void main(String[] args) {
        Subclass utilClass = new Subclass();
    }
}
```
이러한 결과들로 추상을 추가하더라도 얼마든지 인스턴스는 만들 수 있으며 오히려 abstract라는 키워드로로
 "상속"이라고 착각하게 만들 수도 있다.

### 접근 지정자를 private으로 바꾸자
```java

public class UtilClass {
    private UtilClass(){
        System.out.println("Default Constructor");
    }
    public  static String hello(){
        return "hello";
    }
  public static void main(String[] args) {
 String hello = UtilClass.hello();
 //UtilClass utilClass = new UtilClass();
  }
  
}
```

하지만 이상테에서도 주석 친 것 처럼 내부에서는 인스턴스 생성이 가능하다. 이 부분까지 방지하려면
아래코드에서 처럼 바꾸어 주면 된다.
```java

public class UtilClass {
    private UtilClass(){
        throw new AssertionError(); //exception이 아니라 에러를 던지고 잇다-> 잘못됨을 면시적으로 이야기 해줌
    }
    public  static String hello(){
        return "hello";
    }
  public static void main(String[] args) {
 String hello = UtilClass.hello();
 //UtilClass utilClass = new UtilClass();
  }
  
}
```
하지만 굳이 생성자를 만들면서까지 생성자를 못쓰게 하는 것도 아이러니일 수도 있다.
그렇기에 다음처럼 문서화를 해서 private 생성자를 만들었는지 기록을 하는 방법이 조금 더 좋을 것 같다.
```java

public class UtilClass {
    /*이 클래스는 인스턴스를 방지 합니다*/
    private UtilClass(){
        throw new AssertionError(); //exception이 아니라 에러를 던지고 잇다-> 잘못됨을 면시적으로 이야기 해줌
    }
    public  static String hello(){
        return "hello";
    }
  public static void main(String[] args) {
 String hello = UtilClass.hello();
  }
  
}
```

### 정리
- 정적 메서드만 담은 류틸리티 클레스는 인스턴스로 만들어쓰려고 설계한 클래스가 아니다.
- 추상 클래스로 만드는 것으로는 인스턴스화를 막을 수 없다.
- private 생성자를 추사하면 클래스의 인스턴스화를 막을 수 있다. 
- 생성자에 주석으로 인스턴스화 불가한 이유를 설명하는 것이 좋다.
- 상속을 방지 할떄도 같은 방법을 사용할 수 있다.