package org.example.testing.extension

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer
import java.net.ServerSocket
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest

class DynamoDbLocalClientExtension : BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    companion object {
        lateinit var dynamoDbClient: DynamoDbClient
        lateinit var dynamoDbEnhancedClient: DynamoDbEnhancedClient
        private val initialized = AtomicBoolean()

        fun <T> createTable(
            clazz: Class<T>,
            tableName: String,
            createTableRequest: CreateTableRequest,
        ): DynamoDbTable<T> {
            val describeTableRequest = DescribeTableRequest
                .builder()
                .tableName(tableName)
                .build()

            val waiter = dynamoDbClient.waiter()
            waiter.waitUntilTableNotExists(describeTableRequest)
            println("creating table")
            dynamoDbClient.createTable(createTableRequest)
            waiter.waitUntilTableExists(describeTableRequest)

            println("table created")

            return dynamoDbEnhancedClient.table(tableName, TableSchema.fromImmutableClass(clazz))
        }

        fun deleteTable(tableName: String) {
            val waiter = dynamoDbClient.waiter()

            val describeTableRequest = DescribeTableRequest
                .builder()
                .tableName(tableName)
                .build()

            val deleteTableRequest = DeleteTableRequest
                .builder()
                .tableName(tableName)
                .build()

            waiter.waitUntilTableExists(describeTableRequest)
            println("deleting table")
            dynamoDbClient.deleteTable(deleteTableRequest)
            waiter.waitUntilTableNotExists(describeTableRequest)
            println("table deleted")
        }
    }

    private lateinit var dynamoDbServer: DynamoDBProxyServer

    override fun beforeAll(context: ExtensionContext) {
        if (initialized.compareAndSet(false, true)) {
            println("Hello! Starting DynamoDB Project")

            // The following line registers a callback hook when the root test context is shut down
            context.root.getStore(GLOBAL).put(this::class.qualifiedName, this)

            val port = ServerSocket(0, 0).use { it.localPort.toString() }

            println("Start Init server")

            dynamoDbServer =
                ServerRunner
                    .createServerFromCommandLineArgs(arrayOf("-inMemory", "-port", port))
            dynamoDbServer.start()

            println("Start Init client")

            dynamoDbClient =
                DynamoDbClient.builder()
                    .endpointOverride(URI("http://localhost:$port"))
                    .region(Region.US_WEST_2)
                    .httpClient(UrlConnectionHttpClient.builder().build())
                    .credentialsProvider(
                        StaticCredentialsProvider
                            .create(AwsBasicCredentials.create("dummyKey", "dummySecret")),
                    )
                    .build()

            println("Start Init enhanced")

            dynamoDbEnhancedClient =
                DynamoDbEnhancedClient
                    .builder()
                    .dynamoDbClient(dynamoDbClient)
                    .build()

            println("Hello! Started DynamoDB Project")
        }
    }

    override fun close() {
        println("Hello! Finishing DynamoDB Project")
        dynamoDbClient.close()
        dynamoDbServer.stop()
        println("Hello! Finished DynamoDB Project")
    }
}
