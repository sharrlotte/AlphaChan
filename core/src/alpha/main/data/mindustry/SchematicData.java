package alpha.main.data.mindustry;

public class SchematicData {

    private String id;
    private String data;

    public SchematicData() {
    }

    public SchematicData(String id, String data) {
        this.id = id;
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

}
