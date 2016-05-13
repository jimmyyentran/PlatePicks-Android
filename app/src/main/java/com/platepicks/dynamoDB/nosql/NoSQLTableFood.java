package com.platepicks.dynamoDB.nosql;

import android.content.Context;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.util.ThreadUtils;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.platepicks.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class NoSQLTableFood extends NoSQLTableBase {
    private static final String LOG_TAG = NoSQLTableFood.class.getSimpleName();

    /** Condition demo key for Primary Queries with filter conditions. */
    private static final String DEMO_PRIMARY_CONDITION_KEY = "dislike";
    /** Condition demo value for Queries with filter conditions. */
    private static final String DEMO_PRIMARY_CONDITION_VALUE = "1111500000";

    /** Condition demo key for Secondary Queries with filter conditions. */
    private static final String DEMO_SECONDARY_CONDITION_KEY = "businessId";
    /** Condition demo value for Queries with filter conditions. */
    private static final String DEMO_SECONDARY_CONDITION_VALUE = "demo-" + DEMO_SECONDARY_CONDITION_KEY + "-500000";

    private static final String DEMO_PARTITION_KEY = "foodId";
    /** The Primary Partition Key Name.  All queries must use an equality condition on this key. */
    /** Partition/Hash demo value for Get and Queries. */
    private static final String DEMO_PARTITION_VALUE = "demo-" + DEMO_PARTITION_KEY + "-3";
    private String getDemoPartitionValue() {
        return DEMO_PARTITION_VALUE;
    }
    private String getDemoPartitionValueText() {
        return DEMO_PARTITION_VALUE;
    }
    /** The Primary Sort Key Name. */
    private static final String DEMO_SORT_KEY = "businessId";
    /** Sort/Range demo value for Get and Queries. */
    private static final String DEMO_SORT_VALUE = "demo-" + DEMO_SORT_KEY + "-500000";
    private static final String DEMO_SORT_VALUE_TEXT = DEMO_SORT_VALUE;

    /********* Primary Get Query Inner Classes *********/

    public class GetWithPartitionKeyAndSortKey extends NoSQLOperationBase {
        private FoodDO result;
        private boolean resultRetrieved = true;

        GetWithPartitionKeyAndSortKey(final Context context) {
            super(context.getString(R.string.nosql_operation_get_by_partition_and_sort_text),
                String.format(context.getString(R.string.nosql_operation_example_get_by_partition_and_sort_text),
                    DEMO_PARTITION_KEY, getDemoPartitionValueText(),
                    DEMO_SORT_KEY, DEMO_SORT_VALUE_TEXT));
        }

        @Override
        public boolean executeOperation() {
            // Retrieve an item by passing the partition key using the object mapper.
            result = mapper.load(FoodDO.class, getDemoPartitionValue(), DEMO_SORT_VALUE);

            if (result != null) {
                resultRetrieved = false;
                return true;
            }
            return false;
        }

        @Override
        public List<NoSQLResult> getNextResultGroup() {
            if (resultRetrieved) {
                return null;
            }
            final List<NoSQLResult> results = new ArrayList<>();
            results.add(new NoSQLFoodResult(result));
            resultRetrieved = true;
            return results;
        }

        @Override
        public void resetResults() {
            resultRetrieved = false;
        }
    }

    /* ******** Primary Index Query Inner Classes ******** */

    public class QueryWithPartitionKeyAndSortKeyCondition extends NoSQLOperationBase {

        private PaginatedQueryList<FoodDO> results;
        private Iterator<FoodDO> resultsIterator;

        QueryWithPartitionKeyAndSortKeyCondition(final Context context) {
            super(context.getString(R.string.nosql_operation_title_query_by_partition_and_sort_condition_text),
                  String.format(context.getString(R.string.nosql_operation_example_query_by_partition_and_sort_condition_text),
                      DEMO_PARTITION_KEY, getDemoPartitionValueText(),
                      DEMO_SORT_KEY, DEMO_SORT_VALUE_TEXT));
        }

        @Override
        public boolean executeOperation() {
            final FoodDO itemToFind = new FoodDO();
            itemToFind.setFoodId(getDemoPartitionValue());

            final Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())
                .withAttributeValueList(new AttributeValue().withS(DEMO_SORT_VALUE));
            final DynamoDBQueryExpression<FoodDO> queryExpression = new DynamoDBQueryExpression<FoodDO>()
                .withHashKeyValues(itemToFind)
                .withRangeKeyCondition(DEMO_SORT_KEY, rangeKeyCondition)
                .withConsistentRead(false)
                .withLimit(RESULTS_PER_RESULT_GROUP);

            results = mapper.query(FoodDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Gets the next page of results from the query.
         * @return list of results, or null if there are no more results.
         */
        public List<NoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    public class QueryWithPartitionKeyOnly extends NoSQLOperationBase {

        private PaginatedQueryList<FoodDO> results;
        private Iterator<FoodDO> resultsIterator;

        QueryWithPartitionKeyOnly(final Context context) {
            super(context.getString(R.string.nosql_operation_title_query_by_partition_text),
                String.format(context.getString(R.string.nosql_operation_example_query_by_partition_text),
                    DEMO_PARTITION_KEY, getDemoPartitionValueText()));
        }

        @Override
        public boolean executeOperation() {
            final FoodDO itemToFind = new FoodDO();
            itemToFind.setFoodId(getDemoPartitionValue());

            final DynamoDBQueryExpression<FoodDO> queryExpression = new DynamoDBQueryExpression<FoodDO>()
                .withHashKeyValues(itemToFind)
                .withConsistentRead(false)
                .withLimit(RESULTS_PER_RESULT_GROUP);

            results = mapper.query(FoodDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<NoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    public class QueryWithPartitionKeyAndFilter extends NoSQLOperationBase {

        private PaginatedQueryList<FoodDO> results;
        private Iterator<FoodDO> resultsIterator;

        QueryWithPartitionKeyAndFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_query_by_partition_and_filter_text),
                  String.format(context.getString(R.string.nosql_operation_example_query_by_partition_and_filter_text),
                      DEMO_PARTITION_KEY, getDemoPartitionValueText(),
                      DEMO_PRIMARY_CONDITION_KEY, DEMO_PRIMARY_CONDITION_VALUE));
        }

        @Override
        public boolean executeOperation() {
            final FoodDO itemToFind = new FoodDO();
            itemToFind.setFoodId(getDemoPartitionValue());

            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#dislike", DEMO_PRIMARY_CONDITION_KEY);

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":Mindislike",
                new AttributeValue().withN(DEMO_PRIMARY_CONDITION_VALUE));

            final DynamoDBQueryExpression<FoodDO> queryExpression = new DynamoDBQueryExpression<FoodDO>()
                .withHashKeyValues(itemToFind)
                .withFilterExpression("#dislike > :Mindislike")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues)
                .withConsistentRead(false)
                .withLimit(RESULTS_PER_RESULT_GROUP);

            results = mapper.query(FoodDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<NoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public void resetResults() {
             resultsIterator = results.iterator();
         }
    }

    public class QueryWithPartitionKeySortKeyConditionAndFilter extends NoSQLOperationBase {

        private PaginatedQueryList<FoodDO> results;
        private Iterator<FoodDO> resultsIterator;

        QueryWithPartitionKeySortKeyConditionAndFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_query_by_partition_sort_condition_and_filter_text),
                  String.format(context.getString(R.string.nosql_operation_example_query_by_partition_sort_condition_and_filter_text),
                      DEMO_PARTITION_KEY, getDemoPartitionValueText(),
                      DEMO_SORT_KEY, DEMO_SORT_VALUE_TEXT,
                      DEMO_PRIMARY_CONDITION_KEY, DEMO_PRIMARY_CONDITION_VALUE));
        }

        public boolean executeOperation() {
            final FoodDO itemToFind = new FoodDO();
            itemToFind.setFoodId(getDemoPartitionValue());

            final Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())
                .withAttributeValueList(new AttributeValue().withS(DEMO_SORT_VALUE));

            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#dislike", DEMO_PRIMARY_CONDITION_KEY);

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":Mindislike",
                new AttributeValue().withN(DEMO_PRIMARY_CONDITION_VALUE));

            final DynamoDBQueryExpression<FoodDO> queryExpression = new DynamoDBQueryExpression<FoodDO>()
                .withHashKeyValues(itemToFind)
                .withRangeKeyCondition(DEMO_SORT_KEY, rangeKeyCondition)
                .withFilterExpression("#dislike > :Mindislike")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues)
                .withConsistentRead(false)
                .withLimit(RESULTS_PER_RESULT_GROUP);

            results = mapper.query(FoodDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<NoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    /* ******** Secondary Named Index Query Inner Classes ******** */

    /********* Scan Inner Classes *********/

    public class ScanWithFilter extends NoSQLOperationBase {

        private PaginatedScanList<FoodDO> results;
        private Iterator<FoodDO> resultsIterator;

        ScanWithFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_scan_with_filter),
                String.format(context.getString(R.string.nosql_operation_example_scan_with_filter),
                    DEMO_PRIMARY_CONDITION_KEY, DEMO_PRIMARY_CONDITION_VALUE));
        }

        @Override
        public boolean executeOperation() {
            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#dislike", DEMO_PRIMARY_CONDITION_KEY);

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":Mindislike",
                new AttributeValue().withN(DEMO_PRIMARY_CONDITION_VALUE));
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#dislike > :Mindislike")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues);

            results = mapper.scan(FoodDO.class, scanExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<NoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public boolean isScan() {
            return true;
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    public class ScanWithoutFilter extends NoSQLOperationBase {

        private PaginatedScanList<FoodDO> results;
        private Iterator<FoodDO> resultsIterator;

        ScanWithoutFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_scan_without_filter),
                context.getString(R.string.nosql_operation_example_scan_without_filter));
        }

        @Override
        public boolean executeOperation() {
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            results = mapper.scan(FoodDO.class, scanExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<NoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public boolean isScan() {
            return true;
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    /**
     * Helper Method to handle retrieving the next group of query results.
     * @param resultsIterator the iterator for all the results (makes a new service call for each result group).
     * @return the next list of results.
     */
    private static List<NoSQLResult> getNextResultsGroupFromIterator(final Iterator<FoodDO> resultsIterator) {
        if (!resultsIterator.hasNext()) {
            return null;
        }
        List<NoSQLResult> resultGroup = new LinkedList<>();
        int itemsRetrieved = 0;
        do {
            // Retrieve the item from the paginated results.
            final FoodDO item = resultsIterator.next();
            // Add the item to a group of results that will be displayed later.
            resultGroup.add(new NoSQLFoodResult(item));
            itemsRetrieved++;
        } while ((itemsRetrieved < RESULTS_PER_RESULT_GROUP) && resultsIterator.hasNext());
        return resultGroup;
    }

    /** Inner classes use this value to determine how many results to retrieve per service call. */
    private final static int RESULTS_PER_RESULT_GROUP = 40;
    /** Removing sample data removes the items in batches of the following size. */
    private static final int MAX_BATCH_SIZE_FOR_DELETE = 50;

    /** The table name. */
    private static final String TABLE_NAME = "food";
    /** The Primary Partition Key Type. */
    private static final String DEMO_PARTITION_KEY_TYPE = "String";
    /** The Sort Key Type. */
    private static final String DEMO_SORT_KEY_TYPE = "String";
    /** The number of secondary table indexes. */
    private static final int NUM_TABLE_INDEXES = 0;

    /** The DynamoDB object mapper for accessing DynamoDB. */
    private final DynamoDBMapper mapper;


    public NoSQLTableFood() {
        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getPartitionKeyName() {
        return DEMO_PARTITION_KEY;
    }

    public String getPartitionKeyType() {
        return DEMO_PARTITION_KEY_TYPE;
    }

    @Override
    public String getSortKeyName() {
        return DEMO_SORT_KEY;
    }

    public String getSortKeyType() {
        return DEMO_SORT_KEY_TYPE;
    }

    @Override
    public int getNumIndexes() {
        return NUM_TABLE_INDEXES;
    }


    @Override
    public void insertSampleData() throws AmazonClientException {
        Log.d(LOG_TAG, "Inserting Sample data.");
        final FoodDO firstItem = new FoodDO();

        firstItem.setFoodId(getDemoPartitionValue());
        firstItem.setBusinessId(DEMO_SORT_VALUE);
        firstItem.setDislike(SampleDataGenerator.getRandomSampleNumber());
        firstItem.setLike(SampleDataGenerator.getRandomSampleNumber());
        AmazonClientException lastException = null;

        try {
            mapper.save(firstItem);
        } catch (final AmazonClientException ex) {
            Log.e(LOG_TAG, "Failed saving item : " + ex.getMessage(), ex);
            lastException = ex;
        }

        final FoodDO[] items = new FoodDO[SAMPLE_DATA_ENTRIES_PER_INSERT-1];
        for (int count = 0; count < SAMPLE_DATA_ENTRIES_PER_INSERT-1; count++) {
            final FoodDO item = new FoodDO();
            item.setFoodId(SampleDataGenerator.getRandomPartitionSampleString("foodId"));
            item.setBusinessId(SampleDataGenerator.getRandomSampleString("businessId"));
            item.setDislike(SampleDataGenerator.getRandomSampleNumber());
            item.setLike(SampleDataGenerator.getRandomSampleNumber());

            items[count] = item;
        }
        try {
            mapper.batchSave(Arrays.asList(items));
        } catch (final AmazonClientException ex) {
            Log.e(LOG_TAG, "Failed saving item batch : " + ex.getMessage(), ex);
            lastException = ex;
        }

        if (lastException != null) {
            // Re-throw the last exception encountered to alert the user.
            throw lastException;
        }
    }

    @Override
    public void removeSampleData() throws AmazonClientException {
        // Scan for the sample data to remove it.

        // Use an expression names Map to avoid the potential for attribute names
        // colliding with DynamoDB reserved words.
        final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
        filterExpressionAttributeNames.put("#hashAttribute","foodId");

        final Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":prefixVal", new AttributeValue().withS(
            SampleDataGenerator.SAMPLE_DATA_STRING_PREFIX));

        final String hashKeyFilterCondition = "begins_with(#hashAttribute, :prefixVal)";

        filterExpressionAttributeNames.put("#rangeAttribute","businessId");
        expressionAttributeValues.put(":prefixVal2", new AttributeValue().withS(
            SampleDataGenerator.SAMPLE_DATA_STRING_PREFIX));
        final String sortKeyFilterCondition = "begins_with(#rangeAttribute, :prefixVal2)";
        final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
            .withFilterExpression(hashKeyFilterCondition + " and " + sortKeyFilterCondition)
            .withExpressionAttributeNames(filterExpressionAttributeNames)
            .withExpressionAttributeValues(expressionAttributeValues);

        PaginatedScanList<FoodDO> results = mapper.scan(FoodDO.class, scanExpression);

        Iterator<FoodDO> resultsIterator = results.iterator();

        AmazonClientException lastException = null;

        if (resultsIterator.hasNext()) {
            final FoodDO item = resultsIterator.next();

            // Demonstrate deleting a single item.
            try {
                mapper.delete(item);
            } catch (final AmazonClientException ex) {
                Log.e(LOG_TAG, "Failed deleting item : " + ex.getMessage(), ex);
                lastException = ex;
            }
        }

        final List<FoodDO> batchOfItems = new LinkedList<FoodDO>();
        while (resultsIterator.hasNext()) {
            // Build a batch of books to delete.
            for (int index = 0; index < MAX_BATCH_SIZE_FOR_DELETE && resultsIterator.hasNext(); index++) {
                batchOfItems.add(resultsIterator.next());
            }
            try {
                // Delete a batch of items.
                mapper.batchDelete(batchOfItems);
            } catch (final AmazonClientException ex) {
                Log.e(LOG_TAG, "Failed deleting item batch : " + ex.getMessage(), ex);
                lastException = ex;
            }

            // clear the list for re-use.
            batchOfItems.clear();
        }


        if (lastException != null) {
            // Re-throw the last exception encountered to alert the user.
            // The logs contain all the exceptions that occurred during attempted delete.
            throw lastException;
        }
    }

//    private List<NoSQLOperationListItem> getSupportedDemoOperations(final Context context) {
//        List<NoSQLOperationListItem> noSQLOperationsList = new ArrayList<NoSQLOperationListItem>();
//        noSQLOperationsList.add(new NoSQLOperationListHeader(
//            context.getString(R.string.nosql_operation_header_get)));
//        noSQLOperationsList.add(new GetWithPartitionKeyAndSortKey(context));
//
//        noSQLOperationsList.add(new NoSQLOperationListHeader(
//            context.getString(R.string.nosql_operation_header_primary_queries)));
//        noSQLOperationsList.add(new QueryWithPartitionKeyOnly(context));
//        noSQLOperationsList.add(new QueryWithPartitionKeyAndFilter(context));
//        noSQLOperationsList.add(new QueryWithPartitionKeyAndSortKeyCondition(context));
//        noSQLOperationsList.add(new QueryWithPartitionKeySortKeyConditionAndFilter(context));
//
//        noSQLOperationsList.add(new NoSQLOperationListHeader(
//            context.getString(R.string.nosql_operation_header_scan)));
//        noSQLOperationsList.add(new ScanWithoutFilter(context));
//        noSQLOperationsList.add(new ScanWithFilter(context));
//        return noSQLOperationsList;
//    }

//    @Override
//    public void getSupportedDemoOperations(final Context context,
//                                           final SupportedDemoOperationsHandler opsHandler) {
//        ThreadUtils.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                opsHandler.onSupportedOperationsReceived(getSupportedDemoOperations(context));
//            }
//        });
//    }
}
