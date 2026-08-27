package at.redi2go.photonics.engine;

import at.redi2go.photonics.engine.iris.pipeline.uniforms.IValueUpdateNotifier;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UniformUpdater {
    private boolean needsUpdate = true;
    private final Set<NotifierImpl> updateRequired = ConcurrentHashMap.newKeySet();

    public IValueUpdateNotifier newNotifier() {
        return new NotifierImpl();
    }

    public void updateNextFrame() {
        needsUpdate = true;
    }

    public void updateNow() {
        needsUpdate = true;
        updateAll();
    }

    public void updateAll() {
        if (!needsUpdate) return;
        needsUpdate = false;

        for (var itr = updateRequired.iterator(); itr.hasNext(); ) {
            itr.next().update();
            itr.remove();
        }
    }

    private class NotifierImpl implements IValueUpdateNotifier {
        private Runnable listener = null;

        public void update() {
            var listener = this.listener;
            this.listener = null;

            if (listener != null)
                listener.run();
        }

        @Override
        public void setListener(Runnable var1) {
            var oldValue = listener;
            listener = var1;

            if (oldValue == null)
                updateRequired.add(this);
        }
    }
}
