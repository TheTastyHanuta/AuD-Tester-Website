import tester.annotations.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@Exercises({ @Ex(exID = CaesarChiffrePublicTest._0CODESTYLE, points = 2),
		@Ex(exID = CaesarChiffrePublicTest._1CONSTANTS, points = 0.5),
		@Ex(exID = CaesarChiffrePublicTest._2GETINDEXOFMAXIMUMENTRY, points = 1.5),
		@Ex(exID = CaesarChiffrePublicTest._3GETHISTOGRAM, points = 2.5),
		@Ex(exID = CaesarChiffrePublicTest._4GETSIGNIFICANTLETTER, points = 2),
		@Ex(exID = CaesarChiffrePublicTest._5GETSHIFT, points = 1.5),
		@Ex(exID = CaesarChiffrePublicTest._6DECODE, points = 3) })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CaesarChiffrePublicTest {

	public static final String _0CODESTYLE = "0. Coderichtlinien";
	public static final String _1CONSTANTS = "1. Konstanten";
	public static final String _2GETINDEXOFMAXIMUMENTRY = "2. getIndexOfMaximumEntry";
	public static final String _3GETHISTOGRAM = "3. getHistogram";
	public static final String _4GETSIGNIFICANTLETTER = "4. getSignificantLetter";
	public static final String _5GETSHIFT = "5. getShift";
	public static final String _6DECODE = "6. decode";

	// ========== SYSTEM ===========
//	@Rule
//	public final PointsLogger pointsLogger = new PointsLogger();
//	@ClassRule
//	public final static PointsSummary pointsSummary = new PointsSummary();

	public static final String GERMAN_LANGUAGE_PATTERN = "Werden zwei Glasstaebe mit einem "
			+ "Wolltuch gerieben, dann kann man feststellen, dass sich die beiden "
			+ "Staebe gegenseitig abstossen. Wird das gleiche Experiment mit zwei "
			+ "Kunststoffstaeben wiederholt, dann bleibt das Ergebnis gleich, auch "
			+ "diese beiden Staebe stossen sich gegenseitig ab. Im Gegensatz dazu ziehen "
			+ "sich ein Glas und ein Kunststoffstab gegenseitig an. Diese mit den Gesetzen "
			+ "der Mechanik nicht zu erklaerende Erscheinung fuehrt man auf Ladungen zurueck. "
			+ "Da sowohl Anziehung als auch Abstossung auftritt, muessen zwei verschiedene "
			+ "Arten von Ladungen existieren. Man unterscheidet daher positive und negative Ladungen.";

	public static final String ENCRYPTED_MESSAGE = "I;>H =KJ! :K >7IJ :;D 9E:; =;AD79AJ KD: :?H ;?D; 8;"
			+ "BE>DK= <K;H :?; =;J7D; 7H8;?J H;:B?9> L;H:?;DJ. M;DD :K :;?D;C JKJEH ;HAB7;H;D A7DDIJ, M"
			+ "7I FEBOCEHF>?; 8;:;KJ;J, KD: =BK;9A >7IJ, 8;AECCIJ :K L?;BB;?9>J ;?D;D =BK;>M;?D 7KI=;=;"
			+ "8;D. L?;B IF7II!";

	
	// ========== TESTS ===========
	@Test(timeout = 500)
	@Points(exID = _0CODESTYLE, bonus = 1e-8, malus = 1, comment = "Noch nicht korrigiert")
	public void pubTest__checkCodeStyle() {
	}

	@Test(timeout = 500)
	@Points(exID = _1CONSTANTS, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkConstantsExist() {
	}

	@Test(timeout = 500)
	@Points(exID = _2GETINDEXOFMAXIMUMENTRY, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkGetIndexOfMaximumEntryExists() {
		try {
			CaesarChiffre.getIndexOfMaximumEntry(new int[] { 1, 2, 3, 4, 5 });
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 500)
	@Points(exID = _3GETHISTOGRAM, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkGetHistogramExists() {
		try {
			CaesarChiffre.getHistogram("abcdefgabcdefga");
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 500)
	@Points(exID = _4GETSIGNIFICANTLETTER, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkGetSignificantLetterExists() {
		try {
			CaesarChiffre.getSignificantLetter("abcdefgabcdefga");
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 500)
	@Points(exID = _5GETSHIFT, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkGetShiftExists() {
		try {
			CaesarChiffre.getShift(ENCRYPTED_MESSAGE, GERMAN_LANGUAGE_PATTERN);
		} catch (Exception e) {
			
		}
		
	}

	@Test(timeout = 500)
	@Points(exID = _6DECODE, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkDecodeExists() {
		try {
			CaesarChiffre.decode(ENCRYPTED_MESSAGE, GERMAN_LANGUAGE_PATTERN);
		} catch (Exception e) {
			
		}
	}
}
