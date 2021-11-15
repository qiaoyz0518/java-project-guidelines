# 开发前准备

## 环境

!!! caution "重要"
    以下linux 环境 shell 终端使用zsh，如果使用默认的 bash，请编辑 ~/.bashrc 来操作。

### git 安装

=== "ubuntu-linux"

    - 安装 git"

    ``` shell
    $ apt install git
    ```

    - （可选）如果需要下载最新版本的git，请加入如下ppa 仓库

    ```
    $ add-apt-repository ppa:git-core/ppa && apt update && apt install git
    ```

=== "windows"

    1. 下载 [`git for windows`](https://git-scm.com/download/win)， [淘宝镜像](https://npm.taobao.org/mirrors/git-for-windows/)；
    2. 安装 `Git for Windows` (默认已包含 `bit bash`)。

### `SDKMAN`（推荐）

`SDKMAN` 是一个JVM 平台环境集成管理工具，可以使用它很方便对各版本 sdk的进行管理。例如 `jdk`， `gradle`， `maven`等众多jvm工具版本的切换
安装，卸载等。

SDKMAN! 是一个在 unix 系统环境下，为一些 软件工具包 管理多版本问题的工具。提供了一个方便的命令行接口(CLI)， 方便执行 “安装”, “切换”，
 “移除”，“列出” 相关版本工具包的命令。它以前被称为 GVM，即 Groovy 环境管理器，其灵感来自于 Ruby 社区普遍使用的非常有用的 RVM 
 和 rbenv 工具.

- Making life easier. 节省了去官网下载安装的步骤， 省略了环境变量的配置。
- 支持 N 多的 JVM 工具包。例如  Java、 Groovy、 Scala、 Kotlin 和 Ceylon。Ant、 Gradle、 Grails、
Maven、 SBT、 Spark、 Spring Boot、 Vert.x 等。
- 轻量级.用 bash 编写，只需要 curl 和 zip/unzip 就可以出现在系统中，甚至可以与 ZSH 一起工作。

!!! note "提示"
    windows 环境开发需要安装 类似于 `WSL` 一样的linux 集成环境，如果缺乏linux开发经验, 开发环境集成请参阅 SDK [单独手动安装]

#### SDKMAN安装

=== "ubuntu-linux"

    1. 下载并执行安装脚本；

        ```bash
        curl -s "https://get.sdkman.io" | bash
        ```

        > (可选) 将 sdkman 安装到指定位置
            ```bash
            export SDKMAN_DIR="/usr/local/sdkman" && curl -s "https://get.sdkman.io" | bash
            ```

    2. 执行脚本；

        ```bash
        source "$HOME/.sdkman/bin/sdkman-init.sh"
        ```

    3. 验证是否安装完成；

        ```bash
        sdk version
        ```
        显示如下：

        ```bash
        sdkman 5.0.0+51
        ```

=== "windows"

    1. 安装 linux 子系统。现在有很多方式可以在 Windows 上安装 SDKMAN：

        1. 第一个解决方案是：在尝试 SDKMAN 安装之前为 windows 安装 linux 子系统 - WSL。您需要一个基本的工具链，
        包括 `bash`、 `zip` 、 `unzip` 和 `curl` (特殊情况需要 `tar` 和 `gzip` )。

        1. 另一个解决方案是： 在尝试 SDKMAN 安装之前先安装 Cygwin。为了使我们的软件正常运行，我们要求 Cygwin 安装的工具链与 
        WSL 相同。

        2. 第三个解决方案是为使用 Git Bash for Windows 环境的 Git 用户提供的。为了实现这一点，需要用 MinGW 工具对环境进行补充，以添加必要的工具链来实现功能。

    1. 安装 sdkman，参见linux 安装过程。

!!! note "TIP"
    sdkman 安装的 sdk工具包无需设置环境变量。

#### SDK 安装

sdkman 如果官方仓库源太慢，建议使用 “本地版本” 安装的方式安装sdk，然后交过sdkman 做版本管理。参阅 [SDK 本地版本安装] 章节 .

!!! info "注意"
    安装指定版本号，请选择 `Identifier` 的值。

=== "JDK"

    1.查看可选的 java 版本；

      ```bash
      sdk ls java
      ```

    2.选择合适的版本进行安装;

        ```bash
        sdk install java 8.0.292.j9-adpt
        ```
       - （推荐）“本地版本安装” 安装jdk。相关教程移步 [SDK 本地版本安装] 章节 。

=== "gradle"

    1. 查看可选的 gradle 版本；

        ```bash
        sdk ls gradle
        ```

    2. 选择合适的版本进行安装;

        ```bash
        sdk install gradle 4.10.3
        ```

=== "maven"

    1. 查看可选的 maven 版本；

        ```bash
        sdk ls maven
        ```

    2. 选择合适的版本进行安装（可以选择最新版本）;

        ```bash
        sdk install maven
        ```

[SDK 本地版本安装]: #sdk-local-install
[单独手动安装]: #sdk-manual-install

#### SDK 本地版本安装 {: #sdk-local-install}

因为sdkman仓库网络龟速以及众所周知的原因，当出现网络问题时，建议使用 “本地版本” 安装对应的 sdk 版本，然后将版本管理交给sdkman 来管理.

以下以 本地安装 open jdk8 作为例子来阐述安装步骤：

1. 将 jdk 安装包解压到指定目录，例如 `~/.jdks/` ,解压的完整路径为 `~/.jdks/adopt-openjdk-1.8.0_275`,
2. 将本地安装包纳入 sdkman 版本管理 命令格式： sdk install java {自定义版本号} {jdk目录};

    ```bash
    sdk install java 8.0.275 ~/.jdks/adopt-openjdk-1.8.0_275
    ```

3. 激活版本；

    ```bash
    sdk use java 8.0.275
    ```

4. 验证当前的 sdkman 管理的当前 java 版本,插卡输出结果。

    ```bash
    sdk current java
    java -version
    ```  

#### 常用命令

<!-- markdownlint-disable MD013 -->
```txt
commands:
    install   or i    <candidate> [version] [local-path]                安装sdk
    uninstall or rm   <candidate> <version>                             卸载sdk
    list      or ls   [candidate]                                       列出支持安装的sdk信息
    use       or u    <candidate> <version>                             当前使用 sdk 的版本
    config                                                              编辑 sdk 配置
    default   or d    <candidate> [version]                             默认使用 sdk 版本
    home      or h    <candidate> <version>                             显示 sdk 的home目录
    env       or e    [init|install|clear]                              为当前项目初始化，安装，清理sdk的环境      
    current   or c    [candidate]                                       显示当前 sdk 使用的版本
    flush             [archives|tmp|broadcast|metadata|version]         清理  archives|tmp|broadcast|metadata|version 等缓存空间 
```
<!-- markdownlint-enable MD013 -->

### 单独手动安装 {: #sdk-manual-install}

单独安装 sdk 请参阅 官方文档。本节不做描述。

以下为官方 sdk下载网址：

- [JDK8]
- [GRADLE]
- [MAVEN]

[JDK8]: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
[GRADLE]: https://gradle.org/releases/
[MAVEN]: https://maven.apache.org/download.cgi

### 环境变量配置

windows 环境变量配置窗口： 右键 This PC(此电脑) -> Properties（属性） ->
Advanced system settings（高级系统设置） -> Environment Variables（环境变量）...

#### JDK

- JAVA_HOME: 一个 JDK 比较重要的环境，很多基于 java环境的工具默认使用该环境变量。

=== "linux"

    ```bash
    echo "           
    JAVA_HOME=/home/luwei/.sdkman/candidates/java/current
    export JAVA_HOME
    export CLASSPATH=.:${JAVA_HOME}/lib/tools.jar:${JAVA_HOME}/lib/dt.jar
    export PATH=$JAVA_HOME/bin:\$PATH" >> ~/.zshrc
    ```

=== "windows"

    1. 打开 环境变量窗口
        

    1. 新建JAVA_HOME 变量
        点击 New（新建）... 按钮

        输入:

        变量名：JAVA_HOME  
        变量值：电脑上JDK安装的绝对路径  
        输入完毕后点击 OK。

    1. 新建/修改 `CLASSPATH` 变量
        如果存在 `CLASSPATH` 变量，选中点击 Edit(编辑)。

        如果没有，点击 New（新建）... 新建。

        输入/在已有的变量值后面添加：

        变量名：`CLASSPATH`  
        变量值：`.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar;`  
        点击 OK 保存。

    1. 修改 `Path` 变量
        由于 win10 的不同，当选中 Path 变量的时候，系统会很方便的把所有不同路径都分开了，不会像 win7 或者 win8 那样连在一起。

        新建两条路径：

        - `%JAVA_HOME%\bin`
        - `%JAVA_HOME%\jre\bin`

    1. 检查 打开 cmd，输入 java，出现一连串的指令提示，说明配置成功了:

#### GRADLE

- GRADLE_HOME: GRADLE 的安装路径。
- GRADLE_USER_HOME: 存放 gradle 缓存包的路径。以替换 `~/.gradle` 路径。

=== "linux"

    ```bash
    echo "           
    GRADLE_HOME=/data/home/luwei/.sdkman/candidates/gradle/4.10.3
    export GRADLE_HOME
    export GRADLE_USER_HOME=/data/home/luwei/localGradle
    export PATH=$GRADLE_HOME/bin:\$PATH" >> ~/.zshrc
    ```

=== "windows"

    1. 打开 环境变量窗口

    2. 新建 `GRADLE_HOME` 变量
        点击 New（新建）... 按钮

        输入:

        变量名： `GRADLE_HOME`
        变量值：电脑上 `gradle` 安装的绝对路径
        输入完毕后点击 OK。

    3. 新建/修改 `GRADLE_USER_HOME` 变量
        如果存在 `GRADLE_USER_HOME` 变量，选中点击 Edit(编辑)。

        如果没有，点击 New（新建）... 新建。

        输入/在已有的变量值后面添加：

        变量名：`GRADLE_USER_HOME`  
        变量值：`d://gradl_cache`
        点击 OK 保存。

    4. 修改 `Path` 变量

        新建路径：

        - `%GRADLE_HOME%\bin`

    5. 检查 打开 cmd，输入 `gradle --v`，出现一连串的指令提示，说明配置成功了

#### MAVEN

- `MAVEN_HOME`: maven 的安装路径。

=== "linux"

    ```bash
    echo "           
    MAVEN_HOME=/data/home/luwei/.sdkman/candidates/maven/3.8.3
    export MAVEN_HOME
    export PATH=$MAVEN_HOME/bin:\$PATH" >> ~/.zshrc

    ```

=== "windows"

    1. 打开 环境变量窗口

    2. 新建 `MAVEN_HOME` 变量
        点击 New（新建）... 按钮

        输入:

        变量名： `MAVEN_HOME`
        变量值：电脑上 `maven` 安装的绝对路径
        输入完毕后点击 OK。

    3. 修改 `Path` 变量

        新建路径：

        - `%MAVEN_HOME%\bin`

    4. 检查 打开 cmd，输入 `mvn --v`，出现一连串的指令提示，说明配置成功了

## 项目实践

### 初始化项目

由于我们使用 `gradle` 作为应用管理工具，因此使用 `gradle init` 初始化一个项目骨架貌似是一个不错的选择。
当然也可以选择 `mvn init`,  idea工具, [spring initializr](https://start.spring.io) 等等方式来初始化项目

1. 启动终端,初始化；

    ```bash
    gradle init
    ```

1. 文件确认；

    可以看到一共创建了2个目录和6个文件，其中2个目录和4个文件都跟wrapper有关：

    ```txt
    .
    ├── build.gradle
    ├── gradle
    │   └── wrapper
    │       ├── gradle-wrapper.jar
    │       └── gradle-wrapper.properties
    ├── gradlew
    ├── gradlew.bat
    └── settings.gradle

    ```

    - gradlew：linux或者Unix下用于执行wrapper命令的Shell脚本
    - gradlew.bat：Windows下用于执行wrapper命令的批处理脚本
    - gradle-wrapper.jar：用于下载Gradle的相关代码实现
    - gradle-wrapper.properties：wrapper所使用的配置信息，比如gradle的版本等信息
    - build.gradle: 用于存放构建相关的Task

1. 增加源码包

    在项目根目录创建以下目录

    - `src/main/java`: java 源码目录；
    - `src/main/resources`: 项目静态资源文件目录；
    - `src/test`: 单元测试目录；
  
### 项目管理

根目录 `build.gradle` 包含有整个项目的构建脚本。它的工作包含整个项目的源码构成，插件管理，依赖管理等

最简单的项目 脚本如下：

```groovy
plugins {                                                             #(1)
    id 'org.springframework.boot' version '2.4.12'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '1.8'

repositories {                                                      #(2) 
    mavenCentral()
}

dependencies {                                                     #(3) 
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

test {
    useJUnitPlatform()
}

```


``` yaml
theme:
  features:
    - content.code.annotate # (1)
```

1. 项目插件管理.
1. 依赖包仓库源；
1. 依赖。
  
### 构建项目

```bash
gradle clean build
```
  
### 运行项目

```
java -jar demo.jar
```