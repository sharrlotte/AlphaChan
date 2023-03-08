package AlphaChan.main.handler;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.concurrent.ConcurrentHashMap;

import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.util.Log;

public final class DatabaseHandler {

    public static enum DATABASE {
        USER, GUILD, LOG, DAILY, MINDUSTRY, STAR, PENGUIN
    }

    public static enum LOG_TYPE {
        MESSAGE, DATABASE, USER, MESSAGE_DELETED
    }

    private static DatabaseHandler instance = new DatabaseHandler();

    private static String DATABASE_URL = System.getenv("DATABASE_URL");
    private static ConnectionString connectionString = new ConnectionString(DATABASE_URL);
    private static MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
            .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build();

    private static MongoClient mongoClient = MongoClients.create(settings);

    private static CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    private static CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
            fromProviders(pojoCodecProvider));

    private static ConcurrentHashMap<String, MongoDatabase> database = new ConcurrentHashMap<String, MongoDatabase>();

    private DatabaseHandler() {
        Log.system("Database handler up");
    }

    public static DatabaseHandler getInstance() {
        if (instance == null)
            instance = new DatabaseHandler();
        return instance;
    }

    public static MongoDatabase getDatabase(DATABASE name) {
        if (database.containsKey(name.name()))
            return database.get(name.name());
        MongoDatabase db = mongoClient.getDatabase(name.name()).withCodecRegistry(pojoCodecRegistry);
        database.put(name.name(), db);
        return db;
    }

    public static <T> MongoCollection<T> getCollection(DATABASE databaseName, final String collectionName,
            Class<T> type) {
        if (!DatabaseHandler.collectionExists(databaseName, collectionName)) {
            DatabaseHandler.createCollection(databaseName, collectionName);
        }

        return DatabaseHandler.getDatabase(databaseName).getCollection(collectionName, type);
    }

    // Check if collection exists
    public static boolean collectionExists(MongoDatabase database, final String collectionName) {
        try {
            MongoCursor<String> collectionNames = database.listCollectionNames().iterator();
            String name;
            while (collectionNames.hasNext()) {
                name = collectionNames.next();
                if (name.equalsIgnoreCase(collectionName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    public static boolean collectionExists(DATABASE databaseName, final String collectionName) {
        return collectionExists(getDatabase(databaseName), collectionName);
    }

    public static void createCollection(DATABASE databaseName, final String collectionName) {
        getDatabase(databaseName).createCollection(collectionName);
        log(LOG_TYPE.DATABASE, new Document().append("CREATE GUILD", collectionName));
    }

    public static void log(LOG_TYPE log, Document content) {
        UpdatableHandler.run("LOG " + log.name(), 0, () -> {
            // Create collection if it doesn't exist
            MongoDatabase logDatabase = getDatabase(DATABASE.LOG);
            if (!collectionExists(logDatabase, log.name()))
                logDatabase.createCollection(log.name());

            MongoCollection<Document> collection = logDatabase.getCollection(log.name(), Document.class);
            long count = collection.estimatedDocumentCount();
            int maxLogCount = BotConfig.readInt(Config.MAX_LOG_COUNT, 10000);

            if (count > maxLogCount) {
                while (count > maxLogCount - 1000) {
                    collection.deleteOne(new Document());
                    count--;
                    Log.system("Delete log: " + count);
                }
            }
            // Insert log message
            collection.insertOne(
                    content.append(BotConfig.readString(Config.TIME_INSERT, "_timeInsert"),
                            new BsonDateTime(System.currentTimeMillis())));

        });
    }
}
