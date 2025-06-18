package pipelining;

import pipelining.clparser.Command;
import pipelining.clparser.CommandArgsParser;
import pipelining.clparser.Option;
import pipelining.util.loggedtask.LoggedTaskLog;
import io.quarkus.runtime.QuarkusApplication;
import pipelining.application.Application;
import pipelining.actions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pipelining.logging.Log.log;

public class Main implements QuarkusApplication {

    public int run(String... args) {
        Application.setDebug(false);
        CommandArgsParser argsParser = CommandArgsParser.create()
        .command(Constants.PARSE, "assemble pipeline definition manifest from the information in markdown files",
            Option.withValue(Constants.I, Constants.INPUT, "specifies markdown file(s), wildcard * possible")
                    .withDefault(Constants.INPUT_DEFAULT).repeatable().optional(),
            Option.withValue(Constants.O, Constants.OUTPUT, "specifies yaml file(s), wildcard * possible")
                    .withDefault(Constants.OUTPUT_DEFAULT),
            Option.catchAll("custom arguments (must be defined in markdown section 'arguments'")
        )
        .command(Constants.RUN, "create a pipeline run manifest, to execute the run use command 'apply'",
            Option.withValue(Constants.O, Constants.OUTPUT, "specifies run manifest yaml file, append to pipeline definition manifest if omitted").optional(),
            Option.withValue(Constants.P, Constants.PIPELINE, "the name of pipeline to be executed").optional(),
            Option.withValue(Constants.C, Constants.CONFIG, "the name of the run configuration to be used").optional(),
            Option.withValue(Constants.N, Constants.NAME, "the name of the pipeline run to be created").optional(),
            Option.withoutValue(Constants.T, Constants.TIMESTAMP, "if specified, the current timestamp will be appended to run name")
        )
        .command(Constants.SCHEDULE, "schedule",
            Option.withValue(Constants.O, Constants.OUTPUT, "specifies schedule manifest yaml file, append to pipeline definition manifest if omitted").optional()
        )
        .command(Constants.CLEAN, "clean file cache")
        .command(Constants.BUILD, "build bundled docker images")
        .command(Constants.PUSH, "push bundled docker images")
        .command(Constants.PULL, "pull all docker images (bundled, managed and generic), need to be logged in to kubernetes cluster in case there are any managed images")
        .command(Constants.LOGIN, "log in to kubernetes cluster (from markdown file, or explicitely defined)",
            Option.withValue(Constants.P, Constants.PIPELINE, "login to cluster defined in specified pipeline (if omitted, take last parsed pipeline)").optional(),
            Option.withValue(Constants.N, Constants.NAMESPACE, "switch to specified namespace (if omitted take namespace of pipeline, if blank do not login to any namespace)").optional(),
            Option.withValue(Constants.R, Constants.REGISTRY, "registry to log in for docker commands, (if omitted take definition in pipeline, if blank do not login to docker registry)").optional()
        )
        .command(Constants.APPLY, "apply created manifests (pipeline definitions, runs, schedules) on k8s cluster",
            Option.withoutValue(Constants.K, Constants.KEEP_EXISTING, "if the flag is set, an existing run with same name will be kept (i.e. no new run will be started), otherwise existing runs are deleted before creating a run with same name").optional()
        )
        .command(Constants.SIMULATE, "execute specified pipeline runs on local machine using docker",
            Option.withValue(Constants.P, Constants.PIPELINE, "the name of the pipeline to be simulated").optional(),
            Option.withValue(Constants.W, Constants.WORKDIR, "directory in host which is mounted to the pipelining-tools container under /workdir"),
            Option.withValue(Constants.S, Constants.SIMULATIONDIR, "directory relative to workdir in which simulation takes place").withDefault(Constants.DEFAULT_SIMULATION_DIR),
            Option.withValue(Constants.B, Constants.BEGIN, "optional id of the step for which simulation gets started").optional(),
            Option.withValue(Constants.E, Constants.END, "optional id of the step for which simulation ends").optional(),
            Option.withValue(Constants.C, Constants.CREDENTIALS, "the mount string for the credentials to use (format outside:inside with absolute paths)").withDefault(Constants.DEFAULT_CREDENTIALS)
        )
        .command(Constants.FOLLOW, "create a pipeline run manifest, to execute the run use command 'apply'",
                Option.withValue(Constants.T, Constants.TIMEOUT, "specifies maximum time to follow runs (using unit s/m/h)").optional()
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
            case Constants.PARSE: a = new ParseAction(); break;
            case Constants.RUN: a = new RunAction(); break;
            case Constants.SCHEDULE: a = new ScheduleAction(); break;
            case Constants.CLEAN: a = new CleanAction(); break;
            case Constants.LOGIN: a = new LoginAction(); break;
            case Constants.BUILD: a = new BuildAction(); break;
            case Constants.PUSH: a = new PushAction(); break;
            case Constants.PULL: a = new PullAction(); break;
            case Constants.APPLY: a = new ApplyAction(); break;
            case Constants.SIMULATE: a = new SimulateAction(); break;
  /*          case FOLLOW: a = new FollowAction(); break;*/
        }
        if (a != null) {
            LoggedTaskLog.logHeading(command.getKey(), 1);
            a.doAction(command, ad);
        }
    }
}
