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
package brut.androlib.res;

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.common.BrutException;
import brut.common.Log;
import brut.util.OS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides standalone aapt2 operations beyond compile and link.
 * Each method wraps a specific aapt2 sub-command, encapsulating
 * the command-line argument construction and error handling.
 */
public class Aapt2Operations {
    private static final String TAG = Aapt2Operations.class.getName();

    private final String mAaptPath;

    /**
     * Creates a new instance using the given configuration to locate the aapt2 binary.
     *
     * @param config the configuration (may contain a custom aapt2 path)
     * @throws AndrolibException if the aapt2 binary cannot be located
     */
    public Aapt2Operations(Config config) throws AndrolibException {
        String path = config.getAaptBinary();
        if (path == null || path.isEmpty()) {
            try {
                path = AaptManager.getBinaryFile().getPath();
            } catch (AndrolibException ex) {
                path = AaptManager.getBinaryName();
                Log.w(TAG, path + ": " + ex.getMessage() + " (defaulting to $PATH binary)");
            }
        }
        mAaptPath = path;
    }

    /**
     * Dumps the resource table from an APK file.
     *
     * @param apkFile the APK file to dump
     * @return the dump output as a string
     * @throws AndrolibException if aapt2 execution fails
     */
    public String dumpResources(File apkFile) throws AndrolibException {
        return executeDump("resources", apkFile, false);
    }

    /**
     * Dumps the AndroidManifest.xml from an APK file.
     *
     * @param apkFile the APK file to dump
     * @return the dump output as a string
     * @throws AndrolibException if aapt2 execution fails
     */
    public String dumpManifest(File apkFile) throws AndrolibException {
        return executeDump("xmltree", apkFile, true);
    }

    /**
     * Dumps the string pool from an APK file.
     *
     * @param apkFile the APK file to dump
     * @return the dump output as a string
     * @throws AndrolibException if aapt2 execution fails
     */
    public String dumpStrings(File apkFile) throws AndrolibException {
        return executeDump("strings", apkFile, false);
    }

    /**
     * Dumps APK configuration information (densities, locales, etc.).
     *
     * @param apkFile the APK file to dump
     * @return the dump output as a string
     * @throws AndrolibException if aapt2 execution fails
     */
    public String dumpConfigurations(File apkFile) throws AndrolibException {
        return executeDump("configurations", apkFile, false);
    }

    /**
     * Dumps the badging information (label, icon, permissions) from an APK.
     *
     * @param apkFile the APK file to dump
     * @return the dump output as a string
     * @throws AndrolibException if aapt2 execution fails
     */
    public String dumpBadging(File apkFile) throws AndrolibException {
        return executeDump("badging", apkFile, false);
    }

    /**
     * Dumps the permissions declared in an APK.
     *
     * @param apkFile the APK file to dump
     * @return the dump output as a string
     * @throws AndrolibException if aapt2 execution fails
     */
    public String dumpPermissions(File apkFile) throws AndrolibException {
        return executeDump("permissions", apkFile, false);
    }

    /**
     * Optimizes an APK's resources for size and performance.
     *
     * @param inputApk          the source APK file
     * @param outputApk         the optimized output APK file
     * @param enableSparseEncoding  whether to enable sparse resource encoding
     * @param collapseResourceNames whether to collapse resource names
     * @param shortenResourcePaths  whether to shorten resource file paths
     * @throws AndrolibException if aapt2 execution fails
     */
    public void optimize(File inputApk, File outputApk, boolean enableSparseEncoding,
                         boolean collapseResourceNames, boolean shortenResourcePaths)
            throws AndrolibException {
        List<String> cmd = new ArrayList<>();
        cmd.add(mAaptPath);
        cmd.add("optimize");

        cmd.add("-o");
        cmd.add(outputApk.getPath());

        if (enableSparseEncoding) {
            cmd.add("--enable-sparse-encoding");
        }
        if (collapseResourceNames) {
            cmd.add("--collapse-resource-names");
        }
        if (shortenResourcePaths) {
            cmd.add("--shorten-resource-paths");
        }

        cmd.add(inputApk.getPath());

        try {
            OS.exec(cmd.toArray(new String[0]));
            Log.i(TAG, "Optimized APK saved to: " + outputApk.getPath());
        } catch (BrutException ex) {
            throw new AndrolibException("Failed to optimize APK: " + ex.getMessage(), ex);
        }
    }

    /**
     * Converts an APK between binary and proto resource formats.
     *
     * @param inputApk  the source APK file
     * @param outputApk the converted output APK file
     * @param toProto   if true, convert to proto format; if false, convert to binary
     * @throws AndrolibException if aapt2 execution fails
     */
    public void convert(File inputApk, File outputApk, boolean toProto) throws AndrolibException {
        List<String> cmd = new ArrayList<>();
        cmd.add(mAaptPath);
        cmd.add("convert");

        cmd.add("-o");
        cmd.add(outputApk.getPath());

        if (toProto) {
            cmd.add("--output-format");
            cmd.add("proto");
        } else {
            cmd.add("--output-format");
            cmd.add("binary");
        }

        cmd.add(inputApk.getPath());

        try {
            OS.exec(cmd.toArray(new String[0]));
            Log.i(TAG, "Converted APK saved to: " + outputApk.getPath());
        } catch (BrutException ex) {
            throw new AndrolibException("Failed to convert APK: " + ex.getMessage(), ex);
        }
    }

    /**
     * Retrieves the aapt2 version string.
     *
     * @return the version string
     * @throws AndrolibException if aapt2 execution fails
     */
    public String getVersion() throws AndrolibException {
        String result = OS.execAndReturn(new String[]{ mAaptPath, "version" });
        if (result == null) {
            throw new AndrolibException("Could not execute aapt2 binary at: " + mAaptPath);
        }
        return result;
    }

    private String executeDump(String subCommand, File apkFile, boolean isXmlTree)
            throws AndrolibException {
        List<String> cmd = new ArrayList<>();
        cmd.add(mAaptPath);
        cmd.add("dump");
        cmd.add(subCommand);

        if (isXmlTree) {
            cmd.add("--file");
            cmd.add("AndroidManifest.xml");
        }

        cmd.add(apkFile.getPath());

        String result = OS.execAndReturn(cmd.toArray(new String[0]));
        if (result == null) {
            throw new AndrolibException("aapt2 dump " + subCommand + " returned no output.");
        }
        Log.d(TAG, "aapt2 dump " + subCommand + " completed.");
        return result;
    }
}
