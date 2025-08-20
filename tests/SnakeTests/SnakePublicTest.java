import tester.annotations.*;

import org.junit.Test;

import java.lang.reflect.Method;

@Exercises({
        @Ex(exID = "0. Coderichtlinien", points = 4),
        @Ex(exID = "Apple", points = 5),
        @Ex(exID = "Brick", points = 3),
        @Ex(exID = "Point", points = 2),
        @Ex(exID = "SnakeTests", points = 21),
        @Ex(exID = "GameItem", points = 4),
        @Ex(exID = "SnakeGame", points = 21),

})
//@CompareInterface({"Box", "BoxTarget", "Direction", "EmptyPassage", "GameItem", "Passage", "Player", "Point", "SokobanGame", "Tile", "Wall"})
public class SnakePublicTest {

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
    @Points(exID = "Apple", malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__checkApple() {
        Apple a = new Apple(0, 0);

        try {
            a.paint(new AudGraphics());
        } catch (Exception e) {
            e.printStackTrace();
        }

// test via reflection as this is a common mistake
//        try {
//            a.getValue();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            a.getPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(timeout = 500)
    @Points(exID = "Brick", malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__checkBrick() {
        Brick b = new Brick(0, 0);
        try {
            b.paint(new AudGraphics());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            b.getPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(timeout = 500)
    @Points(exID = "GameItem", malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__checkGameItem() {
    }

    @Test(timeout = 500)
    @Points(exID = "Point", malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__checkPoint() {
        Point p = new Point(0, 0);
        try {
            int x = p.getX();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int y = p.getY();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeTests", malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__checkSnake() {
        System.out.println(Snake.Direction.RIGHT);
        System.out.println(Snake.Direction.DOWN);
        System.out.println(Snake.Direction.UP);
        System.out.println(Snake.Direction.LEFT);

        try {
            Snake snake = new Snake(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Snake snake = new Snake(0, 0);
            snake.setNextDirection(Snake.Direction.LEFT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Snake snake = new Snake(0, 0);
            snake.step();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Snake snake = new Snake(0, 0);
            snake.grow(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Snake snake = new Snake(0, 0);
            snake.collidesWith(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Snake snake = new Snake(0, 0);
            snake.collidesWith(new Apple(0, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Snake snake = new Snake(0, 0);
            snake.collidesWith(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Snake snake = new Snake(0, 0);
            snake.collidesWithSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Snake snake = new Snake(0, 0);
            snake.paint(new AudGraphics());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(timeout = 500)
    @Points(exID = "SnakeGame", malus = 1000, bonus = 0.000001, comment = " ")
    public void pubTest__checkSnakeGame() {
        try {
            SnakeGame g = new SnakeGame();
            g.handleInput(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SnakeGame g = new SnakeGame();
            g.updateGame(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int i = SnakeGame.SQUARE_SIZE;
            System.out.println(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SnakeGame g = new SnakeGame();
            Method m = SnakeGame.class.getDeclaredMethod("createNewApple");
            TestcaseHelper.invoke(m, g, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SnakeGame g = new SnakeGame();
            g.paintGame(new AudGraphics());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SnakeGame g = new SnakeGame();
            Method m = SnakeGame.class.getDeclaredMethod("checkCollisions");
            TestcaseHelper.invoke(m, g, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
