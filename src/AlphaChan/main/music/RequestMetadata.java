package AlphaChan.main.music;

import net.dv8tion.jda.api.entities.Member;

public class RequestMetadata {

    public static final RequestMetadata EMPTY = new RequestMetadata(null);

    public final UserInfo user;

    public RequestMetadata(Member user) {
        this.user = user == null ? null
                : new UserInfo(user.getIdLong(), user.getEffectiveName(),
                        user.getEffectiveAvatarUrl());
    }

    public long getOwner() {
        return user == null ? 0L : user.id;
    }

    public String getRequester() {
        return user == null ? null : user.username;
    }

    public class RequestInfo {
        public final String query, url;

        private RequestInfo(String query, String url) {
            this.query = query;
            this.url = url;
        }
    }

    public class UserInfo {
        public final long id;
        public final String username, avatar;

        private UserInfo(long id, String username, String avatar) {
            this.id = id;
            this.username = username;
            this.avatar = avatar;
        }
    }
}
