import org.junit.Assert;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TestcaseHelper {

	// JUnit helpers:

	public static void assertArrayEquals(boolean[] expecteds, boolean[] actuals, String message) {
		if (expecteds == null && actuals == null) {
			return;
		}
		if (expecteds == null && actuals != null) {
			fail(message + "\nExpected null array but actual array is non-null.");
		}
		if (actuals == null) {
			fail(message + "\nExpected non-null array but actual array is null.");
		}
		if (expecteds.length != actuals.length) {
			fail(message + "\nExpected and actual arrays have different lengths:\nExpected: " + expecteds.length
					+ "\nActual: " + actuals.length);
		}
		for (int i = 0; i < expecteds.length; i++) {
			if (expecteds[i] != actuals[i]) {
				fail(message + "Arrays differ at index " + i + ":\nExpected: " + expecteds[i] + "\nActual: "
						+ actuals[i]);
			}
		}
	}

	public static void assertArrayEquals(char[] expecteds, char[] actuals, String message) {
		Assert.assertArrayEquals(message, expecteds, actuals);
	}

	public static void assertArrayEquals(byte[] expecteds, byte[] actuals, String message) {
		Assert.assertArrayEquals(message, expecteds, actuals);
	}

	public static void assertArrayEquals(short[] expecteds, short[] actuals, String message) {
		Assert.assertArrayEquals(message, expecteds, actuals);
	}

	public static void assertArrayEquals(int[] expecteds, int[] actuals, String message) {
		Assert.assertArrayEquals(message, expecteds, actuals);
	}

	public static void assertArrayEquals(double[] expecteds, double[] actuals, String message) {
		Assert.assertArrayEquals(message, expecteds, actuals, 10e-8);
	}

	public static void assertArrayEquals(long[] expecteds, long[] actuals, String message) {
		Assert.assertArrayEquals(message, expecteds, actuals);
	}

	public static void assertEquals(double expected, double actual, double epsilon, String message) {
		try {
			Assert.assertEquals(expected, actual, epsilon);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertEquals(double expected, double actual, String message) {
		assertEquals(expected, actual, 10e-8, message);
	}

	public static void assertEquals(float expected, float actual, float epsilon, String message) {
		try {
			Assert.assertEquals(expected, actual, epsilon);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertEquals(boolean expected, boolean actual, String message) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertEquals(char expected, char actual, String message) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertEquals(byte expected, byte actual, String message) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertEquals(short expected, short actual, String message) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertEquals(int expected, int actual, String message) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertEquals(long expected, long actual, String message) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertEquals(Object expected, Object actual, String message) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertNotEquals(double notExpected, double actual, String message) {
		try {
			Assert.assertNotEquals(notExpected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertNotEquals(float notExpected, float actual, String message) {
		try {
			Assert.assertNotEquals(notExpected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertNotEquals(boolean notExpected, boolean actual, String message) {
		try {
			Assert.assertNotEquals(notExpected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertNotEquals(char notExpected, char actual, String message) {
		try {
			Assert.assertNotEquals(notExpected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertNotEquals(byte notExpected, byte actual, String message) {
		try {
			Assert.assertNotEquals(notExpected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertNotEquals(short notExpected, short actual, String message) {
		try {
			Assert.assertNotEquals(notExpected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertNotEquals(int notExpected, int actual, String message) {
		try {
			Assert.assertNotEquals(notExpected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertNotEquals(long notExpected, long actual, String message) {
		try {
			Assert.assertNotEquals(notExpected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertNotEquals(Object notExpected, Object actual, String message) {
		try {
			Assert.assertNotEquals(notExpected, actual);
		} catch (AssertionError e) {
			Assert.fail(message + "\n" + e.getMessage());
		}
	}

	public static void assertFalse(boolean condition, String message) {
		Assert.assertFalse(message, condition);
	}

	public static void assertTrue(boolean condition, String message) {
		Assert.assertTrue(message, condition);
	}

	public static void assertNotNull(Object object, String message) {
		Assert.assertNotNull(message, object);
	}

	public static void assertNull(Object object, String message) {
		Assert.assertNull(message, object);
	}

	public static void assertMethodExists(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			clazz.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			String message = "Could not find the method \"" + name + "\"";
			throw new AssertionError(message);
		}
	}

	public static void assertAllAttributesArePrivate(Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if ((field.getModifiers() & Modifier.PRIVATE) == 0) {
				fail("Attribute \"" + field.getName() + "\" is not private!");
			}
		}
	}

	public static void assertAttributeExists(Class<?> clazz, String name) {
		try {
			clazz.getDeclaredField(name);
		} catch (NoSuchFieldException | SecurityException e) {
			fail("Can not find attribute \"" + name + "\"");
		}
	}

	public static void assertAllMethodsNotContainedInInterfaceShouldBePrivate(Class<?> clazz, Class<?> interfaze) {
		assertAllMethodsNotContainedInInterfaceShouldBePrivateWithException(clazz, interfaze, new String[] {});
	}

	public static void assertAllMethodsNotContainedInInterfaceShouldBePrivateWithException(Class<?> clazz,
			Class<?> interfaze, String... exceptions) {
		Method[] interfaceMethods = interfaze.getDeclaredMethods();
		Method[] classMethods = clazz.getDeclaredMethods();

		List<String> exceptionList = new ArrayList<String>(Arrays.asList(exceptions));
		List<Method> interfaceMethodList = Arrays.asList(interfaceMethods);
		List<Method> classMethodList = Arrays.asList(classMethods);

		mainLoop: for (Method method : classMethodList) {
			if (exceptionList.contains(method.toString()) || method.getName().startsWith("access$")
					|| method.getName().equals("main")) {
				continue;
			}

			for (Method imethod : interfaceMethodList) {
				if (equalMethods(method, imethod)) {
					continue mainLoop;
				}
			}

			if ((method.getModifiers() & Modifier.PRIVATE) == 0 && (method.getModifiers() & Modifier.STATIC) == 0) {
				fail("Method \"" + method.getName() + "\" should be private");
			}
		}
	}

	public static boolean equalMethods(Method m1, Method m2) {
		if (!m1.getName().equals(m2.getName())) {
			return false;
		}

		return Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes());
	}

	public static void fail(String message) {
		Assert.fail(message);
	}

	// Reflection helpers:

	public static void checkImplementsInterface(Class<?> clazz, Class<?> iface) {
		Class<?>[] interfaces = clazz.getInterfaces();
		boolean found = false;

		for (Class<?> i : interfaces) {
			if (i.equals(iface)) {
				found = true;
				break;
			}
		}

		assertTrue(found, "Class '" + clazz.getName() + "' does not implement interface '" + iface.getName() + "'.");
	}

	public static void checkSuperclass(Class<?> clazz, Class<?> expectedSuper) {
		if (expectedSuper == null) {
			expectedSuper = Object.class;
		}
		ArrayList<String> errors = new ArrayList<String>();
		if (!clazz.getSuperclass().equals(expectedSuper)) {
			errors.add(
					"Class '" + clazz.getName() + "' is not a direct subclass of '" + expectedSuper.getName() + "'!");
		}

		if (errors.size() > 0) {
			String error = "";
			for (String e : errors) {
				error = error + e + "\n";
			}
			fail(error);
		}
	}

	public static Method getMethod(Class<?> c, String methodName, Class<?>... args) {
		try {
			Method m = c.getDeclaredMethod(methodName, args);
			m.setAccessible(true);
			return m;
		} catch (SecurityException e) {
			System.err.println("Why the hell is there a SecurityManager?");

		} catch (NoSuchMethodException e) {
			fail("Method '" + methodName + "' with that signature wasn't found.");
		}
		System.exit(-1);
		return null;
	}

	public static Method getMethodOfSuperclass(Class<?> c, String methodName, Class<?>... args) {
		return getMethod(c.getSuperclass(), methodName, args);
	}

	@SuppressWarnings("rawtypes")
	public static Constructor getConstructor(Class<?> c, Class<?>... args) {
		try {
			Constructor m = c.getDeclaredConstructor(args);
			m.setAccessible(true);
			return m;
		} catch (NoSuchMethodException e) {
			fail("Constructor with that signature wasn't found.");
		}
		System.exit(-1);
		return null;
	}

	public static Object invoke(Method m, Object o, Object... args) {
		try {
			m.setAccessible(true);
			return m.invoke(o, args);
		} catch (IllegalAccessException e) {
			fail("Couldn't invoke method (1) " + m.getName());
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
			System.err.println("Method threw a non-RuntimeException.");
			System.exit(-1);
		}
		return null;
	}

	public static Object invoke(Constructor m, Object... args) throws InstantiationException, IllegalArgumentException {
		try {
			m.setAccessible(true);
			return m.newInstance(args);
		} catch (IllegalAccessException e) {
			fail("Couldn't invoke method (1) " + m.getName());
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
			System.err.println("Constructor threw a non-RuntimeException.");
			System.exit(-1);
		}
		return null;
	}

	@SuppressWarnings({ "unchecked" })
	public static <T> T getFieldValue(Class<?> c, Object instance, String fieldName) {
		try {
			Field f = c.getDeclaredField(fieldName);
			f.setAccessible(true);
			return (T) f.get(instance);
		} catch (SecurityException e) {
			fail("Why the hell is there a SecurityManager?");
		} catch (NoSuchFieldException e) {
			fail("There's no field '" + fieldName + "'");
		} catch (IllegalAccessException e) {
			fail("Why the hell is there a SecurityManager?");
		}
		return null;
	}

	public static void setFieldValue(Class<?> c, Object instance, String fieldName, Object value) {
		try {
			Field f = c.getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(instance, value);
		} catch (SecurityException e) {
			fail("Why the hell is there a SecurityManager?");
		} catch (NoSuchFieldException e) {
			fail("There's no field '" + fieldName + "'");
		} catch (IllegalAccessException e) {
			fail("Why the hell is there a SecurityManager?");
		}
	}

	// Access private fields
	@SuppressWarnings("unchecked")
	public static <E, O> E getValueOfPrivateField(O obj, String name) {

		Field privateField;
		try {
			privateField = obj.getClass().getDeclaredField(name);
			privateField.setAccessible(true);
			return (E) privateField.get(obj);
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <E, O> E getValueOfPrivateSuperclassField(O obj, String name)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		Field privateField = obj.getClass().getSuperclass().getDeclaredField(name);
		privateField.setAccessible(true);
		return (E) privateField.get(obj);
	}

	public static <E> E getValueOfPrivateStaticField(Class<?> clazz, String name) throws Exception {
		Field privateField = clazz.getDeclaredField(name);
		privateField.setAccessible(true);
		return (E) privateField.get(null);
	}

	public static <E> void setValueOfPrivateStaticField(Class<?> clazz, String name, E value) throws Exception {
		Field privateField = clazz.getDeclaredField(name);
		privateField.setAccessible(true);
		privateField.set(null, value);
	}

	public static <O, E> void setValueOfPrivateField(O obj, String name, E value) throws Exception {

		Field privateField = obj.getClass().getDeclaredField(name);
		privateField.setAccessible(true);

		privateField.set(obj, value);
	}

	public static <O, E> void setValueOfPrivateSuperclassField(O obj, String name, E value) throws Exception {

		Field privateField = obj.getClass().getSuperclass().getDeclaredField(name);
		privateField.setAccessible(true);

		privateField.set(obj, value);
	}

	// Testing attributes, methods, constructors:

	public static String signatureString(Method m) {
		String ret = m.getName() + "(";
		Class<?>[] param = m.getParameterTypes();

		if (param.length > 0) {
			if (param[0].isArray()) {
				ret = ret + param[0].getComponentType().getCanonicalName() + "[]";
			} else {
				ret = ret + param[0].getCanonicalName();
			}

			for (int i = 1; i < param.length; i++) {
				if (param[i].isArray()) {
					ret = ret + ", " + param[i].getComponentType().getCanonicalName() + "[]";
				} else {
					ret = ret + ", " + param[i].getCanonicalName();
				}
			}
		}

		return ret + ")";
	}

	public static String signatureString(Constructor<?> c) {
		String ret = c.getName() + "(";
		Class<?>[] param = c.getParameterTypes();

		if (param.length > 0) {
			if (param[0].isArray()) {
				ret = ret + param[0].getComponentType().getCanonicalName() + "[]";
			} else {
				ret = ret + param[0].getCanonicalName();
			}

			for (int i = 1; i < param.length; i++) {
				if (param[i].isArray()) {
					ret = ret + ", " + param[i].getComponentType().getCanonicalName() + "[]";
				} else {
					ret = ret + ", " + param[i].getCanonicalName();
				}
			}
		}

		return ret + ")";
	}

	public static void extractAttributes(Class<?> c) {
		Field[] fields = c.getDeclaredFields();
		System.out.println("@Test\npublic void testAttributes() {");
		System.out.println("\tString[] names = {");
		for (Field f : fields) {
			System.out.println("\t\t\"" + f.getName() + "\",");
		}
		System.out.println("\t};");
		System.out.println("\tClass<?>[] types = {");
		for (Field f : fields) {
			System.out.println("\t\t" + f.getType().getCanonicalName() + ".class,");
		}
		System.out.println("\t};");
		System.out.println("\tint[] modifiers = {");
		for (Field f : fields) {
			System.out.println("\t\t" + f.getModifiers() + ", // " + Modifier.toString(f.getModifiers()));
		}
		System.out.println("\t};");
		System.out.println("\tcheckAttributes(" + c.getName() + ".class, names, types, modifiers);");
		System.out.println("}");
	}

	public static void extractMethods(Class<?> c) {
		Method[] methods = c.getDeclaredMethods();
		System.out.println("@Test\npublic void testMethods() {");
		System.out.println("\tString[] names = {");
		for (Method f : methods) {
			System.out.println("\t\t\"" + signatureString(f) + "\",");
		}
		System.out.println("\t};");
		System.out.println("\tClass<?>[] ret = {");
		for (Method f : methods) {
			System.out.println("\t\t" + f.getReturnType().getCanonicalName() + ".class,");
		}
		System.out.println("\t};");
		System.out.println("\tint[] modifiers = {");
		for (Method f : methods) {
			System.out.println("\t\t" + f.getModifiers() + ", // " + Modifier.toString(f.getModifiers()));
		}
		System.out.println("\t};");
		System.out.println("\tcheckMethods(" + c.getName() + ".class, names, ret, modifiers);");
		System.out.println("}");
	}

	public static void extractConstructors(Class<?> c) {
		Constructor<?>[] constructors = c.getDeclaredConstructors();
		System.out.println("@Test\npublic void testConstructors() {");
		System.out.println("\tString[] names = {");
		for (Constructor<?> f : constructors) {
			System.out.println("\t\t\"" + signatureString(f) + "\",");
		}
		System.out.println("\t};");
		System.out.println("\tint[] modifiers = {");
		for (Constructor<?> f : constructors) {
			System.out.println("\t\t" + f.getModifiers() + ", // " + Modifier.toString(f.getModifiers()));
		}
		System.out.println("\t};");
		System.out.println("\tcheckConstructors(" + c.getName() + ".class, names, modifiers);");
		System.out.println("}");
	}

	public static void createStructureTests(Class<?> c) {
		extractAttributes(c);
		extractMethods(c);
		extractConstructors(c);
	}

	public static void createMethodObjects(Class<?> c) {
		Method[] methods = c.getDeclaredMethods();

		for (Method m : methods) {
			System.out.print("private Method m_" + m.getName() + "; // ");
			if (Modifier.isStatic(m.getModifiers())) {
				System.out.println("static");
			} else {
				System.out.println("non-static");
			}
		}

		System.out.println("@Before\npublic void setup() {");
		for (Method m : methods) {
			System.out.print("\tm_" + m.getName() + " = getMethod(" + c.getName() + ".class, \"" + m.getName() + "\"");

			Class<?>[] types = m.getParameterTypes();

			for (Class<?> cl : types) {
				System.out.print(", " + cl.getCanonicalName() + ".class");
			}

			System.out.println(");");
		}
		System.out.println("}");
	}

	public static void checkClassSignature(Class<?> clazz, int modifiers) {
		ArrayList<String> errors = new ArrayList<String>();

		if (clazz.getModifiers() != (modifiers & 4095)) {
			errors.add("Class '" + clazz.getName() + "' should have modifiers '" + Modifier.toString(modifiers)
					+ "' but has modifiers '" + Modifier.toString(clazz.getModifiers()) + "'.");
		}

		if (errors.size() > 0) {
			String error = "";
			for (String e : errors) {
				error = error + e + "\n";
			}
			fail(error);
		}
	}
	
	public static void checkAttributeExists(Class<?> clazz, String name) {
		Field[] fields = clazz.getDeclaredFields();

		HashMap<String, Field> map = new HashMap<String, Field>();
		for (Field f : fields) {
			map.put(f.getName(), f);
		}

		if (!map.containsKey(name)) {
			fail("Class '" + clazz.getName() + "' does not have field '" + name + "'.");
		}
	}
	
	public static void checkAttributesExist(Class<?> clazz, String[] names) {
		ArrayList<String> errors = new ArrayList<String>();
		Field[] fields = clazz.getDeclaredFields();

		HashMap<String, Field> map = new HashMap<String, Field>();
		for (Field f : fields) {
			map.put(f.getName(), f);
		}

		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i])) {
				fail("Class '" + clazz.getName() + "' does not have field '" + names[i] + "'.");
			}
		}

		if (errors.size() > 0) {
			String error = String.join("\n", errors);
			fail(error);
		}
	}
	
	public static void checkAttributeType(Class<?> clazz, String name, Class<?> type) {
		try {
			Field f = clazz.getDeclaredField(name);
			if (!f.getType().equals(type)) {
				fail("Field '" + name + "' of class '" + clazz.getName() + "' should have type '"
					+ type.getName() + "' but has type '" + f.getType().getName() + "'.");
			}
		} catch (NoSuchFieldException e) {
//			fail(e.getMessage());
		} catch (SecurityException e) {
			fail(e.getMessage());
		}
	}
	
	public static void checkAttributesTypes(Class<?> clazz, String[] names, Class<?>[] types) {
		ArrayList<String> errors = new ArrayList<String>();
		Field[] fields = clazz.getDeclaredFields();

		HashMap<String, Field> map = new HashMap<String, Field>();
		for (Field f : fields) {
			map.put(f.getName(), f);
		}

		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i])) {
				fail("Class '" + clazz.getName() + "' does not have field '" + names[i] + "'.");
				continue;
			}
			
			Field f = map.get(names[i]);
			if (!f.getType().equals(types[i])) {
				fail("Field '" + names[i] + "' of class '" + clazz.getName() + "' should have type '"
					+ types[i].getName() + "' but has type '" + f.getType().getName() + "'.");
			}
		}

		if (errors.size() > 0) {
			String error = String.join("\n", errors);
			fail(error);
		}
	}

	public static void checkAttributesTypes(Class<?> clazz, String[] names, String[] types) {
		ArrayList<String> errors = new ArrayList<String>();
		Field[] fields = clazz.getDeclaredFields();

		HashMap<String, Field> map = new HashMap<String, Field>();
		for (Field f : fields) {
			map.put(f.getName(), f);
		}

		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i])) {
				fail("Class '" + clazz.getName() + "' does not have field '" + names[i] + "'.");
				continue;
			}
			
			Field f = map.get(names[i]);
			if (!f.getType().getName().equals(types[i])) {
				fail("Field '" + names[i] + "' of class '" + clazz.getName() + "' should have type '"
					+ types[i] + "' but has type '" + f.getType().getName() + "'.");
			}
		}

		if (errors.size() > 0) {
			String error = String.join("\n", errors);
			fail(error);
		}
	}
	
	public static void checkAttributeModifiers(Class<?> clazz, String name, int modifiers) {
		try {
			Field f = clazz.getDeclaredField(name);
			if (f.getModifiers() != modifiers) {
				fail("Field '" + name + "' of class '" + clazz.getName() + "' should have modifiers '"
					+ Modifier.toString(modifiers) + "' but has modifiers '"
					+ Modifier.toString(f.getModifiers()) + "'.");
			}
		} catch (NoSuchFieldException e) {
//			fail(e.getMessage());
		} catch (SecurityException e) {
			fail(e.getMessage());
		}
	}
	
	public static void checkAttributesModifiers(Class<?> clazz, String[] names, int[] modifiers) {
		ArrayList<String> errors = new ArrayList<String>();
		Field[] fields = clazz.getDeclaredFields();

		HashMap<String, Field> map = new HashMap<String, Field>();
		for (Field f : fields) {
			map.put(f.getName(), f);
		}

		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i])) {
				fail("Class '" + clazz.getName() + "' does not have field '" + names[i] + "'.");
				continue;
			}
			
			Field f = map.get(names[i]);
			
			if (f.getModifiers() != modifiers[i]) {
				errors.add("Field '" + names[i] + "' of class '" + clazz.getName() + "' should have modifiers '"
						+ Modifier.toString(modifiers[i]) + "' but has modifiers '"
						+ Modifier.toString(f.getModifiers()) + "'.");
			}
		}

		if (errors.size() > 0) {
			String error = String.join("\n", errors);
			fail(error);
		}
	}

	public static void checkAttributes(Class<?> clazz, String[] names, Class<?>[] types, int[] modifiers) {
		ArrayList<String> errors = new ArrayList<String>();
		Field[] fields = clazz.getDeclaredFields();

		HashMap<String, Field> map = new HashMap<String, Field>();
		for (Field f : fields) {
			map.put(f.getName(), f);
		}

		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i])) {
				errors.add("Class '" + clazz.getName() + "' does not have field '" + names[i] + "'.");
				continue;
			}

			Field f = map.get(names[i]);

			if (!f.getType().equals(types[i])) {
				errors.add("Field '" + names[i] + "' of class '" + clazz.getName() + "' should have type '"
						+ types[i].getName() + "' but has type '" + f.getType().getName() + "'.");
			}

			if (f.getModifiers() != modifiers[i]) {
				errors.add("Field '" + names[i] + "' of class '" + clazz.getName() + "' should have modifiers '"
						+ Modifier.toString(modifiers[i]) + "' but has modifiers '"
						+ Modifier.toString(f.getModifiers()) + "'.");
			}
		}

		if (errors.size() > 0) {
			String error = "";
			for (String e : errors) {
				error = error + e + "\n";
			}
			fail(error);
		}
	}

	public static void checkAttributes(Class<?> clazz, String[] names, String[] types, int[] modifiers) {
		ArrayList<String> errors = new ArrayList<String>();
		Field[] fields = clazz.getDeclaredFields();

		HashMap<String, Field> map = new HashMap<String, Field>();
		for (Field f : fields) {
			map.put(f.getName(), f);
		}

		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i])) {
				errors.add("Class '" + clazz.getName() + "' does not have field '" + names[i] + "'.");
				continue;
			}

			Field f = map.get(names[i]);

			if (!f.getType().getName().equals(types[i])) {
				errors.add("Field '" + names[i] + "' of class '" + clazz.getName() + "' should have type '" + types[i]
						+ "' but has type '" + f.getType().getName() + "'.");
			}

			if (f.getModifiers() != modifiers[i]) {
				errors.add("Field '" + names[i] + "' of class '" + clazz.getName() + "' should have modifiers '"
						+ Modifier.toString(modifiers[i]) + "' but has modifiers '"
						+ Modifier.toString(f.getModifiers()) + "'.");
			}
		}

		if (errors.size() > 0) {
			String error = "";
			for (String e : errors) {
				error = error + e + "\n";
			}
			fail(error);
		}
	}

	public static void checkMethods(Class<?> clazz, String[] names, Class<?>[] ret, int[] modifiers) {
		ArrayList<String> errors = new ArrayList<String>();
		Method[] methods = clazz.getDeclaredMethods();

		HashMap<String, Method> map = new HashMap<String, Method>();
		for (Method m : methods) {
			map.put(signatureString(m), m);
		}

		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i])) {
				errors.add("Class '" + clazz.getName() + "' does not have method '" + names[i] + "'.");
				continue;
			}

			Method m = map.get(names[i]);

			if (!m.getReturnType().equals(ret[i])) {
				errors.add("Method '" + names[i] + "' of class '" + clazz.getName() + "' should have return type '"
						+ ret[i].getName() + "' but has return type '" + m.getReturnType().getName() + "'.");
			}

			if ((m.getModifiers() & 4095) != modifiers[i]) {
				errors.add("Method '" + names[i] + "' of class '" + clazz.getName() + "' should have modifiers '"
						+ Modifier.toString(modifiers[i]) + "' but has modifiers '"
						+ Modifier.toString(m.getModifiers()) + "'.");
			}
		}

		if (errors.size() > 0) {
			String error = "";
			for (String e : errors) {
				error = error + e + "\n";
			}
			fail(error);
		}
	}
	
	public static Method getMethod(Class<?> clazz, String name) {
		Method[] methods = clazz.getDeclaredMethods();

		HashMap<String, Method> map = new HashMap<String, Method>();
		for (Method m : methods) {
			map.put(signatureString(m), m);
		}
		
		if (!map.containsKey(name)) {
			//fail("Class '" + clazz.getName() + "' does not have method '" + name + "'.");
			return null;
		} else {
			return map.get(name);
		}
	}
	
	public static void checkMethod(Class<?> clazz, String name, Class<?> ret, int modifiers) {
		ArrayList<String> errors = new ArrayList<String>();
		Method[] methods = clazz.getDeclaredMethods();

		HashMap<String, Method> map = new HashMap<String, Method>();
		for (Method m : methods) {
			map.put(signatureString(m), m);
		}
		
		if (!map.containsKey(name)) {
			fail("Class '" + clazz.getName() + "' does not have method '" + name + "'.");
		}

		Method m = map.get(name);

		if (!m.getReturnType().equals(ret)) {
			errors.add("Method '" + name + "' of class '" + clazz.getName() + "' should have return type '"
					+ ret.getName() + "' but has return type '" + m.getReturnType().getName() + "'.");
		}

		if ((m.getModifiers() & 4095) != modifiers) {
			errors.add("Method '" + name + "' of class '" + clazz.getName() + "' should have modifiers '"
					+ Modifier.toString(modifiers) + "' but has modifiers '"
					+ Modifier.toString(m.getModifiers()) + "'.");
		}
		
		if (errors.size() > 0) {
			String error = "";
			for (String e : errors) {
				error = error + e + "\n";
			}
			fail(error);
		}
	}

	public static void checkConstructors(Class<?> clazz, String[] names, int[] modifiers) {
		ArrayList<String> errors = new ArrayList<String>();
		Constructor<?>[] constructors = clazz.getDeclaredConstructors();

		HashMap<String, Constructor<?>> map = new HashMap<String, Constructor<?>>();
		for (Constructor<?> c : constructors) {
			map.put(signatureString(c), c);
		}

		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i])) {
				errors.add("Class '" + clazz.getName() + "' does not have constructor '" + names[i] + "'.");
				continue;
			}

			Constructor<?> c = map.get(names[i]);

			if (c.getModifiers() != modifiers[i]) {
				errors.add("Constructor '" + names[i] + "' of class '" + clazz.getName() + "' should have modifiers '"
						+ Modifier.toString(modifiers[i]) + "' but has modifiers '"
						+ Modifier.toString(c.getModifiers()) + "'.");
			}
		}

		if (errors.size() > 0) {
			String error = "";
			for (String e : errors) {
				error = error + e + "\n";
			}
			fail(error);
		}
	}
	
	public static void checkGetters(Object obj, String[] attrNames) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for (String attrName : attrNames) {
			checkGetter(obj, attrName);
		}
	}
	
	public static void checkGetter(Object obj, String attrName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		checkGetter(obj, attrName, "get" + attrName.substring(0, 1).toUpperCase() + attrName.substring(1));
	}

	public static <E> void checkGetter(Object obj, String attrName, String getterName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends Object> clazz = obj.getClass();
		Method m = getMethod(clazz, getterName + "()");
		
		System.out.println(attrName);
		System.out.println(getterName);
		
		E o1 = getValueOfPrivateField(obj, attrName);
		E o2 = (E) m.invoke(obj);
		
		System.out.println(o1);
		System.out.println(o2);
		
		assertEquals(o1, o2, getterName + " does not return the value of " + attrName);
	}
	
	public static void checkInnerClass(String clazzName) {
		Class<?> innerClazz = null;
		try {
			innerClazz = Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			fail(e.getMessage());
		}
		if (innerClazz == null) {
			fail("Class " + clazzName + " does not exist!");
		}
	}

	public static Class<?> getInnerClass(String clazzName) {
		Class<?> innerClazz = null;
		try {
			innerClazz = Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			fail(e.getMessage());
		}

		return innerClazz;
	}

	// No System.exit
	// handled by AuDoscore
//	public static class ExitTrappedException extends SecurityException {
//		private static final long serialVersionUID = -2367164191038933517L;
//	}
//
//	// Source:
//	// http://stackoverflow.com/questions/5401281/preventing-system-exit-from-api
//	private static class ExitManager extends SecurityManager {
//
//		/** Deny permission to exit the VM. */
//		@Override
//		public void checkExit(int status) {
//			throw new ExitTrappedException();
//		}
//
//		public void checkPermission(Permission perm) {
//		}
//	}
//
//	private static final SecurityManager MANAGER;
//	private static final ExitManager EXIT_MANAGER;
//
//	static {
//		MANAGER = System.getSecurityManager();
//		EXIT_MANAGER = new ExitManager();
//	}
//
//	public static void forbidSystemExit() {
//		System.setSecurityManager(EXIT_MANAGER);
//	}
//
//	public static void allowSystemExit() {
//		System.setSecurityManager(MANAGER);
//	}

}
