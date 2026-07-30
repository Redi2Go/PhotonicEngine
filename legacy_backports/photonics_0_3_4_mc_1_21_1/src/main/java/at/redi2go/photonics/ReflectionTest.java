package at.redi2go.photonics;

import net.minecraft.client.renderer.block.model.FaceBakery;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionTest {
    public static void main(String[] args) {
        for (Method m : FaceBakery.class.getDeclaredMethods()) {
            if (m.getName().equals("bakeQuad")) {
                System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
            }
        }
    }
}
