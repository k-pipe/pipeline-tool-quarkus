package cloud.kpipe.actions;

import cloud.kpipe.clparser.Command;
import org.jkube.logging.Log;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class ScheduleAction implements Action {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss");

    @Override
    public void doAction(Command command, ActionData ad) {
        ad.getSchedules().forEach((pipeline, schedules) -> {
            String pipelineName = pipeline.getName();
            Path output = command.getOptionalOptionValue(Constants.OUTPUT).map(ad::getOutputPath).orElse(ad.getManifest(pipelineName));
            schedules.forEach(s -> {
                Log.log("Creating manifest for schedule "+s.getName()+" of pipeline "+pipelineName+" in "+output);
                ad.appendToManifest(output, s.createManifest(pipeline));
            });
        });
    }

}
