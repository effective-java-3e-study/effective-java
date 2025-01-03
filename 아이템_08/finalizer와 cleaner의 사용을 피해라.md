# finalizer와 cleaner의 사용을 피하라. 대신 AutoCloseable을 사용하자

finalizer와 cleaner은 객체 "소멸자" 이다. 결론적으로는 이 두가지 객체 소멸자 모두 "지양" 된다. 

finalizer: 자바 9 deprecated API 지정.(deprected란 중요도가 떨어져 더 이상 사용되지 않고 앞으로는 사라지게 될 지양되는 API이다.) cleaner를 대안으로 소개.
cleaner: 자바 9부터 지원하는 객체 소멸자.

### 자원 반납을 해야하는 이유
OS마다 open할 수 있는 개수가 제한이 되어있다. 파일을 open할 때마다 파일 핸들러라는 것이 만들어지는데 쉽게 생각하면 id라고 보면 된다. 이렇게 파일을 너무 많이 open하면 리눅스 기준 "Too many open files" 에러가 발생하게 된다. 이러한 에러가 발생하는 이유 중 하나로 InputStream, OutputStream 같은 자원을 제대로 반납하지 않는 경우 즉, 열려있는 커넥션을 제대로 정리해주지 않기 때문에 발생한다.
따라서 자원을 잘반납하라고 만든 것이 finalizer와 cleaner이지만 의도와 다르게 제목처럼 사용을 피해야 한다. finalizer는 cleaner 보다 위험하고, finalizer와 cleaner 둘 다 언제 실행될지 보장할 수 없고 실행 자체가 안될 수 있는 문제가 있다. 이 말은 리소스가 반납이 안될 수 있다는 말이다. 그리고 finalizer는 우리가 접근할 수 있다. public 클래스가 아닌 package 레벨의 클래스로 기본적으로는 접근할 수 없어 우리가 참조해서 쓸 수 없다. public 클래스이기 때문에 cleaner는 우리가 접근할 수 있다

### finalizer
어떤 객체가 더 이상 자신을 참조하지 않는다고 GC가 판단하면 그 객체에 있는 finalize() 메서드를 호출한다. 서브클래스는 finalize() 메서드를 오버라이드해서 시스템 리소스를 처분하는 등 기타 정리 작업을 수행한다.
```java
public class FinalizerIsBad {

    @Override
    protected void finalize() throws Throwable {
        System.out.println("");
    }
}
```
#### 종료화 대상으로 등록된 객체의 생명주기
1.	GC가 객체를 발견
보통 가비지 수집기(GC)는 더 이상 참조되지 않는, 즉 사용되지 않는 객체를 찾아 제거. 하지만 만약 객체가 finalize() 메서드를 가지고 있다면, 이 객체는 “종료화 대상”으로 분류되어 곧바로 수거되지 않고 일단 별도의 큐(종료화 큐)에 들어간다.
2.	종료화 큐(Finalization Queue)로 이동
GC가 “이 객체는 참조가 없으니 제거해도 되지만, finalize()가 있네?” 하고 확인하면, 그 객체를 종료화 큐에 집어넣는다.
3.	종료화 스레드(Finalizer Thread)가 종료화 큐를 처리
애플리케이션 스레드(우리가 작성한 메인 프로그램의 스레드들)와는 별도로 종료화 전용 스레드가 있습니다. 이 스레드는 종료화 큐에서 객체를 하나씩 꺼내서 finalize() 메서드를 호출한다.
4.	최종적으로 GC가 객체를 진짜로 회수
finalize()가 끝나면, 그 객체는 이제 다시 “쓰레기(garbage)” 상태가 되고, 다음 GC 사이클에서 실제로 메모리에서 제거(수거)됩니다.

---> 종료화 대상 객체는 최소 한 번 더 GC 사이클을 거치므로 바로 수거 되지 않는다는 문제점.

#### finalizer의 문제점들
- C++과 같은 언어에서는 프로그래머가 직접 객체 수명을 명시적으로 관리하여 리소스 획득/해제가 객체 수명과 직접적인 연관이 있어 객체가 삭제되면 바로 해제된다. 반면, 자바의 메모리 관리 시스템은 할당할 가용 메모리가 부족하면 그때그때 반사적으로 GC를 실행시키기 때문에 GC가 언제 일어날지는 아무도 몰라 객체가 수집될 때에만 실행되는 finalize() 메서드도 언제 실행될지 알 길이 없다.
따라서 가비지 수집은 딱 정해진 시간에 실행되는 법이 없으므로 종료화를 통해 자동으로 리소스 자원을 관리한다는 것은 말이 되지 않는다. 리소스 해제와 객체 수명을 엮는 장치가 따로 없으니 항상 리소스가 고갈될 위험에 노출되어 있다고 생각해도 무방하다
- 종료화 스레드 실행 도중 메서드에서 예외가 발생하면 유저 애플리케이션 코드 내부에는 아무런 컨텍스트도 없기 때문에 발생한 예외는 그냥 무시된다. 따라서 종료화 도중 발생한 오류는 어떻게 개선될 여지가 없다.
- 종료화에 블로킹 작업이 있을지 모르니 JVM이 스레드를 하나 더 만들어 finalize() 메서드를 실행해야 한다. 따라서 새 스레드를 생성/실행하는 오버헤드또항 존재한다.


```java
public class App {

    /**
     * 코드 참고 https://www.baeldung.com/java-finalize
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        int i = 0;
        while (true) {
            i++;
            new FinalizerIsBad();
            if ((i % 1_000_000) == 0) {
                Class<?> finalizerClass = Class.forName("java.lang.ref.Finalizer");
                Field queueStaticField = finalizerClass.getDeclaredField("queue");
                queueStaticField.setAccessible(true);
                ReferenceQueue<Object> referenceQueue = (ReferenceQueue) queueStaticField.get(null);

                Field queueLengthField = ReferenceQueue.class.getDeclaredField("queueLength");
                queueLengthField.setAccessible(true);
                long queueLength = (long) queueLengthField.get(referenceQueue);
                System.out.printf("There are %d references in the queue%n", queueLength);
            }
        }
    }
}
```
Object 클래스가 제공하는 finalize() 메서드를 재정의해서 new FinalizerIsBad();를 통해 생성자를 호출해서 객체를 계속 무한성 생성하도록 만든 코드이다.그리고 참조하는 변수를 없기 때문에 바로 GC의 대상이 된다.
그 뒤에 객체를 100만개를 만들때마다 Reflection을 사용해서 한 번씩 Finalizer 클래스를 참조해서 ReferenceQueue에 들어간 객체가 얼마나 있는지를 출력한다.
출력되는 결과를 보면 계속해서 쌓이다 결국 OutOfMemoryError가 발생한 것을 볼 수 있다. 즉, 정상적으로 GC를 하지 못하는 것이다. 이는 Queue를 처리하는 해당 스레드의 우선순위가 낮아 객체를 만드느라 너무 바빠 ReferenceQueue를 비우지 못해 정리하지 못한 것이다. 
그리고 finalizer() 메서드 안에서 다른 객체를 참조한다거나 본인 자신을 참조하는 등의 GC를 하기 위해 finalize() 메서드를 실행하려는데 오히려 객체가 늘어나는 이슈가 발생할 수도 있다.

### Cleaner
```java
public class BigObject {

    private List<Object> resource;// 정리가 되어야하는 용도

    public BigObject(List<Object> resource) {
        this.resource = resource;
    }
// 정리가 되어야하는 inalizer() 메서드에서 하는 일은 Runnable 인터페이스 구현체에서 진행 
    public static class ResourceCleaner implements Runnable {

        private List<Object> resourceToClean;

        public ResourceCleaner(List<Object> resourceToClean) {
            this.resourceToClean = resourceToClean;
        }

        @Override
        public void run() {
            resourceToClean = null;
            System.out.println("cleaned up.");
        }
    }
}
```
- inner 클래스로 만들 경우에 static 클래스로 만들어야 하고, 절대 BigObject 클래스 즉, 상위 클래스의 레퍼런스가 존재해서는 안 된다. 만약 중첩 클래스로 사용할 때 정적이 아닌 중첩 클래스면 자동으로 바깥 객체의 참조를 갖기 때문에 상위 클래스의 레퍼런스를 정리하지 못할 수 있다.
- 또한 clean해주는 작업을 하는데 정리하려는 BigObject를 참조하게 되면 다시 객체가 부활할 수도 있는 이슈가 생길 수 있다. 따라서 inner 클래스에서 참조하는 값은 BigObject 인스턴스가 아닌 BigObject가 들고 있는 resource 필를 참조하도록 만들어야 한다.

#### Cleaner 사용
```java
public class CleanerIsNotGood {

    public static void main(String[] args) throws InterruptedException {
        Cleaner cleaner = Cleaner.create();

        List<Object> resourceToCleanUp = new ArrayList<>();
        BigObject bigObject = new BigObject(resourceToCleanUp);

        // 어떤 object가 gc될 때, 두 번째 인자처럼 Runnable 구현체를 사용해서 자원을 해제
        cleaner.register(bigObject, new BigObject.ResourceCleaner(resourceToCleanUp));

        bigObject = null;
        System.gc();
        Thread.sleep(3000L);
    }
}
```
Cleaner 자체가 PhantomReference를 사용해서 만들어져 있기 때문에 Cleaner를 사용하는 것 자체 PhantomReference를 쓰는 것과 비슷.
팩토리 메서드 패턴을 통해 cleaner 인스턴스를 생성하고, 사용하고 싶은 객체(bigObject, resourceToCleanup)을 만든다.  그리고 cleaner.register(bigObject, new BigObject.ResourceCleaner(resourceToCleanUp));로 cleaner에 등록. 그러면 bigObject가 GC가 될 때 new BigObject.ResourceCleaner(resourceToCleanUp)으로 작업을 해제.



### 정리
- finalizer와 cleaner는 즉시 수행된다는 보장이 없다.
- finalizer와 cleaner는 실행되니 않을 수도 있다.
- finalizer 동작 중에 예외가 발생하면 정리 작업이 처리되지 않을 수도 있다.
- finalizer와 cleaner는 심각한 성능 문제가 있다.
- finalizer는 보안 문제가 있다.
- 반납할 자원이 있는 클래스는 AutoCloseable을 구현하고 클라이언트에서 close()를 호출하거나 try-with-resource를 구현하여야한다.
