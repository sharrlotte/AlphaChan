package alpha.main.data.mindustry;

import org.bson.Document;
import org.bson.conversions.Bson;

import alpha.main.BotConfig;
import alpha.main.BotConfig.Config;
import alpha.main.data.user.DatabaseObject;
import alpha.main.data.user.TimeObject;
import alpha.main.handler.DatabaseHandler;

public class SchematicCache extends TimeObject implements DatabaseObject {

    private SchematicData data;

    public SchematicCache(SchematicData data) {
        this.data = data;
    }

    public SchematicData getData() {
        return data;
    }

    @Override
    public void update(Runnable cacheCleaner) {
        String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);
        Bson filter = new Document().append("_id", data.getId());
        DatabaseHandler.update(null, schematicDataCollectionName, SchematicData.class, filter, data);
    }

    @Override
    public void delete() {
        String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);
        Document filter = new Document().append("_id", data.getId());
        DatabaseHandler.delete(null, schematicDataCollectionName, SchematicData.class, filter);
    }
}
