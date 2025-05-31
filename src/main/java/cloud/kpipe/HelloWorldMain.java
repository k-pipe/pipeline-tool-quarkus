package cloud.kpipe;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.util.List;

public class HelloWorldMain implements QuarkusApplication {
    @Override
    public int run(String... args) throws Exception {
        System.out.println("Hello " + List.of(args));
        return 0;
    }
}