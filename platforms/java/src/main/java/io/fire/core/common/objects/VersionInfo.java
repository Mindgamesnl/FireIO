package io.fire.core.common.objects;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class VersionInfo {

    @Getter private Boolean release = true;   // switch between development or a production release
    @Getter private int coreVersion  = 133;    // version of this release/build
    @Getter private int protocolVersion = 132; // release number since last major protocol change

    @Override
    public String toString() {
        return this.release.toString() + "," + this.coreVersion + "," + this.protocolVersion;
    }

    public VersionInfo fromString(String string) {
        String[] parts = string.split(",");
        this.release = Boolean.valueOf(parts[0]);
        this.coreVersion = Integer.parseInt(parts[1]);
        this.protocolVersion = Integer.parseInt(parts[2]);
        return this;
    }

}
