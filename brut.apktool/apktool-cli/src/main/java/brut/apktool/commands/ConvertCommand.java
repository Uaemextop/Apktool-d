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
 * Converts an APK between binary and proto resource formats using aapt2 convert.
 * Proto format is used by Android App Bundles (AAB).
 *
 * <p>Usage: {@code apktool convert [options] <apk-file>}
 */
public class ConvertCommand implements Command {

    private static final Option outputOption = Option.builder("o")
        .longOpt("output")
        .desc("Output the converted APK to <file>.")
        .hasArg()
        .argName("file")
        .required()
        .build();

    private static final Option formatOption = Option.builder()
        .longOpt("output-format")
        .desc("Output format: 'proto' or 'binary'. Default: 'proto'.")
        .hasArg()
        .argName("format")
        .build();

    @Override
    public String getName() {
        return "convert";
    }

    @Override
    public String getAlias() {
        return "con";
    }

    @Override
    public String getDescription() {
        return "Convert APK between binary and proto resource formats.";
    }

    @Override
    public String getUsage() {
        return "apktool con|convert -o <output-file> [options] <apk-file>";
    }

    @Override
    public void addOptions(Options options, boolean advanced) {
        options.addOption(outputOption);
        options.addOption(formatOption);
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

        File outputApk = new File(cli.getOptionValue(outputOption));

        String format = cli.hasOption(formatOption) ? cli.getOptionValue(formatOption) : "proto";
        boolean toProto;
        switch (format) {
            case "proto":
                toProto = true;
                break;
            case "binary":
                toProto = false;
                break;
            default:
                System.err.println("Unknown output format: " + format);
                System.err.println("Expected: 'proto' or 'binary'.");
                System.exit(1);
                return;
        }

        Aapt2Operations aapt2 = new Aapt2Operations(config);
        aapt2.convert(inputApk, outputApk, toProto);

        System.out.println("Converted APK saved to: " + outputApk.getPath());
    }
}
