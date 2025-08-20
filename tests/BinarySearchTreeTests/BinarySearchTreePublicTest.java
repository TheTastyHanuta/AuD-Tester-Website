import tester.annotations.*;

import org.junit.Test;

import java.util.List;

@Exercises({
	@Ex(exID = "0. Coderichtlinien", points = 2),
	@Ex(exID = "ElementExistsException", points = 0.5),
	@Ex(exID = "BinarySearchTreeTests", points = 17.5)
})
public class BinarySearchTreePublicTest {
	// ========== SYSTEM ===========
//	@Rule
//	public final PointsLogger pointsLogger = new PointsLogger();
//	@ClassRule
//	public final static PointsSummary pointsSummary = new PointsSummary();
	
	@Test(timeout = 500)
	@Points(exID = "0. Coderichtlinien", bonus = 1e-8, malus = 1, comment = " ")
	public void pubTest__checkCodeStyle() {
	}
	
	@Test(timeout = 500)
	@Points(exID = "ElementExistsException", malus = 1000, bonus = 0.000001, comment = " ")
	public void pubTest__exception() {
		try {
			ElementExistsException e = null;
			System.out.println(e.getMessage());
		} catch (Exception e) {
			// do nothing
		}
	}
	
	@Test(timeout = 500)
	@Points(exID = "BinarySearchTreeTests", malus = 1000, bonus = 0.000001, comment = "Methoden mit Fehlern werden noch manuell nachkorrigiert (-> Teilpunkte moeglich)")
	public void pubTest__bst() {
		try {
			BinarySearchTree bst = new BinarySearchTree();
			
			try {
				bst.clear();
			} catch (Exception e) {
				// do nothing
			}
			
			try {
				boolean b = bst.exists(42);
				System.out.println(b);
			} catch (Exception e) {
				// do nothing
			}
			
			try {
				bst.insert(42);
			} catch (Exception e) {
				// do nothing
			}
	
			try {
				bst.remove(42);
			} catch (Exception e) {
				// do nothing
			}
			
			try {
				List<Integer> l = bst.inOrderList();
				System.out.println(l.toString());
			} catch (Exception e) {
				// do nothing
			}
		} catch (Exception e) {
			
		}
	}
}
