package AlphaChan.main.data.mindustry;

import java.util.ArrayList;
import java.util.List;

public class SchematicInfo {

    private String id;
    private String authorId;
    private List<String> tag = new ArrayList<String>();
    private long like = -1;
    private long dislike = -1;

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

    public long getLike() {
        return like;
    }

    public void setLike(long like) {
        this.like = like;
    }

    public long getDislike() {
        return dislike;
    }

    public void setDislike(long dislike) {
        this.dislike = dislike;
    }
}
