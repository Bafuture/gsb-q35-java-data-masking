package com.example.gsb.masking;

import com.example.gsb.masking.json.JsonWriter;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脱敏引擎。线程安全；策略实例按类型缓存复用。
 *
 * <p>两种输出形式：</p>
 * <ul>
 *   <li>{@link #mask(Object)}：深度复制对象图并脱敏，原对象不被修改，循环引用被保留；</li>
 *   <li>{@link #toMaskedMap(Object)} / {@link #toMaskedJson(Object)}：输出 Map/JSON 结构，
 *       循环引用处以 {@value #CIRCULAR_MARKER} 占位。</li>
 * </ul>
 */
public class MaskingEngine {

    /** Map/JSON 输出中循环引用的占位文本。 */
    public static final String CIRCULAR_MARKER = "[Circular Reference]";

    private final MaskingConfig config;
    private final Map<Class<? extends MaskingStrategy>, MaskingStrategy> strategyCache =
            new ConcurrentHashMap<>();

    public MaskingEngine() {
        this(MaskingConfig.defaults());
    }

    public MaskingEngine(MaskingConfig config) {
        this.config = config == null ? MaskingConfig.defaults() : config;
    }

    /** 返回脱敏后的副本；原对象及其对象图不会被修改。 */
    @SuppressWarnings("unchecked")
    public <T> T mask(T source) {
        if (source == null) {
            return null;
        }
        IdentityHashMap<Object, Object> copies = new IdentityHashMap<>();
        return (T) copyValue(source, copies, "", false);
    }

    /** 返回脱敏后的 Map/List/标量结构，便于序列化或日志输出。 */
    public Object toMaskedMap(Object source) {
        return mapValue(source, new IdentityHashMap<>(), "", false);
    }

    /** 返回脱敏后的 JSON 字符串（内置轻量序列化器，无第三方依赖）。 */
    public String toMaskedJson(Object source) {
        return JsonWriter.write(toMaskedMap(source));
    }

    // ------------------------------------------------------------------
    // 副本模式
    // ------------------------------------------------------------------

    private Object copyValue(Object value, IdentityHashMap<Object, Object> copies,
                             String path, boolean suppressMasking) {
        if (value == null) {
            return null;
        }
        Object existing = copies.get(value);
        if (existing != null) {
            return existing;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object copy = Array.newInstance(value.getClass().getComponentType(), length);
            copies.put(value, copy);
            for (int i = 0; i < length; i++) {
                Array.set(copy, i, copyValue(Array.get(value, i), copies, path + "[" + i + "]", suppressMasking));
            }
            return copy;
        }
        if (value instanceof Collection<?> collection) {
            Collection<Object> copy = newCollectionFor(collection);
            copies.put(value, copy);
            int index = 0;
            for (Object element : collection) {
                copy.add(copyValue(element, copies, path + "[" + index++ + "]", suppressMasking));
            }
            return copy;
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            copies.put(value, copy);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put(entry.getKey(),
                        copyValue(entry.getValue(), copies, path + "[" + entry.getKey() + "]", suppressMasking));
            }
            return copy;
        }
        if (isLeaf(value.getClass())) {
            return value;
        }
        return copyPojo(value, copies, path, suppressMasking);
    }

    private Object copyPojo(Object value, IdentityHashMap<Object, Object> copies,
                            String path, boolean suppressMasking) {
        Class<?> type = value.getClass();
        Object copy;
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            copy = constructor.newInstance();
        } catch (Exception e) {
            report(path, type, e);
            // 无法实例化时退回原引用（引擎只读原对象，不会修改它）。
            return value;
        }
        copies.put(value, copy);
        for (Field field : allFields(type)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String fieldPath = path.isEmpty() ? field.getName() : path + "." + field.getName();
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(value);
                Object newValue;
                if (field.isAnnotationPresent(SensitiveIgnore.class)) {
                    newValue = fieldValue;
                } else {
                    MaskingStrategy strategy = suppressMasking ? null : resolveStrategy(field, fieldPath);
                    if (strategy != null && fieldValue != null) {
                        newValue = applyStrategy(strategy, fieldValue, fieldPath);
                    } else {
                        newValue = copyValue(fieldValue, copies, fieldPath, suppressMasking);
                    }
                }
                field.set(copy, newValue);
            } catch (Exception e) {
                report(fieldPath, type, e);
                applyFieldFallback(copy, field, value);
            }
        }
        return copy;
    }

    // ------------------------------------------------------------------
    // Map 模式
    // ------------------------------------------------------------------

    private Object mapValue(Object value, IdentityHashMap<Object, Boolean> visiting,
                            String path, boolean suppressMasking) {
        if (value == null) {
            return null;
        }
        if (visiting.containsKey(value)) {
            return CIRCULAR_MARKER;
        }
        visiting.put(value, Boolean.TRUE);
        try {
            if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                List<Object> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(mapValue(Array.get(value, i), visiting, path + "[" + i + "]", suppressMasking));
                }
                return list;
            }
            if (value instanceof Collection<?> collection) {
                List<Object> list = new ArrayList<>(collection.size());
                int index = 0;
                for (Object element : collection) {
                    list.add(mapValue(element, visiting, path + "[" + index++ + "]", suppressMasking));
                }
                return list;
            }
            if (value instanceof Map<?, ?> map) {
                Map<Object, Object> result = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    result.put(entry.getKey(),
                            mapValue(entry.getValue(), visiting, path + "[" + entry.getKey() + "]", suppressMasking));
                }
                return result;
            }
            if (isLeaf(value.getClass())) {
                return value;
            }
            return pojoToMap(value, visiting, path, suppressMasking);
        } finally {
            visiting.remove(value);
        }
    }

    private Object pojoToMap(Object value, IdentityHashMap<Object, Boolean> visiting,
                             String path, boolean suppressMasking) {
        Class<?> type = value.getClass();
        Map<String, Object> result = new LinkedHashMap<>();
        for (Field field : allFields(type)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String fieldPath = path.isEmpty() ? field.getName() : path + "." + field.getName();
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(value);
                if (field.isAnnotationPresent(SensitiveIgnore.class)) {
                    result.put(field.getName(), mapValue(fieldValue, visiting, fieldPath, true));
                    continue;
                }
                MaskingStrategy strategy = suppressMasking ? null : resolveStrategy(field, fieldPath);
                if (strategy != null && fieldValue != null) {
                    result.put(field.getName(), applyStrategy(strategy, fieldValue, fieldPath));
                } else {
                    result.put(field.getName(), mapValue(fieldValue, visiting, fieldPath, suppressMasking));
                }
            } catch (Exception e) {
                report(fieldPath, type, e);
                result.put(field.getName(), config.getOnError() == OnError.PLACEHOLDER
                        ? config.getPlaceholder() : safeRawValue(field, value));
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // 策略解析与异常兜底
    // ------------------------------------------------------------------

    private MaskingStrategy resolveStrategy(Field field, String fieldPath) {
        for (Sensitive annotation : field.getAnnotationsByType(Sensitive.class)) {
            if (annotation.groups().length == 0 || groupsMatch(annotation.groups())) {
                try {
                    return strategyCache.computeIfAbsent(annotation.strategy(), this::instantiate);
                } catch (RuntimeException e) {
                    report(fieldPath, field.getDeclaringClass(), e);
                    return null;
                }
            }
        }
        return null;
    }

    private boolean groupsMatch(String[] groups) {
        for (String group : groups) {
            if (config.getActiveGroups().contains(group)) {
                return true;
            }
        }
        return false;
    }

    private MaskingStrategy instantiate(Class<? extends MaskingStrategy> type) {
        try {
            Constructor<? extends MaskingStrategy> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new MaskingException("cannot instantiate strategy " + type.getName(), e);
        }
    }

    private Object applyStrategy(MaskingStrategy strategy, Object fieldValue, String fieldPath) {
        try {
            return strategy.mask(String.valueOf(fieldValue));
        } catch (Exception e) {
            report(fieldPath, null, e);
            return config.getOnError() == OnError.PLACEHOLDER ? config.getPlaceholder() : fieldValue;
        }
    }

    private void applyFieldFallback(Object copy, Field field, Object source) {
        try {
            field.setAccessible(true);
            if (config.getOnError() == OnError.PLACEHOLDER) {
                field.set(copy, String.class.isAssignableFrom(field.getType())
                        ? config.getPlaceholder() : null);
            } else {
                field.set(copy, field.get(source));
            }
        } catch (Exception ignored) {
            // 兜底失败时保持副本字段默认值，不中断流程。
        }
    }

    private Object safeRawValue(Field field, Object source) {
        try {
            field.setAccessible(true);
            return field.get(source);
        } catch (Exception e) {
            return null;
        }
    }

    private void report(String path, Class<?> ownerType, Throwable cause) {
        try {
            config.getErrorListener().onError(new MaskingError(path, ownerType, cause));
        } catch (Exception ignored) {
            // 监听器自身异常不得影响主流程。
        }
    }

    // ------------------------------------------------------------------
    // 工具方法
    // ------------------------------------------------------------------

    private static boolean isLeaf(Class<?> type) {
        if (type.isPrimitive() || type.isEnum()) {
            return true;
        }
        String name = type.getName();
        return name.startsWith("java.") || name.startsWith("javax.");
    }

    private static Collection<Object> newCollectionFor(Collection<?> source) {
        if (source instanceof java.util.SortedSet || source instanceof java.util.NavigableSet) {
            return new java.util.TreeSet<>();
        }
        if (source instanceof java.util.Set) {
            return new LinkedHashSet<>();
        }
        return new ArrayList<>();
    }

    private static List<Field> allFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!field.isSynthetic()) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }
}
