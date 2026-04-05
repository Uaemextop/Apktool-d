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
 * Validates a decoded APK project directory for structural integrity
 * and completeness before building. Checks for required files,
 * smali directories, resources, and metadata.
 *
 * <p>Usage: {@code apktool validate [options] <project-dir>}
 */
public class ValidateCommand implements Command {

    @Override
    public String getName() {
        return "validate";
    }

    @Override
    public String getAlias() {
        return "val";
    }

    @Override
    public String getDescription() {
        return "Validate a decoded APK project before building.";
    }

    @Override
    public String getUsage() {
        return "apktool val|validate <project-dir>";
    }

    @Override
    public void addOptions(Options options, boolean advanced) {
        // No additional options for validate.
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
        String dirName;
        if (argList.isEmpty()) {
            dirName = ".";
        } else {
            dirName = argList.get(0);
        }

        File projectDir = new File(dirName);
        if (!projectDir.isDirectory()) {
            System.err.println("Project directory not found: " + dirName);
            System.exit(1);
            return;
        }

        ApkAnalyzer analyzer = new ApkAnalyzer(config);
        boolean valid = analyzer.validateProject(projectDir, System.out);

        if (!valid) {
            System.exit(1);
        }
    }
}
