package de.javagil.nuclos.extensions.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EventListener;

import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;

import de.javagil.nuclos.extensions.WrappedException;

public class Reflection {
	public static <T> T callMethod(Class<? extends Object> declaringClass, Object instance, String methodName, Class<?>[] argTypes, Object... args) {
		try {
			Method method = declaringClass.getDeclaredMethod(methodName, argTypes);
			method.setAccessible(true);
			return (T) method.invoke(instance, args);
		} catch ( Exception exc) {
			throw new WrappedException("HACK FAILED, Seems Nuclos internals have changed, we need to amend the hack.", exc);
		}
	}

	public static <T> T getFieldValue(Class<? extends Object> declaringClass, Object instance, String fieldName) {
		try {
			Field field = declaringClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(instance);
		} catch ( Exception exc) {
			throw new WrappedException("HACK FAILED, Seems Nuclos internals have changed, we need to amend the hack.", exc);
		}
	}

	public static <T> T getFieldValueOrNull(Object instance, String fieldName) {
		return getFieldValueOrNull(instance.getClass(), instance, fieldName);
	}

	public static <T> T getFieldValueOrNull(Class<? extends Object> declaringClass, Object instance, String fieldName) {
		try {
			Field field = declaringClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(instance);
		} catch (NoSuchFieldException exc) {
			if ( declaringClass.getSuperclass() != Object.class ) {
				return  getFieldValueOrNull(declaringClass.getSuperclass(), instance, fieldName);
			}
			return null;
		} catch ( Exception exc) {
			throw new WrappedException("HACK FAILED, Seems Nuclos internals have changed, we need to amend the hack.", exc);
		}
	}

	public static <T> T castIfPossible(Object source, Class<T> targetClass) {
		if ( source != null && targetClass.isAssignableFrom(source.getClass()) ) {
			return (T) source;
		}
		return null;
	}
}
