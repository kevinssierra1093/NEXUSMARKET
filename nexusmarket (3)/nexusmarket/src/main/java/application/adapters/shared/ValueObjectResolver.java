package application.adapters.shared;

import application.domain.exceptions.DomainException;
import application.domain.valueobjects.DomainCatalog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converts catalog codes (e.g. "PAID") back to the single constant instance of the
 * corresponding Value Object (e.g. OrderStatus.PAID), and Value Objects to their codes.
 * Used by the adapters only: the domain never deals with codes received from outside.
 */
public final class ValueObjectResolver {

    private static final Map<Class<?>, Map<String, DomainCatalog>> CACHE = new ConcurrentHashMap<>();

    private ValueObjectResolver() {
    }

    public static <T extends DomainCatalog> T fromCode(Class<T> type, String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        DomainCatalog value = CACHE.computeIfAbsent(type, ValueObjectResolver::loadConstants)
                .get(code.trim().toUpperCase());
        if (value == null) {
            throw new DomainException("Unknown " + type.getSimpleName() + " code: " + code + ".");
        }
        return type.cast(value);
    }

    public static String code(DomainCatalog value) {
        return value == null ? null : value.getCode();
    }

    private static Map<String, DomainCatalog> loadConstants(Class<?> type) {
        Map<String, DomainCatalog> constants = new ConcurrentHashMap<>();
        for (Field field : type.getFields()) {
            int modifiers = field.getModifiers();
            boolean constant = Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && type.isAssignableFrom(field.getType());
            if (!constant) {
                continue;
            }
            try {
                DomainCatalog value = (DomainCatalog) field.get(null);
                constants.put(value.getCode(), value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot read catalog constant " + field.getName(), e);
            }
        }
        return constants;
    }
}
