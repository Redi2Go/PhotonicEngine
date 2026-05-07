package at.redi2go.photonics.core.iris.pipeline.texture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.TYPE_USE})
public @interface AttachmentUsage {
    int FLIP = 1 << 1;

    int CREATE_SAMPLER = 1 << 2;
    int CREATE_PREV_SAMPLER = 1 << 3;
}
