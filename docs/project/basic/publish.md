## 目标

目标： 在现有测试环境使用 `jekins` pipline 脚本部署上述开发的开始项目。

前提: 

1. jekins 服务器。
1. web 运行服务器；
1. Gitlab 代码版本控制服务；


## 构建

运行命令 `gradlew clean build`

```bash
Starting a Gradle Daemon, 1 incompatible Daemon could not be reused, use --status for details

> Task :test
16:10:59.572 [SpringContextShutdownHook] DEBUG org.springframework.web.context.support.GenericWebApplicationContext - Closing org.springframework.web.context.support.GenericWebApplicationContext@958c9bcb, started on Wed May 18 16:
10:57 CST 2022

BUILD SUCCESSFUL in 19s
4 actionable tasks: 2 executed, 2 up-to-date
C:\Users\luwei\Desktop\tendata\kpi\documents\tendata-jstart>gradlew.bat build

BUILD SUCCESSFUL in 3s
7 actionable tasks: 7 up-to-date

```

!!! info
    第一次运行构建时，Gradle 将检查您的 `~/.gradle` 下的缓存中是否已经具有所需的依赖项。如果没有，依赖包将被下载并存储在这里。下次运行构建时，将使用缓存的版本。生成任务编译类、运行测试并生成测试报告。

您可以通过打开位于 `build/reports/tests/test/index.html` 中的 HTML 文件来查看测试报告。

您可以在名为s `build/libs` 目录中找到新打包的 JAR 文件。通过运行以下命令验证存档是否有效:

```bash
$ jar tf build/libs/tendata-jstart-0.0.1-SNAPSHOT.jar
META-INF/
META-INF/MANIFEST.MF
org/
org/springframework/
org/springframework/boot/
org/springframework/boot/loader/
org/springframework/boot/loader/ClassPathIndexFile.class
org/springframework/boot/loader/ExecutableArchiveLauncher.class
org/springframework/boot/loader/JarLauncher.class
org/springframework/boot/loader/LaunchedURLClassLoader$DefinePackageCallType.class
org/springframework/boot/loader/LaunchedURLClassLoader$UseFastConnectionExceptionsEnumeration.class
org/springframework/boot/loader/LaunchedURLClassLoader.class
org/springframework/boot/loader/Launcher.class
org/springframework/boot/loader/MainMethodRunner.class
org/springframework/boot/loader/PropertiesLauncher$1.class
...
org/springframework/boot/loader/util/
org/springframework/boot/loader/util/SystemPropertyUtils.class
BOOT-INF/
BOOT-INF/classes/
BOOT-INF/classes/cn/
BOOT-INF/classes/cn/tendata/
BOOT-INF/classes/cn/tendata/jstart/
BOOT-INF/classes/cn/tendata/jstart/Application$JpaConfig.class
BOOT-INF/classes/cn/tendata/jstart/Application.class
BOOT-INF/classes/cn/tendata/jstart/config/
BOOT-INF/classes/cn/tendata/jstart/config/jackson/
BOOT-INF/classes/cn/tendata/jstart/config/jackson/databind/
BOOT-INF/classes/cn/tendata/jstart/config/jackson/databind/PageSerializer.class
BOOT-INF/classes/cn/tendata/jstart/config/JacksonConfig$EmptyStringAsNullDeserializer.class
BOOT-INF/classes/cn/tendata/jstart/config/JacksonConfig.class
...
BOOT-INF/classes/cn/tendata/jstart/service/OwnerService.class
BOOT-INF/classes/cn/tendata/jstart/service/OwnerServiceImpl.class
BOOT-INF/classes/application.yml
BOOT-INF/classes/swagger/
BOOT-INF/classes/swagger/api.yml
BOOT-INF/classes/swagger/parameters/
BOOT-INF/classes/swagger/parameters/path/
BOOT-INF/classes/swagger/parameters/path/ownerId.yml
BOOT-INF/classes/swagger/parameters/_index.yml
BOOT-INF/classes/swagger/resources/
BOOT-INF/classes/swagger/resources/owner.yml
BOOT-INF/classes/swagger/resources/owners.yml
BOOT-INF/classes/swagger/schemas/
BOOT-INF/classes/swagger/schemas/component.yml
BOOT-INF/classes/swagger/schemas/Error.yaml
BOOT-INF/lib/
BOOT-INF/lib/h2-1.4.200.jar
BOOT-INF/lib/jackson-datatype-jdk8-2.13.2.jar
BOOT-INF/lib/spring-data-jpa-2.6.4.jar
BOOT-INF/lib/spring-aspects-5.3.19.jar
BOOT-INF/lib/spring-webmvc-5.3.19.jar
BOOT-INF/lib/spring-web-5.3.19.jar
BOOT-INF/lib/tomcat-embed-el-9.0.62.jar
BOOT-INF/lib/hibernate-validator-6.2.3.Final.jar
BOOT-INF/lib/javax.transaction-api-1.3.jar
BOOT-INF/lib/spring-boot-autoconfigure-2.6.7.jar
BOOT-INF/lib/spring-boot-2.6.7.jar
BOOT-INF/lib/spring-context-5.3.19.jar
BOOT-INF/lib/spring-aop-5.3.19.jar
BOOT-INF/lib/aspectjweaver-1.9.7.jar
BOOT-INF/lib/HikariCP-4.0.3.jar
BOOT-INF/lib/spring-orm-5.3.19.jar
BOOT-INF/lib/spring-jdbc-5.3.19.jar
...
BOOT-INF/classpath.idx
BOOT-INF/layers.idx

```

您应该看到所需的清单文件ー manifest.mf ー和编译后的 Library 类

## 注册服务
1. 上传jar 包到Web服务器，目录为 `~/server/tendata/jstart`,并修改名称为 `tendata-jstart.jar`,并使用命令 `sudo chmod +x tendata-jstart.jar` 为其授予执行权限。


1. 进入Web服务器目录
    ```bash
     $ cd /usr/lib/systemd/system
    ```

1. copy 以下内容到 `tendata-jstart.service` 文件中（服务名 `tendata-jstart` ）
    ```ini
    [Unit]
    Description=tendata java start project
    After=syslog.target network.target
    
    [Service]
    ExecStart=/home/tend/server/tendata/jstart/tendata-jstart.jar
    SuccessExitStatus=143
    User=root
    Group=root
    
    [Install]
    WantedBy=multi-user.target
    ```

!!! tip
    在web服务器可以使用 `sudo systemctl start tendata-jstart` 单独启动服务。

## 配置 jekins

针对jekins 配置，这里只针对 jenkins 的pipline 脚本进行配置。其他基础配置例如新建项目等，可在测试环境摸索。

```groovy
pipeline {
  agent any
 
  tools {
    git "git"
  }
 
  stages {
    stage("Check out"){
      steps {
        git  branch: "develop",credentialsId: '3af5404b-e7b4-462d-8a74-fcebc456b498', url: 'http://xxxx.com.cn/tendata/develop/tendata-jstart.git'
      }
    }
         
    stage('Build') {
      steps {
        sh "chmod +x gradlew"
        sh "./gradlew clean build"
      }
    }
     
    stage('Deploy') {
      steps {
        sh "scp -i /var/jenkins_home/ssh/id_rsa -P 22 -r tendata-jstart/build/libs/tendata-jstart-*.jar tend@192.168.xxx.xxx:/home/tend/server/tendata/jstart/"
        sh "ssh -i /var/jenkins_home/ssh/id_rsa tend@192.168.xxx.xxx 'cd jenkins && ./deploy.sh /home/tend/server/tendata/jstart tendata-jstart tendata-jstart-server ${BUILD_NUMBER}'"
      }
    }
  }
}
```

在本脚本中， 主要有以下几个操作:

1. 使用 jenkins 的 git 插件；
1. 从 Git 仓库检出 项目代码；
1. 将检出代码构建为jar 包；
1. 将jar 复制到远程web服务器；
1. 运行脚本启动 `tendata-jstart` 服务。