package org.spmart.ytapbot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Builds and executes shell process.
 */
public class ProcessExecutor {

    /**
     * Runs new process with bash -c command. Dies in 300 seconds, if hangs.
     * @param cmdline Command to execute with bash.
     * @param workingDirectory Directory from which the process will be launched
     * @return Process output with an exit code.
     */
    public List<String> runProcess(final String cmdline, final String workingDirectory) {
        final String IO_EXCEPTION_CODE = "42";
        final String INTERRUPTED_EXCEPTION_CODE = "44";
        final int TIMEOUT = 300;  // Timeout in seconds. After timeout process must die.

        final String SHELL = "bash";
        final String SHELL_ARGS = "-c";

        List<String> processOutput = new ArrayList<>();
        try {
            Process process = new ProcessBuilder(SHELL, SHELL_ARGS, cmdline)
                    .redirectErrorStream(true)
                    .directory(new File(workingDirectory))
                    .start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                processOutput.add(line);
            }

            process.waitFor(TIMEOUT, TimeUnit.SECONDS);  // Start process
            processOutput.add(String.valueOf(process.exitValue())); // Always add an exit code in the end of output

        } catch (IOException e) {
            processOutput.add(String.format("I/O exception occurred when running command: %s", cmdline));
            processOutput.add(e.getMessage());
            processOutput.add(IO_EXCEPTION_CODE); // Because

        } catch (InterruptedException e) {
            processOutput.add(String.format("Interrupted exception occurred when running command: %s", cmdline));
            processOutput.add(e.getMessage());
            processOutput.add(INTERRUPTED_EXCEPTION_CODE);
        }

        return processOutput;
    }

    public List<String> runProcess(final String cmdline) {
        return runProcess(cmdline, "./");
    }

}
