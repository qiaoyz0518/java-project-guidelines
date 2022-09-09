## 为什么

为什么如此重要？
每个从事长期项目的开发人员在修复坏味道的代码时都会遇到一些常见问题，并扪心自问：

    "我在代码库中如何以及在哪里解决这个问题？"
    "这些代码行在做什么？"
    "这个代码仍然需要或使用吗？"

更重要的是：

    "我怎样才能确保如果我改变这个，不会有其他问题？"

以及其他类似的令人毛骨悚然的问题，有时开发人员最终只是浪费时间试图理解以下代码：没有文档，由于目的不明确而没有意义，具有包含一千行代码的功能，完全不可读和难以理解等等。这些是来自偷工减料的好处，或者在这种情况下，来自"削减"成本。


简而言之，自动代码测试的代码将在实际中有效地转化为各种好处：

1. 更多的可重用性；
1. 重构使代码变得简单/干净；
1. 敏捷代码；
1. 健壮的代码；
1. 可读代码；
1. 简化调试。

## 原则

FIRST 原则

- `F——Fast`： **快速** 
    在调试bug时，需要频繁去运行单元测试验证结果是否正确。如果单元测试足够快速，就可以省去不必要浪费的时间，提高工作效率。

- `I——Isolated`：**隔离**
    1. 好的单元测试是每个测试只关注逻辑的一个方面，这样有利于排错。
    1. 每个测试之间不应该产生依赖，不会因为测试顺序不同而导致运行的结果不同。
    1. 测试时不要依赖和修改外部数据等其他共享资源，做到测试前后共享资源数据一致。
- `R——Repeatable`：**可重复**
    单元测试需要保持运行稳定，每次运行都需要得到同样的结果，如果间歇性的失败，会导致我们不断的去查看这个测试，不可靠的测试也就失去了意义。

- `S——Self-verifying`：**自我验证**
    单元测试需要采用Asset函数等进行自验证，即当单元测试执行完毕之后就可得知测试结果，全程无需人工接入。

- `T——Timely`：**及时**
    等代码稳定运行再来补齐单元测试可能是低效的，最有效的方式是在写好功能函数接口后（实现函数功能前）进行单元测试。

## 实际操作

目标: 针对 "开发" 章节的 "宠物主人" 模块进行单元测试。

### 添加依赖

由于我们使用了gradle 作为构建工具，因此选用一款gradle测试插件工具貌似是一个聪明的选择。

1.  在项目 `build.gradle` 中添加如下内容,最终结果如下:

    ```groovy hl_lines="4 12"
    plugins {
    id 'org.springframework.boot' version '2.6.7'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id "org.unbroken-dome.test-sets" version "4.0.0"
    id 'java'
    }
    
    apply plugin: 'idea'
    apply plugin: 'eclipse'
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'org.unbroken-dome.test-sets'
    
    group = 'cn.tendata.jstart'
    version = '0.0.1-SNAPSHOT'
    
    sourceCompatibility = 11
    targetCompatibility = 11
    
    configurations {
        integrationTestImplementation.extendsFrom testImplementation
        integrationTestRuntimeOnly.extendsFrom testRuntimeOnly
    }
    
    repositories {
        mavenLocal()
        maven { url 'https://maven.aliyun.com/repository/public/' }
        mavenCentral()
    }
    
    dependencies {
        implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
        implementation 'com.h2database:h2'
        implementation 'org.springframework.boot:spring-boot-starter-web'
        implementation 'org.springframework.boot:spring-boot-starter-validation'
        implementation "com.fasterxml.jackson.datatype:jackson-datatype-hibernate5"
        implementation "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"
    
        testImplementation 'org.junit.jupiter:junit-jupiter'
        testImplementation 'org.mockito:mockito-junit-jupiter'
        testImplementation "org.springframework:spring-test"
        testImplementation "com.jayway.jsonpath:json-path"
        testImplementation "com.jayway.jsonpath:json-path-assert"
    }
    
    ext {
        snippetsDir = file('build/generated-snippets')
    }
    
    test {
        outputs.dir snippetsDir
    }
    
    tasks.withType(JavaCompile) { options.encoding = "UTF-8" }
    
    testSets {
        integrationTest { dirName = 'integration-test' }
    }
    
    project.integrationTest {
        outputs.upToDateWhen { false }
    }
    
    check.dependsOn integrationTest
    integrationTest.mustRunAfter test
    
    tasks.withType(Test) {
        useJUnitPlatform()
        reports.html.destination = file("${reporting.baseDir}/${name}")
    }
    
    ```            
    以上构建脚本主要做了如下工作:

    1. 使用 `org.unbroken-dome.test-sets` 单元测试插件；
    1. 集成测试集包名重命名为 `integeration-test`；
    1. 项目构建的check 任务顺序吗为 `test` -> `integrationTest`;
    1. 测试框架使用 Junit5；
    1. 测试使用 `spring-test` 环境；

    
1. 添加 测试基础组件；

    本单元测试主要依赖 spring context环境， spring web test 环境，mockito 测试框架等，因此需要构建他们基础环境。

    1. 新建 `src/test/java`， `src/test/resources` 目录分别用于存放单元测试源代码和静态资源；
    1. 在 `src/test/java` 目录创建 `cn/tendata/jstart/controller` 目录，此目录和 `src/main/java` 中 存放 `OwnerController` 相同。并在此目录新建 `OwnerControllerTest.java` 文件；
    1. 在 `src/test/java/cn/tendata/jstart/controller`  新建 `config`, `util` 两个目录，用于存放测试环境的 spring context 配置和测试工具类；
    1. 在 以上 `config` 目录 新建 `WebmvcConfig.java`, `TestConfig.java`;
    
        WebmvcConfig.java: 测试环境 web mvc 个性化配置:

        ```java
        package cn.tendata.jstart.controller.config;

        import cn.tendata.jstart.config.jackson.databind.PageSerializer;
        import com.fasterxml.jackson.core.Version;
        import com.fasterxml.jackson.databind.Module;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import com.fasterxml.jackson.databind.SerializationFeature;
        import com.fasterxml.jackson.databind.module.SimpleModule;
        import com.fasterxml.jackson.databind.util.StdDateFormat;
        import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
        import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.ComponentScan;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
        import org.springframework.data.domain.PageImpl;
        import org.springframework.data.web.config.EnableSpringDataWebSupport;
        import org.springframework.format.FormatterRegistry;
        import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
        import org.springframework.http.converter.HttpMessageConverter;
        import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
        import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
        import org.springframework.web.servlet.config.annotation.EnableWebMvc;
        import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
        
        import java.nio.charset.StandardCharsets;
        import java.util.List;
        
        import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
        
        @Configuration
        @EnableWebMvc
        @EnableSpringDataWebSupport
        @ComponentScan(basePackages = "cn.tendata.jstart.controller")
        public class WebmvcConfig implements WebMvcConfigurer {
        
            @Bean
            public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
                return new PropertySourcesPlaceholderConfigurer();
            }
        
            @Bean
            public Jackson2ObjectMapperBuilder configureObjectMapper() {
                return new Jackson2ObjectMapperBuilder()
                        .modulesToInstall(jacksonHibernate5Module(), jacksonPageWithJsonViewModule());
            }
        
            @Override
            public void addFormatters(FormatterRegistry registry) {
                DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
                registrar.setUseIsoFormat(true);
                registrar.registerFormatters(registry);
            }
        
            @Override
            public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
                ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().modules(
                                new JavaTimeModule(),                // java date time 序列化支持
                                jacksonHibernate5Module(),            //hibernate 数据序列化支持
                                jacksonPageWithJsonViewModule())    // page with json view 解析支持
                        .deserializers()
                        .featuresToEnable(READ_UNKNOWN_ENUM_VALUES_AS_NULL) // 为空或者不识别的enum 视为null
                        .serializers().featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .dateFormat(new StdDateFormat())        //时间反序列化 iso ，local以本地服务器为主
                        .build();
        
                MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(
                        objectMapper);
                converter.setDefaultCharset(StandardCharsets.UTF_8);
                converters.add(converter);
            }
        
            @Bean
            public Module jacksonHibernate5Module() {
                Hibernate5Module module = new Hibernate5Module();
                return module;
            }
        
            @Bean
            public Module jacksonPageWithJsonViewModule() {
                SimpleModule module = new SimpleModule("jackson-page-with-jsonview",
                        Version.unknownVersion());
                module.addSerializer(PageImpl.class, new PageSerializer());
                return module;
            }
        }

        ```

        TestConfig.java： 用于将一些不属于单元测试组件的其它组件 mock 掉:

        ```java
        package cn.tendata.jstart.controller.config;

        import cn.tendata.jstart.service.OwnerService;
        import org.mockito.Mockito;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.context.annotation.Import;
        
        @Configuration
        @Import(WebmvcConfig.class)
        public class TestConfig {
        
            @Bean
            public OwnerService ownerService() {
                return Mockito.mock(OwnerService.class);
            }
        
        }

        ```

    1.  其他组件， `PageSerializer.java`，用于 jackson `PageImpl` 分页对象序列化支持；

        PageSerializer.java 文件内容如下:
        ```java
        package cn.tendata.jstart.config.jackson.databind;

        import com.fasterxml.jackson.core.JsonGenerator;
        import com.fasterxml.jackson.databind.SerializerProvider;
        import com.fasterxml.jackson.databind.ser.std.StdSerializer;
        import org.springframework.data.domain.Page;
        
        import java.io.IOException;
        
        @SuppressWarnings("rawtypes")
        public class PageSerializer extends StdSerializer<Page> {
        
            private static final long serialVersionUID = -9196525484747505477L;
        
            public PageSerializer() {
                super(Page.class);
            }
        
            @Override
            public void serialize(Page value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeStartObject();
                gen.writeNumberField("number", value.getNumber());
                gen.writeNumberField("numberOfElements", value.getNumberOfElements());
                gen.writeNumberField("totalElements", value.getTotalElements());
                gen.writeNumberField("totalPages", value.getTotalPages());
                gen.writeNumberField("size", value.getSize());
                gen.writeFieldName("content");
                provider.defaultSerializeValue(value.getContent(), gen);
                gen.writeEndObject();
            }
        }
        ```

    1. 增加 spring web 测试组件 - mockMvc 支持组件 `MockMvcTestSupport.java` 文件:

        ```java
        package cn.tendata.jstart.controller;

        import cn.tendata.jstart.controller.config.TestConfig;
        import org.junit.jupiter.api.BeforeEach;
        import org.junit.jupiter.api.extension.ExtendWith;
        import org.mockito.junit.jupiter.MockitoExtension;
        import org.springframework.test.context.ActiveProfiles;
        import org.springframework.test.context.ContextConfiguration;
        import org.springframework.test.context.junit.jupiter.SpringExtension;
        import org.springframework.test.context.web.WebAppConfiguration;
        import org.springframework.test.web.servlet.MockMvc;
        import org.springframework.test.web.servlet.setup.MockMvcBuilders;
        import org.springframework.web.context.WebApplicationContext;
        
        import javax.annotation.Resource;
        
        @ExtendWith({SpringExtension.class, MockitoExtension.class})
        @ContextConfiguration(classes = {TestConfig.class})
        @WebAppConfiguration
        @ActiveProfiles("test")
        public abstract class MockMvcTestSupport {
        
            @Resource
            protected WebApplicationContext context;
        
            protected MockMvc mockMvc;
        
            @BeforeEach
            public void setUp() {
                this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
            }
        
        }

        ```    

 最后的文件目录组织结果如下:

```bash
test
├── java
│   └── cn
│       └── tendata
│           └── jstart
│               └── controller
│                   ├── MockMvcTestSupport.java
│                   ├── OwnerControllerTest.java
│                   ├── config
│                   │   ├── TestConfig.java
│                   │   └── WebmvcConfig.java
│                   └── util
│                       ├── OwnerDataUtils.java
│                       └── TestUtils.java
└── resources

```       

### 编写测试


#### 测试分页

在 `OwnerControllerTest.java` 中添加如下代码:
```java
package cn.tendata.jstart.controller;

import cn.tendata.jstart.controller.util.OwnerDataUtils;
import cn.tendata.jstart.controller.util.TestUtils;
import cn.tendata.jstart.data.domain.Owner;
import cn.tendata.jstart.service.OwnerService;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class OwnerControllerTest extends MockMvcTestSupport {

    @Autowired
    private OwnerService service;

    @Test
    void pageOwner_findByName_shouldReturnMatchedResult() throws Exception {

        Mockito.when(this.service.getAll(ArgumentMatchers.any(Owner.class), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(
                        OwnerDataUtils.createOwner("张", "大宝", 1),
                        OwnerDataUtils.createOwner("张二", "宝", 2)
                )));

        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/owners")
                                .param("firstName", "张")
                                .param("lastName", "宝"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.numberOfElements", Is.is(2)))
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[0].firstName", Is.is("张")))
                .andExpect(jsonPath("$.content[0].lastName", Is.is("大宝")))
                .andExpect(jsonPath("$.content[1].firstName", Is.is("张二")))
                .andExpect(jsonPath("$.content[1].lastName", Is.is("宝")))
                .andReturn();
    }
    ...
}
``` 

在以上测试代码示例中， 主要模拟 客户端 向我们 前面章节编写的 `OnwerController` 中的 `GET /owners` - "宠物主人分页" 接口 发送请求，查询符合条件的宠物主人列表。
在以上单元 测试中，我们必须满足几个编写原则:

1.  保证测试单元的隔离性。如果为 controller 做单元测试，那么controller 中的依赖，此处 为 `OwnerService` 对象，我们不需要关心其具体实现。我们只需要将他 mock 即可。mock 的一般语法就是： "当 mock 对象满足 什么条件时，返回什么结果".此处使用 mockito 框架 的 `when ... thenReturn ...` 语法。
2.  增强可读性。可读性一般在代码编写中使用合适的命名和适当的语法顺序来实现。比如，此处使用  `pageOwner_findByName_shouldReturnMatchedResult` 来命名测试方法。 前缀 `pageOwner` 代表我们测试的函数; `findByName` 表示我们使用了 名称 来做查询条件; `shouldReturn` 表示会返回一个什么结果，各个单元使用 `_` 来分割；
3.  尽量使 单元测试代码精炼简洁。 单元测试一般包含三个部分， `GIVEN` ,`WHEN`, `THEN`. 各部分可使用一个空白行来分割；
4. 测试数据外置。将测试数据外置是一个精简测试语法的好办法。将一些数据构建从测试代码剥离出去，此处在 `util` 目录提供各种数据生成的静态方法。在单元测试中，我们只需要关心 输入的是什么值，而无需要关心如何构建数据。

#### 测试更新

在 `OwnerControllerTest.java` 中添加如下代码:
```java
...

class OwnerControllerTest extends MockMvcTestSupport {


    ...
    @Test
    void createOwner_shouldReturnSaveOne() throws Exception {
        Mockito.when(this.service.save(ArgumentMatchers.any(Owner.class)))
                .thenReturn(
                        OwnerDataUtils.createOwner("张二", "宝", 1)
                );

        Owner createRequest = OwnerDataUtils.createOwner("张二", "宝", null);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.convertObjectToJsonBytes(createRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.firstName", Is.is("张二")))
                .andExpect(jsonPath("$.lastName", Is.is("宝")))
                .andExpect(jsonPath("$.id", Is.is(1)))
                .andReturn();
    }

    ...
}
``` 

按照 [测试分页](#测试分页) 的规则。

1. 方法命名 - 当调用 createOwner 接口时，即 创建一个新的 "宠物主人"记录 请求时，如果请求成功将返回 "保存成功的宠物主人" 对象；
2. 代码组件 - (`WHEN`) - 当依赖 `OwnerService` mock 对象调用 save 方法时，并且请求参数是任务 `Owner` 对象时，返回一个 保存好的 `Owner` 对象; (`GIVEN`) - 使用 `firstName: 张二` ， `lastName: 宝` 构建创建 "宠物主人" 请求对象； (`THEN`) - 请求发送 `POST /owners` 后是否返回 `201` - `Created` http status. 返回的对象 `id` 是否 为 `1`， `firstName` 属性是否为 `张二`， `lastName` 是否等于 `宝`。 


### 自动化测试

gradle 内置一个 自动化测试任务- `test`, 它编译并运行 所有 `test` 源码包中的内容，并将测试结果以web形式展示给测试人员；

运行 `gradlew test` 命令

```bash
$ gradlew test

Starting a Gradle Daemon, 1 incompatible Daemon could not be reused, use --status for details

> Task :test
16:10:59.572 [SpringContextShutdownHook] DEBUG org.springframework.web.context.support.GenericWebApplicationContext - Closing org.springframework.web.context.support.GenericWebApplicationContext@958c9bcb, started on Wed May 18 16:
10:57 CST 2022

BUILD SUCCESSFUL in 19s

```