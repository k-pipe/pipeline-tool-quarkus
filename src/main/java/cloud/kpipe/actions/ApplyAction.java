package cloud.kpipe.actions;

import cloud.kpipe.clparser.Command;
import cloud.kpipe.util.ExternalProcess;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplyAction implements Action {

    @Override
    public void doAction(Command command, ActionData ad) {
        if (!command.isFlagSet(Constants.KEEP_EXISTING)) {
            deleteExistingRuns(ad.getRunsByNamespace());
        }
        apply(ad.getManifests());
    }

    private void deleteExistingRuns(Map<String, Set<String>> runsByNamespace) {
        runsByNamespace.forEach((namespace,runIds) -> {
            deleteRuns(namespace, getExistingRuns(namespace, runIds));
        });
    }

    private void deleteRuns(String namespace, List<String> runIds) {
        runIds.forEach(runId -> {
            Log.log("Deleting existing run "+runId);
            List<String> args = new LinkedList<>();
            args.add("delete");
            args.add("pipelinerun");
            args.add(runId);
            args.add("-n");
            args.add(namespace);
            kubectl(args, "delete pipeline run");
        });
    }

    private List<String> getExistingRuns(String namespace, Set<String> runIds) {
        List<String> args = new LinkedList<>();
        args.add("get");
        args.add("pipelinerun");
        args.add("-n");
        args.add(namespace);
        args.add("-o");
        args.add("custom-columns=:metadata.name");
        return kubectl(args,"get pipeline runs").stream().filter(runIds::contains).collect(Collectors.toList());
    }

    private void apply(List<String> manifests) {
        List<String> args = new LinkedList<>();
        args.add("apply");
        args.add("-f");
        manifests.forEach(args::add);
        kubectl(args, "apply manifests");
    }

    private List<String> kubectl(List<String> args, String action) {
        ExternalProcess proc = new ExternalProcess(Map.of()).command("kubectl", args);
        Log.log("Executing command: "+proc.toString());
        proc.execute();
        Expect.isTrue(proc.hasSucceeded()).elseFail("Could not "+action);
        return proc.getOutput();
    }

}
