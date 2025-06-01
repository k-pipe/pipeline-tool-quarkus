package com.kneissler.script.pipeline.pipeline_v2;

import org.jkube.logging.Log;
import org.jkube.markdown.MarkdownParsingException;
import org.jkube.markdown.pipeline.v1.PipelinePumlParser;
import org.jkube.pipeline.definition.PipelineConnector;
import org.jkube.pipeline.definition.PipelineStep;

import java.util.List;

public class PipelinePumlParserV2 extends PipelinePumlParser {

    private static final String FAILED_PIPE = "FAILED"+EXTENSION_MARKER+DEFAULT_EXTENSION;
    private static final String BATCH_ARROW_TYPE = "[BATCHED]";
    private static final String COMBINED_ARROW_TYPE = "[COMBINED]";
    private static final String BATCHED_PIPE_FROM = "batches.txt";
    private static final String BATCHED_PIPE_TO = "batch.pipe";
    private static final String PLANT_UML_COMMENT_PREFIX = "'";
    private static final String PLANT_UML_DEFINITION_PREFIX = "!";
    private static final String ARROW_BEGIN = "-";
    private static final String ARROW_END = ">";

    public PipelinePlantUmlV2 parse(List<String> lines) {
        PipelinePlantUmlV2 res = new PipelinePlantUmlV2(lines);
        if (!lines.get(0).equals(STARTUML))  {
            throw new MarkdownParsingException("Expected beginning of plantuml block: "+STARTUML);
        }
        if (!lines.get(lines.size()-1).equals(ENDUML))  {
            throw new MarkdownParsingException("Expected beginning of plantuml block: "+STARTUML);
        }
        for (int i = 1; i < lines.size()-1; i++) {
            String line = lines.get(i);
            Log.onException(() -> res.getLines2parsedObjects().put(line, parseLineV2(line, res)))
                    .rethrow("Parsing failed in line "+i+" of plantuml section: " + line);
        }
        return res;
    }

    protected Object parseLineV2(final String line, PipelinePlantUmlV2 res) {
        String trimmedline = removeComment(line.trim());
        if (trimmedline.isBlank()) {
            return null;
        }
        String[] split = trimmedline.split(" ");
        if (parseFrameStart(split, res) || parseFrameEnd(split, res)) {
            return null;
        }
        Object umlObject = parseNode(split, res);
        if (umlObject != null) {
            return umlObject;
        }
        umlObject = parseTransition(split, res);
        if (umlObject != null) {
            return umlObject;
        }
        throw new MarkdownParsingException("Could not parse this line: "+line);
    }

    private PipelineStep parseNode(final String[] split, PipelinePlantUmlV2 res) {
        if (split.length != 1) {
            return null;
        }
        PipelineStep step = parseStep(split[0], res);
        res.getFrameHandler().assignToFrame(step, true);
        return step;
    }


    public String removeComment(String line) {
        return line.startsWith(PLANT_UML_COMMENT_PREFIX) || line.startsWith(PLANT_UML_DEFINITION_PREFIX) ? "" : line;
    }

    private boolean parseFrameStart(final String[] split, PipelinePlantUmlV2 res) {
        if (split.length != 3) {
            return false;
        }
        if (("frame".equals(split[0])) && "{".equals(split[2])) {
            res.getFrameHandler().openFrame(split[1]);
            return true;
        }
        return false;
    }

    private boolean parseFrameEnd(final String[] split, PipelinePlantUmlV2 res) {
        if (split.length != 1) {
            return false;
        }
        if ("}".equals(split[0])) {
            res.getFrameHandler().closeFrame();
            return true;
        }
        return false;
    }

    private Object parseTransition(final String[] split, PipelinePlantUmlV2 res) {
        if ((split.length != 3) && (split.length != 5))  {
            return null;
        }
        String arrowType = getArrowType(split[1]);
        if (arrowType == null) {
            throw new MarkdownParsingException("got '"+split[1]+"' instead of arrow");
        }
        while(arrowType.startsWith("-")) {
            arrowType = arrowType.substring(1);
        }
        while(arrowType.endsWith("-")) {
            arrowType = arrowType.substring(0, arrowType.length()-1);
        }
        boolean batched = arrowType.startsWith(BATCH_ARROW_TYPE);
        boolean combined = arrowType.startsWith(COMBINED_ARROW_TYPE);
        boolean regularTransition = split[0].startsWith("(");
        String nameFrom = batched ? BATCHED_PIPE_FROM : regularTransition ? DEFAULT_OUTPUT_ARG : FAILED_PIPE;
        String nameTo = batched ? BATCHED_PIPE_TO : regularTransition ? DEFAULT_INPUT_ARG : FAILED_PIPE;
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
            if (split2.length == 1) {
                // extension only specified
                filename = DEFAULT_FILENAME+EXTENSION_MARKER+split2[0];
            } else {
                nameFrom = split2[0];
                filename = split2[1];
                nameTo = names.length == 1 ? nameFrom : names[1];
            }
        }
        nameFrom =  applyPipeNameDefaults(nameFrom,  regularTransition ? DEFAULT_OUTPUT_ARG : FAILED_PIPE);
        nameTo = applyPipeNameDefaults(nameTo, regularTransition ? DEFAULT_INPUT_ARG : FAILED_PIPE);
        filename = applyPipeNameDefaults(filename, DEFAULT_FILENAME);
        PipelineStep to = parseStep(split[2], res);
        if (regularTransition) {
            PipelineStep from = parseStep(split[0], res);
            PipelineConnector connector = addTransition(from, to, nameFrom, nameTo, filename, res );
            if (batched) {
                if (res.getBatchedConnector() != null) {
                    throw new MarkdownParsingException("got multiple batched connectors");
                }
                res.setBatchedConnector(connector);
                System.out.println("Found batched transition: "+from.getId()+" --> "+to.getId());
            }
            if (combined) {
                res.getCombinedTransitions().add(connector);
            }
            return connector;
        } else {
            return res.getFrameHandler().setCatch(split[0], to);
        }
    }

    public static PipelineConnector addTransition(final PipelineStep from, final PipelineStep to, final String nameFrom, final String nameTo, final String filename, PipelinePlantUmlV2 res) {
        PipelineConnector connector = new PipelineConnector(from, to, nameFrom, nameTo, filename);
        res.connectors().add(connector);
        return connector;
    }

    private String getArrowType(String s) {
        if (!s.startsWith(ARROW_BEGIN) || !s.endsWith(ARROW_END) || (s.length() < ARROW_BEGIN.length()+ARROW_END.length())) {
            return null;
        }
        return s.substring(ARROW_BEGIN.length(), s.length()-ARROW_END.length());
    }

}
