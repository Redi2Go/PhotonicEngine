package at.redi2go.photonics.api.gpu.textures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.TYPE_USE})
public @interface TextureUsage {
    int COPY_DST = 1;
    int COPY_SRC = 2;
    int TEXTURE_BINDING = 4;
    int RENDER_ATTACHMENT = 8;
    int CUBEMAP_COMPATIBLE = 16;

}
