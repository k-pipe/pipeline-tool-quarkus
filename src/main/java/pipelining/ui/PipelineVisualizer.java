package pipelining.ui;

import pipelining.script.pipeline.pipeline_v2.CatchFrame;
import pipelining.ui.domain.StepState;
import pipelining.util.pantuml.GeneratePlantUML;
import pipelining.application.Application;
import pipelining.logging.Log;
import pipelining.pipeline.definition.PipelineConnector;
import pipelining.pipeline.definition.PipelineStep;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineVisualizer {

    private final static String INIT = "@startuml\nhide stereotype\nskinparam UsecaseBackgroundColor<<PENDING>> white\nskinparam UsecaseFontColor<<PENDING>> gray\nskinparam UsecaseBorderColor<<PENDING>> grey\nskinparam UsecaseBackgroundColor<<RUNNING>> deepSkyBlue\nskinparam UsecaseFontColor<<RUNNING>> black\nskinparam UsecaseBorderColor<<RUNNING>> blue\nskinparam UsecaseBackgroundColor<<SUCCESS>> palegreen\nskinparam UsecaseFontColor<<SUCCESS>> black\nskinparam UsecaseBorderColor<<SUCCESS>> green\nskinparam UsecaseBackgroundColor<<ERROR>> salmon\nskinparam UsecaseFontColor<<ERROR>> black\nskinparam UsecaseBorderColor<<ERROR>> red\nskinparam FrameBackgroundColor ghostwhite\nskinparam FrameBorderColor darkred\nskinparam ArrowColor black";
    private final static String END = "@enduml";
    private final Map<String, Object> umlWithObjects;
    private final Map<PipelineStep, StepState> stepState = new HashMap<>();
    private final Map<PipelineStep, Long> failedCount = new HashMap<>();
    private final Map<PipelineConnector, String> itemsCount = new HashMap<>();

    public PipelineVisualizer(Map<String, Object> umlWithObjects) {
        this.umlWithObjects = umlWithObjects;
    }

    public void setState(PipelineStep step, StepState state) {
        Log.log("Set state {} for step {}", state, step.getId());
        if (state != null) {
            stepState.put(step, state);
        } else {
            stepState.remove(step);
        }
    }

    public byte[] getImage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generateImage(baos);
        return baos.toByteArray();
    }

    private void generateImage(OutputStream out) {
        StringBuilder plantUml = new StringBuilder();
        plantUml.append(INIT);
        umlWithObjects.forEach((line, obj) -> plantUml.append(createLine(line, obj)+"\n"));
        plantUml.append(END);
        //System.out.println("UML: "+plantUml.toString());
        GeneratePlantUML.generatePNG(plantUml.toString(), out);
    }

    private String createLine(String line, Object object) {
        if (object == null) {
            return line;
        }
        if (object instanceof PipelineStep) {
            return createLineForStep((PipelineStep) object);
        }
        if (object instanceof PipelineConnector) {
            return createLineForConnector(line, (PipelineConnector) object);
        }
        if (object instanceof CatchFrame) {
            return createLineFroCatchFrame(line, (CatchFrame) object);
        }
        return Application.fail("unexpected markdown object of class "+object.getClass());
    }

    private String createLineForStep(PipelineStep step) {
        StepState state = stepState.get(step);
        String stateString = state == null ? "PENDING" : state.toString();
        return node(step)+" <<"+stateString+">>";
    }

    private String createLineForConnector(String line, PipelineConnector connector) {
        String num = itemsCount.get(connector);
        String suffix = num == null ? " #grey" : " : "+num;
        return node(connector.getSource())+arrow(line)+node(connector.getTarget())+suffix;
    }

    private String arrow(String line) {
        return line.trim().split(" ")[1];
    }

    private String createLineFroCatchFrame(String line, CatchFrame frame) {
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (PipelineStep step : frame.getSteps()) {
            Long count = failedCount.get(step);
            if (count != null) {
                found = true;
                if (count != 0) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(step.getId()+":"+count);
                }
             }
        }
        if (sb.length() == 0) {
            sb.append("0");
        }
        return line+(found ? " : "+sb.toString() : " #grey");
    }

    private String node(PipelineStep step) {
        return "("+step.getId()+")";
    }

    public String getImageType() {
        return "image/png";
    }

    public void setNumItems(String numString, List<PipelineConnector> connectors) {
        connectors.forEach(c -> setNumItems(c, numString));
    }

    public void setNumItems(PipelineConnector connector, String numString) {
        Log.log("Set num items {} for connector {} step {}", numString, connector.getNameAtSource(), connector.getSource().getId());
        itemsCount.put(connector, numString);
    }

    public void setNumFailed(PipelineStep step, long num) {
        Log.log("Set num failed {} for step {}", num, step.getId());
        failedCount.put(step, num);
    }

    public void storeImage(Path imageFile) {
        try (FileOutputStream out = new FileOutputStream(imageFile.toFile())) {
            generateImage(out);
        } catch (IOException e) {
            Application.fail("Could not store image file");
        }
    }
}
