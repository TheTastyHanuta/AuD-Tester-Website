/////// Gesamt: 11 Punkte (+2 Punkte auf Coderichtlinien)
public class CaesarChiffre {

	// 0.5 Punkte auf korrektes Anlegen beider String Konstanten
	public static final String GERMAN_LANGUAGE_PATTERN = "Werden zwei Glasstaebe mit einem "
			+ "Wolltuch gerieben, dann kann man feststellen, dass sich die beiden "
			+ "Staebe gegenseitig abstossen. Wird das gleiche Experiment mit zwei "
			+ "Kunststoffstaeben wiederholt, dann bleibt das Ergebnis gleich, auch "
			+ "diese beiden Staebe stossen sich gegenseitig ab. Im Gegensatz dazu ziehen "
			+ "sich ein Glas und ein Kunststoffstab gegenseitig an. Diese mit den Gesetzen "
			+ "der Mechanik nicht zu erklaerende Erscheinung fuehrt man auf Ladungen zurueck. "
			+ "Da sowohl Anziehung als auch Abstossung auftritt, muessen zwei verschiedene "
			+ "Arten von Ladungen existieren. Man unterscheidet daher positive und negative Ladungen.";

	// TODO: JEDES SEMESTER DEN TEXT AUSTAUSCHEN!!!
	public static final String ENCRYPTED_MESSAGE = "ugjt iwv! fw jcuv fgp eqfg igmpcemv wpf fkt uq twjo wpf gjtg gtyqtdgp. pqtocngtygkug mcpp ocp jkgt inwgjygkp qfgt cgjpnkejgu igykppgp. fkgu hcgnnv kp fkgugo ugoguvgt ngkfgt cwu... uqtt{";

	public static final char SEPARATOR = ' ';

	// Gesamt: 1.5 Punkte
	public static int getIndexOfMaximumEntry(int[] values) {
		// 0.5 Punkte auf korrektes Anlegen und Zurueckgeben von maxIndex
		int maxIndex = 0;

		// 1 Punkte auf korrekte Schleife
		for (int i = 0; i < values.length; i++) {
			if (values[i] > values[maxIndex]) {
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	// Gesamt: 2.5 Punkte
	public static int[] getHistogram(String text) {
		// 0.5 Punkte auf korrektes Anlegen von histogram
		// (256 als groesse ebenfalls korrekt (extended ASCII))
		int[] histogram = new int[128];

		// 0.5 Punkte auf Umwandlung in Kleinbuchstaben
		text = text.toLowerCase();

		// 1.5 Punkte auf Schleife:
		// 0.5 auf korrektes Verwenden von charAt(),
		// 0.5 auf Nicht-mitzaehlen von separator,
		// 0.5 Punkte auf korretes Inkrementieren
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == SEPARATOR) {
				continue;
			}
			histogram[c]++;
		}

		return histogram;
	}

	// Gesamt: 2 Punkte
	public static char getSignificantLetter(String text) {
		int[] histogram = getHistogram(text);

		// 0.5 Punkte auf korrektes Anlegen von significantLetter
		char significantLetter = (char) getIndexOfMaximumEntry(histogram);
		// 0.5 Punkte auf korrektes Anlegen von quantity
		int quantity = histogram[significantLetter];
		// 0.5 Punkte auf korrektes Anlegen von quota
		int quota = (int) (((double) quantity) / text.length() * 100.0);

		System.out.println("Most significant letter: " + significantLetter);
		System.out.println("Quantity: " + quantity + " times (" + quota + " % of whole text)");

		// 0.5 Punkte auf Rueckgabe
		return significantLetter;
	}

	// Gesamt: 1.5 Punkte
	public static int getShift(String encryptedText, String languagePattern) {
		// 0.5 Punkte auf Anlegen und korrekten Aufruf der Methoden
		char sigOfChiffre = getSignificantLetter(encryptedText);
		char sigOfPattern = getSignificantLetter(languagePattern);

		// 0.5 Punkte auf Berechnen von shift
		int shift = sigOfPattern - sigOfChiffre;
		//int shift = sigOfChiffre - sigOfPattern;

		// 0.5 Punkte auf korrekte Ausgabe
		System.out.println("Most significant letter in the pattern text: " + sigOfPattern);
		System.out.println("Most significant letter in the encrypted text: " + sigOfChiffre);
		System.out.println("Resulting shift: " + shift);

		return shift;
	}

	// Gesamt: 3 Punkte
	public static String decode(String encryptedText, String languagePattern) {
		int shift = getShift(encryptedText, languagePattern);

		char[] lettersEncryptedText = encryptedText.toCharArray();

		// 2 Punkte auf Schleife mit korrekter Abfrage und korrektem Decodieren
		for (int i = 0; i < lettersEncryptedText.length; i++) {
			if (('a' - shift) <= lettersEncryptedText[i] && lettersEncryptedText[i] <= ('z' - shift)) {
				lettersEncryptedText[i] += shift;
			}
			/*
			if (lettersEncryptedText[i] >= ('a' + shift) && lettersEncryptedText[i] <= ('z' + shift)) {
				lettersEncryptedText[i] -= shift;
			}
			 */
		}
		// 1 Punkt auf Initialisieren von decoded + Rueckgabe
		String decoded = new String(lettersEncryptedText);
		return decoded;
	}

	// Gesamt: 0 Punkte
	public static void main(String[] args) {
		// 0.5 Punkte auf korrektes Anlegen von decodedText und Aufrufen der Methoden
		String decodedText = decode(ENCRYPTED_MESSAGE, GERMAN_LANGUAGE_PATTERN);

		// 0.5 Punkte auf korrekte Ausgabe
		System.out.println("Unreadable, encrypted text:\n" + ENCRYPTED_MESSAGE);
		System.out.println("Readable, decoded output text:\n" + decodedText);

		// 0.5 Punkte auf korrekt entschluesselten Text
		/*
		 * sehr gut! du hast den code geknackt und dir so ruhm und ehre erworben.
		 * normalerweise kann man hier gluehwein oder aehnliches gewinnen.
		 * dies faellt in diesem semester leider aus... sorry
		 */

		// 0.5 Punkte auf korrekten Shift
		/*
		 * Shift: -2
		 * Alternativ: +2 (Je nach getShift-Methode)
		 */
	}

}
