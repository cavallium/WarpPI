package org.warp.picalculator;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ClassUtils {

	public static Class<?> classLoader;

	public static Object invokeStaticMethod(String path, Var<?>... vars) {
		return invokeMethod(path, null, vars);
	}

	@SuppressWarnings("rawtypes")
	public static Object invokeMethod(String path, Object instance, Var<?>... vars) {
		Class[] classes = new Class[vars.length];
		Object[] objects = new Object[vars.length];
		String[] blob = path.split("\\.");
		String className = String.join(".", Arrays.copyOfRange(blob, 0, blob.length - 1));
		String methodName = blob[blob.length - 1];
		int index = 0;
		for (Var<?> v : vars) {
			classes[index] = v.getType();
			objects[index] = v.getVar();
			index++;
		}
		try {
			return classLoader.getClassLoader().loadClass(className).getMethod(methodName, classes).invoke(instance, objects);
		} catch (InvocationTargetException ex) {
			NullPointerException exc = new NullPointerException();
			exc.addSuppressed(ex);
			throw exc;
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			NullPointerException exc = new NullPointerException();
			exc.addSuppressed(e);
			throw exc;
		}
	}

	@SuppressWarnings("rawtypes")
	public static Object newClassInstance(String className, Var<?>... vars) {
		Class[] classes = new Class[vars.length];
		Object[] objects = new Object[vars.length];
		int index = 0;
		for (Var<?> v : vars) {
			classes[index] = v.getType();
			objects[index] = v.getVar();
			index++;
		}
		try {
			return classLoader.getClassLoader().loadClass(className).getDeclaredConstructor(classes).newInstance(objects);
		} catch (InvocationTargetException ex) {
			NullPointerException exc = new NullPointerException();
			exc.addSuppressed(ex);
			throw exc;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			NullPointerException exc = new NullPointerException();
			exc.addSuppressed(e);
			throw exc;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getStaticField(String path, Class<T> type) {
		try {
			String[] blob = path.split("\\.");
			String className = String.join(".", Arrays.copyOfRange(blob, 0, blob.length - 1));
			String var = blob[blob.length - 1];
			return (T) classLoader.getClassLoader().loadClass(className).getField(var).get(null);
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			NullPointerException exc = new NullPointerException();
			exc.addSuppressed(e);
			throw exc;
		}
	}

	public static <T> Var<T> getStaticFieldVar(String path, Class<T> type) {
		T obj = getStaticField(path, type);
		return new Var<T>(obj, type);
	}

	public static int getEnumIndex(String className, String value) {
		try {
			Object[] enumConstants = classLoader.getClassLoader().loadClass(className).getEnumConstants();
			int index = 0;
			for (Object o : enumConstants) {
				if (o.toString().equals(value)) {
					return index;
				}
				index++;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			NullPointerException exc = new NullPointerException();
			exc.addSuppressed(e);
			throw exc;
		}
		return -1;
	}

	public static Object getEnumObject(String className, String value) {
		try {
			Object[] enumConstants = classLoader.getClassLoader().loadClass(className).getEnumConstants();
			for (Object o : enumConstants) {
				if (o.toString().equals(value)) {
					return o;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			NullPointerException exc = new NullPointerException();
			exc.addSuppressed(e);
			throw exc;
		}
		return new Object();
	}

	public static class Var<E> {
		private final E var;
		private final Class<E> type;

		public Var(E var, Class<E> type) {
			this.var = var;
			this.type = type;
		}

		public synchronized final E getVar() {
			return var;
		}

		public synchronized final Class<E> getType() {
			return type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((var == null) ? 0 : var.hashCode());
			return result;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Var other = (Var) obj;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			if (var == null) {
				if (other.var != null)
					return false;
			} else if (!var.equals(other.var))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Var [var=" + var + ", type=" + type + "]";
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static Var<Integer> newVar(int i) {
			return new Var(i, int.class);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static Var<Short> newVar(short i) {
			return new Var(i, short.class);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static Var<Byte> newVar(byte i) {
			return new Var(i, byte.class);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static Var<Boolean> newVar(boolean i) {
			return new Var(i, boolean.class);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static Var<Character> newVar(char i) {
			return new Var(i, char.class);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static Var<Long> newVar(long i) {
			return new Var(i, long.class);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static Var<Float> newVar(float i) {
			return new Var(i, float.class);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static Var<Double> newVar(double i) {
			return new Var(i, double.class);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static Var<String> newVar(String i) {
			return new Var(i, String.class);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static <E> Var<E> newVar(E[] i, Class<E> type) {
			return new Var(i, getArrayClass(type));
		}

		@SuppressWarnings("unchecked")
		private static <T> Class<? extends T[]> getArrayClass(Class<T> clazz) {
			return (Class<? extends T[]>) Array.newInstance(clazz, 0).getClass();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static Var<?> newVarFromClass(Object classObj, String className) {
			try {
				return new Var(classObj, classLoader.getClassLoader().loadClass(className));
			} catch (ClassNotFoundException | IllegalArgumentException e) {
				e.printStackTrace();
				NullPointerException exc = new NullPointerException();
				exc.addSuppressed(e);
				throw exc;
			}
		}
	}
}
