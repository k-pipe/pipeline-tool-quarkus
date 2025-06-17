package pipelining.util;

import pipelining.job.Run;
import pipelining.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ExternalTable {

    private final static String CACHE_DIR = Run.DOCKER_WORKDIR+"/.cache";
    private static final String EXTENSION = ".tsv";

    public static List<String> read(String filename, String url) {
        Path path = Path.of(CACHE_DIR, filename+EXTENSION);
        File file = path.toFile();
        String source;
        if (file.exists()) {
            source = path.toString();
        } else {
            file.getParentFile().mkdirs();
            ExternalProcess proc = new ExternalProcess(Map.of())
                    .command("curl", List.of("-L", url, "-o", file.toString()))
                    .noError(".*");
            Log.log("Executing command '"+proc.toString()+"':");
            proc.execute();
            Expect.isTrue(proc.hasSucceeded()).elseFail("Could not download external table "+filename);
            source = url;
        }
        try {
            List<String> res = Files.readAllLines(path);
            Log.log("Obtained "+res.size()+" lines from "+source);
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
