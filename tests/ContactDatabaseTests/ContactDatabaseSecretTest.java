import tester.annotations.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SecretClass
public class ContactDatabaseSecretTest {
    // =============== SYSTEM ===============
//        @Rule
//    public final PointsLogger pointsLogger = new PointsLogger();
//        @ClassRule
//    public static final PointsSummary pointsSummary = new PointsSummary();

    // ========= TEST DATA =========
    private static final Random RND = new Random(4711_0815_666L);

    // ============ TESTS ==========

    @Test(timeout = 500)
    @Points(exID = "0. Coderichtlinien", bonus = 2, comment = "Wird noch manuell korrigiert.")
    public void secTest__checkCodeStyle() {
        fail();
    }

    // ============ AuDHashTable ==========

    // Klasse wird zu Verfuegung gestellt
    /*
    @Test(timeout = 500)
    @Points(exID = "AuDHashTable", bonus = 0.5, comment = "Signatur")
    public void secTest__AuDHashTable__signature() {
        TestcaseHelper.checkClassSignature(AuDHashTable.class, Modifier.PUBLIC | Modifier.ABSTRACT);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDHashTable", bonus = 0.5, comment = "Attribut capacity")
    public void secTest__AuDHashTable__attributes_capacity() {
        TestcaseHelper.checkAttributeModifiers(AuDHashTable.class, "capacity", Modifier.PROTECTED);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDHashTable", bonus = 0.5, comment = "Konstruktor (capacity)")
    public void secTest__AuDHashTable__constructor() {
        AuDHashTableTestcaseHelper ht = new AuDHashTableTestcaseHelper(10);
        TestcaseHelper.checkAttributeExists(AuDHashTable.class, "capacity");
        assertEquals("The constructor does not set the capacity correctly.", 10, ht.getCapacity());
    }

    @Test(timeout = 500)
    @Points(exID = "AuDHashTable", bonus = 0.5, comment = "insert")
    public void secTest__AuDHashTable__insert() {
        TestcaseHelper.assertMethodExists(AuDHashTable.class, "insert", Contact.class);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDHashTable", bonus = 0.5, comment = "remove")
    public void secTest__AuDHashTable__remove() {
        TestcaseHelper.assertMethodExists(AuDHashTable.class, "remove", Contact.class);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDHashTable", bonus = 0.5, comment = "getContact")
    public void secTest__AuDHashTable__getContact() {
        TestcaseHelper.assertMethodExists(AuDHashTable.class, "getContact", String.class);
    }
    */

    @Test(timeout = 500)
    @Points(exID = "AuDHashTable", bonus = 1.5, comment = "hash")
    public void secTest__AuDHashTable__hash() {
        String s1 = "";
        String s2 = "What is the hash-value of this String?";
        AuDHashTableTestcaseHelper ht = new AuDHashTableTestcaseHelper(10);
        assertEquals("The hash-value was not calculated correctly.", ht.cleanHash(s1), ht.testHash(s1));
        assertEquals("The hash-value was not calculated correctly.", ht.cleanHash(s2), ht.testHash(s2));
    }

    // ============ AuDClosedHashTable ==========
    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "Signatur")
    public void secTest__AuDClosedHashTable__signature() {
        TestcaseHelper.checkClassSignature(AuDClosedHashTable.class, Modifier.PUBLIC);
        TestcaseHelper.checkSuperclass(AuDClosedHashTable.class, AuDHashTable.class);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "Attribute")
    public void secTest__AuDClosedHashTable__attributes() {
        TestcaseHelper.checkAttributesExist(AuDClosedHashTable.class, new String[]{"table", "deleted", "counter"});
        TestcaseHelper.assertAllAttributesArePrivate(AuDClosedHashTable.class);
    }

    private int _helperGetCapacity(Object obj) {
        try {
            Field privateField = obj.getClass().getSuperclass().getSuperclass().getDeclaredField("capacity");
            privateField.setAccessible(true);
            return (int) privateField.get(obj);
        } catch (Exception e) {
            try {
                Field privateField = obj.getClass().getSuperclass().getDeclaredField("capacity");
                privateField.setAccessible(true);
                return (int) privateField.get(obj);
            } catch (Exception e2) {
                e.printStackTrace();
                fail("Irgendetwas ist schief gelaufen. Wird manuell geprueft.");
            }
        }
        return 0;
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "Konstruktor (super)")
    public void secTest__AuDClosedHashTable__constructor_super() {
        try {
            AuDClosedHashTable.class.getDeclaredField("capacity");
            fail("Attribute capacity should not be overwritten but inherited from AuDHashTable! The constructor should call super(capacity).");

        } catch (NoSuchFieldException e) {
            AuDHashTableTestcaseHelper ht = new AuDHashTableTestcaseHelper(10);
            TestcaseHelper.checkAttributeExists(AuDHashTable.class, "capacity");
            assertEquals("The super constructor does not set the capacity correctly.", 10, ht.getCapacity());

            AuDClosedHashTable cht = new AuDClosedHashTable(10);
            assertEquals("The constructor should call super(capacity).", 10, _helperGetCapacity(cht));
        }
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "Konstruktor (Initialisierung)")
    public void secTest__AuDClosedHashTable__constructor_attributes() {
        AuDClosedHashTable cht = new AuDClosedHashTable(10);
        int counter = TestcaseHelper.getValueOfPrivateField(cht, "counter");
        Contact[] table = TestcaseHelper.getValueOfPrivateField(cht, "table");
        boolean[] deleted = TestcaseHelper.getValueOfPrivateField(cht, "deleted");
        assertEquals(counter, 0);
        assertEquals(10, table.length);
        assertEquals(10, deleted.length);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "isFull")
    public void secTest__AuDClosedHashTable__isFull() throws Exception {
        AuDClosedHashTable cht = new AuDClosedHashTable(10);
        TestcaseHelper.setValueOfPrivateSuperclassField(cht, "capacity", 10);
        TestcaseHelper.setValueOfPrivateField(cht, "counter", (int) 10);
        assertTrue("isFull returns false when it should actually return true", cht.isFull());
        TestcaseHelper.setValueOfPrivateField(cht, "counter", (int) 9);
        assertFalse("isFull returns true when it should actually return false", cht.isFull());
        TestcaseHelper.setValueOfPrivateField(cht, "counter", (int) 12);
        assertFalse("isFull returns true when it should actually return false", cht.isFull());
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 1.5, comment = "hash")
    public void secTest__AuDClosedHashTable__hash() throws Exception {
        String s1 = "";
        String s2 = "bbbbbblassssssssssadkhrtg";
        String s3 = "Was ist die Hashkodierung dieses Texts?";
        AuDClosedHashTableTestcaseHelper ch = new AuDClosedHashTableTestcaseHelper(10);
        AuDClosedHashTable h = new AuDClosedHashTable(10);


        Method hash = AuDClosedHashTable.class.getDeclaredMethod("hash", String.class, int.class);
        hash.setAccessible(true);


        for (int i = 0; i < 10; i++) {
            assertEquals("The hash value was not calculated correctly.", ch.hash(s1, i), hash.invoke(h, s1, i));
            assertEquals("The hash value was not calculated correctly.", ch.hash(s2, i), hash.invoke(h, s2, i));
            assertEquals("The hash value was not calculated correctly.", ch.hash(s3, i), hash.invoke(h, s3, i));
        }
    }

    @Test(timeout = 500, expected = UnsupportedOperationException.class)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "insert (Abbruch)")
    public void secTest__AuDClosedHashTable__insert_full() throws Exception {
        AuDClosedHashTable h = new AuDClosedHashTable(7);
        TestcaseHelper.setValueOfPrivateSuperclassField(h, "capacity", 7);
        TestcaseHelper.setValueOfPrivateField(h, "counter", 7);
        h.insert(new Contact("test@test.test"));
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "insert (counter)")
    public void secTest__AuDClosedHashTable__insert_counter() throws Exception {
        AuDClosedHashTable h = new AuDClosedHashTable(10);
        TestcaseHelper.setValueOfPrivateSuperclassField(h, "capacity", 10);
        for (int i = 0; i < 10; i++) {
            assertEquals("Counter is not set correctly.", i, (int) TestcaseHelper.getValueOfPrivateField(h, "counter"));
            h.insert(new Contact("test@test.test" + i));
        }
    }

    @Test(timeout = 1500)
    @Points(exID = "AuDClosedHashTable", bonus = 1.0, comment = "insert")
    public void secTest__AuDClosedHashTable__insert() throws Exception {

        AuDClosedHashTableTestcaseHelper ch = new AuDClosedHashTableTestcaseHelper(8);
        AuDClosedHashTableTestcaseHelper h = new AuDClosedHashTableTestcaseHelper(8);

        for (int i = 0; i < 4; i++) {
            Contact c0 = new Contact("test@test.test" + i);
            Contact c1 = new Contact("tset@test.test" + i);
            ch.cleanInsert(c0);
            h.insert(c0);
            ch.cleanInsert(c1);
            h.insert(c1);
        }

        for (int i = 0; i < 4; i++) {
            assertEquals("Contacts don't get inserted at all.", ch.cleanGetContact("test@test.test" + i), h.cleanGetContact("test@test.test" + i));
            assertEquals("Contacts don't get inserted at all.", ch.cleanGetContact("tset@test.test" + i), h.cleanGetContact("tset@test.test" + i));
            assertEquals("The hash-function doesn't seem to be called correctly.", ch.cleanGetIndexOf("test@test.test" + i), h.cleanGetIndexOf("test@test.test" + i));
            assertEquals("The hash-function doesn't seem to be called correctly.", ch.cleanGetIndexOf("tset@test.test" + i), h.cleanGetIndexOf("tset@test.test" + i));
        }
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 2.5, comment = "getIndexOf")
    public void secTest__AuDClosedHashTable__getIndexOf() throws Throwable {
        AuDClosedHashTableTestcaseHelper ch = new AuDClosedHashTableTestcaseHelper(10);

        Contact c0 = new Contact("test@test.test0");
        Contact c1 = new Contact("test@test.test1");
        Contact c2 = new Contact("test@test.test2");

        ch.cleanInsert(c2);
        ch.cleanInsert(c0);
        ch.cleanInsert(c1);

        for (int i = 0; i < 3; i++) {
            Method getIndexOf = AuDClosedHashTable.class.getDeclaredMethod("getIndexOf", String.class);
            getIndexOf.setAccessible(true);
            try {
                assertEquals("getIndexOf returned the wrong value", ch.cleanGetIndexOf("test@test.test" + i), getIndexOf.invoke(ch, "test@test.test" + i));
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "getIndexOf (exception)")
    public void secTest__AuDClosedHashTable__getIndexOf_exception() throws Exception {
        boolean exception = false;
        boolean expectedException = false;

        AuDClosedHashTable cht = new AuDClosedHashTable(10);

        Method getIndexOf = AuDClosedHashTable.class.getDeclaredMethod("getIndexOf", String.class);
        getIndexOf.setAccessible(true);

        try {
            getIndexOf.invoke(cht, "test@test.test");
        } catch (InvocationTargetException inv) {
            exception = true;
            if (inv.getCause() instanceof NoSuchElementException) {
                expectedException = true;
            }
        } finally {
            assertTrue("No exception was thrown.", exception);
            assertTrue("Wrong exception was thrown (NoSuchElementException expected).", expectedException);
        }
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "remove")
    public void secTest__AuDClosedHashTable__remove() throws Throwable {
        AuDClosedHashTableTestcaseHelper ch = new AuDClosedHashTableTestcaseHelper(10);
        Contact cr = new Contact("test@test.test");
        ch.cleanInsert(cr);
        Contact[] table = TestcaseHelper.getValueOfPrivateSuperclassField(ch, "table");

        for (int i = 0; i < table.length; i++) {
            if (table[i] == null) {
                table[i] = new Contact("ftest@ftest.ftest");
            }
        }

        TestcaseHelper.setValueOfPrivateSuperclassField(ch, "table", table);

        Method getIndexOf = AuDClosedHashTable.class.getDeclaredMethod("getIndexOf", String.class);
        getIndexOf.setAccessible(true);
        int idx = 0;
        try {
            idx = (int) getIndexOf.invoke(ch, "test@test.test");
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }

        ch.remove(cr);
        table = TestcaseHelper.getValueOfPrivateSuperclassField(ch, "table");
        int removed = 0;

        for (Contact c : table) {
            if (c == null) {
                removed++;
            }
        }
        if (removed == 0) {
            fail("Contacts are not getting removed.");
        }
        if (removed > 1) {
            fail("Too many contacts are getting removed.");
        }
        assertNull("Wrong Contact is getting removed.", table[idx]);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "remove (counter)")
    public void secTest__AuDClosedHashTable__remove_counter() throws Exception {
        AuDClosedHashTableTestcaseHelper ch = new AuDClosedHashTableTestcaseHelper(10);
        for (int i = 0; i < 10; i++) {
            ch.cleanInsert(new Contact("test@test.test" + i));
        }
        for (int i = 9; i >= 0; i--) {
            if((int) TestcaseHelper.getValueOfPrivateSuperclassField(ch, "counter") != i + 1) {
                fail("The counter is not getting decremented correctly.");
            }
            ch.remove(new Contact("test@test.test" + i));
        }
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 0.5, comment = "remove (flag)")
    public void secTest__AuDClosedHashTable__remove_flag() throws Exception {

        AuDClosedHashTableTestcaseHelper ch = new AuDClosedHashTableTestcaseHelper(10);

        Contact cr = new Contact("test@test.test");
        ch.cleanInsert(cr);


        Contact[] table = TestcaseHelper.getValueOfPrivateSuperclassField(ch, "table");

        for (int i = 0; i < table.length; i++) {
            if (table[i] == null) {
                table[i] = new Contact("ftest@ftest.ftest");
            }
        }

        TestcaseHelper.setValueOfPrivateSuperclassField(ch, "table", table);

        Method getIndexOf = AuDClosedHashTable.class.getDeclaredMethod("getIndexOf", String.class);
        getIndexOf.setAccessible(true);

        ch.remove(cr);

        table = TestcaseHelper.getValueOfPrivateSuperclassField(ch, "table");
        boolean[] deleted = TestcaseHelper.getValueOfPrivateSuperclassField(ch, "deleted");

        for (int i = 0; i < table.length; i++) {
            if (table[i] == null && !deleted[i]) {
                fail("Flag is not set.");
            }
        }
    }

    @Test(timeout = 500)
    @Points(exID = "AuDClosedHashTable", bonus = 1.0, comment = "getContact")
    public void secTest__AuDClosedHashTable__getContact() throws Throwable {

        AuDClosedHashTableTestcaseHelper ch = new AuDClosedHashTableTestcaseHelper(10);

        Contact c1 = new Contact("test1@test.test");
        Contact c2 = new Contact("test2@test.test");
        Contact c3 = new Contact("test3@test.test");

        ch.cleanInsert(c1);
        ch.cleanInsert(c2);
        ch.cleanInsert(c3);

        Method getIndexOf = AuDClosedHashTable.class.getDeclaredMethod("getIndexOf", String.class);
        getIndexOf.setAccessible(true);

        Contact[] table = TestcaseHelper.getValueOfPrivateSuperclassField(ch, "table");

        try {
            assertEquals("Wrong contact was returned.", table[(int) getIndexOf.invoke(ch, "test2@test.test")], ch.getContact("test2@test.test"));
            assertEquals("Wrong contact was returned.", table[(int) getIndexOf.invoke(ch, "test1@test.test")], ch.getContact("test1@test.test"));
            assertEquals("Wrong contact was returned.", table[(int) getIndexOf.invoke(ch, "test3@test.test")], ch.getContact("test3@test.test"));
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    // ============ AuDOpenHashTable ==========
    @Test(timeout = 500)
    @Points(exID = "AuDOpenHashTable", bonus = 0.5, comment = "Signatur")
    public void secTest__AuDOpenHashTable__signature() {
        TestcaseHelper.checkClassSignature(AuDOpenHashTable.class, Modifier.PUBLIC);
        TestcaseHelper.checkSuperclass(AuDOpenHashTable.class, AuDHashTable.class);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDOpenHashTable", bonus = 0.5, comment = "table")
    public void secTest__AuDOpenHashTable__attributes_table() {
        TestcaseHelper.checkAttributeExists(AuDOpenHashTable.class, "table");
        TestcaseHelper.checkAttributes(AuDOpenHashTable.class, new String[]{"table"}, new Class<?>[]{LinkedList[].class}, new int[]{Modifier.PRIVATE});
    }

    @Test(timeout = 500)
    @Points(exID = "AuDOpenHashTable", bonus = 0.5, comment = "Konstruktor (super)")
    public void secTest__AuDOpenHashTable__constructor_super() {

        try {
            AuDOpenHashTable.class.getDeclaredField("capacity");
            fail("Attribute capacity should not be overwritten but inherited from AuDHashTable! The constructor should call super(capacity).");

        } catch (NoSuchFieldException e) {
            AuDHashTableTestcaseHelper ht = new AuDHashTableTestcaseHelper(10);
            TestcaseHelper.checkAttributeExists(AuDHashTable.class, "capacity");
            assertEquals("The super constructor does not set the capacity correctly.", 10, ht.getCapacity());

            AuDOpenHashTable ch = new AuDOpenHashTable(10);
            assertEquals("The constructor should call super(capacity).", 10, _helperGetCapacity(ch));
        }
    }

    @Test(timeout = 500)
    @Points(exID = "AuDOpenHashTable", bonus = 0.5, comment = "Konstruktor (table create)")
    public void secTest__AuDOpenHashTable__constructor_attributes() {
        AuDOpenHashTable oht = new AuDOpenHashTable(10);
        LinkedList<Contact>[] table = TestcaseHelper.getValueOfPrivateField(oht, "table");
        assertEquals("", 10, table.length);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDOpenHashTable", bonus = 0.5, comment = "Konstruktor (table init)")
    public void secTest__AuDOpenHashTable__constructor_initializes_table() throws Exception {
        AuDOpenHashTable h = new AuDOpenHashTable(10);
        TestcaseHelper.setValueOfPrivateSuperclassField(h, "capacity", 10);
        LinkedList<Contact>[] table = TestcaseHelper.getValueOfPrivateField(h, "table");
        for (LinkedList<Contact> l : table) {
            assertNotNull("Table is not initialized correctly.", l);
        }
    }

    @Test(timeout = 1500)
    @Points(exID = "AuDOpenHashTable", bonus = 1.0, comment = "insert")
    public void secTest__AuDOpenHashTable__insert() throws Exception {

        AuDOpenHashTableTestcaseHelper ch = new AuDOpenHashTableTestcaseHelper(10);

        Contact c0 = new Contact("test@test.test");
        Contact c1 = new Contact("tset@test.test");
        Contact c2 = new Contact("tset@tesa.test");
        Contact c3 = new Contact("tset@tesb.test");

        ch.insert(c0);
        ch.insert(c1);
        ch.insert(c2);
        ch.insert(c3);

        LinkedList<Contact>[] table = TestcaseHelper.getValueOfPrivateSuperclassField(ch, "table");

        assertEquals("The element was not inserted correctly.", table[ch.hash("test@test.test")].get(0), c0);
        assertEquals("The element was not inserted correctly.", table[ch.hash("test@test.test")].get(1), c1);
        assertEquals("The element was not inserted correctly.", table[ch.hash("test@tesa.test")].get(0), c2);
        assertEquals("The element was not inserted correctly.", table[ch.hash("test@tesb.test")].get(0), c3);

    }


    @Test(timeout = 500)
    @Points(exID = "AuDOpenHashTable", bonus = 1.0, comment = "remove (find and delete)")
    public void secTest__AuDOpenHashTable__remove_find_in_list() throws Exception {

        AuDOpenHashTableTestcaseHelper ch = new AuDOpenHashTableTestcaseHelper(7);

        Contact c0 = new Contact("test@test.test");
        Contact c1 = new Contact("tset@test.test");
        Contact c2 = new Contact("test@test.test5");
        Contact c3 = new Contact("tset@test.test5");
        Contact c4 = new Contact("ttes@test.test5");
        Contact c5 = new Contact("tets@test.test5");

        ch.cleanInsert(c0);
        ch.cleanInsert(c1);
        ch.cleanInsert(c2);
        ch.cleanInsert(c3);
        ch.cleanInsert(c4);
        ch.cleanInsert(c5);

        ch.remove(c1);
        ch.remove(c2);
        ch.remove(c4);

        LinkedList<Contact>[] table = TestcaseHelper.getValueOfPrivateSuperclassField(ch, "table");

        assertEquals("Wrong Contacts are getting removed.", c0, table[ch.hash("test@test.test")].get(0));
        assertEquals("Wrong Contacts are getting removed.", c3, table[ch.hash("test@test.test5")].get(0));
        assertEquals("Wrong Contacts are getting removed.", c5, table[ch.hash("tset@test.test5")].get(1));
    }

    @Test(timeout = 500)
    @Points(exID = "AuDOpenHashTable", bonus = 0.5, comment = "remove (hashing)")
    public void secTest__AuDOpenHashTable__remove_hashing() throws Exception {

        AuDOpenHashTableTestcaseHelper ch = new AuDOpenHashTableTestcaseHelper(7);

        Contact c0 = new Contact("test@test.test");
        Contact c1 = new Contact("tset@test.test");
        Contact c2 = new Contact("test@test.test5");
        Contact c3 = new Contact("tset@test.test5");
        Contact c4 = new Contact("test@test.test7");

        ch.cleanInsert(c0);
        ch.cleanInsert(c1);
        ch.cleanInsert(c2);
        ch.cleanInsert(c3);
        ch.cleanInsert(c4);


        ch.remove(c1);
        ch.remove(c2);
        ch.remove(c3);
        ch.remove(c4);

        LinkedList<Contact>[] table = TestcaseHelper.getValueOfPrivateSuperclassField(ch, "table");

        assertEquals("Wrong Contacts are getting removed.", 1, table[ch.hash("test@test.test")].size());
        assertEquals("Wrong Contacts are getting removed.", 0, table[ch.hash("test@test.test5")].size());
        assertEquals("Wrong Contacts are getting removed.", 0, table[ch.hash("test@test.test7")].size());
    }

    @Test(timeout = 500, expected = NoSuchElementException.class)
    @Points(exID = "AuDOpenHashTable", bonus = 0.5, comment = "remove (exception)")
    public void secTest__AuDOpenHashTable__remove_exception() throws Exception {

        AuDOpenHashTableTestcaseHelper oht = new AuDOpenHashTableTestcaseHelper(7);

        Contact c0 = new Contact("test@test.test");
        Contact c1 = new Contact("tset@test.test");
        Contact c2 = new Contact("test@test.test5");
        Contact c3 = new Contact("tset@test.test5");
        Contact c4 = new Contact("test@test.test7");

        oht.cleanInsert(c0);
        oht.cleanInsert(c1);
        oht.cleanInsert(c2);
        oht.cleanInsert(c3);

        oht.remove(c4);
    }

    @Test(timeout = 500)
    @Points(exID = "AuDOpenHashTable", bonus = 1.5, comment = "getContact")
    public void secTest__AuDOpenHashTable__getContact() throws Exception {
        AuDOpenHashTableTestcaseHelper ch = new AuDOpenHashTableTestcaseHelper(7);
        AuDOpenHashTable h = new AuDOpenHashTable(7);

        Contact c0 = new Contact("test@test.test");
        Contact c2 = new Contact("test@test.test5");
        Contact c3 = new Contact("tset@test.test5");
        Contact c4 = new Contact("test@test.test7");

        ch.cleanInsert(c0);
        ch.cleanInsert(c2);
        ch.cleanInsert(c3);
        ch.cleanInsert(c4);

        assertEquals("Returns wrong Contact.", c0, ch.getContact("test@test.test"));
        assertEquals("Returns wrong Contact.", c2, ch.getContact("test@test.test5"));
        assertEquals("Returns wrong Contact.", c3, ch.getContact("tset@test.test5"));
        assertEquals("Returns wrong Contact.", c4, ch.getContact("test@test.test7"));
    }

    @Test(timeout = 500, expected = NoSuchElementException.class)
    @Points(exID = "AuDOpenHashTable", bonus = 0.5, comment = "getContact (exception)")
    public void secTest__AuDOpenHashTable__getContact_exception() throws Exception {
        AuDOpenHashTableTestcaseHelper ch = new AuDOpenHashTableTestcaseHelper(7);

        Contact c0 = new Contact("test@test.test");
        Contact c1 = new Contact("tset@test.test");
        Contact c2 = new Contact("test@test.test5");
        Contact c3 = new Contact("tset@test.test5");
        Contact c4 = new Contact("test@test.test7");

        ch.cleanInsert(c0);
        ch.cleanInsert(c1);
        ch.cleanInsert(c2);
        ch.cleanInsert(c3);

        ch.getContact("test@test.test7");
    }

    // ============ ContactDatabase ==========
    @Test(timeout = 500)
    @Points(exID = "ContactDatabaseTests", bonus = 0.5, comment = "Signatur")
    public void secTest__ContactDatabase__signature() {
        fail("Wird noch manuell korrigiert (max. 0.5 Punkte)");
    }

    @Test(timeout = 500)
    @Points(exID = "ContactDatabaseTests", bonus = 4.0, comment = "Tests in main-Methode")
    public void secTest__ContactDatabase__main() {
        fail("Wird noch manuell korrigiert (max. 4 Punkte)");
    }
}


