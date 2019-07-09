package ca.edtoaster.data;

public class Artifact {
    public String name;
    public String payload;

    public Artifact(String name, String payload) {
        this.name = name;
        this.payload = payload;
    }

    public String toString() {
        return name + ": " + payload;
    }
}
