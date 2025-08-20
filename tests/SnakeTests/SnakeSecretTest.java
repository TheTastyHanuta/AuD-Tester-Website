import tester.annotations.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

@SecretClass
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SnakeSecretTest {
    // =============== SYSTEM ===============
//    @Rule
//    public final PointsLogger pointsLogger = new PointsLogger();
//    @ClassRule
//    public final static PointsSummary pointsSummary = new PointsSummary();

    // ========= TEST DATA =========
    private static final Random RND = new Random(4711_0815_666L);

    // ============ TESTS ==========
    @Test(timeout = 500)
    @Points(exID = "0. Coderichtlinien", bonus = 4, comment = "Noch nicht korrigiert (4 Punkte)")
    public void secTest__checkCodeStyle() {
        fail();
    }

    // ============== Apple ===============
    // Insgesamt 5 Punkte

    @Test(timeout = 500)
    @Points(exID = "Apple", bonus = 1, comment = "Signatur")
    public void secTest__Apple__signature() {
        TestcaseHelper.checkClassSignature(Apple.class, Modifier.PUBLIC);
        TestcaseHelper.checkSuperclass(Apple.class, GameItem.class);
    }

    @Test(timeout = 500)
    @Points(exID = "Apple", bonus = 0.5, comment = "Attribute (nextValue)")
    public void secTest__Apple__attribute_nextValue() {
        TestcaseHelper.checkAttributeModifiers(Apple.class, "nextValue",
                Modifier.PRIVATE | Modifier.STATIC);
        TestcaseHelper.checkAttributeType(Apple.class, "nextValue", int.class);
        // check if nextValue has been initialized with 1
        try {
            assertEquals("nextValue has not been initialized with 1", 1, (int) TestcaseHelper.getValueOfPrivateStaticField(Apple.class, "nextValue"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(timeout = 500)
    @Points(exID = "Apple", bonus = 0.5, comment = "Attribute (VALUE)")
    public void secTest__Apple__attribute_VALUE() {
        TestcaseHelper.checkAttributeModifiers(Apple.class, "VALUE",
                Modifier.PRIVATE | Modifier.FINAL);
        TestcaseHelper.checkAttributeType(Apple.class, "VALUE", int.class);
    }

    @Test(timeout = 500)
    @Points(exID = "Apple", bonus = 1.5, comment = "Konstruktor")
    public void secTest__Apple__constructor() {
        TestcaseHelper.checkConstructors(Apple.class, new String[]{"SnakeTests.Apple(int, int)"},
                new int[]{Modifier.PUBLIC});
        // can't test whether super() was called

        try {
            // the next line should throw an error if position is inherited from GameItem
            // (as should be the case)
            Apple.class.getDeclaredField("position");

            // If no exception was thrown and we end up here, position was overwritten
            // instead of inherited!
            fail("Attribute position should not be overwritten but inherited from GameItem! The constructor should call super(x,y)");
        } catch (NoSuchFieldException e) {
        }

        // test if VALUE == nextValue
        Apple a = new Apple(10, 12);
        int nextValue = TestcaseHelper.getValueOfPrivateField(a, "nextValue");
        assertEquals("VALUE has not been initialized correctly", nextValue - 1, (int) TestcaseHelper.getValueOfPrivateField(a, "VALUE"));
        for (int i = 0; i < 10; i++) {
            a = new Apple(15, 14);
            nextValue++;
            // System.out.println(nextValue);
            assertEquals("VALUE and nextValue are not counting up when new apples are being created", nextValue - 1, (int) TestcaseHelper.getValueOfPrivateField(a, "VALUE"));
        }
    }

    @Test(timeout = 500)
    @Points(exID = "Apple", bonus = 0.5, comment = "getValue")
    public void secTest__Apple__getValue() {
        Apple tester = new Apple(0, 0);
        //     TestcaseHelper.setValueOfPrivateField(tester, "VALUE", );


        try {
            Method m = Apple.class.getDeclaredMethod("getValue");

            int expected = (int) TestcaseHelper.getValueOfPrivateField(tester, "VALUE");


            int got = (int) TestcaseHelper.invoke(m, tester, null);

            TestcaseHelper.assertEquals(expected, got, "getValue does not return the correct VALUE");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    @Test(timeout = 500)
    @Points(exID = "Apple", bonus = 1, comment = "paint")
    public void secTest__Apple__paint() {
        fail("Noch nicht korrigiert (1 Punkt)");
    }


    // ================ Brick =================
    // Insgesamt 3 Punkte

    @Test(timeout = 500)
    @Points(exID = "Brick", bonus = 1, comment = "Signatur")
    public void secTest__Brick__signature() {
        TestcaseHelper.checkClassSignature(Brick.class, Modifier.PUBLIC);
        TestcaseHelper.checkSuperclass(Brick.class, GameItem.class);
    }

    @Test(timeout = 500)
    @Points(exID = "Brick", bonus = 1, comment = "Konstruktor")
    public void secTest__Brick__constructor() throws SecurityException {
        TestcaseHelper.checkConstructors(Brick.class, new String[]{"SnakeTests.Brick(int, int)"},
                new int[]{Modifier.PUBLIC});
        // can't test whether super() was called

        try {
            // the next line should throw an error if position is inherited from GameItem
            // (as should be the case)
            Brick.class.getDeclaredField("position");

            // If no exception was thrown and we end up here, position was overwritten
            // instead of inherited!
            fail("Attribute position should not be overwritten but inherited from GameItem! The constructor should call super(x,y)");
        } catch (NoSuchFieldException e) {

        }
    }

    @Test(timeout = 500)
    @Points(exID = "Brick", bonus = 1, comment = "paint")
    public void secTest__Brick__paint() {
        fail("Noch nicht korrigiert (1 Punkt)");
    }


    // ============== GameItem ================
    // Insgesamt 4 Punkte

    @Test(timeout = 500)
    @Points(exID = "GameItem", bonus = 1, comment = "Signatur")
    public void secTest__GameItem__signature() {
        TestcaseHelper.checkClassSignature(GameItem.class, Modifier.PUBLIC | Modifier.ABSTRACT);
        TestcaseHelper.checkSuperclass(GameItem.class, null);
    }

    @Test(timeout = 500)
    @Points(exID = "GameItem", bonus = 1, comment = "position (Modifier, Typ)")
    public void secTest__GameItem__position() {
        TestcaseHelper.checkAttributeModifiers(GameItem.class, "position", Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(GameItem.class, "position", Point.class);
    }

    @Test(timeout = 500)
    @Points(exID = "GameItem", bonus = 0.5, comment = "Konstruktor (0,5P)")
    public void secTest__GameItem__constructor() throws Exception {
        TestcaseHelper.checkConstructors(GameItem.class, new String[]{"SnakeTests.GameItem(int, int)"},
                new int[]{Modifier.PUBLIC});

//        final class Tester extends GameItem {
//            public Tester(int x, int y) {
//                super(x, y);
//            }
//
//            @Override
//            public void paint(AudGraphics g) {
//                // nothing
//            }
//        }

//        Field fieldPos = GameItem.class.getDeclaredField("position");
//        fieldPos.setAccessible(true);
//
//        Tester t = new Tester(37, 42);
//        Point p = (Point) fieldPos.get(t);
//
//        int x = p.getX();
//        int y = p.getY();
//
//        assertEquals("X position is incorrect.", 37, x);
//        assertEquals("Y position is incorrect.", 42, y);
        fail("Wird manuell getestet.");
    }

    @Test(timeout = 500)
    @Points(exID = "GameItem", bonus = 0.5, comment = "getPosition (0,5P)")
    public void secTest__GameItem__getPosition() throws Exception {
//        final class Tester extends GameItem {
//            public Tester(int x, int y) {
//                super(x, y);
//            }
//
//            @Override
//            public void paint(AudGraphics g) {
//                // nothing
//            }
//        }

//        Tester t = new Tester(37, 42);
//        Point p = t.getPosition();
//
//        int x = p.getX();
//        int y = p.getY();
//
//        assertEquals("X position is incorrect.", 37, x);
//        assertEquals("Y position is incorrect.", 42, y);
        fail("Wird manuell getestet.");
    }

    @Test(timeout = 500)
    @Points(exID = "GameItem", bonus = 1, comment = "paint")
    public void secTest__GameItem__paint() {
        TestcaseHelper.checkMethod(GameItem.class, "paint(SnakeTests.AudGraphics)", void.class,
                Modifier.PUBLIC | Modifier.ABSTRACT);
    }

    // ================ Point =================
    // Insgesamt 2 Punkte

    //bei Point keine Punkte fuer Signatur

    /*
    @Test(timeout = 500)
    @Points(exID = "Point", bonus = 0.5, comment = "Signatur")
    public void secTest__Point__signature() {
        TestcaseHelper.checkClassSignature(Point.class, Modifier.PUBLIC);
        TestcaseHelper.checkSuperclass(Point.class, null);
    }
     */

    @Test(timeout = 500)
    @Points(exID = "Point", bonus = 0.5, comment = "Attribute")
    public void secTest__Point__attributes() {
        TestcaseHelper.checkAttributeModifiers(Point.class, "x", Modifier.PRIVATE);
        TestcaseHelper.checkAttributeModifiers(Point.class, "y", Modifier.PRIVATE);
    }

    @Test(timeout = 500)
    @Points(exID = "Point", bonus = 0.5, comment = "Konstruktor")
    public void secTest__Point__constructor() throws Exception {
        TestcaseHelper.checkConstructors(Point.class, new String[]{"SnakeTests.Point(int, int)"},
                new int[]{Modifier.PUBLIC});

        for (int xExp : RND.ints(20, 0, 50).toArray()) {
            for (int yExp : RND.ints(20, 0, 50).toArray()) {
                Point p = new Point(xExp, yExp);

                int x = p.getX();
                int y = p.getY();

                assertEquals("Value for x has not been stored correctly.", xExp, x);
                assertEquals("Value for y has not been stored correctly.", yExp, y);
            }
        }

    }

    @Test(timeout = 500)
    @Points(exID = "Point", bonus = 0.5, comment = "getX")
    public void secTest__Point__getX() throws Exception {
        Point p = new Point(0, 0);

        for (int x : RND.ints(20, 0, 50).toArray()) {
            TestcaseHelper.setValueOfPrivateField(p, "x", x);
            assertEquals("getX() does not return the value of the private attribute x", x, p.getX());
        }
    }

    @Test(timeout = 500)
    @Points(exID = "Point", bonus = 0.5, comment = "getY")
    public void secTest__Point__getY() throws Exception {
        Point p = new Point(0, 0);

        for (int y : RND.ints(20, 0, 50).toArray()) {
            TestcaseHelper.setValueOfPrivateField(p, "y", y);
            assertEquals("getY() does not return the value of the private attribute y", y, p.getY());
        }
    }


    // ================ Snake ================


    // Insgesamt: 21.5 Punkte

    //no points for signature
    /*@Test(timeout = 500)
    @Points(exID = "Snake", bonus = 0.5, comment = "Signatur")
    public void secTest__Snake__signature() {
        TestcaseHelper.checkClassSignature(Snake.class, Modifier.PUBLIC);
    }*/

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 1, comment = "Direction")
    public void secTest__Snake__Direction() {

        assertEquals("Incorrect modifiers for enum Direction.", Modifier.toString(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL),
                Modifier.toString(Snake.Direction.class.getModifiers()));

        Snake.Direction[] values = Snake.Direction.class.getEnumConstants();
        String[] actual = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            actual[i] = values[i].toString();
        }

        Arrays.sort(actual);

        String[] expected = new String[]{"DOWN", "LEFT", "RIGHT", "UP"};

        assertArrayEquals("Enum does not provide correct values", expected, actual);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 0.5, comment = "Attribute (points)")
    public void secTest__Snake__attribute_points() {
        TestcaseHelper.checkAttributeModifiers(Snake.class, "points",
                Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(Snake.class, "points", Point[].class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 0.5, comment = "Attribute (nextDirection)")
    public void secTest__Snake__attribute_nextDirection() {
        TestcaseHelper.checkAttributeModifiers(Snake.class, "nextDirection",
                Modifier.PRIVATE);
        // TODO: check Type Direction?
        //TestcaseHelper.checkAttributeType(Snake.class, "nextDirection", Snake.Direction);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 0.5, comment = "Attribute (lastDirection)")
    public void secTest__Snake__attribute_lastDirection() {
        TestcaseHelper.checkAttributeModifiers(Snake.class, "lastDirection",
                Modifier.PRIVATE);
        // TODO: check Type Direction?
        //TestcaseHelper.checkAttributeType(Snake.class, "nextDirection", Direction.class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 0.5, comment = "Attribute (color)")
    public void secTest__Snake__attribute_color() {
        TestcaseHelper.checkAttributeModifiers(Snake.class, "color",
                Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(Snake.class, "color", AudColor.class);
    }


    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 1, comment = "Konstruktor (Erzeugen des Arrays und Zuweisung des Kopfes")
    public void secTest__Snake__constructor_points() {
        /*TestcaseHelper.checkConstructors(Snake.class, new String[]{"Snake(int, int)", "Snake(int, int, int)"},
                new int[]{Modifier.PUBLIC, Modifier.PUBLIC});*/
        TestcaseHelper.checkConstructors(Snake.class, new String[]{"SnakeTests.Snake(int, int, int)"}, new int[]{Modifier.PUBLIC});

        for (int x : RND.ints(20, 1, 50).toArray()) {
            for (int y : RND.ints(20, 1, 50).toArray()) {
                // I also used y for length because three for-loops... meh?
                Snake s = new Snake(x, y, y);
                Point[] points = TestcaseHelper.getValueOfPrivateField(s, "points");
                assertEquals("Size of snake is wrong.", y, points.length);
                // I used getValueOfPrivateField for x and y, since I don't want to depend on points[0].getX() or points[0].getY() to work
                assertEquals("Starting point (x) is not set correctly.", x, (int) TestcaseHelper.getValueOfPrivateField(points[0], "x"));
                assertEquals("Starting point (y) is not set correctly.", y, (int) TestcaseHelper.getValueOfPrivateField(points[0], "y"));
            }
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 1, comment = "Konstruktor (Fehler bei Laenge <= 0)")
    public void secTest__Snake__constructor_negative_length() {
        try {
            for (int length : RND.ints(20, -100, 1).toArray()) {

                Snake s = new Snake(15, 24, length);

            }
        } catch (Exception e) {
            if (e instanceof NegativeArraySizeException) {
                e.printStackTrace();
                fail("Snake constructor did not prohibit a negative value for length");
            }
            //e.printStackTrace();
        }

        try {
            Snake s = new Snake(15, 24, 0);
        } catch (Exception e) {
            if (e instanceof ArrayIndexOutOfBoundsException) {
                e.printStackTrace();
                fail("Snake constructor did not prohibit length = 0");
            }
            //e.printStackTrace();
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 1, comment = "Konstruktor (Aufruf von this)")
    public void secTest__Snake__constructor_this() {
        TestcaseHelper.checkConstructors(Snake.class, new String[]{"SnakeTests.Snake(int, int)"}, new int[]{Modifier.PUBLIC});
        fail("Noch nicht korrigiert, ob mit this gearbeitet wurde (1 Punkt)");
    }


    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 1.5, comment = "setNextDirection")
    public void secTest__Snake__setNextDirection() {
        Snake s = new Snake(0, 0, 5);
        Snake.Direction defaultDirection = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
        if (defaultDirection == null) {
            fail("There was no default direction set");
        }
        // (ordinal() + 2) % 4 might not work if the student defined the enumeration in a different order than expected
        // therefore this is really ugly I'm sorry
        Snake.Direction impossibleDirection = helper_getImpossibleDirection(defaultDirection);

        // TODO: should also work if they hardcoded their impossible direction
        s.setNextDirection(impossibleDirection);
        Snake.Direction newDirection = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
        assertNotEquals("Direction should not have changed into the impossible direction.", impossibleDirection, newDirection);

        // I don't use values() because the order is important
        Snake.Direction[] directions = {Snake.Direction.LEFT, Snake.Direction.UP, Snake.Direction.RIGHT, Snake.Direction.DOWN};
        for (Snake.Direction d : directions) {
            Snake.Direction actual = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
            try {
                TestcaseHelper.setValueOfPrivateField(s, "lastDirection", actual);
            } catch (Exception e) {
                e.printStackTrace();
            }
            impossibleDirection = helper_getImpossibleDirection(actual);
            s.setNextDirection(d);
            if (d == impossibleDirection) {
                assertNotEquals("Direction should not have changed into the impossible direction.", impossibleDirection, defaultDirection);
                continue;
            }
            actual = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
            assertEquals("nextDirection not set correctly.", d, actual);
        }

        // check if they hardcoded their initial impossible direction

        Snake.Direction latestDirection = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
        System.out.println(latestDirection);
        try {
            TestcaseHelper.setValueOfPrivateField(s, "lastDirection", latestDirection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        impossibleDirection = helper_getImpossibleDirection(latestDirection);
        s.setNextDirection(impossibleDirection);
        Snake.Direction updatedDirection = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
        assertNotEquals("Direction should not have changed into the impossible direction.", impossibleDirection, updatedDirection);
    }

    private Snake.Direction helper_getImpossibleDirection(Snake.Direction direction) {
        Snake.Direction[] directions = {Snake.Direction.LEFT, Snake.Direction.UP, Snake.Direction.RIGHT, Snake.Direction.DOWN};
        for (int i = 0; i < directions.length; i++) {
            if (directions[i].equals(direction)) {
                return directions[(i + 2) % 4];
            }
        }
        return null;
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 3.5, comment = "step")
    public void secTest__Snake__step() throws Exception {
        {  //RIGHT
            Snake s = new Snake(10, 10);
            Point[] pointsAfter = TestcaseHelper.getValueOfPrivateField(s, "points");
            Point[] pointsBefore = new Point[pointsAfter.length];
            System.arraycopy(pointsAfter, 0, pointsBefore, 0, pointsAfter.length);
            // TODO: eigenes Direction erstellen?
            // TODO: hier ist der Test ein wenig harsh... wenn sie vergessen, lastDirection upzudaten, kriegen sie gleich 0/3.5 Punkte


            TestcaseHelper.setValueOfPrivateField(s, "nextDirection", Snake.Direction.RIGHT);
            s.step();
            // pointsAfter = TestcaseHelper.getValueOfPrivateField(s, "points");
            try {
                int actualX = TestcaseHelper.getValueOfPrivateField(pointsAfter[0], "x");
                int actualY = TestcaseHelper.getValueOfPrivateField(pointsAfter[0], "y");
                int expectedX = (int) TestcaseHelper.getValueOfPrivateField(pointsBefore[0], "x") + 1;
                int expectedY = TestcaseHelper.getValueOfPrivateField(pointsBefore[0], "y");
                assertEquals("The y coordinate of points[0] is not set correctly when going right", expectedY, actualY);
                assertEquals("The x coordinate of points[0] is not set correctly when going right", expectedX, actualX);
            } catch (NullPointerException e) {
                e.printStackTrace();
                fail("step does not work correctly, points[0] is null");
            }
            assertEquals("lastDirection has not been updated", Snake.Direction.RIGHT, TestcaseHelper.getValueOfPrivateField(s, "lastDirection"));
        }

        { //LEFT
            Snake s = new Snake(10, 10);
            Point[] pointsAfter = TestcaseHelper.getValueOfPrivateField(s, "points");
            Point[] pointsBefore = new Point[pointsAfter.length];
            System.arraycopy(pointsAfter, 0, pointsBefore, 0, pointsAfter.length);
            TestcaseHelper.setValueOfPrivateField(s, "nextDirection", Snake.Direction.LEFT);
            s.step();
            //pointsAfter = TestcaseHelper.getValueOfPrivateField(s, "points");
            try {
                int actualX = TestcaseHelper.getValueOfPrivateField(pointsAfter[0], "x");
                int actualY = TestcaseHelper.getValueOfPrivateField(pointsAfter[0], "y");
                int expectedX = (int) TestcaseHelper.getValueOfPrivateField(pointsBefore[0], "x") - 1;
                int expectedY = TestcaseHelper.getValueOfPrivateField(pointsBefore[0], "y");
                assertEquals("The y coordinate of points[0] is not set correctly when going left", expectedY, actualY);
                assertEquals("The x coordinate of points[0] is not set correctly when going left", expectedX, actualX);
            } catch (NullPointerException e) {
                e.printStackTrace();
                fail("step does not work correctly, points[0] is null");
            }
            assertEquals("lastDirection has not been updated", Snake.Direction.LEFT, TestcaseHelper.getValueOfPrivateField(s, "lastDirection"));
        }

        {//UP
            Snake s = new Snake(10, 10);
            Point[] pointsAfter = TestcaseHelper.getValueOfPrivateField(s, "points");
            Point[] pointsBefore = new Point[pointsAfter.length];
            System.arraycopy(pointsAfter, 0, pointsBefore, 0, pointsAfter.length);
            TestcaseHelper.setValueOfPrivateField(s, "nextDirection", Snake.Direction.UP);
            s.step();
            // Point[] pointsAfter = TestcaseHelper.getValueOfPrivateField(s, "points");
            try {
                int actualX = TestcaseHelper.getValueOfPrivateField(pointsAfter[0], "x");
                int actualY = TestcaseHelper.getValueOfPrivateField(pointsAfter[0], "y");
                int expectedX = (int) TestcaseHelper.getValueOfPrivateField(pointsBefore[0], "x");
                int expectedY = (int) TestcaseHelper.getValueOfPrivateField(pointsBefore[0], "y") - 1;
                assertEquals("The y coordinate of points[0] is not set correctly when going up", expectedY, actualY);
                assertEquals("The x coordinate of points[0] is not set correctly when going up", expectedX, actualX);
            } catch (NullPointerException e) {
                e.printStackTrace();
                fail("step does not work correctly, points[0] is null");
            }
            assertEquals("lastDirection has not been updated", Snake.Direction.UP, TestcaseHelper.getValueOfPrivateField(s, "lastDirection"));
        }

        {//DOWN
            Snake s = new Snake(10, 10);
            Point[] pointsAfter = TestcaseHelper.getValueOfPrivateField(s, "points");
            Point[] pointsBefore = new Point[pointsAfter.length];
            System.arraycopy(pointsAfter, 0, pointsBefore, 0, pointsAfter.length);
            TestcaseHelper.setValueOfPrivateField(s, "nextDirection", Snake.Direction.DOWN);
            s.step();
            // Point[] pointsAfter = TestcaseHelper.getValueOfPrivateField(s, "points");
            try {
                int actualX = TestcaseHelper.getValueOfPrivateField(pointsAfter[0], "x");
                int actualY = TestcaseHelper.getValueOfPrivateField(pointsAfter[0], "y");
                int expectedX = (int) TestcaseHelper.getValueOfPrivateField(pointsBefore[0], "x");
                int expectedY = (int) TestcaseHelper.getValueOfPrivateField(pointsBefore[0], "y") + 1;
                assertEquals("The y coordinate of points[0] is not set correctly when going down", expectedY, actualY);
                assertEquals("The x coordinate of points[0] is not set correctly when going down", expectedX, actualX);
            } catch (NullPointerException e) {
                e.printStackTrace();
                fail("step does not work correctly, points[0] is null");
            }
            assertEquals("lastDirection has not been updated", Snake.Direction.DOWN, TestcaseHelper.getValueOfPrivateField(s, "lastDirection"));
        }
    }


    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 2, comment = "grow")
    public void secTest__Snake__grow() {
        Snake s = new Snake(0, 0, 5);
        for (int amount : RND.ints(20, 1, 50).toArray()) {
            Point[] points = TestcaseHelper.getValueOfPrivateField(s, "points");
            int oldLength = points.length;
            s.grow(amount);
            points = TestcaseHelper.getValueOfPrivateField(s, "points");
            assertEquals("new length of snake is not correct", (oldLength + amount), points.length);
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 1, comment = "grow (ungueltiger Wert)")
    public void secTest__Snake__grow_negative_amount() {
        Snake s = new Snake(0, 0, 5);
        for (int amount : RND.ints(20, -100, 0).toArray()) {
            try {
                s.grow(amount);
                fail("grow did not throw an Exception when given a negative amount");
            } catch (IllegalArgumentException e) {
                //e.printStackTrace();
            }

        }
        try {
            s.grow(0);
            fail("grow did not throw an Exception when given 0 for the amount");
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 2, comment = "collidesWith(int x, int y)")
    public void secTest__Snake__collidesWith_Point() {
        for (int x : RND.ints(20, 0, 50).toArray()) {
            for (int y : RND.ints(20, 0, 50).toArray()) {
                Snake s = new Snake(x, y, 5);
                boolean collision = s.collidesWith(x, y);
                assertEquals("collidesWith should have returned true but actually returned false", true, collision);
                collision = s.collidesWith(x + 10, y + 10);
                assertEquals("collidesWith should have returned false but actually returned true", false, collision);
            }
        }


    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 1, comment = "collidesWith(GameItem g)")
    public void secTest__Snake__collidesWith_GameItem() {
        fail("Noch nicht korrigiert, ob ueberladene Methode aufgerufen wurde (1 Punkt)");
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 2, comment = "collidesWithSelf")
    public void secTest__Snake__collidesWithSelf() throws Exception {
        Snake s = new Snake(0, 0, 15);
        Point[] points = TestcaseHelper.getValueOfPrivateField(s, "points");

        // for (int i : RND.ints(10,1,points.length).toArray()) {
        boolean actual = s.collidesWithSelf();
        TestcaseHelper.assertEquals(false, actual, "CollidesWithSelf returns true right in the beginning even though that is not possible");

        s.step();
        try {
            int xCollision = TestcaseHelper.getValueOfPrivateField(points[0], "x");
            int yCollision = TestcaseHelper.getValueOfPrivateField(points[0], "y");
            TestcaseHelper.setValueOfPrivateField(points[1], "x", xCollision);
            TestcaseHelper.setValueOfPrivateField(points[1], "y", yCollision);
            //points[0] = points[i];
            //TestcaseHelper.setValueOfPrivateField(s, "points", points);
            actual = s.collidesWithSelf();
            TestcaseHelper.assertEquals(true, actual, "CollidesWithSelf returns false but should have returned true");
        } catch (NullPointerException e) {
            e.printStackTrace();
            fail("points[0] is null");
        }
        // }


        //fail("Noch nicht korrigiert (2 Punkte)")
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", bonus = 2, comment = "paint")
    public void secTest__Snake__paint() {
        fail("Noch nicht korrigiert (2 Punkte)");
    }


    // ============= SnakeGame ==============
    // Insgesamt 21 Punkte
    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Signatur (Vererbung)")
    public void secTest__SnakeGame__signature() {
        TestcaseHelper.checkClassSignature(SnakeGame.class, Modifier.PUBLIC);
        TestcaseHelper.checkSuperclass(SnakeGame.class, AudGameWindow.class);
    }


    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (SQUARE_SIZE)")
    public void secTest__SnakeGame__attribute_SQUARE_SIZE() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "SQUARE_SIZE",
                Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "SQUARE_SIZE", int.class);
        assertEquals("SQUARE_SIZE is not set correctly", 16, SnakeGame.SQUARE_SIZE);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (STEP_TIME)")
    public void secTest__SnakeGame__attribute_STEP_TIME() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "STEP_TIME", Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "STEP_TIME", int.class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (GROW_AMOUNT)")
    public void secTest__SnakeGame__attribute_GROW_AMOUNT() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "GROW_AMOUNT", Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "GROW_AMOUNT", int.class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (height)")
    public void secTest__SnakeGame__attribute_height() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "height", Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "height", int.class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (width)")
    public void secTest__SnakeGame__attribute_width() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "width", Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "width", int.class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (snake)")
    public void secTest__SnakeGame__attribute_snake() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "snake", Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "snake", Snake.class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (lastSnakeUpdate)")
    public void secTest__SnakeGame__attribute_lastSnakeUpdate() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "lastSnakeUpdate", Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "lastSnakeUpdate", long.class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (wall)")
    public void secTest__SnakeGame__attribute_wall() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "wall", Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "wall", Brick[].class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (apple)")
    public void secTest__SnakeGame__attribute_apple() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "apple", Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "apple", Apple.class);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Attribute (score)")
    public void secTest__SnakeGame__attribute_score() {
        TestcaseHelper.checkAttributeModifiers(SnakeGame.class, "score", Modifier.PRIVATE);
        TestcaseHelper.checkAttributeType(SnakeGame.class, "score", int.class);
        // should already be initialized with 0
        SnakeGame sg = new SnakeGame();
        try {
            assertEquals("score is not 0 in the beginning", 0, (int) TestcaseHelper.getValueOfPrivateField(sg, "score"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Konstruktor ()")
    public void secTest__SnakeGame__constructor_title() {
        SnakeGame sg = new SnakeGame();
        assertEquals("Title has not been set correctly", "AuD-Snake - Score: 0", sg.title);
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Konstruktor (Initialisierung width)")
    public void secTest__SnakeGame__constructor_width_height() {

        SnakeGame sg = new SnakeGame();
        // I have to initialize width here, otherwise the I can't use the try catch block (the same applies to the following methods)
        int width = 0;
        try {
            width = TestcaseHelper.getValueOfPrivateField(sg, "width");
        } catch (NullPointerException e) {
            e.printStackTrace();
            fail("width was not initialized");
        }
        TestcaseHelper.assertEquals(sg.getGameAreaWidth() / SnakeGame.SQUARE_SIZE, width,
                "Width is not initialized with the correct value");

        int height = 0;
        try {
            height = TestcaseHelper.getValueOfPrivateField(sg, "height");
        } catch (NullPointerException e) {
            e.printStackTrace();
            fail("height was not initialized");
        }
        TestcaseHelper.assertEquals(sg.getGameAreaHeight() / SnakeGame.SQUARE_SIZE, height,
                "Height is not initialized with the correct value");
    }


    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Konstruktor (snake + LastSnakeUpdate)")
    public void secTest__SnakeGame__constructor_snake() {
        SnakeGame sg = new SnakeGame();
        Snake s = TestcaseHelper.getValueOfPrivateField(sg, "snake");
        Point[] points = TestcaseHelper.getValueOfPrivateField(s, "points");
        assertEquals("The starting position of snake is not initialized correctly", ((sg.getGameAreaWidth() / SnakeGame.SQUARE_SIZE) / 2), points[0].getX());
        assertEquals("The starting position of snake is not initialized correctly", ((sg.getGameAreaHeight() / SnakeGame.SQUARE_SIZE) / 2), points[0].getY());
       /* long lastSnakeUpdateActual = 0;
        try {
            lastSnakeUpdateActual = TestcaseHelper.getValueOfPrivateField(sg, "lastSnakeUpdate");
        } catch (NullPointerException e) {
            e.printStackTrace();
            fail("lastSnakeUpdate was not initialized");
        }*/
        //TODO: Funktioniert so leider nicht immer, lieber manuell korrigieren - Darauf gibts eh keine Punkte? Also doch nicht
        //assertEquals("LastSnakeUpdate was not correctly initialized", System.currentTimeMillis(), (long) TestcaseHelper.getValueOfPrivateField(sg, "lastSnakeUpdate"));

    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 2, comment = "Konstruktor (wall)")
    public void secTest__SnakeGame__constructor_wall() {
        SnakeGame sg = new SnakeGame();
        Brick[] wall_actual = TestcaseHelper.getValueOfPrivateField(sg, "wall");
        if (wall_actual == null) {
            fail("Wall has not been initialized");
        }

        int width = sg.getGameAreaWidth() / SnakeGame.SQUARE_SIZE;
        int height = sg.getGameAreaHeight() / SnakeGame.SQUARE_SIZE;
        //TODO: soll man vielleicht ein eigenes SQUARE_SIZE anlegen? Fuer alle Faelle
        Brick[] wall_correct = new Brick[2 * width + 2 * height - 4];
        int wi = 0;
        for (int x = 0; x < width; x++) {
            wall_correct[wi++] = new Brick(x, 0);
            wall_correct[wi++] = new Brick(x, height - 1);
        }

        for (int y = 1; y < height - 1; y++) {
            wall_correct[wi++] = new Brick(0, y);
            wall_correct[wi++] = new Brick(width - 1, y);
        }

        for (int i = 0; i < wall_correct.length; i++) {
            try {
                //TODO: wie vergleichen?
                //assertEquals("Wall is not initialized correctly");
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        }

        fail("Noch nicht korrigiert (2 Punkte)");

    }


    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "Konstruktor (createNewApple)")
    public void secTest__SnakeGame__constructor_createNewApple() {
        fail("Methodenaufruf noch nicht korrigiert (0.5 Punkte)");
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 2, comment = "paintGame")
    public void secTest__SnakeGame__paintGame() {
        fail("Noch nicht korrigiert (2 Punkte)");
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 2, comment = "updateGame")
    public void secTest__SnakeGame__updateGame() {
        fail("Noch nicht korrigiert (2 Punkte)");
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 2, comment = "handleInput")
    public void secTest__SnakeGame__handleInput() {
        SnakeGame sg = new SnakeGame();
        Snake s = TestcaseHelper.getValueOfPrivateField(sg, "snake");

        //DOWN
        Snake.Direction impossibleDirection = helper_getImpossibleDirection(TestcaseHelper.getValueOfPrivateField(s, "lastDirection"));
        sg.handleInput(AudGameWindow.KeyEvent.VK_DOWN);
        if (Snake.Direction.DOWN != impossibleDirection) {
            Snake.Direction actual = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
            assertEquals("handleInput did not handle input 'DOWN'", Snake.Direction.DOWN, actual);
        }

        //RIGHT
        impossibleDirection = helper_getImpossibleDirection(TestcaseHelper.getValueOfPrivateField(s, "lastDirection"));
        sg.handleInput(AudGameWindow.KeyEvent.VK_RIGHT);
        if (Snake.Direction.RIGHT != impossibleDirection) {
            Snake.Direction actual = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
            assertEquals("handleInput did not handle input 'RIGHT'", Snake.Direction.RIGHT, actual);
        }

        //UP
        impossibleDirection = helper_getImpossibleDirection(TestcaseHelper.getValueOfPrivateField(s, "lastDirection"));
        sg.handleInput(AudGameWindow.KeyEvent.VK_UP);
        if (Snake.Direction.UP != impossibleDirection) {
            Snake.Direction actual = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
            assertEquals("handleInput did not handle input 'UP'", Snake.Direction.UP, actual);
        }

        //LEFT
        impossibleDirection = helper_getImpossibleDirection(TestcaseHelper.getValueOfPrivateField(s, "lastDirection"));
        sg.handleInput(AudGameWindow.KeyEvent.VK_LEFT);
        if (Snake.Direction.LEFT != impossibleDirection) {
            Snake.Direction actual = TestcaseHelper.getValueOfPrivateField(s, "nextDirection");
            assertEquals("handleInput did not handle input 'LEFT'", Snake.Direction.LEFT, actual);
        }


        //fail("Noch nicht korrigiert (2 Punkte)");
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 3, comment = "checkCollisions")
    public void secTest__SnakeGame__checkCollisions() {
        fail("Noch nicht korrigiert (3 Punkte)");
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 2, comment = "createNewApple")
    public void secTest__SnakeGame__createNewApple() {
        SnakeGame sg = new SnakeGame();
        Method m = null;
        int width = sg.getGameAreaWidth() / SnakeGame.SQUARE_SIZE;
        int height = sg.getGameAreaHeight() / SnakeGame.SQUARE_SIZE;
        Snake s = TestcaseHelper.getValueOfPrivateField(sg, "snake");
        Point[] points = TestcaseHelper.getValueOfPrivateField(s, "points");

        try {
            m = SnakeGame.class.getDeclaredMethod("createNewApple");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 100; i++) { //muss 100 Mal durchlaufen, damit auch wirklich mal zufaellig ein Fehlerfall vorkommt...
            assert m != null;
            TestcaseHelper.invoke(m, sg, null);
            Apple a = TestcaseHelper.getValueOfPrivateField(sg, "apple");

            Point applePos = null;
            try {
                applePos = TestcaseHelper.getValueOfPrivateSuperclassField(a, "position");
            } catch (Exception e) {
                e.printStackTrace();
                fail("The position of apple can't be reached");
            }
            //assert applePos != null;

            int appleX = TestcaseHelper.getValueOfPrivateField(applePos, "x"); //a.getPosition().getX();
            int appleY = TestcaseHelper.getValueOfPrivateField(applePos, "y");//a.getPosition().getY();


            if (appleX == 0 || appleX == (width - 1) || appleY == 0 || appleY == (height - 1)) {
                fail("apple spawns inside wall");
            }
            for (Point p : points) {
                if (p == null) {
                    continue;
                }

                int pointX = TestcaseHelper.getValueOfPrivateField(p, "x");
                int pointY = TestcaseHelper.getValueOfPrivateField(p, "y");
                if (pointX == appleX && pointY == appleY) {
                    fail("apple spawns inside snake");
                }
            }

        }

    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", bonus = 0.5, comment = "main-Methode")
    public void secTest__SnakeGame__main() {
        fail("Noch nicht korrigiert (0.5 Punkte)");
    }


}