package pipelining.actions;

import pipelining.clparser.Command;
import pipelining.util.ExternalProcess;
import pipelining.logging.Log;
import pipelining.util.Expect;

import java.util.List;
import java.util.Map;

public class PullAction implements Action {

    @Override
    public void doAction(Command command, ActionData ad) {
        for (String image : ad.getDockerImageNames(false)) {
            docker( "pull", image);
        }
    }

    private void docker(String... args) {
        ExternalProcess proc = new ExternalProcess(Map.of())
                .command("docker", List.of(args))
                .noError(".*");
        Log.log("Executing command '"+proc.toString()+"'");
        proc.execute();
        Expect.isTrue(proc.hasSucceeded()).elseFail("Could not pull docker image");
    }

}
