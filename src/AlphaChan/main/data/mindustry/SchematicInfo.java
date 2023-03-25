package AlphaChan.main.data.mindustry;

import java.util.ArrayList;
import java.util.List;

public class SchematicInfo {

    private String id;
    private String authorId;
    private List<String> tag = new ArrayList<String>();
    private int star = -1;
    private int penguin = -1;

    public SchematicInfo() {
    }

    public SchematicInfo(String id, String authorId, List<String> tag) {
        this.id = id;
        this.authorId = authorId;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getPenguin() {
        return penguin;
    }

    public void setPenguin(int penguin) {
        this.penguin = penguin;
    }

}
