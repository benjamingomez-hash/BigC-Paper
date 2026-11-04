package dev.linqfy.bigCasares.duperestore;

import java.lang.reflect.Field;
import java.util.EnumMap;

final class PaperDupeFlags implements FlagLease.Access {
    private final Object settings;
    private final EnumMap<FlagLease.Key, Field> fields = new EnumMap<>(FlagLease.Key.class);

    PaperDupeFlags(ClassLoader serverClassLoader) throws ReflectiveOperationException {
        Class<?> configuration = Class.forName("io.papermc.paper.configuration.GlobalConfiguration", true, serverClassLoader);
        Object global = configuration.getMethod("get").invoke(null);
        settings = configuration.getField("unsupportedSettings").get(global);
        fields.put(FlagLease.Key.PISTONS, settings.getClass().getField("allowPistonDuplication"));
        fields.put(FlagLease.Key.TRIPWIRE_HOOKS, settings.getClass().getField("skipTripwireHookPlacementValidation"));
        for (Field field : fields.values()) {
            if (field.getType() != boolean.class) throw new NoSuchFieldException("Expected a boolean: " + field.getName());
            field.getBoolean(settings);
        }
    }

    public boolean get(FlagLease.Key key) {
        try { return fields.get(key).getBoolean(settings); }
        catch (IllegalAccessException error) { throw new IllegalStateException("No se pudo leer la opción de Paper " + key, error); }
    }

    public void set(FlagLease.Key key, boolean value) {
        try { fields.get(key).setBoolean(settings, value); }
        catch (IllegalAccessException error) { throw new IllegalStateException("No se pudo cambiar la opción de Paper " + key, error); }
    }
}
