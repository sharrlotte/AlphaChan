package AlphaChan.main.data.mindustry;

import org.bson.Document;
import org.bson.conversions.Bson;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.data.user.DatabaseObject;
import AlphaChan.main.data.user.TimeObject;
import AlphaChan.main.handler.DatabaseHandler;

public class SchematicCache extends TimeObject implements DatabaseObject {

    private SchematicData data;

    public SchematicCache(SchematicData data) {
        this.data = data;
    }

    public SchematicData getData() {
        return data;
    }

    @Override
    public void update() {
        if (isAlive()) {
            String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);
            Bson filter = new Document().append("_id", data.getId());
            DatabaseHandler.update(null, schematicDataCollectionName, SchematicData.class, filter, data);
        }
    }

    @Override
    public void delete() {
        if (isAlive()) {
            String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);
            Document filter = new Document().append("_id", data.getId());
            DatabaseHandler.delete(null, schematicDataCollectionName, SchematicData.class, filter);
            killTimer();
        }
    }
}
