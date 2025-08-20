package tester.annotations;

import java.lang.annotation.*;

/**
 * Defines the exercises. Use this annotation to annotate the public test
 * class.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Exercises {
    /**
     * An array of exercise definitions. You may use multiple definitions if the
     * tests are used for multiple exercises.
     */
    Ex[] value();
}