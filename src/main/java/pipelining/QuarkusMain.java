package pipelining;

import io.quarkus.runtime.Quarkus;

@io.quarkus.runtime.annotations.QuarkusMain
public class QuarkusMain {

    public static void main(String... args) {
        Quarkus.run(Main.class, args);
    }
}