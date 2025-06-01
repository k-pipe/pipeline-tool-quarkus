package org.jkube.markdown.pipeline.v1;

import org.jkube.logging.Log;
import org.jkube.markdown.MarkdownParsingException;
import org.jkube.pipeline.definition.PipelineStep;

import java.util.List;

public class PipelinePumlParser {

    protected static final CharSequence EXTENSION_MARKER = ".";
    protected static final String DEFAULT_INPUT_ARG = "input";
    protected static final String DEFAULT_OUTPUT_ARG = "output";
    protected static final String DEFAULT_EXTENSION = ".pipe";

    protected static final String DEFAULT_FILENAME = "data";
    private static final String INPUT = "IN";
    private static final String OUTPUT = "OUT";

    public static final String STARTUML = "@startuml";
    public static final String ENDUML = "@enduml";


    public PipelinePlantUML parse(List<String> lines) {
        PipelinePlantUML res = new PipelinePlantUML(lines);
        if (!lines.get(0).equals(STARTUML))  {
            throw new MarkdownParsingException("Expected beginning of plantuml block: "+STARTUML);
        }
        if (!lines.get(lines.size()-1).equals(ENDUML))  {
            throw new MarkdownParsingException("Expected beginning of plantuml block: "+STARTUML);
        }
        for (int i = 1; i < lines.size()-1; i++) {
            String line = lines.get(i);
            Log.onException(() -> res.getLines2parsedObjects().put(line, parseLine(line, res)))
                    .rethrow("Parsing failed in line "+i+" of plantuml section: " + line);
        }
        return res;
    }

    protected Object parseLine(final String line, PipelinePlantUML res) {
        String[] split = line.trim().split(" ");
        if ((split.length != 3) && (split.length != 5))  {
            throw new MarkdownParsingException("expected 3 or 5 items in pipeline line, got "+line);
        }
        if (!split[1].startsWith("-") || !split[1].endsWith("->")) {
            throw new MarkdownParsingException("expected arrow, got "+split[1]);
        }
        String nameFrom = "";
        String nameTo = "";
        String filename = "";
        if (split.length == 5) {
            if (!split[3].equals(":")) {
                throw new MarkdownParsingException("expected :, got " + split[3]);
            }
            String[] names = split[4].split("~");
            if (names.length > 2) {
                throw new MarkdownParsingException("expected at most one ~, got multiple: " + split[4]);
            }
            String[] split2 = names[0].split("=");
            if (split2.length > 2) {
                throw new MarkdownParsingException("expected at most one =, got multiple: " + names[0]);
            }
            nameFrom = split2[0];
            if (split2.length == 2) {
                filename = split2[1];
            }
            nameTo = names.length == 1 ? nameFrom : names[1];
        }
        nameFrom = applyPipeNameDefaults(nameFrom, DEFAULT_OUTPUT_ARG);
        nameTo = applyPipeNameDefaults(nameTo, DEFAULT_INPUT_ARG);
        filename = applyPipeNameDefaults(filename, DEFAULT_FILENAME);
        PipelineStep from = parseStep(split[0], res);
        PipelineStep to = parseStep(split[2], res);
        return PipelineStep.addTransition(from, to, nameFrom, nameTo, filename);
    }

    protected String applyPipeNameDefaults(final String specifiedName, String defaultName) {
        String name = (specifiedName == null) || specifiedName.isBlank() ? defaultName : specifiedName;
        //if (!name.contains(EXTENSION_MARKER)) {
        //    name = specifiedName+EXTENSION_MARKER+PIPELINE_EXTENSION;
        //    log("No extension in filename: adding pipeline extension {} -> {}", specifiedName, name);
        //}
        return name;
    }

    protected PipelineStep parseStep(final String s, PipelinePlantUML res) {
        if (!s.startsWith("(") || !s.endsWith(")") || (s.length() < 3)) {
            throw new MarkdownParsingException("step name in round brackets, got "+s);
        }
        String id = s.substring(1, s.length()-1);
        if (id.equals(INPUT)) {
            return getOrCreateInput(res);
        }
        if (id.equals(OUTPUT)) {
            return getOrCreateOutput(res);
        }
        res.getSteps().putIfAbsent(id, new PipelineStep(id));
        return res.getSteps().get(id);
    }

    private PipelineStep getOrCreateInput( PipelinePlantUML res) {
        if (res.getInput() == null) {
            res.setInput(new PipelineStep(INPUT));
        }
        return res.getInput();
    }

    private PipelineStep getOrCreateOutput( PipelinePlantUML res) {
        if (res.getOutput() == null) {
            res.setOutput(new PipelineStep(OUTPUT));
        }
        return res.getOutput();
    }

}
