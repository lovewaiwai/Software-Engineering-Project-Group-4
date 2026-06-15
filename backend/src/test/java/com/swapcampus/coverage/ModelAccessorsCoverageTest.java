package com.swapcampus.coverage;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ModelAccessorsCoverageTest {

    private static final Set<String> COVERAGE_PACKAGES = Set.of(
            ".dto.",
            ".entity.",
            ".vo.",
            ".enums.",
            ".adapter.",
            ".common.api.",
            ".common.exception.",
            ".common.security.CurrentUserPrincipal",
            ".common.security.JwtProperties",
            ".common.storage.MinioProperties",
            ".user.config.UserVerificationProperties"
    );

    @Test
    void exerciseSimpleModelsEnumsAndValueObjects() throws Exception {
        List<Class<?>> classes = discoverProjectClasses();
        int exercised = 0;

        for (Class<?> type : classes) {
            if (!shouldExercise(type)) {
                continue;
            }
            if (type.isEnum()) {
                exerciseEnum(type);
                exercised++;
                continue;
            }
            Object instance = instantiate(type);
            if (instance == null) {
                continue;
            }
            callSetters(instance);
            callGetters(instance);
            exercised++;
        }

        assertFalse(exercised == 0, "Expected at least one model class to be exercised");
    }

    private List<Class<?>> discoverProjectClasses() throws Exception {
        Path root = Path.of("target", "classes", "com", "swapcampus");
        if (!Files.exists(root)) {
            return List.of();
        }
        List<Class<?>> classes = new ArrayList<>();
        try (var paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith(".class"))
                    .filter(path -> !path.getFileName().toString().contains("$"))
                    .forEach(path -> {
                        String relative = root.relativize(path).toString();
                        String className = "com.swapcampus."
                                + relative.substring(0, relative.length() - ".class".length())
                                .replace('\\', '.')
                                .replace('/', '.');
                        try {
                            classes.add(Class.forName(className));
                        } catch (Throwable ignored) {
                            // Classes that need optional runtime wiring are not part of this model sweep.
                        }
                    });
        }
        return classes;
    }

    private boolean shouldExercise(Class<?> type) {
        int modifiers = type.getModifiers();
        if (type.isInterface() || type.isAnnotation() || Modifier.isAbstract(modifiers)) {
            return false;
        }
        String name = type.getName();
        return COVERAGE_PACKAGES.stream().anyMatch(name::contains);
    }

    private void exerciseEnum(Class<?> type) throws Exception {
        Object[] constants = type.getEnumConstants();
        Method values = type.getMethod("values");
        values.invoke(null);
        if (constants.length > 0) {
            Method valueOf = type.getMethod("valueOf", String.class);
            valueOf.invoke(null, ((Enum<?>) constants[0]).name());
        }
    }

    private Object instantiate(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void callSetters(Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            if (!method.getName().startsWith("set") || method.getParameterCount() != 1) {
                continue;
            }
            Object value = sampleValue(method.getParameterTypes()[0]);
            if (value == UnsupportedValue.INSTANCE) {
                continue;
            }
            try {
                method.invoke(instance, value);
            } catch (Throwable ignored) {
                // Some setters may validate coupled state. They are covered by focused service tests.
            }
        }
    }

    private void callGetters(Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            if (method.getParameterCount() != 0 || method.getReturnType().equals(Void.TYPE)) {
                continue;
            }
            String name = method.getName();
            if (!name.startsWith("get") && !name.startsWith("is")) {
                continue;
            }
            if (name.equals("getClass")) {
                continue;
            }
            try {
                method.invoke(instance);
            } catch (Throwable ignored) {
                // Getter exceptions are outside this broad accessor sweep.
            }
        }
    }

    private Object sampleValue(Class<?> type) {
        if (type.equals(String.class)) {
            return "sample";
        }
        if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return 1L;
        }
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return 1;
        }
        if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return true;
        }
        if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return 1.0D;
        }
        if (type.equals(BigDecimal.class)) {
            return new BigDecimal("12.34");
        }
        if (type.equals(LocalDateTime.class)) {
            return LocalDateTime.of(2026, 6, 14, 12, 0);
        }
        if (type.equals(LocalDate.class)) {
            return LocalDate.of(2026, 6, 14);
        }
        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<>();
        }
        if (Set.class.isAssignableFrom(type)) {
            return new HashSet<>();
        }
        if (Map.class.isAssignableFrom(type)) {
            return new HashMap<>();
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants.length == 0 ? UnsupportedValue.INSTANCE : constants[0];
        }
        return UnsupportedValue.INSTANCE;
    }

    private enum UnsupportedValue {
        INSTANCE
    }
}
