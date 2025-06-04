package cloud.kpipe.actions;

import cloud.kpipe.clparser.Command;
import cloud.kpipe.util.ExternalProcess;
import org.jkube.job.DockerImage;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.util.List;
import java.util.Map;

public class PushAction implements Action {

    @Override
    public void doAction(Command command, ActionData ad) {
        for (DockerImage di : ad.getDockerImages()) {
            if (di.isBundled()) {
                docker(di.getPath(), "push", di.getImageWithTag());
            }
        }
    }

    private void docker(String directory, String... args) {
        ExternalProcess proc = new ExternalProcess(Map.of())
                .command("docker", List.of(args))
                .dir(directory)
                .noError(".*");
        Log.log("Executing command '"+proc.toString()+"' in directory: "+directory);
        proc.execute();
        Expect.isTrue(proc.hasSucceeded()).elseFail("Could not push docker image in folder "+directory);
    }

}
