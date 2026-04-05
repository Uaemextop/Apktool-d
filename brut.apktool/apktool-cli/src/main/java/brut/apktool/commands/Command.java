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
import org.apache.commons.cli.Options;

/**
 * Interface for all CLI commands. Each command encapsulates its own
 * option definitions, argument parsing, and execution logic.
 */
public interface Command {

    /**
     * Returns the primary name of this command (e.g. "decode", "build").
     */
    String getName();

    /**
     * Returns short alias(es) for this command (e.g. "d" for decode).
     */
    String getAlias();

    /**
     * Returns a brief description of what this command does.
     */
    String getDescription();

    /**
     * Returns the usage pattern for this command (e.g. "[options] &lt;apk-file&gt;").
     */
    String getUsage();

    /**
     * Populates the given {@link Options} with this command's specific options.
     *
     * @param options   the options container to populate
     * @param advanced  whether to include advanced options
     */
    void addOptions(Options options, boolean advanced);

    /**
     * Executes this command with the given arguments and configuration.
     *
     * @param args   the command-line arguments (after the command name)
     * @param config the shared configuration instance
     * @throws AndrolibException if the command fails
     */
    void execute(String[] args, Config config) throws AndrolibException;
}
