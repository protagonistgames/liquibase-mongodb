package liquibase.ext.mongodb.statement;

/*-
 * #%L
 * Liquibase MongoDB Extension
 * %%
 * Copyright (C) 2021 Mastercard
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.nosql.statement.NoSqlExecuteStatement;
import liquibase.nosql.statement.NoSqlUpdateStatement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static liquibase.ext.mongodb.statement.AbstractRunCommandStatement.SHELL_DB_PREFIX;

@Getter
@EqualsAndHashCode(callSuper = true)
public class FindOneAndUpdateStatement extends AbstractCollectionStatement
        implements NoSqlExecuteStatement<MongoLiquibaseDatabase>, NoSqlUpdateStatement<MongoLiquibaseDatabase> {

    public static final String COMMAND_NAME = "updateLastTag";

    private final Bson filter;
    private final Bson document;
    private final Bson sort;

    public FindOneAndUpdateStatement(final String collectionName, final Bson filter, final Bson document, final Bson sort) {
        super(collectionName);
        this.filter = filter;
        this.document = document;
        this.sort = sort;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String toJs() {
        return
                SHELL_DB_PREFIX +
                        getCollectionName() +
                        "." +
                        getCommandName() +
                        "(" +
                        ofNullable(filter).map(Bson::toString).orElse(null) +
                        ", " +
                        ofNullable(document).map(Bson::toString).orElse(null) +
                        ", " +
                        ofNullable(sort).map(Bson::toString).orElse(null) +
                        ");";
    }

    @Override
    public void execute(final MongoLiquibaseDatabase database) {
        update(database);
    }

    @Override
    public int update(final MongoLiquibaseDatabase database) {
        final MongoCollection<Document> collection = database.getMongoDatabase().getCollection(getCollectionName());
        return isNull(collection.findOneAndUpdate(filter, document, new FindOneAndUpdateOptions().sort(sort))) ? 0 : 1;
    }
}
