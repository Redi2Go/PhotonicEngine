package at.redi2go.photonics.client.rendering.patching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PatchTest {
   @Test
   void replacesPresentAnchor() {
      assertEquals("before replacement after", Patch.replaceOrWarn("before anchor after", "anchor", "replacement", "test"));
   }

   @Test
   void leavesSourceUnchangedWhenAnchorIsMissing() {
      assertEquals("unchanged", Patch.replaceOrWarn("unchanged", "missing", "replacement", "test", false));
   }
}
