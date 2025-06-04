package pipelining.actions;

import pipelining.clparser.Command;

public interface Action {
    void doAction(Command command, ActionData ad);
}
