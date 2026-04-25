package tn.esprit.Pidev3A49.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Text-to-Speech service using Windows SAPI via PowerShell.
 * No external dependencies required.
 */
public class CoachTtsService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "coach-tts");
        t.setDaemon(true);
        return t;
    });

    private Future<?> currentTask;
    private volatile double volume = 1.0; // 0.0 to 1.0
    private volatile boolean enabled = true;

    /**
     * Speak the given text asynchronously. Cancels any ongoing speech first.
     */
    public void speak(String text) {
        if (!enabled || text == null || text.isBlank()) return;
        cancelCurrent();
        currentTask = executor.submit(() -> {
            try {
                // Map volume 0.0-1.0 to SAPI volume 0-100
                int sapiVolume = (int) Math.round(volume * 100);
                // Escape single quotes for PowerShell
                String safe = text.replace("'", " ").replace("\"", " ");
                String script = String.format(
                    "Add-Type -AssemblyName System.Speech; " +
                    "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$s.Volume = %d; " +
                    "$s.Rate = 1; " +
                    "$s.Speak('%s');",
                    sapiVolume, safe
                );
                ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-NoProfile", "-NonInteractive", "-Command", script
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // TTS unavailable — silently ignore
            }
        });
    }

    /** Stop any ongoing speech immediately. */
    public void cancelCurrent() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
    }

    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) cancelCurrent();
    }

    public void shutdown() {
        cancelCurrent();
        executor.shutdownNow();
    }
}
