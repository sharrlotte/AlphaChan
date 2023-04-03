package AlphaChan.main.data.user;

import AlphaChan.main.event.Signal;

public abstract class TimeObject {

    private final int ALIVE_TIME;

    private int time = 0;
    private boolean isKilled = false;

    protected Signal<Integer> onTimeOut = new Signal<>();
    protected Signal<Integer> onUpdate = new Signal<>();

    public TimeObject() {
        this(15);
    }

    public TimeObject(final int aliveTime) {
        this.ALIVE_TIME = aliveTime;
        this.time = aliveTime;
    }

    public boolean isAlive() {
        if (isKilled == true)
            return false;

        if (time > 0)
            return true;

        onTimeOut.emit(0);
        isKilled = true;

        return false;
    }

    public boolean isAlive(int n) {
        time -= n;
        return isAlive();
    }

    public void resetTimer() {
        time = ALIVE_TIME;
    }

    public void kill() {
        isKilled = true;
        time = 0;
    }

    public int getTime() {
        return time;
    }
}
