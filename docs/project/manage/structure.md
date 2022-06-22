在 Java 中，我们使用包来对相关的类、接口和子包进行分组。

这样做的主要好处是：

1. 让相关联的类更容易差早 - 一个包通常将逻辑相关联的类包含进去；
1. 避免命名冲突 - 可以用不同的包包含相同命名的类；
1. 控制访问级别 - 可以通过访问修饰符和包来控制访问级别
 命名约定

为了避免使用相同名称的包，我们遵循一些命名约定：

- 报名应该都是小写的；
- 多个包名使用 `.` 分割，例如 `cn.tendata.jstart`;
- 包名可由公司或者组织来组织；

为了根据组织确定包名，我们通常首先反转公司的 URL。在此之后，变数命名原则由公司定义，可能包括部门名称和项目名称。 例如 `www.tendata.cn` -> `cn.tendata`.然后我们可以进一步定义子包 `cn.tendata.jstart.core`, `cn.tendata.start.service`等。

### 目录结构

java 中的包对应一个目录结构。

每个包和子包都有自己的目录。所以，对于 `cn.tendata.jstart`包，我们应该有一个 `com` -> `baeldung` -> `packages` 的目录结构。

大多数 IDE 将根据我们的包名称帮助创建此目录结构，因此我们不必手动创建这些。

### 完整包名

有时候，我们可能会从不同的包中使用同名的两个类。例如，我们可能同时使用 `java.sql.date`和 `java.util.date` 。。当我们遇到命名冲突时，我们需要为至少一个类使用完整的类名。

## 分包原理

虽然没有官方标准，单在java 社区中有两种分包规则: `按责任该分包`, `按领域功能分包`

### 按责任分包

例如，应用程序可能有一个数据库层。然后您将创建一个数据库包。然后，与数据库通信所涉及的所有类都将位于数据库包中。在 spring 社区 我们一般采用该分层方式。

这种方式在项目开发中也是最常见的分包方式， 此分包也称为 `按层分包`， `水平切片`

```bash
h-layer
├── controller
│   └── OwnerController.java
├── core
├── data
│   ├── domain
│   │   └── OwnerEntity.java
│   └── repository
│       └── OwnerRepository.java
└── service
    └── OnwerService.java

```

如果没有机会处理具有这种结构的项目，可以在一些框架教程中遇到它。例如，直到版本2.2, Play 框架推荐这种方法。当初，Angular.js 的教程建议根据他们的职责把事情组织起来。

好处:

1. 固定的按层分包规则，会广泛影响开发团队多个项目的包结构构建，因此当阅读同一个团队多个项目时，项目成员可以很熟悉的了解项目结构；
2. 保持项目结构的有序性，让经验不足的开发人员也很容易理解。

### 按领域模型分包

例如你的应用程序有一个宠物主人管理的功能区域，你可以创建一个名为 `Owner` 的 Java 包。关于宠物主人的 逻辑，路由，orm 等都在该包中完成。

```bash
feature-layer
└── onwer
    ├── OnwerService.java
    ├── OwnerController.java
    ├── OwnerEntity.java
    └── OwnerRepository.java

```

和 水平切片相对的 该种分包页脚垂直分包。

## 实践

虽然 Java 没有强制使用哪种项目结构，但遵循一致的模式来组织我们的源文件、测试、配置、数据和其他代码工件还是非常有用的。Maven 是一种流行的 Java 构建工具，
它规定了特定的项目结构。虽然我们可能不使用 Maven，但坚持约定总是好的。

- `src/main/java`：用于源文件
- `src/main/resources`：用于资源文件，如属性
- `src/test/java` : 用于测试源文件
- `src/test/resources`：用于测试资源文件，如属性

### 默认布局

```bash
./
└── src
    ├── checkstyle
    ├── integration-test
    │   ├── java
    │   │   └── cn
    │   │       └── tendata
    │   │           └── samples
    │   │               └── petclinic
    │   │                   └── controller
    │   └── resources
    ├── main
    │   ├── generated
    │   ├── java
    │   │   └── cn
    │   │       └── tendata
    │   │           └── samples
    │   │               └── petclinic
    │   │                   ├── bind
    │   │                   ├── config
    │   │                   ├── controller
    │   │                   ├── core
    │   │                   ├── data
    │   │                   │   ├── jpa
    │   │                   │   │   ├── config
    │   │                   │   │   ├── domain
    │   │                   │   │   ├── jackson
    │   │                   │   │   └── repository
    │   │                   ├── service
    │   │                   ├── support
    │   │                   └── util
    │   ├── resources
    └── test
        └── jmeter
├── build.gradle
├── docker-compose.yml
├── gradle.properties
├── gradlew
├── gradlew.bat
├── readme.md
├── README.md
└── settings.gradle

```

以上为一个常见的 gradle 工程目录结构:

- `src/main/java`: 工程源代码
- `src/integeration-test`： 项目集成测试源代码
- `src/test/`: 项目单元测试
- `build.gradle`: 项目构建脚本文件

现有项目采用 混合分包模式，总体上以 `水平切片` 的方式分包，如果有些比较重量级的模块，新建一个包置于和顶层包同级即可。例如，有个 `mail`的复杂模块
我们可以放到和cotroller包同级。
