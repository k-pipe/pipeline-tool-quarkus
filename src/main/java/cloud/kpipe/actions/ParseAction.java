package cloud.kpipe.actions;

import cloud.kpipe.clparser.Command;
import cloud.kpipe.clparser.Option;
import cloud.kpipe.pipeline.Pipeline;
import com.kneissler.script.pipeline.PipelineMarkdownWithSettings;
import com.kneissler.script.pipeline.pipeline_v2.PipelineV2;
import org.jkube.application.Application;
import org.jkube.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseAction implements Action {
    @Override
    public void doAction(Command command, ActionData ad) {
        String outputFilePattern = command.getOptionValue(Constants.OUTPUT);
        List<String> inputs = command.getOptionValues(Constants.INPUT);
        Map<String,String> variables = getVariables(command.getWildcardOptions());
        List<Pipeline> parsed = new LinkedList<>();
        for (String inputPattern : inputs) {
            for (Path input : getInputFiles(inputPattern)) {
                Path output = determineOutputFile(outputFilePattern, input, ad);
                Pipeline pipeline = compile(input, output, new LinkedHashMap<>(variables), ad);
                ad.appendToManifest(output, pipeline.createManifest());
                ad.registerManifestForPipeline(pipeline.getName(), output);
                parsed.add(pipeline);
            }
        }
        ad.addParsedPipelines(parsed);
    }

    private Map<String, String> getVariables(Stream<Map.Entry<Option, List<String>>> optionValues) {
        Map<String, String> res = new TreeMap<>();
        optionValues.forEach(e -> res.put(e.getKey().getKey(), e.getValue().get(0)));
        Log.log("OptionValues: "+res);
        return res;
    }

    private List<Path> getInputFiles(String inputPattern) {
        if (inputPattern.contains(Constants.WILDCARD)) {
            Path path = Path.of(inputPattern);
            Path dir = path.getParent();
            if (dir == null) {
                // use working dir
                dir = Path.of(".");
            }
            String filename = path.getFileName().toString();
            String regex = filename
                    .replaceAll("\\.", "\\\\.")
                    .replaceAll(Constants.WILDCARD_FIND_REGEX, Constants.WILDCARD_REPLACE_REGEX);
            Pattern pattern = Pattern.compile(regex);
            Log.log("Scanning subfolders of "+dir+" for files matching regex "+regex);
            try {
                return Files.walk(dir).filter(p -> pattern.matcher(p.getFileName().toString()).matches()).collect(Collectors.toList());
            } catch (IOException e) {
                Application.fail("Exception occurred walking file tree: "+e);
                return null;
            }
        } else {
            Path input = Path.of(inputPattern);
            if (input.getParent() == null) {
                input = Path.of(".").resolve(input);
            }
            return Collections.singletonList(input);
        }
    }

    private Path determineOutputFile(String outputFilePattern, Path input, ActionData ad) {
        if (outputFilePattern.contains(Constants.WILDCARD)) {
            // wildcard specified in output
            String filename = input.getFileName().toString();
            int pos = filename.lastIndexOf('.');
            filename = filename.substring(0, pos);
            String outputFilename = outputFilePattern.replace(Constants.WILDCARD, filename);
            Path output = Path.of(outputFilename);
            if (output.getParent() == null) {
                // no directory specified in output pattern, use input file parent
                return input.getParent().resolve(output);
            } else {
                // has some path component, return the output path as it is
                return output;
            }
        } else {
            // no wildcard specified in output
            return ad.getOutputPath(outputFilePattern);
        }
    }

    private Pipeline compile(Path input, Path output, Map<String, String> parameters, ActionData ad) {
        Log.log("Compiling "+input+" to "+output);
        PipelineMarkdownWithSettings markdown = parseMarkdown(input, parameters);
        Map<String, String> variables = markdown.getVariables();
        PipelineV2 pipeline = markdown.createPipeline(variables);
        pipeline.getSteps().forEach(s -> ad.addDockerImage(s.getDockerImage()));
        List<String> pumlLines = markdown.getPipelineUMLLines();
        Map<String,String> namings = markdown.getNamings(variables);
        Log.log("Naming conventions after resolution:");
        namings.forEach((k,v) -> {
            Log.log("   "+k+"="+v);
        });
        return new Pipeline(markdown, pipeline, pumlLines, namings, variables);
    }

    private PipelineMarkdownWithSettings parseMarkdown(final Path markdownPath, Map<String, String> parameters) {
        return new PipelineMarkdownWithSettings(markdownPath.getParent(),  markdownPath,false, parameters);
    }

}
