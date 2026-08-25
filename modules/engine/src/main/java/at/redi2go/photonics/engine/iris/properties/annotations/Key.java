package at.redi2go.photonics.engine.iris.properties.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Key {
    String value() default "";
    String prefix() default "";

    String[] legacy() default { "" };
}
