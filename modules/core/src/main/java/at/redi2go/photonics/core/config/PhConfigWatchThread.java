package at.redi2go.photonics.core.config;

import at.redi2go.photonics.api.mc.Mc;
import at.redi2go.photonics.core.Photonics;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class PhConfigWatchThread extends Thread {
    private static String ERROR_MESSAGE = "an error was thrown in the config watch service";
    public static PhConfigWatchThread INSTANCE = new PhConfigWatchThread();

    private WatchService service;
    private WatchKey key;

    private volatile boolean isSaving = false;
    private volatile boolean ignoreNextModify = false;

    private PhConfigWatchThread() {
        super("photonics-config-watcher");
    }

    public static void beginSave() {
        INSTANCE.isSaving = true;
    }

    public static void endSave() {
        INSTANCE.ignoreNextModify = true;
        INSTANCE.isSaving = false;
    }

    public static void reset() {
        INSTANCE.ignoreNextModify = false;
        INSTANCE.isSaving = false;
    }

    @SuppressWarnings("unchecked")
    private void runImpl() throws IOException, InterruptedException {
        service = FileSystems.getDefault().newWatchService();
        final var folder = PhConfig.PATH.get().getParent();
        final var file = folder.relativize(PhConfig.PATH.get());

        key = folder.register(service, StandardWatchEventKinds.ENTRY_MODIFY);

        while (true) {
            try {
                final var events = key.pollEvents();
                if (events.isEmpty()) continue;

                for (var event : events) {
                    final var path = ((WatchEvent<Path>) event).context();
                    if (path.equals(file)) {
                        if (isSaving) continue;

                        if (ignoreNextModify) {
                            ignoreNextModify = false;
                            continue;
                        }

                        Mc.schedule(() -> {
                            Photonics.LOGGER.info("Detected config change, reloading...");
                            PhConfig.reloadConfig();
                        });
                    }
                }
            } catch(Exception e) {
                Photonics.LOGGER.error("Error reloading config", e);
            } finally {
                Thread.sleep(1000);
            }
        }
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (IOException | InterruptedException e) {
            Photonics.LOGGER.error(ERROR_MESSAGE, e);

            if (key != null) {
                key.cancel();
                key = null;
            }

            if (service != null) {
                try {
                    service.close();
                    service = null;
                } catch (IOException ex) {
                    Photonics.LOGGER.error(ERROR_MESSAGE, e);
                }
            }
        }
    }


}
