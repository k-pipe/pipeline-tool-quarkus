package cloud.kpipe;

import cloud.kpipe.actions.*;
import cloud.kpipe.clparser.Command;
import cloud.kpipe.clparser.CommandArgsParser;
import cloud.kpipe.clparser.Option;
import com.kneissler.util.loggedtask.LoggedTaskLog;
import io.quarkus.runtime.QuarkusApplication;
import org.jkube.application.Application;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cloud.kpipe.actions.Constants.*;
import static org.jkube.logging.Log.log;

public class Main implements QuarkusApplication {

    public int run(String... args) {
        Application.setDebug(false);
        CommandArgsParser argsParser = CommandArgsParser.create()
        .command(PARSE, "assemble pipeline definition manifest from the information in markdown files",
            Option.withValue(I, INPUT, "specifies markdown file(s), wildcard * possible")
                    .withDefault(INPUT_DEFAULT).repeatable().optional(),
            Option.withValue(O, OUTPUT, "specifies yaml file(s), wildcard * possible")
                    .withDefault(OUTPUT_DEFAULT),
            Option.catchAll("custom arguments (must be defined in markdown section 'arguments'")
        )
        .command(RUN, "create a pipeline run manifest, to execute the run use command 'apply'",
            Option.withValue(O, OUTPUT, "specifies run manifest yaml file, append to pipeline definition manifest if omitted").optional(),
            Option.withValue(P, PIPELINE, "the name of pipeline to be executed").optional(),
            Option.withValue(C, CONFIG, "the name of the run configuration to be used").optional(),
            Option.withValue(N, NAME, "the name of the pipeline run to be created").optional(),
            Option.withoutValue(T, TIMESTAMP, "if specified, the current timestamp will be appended to run name")
        )
        .command(SCHEDULE, "schedule",
            Option.withValue(O, OUTPUT, "specifies schedule manifest yaml file, append to pipeline definition manifest if omitted").optional()
        )
        .command(BUILD, "build bundled docker images")
        .command(PUSH, "push bundled docker images")
        .command(PULL, "pull all docker images (bundled, managed and generic), need to be logged in to kubernetes cluster in case there are any managed images")
        .command(LOGIN, "log in to kubernetes cluster (from markdown file, or explicitely defined)",
            Option.withValue(P, PIPELINE, "login to cluster defined in specified pipeline (if omitted, take last parsed pipeline)").optional(),
            Option.withValue(N, NAMESPACE, "switch to specified namespace (if omitted take namespace of pipeline, if blank do not login to any namespace)").optional(),
            Option.withValue(R, REGISTRY, "registry to log in for docker commands, (if omitted take definition in pipeline, if blank do not login to docker registry)").optional()
        )
        .command(APPLY, "apply created manifests (pipeline definitions, runs, schedules) on k8s cluster",
            Option.withoutValue(K, KEEP_EXISTING, "if the flag is set, an existing run with same name will be kept (i.e. no new run will be started), otherwise existing runs are deleted before creating a run with same name").optional()
        )
        .command(SIMULATE, "execute specified pipeline runs on local machine using docker",
            Option.withValue(P, PIPELINE, "the name of the pipeline to be simulated").optional(),
            Option.withValue(W, WORKDIR, "directory in host which is mounted to the pipelining-tools container under /workdir"),
            Option.withValue(S, SIMULATIONDIR, "directory relative to workdir in which simulation takes place").withDefault(DEFAULT_SIMULATION_DIR),
            Option.withValue(B, BEGIN, "optional id of the step for which simulation gets started").optional(),
            Option.withValue(E, END, "optional id of the step for which simulation ends").optional(),
            Option.withValue(C, CREDENTIALS, "the mount string for the credentials to use (format outside:inside with absolute paths)").withDefault(DEFAULT_CREDENTIALS)
        )
        .command(FOLLOW, "create a pipeline run manifest, to execute the run use command 'apply'",
                Option.withValue(T, TIMEOUT, "specifies maximum time to follow runs (using unit s/m/h)").optional()
        );
        log("Parsing commandline: "+ Arrays.stream(args).collect(Collectors.joining(" ")));
        argsParser.parse(args);
        if (argsParser.getCommands().isEmpty()) {
            argsParser.showUsage();
        } else {
            executeCommands(argsParser.getCommands());
        }
        return 0;
    }

    private void executeCommands(List<Command> commands) {
        log("Commands to be executed: ");
        commands.forEach(c -> log("   "+c.logString()));
        ActionData ad = new ActionData();
        commands.forEach(c -> execute(c, ad));
    }

    private void execute(Command command, ActionData ad) {
        Action a = null;
        switch (command.getKey()) {
            case PARSE: a = new ParseAction(); break;
            case RUN: a = new RunAction(); break;
            case SCHEDULE: a = new ScheduleAction(); break;
            case LOGIN: a = new LoginAction(); break;
            case BUILD: a = new BuildAction(); break;
            case PUSH: a = new PushAction(); break;
            case PULL: a = new PullAction(); break;
            case APPLY: a = new ApplyAction(); break;
            case SIMULATE: a = new SimulateAction(); break;
  /*          case FOLLOW: a = new FollowAction(); break;*/
        }
        if (a != null) {
            LoggedTaskLog.logHeading(command.getKey(), 1);
            a.doAction(command, ad);
        }
    }
}
