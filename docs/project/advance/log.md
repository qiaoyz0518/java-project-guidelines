## 原则

<!-- https://www.atatus.com/blog/9-best-practice-for-application-logging-that-you-must-know/ -->

日志是一个记录系统和用户之间通信的文件，或者是一个数据收集工具，它记录用户从系统终端进行的事务的类型、内容和时间。

日志系统包含在许多操作系统、软件框架和程序中。Syslog 由 Internet Engineering Task Force (IETF)创建，是一个广泛使用的日志记录标准。Syslog 标准允许通过专门的标准化子系统生成、筛选、记录和分析日志消息。这就消除了软件开发人员创建和构建他们自己的特别日志系统的需要。

### what

首先要知道 日志可以记录什么:

-  输入、输出消息  
    传入和传出的消息都必须用 API 端点 url、请求参数、请求来源和中间 ip、请求头、作者信息、请求和响应主体、业务上下文、时间戳以及组件通过消息传递进行通信时的内部处理步骤进行记录。
-  服务和功能调用  
    在调用服务或函数时，最好以较低的日志级别报告调用的上下文，主要用于调试。将这些日志放在手边可以更容易地探索业务逻辑的问题，特别是当我们没有能力将调试器连接到您的应用程序时。
-  用户交互和业务统计  
    每个应用程序都有自己的一组业务用例和用户流程，这为系统的领域专家提供了丰富的信息。其他与业务相关的数据，如交易量和活跃用户及其阶段，在获得业务洞察力方面非常有用，甚至可以用于业务智能。
-  数据操作
    在大多数企业应用程序中，为与数据相关的操作保留一个单独的日志，其中包含所有重要信息，如访问 id、使用的确切服务实例和角色特权、时间戳、数据层查询，以及已更改数据集以前和新状态的快照，这是出于安全和遵从性的原因。用户以及其他系统和服务所做的所有与数据相关的尝试和 CRUD 操作都必须记录在审计跟踪中。
- 系统事件    
    行为事件、转换模式、服务间通信、服务实例 id、主动服务 api、主动监听 IP 和端口范围、加载的配置、总体服务健康状况以及其他有助于理解系统行为的所有东西都必须在系统事件中捕获。


### when

系统中每个事件的严重性由日志级别指定。大多数日志框架都可以访问以下级别。   

- FATAL - 标识可能导致应用程序中止的极其严重的错误事件。通常，这会导致灾难性的故障
- ERROR - 标识仍然可以让软件运行但在所影响的路由中具有受限功能的错误事件
- WARN  - 描述破坏性小于错误的事件。它们通常不会导致应用程序功能的任何减少或其完全失败。然而，这些都是必须调查的红色标志
- INFO - 在申请行为方面，表示主要事件的横额及资讯讯息
- DEBUG - 表示主要用于调试的特殊和详细的数据。这些日志帮助我们调试代码
- TRACE - 为了提供关于特定事件/上下文的最大信息，它表示最底层的信息，例如代码的堆栈跟踪。这些日志允许我们检查变量值以及完整的错误堆栈

无论每个日志级别实现的复杂性和深度如何，我们都必须在代码中适当地设置它们，以便为每个环境提供最佳程度的信息。

### How - 使用英语和友好的消息

一些工具和终端控制台不支持某些 Unicode 字符以打印和保存日志消息。在日志级别，本地化和其他复杂特性可能难以实现。因此，在编写日志消息时，一定要坚持使用英语，并始终使用公认的字符集

如果我们记录的太少，我们可能无法收集到足够的信息来建立每个关键事件的上下文。如果日志记录过多，我们将会担心性能问题。

全面掌握系统的功能性和非功能性需求，计划适当的日志消息质量和数量，以优化日志消息。让每一条日志信息都有用并与当时的情况相关——并且始终保持简短和恰到好处。

### How - 结构化

好的日志记录需要跨所有日志文件保持一致的标准日志文件结构。每个日志行应该反映一个事件，包括时间戳、主机名、服务和日志器名称等。线程或进程 id、事件 id、会话 id 和用户 id 都可以用作附加值。

其他重要值可以连接到事件的环境，例如实例 ID、部署名称、应用程序版本或任何其他键值对。确保您的时间戳格式包含时区数据并使用高精度时间戳。

最后，如果你想让自己看起来像个专业人士，给每个日志行一个唯一的 ID。一个日志行 通常有一个设置组件和一个可变部件，这使得过滤某些模式变得困难。在这一点上，独特的 ID 很有帮助。为日志错误添加错误 ID。当您需要在知识管理系统中搜索任何内容时，这将派上用场。

### How - 使用度量（Metrics）

度量是日志记录中的一个基本概念。度量是一个随着时间的推移而价值连城的财产，通常有固定的间隔。

以下是常用指标类型的列表:

- `Meter`  - 计算事件发生的频率 (例如: 访问你网站的人数)
- `Timer` - 测量完成一项程序所需的时间(例如: 您的 web 服务器响应时间)
- `Counter` - 整数值递增和递减(例如: 登录用户数目)
- `Gauge`  - 要测量的任意值 (例如: CPU)

每个度量表示某个系统属性的条件。度量最棒的地方在于，您可以拥有很多度量，并将它们彼此关联起来。

!!! note
    建议跟踪和记录度量，或者将度量与您的日志分开。

### How - 日志唯一

大多数初学者都会犯这样一个错误，即将相同的日志消息复制粘贴到许多文件中，导致最终的日志聚合被系统各个部分的相似日志行填充。当他们这样做时，很难确定触发事件的代码中的具体位置。

如果不能更改语句，那么至少要用日志消息指定日志源，以区分最终的日志行。此外，如果父类处理日志记录，请确保在启动时提交标识符，并在为子行为写日志消息时使用它

### How - 提供上下文

日志是由开发人员根据代码编写的。这意味着开发人员在编写代码时将日志基于代码的上下文。不幸的是，阅读日志的个人缺乏上下文，在某些情况下，甚至可以访问源代码。

例如:

1. "The database is unavailable."
2. "Failed to Get users' preferences for user id=1. Configuration Database not responding. Please retry again in 3 minutes."

通过读取第二个日志行，我们可以很容易地推断出应用程序试图完成什么、哪个组件失败，以及是否存在问题的解决方案。每个日志行都应该有足够的信息，让您准确地理解发生了什么以及应用程序当时的状态是什么。

### How - 记录警告和异常处理

尽管日志异常是日志记录最重要的功能之一，但许多程序员将日志记录视为处理异常的一种方法。


### How - 编写日志解析器并主动监听日志

大多数 API 日志记录系统都能够创建自定义日志解析器和过滤器。这些解析器允许我们以更有组织的方式存储日志数据，使查询更加容易和快速。正确组织的日志数据也可以被提供到日志监控和异常检测管理系统中，以便主动监控系统和预测未来事件。

这些技术非常复杂，通过基于时间序列和基于日志数据和其他来源的实时事件分析的交互式仪表板。


## 日志框架

### log4j2

`Log4j 2` 是 `Log4j` 日志框架的新改进版本。最引人注目的改进是"异步日志记录"。

`Log4j 2` 依赖:

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.6.1</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.6.1</version>
</dependency>
```

配置 `Log4j 2` 基于主配置` log4j2.xml`文件。首先要配置的是 `appender`。
这些决定了日志消息将被路由到哪里。目的地可以是"控制台"、"文件"、"套接字"等。

`Log4j 2` 有许多用于不同目的的`eppender`，可以在`Log4j 2`官方网站上找到更多信息。

log4j2.xml：

```mxl
<Configuration status="debug" name="baeldung" packages="">
    <Appenders>
        <Console name="stdout" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} %p %m%n"/>
        </Console>
    </Appenders>
</Configuration>
```

您可以为每个 `appender` 设置一个名称，例如使用 其他控制台名称 而不是 "stdout"。

注意 `PatternLayout` 元素——这决定了消息应该是什么样子。在我们的示例中，模式是基于模式参数设置的，其中`% d` 决定日期模式,`% p` 决定日志级别的输出,`% m` 决定日志消息的输出,`% n` 决定新的行符号。你可以在` Log4j 2`官方页面上找到更多关于 日志模式的信息。

最后， 启用一个(或多个) `appender` ，将他添加到 `<root>` 中

```xml
<Root level="error">
    <AppenderRef ref="STDOUT"/>
</Root>
```

#### 文件输出

有时候你需要在文件中使用日志记录，所以我们会在配置中添加 name = "f-out" 日志记录器:

```xml
<Appenders>
    <File name="fout" fileName="baeldung.log" append="true">
        <PatternLayout>
            <Pattern>%d{yyyy-MM-dd HH:mm:ss} %-5p %m%nw</Pattern>
        </PatternLayout>
    </File>
</Appenders>
```

- `fileName` - 配置日志文件的名称；
- `append` - 此参数的默认值为 `true`，这意味着默认情况下 File appender 将附加到现有文件，而不是截断该文件。

启用 文件日志记录

```xml
<Root level="INFO">
    <AppenderRef ref="stdout" />
    <AppenderRef ref="fout"/>
</Root>
```

#### 异步日志

如果想让 Log4j 2变得异步，需要在 pom.xml 中添加 LMAX disruptor 库。LMAX 干扰器是一个无锁的线程间通信库。

```xml
<dependency>
    <groupId>com.lmax</groupId>
    <artifactId>disruptor</artifactId>
    <version>3.3.4</version>
</dependency>
```

如果想使用 LMAX disruptor，需要在的配置中使用 `< asyncroot >` 而不是 `< root >` 。

```xml
<AsyncRoot level="DEBUG">
    <AppenderRef ref="stdout" />
    <AppenderRef ref="fout"/>
</AsyncRoot>
```

!!! note
    或者，您可以通过将系统属性 `Log4jContextSelector` 设置为 `org.apache.logging.log4j.core.async.AsyncLoggerContextSelector` 来启用异步日志记录。

### Logback

Logback 是 Log4j 的改进版，由同一个开发者开发。

与 Log4j 相比，Logback 有更多的特性，其中许多特性也被引入到 Log4j 2中d

```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.6</version>
</dependency>
```

这个依赖项将传递性地引入另外两个依赖项: `logback-core` 和 `slf4j-api`


配置:

```xml
<configuration>
  # Console appender
  <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
    <layout class="ch.qos.logback.classic.PatternLayout">
      # Pattern of log message for console appender
      <Pattern>%d{yyyy-MM-dd HH:mm:ss} %-5p %m%n</Pattern>
    </layout>
  </appender>

  # File appender
  <appender name="fout" class="ch.qos.logback.core.FileAppender">
    <file>baeldung.log</file>
    <append>false</append>
    <encoder>
      # Pattern of log message for file appender
      <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5p %m%n</pattern>
    </encoder>
  </appender>

  # Override log level for specified package
  <logger name="com.baeldung.log4j" level="TRACE"/>

  <root level="INFO">
    <appender-ref ref="stdout" />
    <appender-ref ref="fout" />
  </root>
</configuration>
```

SLF4J 为大多数 Java 日志框架提供了一个公共接口和抽象。它充当 facade，并提供标准化的 API 来访问日志框架的底层特性。

Logback 使用 SLF4J 作为本地 API 实现其功能:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4jExample {

    private static Logger logger = LoggerFactory.getLogger(Log4jExample.class);

    public static void main(String[] args) {
        logger.debug("Debug log message");
        logger.info("Info log message");
        logger.error("Error log message");
    }
}
```