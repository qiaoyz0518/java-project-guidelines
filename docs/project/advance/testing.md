## 集成测试

测试的第一部分是单元测试，它将根据设计阶段完成的规范自行测试该模块。一旦完成，我们就开始进行集成测试，将各种模块组合在一起，或者整合到整个系统中，或者整合到重要的子系统中。

顾名思义，集成测试的重点是测试许多单独开发的模块是否按预期协同工作。它是通过激活许多模块并对所有模块运行更高级别的测试来执行的，以确保它们一起运行。这些模块可以是单个可执行文件的一部分，也可以是单独的。

在现有实际项目中，往往对 数据库的 集成测试  是一个难点。下面 就 mysql 和 mongodb的集成测试，介绍两种方案 [database rider](https://github.com/database-rider/database-rider) 和 `Embedded MongoDB`

### Database Rider

现方案由 `spring test` + `database rider` + `Junit 5` 组成，因此首先在依赖中添加如下主要依赖:

```groovy
    integrationTestImplementation 'com.h2database:h2'
    integrationTestImplementation 'com.github.database-rider:rider-core:1.32.0'
    integrationTestImplementation 'com.github.database-rider:rider-spring:1.32.0'
    integrationTestImplementation 'org.springframework.boot:spring-boot-starter-test'
```

1. 在以上依赖中，我们使用 `h2` 内存数据库作为集成测试的关系型数据库，他方便且快速； 
1. `rider-core` 是其他 database rider 特性的基础；
1. `rider-spring` 用于对spring 测试环境的支持；

方便起见复用 [初级教程](../basic/developments.md#编写逻辑)中的项目作为集成测试项目环境。用 databse-rider 测试环境测试 `OwnerServiceImpl` 的集成逻辑；

1. 确认以上依赖已经加入；
1. 在 `src` 创建 `integeration-test` 目录用于存放集成测试源代码和静态资源；
1. 在 `integeration-test` 创建 `OwnerServiceImpl` 同级目录；
1. 创建 `OwnerServiceImplTest.java` 测试类，注意命名；
    ```bash
    integration-test/java/cn/tendata/jstart/service/
    └── OwnerServiceImplTest.java
    ```
1. 在 `integration-test/resources` 目录创建 `datasets` 用于创建databse rider 外置测试数据；
1. 在 `datasets` 新建 `owners.yml` 文件，增加以下内容:
    ```yml
    owners:
    - id: 1
        first_name: 张
        last_name: 翠山
        address: 严中路374路
        city: 上海
        telephone: 123456
        deleted: 0
    - id: 2
        first_name: 李
        last_name: 元霸
        address: 浦东南路58号
        city: 上海
        telephone: 9999
        deleted: 0
    ```
1.  按照测试一般原则编写测试代码:
    ```java
    @DBRider
    @SpringBootTest
    @ExtendWith({SpringExtension.class})
    @DBUnit(cacheConnection = false, leakHunter = true)
    class OwnerServiceImplTest {

        @Autowired
        private OwnerService service;

        @DataSet("owners.yml")
        @Test
        void getAll() {
            Page<Owner> actualQuery = service.getAll(new Owner(), Pageable.ofSize(10));

            Assertions.assertAll(() -> {
                MatcherAssert.assertThat(actualQuery.getContent(), Matchers.hasSize(2));
                MatcherAssert.assertThat(actualQuery.getContent().get(0), Matchers.hasProperty("firstName", Is.is("张")));
                MatcherAssert.assertThat(actualQuery.getContent().get(0), Matchers.hasProperty("lastName", Is.is("翠山")));
            });
        }
    }
    ```
    以上测试源码中 
    1. 使用 `@SpringBootTest` 注解集成spring boot 集成测试环境。 
    1. `@ExtendWith({SpringExtension.class})` 支持 srping5 + junit5；
    1.  `@DBRider` 可配置多数据源，在这里默认使用默认数据库 h2;

### Flapdoodle's embedded MongoDB

与任何其他持久性技术一样，能够轻松地测试数据库与应用程序的其余部分的集成是至关重要的。值得庆幸的是，Spring Boot 允许我们轻松地编写这类测试。

核心依赖：

```xml
<dependency>
    <groupId>de.flapdoodle.embed</groupId>
    <artifactId>de.flapdoodle.embed.mongo</artifactId>
    <scope>test</scope>
</dependency>
```
在添加 `de.flapdoodle.embed.mongo` 依赖项到 Spring Boot 之后，在运行测试时将自动尝试下载并启动嵌入式 MongoDB

对于每个版本，只下载包一次，以便后续测试运行得更快。

测试示例如下:
```java
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class MongoDbSpringIntegrationTest {
    @DisplayName("given object to save"
        + " when save object using MongoDB template"
        + " then object is saved")
    @Test
    public void test(@Autowired MongoTemplate mongoTemplate) {
        // given
        DBObject objectToSave = BasicDBObjectBuilder.start()
            .add("key", "value")
            .get();

        // when
        mongoTemplate.save(objectToSave, "collection");

        // then
        assertThat(mongoTemplate.findAll(DBObject.class, "collection")).extracting("key")
            .containsOnly("value");
    }
}
```

我们可以在控制台发现以下日志，嵌入式数据库是由 Spring 自动启动的：
```log
...Starting MongodbExampleApplicationTests on arroyo with PID 10413...

```

Springboot 使得运行测试以验证正确的文档映射和数据库集成变得非常简单。通过添加正确的 Maven 依赖项，我们可以立即在 Spring Boot 集成测试中使用 MongoDB 组件。


我们需要记住，嵌入式 MongoDB 服务器不能被认为是 "真实" 服务器的替代品。


## mockito 框架

Mockito 是一个模拟框架，基于 JAVA ，用于对 JAVA 应用程序进行有效的单元测试。Mockito 用于模拟接口，以便可以将虚拟功能添加到可用于单元测试的模拟接口中。

Mockito 有助于无缝地创建模拟对象。它使用 Java 反射来为给定的接口创建模拟对象。模拟对象只不过是实际实现的代理

Mockito 的好处:

1. No Handwriting - 无需自己编写模拟对象。
1. Refactoring Safe - 重命名接口方法名称或重新排序参数不会破坏测试代码，因为 Mocks 是在运行时创建的。
1. 返回值支持- 支持返回值。
1. 支持异常。
1. 支持检查方法调用的顺序。
1. 支持使用注释创建模拟。

核心依赖:

 ```groovy
 testImplementation "org.mockito:mockito-core:3.+"
 ```   

### 启用 mockito 相关注释

    启用启用 mockito 相关注释有三种方法:

 1. `MockitoJUnitRunner.class`
       ```java
       @RunWith(MockitoJUnitRunner.class)
        public class MockitoAnnotationTest {
            ...
        }
       ``` 
1. 使用编程方式注入：
    ```java
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
 
    ```    
1. MockitoJUnit.rule()
    ```java
    public class MockitoInitWithMockitoJUnitRuleUnitTest {

    @Rule
    public MockitoRule initRule = MockitoJUnit.rule();

    ...
    }
    ```
### `@Mock`    

Mockito 使用最广泛的注释是 `@Mock` 。我们可以使用 `@Mock` 创建和注入 `mock` 实例，而不必手动调用 `Mockito.mock` 。

例如:

```java
@Mock
List<String> mockedList;

@Test
public void whenUseMockAnnotation_thenMockIsInjected() {
    mockedList.add("one");
    Mockito.verify(mockedList).add("one");
    assertEquals(0, mockedList.size());

    Mockito.when(mockedList.size()).thenReturn(100);
    assertEquals(100, mockedList.size());
}
```

等同于 `@mock`:

```java
@Test
public void whenNotUseMockAnnotation_thenCorrect() {
    List mockList = Mockito.mock(ArrayList.class);
    
    mockList.add("one");
    Mockito.verify(mockList).add("one");
    assertEquals(0, mockList.size());

    Mockito.when(mockList.size()).thenReturn(100);
    assertEquals(100, mockList.size());
}
```

### `@Spy`

`@Spy` 注释用于监视现有实例。允许在间谍对象中对字段实例进行快速包装。


```java
@Spy
List<String> spiedList = new ArrayList<String>();

@Test
public void whenUseSpyAnnotation_thenSpyIsInjectedCorrectly() {
    spiedList.add("one");
    spiedList.add("two");

    Mockito.verify(spiedList).add("one");
    Mockito.verify(spiedList).add("two");

    assertEquals(2, spiedList.size());

    Mockito.doReturn(100).when(spiedList).size();
    assertEquals(100, spiedList.size());
}
```
在以上示例中:

1. 使用 `List` 真正的 `add()` 方法添加元素到真正的 `ArrayList` 中；
1. 使用 `Mockito.doReturn()`填充 `spiedList.size()`方法返回`100`而不是`2`

### `@Captor`

`ArgumentCaptor` 是一个参数匹配器的特殊实现，它为进一步的断言来捕获参数值。而 `@Captor` 是其建议做法

```java
@Test
public void whenNotUseCaptorAnnotation_thenCorrect() {
    List mockList = Mockito.mock(List.class);
    ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);

    mockList.add("one");
    Mockito.verify(mockList).add(arg.capture());

    assertEquals("one", arg.getValue());
}
```

`@Captor` 的做法:
```java
@Mock
List mockedList;

@Captor 
ArgumentCaptor argCaptor;

@Test
public void whenUseCaptorAnnotation_thenTheSam() {
    mockedList.add("one");
    Mockito.verify(mockedList).add(argCaptor.capture());

    assertEquals("one", argCaptor.getValue());
}
```

### `@InjectMocks`

自动向测试对象注入模拟或间谍字段

注意,`@injectmocks` 也可以与`@Spy` 注释结合使用，这意味着 Mockito 将把 mock 注入到测试的部分 mock 中。这种复杂性是另一个很好的理由，说明为什么应该只使用部分模拟作为最后手段

```java
@Mock
Map<String, String> wordMap;

@InjectMocks
MyDictionary dic = new MyDictionary();

@Test
public void whenUseInjectMocksAnnotation_thenCorrect() {
    Mockito.when(wordMap.get("aWord")).thenReturn("aMeaning");

    assertEquals("aMeaning", dic.getMeaning("aWord"));
}
```

### mock 验证

1. 验证 mock 对象是否调用
    ```java
    List<String> mockedList = mock(MyList.class);
    mockedList.size();
    verify(mockedList).size();
    ```
1. 验证与 mock 对象调用次数:
    ```java
    List<String> mockedList = mock(MyList.class);
    mockedList.size();
    verify(mockedList, times(1)).size();
    ```

1. 验证无调用:
    ```java
    List<String> mockedList = mock(MyList.class);
    verifyNoInteractions(mockedList);
    ```

1. 验证没有在指定方法上发生调用:
    ```java
    List<String> mockedList = mock(MyList.class);
    verify(mockedList, times(0)).size();
    ```

1. 验证除此之外无其他调用:
    ```java
    List<String> mockedList = mock(MyList.class);
    mockedList.size();
    mockedList.clear();
    verify(mockedList).size();
    verifyNoMoreInteractions(mockedList);
    ```

1. 验证调用顺序:
    ```java
    List<String> mockedList = mock(MyList.class);
    mockedList.size();
    mockedList.add("a parameter");
    mockedList.clear();

    InOrder inOrder = Mockito.inOrder(mockedList);
    inOrder.verify(mockedList).size();
    inOrder.verify(mockedList).add("a parameter");
    inOrder.verify(mockedList).clear();
    ```

1. 验证无调用:
    ```java
    freesta<String> mockedList = mock(MyList.class);
    mockedList.size();
    verify(mockedList, never()).clear();
    ```

1. 验证交互至少发生了一定次数:
    ```java
    List<String> mockedList = mock(MyList.class);
    mockedList.clear();
    mockedList.clear();
    mockedList.clear();

    verify(mockedList, atLeast(1)).clear();
    verify(mockedList, atMost(10)).clear();
    ```

1. 验证使用了指定的值参入了调用
    ```java
    List<String> mockedList = mock(MyList.class);
    mockedList.add("test");
    verify(mockedList).add("test");
    ```

1. 验证参数范围
    ```java
    List<String> mockedList = mock(MyList.class);
    mockedList.add("test");
    verify(mockedList).add(anyString());
    ```
    
1. 使用参数捕获来验证交互:
    ```java
    List<String> mockedList = mock(MyList.class);
    mockedList.addAll(Lists.<String> newArrayList("someElement"));
    ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(mockedList).addAll(argumentCaptor.capture());
    List<String> capturedArgument = argumentCaptor.<List<String>> getValue();
    assertThat(capturedArgument, hasItem("someElement"));
    ```



