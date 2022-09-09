## 目标

1. 使用 `spring boot` + `gradle` 框架和工具创建一个简单的 curd 功能模块；
2. 采用 "宠物医院" 项目中的主人管理功能来示例；
3. 本章节还包含基本的 spring boot 实现 和数据源配置等；
<!-- 
宠物医院数据库模型图:

![宠物医院](../../assets/images/pet-clinic_modle.png) -->

在本章节，采用 "主人" 也就是 "owner" 域 来开始一个简单的开发。

需求:

1. 可使用 "姓名","地址"字段进行模糊搜索主人列表；
1. 需要新增主人功能；
1. 需要修改主人信息功能；
1. 需要删除主人功能；  


## 原则

### 构建API

构建API 往往有三种设计风格： `code-first`, `api-first`, `design-first`;

在往后的开发中，后端开发会以 `api-first` 作为主要风格。引导其他职能组 并向 `design-first` 模式看齐.


- **`code-first`**
    代码优先的方法往往是根据业务需求来编写 API。他通过编码，根据需求实现API，然后使用注释创建 API 描述，或者手动从头开始编写描述。代码优先的方法并不意味着不会有 API 设计,他会将设计过程分布在注释的各个地方。
- **`api-first`**
    API-first 方法意味着将 api 视为组织依赖的最重要的关键业务资产。 
    这种方法包括围绕一个用类似 `OpenAPI` 的 API 描述语言编写的契约来设计每个 API。从一个 API 契约开始，可以确保产生的 API 的一致性、可重用性和广泛的互操作性。   
- **`design-first`**
    设计优先，首先意味着在编写任何代码之前，用人类和计算机都能理解的迭代方式描述每个 API 设计。使用 API 设计优先的方法，每个团队使用相同的设计语言，甚至于他们的设计工具也趋于相同。

!!! tip
    `desgin-first` 往往被称为 "领域驱动设计"，但往往需要各职能部门统一的设计语言和工具，难度往往比较大。 :smile:

## code

### 添加依赖项

在 项目构建建脚本文件添加如下 依赖项,结果如下:

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'com.h2database:h2'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation "com.fasterxml.jackson.datatype:jackson-datatype-hibernate5"
    implementation "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"
}
```

在以上依赖库中，我们使用了如下依赖:

-  `spring web （webmvc）` web项目开发 依赖；
- `H2` 内存数据库；
-  `spring data jpa` orm 框架；
-  数据验证框架；
- `jsr310` 的 `jackson`  时间标准序列化支持；

### 增加启动入口

1.  在 `src\main\java\cn\tendata\jstart` 新增程序入口类文件 `Application.java`（注意首先创建 `cn/tendata/jstart` 目录）：

    ```java
    package cn.tendata.jstart;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;

    @SpringBootApplication
    public class Application {

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }

    ```

1. 在 `src\main\resources` 目录中新建 `application.yml` 项目外部配置文件, 并增加如下内容:

    ```yml
    server:
        port: 8080

    logging:
        file:
            path: logs/
        level:
            root: INFO
    ```   

    在以上配置中:

    1. 定义服务端口为 `8080`;
    1. 日志文件位于运行服务根目录的 `logs` 文件夹;
    1. 全局日志级别 为 `info`;

3. 验证；

    - 在终端 使用 `./gradlew bootRun`,查看项目是否可以正常启动；
        ```bash
        $ ./gradlew bootRun
            Starting a Gradle Daemon, 1 incompatible and 3 stopped Daemons could not be reused, use --status for details

            > Task :bootRun

            .   ____          _            __ _ _
            /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
            ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
            \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
            '  |____| .__|_| |_|_| |_\__, | / / / /
            =========|_|==============|___/=/_/_/_/
            :: Spring Boot ::                (v2.6.7)

            2022-05-16 13:41:36.332  INFO 12968 --- [           main] cn.tendata.jstart.Application            : Starting Application using Java 11.0.10 on DESKTOP-TDFJPJJ with PID 12968 (C:\Users\luwei\Desktop\tendata\kpi\documents\tendata-jst
            art\build\classes\java\main started by luwei in C:\Users\luwei\Desktop\tendata\kpi\documents\tendata-jstart)
            2022-05-16 13:41:36.337  INFO 12968 --- [           main] cn.tendata.jstart.Application            : No active profile set, falling back to 1 default profile: "default"
            2022-05-16 13:41:37.084  INFO 12968 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
            2022-05-16 13:41:37.104  INFO 12968 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 7 ms. Found 0 JPA repository interfaces.
            2022-05-16 13:41:37.977  INFO 12968 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
            2022-05-16 13:41:37.991  INFO 12968 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
            2022-05-16 13:41:37.991  INFO 12968 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.62]
            2022-05-16 13:41:38.137  INFO 12968 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
            2022-05-16 13:41:38.138  INFO 12968 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1751 ms
            2022-05-16 13:41:38.255  INFO 12968 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
            2022-05-16 13:41:38.353  INFO 12968 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
            2022-05-16 13:41:38.396  INFO 12968 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
            2022-05-16 13:41:38.439  INFO 12968 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 5.6.8.Final
            2022-05-16 13:41:38.603  INFO 12968 --- [           main] o.hibernate.annotations.common.Version   : HCANN000001: Hibernate Commons Annotations {5.1.2.Final}
            2022-05-16 13:41:38.717  INFO 12968 --- [           main] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.H2Dialect
            2022-05-16 13:41:38.939  INFO 12968 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
            2022-05-16 13:41:38.947  INFO 12968 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
            2022-05-16 13:41:38.997  WARN 12968 --- [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly confi
            gure spring.jpa.open-in-view to disable this warning
            2022-05-16 13:41:39.346  INFO 12968 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
            2022-05-16 13:41:39.357  INFO 12968 --- [           main] cn.tendata.jstart.Application            : Started Application in 3.742 seconds (JVM running for 3.994)
            2022-05-16 14:09:11.548  WARN 12968 --- [l-1 housekeeper] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Thread starvation or clock leap detected (housekeeper delta=16m32s918ms584?s900ns).
            <==========---> 80% EXECUTING [41m 40s]
            > :bootRun

        ```
        如果运行正常则
        

!!! tip
     windows 使用linux 终端执行 gradle wrapper 任务时，有可能出现 版本不匹配的问题；       

!!! note
     windows 修改 环境变量后最好重启一下，免得浪费大量的时间花费在未知异常上面；       

### API-first

1.  创建 `openApi` 设计文档组织架构目录；
    在 `src/main/resources` 目录下创建 `swagger/parameters`, `swagger/resources`, `swagger/schemas` 等目录。
        ```bash
        $ cd cd src/main/resources 
        $ mkdir -p swagger/parameters swagger/resources swagger/schemas
        ```

2.  创建 `api.yml` api 文档入口文件；
    ```bash
        $ cd cd src/main/resources/swagger 
        $ touch api.yml
    ```

    !!! tip
        open api 入口文件推荐统一使用 `api.yml` 命名；  

3. api 设计；
    1. 在 `api.yml` 声明如下必要属性:
        ```yaml
        openapi: '3.0.1'
        info:
            title: 'tendata 宠物医院'
            version: 1.0.0
        servers:
            - url: https://localhost:8080/api
              description: Development server with TLS Profile
        tags:
            - name: pet
              description: 宠物api
            - name: owner
              description: 宠物主人api
        paths:
        ```

        - `openapi` : 声明了本 api 设计文档遵循的 open api 规范版本；
        - `info` : 本 api 的注释说明；
        - `servers` : 可用的 运行服务器，一般做mock 服务器使用；
        - `tags` : 声明 api 包含几个组；
        - `paths`: api 路由配置;

    1. 设计 domain. 按照需求设计 "主人" 域各种属性。在 `swagger/schemas` 中添加 `component.yml` 文件。添加如下内容:
        
        ```yaml
        PageOwner:
          type: object
          properties:
            number:
              type: number
            numberOfElements:
              type: number
            totalElements:
              description: 总共数据
              type: number
            totalPages:
              type: number
              description: 总共页数
            size:
              type: number
              description:  每页条数
            content:
              type: array
              items:
                $ref: '#/Owner'
        Owner:
          type: object
          description: 宠物主人
          required:
            - firstName
            - lastName
            - address
          properties:
            firstName:
              description:  姓
              type: string
            lastName:
              type: string
              description: 名字
            address:
              type: string
              description: 详细地址
            city:
              type: string
            telephone:
              type: string
              description: 电话
        ```

        - `PageOwner` : "主人"分页对象；
        - `owner`: "主人" 对象；


    1. "宠物主人"  api 按照 restfull 风格分成 `/owners`, `/owners/{id}` 两种，那么将onwer域中的api将被分割成两部分。添加如下配置到 `api.yml` 文件的 `paths` 节点中。并在 `swagger/resources` 目录中创建对应的路由文件 `onwers.yml`, `owner.yml`; 
    
        ```yaml hl_lines="4 5 6 7"
        openapi: '3.0.1'
        ...
        paths:
            /owners:
                $ref: './resources/owners.yml'
            /owners/{id}:
                $ref: './resources/owner.yml'
        ```

        - `owners.yml` : 配置 "分页"，"新增" api， 对应 path `GET /owners`, `POST /owners`;
        - `owners.yml` : 配置 "查看单个主人"，"修改主人"， "删除主人" api， 对应 path `GET /owners/{id}`, `PUT /owners/{id}`, `DELETE /owners/{id}`;

    1. 设计接口。
        - path: `/owners`: 新增和分页接口 open api 文件，`owners.yml` 添加如下配置:
            ```yaml
            post:
                post:
                  tags:
                    - owner
                  summary: 添加主人
                  description: 条件主人
                  operationId: addOwner
                  responses:
                  requestBody:
                    content:
                      application/json:
                        schema:
                          $ref: '../schemas/component.yml#/Owner'
                get:
                  tags:
                    - owner
                  summary: 主人分页
                  description: 主人分页
                  operationId: pageOwner
                  parameters:
                    - name: firstName
                      in: query
                      description: 姓
                      required: false
                      explode: true
                      schema:
                        type: string
                    - name: lastName
                      in: query
                      description: 名字
                      required: false
                      explode: true
                      schema:
                        type: string
                  responses:
                    '200':
                      description: 主人分页返回分页对象
                      content:
                        application/json:
                          schema:
                            $ref: '../schemas/component.yml#/PageOwner'
                    '400':
                      description: 参数传入错误
                    '500':
                      description: 服务器内部错误  tags:
            ```    
        - path: `/owners/{id}`:  查看，修改，删除 open api ，`owner.yml` 文件添加如下配置:
            ```yaml
            get:
                tags:
                   - owner
                 summary: 查看单个主人
                 description: 根据id获取单个主人信息
                 operationId: fetchOwner
                 parameters:
                   - $ref: '../parameters/_index.yml#/ownerId'
                 responses:
                   '200':
                     description: 根据id获取主人对象正确返回
                     content:
                       application/json:
                         schema:
                           $ref: '../schemas/component.yml#/Owner'
                         examples:
                           successExample:
                             summary: '调用成功'
                             description: '调用成功,返回单个对象'
                             value: {"name":"王二狗"}
                   '400':
                     description: 参数传入错误
                   '500':
                     description: 服务器内部错误
               put:
                 tags:
                   - owner
                 summary: 更新修改单个主人
                 description: 根据id修改单个主人信息
                 operationId: updateOwner
                 parameters:
                   - $ref: '../parameters/_index.yml#/ownerId'
                 requestBody:
                   content:
                     application/json:
                       schema:
                         $ref: '../schemas/component.yml#/Owner'
                 responses:
                   '200':
                     description: 根据id修改主人对象正确返回修改后的主人对象
                     content:
                       application/json:
                         schema:
                           $ref: '../schemas/component.yml#/Owner'
                   '400':
                     description: 参数传入错误
                   '500':
                     description: 服务器内部错误
               delete:
                 tags:
                   - owner
                 summary: 删除单个主人
                 description: 根据id删除单个主人信息
                 operationId: deleteOwner
                 parameters:
                   - $ref: '../parameters/_index.yml#/ownerId'
                 responses:
                   '204':
                     description: 根据id删除单个主人后正确返回
                   '500':
                     description: 服务器内部错误

            ```    

!!! tip
    在 open api 设计中，尽量将相同的元素抽象出来，通过 `$ref` 标记来引用;

!!! info
    1. [swagger api 官方文档](https://swagger.io/specification/) 
    2. [swagger api 最佳实践](https://oai.github.io/Documentation/best-practices.html)


api 设计后最终文件目录结构如下:

```bash
swagger
├── api.yml
├── parameters
│   ├── _index.yml
│   └── path
│       └── ownerId.yml
├── resources
│   ├── owner.yml
│   └── owners.yml
└── schemas
    ├── Error.yaml
    └── component.yml
```

### 编写逻辑

1. 在 `src/main/java/cn/tendata/jstart` 目录下 分别创建 `service`, `data`, `controller` 目录；

#### data 层

演示项目采用 [spring-data-jpa](https://spring.io/projects/spring-data-jpa),它基于 JPA ，对数据访问层增强支持。它使得构建使用数据访问技术的 spring 驱动的应用程序,更加容易实现。


1. 在 `data` 目录创建 `domain`, `repository` 目录；
1. 将 [AbstractEntity.java](../../assets/file/AbstractEntity.java), [AbstractEntityAuditable.java](../../assets/file/AbstractEntityAuditable.java) 两个 `model` 基类 放入 `domain` 目录
1. **Model 层**  - 根据 api 文档, 设计 "宠物主人" 域的entity 类 - `Owner.java`，放入 `model` 目录，如下:

    ```java
    package cn.tendata.jstart.data.domain;
    package cn.tendata.jstart.data.domain;

    import org.hibernate.annotations.SQLDelete;
    import org.hibernate.annotations.Where;

    import javax.persistence.GeneratedValue;
    import javax.persistence.Id;

    @Entity
    @Where(clause = "deleted=false")
    @SQLDelete(sql = "update owners set deleted=true where id =?")
    public class Owner  extends AbstractEntity<Integer> {
        private Integer id;
        private String firstName;
        private String lastName;
        private String address;
        private String city;
        private String telephone;
        private Boolean deleted;

        // get set method
        @Id
        @GeneratedValue
        public Integer getId() {
            return id;
        }
        ...
    ```
    !!! note
        `@Where`

1. **Repository 层** -  在 `repository` 目录新建 `OwnerRepository.java` 文件，添加如下内容:
    ```java
    package cn.tendata.jstart.data.repository;

    import cn.tendata.jstart.data.domain.Owner;
    import org.springframework.data.jpa.repository.JpaRepository;

    public interface OwnerRepository extends JpaRepository<Owner,Integer> {
    }
    ```
1. 在 项目启动入口类 `Application.java` 增加spring data jpa 的自动包扫描配置;

  ```java hl_lines="7 8 9 10 11 12"
    public class PetClinicApplication {

    public static void main(String[] args) {
      SpringApplication.run(PetClinicApplication.class, args);
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackages = "cn.tendata.jstart.data.repository")
    @EntityScan(basePackages = "cn.tendata.jstart.data.domain")
    static class JpaConfig {
    }
  }
  ```

 最后的data层 目录文件结构如下:
 

```bash
 data
├── domain
│   ├── AbstractEntity.java
│   ├── AbstractEntityAuditable.java
│   └── Owner.java
└── repository
    └── OwnerRepository.java
```

#### service 层

service层作为组合逻辑的胶水层，是业务逻辑比较复杂的地方.该层包含驱动应用程序核心功能的业务逻辑。比如做决策、计算、评估和处理在其他两层之间传递的数据.

1.  将 [EntityService.java](../../assets/file/EntityService.java),[EntityServiceSupport.java](../../assets/file/EntityServiceSupport.java) 两个 `service` 层基类文件放入 `service` 文件目录。

    !!! tip
        service 基类提供基本的 curd 抽象功能，可以省去写该部分逻辑的时间； 

1.  在 `service` 目录创建 `OwnerService`(service 接口)文件， `OwnerServiceImpl`(service 接口实现类)文件，如下:

   OwnerService.java: 
    ```java
    package cn.tendata.jstart.service;

    import cn.tendata.jstart.data.domain.Owner;

    public interface OwnerService extends EntityService<Owner,Integer>{
      Page<Owner> getAll(Owner query, Pageable pageable);
    }
    ```

  OwnerServiceImpl.java：
  ```java
    package cn.tendata.jstart.service;

    import cn.tendata.jstart.data.domain.Owner;
    import cn.tendata.jstart.data.repository.OwnerRepository;
    import org.springframework.data.domain.Example;
    import org.springframework.data.domain.ExampleMatcher;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;

    @Service
    public class OwnerServiceImpl extends EntityServiceSupport<Owner,Integer, OwnerRepository> implements OwnerService{
        protected OwnerServiceImpl(OwnerRepository repository) {
            super(repository);
        }

        @Override
        public Page<Owner> getAll(Owner query, Pageable pageable) {
            ExampleMatcher matcher = ExampleMatcher.matching()
                    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                    .withIgnoreNullValues();
            Example<Owner> example = Example.of(query, matcher);
            return getRepository().findAll(example,pageable);
        }
    }

  ```

`service` 层最终目录文件结构如下:

```bash
service
├── EntityService.java
├── EntityServiceSupport.java
├── OwnerService.java
└── OwnerServiceImpl.java
```

#### controller 层

controller层 负责接收客户端的请求，然后调用Service层接口控制业务逻辑，获取到数据，传递数据给页面

1. controller 目录增加 `WebUtil` 工具类，主要处理 controller 返回:

    ```java
    package cn.tendata.jstart.controller;

    import cn.tendata.jstart.data.domain.AbstractEntity;
    import org.springframework.data.domain.Page;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.util.Assert;
    import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

    import java.io.Serializable;
    import java.net.URI;

    public abstract class WebUtils {

      public static <T extends AbstractEntity<?>> ResponseEntity<Page<T>> pageResponse(Page<T> page) {
        return ResponseEntity.ok(page);
      }

      public static <T extends AbstractEntity<?>> ResponseEntity<T> fetchResponse(T entity) {
        return ResponseEntity.ok(entity);
      }

      public static <T extends AbstractEntity<?>> ResponseEntity<T> createdResponse(T entity) {
        if (entity != null) {
          Serializable id = entity.getId();
          Assert.notNull(id, "id can't be null");
          return ResponseEntity.created(toLocation(id)).body(entity);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
      }

      public static <T extends AbstractEntity<?>> ResponseEntity<T> updateResponse(T entity) {
        Serializable id = entity.getId();
        Assert.notNull(id, "id can't be null");
        return ResponseEntity.ok(entity);
      }

      public static ResponseEntity<Void> deletedResponse() {
        return ResponseEntity.noContent().build();
      }

      /**
      * @param id persistence entity id
      * @return URI location
      */
      public static URI toLocation(Object id) {
        Assert.notNull(id, "id can't be null");
        return ServletUriComponentsBuilder
          .fromCurrentRequest()
          .path("/{id}")
          .buildAndExpand(id)
          .toUri();
      }
    }

    ```

1. 在 `Owner` entity 类增加如下代码，实现
 Owner 对象的 实例化和 更新功能:
  Owner.java：
  ```java
    public static Owner of(String firstName, String lastName){
        Owner owner = new Owner();
        owner.setFirstName(firstName);
        owner.setLastName(lastName);
        return owner;
    }

    public static Owner mergeField(Owner update, Owner exists){
        Assert.notNull(exists, "exists Owner can't be null");
        mergeField(update.getAddress(),o -> exists.setAddress((String) o));
        mergeField(update.getCity(),o -> exists.setCity((String) o));
        mergeField(update.getFirstName(),o -> exists.setFirstName((String) o));
        mergeField(update.getLastName(),o -> exists.setLastName((String) o));
        mergeField(update.getTelephone(),o -> exists.setTelephone((String) o));
        return exists;
    }

    private static void mergeField(Object field, Consumer<Object> mergeFun){
        if(field != null){
            mergeFun.accept(field);
        }
    }
  ```    

1.  controller 目录增加 `OwnerController.java` 文件，并增加如下内容，处理 前端请求并返回:

    ```java
    package cn.tendata.jstart.controller;

    import cn.tendata.jstart.data.domain.Owner;
    import cn.tendata.jstart.service.OwnerService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.web.PageableDefault;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/owners")
    public class OwnerController {
        private final OwnerService service;

        @Autowired
        public OwnerController(OwnerService service) {
            this.service = service;
        }

        @GetMapping
        public ResponseEntity<Page<Owner>> pageOwner(
                @RequestParam(required = false) String firstName,
                @RequestParam(required = false) String lastName,
                @PageableDefault Pageable page) {
            return WebUtils.pageResponse(this.service.getAll(Owner.of(firstName,lastName),page));
        }

        @PostMapping
        public ResponseEntity<Owner> createOwner(@RequestBody @Validated Owner owner) {
            service.save(owner);
            return WebUtils.createdResponse(owner);
        }

        @GetMapping("/{id}")
        public ResponseEntity<Owner> getOwner(@PathVariable("id") Owner owner) {
            return WebUtils.fetchResponse(owner);
        }

        /**
        *  update 如果局部更新不建议使用 {@link Validated}注解
        */
        @PutMapping("/{id}")
        public ResponseEntity<Owner> updateOwner(
                @RequestBody Owner update,
                @PathVariable("id") Owner owner) {
            Owner.mergeField(update, owner);
            service.save(owner);
            return WebUtils.updateResponse(owner);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteOwner(@PathVariable("id") Owner owner) {
            service.delete(owner);
            return WebUtils.deletedResponse();
        }
    }
    ```

controller 层文件目录结果如下:

```bash
controller
├── OwnerController.java
└── WebUtils.java
```

### 验证

在终端运行 `./gradlw bootRun` 命令，检查更新后的项目是否有依赖和其他问题。 

