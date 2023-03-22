package AlphaChan.main.gui;

import AlphaChan.main.handler.UpdatableHandler;
import engine.Window;

public class Console {

    public Console() {

        Window window = new Window(500, 300, "Console");
        UpdatableHandler.run("CONSOLE", 0, () -> window.run());
    }

}
