package org.robotframework.ide.core.testData.text;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class MainTest {

    public static void main(String[] args) throws IOException {
        // System.out.println("t  t".split("([ ]{2,})")[0]);
        long start = System.nanoTime();
        for (int i = 0; i < 1; i++) {
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(
                            "D:\\userdata\\wypych\\Desktop\\RTest\\F\\D.robot"),
                    "utf-8");
            TxtRobotFileParser parser = new TxtRobotFileParser();
            System.out.println(parser.parse(reader));
            reader.close();
        }
        long end = System.nanoTime();

        System.out.println(end - start);
    }


    public static void main2(String[] args) throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < 2000000; i++) {
            TxtRobotFileLexer lexer = new TxtRobotFileLexer();
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(
                            "D:\\userdata\\wypych\\Desktop\\RTest\\F\\D.robot"),
                    "utf-8");
            List<RobotToken> recognizedTokens = lexer.recognizeTokens(reader);
            reader.close();
            recognizedTokens = null;
            // for (RobotToken t : recognizedTokens) {
            // System.out.println(t);
            // }

        }
        long end = System.nanoTime();
        System.out.println(end - start);
    }
}
