package AlphaChan.main.handler;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.util.Log;

import static AlphaChan.AlphaChan.*;

public final class DatabaseHandler {

    public static enum Database {
        USER, GUILD, LOG, DAILY, MINDUSTRY, LIKE, DISLIKE
    }

    public static enum LogCollection {
        MESSAGE, DATABASE, MESSAGE_DELETED
    }

    private static final String DATABASE_URL = System.getenv("DATABASE_URL");

    private static DatabaseHandler instance = new DatabaseHandler();

    private static ConnectionString connectionString = new ConnectionString(DATABASE_URL);
    private static MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
            .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build();

    private static MongoClient mongoClient = MongoClients.create(settings);
    private static CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    private static CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
            fromProviders(pojoCodecProvider));

    private static ConcurrentHashMap<String, MongoDatabase> database = new ConcurrentHashMap<String, MongoDatabase>();

    private static ExecutorService logExecutor = Executors.newSingleThreadExecutor();
    private static ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    private DatabaseHandler() {

        onShutdown.connect((code) -> {
            try {
                taskExecutor.shutdown();
                taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

                logExecutor.shutdown();
                logExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                Log.error(e);
            }
        });

        Log.system("Database handler up");
    }

    public synchronized static DatabaseHandler getInstance() {
        if (instance == null)
            instance = new DatabaseHandler();
        return instance;
    }

    public static MongoDatabase getDatabase(Database name) {
        if (database.containsKey(name.name()))
            return database.get(name.name());

        MongoDatabase db = mongoClient.getDatabase(name.name()).withCodecRegistry(pojoCodecRegistry);
        database.put(name.name(), db);
        return db;
    }

    public static <T> FindIterable<T> find(Database databaseName, final String collectionName, Class<T> type,
            Bson filter) {
        MongoCollection<T> collection = getCollection(databaseName, collectionName, type);
        return collection.find(filter);
    }

    public static <T> void insert(Database databaseName, final String collectionName, Class<T> type, T value) {
        MongoCollection<T> collection = getCollection(databaseName, collectionName, type);
        collection.insertOne(value);
    }

    public static <T> boolean insertIfNotFound(Database databaseName, final String collectionName, Class<T> type,
            Bson filter, T value) {
        MongoCollection<T> collection = getCollection(databaseName, collectionName, type);

        if (collection.find(filter).first() != null)
            return false;

        collection.insertOne(value);
        return true;
    }

    public static <T> void update(Database databaseName, final String collectionName, Class<T> type, Bson filter,
            T value) {
        MongoCollection<T> collection = getCollection(databaseName, collectionName, type);
        taskExecutor.submit(() -> collection.replaceOne(filter, value, new ReplaceOptions().upsert(true)));
    }

    public static <T> void delete(Database databaseName, final String collectionName, Class<T> type, Bson filter) {
        MongoCollection<T> collection = getCollection(databaseName, collectionName, type);
        taskExecutor.submit(() -> collection.deleteOne(filter));
    }

    public static <T> long count(Database databaseName, final String collectionName, Class<T> type, Bson filter) {
        if (collectionExists(databaseName, collectionName))
            return 0;

        MongoCollection<T> collection = getCollection(databaseName, collectionName, type);

        if (filter == null)
            return collection.countDocuments();

        return collection.countDocuments(filter);
    }

    public static <T> MongoCollection<T> getCollection(Database databaseName, final String collectionName,
            Class<T> type) {
        if (!collectionExists(databaseName, collectionName)) {
            createCollection(databaseName, collectionName);
        }

        return getDatabase(databaseName).getCollection(collectionName, type);
    }

    // Check if collection exists
    public static boolean collectionExists(MongoDatabase Database, final String collectionName) {
        MongoCursor<String> collectionNames = Database.listCollectionNames().iterator();
        String name;
        while (collectionNames.hasNext()) {
            name = collectionNames.next();
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean collectionExists(Database databaseName, final String collectionName) {
        return collectionExists(getDatabase(databaseName), collectionName);
    }

    public static void createCollection(Database databaseName, final String collectionName) {
        getDatabase(databaseName).createCollection(collectionName);

        log(LogCollection.DATABASE, "CREATE GUILD", collectionName);
    }

    public static void log(LogCollection collection, String task, String content) {
        log(collection, new Document(task, content));
    }

    public static void log(LogCollection collection, Document content) {
        logExecutor.submit(new Logger(collection, content));
    }

    public static class Logger implements Runnable {

        private final LogCollection collection;
        private final Document content;

        public Logger(LogCollection collection, Document content) {
            this.collection = collection;
            this.content = content;
        }

        @Override
        public void run() {
            MongoCollection<Document> logCollection = getCollection(Database.LOG, collection.name(), Document.class);
            int maxLogCount = BotConfig.readInt(Config.MAX_LOG_COUNT, 8000);
            long count = logCollection.estimatedDocumentCount();

            if (count > maxLogCount) {
                while (count > maxLogCount - 1000) {
                    logCollection.deleteOne(new Document());
                    count--;
                }
                Log.system("Deleted log for collection " + collection.name() + ": " + count);
            }

            // Insert collection message
            logCollection.insertOne(
                    content.append(BotConfig.readString(Config.TIME_INSERT, "_timeInsert"),
                            new BsonDateTime(System.currentTimeMillis())));
        }
    }
}
