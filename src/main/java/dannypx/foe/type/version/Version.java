package dannypx.foe.type.version;

import org.jspecify.annotations.NonNull;

public class Version implements Comparable<Version> {

    private final String version;

    public final String get() {
        return this.version;
    }

    public Version(String version) {
        if(version == null)
            throw new IllegalArgumentException("Version can not be null");

        int buildDelimPos = version.indexOf('+');
        if (buildDelimPos >= 0) {
            version = version.substring(0, buildDelimPos);
        }

        int preReleaseDelimPos = version.indexOf('-');
        if (preReleaseDelimPos >= 0) {
            version = version.substring(0, preReleaseDelimPos);
        }

        if(!version.matches("[0-9]+(\\.[0-9]+)*"))
            throw new IllegalArgumentException("Invalid version format");

        this.version = version;
    }

    public static Version of(String s) {
        return new Version(s.trim());
    }

    @Override
    public int compareTo(@NonNull Version that) {
        String[] thisParts = this.get().split("\\.");
        String[] thatParts = that.get().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for(int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if(thisPart < thatPart)
                return -1;
            if(thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object that) {
        if(this == that)
            return true;
        if(that == null)
            return false;
        if(this.getClass() != that.getClass())
            return false;
        return this.compareTo((Version) that) == 0;
    }

    @Override
    public String toString() {
        return version;
    }
}