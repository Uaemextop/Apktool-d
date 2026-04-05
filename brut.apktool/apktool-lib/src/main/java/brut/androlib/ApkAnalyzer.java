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
package brut.androlib;

import brut.androlib.exceptions.AndrolibException;
import brut.androlib.meta.ApkInfo;
import brut.androlib.res.Aapt2Operations;
import brut.common.Log;
import brut.directory.Directory;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;

import java.io.File;
import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

/**
 * Analyzes APK files and decoded projects to extract metadata,
 * permissions, component information, and structural validation.
 */
public class ApkAnalyzer {
    private static final String TAG = ApkAnalyzer.class.getName();

    private final Config mConfig;

    public ApkAnalyzer(Config config) {
        mConfig = config;
    }

    /**
     * Prints comprehensive APK information to the given output stream.
     *
     * @param apkFile the APK file to analyze
     * @param out     the output stream to write results to
     * @throws AndrolibException if analysis fails
     */
    public void printApkInfo(File apkFile, PrintStream out) throws AndrolibException {
        if (!apkFile.isFile() || !apkFile.canRead()) {
            throw new AndrolibException("Cannot read APK file: " + apkFile.getPath());
        }

        ExtFile extFile = new ExtFile(apkFile);
        try {
            out.println("=== APK Information ===");
            out.println("File: " + apkFile.getName());
            out.println("Size: " + formatFileSize(apkFile.length()));
            out.println();

            printApkContents(extFile, out);

            // Try aapt2 dump for detailed info.
            Aapt2Operations aapt2;
            try {
                aapt2 = new Aapt2Operations(mConfig);
            } catch (AndrolibException ex) {
                Log.d(TAG, "Could not initialize aapt2: " + ex.getMessage());
                aapt2 = null;
            }

            if (aapt2 != null) {
                try {
                    String badging = aapt2.dumpBadging(apkFile);
                    if (badging != null && !badging.isEmpty()) {
                        out.println("=== Badging Info ===");
                        out.println(badging);
                        out.println();
                    }
                } catch (AndrolibException ex) {
                    Log.d(TAG, "Could not dump badging: " + ex.getMessage());
                }

                try {
                    String permissions = aapt2.dumpPermissions(apkFile);
                    if (permissions != null && !permissions.isEmpty()) {
                        out.println("=== Permissions ===");
                        out.println(permissions);
                        out.println();
                    }
                } catch (AndrolibException ex) {
                    Log.d(TAG, "Could not dump permissions: " + ex.getMessage());
                }
            }
        } finally {
            try {
                extFile.close();
            } catch (DirectoryException ignored) {
            }
        }
    }

    /**
     * Validates a decoded APK project directory for completeness
     * and structural integrity before building.
     *
     * @param projectDir the decoded project directory
     * @param out        the output stream to write validation results
     * @return true if the project is valid, false otherwise
     */
    public boolean validateProject(File projectDir, PrintStream out) {
        boolean valid = true;
        out.println("=== Project Validation ===");
        out.println("Directory: " + projectDir.getPath());
        out.println();

        // Check apktool.yml exists.
        File apktoolYml = new File(projectDir, "apktool.yml");
        if (apktoolYml.isFile()) {
            out.println("[OK] apktool.yml found");
        } else {
            out.println("[ERROR] apktool.yml is missing");
            valid = false;
        }

        // Check AndroidManifest.xml.
        File manifest = new File(projectDir, "AndroidManifest.xml");
        if (manifest.isFile()) {
            out.println("[OK] AndroidManifest.xml found");
        } else {
            out.println("[WARN] AndroidManifest.xml is missing");
        }

        // Check sources.
        File smaliDir = new File(projectDir, "smali");
        int smaliDirCount = 0;
        if (smaliDir.isDirectory()) {
            smaliDirCount++;
        }
        File[] children = projectDir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory() && child.getName().startsWith("smali_")) {
                    smaliDirCount++;
                }
            }
        }
        if (smaliDirCount > 0) {
            out.println("[OK] " + smaliDirCount + " smali " + (smaliDirCount == 1 ? "directory" : "directories") + " found");
        } else {
            // Check for raw dex files instead.
            boolean hasDex = false;
            if (children != null) {
                for (File child : children) {
                    if (child.getName().endsWith(".dex")) {
                        hasDex = true;
                        break;
                    }
                }
            }
            if (hasDex) {
                out.println("[OK] Raw DEX file(s) found");
            } else {
                out.println("[WARN] No smali directories or DEX files found");
            }
        }

        // Check resources.
        File resDir = new File(projectDir, "res");
        if (resDir.isDirectory()) {
            File[] resDirs = resDir.listFiles();
            int resCount = resDirs != null ? resDirs.length : 0;
            out.println("[OK] res/ directory found with " + resCount + " " + (resCount == 1 ? "sub-directory" : "sub-directories"));
        } else {
            File arscFile = new File(projectDir, "resources.arsc");
            if (arscFile.isFile()) {
                out.println("[OK] Raw resources.arsc found");
            } else {
                out.println("[WARN] No res/ directory or resources.arsc found");
            }
        }

        // Check assets.
        File assetsDir = new File(projectDir, "assets");
        if (assetsDir.isDirectory()) {
            out.println("[OK] assets/ directory found");
        }

        // Check lib.
        File libDir = new File(projectDir, "lib");
        if (libDir.isDirectory()) {
            File[] archDirs = libDir.listFiles();
            if (archDirs != null) {
                Set<String> archs = new TreeSet<>();
                for (File archDir : archDirs) {
                    if (archDir.isDirectory()) {
                        archs.add(archDir.getName());
                    }
                }
                if (!archs.isEmpty()) {
                    out.println("[OK] Native libraries found for: " + String.join(", ", archs));
                }
            }
        }

        // Validate apktool.yml content.
        if (apktoolYml.isFile()) {
            try {
                ApkInfo apkInfo = ApkInfo.load(projectDir);
                out.println();
                out.println("=== Metadata ===");
                if (apkInfo.getApkFileName() != null) {
                    out.println("APK Name: " + apkInfo.getApkFileName());
                }
                if (apkInfo.getSdkInfo().getMinSdkVersion() != null) {
                    out.println("Min SDK: " + apkInfo.getSdkInfo().getMinSdkVersion());
                }
                if (apkInfo.getSdkInfo().getTargetSdkVersion() != null) {
                    out.println("Target SDK: " + apkInfo.getSdkInfo().getTargetSdkVersion());
                }
                if (apkInfo.getVersionInfo().getVersionCode() >= 0) {
                    out.println("Version Code: " + apkInfo.getVersionInfo().getVersionCode());
                }
                if (apkInfo.getVersionInfo().getVersionName() != null) {
                    out.println("Version Name: " + apkInfo.getVersionInfo().getVersionName());
                }
            } catch (AndrolibException ex) {
                out.println("[ERROR] Failed to parse apktool.yml: " + ex.getMessage());
                valid = false;
            }
        }

        out.println();
        out.println(valid ? "Validation PASSED" : "Validation FAILED");
        return valid;
    }

    private void printApkContents(ExtFile apkFile, PrintStream out) throws AndrolibException {
        try {
            Directory dir = apkFile.getDirectory();
            Set<String> files = dir.getFiles(true);

            int dexCount = 0;
            int resCount = 0;
            int assetCount = 0;
            int libCount = 0;
            int otherCount = 0;
            long totalSize = 0;

            for (String fileName : files) {
                long size = dir.getSize(fileName);
                totalSize += size;

                if (fileName.endsWith(".dex")) {
                    dexCount++;
                } else if (fileName.startsWith("res/")) {
                    resCount++;
                } else if (fileName.startsWith("assets/")) {
                    assetCount++;
                } else if (fileName.startsWith("lib/")) {
                    libCount++;
                } else {
                    otherCount++;
                }
            }

            out.println("=== Contents Summary ===");
            out.println("Total files: " + files.size());
            out.println("DEX files: " + dexCount);
            out.println("Resources: " + resCount);
            out.println("Assets: " + assetCount);
            out.println("Native libs: " + libCount);
            out.println("Other files: " + otherCount);
            out.println();
        } catch (DirectoryException ex) {
            throw new AndrolibException("Failed to read APK contents", ex);
        }
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        return String.format("%.1f MB", mb);
    }
}
