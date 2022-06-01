## 全局异常

在 Java 中，异常处理是通过 `try`、`catch` 块完成的，但是 `Spring Boot` 还允许我们提供自定义的全局异常处理，我们不需要在任何地方添加 `try catch` 块，我们可以创建一个单独的类来处理异常，它还可以分离来自业务逻辑代码异常处理的代码。

### `@ControllerAdvice`

从 spirng 3.2 后，支持 带有 `@ControllerAdvice` 注释的 `@@ExceptionHandler` 异常处理方式。

这使得它脱离旧的 MVC 模型（只能针对单一 controller），并利用 `ResponseEntity` 和 `@ExceptionHandler` 保证了数据类型安全性和灵活性:

```java
@ControllerAdvice
public class RestResponseEntityExceptionHandler 
  extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value 
      = { IllegalArgumentException.class, IllegalStateException.class })
    protected ResponseEntity<Object> handleConflict(
      RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "This should be application specific";
        return handleExceptionInternal(ex, bodyOfResponse, 
          new HttpHeaders(), HttpStatus.CONFLICT, request);
    }
}
```

`@controlleradvice` 注释允许我们将以前分散的多个 `@ExceptionHandler` 合并到一个单一的全局错误处理组件中。

实际的机制非常简单，但也非常灵活:

- 它使我们能够完全控制响应body以及状态代码
- 它提供了几个异常到同一个方法的映射，这些异常一起处理

!!! note
    `@ExceptionHandler` 声明的异常与作为方法参数使用的异常,如果他们不匹配，编译器是不会报错的，spring 也不会报错。但是当异常在运行时被抛出时，异常解析机制将会失败 `java.lang.IllegalStateException: No suitable resolver for argument [0] [type=...]
HandlerMethod details: ..`

### `ResponseStatusException`

自 spring 5 后又引入了 `ResponseStatusException` 类

```java
@GetMapping(value = "/{id}")
public Foo findById(@PathVariable("id") Long id, HttpServletResponse response) {
    try {
        Foo resourceById = RestPreconditions.checkFound(service.findOne(id));

        eventPublisher.publishEvent(new SingleResourceRetrievedEvent(this, response));
        return resourceById;
     }
    catch (MyResourceNotFoundException exc) {
         throw new ResponseStatusException(
           HttpStatus.NOT_FOUND, "Foo Not Found", exc);
    }
}
```

使用它的好处有以下几方面:

1. 非常适合原型开发: 我们可以很快地实现一个基本的解决方案;
1. 一个类，多个状态码： 一种异常类，可以适配多种不同的返回方案。**与`@ExceptionHandler` 相比，这减少了紧密耦合**
1. 不必创建如此多的自定义异常类；
1. 对异常处理有更多的控制，因为可以通过编程方式创建异常

!!! question
    那么和 全局异常比起来，如何权衡？

 1. 如果没有全局异常处理。
 1. 代码复制。如果你从其他地方controller复制的代码，那么在没弄明白之前不要修改它。

!!! note
    在一个应用程序中结合不同的方法实现需求是可能的。例如，我们可以在全局使用`@ControllerAdvice`，但也可以在本地实现 `responsestatusexception`。
!!! caution
    如果同一个异常，有多个处理方式或者返回，往往会引发一些令人费解的问题，这需要特别注意。   

## 目前解决方案

``` java
package cn.tendata.bizr.rest.controller;

import cn.tendata.bizr.core.BasicErrorCodeException;
import cn.tendata.bizr.core.DefaultMessageSource;
import cn.tendata.bizr.rest.jackson.WebDataView;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler implements MessageSourceAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    private MessageSourceAccessor messages = DefaultMessageSource.getAccessor();

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers,
                                                         HttpStatus status, WebRequest request) {
        return handleValidationException(ex.getBindingResult(), status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleValidationException(ex.getBindingResult(), status);
    }

    private ResponseEntity<Object> handleValidationException(BindingResult result, HttpStatus status) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());
        if (result != null) {
            if (result.hasGlobalErrors()) {
                List<Error> globalErrors = new ArrayList<>(6);
                for (ObjectError err : result.getGlobalErrors()) {
                    Error error = new Error();
                    error.message = messages.getMessage(err.getCode(), err.getDefaultMessage());
                    globalErrors.add(error);
                }
                errorAttributes.put("errors", globalErrors);
            }
            if (result.hasFieldErrors()) {
                List<Error> fieldErrors = new ArrayList<>(6);
                for (FieldError err : result.getFieldErrors()) {
                    Error error = new Error();
                    error.field = err.getField();
                    error.rejected = err.getRejectedValue();
                    error.message = messages.getMessage(err.getCode(), err.getDefaultMessage());
                    fieldErrors.add(error);
                    errorAttributes.put("fieldErrors", fieldErrors);
                }
            }
            errorAttributes.put("message", messages.getMessage("error.VALIDATION_ERROR", "Validation failed for object='"
                    + result.getObjectName() + "'. Error count: " + result.getErrorCount()));
        }
        return new ResponseEntity<>(errorAttributes, status);
    }

    @JsonInclude(Include.NON_EMPTY)
    static class Error {
        public String field;
        public Object rejected;
        public String message;
    }

    @JsonView(WebDataView.Basic.class)
    @ExceptionHandler(BasicErrorCodeException.class)
    public ResponseEntity<?> handleErrorCodeException(BasicErrorCodeException ex, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());
        errorAttributes.put("message", messages.getMessage("error." + ex.getErrorCode(), ex.getMessage()));
        errorAttributes.put("body", ex.getBody());
        return new ResponseEntity<>(errorAttributes, status);
    }

    @ExceptionHandler({EmptyResultDataAccessException.class, EntityNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleNotFoundException() {
    }

    @ExceptionHandler(Exception.class)
    public void handleUncaughtException(Exception ex, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (ex instanceof AccessDeniedException) {
            throw ex;
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logException(ex, request, status);
        String message = messages.getMessage("error.INTERNAL_SERVER_ERROR", ex.getLocalizedMessage());
        response.sendError(status.value(), message);
    }

    private void logException(Exception ex, HttpServletRequest request, HttpStatus status) {
        if (LOGGER.isErrorEnabled() && status.value() >= 500 || LOGGER.isInfoEnabled()) {
            Marker marker = MarkerFactory.getMarker(ex.getClass().getName());
            String uri = request.getRequestURI();
            if (request.getQueryString() != null) {
                uri += '?' + request.getQueryString();
            }
            String msg = String.format("%s %s ~> %s", request.getMethod(), uri, status);
            if (status.value() >= 500) {
                LOGGER.error(marker, msg, ex);
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(marker, msg, ex);
            } else {
                LOGGER.info(marker, msg);
            }
        }
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

}

```

1. 以上解决方案中，通过继承 `ResponseEntityExceptionHandler` 类实现对通用 `spring mvc` 标准异常集中化处理，返回标准的 `ResponseEntity` 对象。
1. 重写了 `ResponseEntityExceptionHandler` 类对 `BindException`, `MethodArgumentNotValidException` 字段验证绑定异常处理，使返回异常更加清晰和机构化。

    ```java
       static class Error {
        public String field;
        public Object rejected;
        public String message;
    }
    ```

1. 通过 `@ExceptionHandler` 注解扩展对 `BasicErrorCodeException` 自定义异常的处理，并支持异常国际化。

1. 通过 `@ExceptionHandler` 和  `@ResponseStatus(value = HttpStatus.NOT_FOUND)` 两个注释对 `EmptyResultDataAccessException`, `EntityNotFoundException` 进行异常处理，当出现以上异常时，返回 `404`.


## Effective

当充分利用好异常时，可以提高程序的可读性、可靠性和可维护性。如果使用不当，则会产生负面效果。

### 不要忽略异常

虽然这一建议似乎显而易见，但它经常被违反，因此值得强调。当 API 的设计人员声明一个抛出异常的方法时，他们试图告诉你一些事情。不要忽略它！如果在方法调用的周围加上一条 `try` 语句，其 `catch` 块为空，可以很容易忽略异常

空 `catch` 块违背了异常的目的， 它的存在是为了强制你处理异常情况。忽略异常类似于忽略火灾警报一样，关掉它之后，其他人就没有机会看到是否真的发生了火灾。**你可能侥幸逃脱，但结果可能是灾难性**的。每当你看到一个空的 `catch` 块，你的脑海中应该响起警报。

在某些情况下，忽略异常是合适的。例如，在关闭 `FileInputStream` 时，忽略异常可能是合适的。你没有更改文件的状态，因此不需要执行任何恢复操作，并且已经从文件中读取了所需的信息，因此没有理由中止正在进行的操作。**记录异常可能是明智的，这样如果这些异常经常发生，你应该研究起因** 。如果你选择忽略异常，`catch` 块应该包含一条注释，解释为什么这样做，并且应该将变量命名为 `ignore`：

```java
Future<Integer> f = exec.submit(planarMap::chromaticNumber);
int numColors = 4; // Default; 
    numColors = f.get(1L, TimeUnit.SECONDS);
}
catch (TimeoutException | ExecutionException ignored) {
    // 使用 default: minimal coloring is desirable, not required
}
```

!!! tip
    本建议同样适用于 `checked` 异常和 `unchecked` 异常。不管异常是表示可预测的异常条件还是编程错误，用空 `catch` 块忽略它将导致程序在错误面前保持静默。然后，程序可能会在未来的任意时间点，在与问题源没有明显关系的代码中失败。正确处理异常可以完全避免失败。仅仅让异常向外传播，可能会导致程序走向失败，保留信息有利于调试。

### 仅在确有异常使用

```java
try {
    int i = 0;
    while(true)
        range[i++].climb();
    }
    catch (ArrayIndexOutOfBoundsException e) {
}
```

这是一个用于遍历数组的元素的非常糟糕的习惯用法。当试图访问数组边界之外的数组元素时，通过抛出、捕获和忽略 `ArrayIndexOutOfBoundsException` 来终止无限循环

标准做法是:
```java
for (Mountain m : range)
    m.climb();
```

利用错误判断机制来提高性能是错误的。这种思路有三点误区：

1. 因为异常是为异常情况设计的，所以 JVM 实现几乎不会让它们像显式测试一样快。
1. 将代码放在 try-catch 块中会抑制 JVM 可能执行的某些优化；
1. 遍历数组的标准习惯用法不一定会导致冗余检查。许多 JVM 实现对它们进行了优化。

事实上，基于异常的用法比标准用法慢得多。用 100 个元素的数组测试，基于异常的用法与标准用法相比速度大约慢了两倍。

**异常只适用于确有异常的情况；它们不应该用于一般的控制流程**。 更进一步说，使用标准的、易于识别的习惯用法，而不是声称能够提供更好性能的过于抖机灵的技术。即使性能优势是真实存在的，但在稳步改进平台实现的前提下，这种优势也并不可靠。而且，来自抖机灵的技术存在的细微缺陷和维护问题肯定会继续存在。

这个原则对 API 设计也有影响。一个设计良好的 API 不能迫使其客户端为一般的控制流程使用异常。只有在某些不可预知的条件下才能调用具有「状态依赖」方法的类，通常应该有一个单独的「状态测试」方法，表明是否适合调用「状态依赖」方法。例如，`Iterator` 接口具有「状态依赖」的 `next` 方法和对应的「状态测试」方法 `hasNext`。这使得传统 `for` 循环（在 `for-each` 循环内部也使用了 `hasNext` 方法）在集合上进行迭代成为标准习惯用法：
```java
for (Iterator<Foo> i = collection.iterator(); i.hasNext(); ) {
    Foo foo = i.next();
    ...
}
```

如果 `Iterator` 缺少 `hasNext` 方法，客户端将被迫这样做：
```java
try {
    Iterator<Foo> i = collection.iterator();
    while(true) {
        Foo foo = i.next();
        ...
    }
}
catch (NoSuchElementException e) {
}
```

这与一开始举例的对数组进行迭代的例子非常相似，除了冗长和误导之外，基于异常的循环执行效果可能很差，并且会掩盖系统中不相关部分的 bug。

提供单独的「状态测试」方法的另一种方式，就是让「状态依赖」方法返回一个空的 `Optional` 对象，或者在它不能执行所需的计算时返回一个可识别的值，比如 null。

有一些指导原则，帮助你在 **「状态测试」方法**、**Optional**、 **可识别的返回值** 之间进行选择。

1. 如果要在没有外部同步的情况下并发地访问对象，或者受制于外部条件的状态转换，则必须使用 Optional 或可识别的返回值，因为对象的状态可能在调用「状态测试」方法与「状态依赖」方法的间隔中发生变化。
1. 如果一个单独的「状态测试」方法重复「状态依赖」方法的工作，从性能问题考虑，可能要求使用 Optional 或可识别的返回值。
1. 在所有其他条件相同的情况下，「状态测试」方法略优于可识别的返回值。它提供了较好的可读性，而且不正确的使用可能更容易被检测：如果你忘记调用「状态测试」方法，「状态依赖」方法将抛出异常，使错误显而易见；
1. 如果你忘记检查一个可识别的返回值，那么这个 bug 可能很难发现。但是这对于返回 Optional 对象的方式来说不是问题。

### `checked` 和 unchecked

Java 提供了三种 `Throwable`：`checked exception`, `runtime exception`, 和 errors。程序员们对什么时候使用这些可抛出项比较困惑。虽然决策并不总是明确的，但是有一些通用规则可以提供有力的指导。


#### checked

使用 `checked` 异常的情况是为了合理地期望调用者能够从中恢复。 通过抛出一个 `checked` 的异常，你可以强制调用者在 `catch` 子句中处理异常，或者将其传播出去.
因此，方法中声明的要抛出的每个 `checked` 异常，都清楚的向 API 用户表明: 这是可能出现的调用结果。

!!! note
    通过向用户提供 `checked` 异常，API 设计者提供了从异常条件中恢复的接口。用户无视异常处理要求，可以捕获异常并忽略，但这通常不是一个好的做法，参见 [不要忽略异常](#不要忽略异常) 章节。

API 设计人员常常忘记异常也是一个成熟对象，可以为其定义任意方法。

此类方法的主要用途是提供捕获异常的代码，并提供有关引发异常的附加信息。如果缺乏此类方法，程序员需要自行解析异常的字符串表示以获取更多信息。这是极坏的做法。这种类很少指定其字符串表示的细节，因此字符串表示可能因实现而异，也可能因版本而异。因此，解析异常的字符串表示形式的代码可能是不可移植且脆弱的

因为 `checked` 异常通常表示可恢复的条件，所以这类异常来说，设计能够提供信息的方法来帮助调用者从异常条件中恢复尤为重要。例如，假设当使用礼品卡购物由于资金不足而失败时，抛出一个 `checked` 异常。该异常应提供一个访问器方法来查询差额。这将使调用者能够将金额传递给购物者.



#### unchecked

有两种 `unchecked `的可抛出项：运行时异常和错误。它们在行为上是一样的：**都是可抛出的，通常不需要也不应该被捕获**。

如果程序抛出 `unchecked `异常或错误，通常情况下是不可能恢复的，如果继续执行，弊大于利。如果程序没有捕获到这样的可抛出项，它将导致当前线程停止，并发出适当的错误消息。

**使用运行时异常来指示编程错误**。 绝大多数运行时异常都表示操作违反了先决条件。违反先决条件是指使用 API 的客户端未能遵守 API 规范所建立的约定。例如，数组访问约定指定数组索引必须大于等于 0 并且小于等于 length-1 （length：数组长度）。`ArrayIndexOutOfBoundsException` 表示违反了此先决条件。

有个问题，并不总能清楚是在处理可恢复的条件还是编程错误，例如，考虑资源耗尽的情况，这可能是由编程错误（如分配一个不合理的大数组）或真正的资源短缺造成的

如果资源枯竭是由于暂时短缺或暂时需求增加造成的，这种情况很可能是可以恢复的。对于 API 设计人员来说，判断给定的资源耗尽实例是否允许恢复是一个问题。

!!! 总结
    如果你认为某个条件可能允许恢复，请使用 `checked` 异常；如果没有，则使用运行时
    
#### error

虽然 Java 语言规范没有要求，但有一个约定俗成的约定，即错误保留给 JVM 使用，以指示：资源不足、不可恢复故障或其他导致无法继续执行的条件。

!!! tip
    考虑到这种约定被大众认可，所以最好不要实现任何新的 Error 子类。因此，你实现的所有 `unchecked `可抛出项都应该继承 RuntimeException（直接或间接）。不仅不应该定义 Error 子类，而且除了 AssertionError 之外，不应该抛出它们。
    
!!! tip
    普通 `checked` 异常是 Exception 的子类，但不是 RuntimeException 的子类

!!! 总结
    总而言之，为可恢复条件抛出 `checked` 异常，为编程错误抛出 `unchecked `异常。当有疑问时，抛出 `unchecked `异常。不要定义任何既不是 `checked` 异常也不是运行时异常的自定义异常。应该为 `checked` 异常设计相关的方法，如提供异常信息，以帮助恢复。

### 复用标准异常

代码复用是一件好事，异常也不例外。Java 库提供了一组异常，涵盖了大多数 API 的大多数异常抛出需求。

复用标准异常有几个好处：

1. 其中最主要的是，它使你的 API 更容易学习和使用，因为它符合程序员已经熟悉的既定约定。
1. 其次，使用你的 API 的程序更容易阅读，因为它们不会因为不熟悉的异常而混乱。最后（也是最不重要的），更少的异常类意味着更小的内存占用和更少的加载类的时间

常见可复用异常:

1. 最常见的复用异常类型是 `IllegalArgumentException` 。这通常是调用者传入不合适的参数时抛出的异常。例如，如果调用者在表示某个操作要重复多少次的参数中传递了一个负数，则抛出这个异常。
1. 另一个常被复用异常是 `IllegalStateException`。如果接收对象的状态导致调用非法，则通常会抛出此异常。例如，如果调用者试图在对象被正确初始化之前使用它，那么这将是抛出的异常。
1. 最后一个需要注意的标准异常是 `UnsupportedOperationException`。如果对象不支持尝试的操作，则抛出此异常。它很少使用，因为大多数对象都支持它们的所有方法。此异常用于一个类没有实现由其实现的接口定义的一个或多个可选操作。例如，对于只支持追加操作的 List 实现，试图从中删除元素时就会抛出这个异常。

!!! caution
    不要直接复用 Exception、RuntimeException、Throwable 或 Error。 应当将这些类视为抽象类。你不能对这些异常进行可靠的测试，因为它们是方法可能抛出的异常的超类。

如果一个异常符合你的需要，那么继续使用它，但前提是你抛出它的条件与异常的文档描述一致：复用必须基于文档化的语义，而不仅仅是基于名称。另外，如果你想添加更多的细节，可以随意子类化标准异常，但是记住，异常是可序列化的。如果没有充分的理由，不要编写自己的异常类。

!!! question
    考虑一个对象，表示一副牌，假设有一个方法代表发牌操作，该方法将手牌多少作为参数。如果调用者传递的值大于牌堆中剩余的牌的数量，则可以将其解释为 `IllegalArgumentException` （handSize 参数值太大）或 `IllegalStateException`（牌堆中包含的牌太少）。在这种情况下，规则是：如果没有参数值，抛出 `IllegalStateException`，否则抛出 `IllegalArgumentException`。

