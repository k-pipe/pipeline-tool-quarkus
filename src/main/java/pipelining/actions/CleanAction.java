package pipelining.actions;

import pipelining.clparser.Command;
import pipelining.logging.Log;
import pipelining.util.FileCache;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class CleanAction implements Action {

    @Override
    public void doAction(Command command, ActionData ad) {
        FileCache.clean();
    }

}
