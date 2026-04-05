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

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.res.Aapt2Operations;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.List;

/**
 * Optimizes an APK's resources for size and performance using aapt2 optimize.
 * Supports sparse encoding, resource name collapsing, and path shortening.
 *
 * <p>Usage: {@code apktool optimize [options] <apk-file>}
 */
public class OptimizeCommand implements Command {

    private static final Option outputOption = Option.builder("o")
        .longOpt("output")
        .desc("Output the optimized APK to <file>. (default: <input>_optimized.apk)")
        .hasArg()
        .argName("file")
        .build();

    private static final Option sparseOption = Option.builder()
        .longOpt("enable-sparse-encoding")
        .desc("Enable sparse encoding for resource tables.")
        .build();

    private static final Option collapseNamesOption = Option.builder()
        .longOpt("collapse-resource-names")
        .desc("Collapse resource names to reduce APK size.")
        .build();

    private static final Option shortenPathsOption = Option.builder()
        .longOpt("shorten-resource-paths")
        .desc("Shorten resource file paths to reduce APK size.")
        .build();

    @Override
    public String getName() {
        return "optimize";
    }

    @Override
    public String getAlias() {
        return "opt";
    }

    @Override
    public String getDescription() {
        return "Optimize APK resources for size and performance using aapt2.";
    }

    @Override
    public String getUsage() {
        return "apktool opt|optimize [options] <apk-file>";
    }

    @Override
    public void addOptions(Options options, boolean advanced) {
        options.addOption(outputOption);
        options.addOption(sparseOption);
        if (advanced) {
            options.addOption(collapseNamesOption);
            options.addOption(shortenPathsOption);
        }
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
        File inputApk = new File(apkName);
        if (!inputApk.isFile()) {
            System.err.println("APK file not found: " + apkName);
            System.exit(1);
            return;
        }

        File outputApk;
        if (cli.hasOption(outputOption)) {
            outputApk = new File(cli.getOptionValue(outputOption));
        } else {
            String baseName = apkName.endsWith(".apk")
                ? apkName.substring(0, apkName.length() - 4) : apkName;
            outputApk = new File(baseName + "_optimized.apk");
        }

        boolean sparse = cli.hasOption(sparseOption);
        boolean collapse = cli.hasOption(collapseNamesOption);
        boolean shorten = cli.hasOption(shortenPathsOption);

        Aapt2Operations aapt2 = new Aapt2Operations(config);
        aapt2.optimize(inputApk, outputApk, sparse, collapse, shorten);

        System.out.println("Optimized APK saved to: " + outputApk.getPath());
    }
}
