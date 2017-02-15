package com.rubenwardy.minetestmodmanager.models;

public class Events {
    private static abstract class Event {
        public String error = "";
        public boolean didError() {
            return !error.isEmpty();
        };
    }

    //
    // LIST EVENTS
    //

    private static abstract class ListEvent extends Event {
        public String list;
    }

    public final static class FetchedListEvent extends ListEvent {
        public FetchedListEvent(String list, String error) {
            this.list = list;
            this.error = error;
        }
    }

    private static abstract class ModEvent extends ListEvent {
        public String modname;
        public String dest;

        ModEvent(String modname, String dest, String list, String error) {
            this.modname = modname;
            this.dest = dest;
            this.list = list;
            this.error = error;
        }
    }

    public final static class ModInstallEvent extends ModEvent {
        public ModInstallEvent(String modname, String dest, String list, String error) {
            super(modname, dest, list, error);
        }
    }

    public final static class ModUninstallEvent extends ModEvent {
        public ModUninstallEvent(String modname, String dest, String list, String error) {
            super(modname, dest, list, error);
        }
    }

    //
    // OTHER EVENTS
    //

    public final static class SearchEvent {
        public String query;
        public SearchEvent(String query) {
            this.query = query;
        }
    }


    public final static class FetchedScreenshotEvent extends Event {
        public String filepath;
        public String modname;

        public FetchedScreenshotEvent(String modname, String filepath, String error) {
            this.modname = modname;
            this.filepath = filepath;
            this.error = error;
        }
    }
}
