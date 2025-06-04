package pipelining.actions;

import pipelining.clparser.Command;
import pipelining.pipeline.Pipeline;
import pipelining.util.ExternalProcess;
import pipelining.logging.Log;
import pipelining.util.Expect;

import java.util.List;
import java.util.Map;

public class LoginAction implements Action {

    @Override
    public void doAction(Command command, ActionData ad) {
        String pipelineName = command.getOptionalOptionValue(Constants.PIPELINE).orElseGet(() -> ad.getLatestParsedPipeline().getName());
        Pipeline pipeline = ad.findParsedPipeline(pipelineName);
        String k8scluster = getNaming(pipeline, "k8s-cluster");
        String k8sproject =  getNaming(pipeline, "k8s-project");
        String region =  getNaming(pipeline, "region");
        gcloud("kubeconfig entry generated", "container", "clusters",  "get-credentials", k8scluster, "--region", region, "--project", k8sproject);
        String namespace = command.getOptionalOptionValue(Constants.NAMESPACE).orElse(pipeline.getNamespace());
        if (!namespace.isBlank()) {
            kubectl("config", "set-context", "--current", "--namespace="+namespace);
        }
        String registry = command.getOptionalOptionValue(Constants.REGISTRY).orElse(getNaming(pipeline,"registry"));
        if (!registry.isBlank()) {
            gcloud("Docker configuration file updated.", "auth", "configure-docker", registry, "-q");
        }
    }

    private String getNaming(Pipeline pipeline, String key) {
        String res = pipeline.getNamings().get(key);
        Expect.notNull(res).elseFail("Naming convention missing: "+key);
        return res;
    }

    private void gcloud(String successMarker, String... args) {
        ExternalProcess proc = new ExternalProcess(Map.of())
                .command("gcloud", List.of(args))
                .errorMarker("ERROR")
                .warning("WARNING")
                .noError("")
                .successMarker(successMarker);
        Log.log("Executing command '"+proc.toString()+"'");
        proc.execute();
        Expect.isTrue(proc.hasSucceeded()).elseFail("Could not execute gcloud command");
    }

    private void kubectl(String... args) {
        ExternalProcess proc = new ExternalProcess(Map.of())
                .command("kubectl", List.of(args));
        Log.log("Executing command '"+proc.toString()+"'");
        proc.execute();
        Expect.isTrue(proc.hasSucceeded()).elseFail("Could not switch to namespace");
    }
}
