/*
 *  Copyright (C) 2010 Ryszard Wiśniewski <brut.alll@gmail.com>
 *  Copyright (C) 2010 Connor Tumbleson <connor.tumbleson@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package brut.apktool.commands;

import brut.androlib.ApkAnalyzer;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.List;

/**
 * Displays comprehensive information about an APK file including
 * its contents summary, badging info, and permissions.
 *
 * <p>Usage: {@code apktool info [options] <apk-file>}
 */
public class InfoCommand implements Command {

    private static final Option outputOption = Option.builder("o")
        .longOpt("output")
        .desc("Write info output to <file> instead of stdout.")
        .hasArg()
        .argName("file")
        .build();

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getAlias() {
        return "i";
    }

    @Override
    public String getDescription() {
        return "Display detailed information about an APK file.";
    }

    @Override
    public String getUsage() {
        return "apktool i|info [options] <apk-file>";
    }

    @Override
    public void addOptions(Options options, boolean advanced) {
        options.addOption(outputOption);
    }

    @Override
    public void execute(String[] args, Config config) throws AndrolibException {
        Options options = new Options();
        addOptions(options, true);

        CommandLine cli;
        try {
            cli = new DefaultParser(false).parse(options, args, false);
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            System.err.println("Usage: " + getUsage());
            System.exit(1);
            return;
        }

        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input APK file was not specified.");
            System.err.println("Usage: " + getUsage());
            System.exit(1);
            return;
        }

        String apkName = argList.get(0);
        File apkFile = new File(apkName);

        ApkAnalyzer analyzer = new ApkAnalyzer(config);

        if (cli.hasOption(outputOption)) {
            File outFile = new File(cli.getOptionValue(outputOption));
            try (java.io.PrintStream ps = new java.io.PrintStream(outFile)) {
                analyzer.printApkInfo(apkFile, ps);
            } catch (java.io.FileNotFoundException ex) {
                throw new AndrolibException("Cannot write to output file: " + outFile.getPath(), ex);
            }
        } else {
            analyzer.printApkInfo(apkFile, System.out);
        }
    }
}
