package tester.annotations;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Bonus {
    String exID();

    double bonus();

    String comment() default "<n.a.>";
}