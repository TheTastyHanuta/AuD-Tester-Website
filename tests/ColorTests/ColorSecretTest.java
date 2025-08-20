import tester.annotations.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SecretClass
public class ColorSecretTest {
	// ========== SYSTEM ===========
//	@Rule
//	public final PointsLogger pointsLogger = new PointsLogger();
//
//	@ClassRule
//	public final static PointsSummary pointsSummary = new PointsSummary();


	// ========= TEST DATA =========
	private static final Random RND = new Random(4711_0815_666L);
	
	// ====== TESTS ======
	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._0CODESTYLE, bonus = 1, comment = "Noch nicht korrigiert")
	public void secTest__checkCodeStyle() {
		fail();
	}
	
	// ---- Constants ----
	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._1CONSTANTS, bonus = 0.25, comment = "BLACK")
	public void secTest__BLACK() {
		String[] names = new String[] { "BLACK" };
		Class<?>[] types = new Class<?>[] { Color.class };
		int[] modifiers = new int[] { Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL };

		TestcaseHelper.checkAttributes(Color.class, names, types, modifiers);
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._1CONSTANTS, bonus = 0.25, comment = "WHITE")
	public void secTest__WHITE() {
		String[] names = new String[] { "WHITE" };
		Class<?>[] types = new Class<?>[] { Color.class };
		int[] modifiers = new int[] { Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL };

		TestcaseHelper.checkAttributes(Color.class, names, types, modifiers);
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._1CONSTANTS, bonus = 0.25, comment = "GREY/GRAY")
	public void secTest__GREY() {
		Field[] fields = Color.class.getDeclaredFields();

		HashMap<String, Field> map = new HashMap<String, Field>();
		for (Field f : fields) {
			map.put(f.getName(), f);
		}
		
		String[] names;
		if (map.containsKey("GREY")) {
			names = new String[] { "GREY" };
		} else {
			names = new String[] { "GRAY" };
		}
		Class<?>[] types = new Class<?>[] { Color.class };
		int[] modifiers = new int[] { Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL };

		TestcaseHelper.checkAttributes(Color.class, names, types, modifiers);
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._1CONSTANTS, bonus = 0.25, comment = "RED")
	public void secTest__RED() {
		String[] names = new String[] { "RED" };
		Class<?>[] types = new Class<?>[] { Color.class };
		int[] modifiers = new int[] { Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL };

		TestcaseHelper.checkAttributes(Color.class, names, types, modifiers);
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._1CONSTANTS, bonus = 0.25, comment = "GREEN")
	public void secTest__GREEN() {
		String[] names = new String[] { "GREEN" };
		Class<?>[] types = new Class<?>[] { Color.class };
		int[] modifiers = new int[] { Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL };

		TestcaseHelper.checkAttributes(Color.class, names, types, modifiers);
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._1CONSTANTS, bonus = 0.25, comment = "BLUE")
	public void secTest__BLUE() {
		String[] names = new String[] { "BLUE" };
		Class<?>[] types = new Class<?>[] { Color.class };
		int[] modifiers = new int[] { Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL };

		TestcaseHelper.checkAttributes(Color.class, names, types, modifiers);
	}

	// ---- Constructors ----
	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._2CONSTRUCTORS, bonus = 0.5, comment = "Attribut rgb")
	public void secTest__Constructors__attribute_rgb() {
		String[] names = new String[] { "rgb" };
		Class<?>[] types = new Class<?>[] { int.class };
		int[] modifiers = new int[] { Modifier.PRIVATE };

		TestcaseHelper.checkAttributes(Color.class, names, types, modifiers);
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._2CONSTRUCTORS, bonus = 0.5, comment = "ColorTests.Color(int)")
	public void secTest__constructors__rgb() {
		String[] names = new String[] { "ColorTests.Color(int)" };
		int[] modifiers = new int[names.length];
		Arrays.fill(modifiers, Modifier.PUBLIC);
		TestcaseHelper.checkConstructors(Color.class, names, modifiers);

		Color white = new Color(ColorRef.WHITE.getRgb());
		TestcaseHelper.assertEquals(ColorRef.WHITE.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(white, "rgb"),
				"Constructor not setting rgb value correctly");

		Color green = new Color(ColorRef.GREEN.getRgb());
		TestcaseHelper.assertEquals(ColorRef.GREEN.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(green, "rgb"),
				"Constructor not setting rgb value correctly");

		Color color = new Color(0);
		ColorRef colorRef = new ColorRef(0);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");

		color = new Color(4620980);
		colorRef = new ColorRef(4620980);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._2CONSTRUCTORS, bonus = 1, comment = "ColorTests.Color(int, int, int)")
	public void secTest__constructors__red_green_blue() {
		String[] names = new String[] { "ColorTests.Color(int, int, int)" };
		int[] modifiers = new int[names.length];
		Arrays.fill(modifiers, Modifier.PUBLIC);
		TestcaseHelper.checkConstructors(Color.class, names, modifiers);

		Color color = new Color(0, 250, 0);
		ColorRef colorRef = new ColorRef(0, 250, 0);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");

		color = new Color(0, 0, 0);
		colorRef = new ColorRef(0, 0, 0);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._2CONSTRUCTORS, bonus = 0.5, comment = "ColorTests.Color(int, int, int) - invalid red")
	public void secTest__constructors__red_green_blue__check_red() {
		Color color = new Color(-1, 0, 0);
		ColorRef colorRef = new ColorRef(-1, 0, 0);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");

		color = new Color(300, 0, 0);
		colorRef = new ColorRef(300, 0, 0);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._2CONSTRUCTORS, bonus = 0.5, comment = "ColorTests.Color(int, int, int) - invalid green")
	public void secTest__constructors__red_green_blue__check_green() {
		Color color = new Color(0, -1, 0);
		ColorRef colorRef = new ColorRef(0, -1, 0);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");

		color = new Color(0, 300, 0);
		colorRef = new ColorRef(0, 300, 0);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._2CONSTRUCTORS, bonus = 0.5, comment = "ColorTests.Color(int, int, int) - invalid blue")
	public void secTest__constructors__red_green_blue__check_blue() {
		Color color = new Color(0, 0, -1);
		ColorRef colorRef = new ColorRef(0, 0, -1);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");

		color = new Color(0, 0, 300);
		colorRef = new ColorRef(0, 0, 300);
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._2CONSTRUCTORS, bonus = 0.5, comment = "ColorTests.Color()")
	public void secTest__constructors__empty() {
		String[] names = new String[] { "ColorTests.Color()" };
		int[] modifiers = new int[names.length];
		Arrays.fill(modifiers, Modifier.PUBLIC);
		TestcaseHelper.checkConstructors(Color.class, names, modifiers);

		Color black = new Color();
		TestcaseHelper.assertEquals(0, (int) TestcaseHelper.getValueOfPrivateField(black, "rgb"),
				"Default constructor not setting rgb value correctly");
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._2CONSTRUCTORS, bonus = 1.5, comment = "ColorTests.Color(String)")
	public void secTest__constructors__hex() {
		String[] names = new String[] { "ColorTests.Color(java.lang.String)" };
		int[] modifiers = new int[names.length];
		Arrays.fill(modifiers, Modifier.PUBLIC);
		TestcaseHelper.checkConstructors(Color.class, names, modifiers);

		ColorRef colorRef = new ColorRef(0x0A0B0C);
		Color color = new Color(colorRef.toString());
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");

		colorRef = new ColorRef(0, 0, 0);
		color = new Color(colorRef.toString());
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");

		colorRef = new ColorRef(1, 1, 1);
		color = new Color(colorRef.toString());
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");

		colorRef = new ColorRef(45, 45, 50);
		color = new Color(colorRef.toString());
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");

		colorRef = new ColorRef(123, 56, 3);
		color = new Color(colorRef.toString());
		TestcaseHelper.assertEquals(colorRef.getRgb(), (int) TestcaseHelper.getValueOfPrivateField(color, "rgb"),
				"Constructor not setting rgb value correctly");
	}

	// ---- Getters ----
	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._3GETTERS, bonus = 0.5, comment = "getRgb()")
	public void secTest__getters__getRgb() {
		for (int i = 0; i < 10; i++) {
			int[] colorArray = getRandomColor();

			ColorRef colorRef = new ColorRef(colorArray[0], colorArray[1], colorArray[2]);
			Color color = new Color(colorArray[0], colorArray[1], colorArray[2]);

			TestcaseHelper.assertEquals(colorRef.getRgb(), color.getRgb(),
					"getRgb() not working correctly for value " + Arrays.toString(colorArray) + " !");
		}
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._3GETTERS, bonus = 1, comment = "getRed()")
	public void secTest__getters__getRed() {
		for (int i = 0; i < 10; i++) {
			int[] colorArray = getRandomColor();

			ColorRef colorRef = new ColorRef(colorArray[0], colorArray[1], colorArray[2]);
			Color color = new Color(colorArray[0], colorArray[1], colorArray[2]);

			TestcaseHelper.assertEquals(colorRef.getRed(), color.getRed(),
					"getRed() not working correctly for value " + Arrays.toString(colorArray) + " !");
		}		
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._3GETTERS, bonus = 1, comment = "getGreen()")
	public void secTest__getters__getGreen() {
		for (int i = 0; i < 10; i++) {
			int[] colorArray = getRandomColor();

			ColorRef colorRef = new ColorRef(colorArray[0], colorArray[1], colorArray[2]);
			Color color = new Color(colorArray[0], colorArray[1], colorArray[2]);

			TestcaseHelper.assertEquals(colorRef.getGreen(), color.getGreen(),
					"getGreen() not working correctly for value " + Arrays.toString(colorArray) + " !");
		}
	}


	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._3GETTERS, bonus = 1, comment = "getBlue()")
	public void secTest__getters__getBlue() {
		for (int i = 0; i < 10; i++) {
			int[] colorArray = getRandomColor();

			ColorRef colorRef = new ColorRef(colorArray[0], colorArray[1], colorArray[2]);
			Color color = new Color(colorArray[0], colorArray[1], colorArray[2]);

			TestcaseHelper.assertEquals(colorRef.getBlue(), color.getBlue(),
					"getBlue() not working correctly for value " + Arrays.toString(colorArray) + " !");
		}
	}

	
	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._4GETHEX, bonus = 1, comment = "leading zeros")
	public void secTest__getHex__leading_zeros() {
		Color white = new Color(0x00000F);
		String hex = white.getHex();
		if (hex.startsWith("#")) {
			assertEquals("No leading zeros. Length of output should be 7.", 7, hex.length());
		} else {
			assertEquals("No leading zeros. Length of output should be 6.", 6, hex.length());			
		}
		
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._4GETHEX, bonus = 0.5, comment = "# prefix")
	public void secTest__getHex__prefix() {
		int[] colorArray = getRandomColor();
		assertTrue("Prefix (#) is missing", new Color(colorArray[0], colorArray[1], colorArray[2]).getHex().startsWith("#"));
	}
	
	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._4GETHEX, bonus = 1, comment = "conversion")
	public void secTest__getHex__conversion() {
		for (int i = 0; i < 10; i++) {
			int[] colorArray = getRandomColor();

			ColorRef colorRef = new ColorRef(colorArray[0], colorArray[1], colorArray[2]);
			Color color = new Color(colorArray[0], colorArray[1], colorArray[2]);
			
			// convert hex output to base 10 and compare to internal rgb value
			String hex = color.getHex();
			hex = hex.replace("#", "");
			int rgb = Integer.parseInt(hex, 16);
			
			assertTrue("Hex value is not correct. Expected: " + colorRef.getHex().replace("#", "") + ", Got: " + hex,
					(int) TestcaseHelper.getValueOfPrivateField(color, "rgb") == rgb);
		}
	}
	
	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._4GETHEX, bonus = 0.5, comment = "upper case?")
	public void secTest__getHex__upper_case() {
		Color white = new Color(0xABCDEF);
		assertEquals("Letters are not upper case", white.getHex().toUpperCase(), white.getHex());
	}
	
	
	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._5MISC, bonus = 1, comment = "complementaryColor")
	public void secTest__complementaryColer() {
		for (int i = 0; i < 10; i++) {
			int[] colorArray = getRandomColor();

			ColorRef colorRef = new ColorRef(colorArray[0], colorArray[1], colorArray[2]);
			Color color = new Color(colorArray[0], colorArray[1], colorArray[2]);
			
			assertEquals("complementary color is wrong for color " + colorRef.getHex(),
					colorRef.complementaryColor().getRgb(), color.complementaryColor().getRgb());
		}
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._5MISC, bonus = 1, comment = "mixColor")
	public void secTest__mixColor() {
		for (int i = 0; i < 10; i++) {
			int[] colorArray = getRandomColor();
			int[] mixColorArray = getRandomColor();

			ColorRef colorRef = new ColorRef(colorArray[0], colorArray[1], colorArray[2]);
			Color color = new Color(colorArray[0], colorArray[1], colorArray[2]);

			ColorRef mixColorRef = new ColorRef(mixColorArray[0], mixColorArray[1], mixColorArray[2]);
			Color mixColor = new Color(mixColorArray[0], mixColorArray[1], mixColorArray[2]);

			assertEquals("mix of " + colorRef.getHex() + " and " + mixColorRef.getHex() + " is incorrect.",
					colorRef.mixColor(mixColorRef).getRgb(), color.mixColor(mixColor).getRgb());
		}
	}

	@Test(timeout = 500)
	@Points(exID = ColorPublicTest._5MISC, bonus = 0.5, comment = "toString")
	public void secTest__toString() {
		for (int i = 0; i < 10; i++) {
			int[] colorArray = getRandomColor();

			Color color = new Color(colorArray[0], colorArray[1], colorArray[2]);

			assertEquals("toString() should be the same as getHex()", color.getHex(), color.toString());
		}
	}

	public static int[] getRandomColor() {
		return RND.ints(3, 0, 256).toArray();
	}

	public static class ColorRef {
		public static final ColorRef BLACK = new ColorRef();
		public static final ColorRef WHITE = new ColorRef(0xFFFFFF);
		public static final ColorRef GREY = new ColorRef(128, 128, 128);
		public static final ColorRef RED = new ColorRef(0xFF0000);
		public static final ColorRef GREEN = new ColorRef(0x00FF00);
		public static final ColorRef BLUE = new ColorRef(0x0000FF);

		private int rgb;

		public ColorRef(int rgb) {
			this.rgb = rgb;
		}

		public ColorRef(int red, int green, int blue) {

			if (red > 255 || red < 0) {
				System.err.println("Invalid red value! Setting to default value.");
				red = Math.max(Math.min(red, 255), 0);
			}

			if (green > 255 || green < 0) {
				System.err.println("Invalid green value! Setting to default value");
				green = Math.max(Math.min(green, 255), 0);
			}

			if (blue > 255 || blue < 0) {
				System.err.println("Invalid blue value! Setting to default value");
				blue = Math.max(Math.min(blue, 255), 0);
			}

			this.rgb = (red << 16) | (green << 8) | blue;
		}

		public ColorRef() {
			this(0x000000);
		}

		public ColorRef(String hex) {
			hex = hex.substring(1);

			this.rgb = Integer.parseInt(hex, 16);
		}

		public int getRgb() {
			return rgb;
		}

		public int getRed() {
			return (rgb >> 16) & 0xFF;
		}

		public int getGreen() {
			return (rgb >> 8) & 0xFF;
		}

		public int getBlue() {
			return rgb & 0xFF;
		}

		public ColorRef complementaryColor() {
			return new ColorRef(255 - getRed(), 255 - getGreen(), 255 - getBlue());
		}

		public ColorRef mixColor(ColorRef color) {
			return new ColorRef((getRed() + color.getRed()) / 2, (getGreen() + color.getGreen()) / 2,
					(getBlue() + color.getBlue()) / 2);
		}

		public String getHex() {
			String hex = "";

			hex = Integer.toHexString(rgb).toUpperCase();

			while (hex.length() < 6) {
				hex = "0" + hex;
			}

			return "#" + hex;
		}

		@Override
		public String toString() {
			return getHex();
		}
	}

}
