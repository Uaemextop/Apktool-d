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
 * Dumps resource table information from an APK file using aapt2 dump.
 * Supports multiple dump modes: resources, manifest, strings, configurations.
 *
 * <p>Usage: {@code apktool dump [options] <apk-file>}
 */
public class DumpResourcesCommand implements Command {

    private static final Option modeOption = Option.builder("m")
        .longOpt("mode")
        .desc("Dump mode: 'resources', 'manifest', 'strings', 'configurations', 'badging', 'permissions'.\n"
            + "Default: 'resources'.")
        .hasArg()
        .argName("mode")
        .build();

    @Override
    public String getName() {
        return "dump";
    }

    @Override
    public String getAlias() {
        return "du";
    }

    @Override
    public String getDescription() {
        return "Dump resource information from an APK using aapt2.";
    }

    @Override
    public String getUsage() {
        return "apktool du|dump [options] <apk-file>";
    }

    @Override
    public void addOptions(Options options, boolean advanced) {
        options.addOption(modeOption);
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
        if (!apkFile.isFile()) {
            System.err.println("APK file not found: " + apkName);
            System.exit(1);
            return;
        }

        String mode = cli.hasOption(modeOption) ? cli.getOptionValue(modeOption) : "resources";
        Aapt2Operations aapt2 = new Aapt2Operations(config);

        String result;
        switch (mode) {
            case "resources":
                result = aapt2.dumpResources(apkFile);
                break;
            case "manifest":
                result = aapt2.dumpManifest(apkFile);
                break;
            case "strings":
                result = aapt2.dumpStrings(apkFile);
                break;
            case "configurations":
                result = aapt2.dumpConfigurations(apkFile);
                break;
            case "badging":
                result = aapt2.dumpBadging(apkFile);
                break;
            case "permissions":
                result = aapt2.dumpPermissions(apkFile);
                break;
            default:
                System.err.println("Unknown dump mode: " + mode);
                System.err.println("Expected: 'resources', 'manifest', 'strings', 'configurations', 'badging', 'permissions'.");
                System.exit(1);
                return;
        }

        System.out.println(result);
    }
}
