package cloud.kpipe.actions;

import cloud.kpipe.clparser.Command;
import cloud.kpipe.util.ExternalProcess;
import org.jkube.job.DockerImage;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

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
