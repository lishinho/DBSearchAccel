package io.github.dbsearchaccel.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 主键解析器实现类.
 * <p>
 * 支持从多种参数类型中解析主键值：
 * - 单个实体对象
 * - 集合类型
 * - Map类型
 * - 基本类型（直接作为主键）
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaPkResolverImpl implements DsaPkResolver {

    private static final Logger log = LoggerFactory.getLogger(DsaPkResolverImpl.class);

    @Override
    public List<String> resolve(Object param, String pkField) {
        List<String> result = new ArrayList<>();

        if (param == null) {
            return result;
        }

        if (param instanceof Collection) {
            Collection<?> collection = (Collection<?>) param;
            for (Object item : collection) {
                List<String> pks = resolveSingle(item, pkField);
                result.addAll(pks);
            }
        } else if (param.getClass().isArray()) {
            Object[] array = (Object[]) param;
            for (Object item : array) {
                List<String> pks = resolveSingle(item, pkField);
                result.addAll(pks);
            }
        } else {
            List<String> pks = resolveSingle(param, pkField);
            result.addAll(pks);
        }

        return result;
    }

    @Override
    public List<String> resolve(Object[] params, String pkField, int paramIndex) {
        List<String> result = new ArrayList<>();

        if (params == null || params.length == 0) {
            return result;
        }

        if (paramIndex < 0 || paramIndex >= params.length) {
            log.warn("Invalid param index: {}, params length: {}", paramIndex, params.length);
            return result;
        }

        Object param = params[paramIndex];
        return resolve(param, pkField);
    }

    /**
     * 从单个对象中解析主键.
     *
     * @param obj     对象
     * @param pkField 主键字段名
     * @return 主键值列表
     */
    private List<String> resolveSingle(Object obj, String pkField) {
        List<String> result = new ArrayList<>();

        if (obj == null) {
            return result;
        }

        if (isPrimitiveOrWrapper(obj.getClass()) || obj instanceof String) {
            result.add(String.valueOf(obj));
            return result;
        }

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Object pkValue = map.get(pkField);
            if (pkValue != null) {
                result.add(String.valueOf(pkValue));
            }
            return result;
        }

        try {
            Field field = findField(obj.getClass(), pkField);
            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    result.add(String.valueOf(value));
                }
            } else {
                log.warn("Field [{}] not found in class [{}]", pkField, obj.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Failed to resolve pk field [{}] from object", pkField, e);
        }

        return result;
    }

    /**
     * 查找字段（支持继承）.
     *
     * @param clazz     类
     * @param fieldName 字段名
     * @return 字段对象
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 判断是否为基本类型或包装类型.
     *
     * @param clazz 类
     * @return true表示是基本类型或包装类型
     */
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Double.class
                || clazz == Float.class
                || clazz == Boolean.class
                || clazz == Character.class
                || clazz == Byte.class
                || clazz == Short.class;
    }
}
