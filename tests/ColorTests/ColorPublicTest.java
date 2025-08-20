import tester.annotations.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@Exercises({ @Ex(exID = ColorPublicTest._0CODESTYLE, points = 2), @Ex(exID = ColorPublicTest._1CONSTANTS, points = 1.5),
		@Ex(exID = ColorPublicTest._2CONSTRUCTORS, points = 5.5), @Ex(exID = ColorPublicTest._3GETTERS, points = 3.5),
		@Ex(exID = ColorPublicTest._4GETHEX, points = 3), @Ex(exID = ColorPublicTest._5MISC, points = 2.5) })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ColorPublicTest {
	public static final String _0CODESTYLE = "0. Coderichtlinien";
	public static final String _1CONSTANTS = "1. Konstanten";
	public static final String _2CONSTRUCTORS = "2. Konstruktoren";
	public static final String _3GETTERS = "3. getRgb, getRed, getGreen, getBlue";
	public static final String _4GETHEX = "4. getHex";
	public static final String _5MISC = "5. complementaryColor, mixColor, toString";

	// ========== SYSTEM ===========
//	@Rule
//	public final PointsLogger pointsLogger = new PointsLogger();
//	@ClassRule
//	public final static PointsSummary pointsSummary = new PointsSummary();

	// ========== TESTS ===========
	@Test(timeout = 500)
	@Points(exID = _0CODESTYLE, bonus = 1e-8, malus = 1, comment = "Noch nicht korrigiert")
	public void pubTest__checkCodeStyle() {
	}
	
	@Test(timeout = 500)
	@Points(exID = _1CONSTANTS, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkConstantsExist() {
//		assertNotNull("BLACK missing.", Color.Color.BLACK);
//		assertNotNull("WHITE missing.", Color.Color.WHITE);
//		assertNotNull("GREY missing.", Color.Color.GREY);
//
//		assertNotNull("RED missing.", Color.Color.RED);
//		assertNotNull("GREEN missing.", Color.Color.GREEN);
//		assertNotNull("BLUE missing.", Color.Color.BLUE);
	}

	@Test(timeout = 500)
	@Points(exID = _2CONSTRUCTORS, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkConstructorsExist() {
//		String[] names = new String[] { "rgb" };
//		Class<?>[] types = new Class<?>[] { int.class };
//		int[] modifiers = new int[] { Modifier.PRIVATE };
//
//		Color.TestcaseHelper.checkAttributes(Color.Color.class, names, types, modifiers);
		try {
			Color c1 = new Color();
			Color c2 = new Color(0);
			Color c3 = new Color("#FFFFFF");
			Color c4 = new Color(255, 255, 255);
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 500)
	@Points(exID = _3GETTERS, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkGettersExist() {
		try {
			Color c = new Color(0);
			int r = c.getRed();
			int b = c.getBlue();
			int g = c.getGreen();
			int rgb = c.getRgb();
		} catch (Exception e) {
			
		}
		
	}

	@Test(timeout = 500)
	@Points(exID = _4GETHEX, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkGetHexExists() {
		try {
			Color c = new Color(0);
			String s = c.getHex();
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 500)
	@Points(exID = _5MISC, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkMiscExists() {
		try {
			Color c = new Color(0);
			String s = c.toString();
			int rgb = c.complementaryColor().getRgb();
			int rgb2 = c.mixColor(new Color()).getRgb();
		} catch (Exception e) {
			
		}
	}
}
