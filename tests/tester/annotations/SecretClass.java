package tester.annotations;

import java.lang.annotation.*;

/**
 * This annotation defines the secret test class.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecretClass {
}