package AlphaChan.main.data.user;

import AlphaChan.main.event.Signal;

public abstract class TimeObject {

    private final int ALIVE_TIME;

    private int time = 0;

    protected Signal<Integer> onTimeOut = new Signal<>();

    public TimeObject(final int aliveTime) {
        this.ALIVE_TIME = aliveTime;
        this.time = ALIVE_TIME;
    }

    public boolean isAlive() {

        if (time > 0) {
            return true;
        }

        onTimeOut.emit(0);
        return false;
    }

    public boolean isAlive(int n) {
        this.time -= n;
        return isAlive();
    }

    public void resetTimer() {
        time = ALIVE_TIME;
    }

    public void killTimer() {
        this.time = 0;
    }

    public int getTime() {
        return time;
    }

    public abstract void update();
}
