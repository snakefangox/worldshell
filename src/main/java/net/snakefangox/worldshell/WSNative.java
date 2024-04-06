package net.snakefangox.worldshell;

import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

import net.fabricmc.loader.api.FabricLoader;

public class WSNative {
    public static void loadLibrary(String string) {
        var libraryName = getOsArchString(string);
        String nativeLibResourcePath = "/natives/" + libraryName;
        var resource = WSNative.class.getResource(nativeLibResourcePath);
        var nativeLibsDir = FabricLoader.getInstance().getGameDir().resolve("natives");

        try {
            if (!Files.isDirectory(nativeLibsDir)) {
                Files.createDirectory(nativeLibsDir);
            }

            var dest = nativeLibsDir.resolve(libraryName);
            Files.deleteIfExists(dest);
            FileUtils.copyURLToFile(resource, dest.toFile());

            System.load(dest.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getOsArchString(String name) {
        String osArchString = name + "_";
        String extension = ".so";
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        String architecture = System.getProperty("os.arch").toLowerCase();

        if (operatingSystem.contains("win")) {
            osArchString += "windows";
            extension = ".dll";
        } else if (operatingSystem.contains("nix") || operatingSystem.contains("nux")
                || operatingSystem.contains("aix")) {
            osArchString += "linux";
        } else if (operatingSystem.contains("mac") || operatingSystem.contains("darwin")) {
            osArchString += "mac";
        } else {
            throw new Error("Invalid operating system: " + operatingSystem);
        }

        osArchString += "_";

        if (architecture.contains("64")) {
            osArchString += "x86_64";
        } else if (architecture.contains("32")) {
            osArchString += "x86_32";
        } else if (architecture.contains("aarch")) {
            osArchString += "arm";
        } else {
            throw new Error("Invalid processor architecture: " + architecture);
        }

        return osArchString + extension;
    }
}
