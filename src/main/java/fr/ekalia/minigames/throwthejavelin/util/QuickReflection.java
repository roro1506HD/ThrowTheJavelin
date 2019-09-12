package fr.ekalia.minigames.throwthejavelin.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.server.v1_14_R1.Packet;

/**
 * @author roro1506_HD
 */
public class QuickReflection {

    /**
     * Internal method to set and cast a private field
     */
    protected void setFieldCastValue(Packet<?> packet, String fieldName, Object value) {
        try {
            Field field = packet.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(packet, field.getType().cast(value));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Internal method to set a private field
     */
    protected void setFieldValue(Object instance, String fieldName, Object value) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Internal method to get a private field
     */
    protected <T> T getFieldValue(Class<T> clazz, Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return clazz.cast(field.get(instance));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Internal method to get a private static field
     */
    protected <T> T getStaticFieldValue(Class<T> clazz, Class<?> owner, String fieldName) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            return clazz.cast(field.get(null));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Internal method to set a private final field
     */
    protected void setFinalFieldValue(Packet<?> packet, String fieldName, Object value) {
        try {
            Field field = packet.getClass().getDeclaredField(fieldName);
            Field modifiers = Field.class.getDeclaredField("modifiers");

            modifiers.setAccessible(true);
            field.setAccessible(true);

            modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(packet, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
