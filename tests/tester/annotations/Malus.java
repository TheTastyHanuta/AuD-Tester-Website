package tester.annotations;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Malus {
    String exID();

    double malus();

    String comment() default "<n.a.>";
}