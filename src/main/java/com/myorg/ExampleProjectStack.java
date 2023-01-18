package com.myorg;

import software.constructs.Construct;

import java.nio.file.Path;
import java.nio.file.Paths;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.appsync.DynamoDbDataSource;
import software.amazon.awscdk.services.appsync.GraphqlApi;
import software.amazon.awscdk.services.appsync.SchemaFile;
import software.amazon.awscdk.services.cognito.CfnIdentityPool;
import software.amazon.awscdk.services.cognito.CfnUserPool;
import software.amazon.awscdk.services.cognito.CfnUserPoolGroup;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.s3.Bucket;

public class ExampleProjectStack extends Stack {
    public ExampleProjectStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public ExampleProjectStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current absolute path is: " + s);

        GraphqlApi graphqlApi = GraphqlApi.Builder.create(this, "TSPAPI")
                .name("TSP")
                .schema(SchemaFile
                .fromAsset(System.getProperty("user.dir") + "/src/main/java/com/myorg/appsync/schema.graphql"))
                .build();

        Table table = Table.Builder.create(this, "tsp-ddb-table")
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .readCapacity(1)
                .writeCapacity(1)
                .removalPolicy(RemovalPolicy.DESTROY).build();

        Bucket bucket = Bucket.Builder.create(this, "tsp-s3-bucket")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        DynamoDbDataSource ddb = graphqlApi
                .addDynamoDbDataSource("tsp-data-source", table);

        CfnUserPool cfnUserPool = CfnUserPool.Builder
                .create(this, "tsp-user-pool")
                .userPoolName("tspUserPool152638")
                .build();

        CfnUserPoolGroup cfnUserPoolGroup = CfnUserPoolGroup.Builder
                .create(this, "tsp-user-pool-group")
                .userPoolId(cfnUserPool.getRef())
                .description("group1")
                .groupName("group1")
                .build();
    }
}
