import tester.annotations.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SecretClass
public class SortedSetSecretTest {

    // =============== SYSTEM ===============
//    @Rule
//    public final PointsLogger pointsLogger = new PointsLogger();
//    @ClassRule
//    public final static PointsSummary pointsSummary = new PointsSummary();

    // ========= TEST DATA =========
    private static final Random RND = new Random(4711_0815_666L);

    // ============ TESTS ==========
    @Test(timeout = 500)
    @Points(exID = "0. Coderichtlinien", bonus = 2, comment = "Wird noch manuell korrigiert (2 Punkte)")
    public void secTest__checkCodeStyle() {
        fail();
    }

    // ============ ElementExistsException ============

    @Test(timeout = 500)
    @Points(exID = "ElementExistsException", bonus = 0.5, comment = "Konstruktor/Signatur")
    public void secTest__ElementExistsException_constructor_signature() {
        TestcaseHelper.checkSuperclass(ElementExistsException.class, RuntimeException.class);

        String[] names = {"SortedSetTests.ElementExistsException()"};
        int[] modifiers = {Modifier.PUBLIC};
        TestcaseHelper.checkConstructors(ElementExistsException.class, names, modifiers);
    }

    @Test(timeout = 500)
    @Points(exID = "ElementExistsException", bonus = 0.5, comment = "Konstruktor (String msg)")
    public void secTest__ElementExistsException_constructor_string() {
        String[] names = {"SortedSetTests.ElementExistsException(java.lang.String)"};
        int[] modifiers = {Modifier.PUBLIC};
        TestcaseHelper.checkConstructors(ElementExistsException.class, names, modifiers);
    }

    @Test(timeout = 500)
    @Points(exID = "ElementExistsException", bonus = 1.0, comment = "Konstruktor super() Aufruf (wird manuell korrigiert, 1 Punkt)")
    public void secTest__ElementExistsException_constructors_super() {
        fail("Wird noch manuell korrigiert (1 Punkt)");
    }

    // ============ SortedSet (insgesamt ML 32 Punkte + 2 Punkte Coderichtlinien) ============

    // ============ ListItem ============ (5 Punkte)

//    @Test(timeout = 500)
//    @Points(exID = "SortedSet", bonus = 0.5, comment = "ListItem Signatur")
//    public void secTest__SortedSet_ListItem_signature() {
//        Class<?> n = TestcaseHelper.getInnerClass("SortedSet$ListItem");
//        TestcaseHelper.checkClassSignature(n, Modifier.PRIVATE);
//    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "ListItem Attribut value")
    public void secTest__SortedSet_ListItem_attribute_value() {
        Class<?> n = TestcaseHelper.getInnerClass("SortedSetTests.SortedSet$ListItem");
        // Don't check for attribute modifier since it's not specified in the task
        TestcaseHelper.checkAttributeType(n, "value", int.class);
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "ListItem Attribut next")
    public void secTest__SortedSet_ListItem_attribute_next() {
        Class<?> n = TestcaseHelper.getInnerClass("SortedSetTests.SortedSet$ListItem");
        // Don't check for attribute modifier since it's not specified in the task
        TestcaseHelper.checkAttributeType(n, "next", n);
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "ListItem Attribute previous")
    public void secTest__SortedSet_ListItem_attribute_previous() {
        Class<?> n = TestcaseHelper.getInnerClass("SortedSetTests.SortedSet$ListItem");
        // Don't check for attribute modifier since it's not specified in the task
        TestcaseHelper.checkAttributeType(n, "previous", n);
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "ListItem Konstruktor")
    public void secTest__SortedSet_ListItem_constructor() {
        Class<?> n = TestcaseHelper.getInnerClass("SortedSetTests.SortedSet$ListItem");
        TestcaseHelper.checkConstructors(n, new String[]{"SortedSetTests.SortedSet$ListItem(SortedSetTests.SortedSet, int)"},
                new int[]{Modifier.PUBLIC});
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "ListItem toString")
    public void secTest__SortedSet_ListItem_toString() throws Exception {
        Class<?> n = TestcaseHelper.getInnerClass("SortedSetTests.SortedSet$ListItem");
        Method mToString = n.getMethod("toString");
        Object o0 = createListItemInstance(5);
        Object o1 = createListItemInstance(7);
        TestcaseHelper.assertEquals("[5]", (String) mToString.invoke(o0), "toString() failed for next == null!");
        TestcaseHelper.assertEquals("[7]", (String) mToString.invoke(o1), "toString() failed for next == null!");

        TestcaseHelper.setValueOfPrivateField(o0, "next", o1);
        TestcaseHelper.assertEquals("[5] --> ", (String) mToString.invoke(o0),
                "toString() failed for next != null!");
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 2, comment = "ListItem equals")
    public void secTest__SortedSet_ListItem_equals() throws Exception {
        Class<?> n = TestcaseHelper.getInnerClass("SortedSetTests.SortedSet$ListItem");
        Method mEquals = n.getMethod("equals", Object.class);

        Object o0 = createListItemInstance(5);
        Object o1 = createListItemInstance(5);

        TestcaseHelper.assertFalse((Boolean) mEquals.invoke(o0, new Object()), "equals() did not call instanceof!");
        TestcaseHelper.assertTrue((Boolean) mEquals.invoke(o0, o1), "equals() failed for values 5, 5!");

        o1 = createListItemInstance(6);
        TestcaseHelper.assertFalse((Boolean) mEquals.invoke(o0, o1), "equals() failed for values 5, 6!");

        o0 = createListItemInstance(6);
        TestcaseHelper.assertTrue((Boolean) mEquals.invoke(o0, o1), "equals() failed for values 6, 6!");
    }

    // ============ ListItem Ende ============

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "Attribut head")
    public void secTest__SortedSet_attribute_head() {
        Class n = TestcaseHelper.getInnerClass("SortedSetTests.SortedSet$ListItem");
        SortedSet s = new SortedSet();
        TestcaseHelper.checkAttributeType(SortedSet.class, "head", n);
        TestcaseHelper.checkAttributeModifiers(SortedSet.class, "head", Modifier.PRIVATE);
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "Attribut tail")
    public void secTest__SortedSet_attribute_tail() {
        Class n = TestcaseHelper.getInnerClass("SortedSetTests.SortedSet$ListItem");
        SortedSet s = new SortedSet();
        TestcaseHelper.checkAttributeType(SortedSet.class, "tail", n);
        TestcaseHelper.checkAttributeModifiers(SortedSet.class, "tail", Modifier.PRIVATE);
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "add")
    public void secTest__SortedSet_add_contains_exception() {
        SortedSet sortedSet = new SortedSet();

        sortedSet.add(10);
        try {
            sortedSet.add(10);
            fail("No Exception was thrown when adding the same value twice!");
        } catch (ElementExistsException e) {
            // do nothing
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "add empty list")
    public void secTest__SortedSet_add_empty_list() {
        SortedSet sortedSet = new SortedSet();

        sortedSet.add(1);

        Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
        Object tail = TestcaseHelper.getValueOfPrivateField(sortedSet, "tail");
        int valueHead = getListItemValue(head);
        int valueTail = getListItemValue(tail);

        TestcaseHelper.assertEquals(1, valueHead, "add() failed - head has wrong value!");
        TestcaseHelper.assertEquals(1, valueTail, "add() failed - tail has wrong value!");
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "add tail")
    public void secTest__SortedSet_add_tail() {
        SortedSet sortedSet = new SortedSet();

        Object head;
        Object tail;
        int valueHead;
        int valueTail;
        for (int i = 0; i < 10; i++) {
            sortedSet.add(i);
            head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
            tail = TestcaseHelper.getValueOfPrivateField(sortedSet, "tail");
            valueHead = getListItemValue(head);
            valueTail = getListItemValue(tail);
            TestcaseHelper.assertEquals(0, valueHead, "add() failed - head has wrong value!");
            TestcaseHelper.assertEquals(i, valueTail, "add() failed - tail has wrong value!");
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 2, comment = "add regular/random")
    public void secTest__SortedSet_add() {
        SortedSetRef sortedSetRef = new SortedSetRef();
        SortedSet sortedSet = new SortedSet();

        int valueHeadRef;
        int valueHead;
        int valueTailRef;
        int valueTail;

        int[] vals = createRandomArray(20);

        for (int i : vals) {
            sortedSet.add(i);
            sortedSetRef.add(i);
            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");

            valueHeadRef = getListItemValue(headRef);
            valueHead = getListItemValue(head);
            valueTailRef = getListItemValue(TestcaseHelper.getValueOfPrivateField(sortedSetRef, "tail"));
            valueTail = getListItemValue(TestcaseHelper.getValueOfPrivateField(sortedSet, "tail"));

            TestcaseHelper.assertEquals(valueHeadRef, valueHead, "add() failed - head has wrong value!");
            TestcaseHelper.assertEquals(valueTailRef, valueTail, "add() failed - tail has wrong value!");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                TestcaseHelper.assertEquals(nextRef, next,
                        "add() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: " + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "add []")
    public void secTest__SortedSet_add_array() {

        SortedSetRef sortedSetRef = new SortedSetRef();
        SortedSet sortedSet = new SortedSet();
        int[] values = createRandomArray(20);

        sortedSetRef.add(values);
        sortedSet.add(values);

        Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
        Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
        while (headRef != null) {
            Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
            Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
            TestcaseHelper.assertEquals(getListItemValue(headRef), getListItemValue(head),
                    "add() failed - order of values wrong!\nExpected: " + headRef + "\nActual: " + toStringListItemRef(head));
            headRef = nextRef;
            head = next;
        }

        for (int value : values) {
            try {
                sortedSet.add(value);
                fail("adding already existing value does not throw Exception!");
            } catch (ElementExistsException e) {

            } catch (Exception e) {
                fail("add() throws wrong Exception!");
            }
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "contains")
    public void secTest__SortedSet_contains() throws Exception {
        SortedSet sortedSet = new SortedSet();
        TestcaseHelper.assertEquals(false, sortedSet.contains(1), "contains did not return false even though list is empty");
        for (int i = 0; i < 10; i++) {
            addRef(sortedSet, i);
            TestcaseHelper.assertTrue(sortedSet.contains(i), "contains() failed - value " + i + " should be in Set, so contains should return True!");
        }

        TestcaseHelper.assertFalse(sortedSet.contains(-1), "contains() failed - value -1 should not be in Set, so contains should return False!");
        TestcaseHelper.assertFalse(sortedSet.contains(10), "contains() failed - value 10 should not be in Set, so contains should return False!");
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "size")
    public void secTest__SortedSet_size() throws Exception {
        SortedSetRef sortedSetRef = new SortedSetRef();
        SortedSet sortedSet = new SortedSet();
        int[] values = createRandomArray(20);

        TestcaseHelper.assertEquals(sortedSetRef.size(), sortedSet.size(), "size mismatch when list is empty!");
        for (int i = 0; i < values.length; i++) {
            addRef(sortedSet, values[i]);
            sortedSetRef.add(values[i]);
            TestcaseHelper.assertEquals(sortedSetRef.size(), sortedSet.size(), "size mismatch!");
        }

        for (int i = 0; i < values.length; i++) {
            removeRef(sortedSet, values[i]);
            sortedSetRef.remove(values[i]);
            TestcaseHelper.assertEquals(sortedSetRef.size(), sortedSet.size(), "size mismatch!");
        }
    }


    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "remove Exception")
    public void secTest__SortedSet_removeException() throws Exception {

        SortedSet sortedSet = new SortedSet();

        for (int i = 0; i < 10; i++) {
            addRef(sortedSet, i);
        }

        // remove head twice
        sortedSet.remove(0);
        try {
            sortedSet.remove(0);
            fail("Removing head twice does not throw Exception!");
        } catch (NoSuchElementException e) {

        } catch (Exception e) {
            fail("Removing head twice throws wrong Exception!");
        }

        // remove tail twice
        sortedSet.remove(9);
        try {
            sortedSet.remove(9);
            fail("Removing tail twice does not throw Exception!");
        } catch (NoSuchElementException e) {

        } catch (Exception e) {
            fail("Removing tail twice throws wrong Exception!");
        }

        // remove middle twice
        sortedSet.remove(3);
        try {
            sortedSet.remove(3);
            fail("Removing middle element twice does not throw Exception!");
        } catch (NoSuchElementException e) {

        } catch (Exception e) {
            fail("Removing middle element twice throws wrong Exception!");
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "remove head")
    public void secTest__SortedSet_removeHead() throws Exception {
        SortedSet sortedSet = new SortedSet();
        for (int i = 0; i < 10; i++) {
            addRef(sortedSet, i);
        }

        Object head;
        int valueHead;
        for (int i = 0; i < 10; i++) {
            sortedSet.remove(i);
            head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
            if (i == 9) {
                TestcaseHelper.assertNull(head, "remove() failed for last element - head is not null!");
            } else {
                valueHead = getListItemValue(head);
                TestcaseHelper.assertEquals(i + 1, valueHead, "remove() failed - head has wrong value!");
            }
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "remove tail")
    public void secTest__SortedSet_removeTail() throws Exception {
        SortedSet sortedSet = new SortedSet();
        for (int i = 0; i < 10; i++) {
            addRef(sortedSet, i);
        }

        Object tail;
        int valueTail;

        for (int i = 9; i >= 0; i--) {
            sortedSet.remove(i);
            tail = TestcaseHelper.getValueOfPrivateField(sortedSet, "tail");
            if (i == 0) {
                TestcaseHelper.assertNull(tail, "remove() failed for last element - tail is not null!");
            } else {
                valueTail = getListItemValue(tail);
                TestcaseHelper.assertEquals(i - 1, valueTail, "remove() failed - tail has wrong value!");
            }
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1.5, comment = "remove multiple random elements")
    public void secTest__SortedSet_removeRandom() throws Exception {
        // remove middle ascending
        {
            SortedSet sortedSet = new SortedSet();
            SortedSetRef sortedSetRef = new SortedSetRef();

            for (int i = 0; i < 10; i++) {
                addRef(sortedSet, i);
                sortedSetRef.add(i);
            }

            int valueHeadRef;
            int valueHead;
            int valueTailRef;
            int valueTail;

            for (int i = 1; i < 9; i++) {
                sortedSet.remove(i);
                sortedSetRef.remove(i);

                Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
                Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

                valueHeadRef = getListItemValue(headRef);
                valueHead = getListItemValue(head);
                valueTailRef = getListItemValue(TestcaseHelper.getValueOfPrivateField(sortedSetRef, "tail"));
                valueTail = getListItemValue(TestcaseHelper.getValueOfPrivateField(sortedSet, "tail"));

                TestcaseHelper.assertEquals(valueHeadRef, valueHead, "remove() failed - head has wrong value!");
                TestcaseHelper.assertEquals(valueTailRef, valueTail, "remove() failed - tail has wrong value!");

                while (headRef != null) {
                    Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                    Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                    if (nextRef == null) {
                        break;
                    }
                    TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                            "remove() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: "
                                    + toStringRef(sortedSet));
                    headRef = nextRef;
                    head = next;
                }
            }
        }

        // remove middle descending
        {
            SortedSet sortedSet = new SortedSet();
            SortedSetRef sortedSetRef = new SortedSetRef();

            for (int i = 0; i < 10; i++) {
                addRef(sortedSet, i);
                sortedSetRef.add(i);
            }

            for (int i = 8; i >= 1; i--) {

                sortedSet.remove(i);
                sortedSetRef.remove(i);


                Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
                Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

                while (headRef != null) {
                    Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                    Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                    if (nextRef == null) {
                        break;
                    }
                    TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                            "remove() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: "
                                    + toStringRef(sortedSet));
                    headRef = nextRef;
                    head = next;
                }
            }
        }

        // remove random
        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();
            int[] values = createRandomArray(20);

            sortedSetRef.add(values);
            addRefArray(sortedSet, values);

            for (int i = 0; i < values.length; i++) {
                sortedSetRef.remove(values[i]);
                sortedSet.remove(values[i]);

                Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
                Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
                if (i != values.length - 1) {
                    while (headRef != null) {
                        Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                        Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                        if (nextRef == null) {
                            break;
                        }
                        TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                                "remove() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: "
                                        + toStringRef(sortedSet));
                        headRef = nextRef;
                        head = next;
                    }
                } else {
                    TestcaseHelper.assertNull(head, "remove failed() - head not null after removing last item!");
                }
            }
        }

    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "remove one element")
    public void secTest__SortedSet_removeOneElement() throws Exception {

        SortedSet sortedSet = new SortedSet();
        addRef(sortedSet, 1);
        sortedSet.remove(1);

        Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
        Object tail = TestcaseHelper.getValueOfPrivateField(sortedSet, "tail");

        TestcaseHelper.assertEquals(0, sortedSet.size(), "remove() failed for single element - size not 0!");

        TestcaseHelper.assertNull(head, "remove() failed for single element - head should be null!");
        TestcaseHelper.assertNull(tail, "remove() failed for single element - tail should be null!");
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "clone")
    public void secTest__SortedSet_clone() throws Exception {
        SortedSet sortedSet = new SortedSet();
        int[] values = createRandomArray(20);

        addRefArray(sortedSet, values);
        SortedSet clonedSet = (SortedSet) sortedSet.clone();

        TestcaseHelper.assertFalse(sortedSet == clonedSet, "clone() failed - returned reference to same object!");

        Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
        Object headClone = TestcaseHelper.getValueOfPrivateField(clonedSet, "head");

        while (head != null) {
            Object nextClone = TestcaseHelper.getValueOfPrivateField(headClone, "next");
            Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
            TestcaseHelper.assertEquals(nextClone, next,
                    "clone() failed - order of values wrong!\nExpected: " + toStringRef(sortedSet) + "\nActual: " + toStringRef(clonedSet));
            headClone = nextClone;
            head = next;
        }

    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 2, comment = "intersect")
    public void secTest__SortedSet_intersect() throws Exception {
        // intersect all
        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();

            for (int i = 0; i < 20; i++) {
                sortedSetRef.add(i);
                addRef(sortedSet, i);
            }

            sortedSetRef = (SortedSetRef) sortedSetRef.intersect(sortedSetRef);
            sortedSet = (SortedSet) sortedSet.intersect(sortedSet);

            TestcaseHelper.assertEquals(sortedSetRef.size(), sizeRef(sortedSet), "intersect() failed  - size mismatch!");

            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                if (nextRef == null) {
                    break;
                }
                TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                        "intersect() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: "
                                + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }

        // intersect lower
        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSetRef intersectSetRef = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();
            SortedSet intersectSet = new SortedSet();

            for (int i = 0; i < 20; i++) {
                sortedSetRef.add(i);
                addRef(sortedSet, i);
                if (i <= 5) {
                    intersectSetRef.add(i);
                    addRef(intersectSet, i);
                }
            }

            intersectSetRef.add(new int[]{-3, -2, -1});
            addRefArray(intersectSet, new int[]{-3, -2, -1});

            sortedSetRef = (SortedSetRef) sortedSetRef.intersect(intersectSetRef);
            sortedSet = (SortedSet) sortedSet.intersect(intersectSet);

            TestcaseHelper.assertEquals(sortedSetRef.size(), sizeRef(sortedSet), "intersect() failed  - size mismatch!");

            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                if (nextRef == null) {
                    break;
                }
                TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                        "intersect() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: "
                                + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }

        // intersect upper
        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSetRef intersectSetRef = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();
            SortedSet intersectSet = new SortedSet();

            for (int i = 0; i < 20; i++) {
                sortedSetRef.add(i);
                addRef(sortedSet, i);
                if (i >= 15) {
                    intersectSetRef.add(i);
                    addRef(intersectSet, i);
                }
            }

            intersectSetRef.add(new int[]{20, 21, 22});
            addRefArray(intersectSet, new int[]{20, 21, 22});

            sortedSetRef = (SortedSetRef) sortedSetRef.intersect(intersectSetRef);
            sortedSet = (SortedSet) sortedSet.intersect(intersectSet);

            TestcaseHelper.assertEquals(sortedSetRef.size(), sizeRef(sortedSet), "intersect() failed  - size mismatch!");

            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                if (nextRef == null) {
                    break;
                }
                TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                        "intersect() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: "
                                + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }

        // intersect middle
        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSetRef intersectSetRef = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();
            SortedSet intersectSet = new SortedSet();

            for (int i = 0; i < 20; i++) {
                sortedSetRef.add(i);
                addRef(sortedSet, i);
                if (i >= 5 && i <= 15) {
                    intersectSetRef.add(i);
                    addRef(intersectSet, i);
                }
            }

            sortedSetRef = (SortedSetRef) sortedSetRef.intersect(intersectSetRef);
            sortedSet = (SortedSet) sortedSet.intersect(intersectSet);

            TestcaseHelper.assertEquals(sortedSetRef.size(), sizeRef(sortedSet), "intersect() failed  - size mismatch!");

            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                if (nextRef == null) {
                    break;
                }
                TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                        "intersect() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: "
                                + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "unite")
    public void secTest__SortedSet_unite() throws Exception {
        // unite same
        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();

            for (int i = 0; i < 20; i++) {
                sortedSetRef.add(i);
                addRef(sortedSet, i);
            }

            try {
                sortedSet = (SortedSet) sortedSet.unite(sortedSet);
                // fail("unite() does not throw exception for adding duplicate
                // elements!");
            } catch (ElementExistsException e) {

            } catch (Exception e) {
                fail("unite() throws wrong Exception!");
            }
        }
        // unite regular
        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSetRef sortedSetRef2 = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();
            SortedSet sortedSet2 = new SortedSet();

            for (int i = 0; i < 10; i++) {
                sortedSetRef.add(i);
                addRef(sortedSet, i);
            }

            for (int i = 10; i < 20; i++) {
                sortedSetRef2.add(i);
                addRef(sortedSet2, i);
            }

            sortedSet = (SortedSet) sortedSet.unite(sortedSet2);
            sortedSetRef = (SortedSetRef) sortedSetRef.unite(sortedSetRef2);

            TestcaseHelper.assertEquals(sortedSetRef.size(), sizeRef(sortedSet), "unite() failed  - size mismatch!");

            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                if (nextRef == null) {
                    break;
                }
                TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                        "unite() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: " + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }

    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "subtract same")
    public void secTest__SortedSet_subtract_same() throws Exception {
        {
            SortedSet sortedSet = new SortedSet();

            for (int i = 0; i < 20; i++) {
                addRef(sortedSet, i);
            }

            sortedSet = (SortedSet) sortedSet.subtract(sortedSet);

            TestcaseHelper.assertEquals(0, sizeRef(sortedSet), "subtract() failed for all elements - size not 0!");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
            TestcaseHelper.assertNull(head, "subtract() failed for all elements - head not null!");
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "subtract regular")
    public void secTest__SortedSet_subtract_regular() throws Exception {

        //subtract regular
        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSetRef sortedSetRef2 = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();
            SortedSet sortedSet2 = new SortedSet();

            for (int i = 0; i < 20; i++) {
                sortedSetRef.add(i);
                addRef(sortedSet, i);
            }

            for (int i = 10; i < 20; i++) {
                sortedSetRef2.add(i);
                addRef(sortedSet2, i);
            }

            sortedSetRef = (SortedSetRef) sortedSetRef.subtract(sortedSetRef2);
            sortedSet = (SortedSet) sortedSet.subtract(sortedSet2);

            TestcaseHelper.assertEquals(sortedSetRef.size(), sizeRef(sortedSet), "subtract() failed  - size mismatch!");
            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                if (nextRef == null) {
                    break;
                }
                TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                        "subtract() failed - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: " + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "subtract is not commutative")
    public void secTest__SortedSet_subtract_not_commutative() throws Exception {
        {
            SortedSet sortedSet = new SortedSet();
            SortedSet clonedSet = new SortedSet();
            SortedSet sortedSet2 = new SortedSet();

            for (int i = 0; i < 20; i++) {
                addRef(sortedSet, i);
                addRef(clonedSet, i);
            }

            for (int i = 10; i < 20; i++) {
                addRef(sortedSet2, i);
            }

            clonedSet = (SortedSet) sortedSet.subtract(sortedSet2);
            sortedSet = (SortedSet) sortedSet2.subtract(sortedSet);

            TestcaseHelper.assertNotEquals(clonedSet.size(), sizeRef(sortedSet), "subtract() failed  - size mismatch!");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
            Object headClone = TestcaseHelper.getValueOfPrivateField(clonedSet, "head");

            while (head != null) {
                TestcaseHelper.assertNotEquals(getListItemValue(head), getListItemValue(headClone),
                        "subtract() failed - heads should not be the same!\nExpected: " + toStringRef(sortedSet) + "\nActual: "
                                + toStringRef(clonedSet));
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                Object nextClone = TestcaseHelper.getValueOfPrivateField(headClone, "next");
                head = next;
                headClone = nextClone;
            }
        }

    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 2, comment = "getSetInBetween")
    public void secTest__SortedSet_getSetInBetween() throws Exception {
        int[] vals = {1, 3, 5, 7, 9, 11, 13, 15};

        // getSetInBetween for existing values

        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();

            sortedSetRef.add(vals);
            addRefArray(sortedSet, vals);

            sortedSetRef = (SortedSetRef) sortedSetRef.getSetInBetween(3, 9);
            sortedSet = (SortedSet) sortedSet.getSetInBetween(3, 9);

            TestcaseHelper.assertEquals(sortedSetRef.size(), sizeRef(sortedSet), "getSetInBetween() failed for existing values - size mismatch!");

            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                if (nextRef == null) {
                    break;
                }
                TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                        "getSetInBetween() failed for existing values - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: "
                                + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }

        // getSetInBetween for not existing values
        {
            SortedSetRef sortedSetRef = new SortedSetRef();
            SortedSet sortedSet = new SortedSet();

            sortedSetRef.add(vals);
            addRefArray(sortedSet, vals);

            sortedSetRef = (SortedSetRef) sortedSetRef.getSetInBetween(2, 8);
            sortedSet = (SortedSet) sortedSet.getSetInBetween(2, 8);

            TestcaseHelper.assertEquals(sortedSetRef.size(), sizeRef(sortedSet), "getSetInBetween() failed for not existing values - size mismatch!");

            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetRef, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                if (nextRef == null) {
                    break;
                }
                TestcaseHelper.assertEquals(getListItemValue(nextRef), getListItemValue(next),
                        "getSetInBetween() failed for not existing values - order of values wrong!\nExpected: " + sortedSetRef + "\nActual: "
                                + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }

        // getSetInBetween for existing and not existing values
        {

            SortedSet sortedSet = new SortedSet();
            addRefArray(sortedSet, vals);

            SortedSet sortedSetOther = (SortedSet) sortedSet.getSetInBetween(2, 10);
            sortedSet = (SortedSet) sortedSet.getSetInBetween(3, 9);

            TestcaseHelper.assertEquals(sizeRef(sortedSetOther), sizeRef(sortedSet),
                    "getSetInBetween() failed for existing and not existing values - size mismatch!");

            Object headRef = TestcaseHelper.getValueOfPrivateField(sortedSetOther, "head");
            Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            while (headRef != null) {
                Object nextRef = TestcaseHelper.getValueOfPrivateField(headRef, "next");
                Object next = TestcaseHelper.getValueOfPrivateField(head, "next");
                TestcaseHelper.assertEquals(nextRef, next, "getSetInBetween() failed for existing and not existing values - order of values wrong!\nExpected: "
                        + toStringRef(sortedSetOther) + "\nActual: " + toStringRef(sortedSet));
                headRef = nextRef;
                head = next;
            }
        }

        // getSetInBetween inverse
        {
            SortedSet sortedSet = new SortedSet();
            addRefArray(sortedSet, vals);
            sortedSet = (SortedSet) sortedSet.getSetInBetween(9, 3);
            TestcaseHelper.assertEquals(0, sizeRef(sortedSet), "getSetInBetween() failed - size not 0 for inverse values (for example from 9 to 3)!");
        }

        // getSetInBetween out of bounds
        {
            SortedSet sortedSet = new SortedSet();
            addRefArray(sortedSet, vals);
            sortedSet = (SortedSet) sortedSet.getSetInBetween(16, 20);
            TestcaseHelper.assertEquals(0, sizeRef(sortedSet), "getSetInBetween() failed - size not 0 when using values that are out of bounds!");
        }

        // getSetInBetween one element
        {
            SortedSet sortedSet = new SortedSet();
            addRefArray(sortedSet, vals);
            sortedSet = (SortedSet) sortedSet.getSetInBetween(11, 11);
            TestcaseHelper.assertEquals(1, sizeRef(sortedSet),
                    "getSetInBetween() failed for getting one element - size not 1!");
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "toArray")
    public void secTest__SortedSet_toArray() throws Exception {
        // empty list
        SortedSet sortedSet = new SortedSet();
        int[] array = sortedSet.toArray();

        TestcaseHelper.assertEquals(0, array.length, "toArray() failed for empty Set - length not 0!");
        TestcaseHelper.assertArrayEquals(new int[]{}, array, "toArray() failed for empty Set!");

        SortedSetRef sortedSetRef = new SortedSetRef();

        int[] values = createRandomArray(20);

        sortedSetRef.add(values);
        addRefArray(sortedSet, values);

        int[] arrayRef = sortedSetRef.toArray();
        array = sortedSet.toArray();

        TestcaseHelper.assertEquals(arrayRef.length, array.length, "toArray() failed - wrong array size!");
        TestcaseHelper.assertArrayEquals(arrayRef, array, "toArray() failed - array entries not equal!");
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "toReversedArray")
    public void secTest__SortedSet_toReversedArray() throws Exception {
        // empty list
        SortedSet sortedSet = new SortedSet();
        int[] array = sortedSet.toReversedArray();

        TestcaseHelper.assertEquals(0, array.length, "toReversedArray() failed for empty Set - length not 0!");
        TestcaseHelper.assertArrayEquals(new int[]{}, array, "toReversedArray() failed for empty Set!");

        SortedSetRef sortedSetRef = new SortedSetRef();
        int[] values = createRandomArray(20);
        sortedSetRef.add(values);
        addRefArray(sortedSet, values);
        int[] arrayRef = sortedSetRef.toReversedArray();
        array = sortedSet.toReversedArray();

        TestcaseHelper.assertEquals(arrayRef.length, array.length, "toReversedArray() failed - wrong array size!");
        TestcaseHelper.assertArrayEquals(arrayRef, array, "toReversedArray() failed - array entries not equal!");
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 1, comment = "toString")
    public void secTest__SortedSet_toString() throws Exception {
        // empty list
        SortedSetRef sortedSetRef = new SortedSetRef();
        SortedSet sortedSet = new SortedSet();

        TestcaseHelper.assertEquals(sortedSetRef.toString(), sortedSet.toString(), "toString() failed for empty Set!");

        int[] values = createRandomArray(20);
        sortedSetRef.add(values);
        addRefArray(sortedSet, values);

        TestcaseHelper.assertEquals(sortedSetRef.toString(), sortedSet.toString(), "toString() failed!");
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 0.5, comment = "clear")
    public void secTest__SortedSet_clear() throws Exception {
        SortedSet sortedSet = new SortedSet();
        for (int i = 0; i < 5; i++) {
            addRef(sortedSet, i);
        }
        sortedSet.clear();

        TestcaseHelper.assertNull(TestcaseHelper.getValueOfPrivateField(sortedSet, "head"),
                "clear() failed - head not null!");
        TestcaseHelper.assertNull(TestcaseHelper.getValueOfPrivateField(sortedSet, "tail"),
                "clear() failed - tail not null!");
        TestcaseHelper.assertEquals(0, sizeRef(sortedSet), "Set does not have size 0 after clear()");
    }

    @Test(timeout = 500)
    @Points(exID = "SortedSetTests", bonus = 2, comment = "main (manuell korrigiert)")
    public void secTest__SortedSet_main() {
        fail("Wird noch manuell korrigert (2 Punkte)");
    }

    // ========= Helper Methods ==========

    private void addRef(SortedSet sortedSet, int value) throws Exception {
        Object element = createListItemInstance(value);
        Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
        Object tail = TestcaseHelper.getValueOfPrivateField(sortedSet, "tail");

        if (head == null) {
            TestcaseHelper.setValueOfPrivateField(sortedSet, "head", element);
            TestcaseHelper.setValueOfPrivateField(sortedSet, "tail", element);
        } else if (((int) TestcaseHelper.getValueOfPrivateField(tail, "value")) < value) {
            TestcaseHelper.setValueOfPrivateField(element, "next", null);
            TestcaseHelper.setValueOfPrivateField(element, "previous", tail);
            TestcaseHelper.setValueOfPrivateField(tail, "next", element);
            TestcaseHelper.setValueOfPrivateField(sortedSet, "tail", element);
            tail = TestcaseHelper.getValueOfPrivateField(sortedSet, "tail");

        } else {
            for (Object e = head; e != null; e = TestcaseHelper.getValueOfPrivateField(e, "next")) {
                if (((int) TestcaseHelper.getValueOfPrivateField(e, "value")) >= value) {
                    TestcaseHelper.setValueOfPrivateField(element, "next", e);
                    TestcaseHelper.setValueOfPrivateField(element, "previous", TestcaseHelper.getValueOfPrivateField(e, "previous"));

                    if (head == e) {
                        TestcaseHelper.setValueOfPrivateField(sortedSet, "head", element);
                    } else {
                        Object previous = TestcaseHelper.getValueOfPrivateField(e, "previous");
                        TestcaseHelper.setValueOfPrivateField(previous, "next", element);
                    }
                    TestcaseHelper.setValueOfPrivateField(e, "previous", element);
                    break;
                }
            }
        }
    }

    private void addRefArray(SortedSet sortedSet, int[] values) throws Exception {
        for (int value : values) {
            addRef(sortedSet, value);
        }
    }

    private int sizeRef(SortedSet sortedSet) {
        int size = 0;
        Object runner = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
        for (; runner != null; runner = TestcaseHelper.getValueOfPrivateField(runner, "next")) {
            size++;
        }
        return size;
    }

    private void removeRef(SortedSet sortedSet, int value) throws Exception {
        Object head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
        Object tail = TestcaseHelper.getValueOfPrivateField(sortedSet, "tail");

        int headVal = getListItemValue(head);
        int tailVal = getListItemValue(tail);

        if (headVal == value) {
            Object headNext = TestcaseHelper.getValueOfPrivateField(head, "next");
            TestcaseHelper.setValueOfPrivateField(sortedSet, "head", headNext);

            // update head
            head = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");

            if (head != null) {
                TestcaseHelper.setValueOfPrivateField(head, "previous", null);
            } else {
                TestcaseHelper.setValueOfPrivateField(sortedSet, "tail", null);
            }

        } else if (tailVal == value) {
            Object tailPrev = TestcaseHelper.getValueOfPrivateField(tail, "previous");
            TestcaseHelper.setValueOfPrivateField(sortedSet, "tail", tailPrev);

            // update tail
            tail = TestcaseHelper.getValueOfPrivateField(sortedSet, "tail");

            TestcaseHelper.setValueOfPrivateField(tail, "next", null);

        } else {
            for (Object e = head; e != null; e = TestcaseHelper.getValueOfPrivateField(e, "next")) {
                if (getListItemValue(e) == value) {
                    Object ePrev = TestcaseHelper.getValueOfPrivateField(e, "previous");
                    Object eNext = TestcaseHelper.getValueOfPrivateField(e, "next");

                    TestcaseHelper.setValueOfPrivateField(ePrev, "next", eNext);
                    TestcaseHelper.setValueOfPrivateField(eNext, "previous", ePrev);
                }
            }
        }
    }

    private String toStringRef(SortedSet sortedSet) {
        String s = "{ ";
        Object e = TestcaseHelper.getValueOfPrivateField(sortedSet, "head");
        for (; e != null; e = TestcaseHelper.getValueOfPrivateField(e, "next")) {
            s += toStringListItemRef(e);
        }
        s += " }";
        return s;
    }

    private String toStringListItemRef(Object e) {
        int value = TestcaseHelper.getValueOfPrivateField(e, "value");
        return (TestcaseHelper.getValueOfPrivateField(e, "next") == null) ? ("[" + value + "]") : ("[" + value + "] --> ");
    }

    private int[] createRandomArray(int size) {
        int[] vals = new int[size];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = (int) (Math.random() * 1000);

            // Prevent duplicate values
            for (int j = 0; j < i; j++) {
                if (vals[i] == vals[j]) {
                    i--;
                    break;
                }
            }
        }
        return vals;
    }

    private Object createListItemInstance(int value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//        Class<?> cInner = TestcaseHelper.getInnerClass("SortedSet$ListItem");
//        Constructor<?> constructor = cInner.getConstructor(SortedSet.class, int.class);
//        constructor.setAccessible(true);
//        return constructor.newInstance(new SortedSet(), value);
        SortedSet set = new SortedSet();
        return set.new ListItem(value);
    }

    private int getListItemValue(Object ListItem) {
        return TestcaseHelper.getValueOfPrivateField(ListItem, "value");
    }

    // Innere Klasse zur Referenz

    public static class SortedSetRef implements OrderedSet {

        private class ListItem {
            private int value;
            private ListItem next;
            private ListItem previous;

            public ListItem(int value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return (next == null) ? ("[" + value + "]") : ("[" + value + "] --> ");
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof ListItem) {
                    ListItem ListItem = (ListItem) obj;
                    return value == ListItem.value;
                }
                return false;
            }
        }

        private ListItem head;
        private ListItem tail;

        public void add(int value) {
            if (contains(value)) {
                throw new ElementExistsException();
            }

            ListItem element = new ListItem(value);

            if (head == null) {
                head = element;
                tail = element;

            } else if (tail.value <= element.value) {
                element.next = null;
                element.previous = tail;
                tail.next = element;
                tail = element;

            } else {
                for (ListItem e = head; e != null; e = e.next) {
                    if (e.value >= element.value) {
                        element.next = e;
                        element.previous = e.previous;

                        if (head == e) {
                            head = element;
                        } else {
                            e.previous.next = element;
                        }

                        e.previous = element;
                        break;
                    }
                }
            }
        }

        public void add(int[] values) {
            for (int value : values) {
                add(value);
            }
        }

        public boolean contains(int value) {
            for (ListItem e = head; e != null; e = e.next) {
                if (e.value == value) {
                    return true;
                }
            }
            return false;
        }

        public int size() {
            int size = 0;

            for (ListItem e = head; e != null; e = e.next) {
                size++;
            }
            return size;
        }

        public void remove(int value) throws NoSuchElementException {

            if (!contains(value)) {
                throw new NoSuchElementException("Element " + value + " couldn't be removed (wasn't found).");
            }

            if (head.value == value) {
                head = head.next;

                if (head != null) {
                    head.previous = null;
                } else {
                    tail = null;
                }

            } else if (tail.value == value) {
                tail = tail.previous;
                tail.next = null;

            } else {
                for (ListItem e = head; e != null; e = e.next) {
                    if (e.value == value) {
                        e.previous.next = e.next;
                        e.next.previous = e.previous;
                    }
                }
            }

        }

        public OrderedSet clone() {
            SortedSetRef cloned = new SortedSetRef();
            cloned.add(toArray());
            return cloned;
        }

        public OrderedSet intersect(OrderedSet s) {
            SortedSetRef intersected = new SortedSetRef();

            for (int value : toArray()) {
                if (s.contains(value)) {
                    intersected.add(value);
                }
            }

            return intersected;
        }

        public OrderedSet unite(OrderedSet s) {
            SortedSetRef united = (SortedSetRef) clone();

            united.add(s.toArray());
            return united;
        }

        public OrderedSet subtract(OrderedSet s) {
            SortedSetRef diffed = (SortedSetRef) clone();
            int[] values = s.toArray();

            for (int value : values) {
                try {
                    diffed.remove(value);
                } catch (NoSuchElementException e) {
                }
            }

            return diffed;
        }

        public OrderedSet getSetInBetween(int from, int to) {
            SortedSetRef range = new SortedSetRef();

            for (int value : toArray()) {
                if (value >= from && value <= to) {
                    range.add(value);
                }
            }
            return range;
        }

        public int[] toArray() {
            int[] values = new int[size()];
            int idx = 0;

            for (ListItem n = head; n != null; n = n.next) {
                values[idx++] = n.value;
            }

            return values;
        }

        public int[] toReversedArray() {
            int[] values = new int[size()];
            int idx = 0;

            for (ListItem n = tail; n != null; n = n.previous) {
                values[idx++] = n.value;
            }

            return values;
        }

        @Override
        public String toString() {
            String s = "{ ";

            for (ListItem e = this.head; e != null; e = e.next) {
                s += e.toString();
            }

            s += " }";
            return s;
        }

        public void clear() {
            this.head = null;
            this.tail = null;
        }
    }
}
