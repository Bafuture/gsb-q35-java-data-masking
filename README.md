# gsb-q35-java-data-masking

注解驱动的 Java 数据脱敏库，零第三方运行时依赖（仅测试使用 JUnit 5 / AssertJ）。
支持递归对象图、循环引用、分组场景、副本/Map/JSON 多种输出，以及日志框架钩子。

## 快速开始

```java
class User {
    @Sensitive(strategy = PhoneMaskingStrategy.class)
    String phone;

    @Sensitive(strategy = IdCardMaskingStrategy.class, groups = "log")
    String idCard;

    @SensitiveIgnore
    String traceId;
}

MaskingEngine engine = new MaskingEngine(
        MaskingConfig.builder().activeGroups("log").build());

User masked = engine.mask(user);          // 脱敏副本，原对象不变
Object map  = engine.toMaskedMap(user);   // 脱敏后的 Map/List 结构
String json = engine.toMaskedJson(user);  // 脱敏后的 JSON 字符串
```

构建与测试：

```bash
./mvnw -q verify
```

## 内置策略清单

| 策略类 | 规则 | 示例 |
|---|---|---|
| `PhoneMaskingStrategy` | 保留前 3 位 + 后 4 位 | `13812345678` → `138****5678` |
| `IdCardMaskingStrategy` | 保留前 6 位（地区码）+ 后 4 位 | `110101199001011234` → `110101********1234` |
| `BankCardMaskingStrategy` | 仅保留后 4 位 | `6222021234567891234` → `***************1234` |
| `EmailMaskingStrategy` | 本地部分保留首字符 | `zhangsan@example.com` → `z***@example.com` |
| `NameMaskingStrategy` | 保留首字 | `欧阳娜娜` → `欧***` |
| `AddressMaskingStrategy` | 保留到区县级（前 6 字） | `北京市海淀区中关村大街27号` → `北京市海淀区****` |

以上策略均位于 `com.example.gsb.masking.strategy` 包。

## 递归与循环引用

引擎递归处理嵌套对象、集合、Map 与数组：

- `mask()` 通过 `IdentityHashMap` 记录已复制对象，循环引用在副本中被原样保留，
  不会栈溢出；
- `toMaskedMap()` / `toMaskedJson()` 中循环引用以 `[Circular Reference]` 占位；
- `mask()` 全程只读原对象，副本为深拷贝，测试见 `CopyImmutabilityTest`。

## 分组与排除

`@Sensitive` 可重复声明，按 `groups` 区分场景；引擎按声明顺序选择第一条
分组匹配的规则，未声明分组的规则始终生效：

```java
class Account {
    @Sensitive(strategy = PhoneMaskingStrategy.class, groups = "log")
    @Sensitive(strategy = IdCardMaskingStrategy.class, groups = "audit")
    String phone;
}

new MaskingEngine(MaskingConfig.builder().activeGroups("log").build());   // 日志场景
new MaskingEngine(MaskingConfig.builder().activeGroups("audit").build()); // 审计场景
```

`@SensitiveIgnore` 标注的字段完全不参与脱敏（含其内部对象），按原样保留。

## 自定义策略

实现 `MaskingStrategy` 接口（需 public 无参构造器），在注解中引用即可：

```java
public class HashTagStrategy implements MaskingStrategy {
    @Override
    public String mask(String raw) {
        return "[hash:" + Integer.toHexString(raw.hashCode()) + "]";
    }
}

class Order {
    @Sensitive(strategy = HashTagStrategy.class)
    String coupon;
}
```

## 异常隔离

字段访问失败或策略抛异常不会中断整体流程：引擎记录 `MaskingError`
（含字段路径与原因）并回调 `MaskingErrorListener`（默认输出到
`java.util.logging`），然后按 `OnError` 配置兜底：

```java
MaskingConfig config = MaskingConfig.builder()
        .onError(OnError.PLACEHOLDER)   // 或 OnError.KEEP_ORIGINAL（默认）
        .placeholder("<masked>")
        .errorListener(error -> alertSink.send(error))
        .build();
```

- `KEEP_ORIGINAL`：出错字段保留原值；
- `PLACEHOLDER`：字符串字段替换为占位符，其他类型置 null。

## 日志框架集成

`LogMaskingHook` 是日志格式化钩子接口，`RegexLogMaskingHook` 为内置实现，
直接在已格式化的日志文本中识别并脱敏手机号、身份证、银行卡、邮箱。
不绑定任何日志框架，接入示例：

**Logback（自定义 Converter）**

```java
public class MaskingConverter extends ClassicConverter {
    private final LogMaskingHook hook = new RegexLogMaskingHook();

    @Override
    public String convert(ILoggingEvent event) {
        return hook.mask(event.getFormattedMessage());
    }
}
```

```xml
<conversionRule conversionWord="maskMsg"
                converterClass="com.example.MaskingConverter"/>
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
  <encoder>
    <pattern>%d %-5level %logger{36} - %maskMsg%n</pattern>
  </encoder>
</appender>
```

**Log4j2（自定义 PatternConverter）**

```java
@Plugin(name = "MaskingConverter", category = "Converter")
@ConverterKeys("maskMsg")
public class MaskingConverter extends LogEventPatternConverter {
    private final LogMaskingHook hook = new RegexLogMaskingHook();

    private MaskingConverter() { super("maskMsg", "maskMsg"); }

    public static MaskingConverter newInstance(String[] options) {
        return new MaskingConverter();
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        toAppendTo.append(hook.mask(event.getMessage().getFormattedMessage()));
    }
}
```

**SLF4J 参数化日志**：也可在业务侧先 `engine.mask(arg)` 再传入占位符。

## 工程结构

```
com.example.gsb.masking
├── MaskingEngine            脱敏引擎（副本 / Map / JSON 三种输出）
├── MaskingConfig            配置：分组、异常兜底、占位符、错误监听
├── Sensitive / Sensitives   字段注解（可重复，支持分组）
├── SensitiveIgnore          排除注解
├── MaskingStrategy          策略 SPI
├── MaskingError / MaskingErrorListener / OnError   异常隔离
├── strategy/                六种内置策略
├── json/JsonWriter          轻量 JSON 序列化器
└── log/                     LogMaskingHook 接口与 RegexLogMaskingHook 实现
```
