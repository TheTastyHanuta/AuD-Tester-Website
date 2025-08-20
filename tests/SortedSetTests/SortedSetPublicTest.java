import tester.annotations.*;

import org.junit.Test;



@Exercises({
        @Ex(exID = "0. Coderichtlinien", points = 2),
        @Ex(exID = "ElementExistsException", points = 2),
        @Ex(exID = "SortedSetTests", points = 32)
})

public class SortedSetPublicTest {

    // ========== SYSTEM ===========
//    @Rule
//    public final PointsLogger pointsLogger = new PointsLogger();
//    @ClassRule
//    public final static PointsSummary pointsSummary = new PointsSummary();

    // ========== TESTS ===========

    @Test(timeout = 500)
    @Points(exID = "0. Coderichtlinien", bonus = 1e-8, malus = 1, comment = "Noch nicht korrigiert")
    public void pubTest__checkCodeStyle() {
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__checkSortedSet() {
        SortedSet test = new SortedSet();

        // TODO: Attribute auch testen?
        // test will fail if NullPointerException occurs


        TestcaseHelper.getValueOfPrivateField(test, "head");
        TestcaseHelper.getValueOfPrivateField(test, "tail");


        try {
            test.add(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            test.add(new int[]{5, 24, 30});
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            boolean c = test.contains(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int s = test.size();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            test.remove(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            OrderedSet os = test.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            OrderedSet os = test.intersect(new SortedSet());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            OrderedSet os = test.unite(new SortedSet());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            OrderedSet os = test.subtract(new SortedSet());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            OrderedSet os = test.getSetInBetween(0, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int[] a = test.toArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int[] a = test.toReversedArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String s = test.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            test.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object o = new SortedSet().new ListItem(5);
    }


    @Test(timeout = 500)
    @Points(exID = "ElementExistsException", malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__checkElementExistsException() {

        ElementExistsException e1 = new ElementExistsException();
        ElementExistsException e2 = new ElementExistsException("Test");
        System.out.println(e1.getMessage() + e2.getMessage());

    }
}
