package annotations.handlers;

import annotations.Entity;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;

public class EntityHandler {
    private static Reflections reflections;
    private static Set<Class<?>> entitiesSet = new HashSet<>();

    public static Set<Class<?>> inspectEntities() {
        entitiesSet = reflections.getTypesAnnotatedWith(Entity.class, true);
        return entitiesSet;
    }

    public static Set<Class<?>> getEntitiesSet() {
        return entitiesSet;
    }

    public static void setReflections(Reflections reflections) {
        EntityHandler.reflections = reflections;
    }
}
