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

import java.util.*;

/**
 * Central registry for all available CLI commands. Maps command names
 * and aliases to their implementations, providing O(1) lookup.
 */
public final class CommandRegistry {

    private final Map<String, Command> commandMap;
    private final List<Command> commands;

    public CommandRegistry() {
        commandMap = new LinkedHashMap<>();
        commands = new ArrayList<>();
    }

    /**
     * Registers a command by its name and alias.
     *
     * @param command the command to register
     */
    public void register(Command command) {
        commands.add(command);
        commandMap.put(command.getName(), command);
        String alias = command.getAlias();
        if (alias != null && !alias.isEmpty()) {
            commandMap.put(alias, command);
        }
    }

    /**
     * Looks up a command by name or alias.
     *
     * @param nameOrAlias the command name or alias
     * @return the matching command, or {@code null} if not found
     */
    public Command getCommand(String nameOrAlias) {
        return commandMap.get(nameOrAlias);
    }

    /**
     * Returns all registered commands in registration order.
     */
    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
