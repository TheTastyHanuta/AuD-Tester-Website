package tester.annotations;

import java.lang.annotation.*;

/**
 * Use this annotation to deactivate the security manager for certain callers.
 * In other words, this annotation can be used to allow actions that usually
 * are forbidden by the security manager for certain methods.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeCallers {
    /**
     * The names of the methods that can perform actions that are otherwise
     * forbidden.
     */
    String[] value();
}