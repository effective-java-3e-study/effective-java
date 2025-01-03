9장 정리.

블로그로 먼저 정리하고 -> md 파일로 옮기다보니 가독성이 블로그가 더 좋습니다.
### 블로그 링크: https://mn040820.tistory.com/135

InputStream, OutputStream, java.sql.Connection은 close()를 통해 직접 닫아줘야 한다.
왜? 알다시피 java에는 gc가 존재한다. 힙영역에서 참조되지 않는 객체를 조사하고 제거하는 역할을 진행한다.
그래서 따로 메모리 해제를 하지 않고 java를 사용해왔다.
하지만 왜 위와 같은 클래스들은 따로 close()를 호출해줘야하는 것일까?

파일, 네트워크 연결, 데이터베이스 는 JVM 힙 영역에 올라가는 객체가 아니다. 운영체제의 리소스이다.
먼저 운영체제의 Stream을 알아야 한다.

![image](https://github.com/user-attachments/assets/fb649fea-2e23-4131-8b68-3b84e1d22325)


즉, 스트림은 프로그램(JVM)밖에 있는 외부 자원이다. JVM과 외부 통신을 통해 DB 연결, 네트워크 통신, 파일 디스크립터 등을 진행하는 것이다.

그래서 JVM 내부의 GC가 작동하지 않는다. DB는 커넥션풀과 같은 정해진 갯수의 풀을 가지고 있어서 심하면 리소스 부족에 이르게되고, 네트워크나 파일은 리소스 누수가 발생한다. GC를 JVM 내부에서 진행한다 해도, os는 스트림이 닫혔는지 알 수 없으므로 연결을 지속하고 있기 때문이다.
<br>즉, close() 호출을 한다는 것은 OS로 스트림을 닫겠다는 메시지를 전달한다는 것과 동일한 말이다.

#### 파일 디스크립터 : 운영체제가 열려 있는 파일, 네트워크 소켓, 장치 등을 참조하는데 사용하는 고유한 숫자(정수)이다. 프로그램이 파일을 열거나 네트워크 연결을 생성하면, 운영체제는 이 디스크립터를 통해 해당 스트림과의 연결을 추적한다.



파일 디스크립터와 같은 리소스는 운영체제 수준에서 제한되어 있다. 제한된 수의 디스크립터를 초과하면 새로운 스트림을 열 수 없다.



또한 스트림은 데이터를 버퍼링할 수 있다. 즉, 데이터를 한 번에 보내는게 아니라 일정량을 메모리에 저장한 뒤 전송한다. close()를 호출하지 않으면 버퍼에 남아 있는 데이터가 저장되지 않거나 유실될 수 있다.



close() 의 역할

1. 파일 디스크립터 반환

2. 버퍼 플러시(Flush) -> close() 전에 flush()가 먼저 호출된 뒤 닫음.

3. 네트워크 소켓 닫기



AutoCloseable을 구현했다면 gc 시, 자동으로 close()가 호출된다. 하지만 호출된다해도, gc는 언제 발생할지 모른다. 즉, 웹서버 특성상 gc가 되기 전 많은 요청이 몰린다면 문제가 될 수 있다. 물론 AutoCloseable 을 구현하지 않았다면 직접 close()를 진행해줘야한다.





-----------

effective java아이템8의 finalizer를 상당수가 안전망으로 사용중이긴하지만 그리 믿을만하지 못하다.



1. try-finally 사용시, 만약 닫아야 할 자원이 여러개라면 코드가 너무 복잡해진다.코드 예시 -



2. 기기 고장 등으로 finally 로직에서도 에러 발생 시 기존 try 블록에서 발생한 에러가 묻힌다.

-> 예외는 try 블록과 finally 블록 모두에서 발생할 수 있다. 이런 상황이면 try 예외가 묻힌다.



위 문제들은 try - with - resources덕에 모두 해결되었다. -AutoCloseable 필수 구현.

- try 블록에서 터진 예외가 기록된다. close()시 터진 에러도 버려지지 않고 숨겨졌다(suppressed)라는 꼬리표를 달고 출력된다. 또한 자바 7 에 Throwable에 추가된 getSuppressed()를 이용하면 가져올 수 있다.



- 마찬가지로 catch 블록 사용 가능하다. -> try 문 중첩해서 사용하지 않아도 된다.


try-finally 사용 테스트
```java
public class Main {

    public static void main(String[] args) {
        Test test = new Test();
        test.test();
    }
    static class Test {
        public void test () {
            try {
                System.out.println("try");
                throw new IllegalArgumentException("try 에러");
            } catch (IllegalArgumentException e) {
                e.getMessage();
            } finally {
                throw new NullPointerException();
            }
        }
    }
}

Exception in thread "main" java.lang.NullPointerException
    at steady.Main$Test.test(Main.java:17)
    at steady.Main.main(Main.java:7)
```

try-with-resource 사용 코드
```java
public class Main {
    public static void main(String[] args) {
        try {
            check();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void check() throws Exception {
        try (ResourceTest test = new ResourceTest()) {
            throw new IllegalArgumentException("try");
        }
    }

    static class ResourceTest implements AutoCloseable {
        @Override
        public void close() throws Exception {
            throw new NullPointerException();
        }
    }
}

java.lang.IllegalArgumentException: try
    at steady.Main.check(Main.java:17)
    at steady.Main.main(Main.java:9)
        Suppressed: java.lang.NullPointerException
        at steady.Main$ResourceTest.close(Main.java:26)
        at steady.Main.check(Main.java:16)
        ... 1 more
```

이미지 참조 : https://www.tcpschool.com/java/java_io_stream
