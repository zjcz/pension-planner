package dev.jonclarke.pensionplanner.export;

public enum ExportFormat {
    CSV("csv"),
    XML("xml"),
    JSON("json");

    private final String extension;

    ExportFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
