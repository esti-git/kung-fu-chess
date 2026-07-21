package input;

import engine.GameEngine;

import javax.swing.Timer;

public class GameLoop {

    private static final int TICK_MS = 16;
    private static final int RESTART_DELAY_MS = 3000;

    private final GameEngine engine;
    private final Runnable onTick;
    private Runnable restartAction;

    private Timer timer;
    private long lastSystemTime;
    private boolean gameOverHandled;

    public GameLoop(GameEngine engine, Runnable onTick) {
        this.engine = engine;
        this.onTick = onTick;
    }

    public void setRestartAction(Runnable restartAction) {
        this.restartAction = restartAction;
    }

    public void start() {
        lastSystemTime = System.currentTimeMillis();
        timer = new Timer(TICK_MS, e -> tick());
        timer.start();
    }

    private void tick() {
        if (engine.isGameOver()) {
            if (!gameOverHandled) {
                gameOverHandled = true;
                scheduleRestart();
            }
        } else {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - lastSystemTime;
            lastSystemTime = currentTime;
            engine.advanceClock(elapsed);
        }

        onTick.run();
    }

    private void scheduleRestart() {
        Timer restartTimer = new Timer(RESTART_DELAY_MS, e -> {
            if (restartAction != null) {
                restartAction.run();
            }
            lastSystemTime = System.currentTimeMillis();
            gameOverHandled = false;
        });
        restartTimer.setRepeats(false);
        restartTimer.start();
    }
}
