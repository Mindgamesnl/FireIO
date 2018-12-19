package io.fire.core.common.objects;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class VersionInfo {

    /**
     * Version info is used to prevent a miss-match from happening with an outdated server/client
     */

    @Getter private Boolean release = true;   // switch between development or a production release
    @Getter private int coreVersion  = 1365;    // version of this release/build
    @Getter private int protocolVersion = 1354; // release number since last major protocol change


    /**
     * Serialize the version info
     *
     * @return string
     */
    @Override
    public String toString() {
        return this.release.toString() + "," + this.coreVersion + "," + this.protocolVersion;
    }


    /**
     * De-serialize/parse version info from a string
     *
     * @param string
     * @return
     */
    public VersionInfo fromString(String string) {
        String[] parts = string.split(",");
        this.release = Boolean.valueOf(parts[0]);
        this.coreVersion = Integer.parseInt(parts[1]);
        this.protocolVersion = Integer.parseInt(parts[2]);
        return this;
    }

}
