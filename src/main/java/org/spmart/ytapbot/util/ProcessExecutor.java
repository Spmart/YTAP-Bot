package org.spmart.ytapbot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessExecutor {

    public List<String> runProcess(final String cmdline, final String workingDirectory) {

        List<String> processOutput = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("bash", "-c", cmdline)
                    .redirectErrorStream(true)
                    .directory(new File(workingDirectory))
                    .start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                processOutput.add(line);
            }

            //There should really be a timeout here.
            process.waitFor(300, TimeUnit.SECONDS);
            processOutput.add(String.valueOf(process.exitValue())); // Always add an exit code in the end of output


        } catch (IOException e) {
            processOutput.add(String.format("I/O exception occurred when running command: %s", cmdline));
            processOutput.add(e.getMessage());
            processOutput.add("42"); // Because

        } catch (InterruptedException e) {
            processOutput.add(String.format("Interrupted exception occurred when running command: %s", cmdline));
            processOutput.add(e.getMessage());
            processOutput.add("44");
        }

        return processOutput;
    }

    public List<String> runProcess(final String cmdline) {
        return runProcess(cmdline, "./");
    }

}
