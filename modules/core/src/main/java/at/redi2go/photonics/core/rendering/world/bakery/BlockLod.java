package at.redi2go.photonics.core.rendering.world.bakery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.TYPE_USE})
public @interface BlockLod {
    int NO_SEED = 1;
    int NO_TINT = 1 << 1;
    int CONTAINED = 1 << 2;
}
