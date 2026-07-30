package at.redi2go.photonics.client.api;

import java.util.Arrays;
import java.util.List;
import net.irisshaders.iris.helpers.StringPair;

public enum AlphaMode {
   NONE(),
   BLOCK(new StringPair("PH_USE_TRANSPARENCY", "")),
   VOXEL(new StringPair("PH_USE_TRANSPARENCY", ""), new StringPair("PH_FULL_TRANSPARENCY", ""));

   private final StringPair[] defines;

   AlphaMode(StringPair... defines) {
      this.defines = defines;
   }

   public void registerDefines(List<StringPair> defines) {
      defines.addAll(Arrays.asList(this.defines));
   }
}
