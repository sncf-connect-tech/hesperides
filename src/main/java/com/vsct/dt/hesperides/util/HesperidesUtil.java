package com.vsct.dt.hesperides.util;

/**
 * Class for some util methods
 *
 * Created by tidiane_sidibe on 26/09/2016.
 */
public class HesperidesUtil {

    public static ModuleInfo moduleInfoFromPath(final String path){
        String [] parts = path.split("#");
        return new ModuleInfo(parts[parts.length - 3], parts[parts.length - 2], parts[parts.length - 1].toUpperCase().equals("RELEASE"));
    }

    public static class ModuleInfo {

        private final String name;
        private final String version;
        private final boolean release;

        public ModuleInfo(final String name, final String version, final boolean release) {
            this.name = name;
            this.version = version;
            this.release = release;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public boolean isRelease (){
            return this.release;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("ModuleInfo{");
            sb.append("name='").append(name).append('\'');
            sb.append(", version='").append(version).append('\'');
            sb.append(", release=").append(release);
            sb.append('}');
            return sb.toString();
        }
    }
}
