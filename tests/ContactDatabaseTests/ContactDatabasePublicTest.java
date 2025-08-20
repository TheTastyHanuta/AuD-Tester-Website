import tester.annotations.*;

import org.junit.Test;

import java.lang.reflect.Method;

@Exercises({
        @Ex(exID = "0. Coderichtlinien", points = 2),
        @Ex(exID = "AuDHashTable", points = 1.5),
        @Ex(exID = "AuDOpenHashTable", points = 7.5),
        @Ex(exID = "AuDClosedHashTable", points = 11.5),
        @Ex(exID = "ContactDatabaseTests", points = 4.5),
})

@Forbidden({"java.util"})
// we allow java.util.Iterator because foreach needs it
@NotForbidden(value = {"java.util.LinkedList", "java.util.NoSuchElementException", "java.util.Iterator", "java.util.ListIterator"}, type = Forbidden.Type.FIXED)
public class ContactDatabasePublicTest {

    // ========== SYSTEM ===========
//      @Rule
//    public final PointsLogger pointsLogger = new PointsLogger();
//       @ClassRule
//    public final static PointsSummary pointsSummary = new PointsSummary();

    @Test(timeout = 500)
    @Points(exID = "0. Coderichtlinien", bonus = 1e-8, malus = 1, comment = " ")
    public void pubTest__checkCodeStyle() {
    }

    @Test(timeout = 500)
    @Points(exID = "ContactDatabaseTests",  malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__ContactDatabase() {
    }

    @Test(timeout = 500)
    @Points(exID = "AuDHashTable",  malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__AuDHashTable() {
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable",  malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__AuDClosedHashTable() {

        try {
            AuDClosedHashTable ch = new AuDClosedHashTable(10);
            Contact c = ch.getContact("abc");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AuDClosedHashTable ch = new AuDClosedHashTable(10);
            ch.insert(new Contact("email"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AuDClosedHashTable ch = new AuDClosedHashTable(10);
            boolean b = ch.isFull();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AuDClosedHashTable ch = new AuDClosedHashTable(10);
            ch.remove(new Contact("email"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AuDClosedHashTable ch = new AuDClosedHashTable(10);
            Method hash = ch.getClass().getDeclaredMethod("hash", String.class, int.class);
            hash.setAccessible(true);
            hash.invoke("a", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AuDClosedHashTable ch = new AuDClosedHashTable(10);
            int i = ch.hash("a");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AuDClosedHashTable ch = new AuDClosedHashTable(10);
            Method getIndexOf = ch.getClass().getDeclaredMethod("getIndexOf", String.class);
            getIndexOf.setAccessible(true);
            getIndexOf.invoke("a");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test(timeout = 500)
    @Points(exID = "AuDOpenHashTable",  malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__AuDOpenHashTable() {

        try {
            AuDOpenHashTable oh = new AuDOpenHashTable(10);
            Contact c = oh.getContact("a");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AuDOpenHashTable oh = new AuDOpenHashTable(10);
            oh.insert(new Contact("email"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AuDOpenHashTable oh = new AuDOpenHashTable(10);
            int i = oh.hash("a");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AuDOpenHashTable oh = new AuDOpenHashTable(10);
            oh.remove(new Contact("email"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


