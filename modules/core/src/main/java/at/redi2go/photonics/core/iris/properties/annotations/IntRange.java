package at.redi2go.photonics.core.iris.properties.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntRange {
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
}
