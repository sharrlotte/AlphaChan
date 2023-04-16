package AlphaChan.main.data.user;

public interface DatabaseObject {

    public void update(Runnable cacheCleaner);

    public void delete();

}
