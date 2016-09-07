package ru.flashsafe.client.util;

import ru.flashsafe.client.util.os.OSArch;
import ru.flashsafe.client.util.os.OSType;

/**
 * Utils for check current OS
 * @author Alexander Krysin
 */
public class OSUtil {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String ARCH = System.getProperty("os.arch").toLowerCase();

    public static OSType getOSType() {
        if(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) {
            return OSType.LINUX;
        } else if(OS.indexOf("mac") >= 0) {
            return OSType.MACOS;
        } else if(OS.indexOf("win") >= 0) {
            return OSType.WINDOWS;
        } else {
            return OSType.UNKNOWN;
        }
    }

    public static OSArch getOSArch() {
        if(ARCH.indexOf("86") >= 0) {
            return OSArch.X86;
        } else if(ARCH.indexOf("64") >= 0 || ARCH.indexOf("powerpc") >= 0 || ARCH.indexOf("pc") >= 0) {
            return OSArch.X64;
        } else {
            return OSArch.UNKNOWN;
        }
    }
}
