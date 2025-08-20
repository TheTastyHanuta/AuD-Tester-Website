package tester.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to perform some initialization task once in the
 * secret test. A field can be annotated with this annotation. The field is
 * initialized with the result of the method whose name is an argument to the
 * annotation. The method is called once and its result is cached for
 * subsequent initializations.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitializeOnce {
    /**
     * The name of the method that is used for the initialization.
     */
    String value();
}
