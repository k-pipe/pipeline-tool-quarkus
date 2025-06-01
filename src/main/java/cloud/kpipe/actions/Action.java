package cloud.kpipe.actions;

import cloud.kpipe.clparser.Command;

public interface Action {
    void doAction(Command command, ActionData ad);
}
