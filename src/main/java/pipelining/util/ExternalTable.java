package pipelining.util;

import pipelining.application.Application;
import pipelining.logging.Log;
import pipelining.script.pipeline.localrunner.PipelineRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ExternalTable {

    private static final String EXTENSION = ".tsv";

    public static List<String> read(String name, String url) {
        String filename = name+EXTENSION;
        String source;
        if (FileCache.exists(filename)) {
            source = FileCache.cachePath(name).toString();
        } else {
            FileCache.ensureCacheFolderExists();
            ExternalProcess proc = new ExternalProcess(Map.of())
                    .command("curl", List.of("-L", url, "-o", FileCache.cacheFile(filename).toString()))
                    .noError(".*");
            Log.log("Executing command '"+proc.toString()+"':");
            proc.execute();
            Expect.isTrue(proc.hasSucceeded()).elseFail("Could not download external table "+filename);
            source = url;
        }
        List<String> res = FileCache.read(filename);
        Log.log("Obtained "+res.size()+" lines from "+source);
        return res;

        /*
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Log.debug("Status code: " + response.statusCode());
        Log.debug("Final URL: " + response.uri());
        Log.debug("Body: \n" + response.body());
        return List.of(response.body().split("\n"));
         */
   }

}
