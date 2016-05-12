package com.platepicks.dynamoDB.nosql;

import android.content.Context;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
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


public class NoSQLTableList extends NoSQLTableBase {
    private static final String LOG_TAG = NoSQLTableList.class.getSimpleName();

    /** Condition demo key for Primary Queries with filter conditions. */
    private static final String DEMO_PRIMARY_CONDITION_KEY = "creationDate";
    /** Condition demo value for Queries with filter conditions. */
    private static final String DEMO_PRIMARY_CONDITION_VALUE = "1111500000";

    /** Condition demo key for Secondary Queries with filter conditions. */
    private static final String DEMO_SECONDARY_CONDITION_KEY = "foodId";
    /** Condition demo value for Queries with filter conditions. */
    private static final String DEMO_SECONDARY_CONDITION_VALUE = "demo-" + DEMO_SECONDARY_CONDITION_KEY + "-500000";

    private static final String DEMO_PARTITION_KEY = "userId";
    /** Partition/Hash demo value for Get and Queries. */
    private String getDemoPartitionValue() {
        return cognitoIdentityId;
    }
    private String getDemoPartitionValueText() {
        return cognitoIdentityId;
    }
    /** The Primary Partition Key Name.  All queries must use an equality condition on this key. */
    /** The Primary Sort Key Name. */
    private static final String DEMO_SORT_KEY = "foodId";
    /** Sort/Range demo value for Get and Queries. */
    private static final String DEMO_SORT_VALUE = "demo-" + DEMO_SORT_KEY + "-500000";
    private static final String DEMO_SORT_VALUE_TEXT = DEMO_SORT_VALUE;

    /********* Primary Get Query Inner Classes *********/

    public class DemoGetWithPartitionKeyAndSortKey extends NoSQLOperationBase {
        private ListDO result;
        private boolean resultRetrieved = true;

        DemoGetWithPartitionKeyAndSortKey(final Context context) {
            super(context.getString(R.string.nosql_operation_get_by_partition_and_sort_text),
                String.format(context.getString(R.string.nosql_operation_example_get_by_partition_and_sort_text),
                    DEMO_PARTITION_KEY, getDemoPartitionValueText(),
                    DEMO_SORT_KEY, DEMO_SORT_VALUE_TEXT));
        }

        @Override
        public boolean executeOperation() {
            // Retrieve an item by passing the partition key using the object mapper.
            result = mapper.load(ListDO.class, getDemoPartitionValue(), DEMO_SORT_VALUE);

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
            results.add(new NoSQLListResult(result));
            resultRetrieved = true;
            return results;
        }

        @Override
        public void resetResults() {
            resultRetrieved = false;
        }
    }

    /* ******** Primary Index Query Inner Classes ******** */

    public class DemoQueryWithPartitionKeyAndSortKeyCondition extends NoSQLOperationBase {

        private PaginatedQueryList<ListDO> results;
        private Iterator<ListDO> resultsIterator;

        DemoQueryWithPartitionKeyAndSortKeyCondition(final Context context) {
            super(context.getString(R.string.nosql_operation_title_query_by_partition_and_sort_condition_text),
                  String.format(context.getString(R.string.nosql_operation_example_query_by_partition_and_sort_condition_text),
                      DEMO_PARTITION_KEY, getDemoPartitionValueText(),
                      DEMO_SORT_KEY, DEMO_SORT_VALUE_TEXT));
        }

        @Override
        public boolean executeOperation() {
            final ListDO itemToFind = new ListDO();
            itemToFind.setUserId(getDemoPartitionValue());

            final Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())
                .withAttributeValueList(new AttributeValue().withS(DEMO_SORT_VALUE));
            final DynamoDBQueryExpression<ListDO> queryExpression = new DynamoDBQueryExpression<ListDO>()
                .withHashKeyValues(itemToFind)
                .withRangeKeyCondition(DEMO_SORT_KEY, rangeKeyCondition)
                .withConsistentRead(false)
                .withLimit(RESULTS_PER_RESULT_GROUP);

            results = mapper.query(ListDO.class, queryExpression);
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

    public class DemoQueryWithPartitionKeyOnly extends NoSQLOperationBase {

        private PaginatedQueryList<ListDO> results;
        private Iterator<ListDO> resultsIterator;

        DemoQueryWithPartitionKeyOnly(final Context context) {
            super(context.getString(R.string.nosql_operation_title_query_by_partition_text),
                String.format(context.getString(R.string.nosql_operation_example_query_by_partition_text),
                    DEMO_PARTITION_KEY, getDemoPartitionValueText()));
        }

        @Override
        public boolean executeOperation() {
            final ListDO itemToFind = new ListDO();
            itemToFind.setUserId(getDemoPartitionValue());

            final DynamoDBQueryExpression<ListDO> queryExpression = new DynamoDBQueryExpression<ListDO>()
                .withHashKeyValues(itemToFind)
                .withConsistentRead(false)
                .withLimit(RESULTS_PER_RESULT_GROUP);

            results = mapper.query(ListDO.class, queryExpression);
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

    public class DemoQueryWithPartitionKeyAndFilter extends NoSQLOperationBase {

        private PaginatedQueryList<ListDO> results;
        private Iterator<ListDO> resultsIterator;

        DemoQueryWithPartitionKeyAndFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_query_by_partition_and_filter_text),
                  String.format(context.getString(R.string.nosql_operation_example_query_by_partition_and_filter_text),
                      DEMO_PARTITION_KEY, getDemoPartitionValueText(),
                      DEMO_PRIMARY_CONDITION_KEY, DEMO_PRIMARY_CONDITION_VALUE));
        }

        @Override
        public boolean executeOperation() {
            final ListDO itemToFind = new ListDO();
            itemToFind.setUserId(getDemoPartitionValue());

            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#creationDate", DEMO_PRIMARY_CONDITION_KEY);

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":MincreationDate",
                new AttributeValue().withN(DEMO_PRIMARY_CONDITION_VALUE));

            final DynamoDBQueryExpression<ListDO> queryExpression = new DynamoDBQueryExpression<ListDO>()
                .withHashKeyValues(itemToFind)
                .withFilterExpression("#creationDate > :MincreationDate")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues)
                .withConsistentRead(false)
                .withLimit(RESULTS_PER_RESULT_GROUP);

            results = mapper.query(ListDO.class, queryExpression);
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

    public class DemoQueryWithPartitionKeySortKeyConditionAndFilter extends NoSQLOperationBase {

        private PaginatedQueryList<ListDO> results;
        private Iterator<ListDO> resultsIterator;

        DemoQueryWithPartitionKeySortKeyConditionAndFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_query_by_partition_sort_condition_and_filter_text),
                  String.format(context.getString(R.string.nosql_operation_example_query_by_partition_sort_condition_and_filter_text),
                      DEMO_PARTITION_KEY, getDemoPartitionValueText(),
                      DEMO_SORT_KEY, DEMO_SORT_VALUE_TEXT,
                      DEMO_PRIMARY_CONDITION_KEY, DEMO_PRIMARY_CONDITION_VALUE));
        }

        public boolean executeOperation() {
            final ListDO itemToFind = new ListDO();
            itemToFind.setUserId(getDemoPartitionValue());

            final Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())
                .withAttributeValueList(new AttributeValue().withS(DEMO_SORT_VALUE));

            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#creationDate", DEMO_PRIMARY_CONDITION_KEY);

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":MincreationDate",
                new AttributeValue().withN(DEMO_PRIMARY_CONDITION_VALUE));

            final DynamoDBQueryExpression<ListDO> queryExpression = new DynamoDBQueryExpression<ListDO>()
                .withHashKeyValues(itemToFind)
                .withRangeKeyCondition(DEMO_SORT_KEY, rangeKeyCondition)
                .withFilterExpression("#creationDate > :MincreationDate")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues)
                .withConsistentRead(false)
                .withLimit(RESULTS_PER_RESULT_GROUP);

            results = mapper.query(ListDO.class, queryExpression);
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

    private static final String DEMO_DATESORTED_PARTITION_KEY = "userId";

    private String getDemoDateSortedPartitionValue() {
        return cognitoIdentityId;
    }

    private String getDemoDateSortedPartitionValueText() {
        return cognitoIdentityId;
    }

    private static final String DEMO_DATESORTED_SORT_KEY = "creationDate";
    private static final Double DEMO_DATESORTED_SORT_VALUE = 1111500000.0;
    private static final String DEMO_DATESORTED_SORT_VALUE_TEXT = "1111500000";

    public class DemoDateSortedQueryWithPartitionKeyAndSortKeyCondition extends NoSQLOperationBase {

        private PaginatedQueryList<ListDO> results;
        private Iterator<ListDO> resultsIterator;
        DemoDateSortedQueryWithPartitionKeyAndSortKeyCondition (final Context context) {
            super(
                context.getString(R.string.nosql_operation_title_index_query_by_partition_and_sort_condition_text),
                context.getString(R.string.nosql_operation_example_index_query_by_partition_and_sort_condition_text,
                    DEMO_DATESORTED_PARTITION_KEY, getDemoDateSortedPartitionValueText(),
                    DEMO_DATESORTED_SORT_KEY, DEMO_DATESORTED_SORT_VALUE_TEXT));
        }

        public boolean executeOperation() {
            // Perform a query using a partition key and sort key condition.
            final ListDO itemToFind = new ListDO();
            itemToFind.setUserId(getDemoDateSortedPartitionValue());
            final Condition sortKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())

                .withAttributeValueList(new AttributeValue().withN(DEMO_DATESORTED_SORT_VALUE.toString()));
            // Perform get using Partition key and sort key condition
            DynamoDBQueryExpression<ListDO> queryExpression = new DynamoDBQueryExpression<ListDO>()
                .withHashKeyValues(itemToFind)
                .withRangeKeyCondition("creationDate", sortKeyCondition)
                .withConsistentRead(false);
            results = mapper.query(ListDO.class, queryExpression);
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

    public class DemoDateSortedQueryWithPartitionKeyOnly extends NoSQLOperationBase {

        private PaginatedQueryList<ListDO> results;
        private Iterator<ListDO> resultsIterator;

        DemoDateSortedQueryWithPartitionKeyOnly(final Context context) {
            super(
                context.getString(R.string.nosql_operation_title_index_query_by_partition_text),
                context.getString(R.string.nosql_operation_example_index_query_by_partition_text,
                    DEMO_DATESORTED_PARTITION_KEY, getDemoDateSortedPartitionValueText()));
        }

        public boolean executeOperation() {
            // Perform a query using a partition key and filter condition.
            final ListDO itemToFind = new ListDO();
            itemToFind.setUserId(getDemoDateSortedPartitionValue());

            // Perform get using Partition key
            DynamoDBQueryExpression<ListDO> queryExpression = new DynamoDBQueryExpression<ListDO>()
                .withHashKeyValues(itemToFind)
                .withConsistentRead(false);
            results = mapper.query(ListDO.class, queryExpression);
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

    public class DemoDateSortedQueryWithPartitionKeyAndFilterCondition extends NoSQLOperationBase {

        private PaginatedQueryList<ListDO> results;
        private Iterator<ListDO> resultsIterator;

        DemoDateSortedQueryWithPartitionKeyAndFilterCondition (final Context context) {
            super(
                context.getString(R.string.nosql_operation_title_index_query_by_partition_and_filter_text),
                context.getString(R.string.nosql_operation_example_index_query_by_partition_and_filter_text,
                    DEMO_DATESORTED_PARTITION_KEY, getDemoDateSortedPartitionValueText(),
                    DEMO_SECONDARY_CONDITION_KEY, DEMO_SECONDARY_CONDITION_VALUE));
        }

        public boolean executeOperation() {
            // Perform a query using a partition key and filter condition.
            final ListDO itemToFind = new ListDO();
            itemToFind.setUserId(getDemoDateSortedPartitionValue());

            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#foodId", DEMO_SECONDARY_CONDITION_KEY);

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":MinfoodId",
                new AttributeValue().withS(DEMO_SECONDARY_CONDITION_VALUE));

            // Perform get using Partition key and sort key condition
            DynamoDBQueryExpression<ListDO> queryExpression = new DynamoDBQueryExpression<ListDO>()
                .withHashKeyValues(itemToFind)
                .withFilterExpression("#foodId > :MinfoodId")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues)
                .withConsistentRead(false);
            results = mapper.query(ListDO.class, queryExpression);
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

    public class DemoDateSortedQueryWithPartitionKeySortKeyAndFilterCondition extends NoSQLOperationBase {

        private PaginatedQueryList<ListDO> results;
        private Iterator<ListDO> resultsIterator;

        DemoDateSortedQueryWithPartitionKeySortKeyAndFilterCondition (final Context context) {
            super(
                context.getString(R.string.nosql_operation_title_index_query_by_partition_sort_condition_and_filter_text),
                context.getString(R.string.nosql_operation_example_index_query_by_partition_sort_condition_and_filter_text,

                    DEMO_DATESORTED_PARTITION_KEY, getDemoDateSortedPartitionValueText(),
                    DEMO_DATESORTED_SORT_KEY, DEMO_DATESORTED_SORT_VALUE,
                    DEMO_SECONDARY_CONDITION_KEY, DEMO_SECONDARY_CONDITION_VALUE));
        }

        public boolean executeOperation() {
            // Perform a query using a partition key, sort condition, and filter.
            final ListDO itemToFind = new ListDO();
            itemToFind.setUserId(getDemoDateSortedPartitionValue());
            final Condition sortKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())

                .withAttributeValueList(new AttributeValue().withN(DEMO_DATESORTED_SORT_VALUE.toString()));
            // Use a map of expression names to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map<String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#foodId", DEMO_SECONDARY_CONDITION_KEY);

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":MinfoodId",
                new AttributeValue().withS(DEMO_SECONDARY_CONDITION_VALUE));

            // Perform get using Partition key and sort key condition
            DynamoDBQueryExpression<ListDO> queryExpression = new DynamoDBQueryExpression<ListDO>()
                .withHashKeyValues(itemToFind)
                .withRangeKeyCondition("creationDate", sortKeyCondition)
                .withFilterExpression("#foodId > :MinfoodId")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues)
                .withConsistentRead(false);
            results = mapper.query(ListDO.class, queryExpression);
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

    /********* Scan Inner Classes *********/

    public class DemoScanWithFilter extends NoSQLOperationBase {

        private PaginatedScanList<ListDO> results;
        private Iterator<ListDO> resultsIterator;

        DemoScanWithFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_scan_with_filter),
                String.format(context.getString(R.string.nosql_operation_example_scan_with_filter),
                    DEMO_PRIMARY_CONDITION_KEY, DEMO_PRIMARY_CONDITION_VALUE));
        }

        @Override
        public boolean executeOperation() {
            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#creationDate", DEMO_PRIMARY_CONDITION_KEY);

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":MincreationDate",
                new AttributeValue().withN(DEMO_PRIMARY_CONDITION_VALUE));
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#creationDate > :MincreationDate")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues);

            results = mapper.scan(ListDO.class, scanExpression);
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

    public class DemoScanWithoutFilter extends NoSQLOperationBase {

        private PaginatedScanList<ListDO> results;
        private Iterator<ListDO> resultsIterator;

        DemoScanWithoutFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_scan_without_filter),
                context.getString(R.string.nosql_operation_example_scan_without_filter));
        }

        @Override
        public boolean executeOperation() {
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            results = mapper.scan(ListDO.class, scanExpression);
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
    private static List<NoSQLResult> getNextResultsGroupFromIterator(final Iterator<ListDO> resultsIterator) {
        if (!resultsIterator.hasNext()) {
            return null;
        }
        List<NoSQLResult> resultGroup = new LinkedList<>();
        int itemsRetrieved = 0;
        do {
            // Retrieve the item from the paginated results.
            final ListDO item = resultsIterator.next();
            // Add the item to a group of results that will be displayed later.
            resultGroup.add(new NoSQLListResult(item));
            itemsRetrieved++;
        } while ((itemsRetrieved < RESULTS_PER_RESULT_GROUP) && resultsIterator.hasNext());
        return resultGroup;
    }

    /** Inner classes use this value to determine how many results to retrieve per service call. */
    private final static int RESULTS_PER_RESULT_GROUP = 40;
    /** Removing sample data removes the items in batches of the following size. */
    private static final int MAX_BATCH_SIZE_FOR_DELETE = 50;

    /** The table name. */
    private static final String TABLE_NAME = "list";
    /** The Primary Partition Key Type. */
    private static final String DEMO_PARTITION_KEY_TYPE = "String";
    /** The Sort Key Type. */
    private static final String DEMO_SORT_KEY_TYPE = "String";
    /** The number of secondary table indexes. */
    private static final int NUM_TABLE_INDEXES = 1;

    /** The DynamoDB object mapper for accessing DynamoDB. */
    private final DynamoDBMapper mapper;

    /** Private and Protected tables must use the Cognito Identity as the hash key, so we must store it. */
    private static String cognitoIdentityId = null;

    public NoSQLTableList() {
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
        final ListDO firstItem = new ListDO();

        firstItem.setUserId(getDemoPartitionValue());
        firstItem.setFoodId(DEMO_SORT_VALUE);
        firstItem.setCreationDate(SampleDataGenerator.getRandomSampleNumber());
        firstItem.setType(SampleDataGenerator.getRandomSampleBinary());
        AmazonClientException lastException = null;

        try {
            mapper.save(firstItem);
        } catch (final AmazonClientException ex) {
            Log.e(LOG_TAG, "Failed saving item : " + ex.getMessage(), ex);
            lastException = ex;
        }

        final ListDO[] items = new ListDO[SAMPLE_DATA_ENTRIES_PER_INSERT-1];
        for (int count = 0; count < SAMPLE_DATA_ENTRIES_PER_INSERT-1; count++) {
            final ListDO item = new ListDO();
            item.setUserId(cognitoIdentityId);
            item.setFoodId(SampleDataGenerator.getRandomSampleString("foodId"));
            item.setCreationDate(SampleDataGenerator.getRandomSampleNumber());
            item.setType(SampleDataGenerator.getRandomSampleBinary());

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
        final ListDO itemToFind = new ListDO();
        itemToFind.setUserId(getDemoPartitionValue());

        final DynamoDBQueryExpression<ListDO> queryExpression = new DynamoDBQueryExpression<ListDO>()
            .withHashKeyValues(itemToFind)
            .withConsistentRead(false)
            .withLimit(MAX_BATCH_SIZE_FOR_DELETE);

        final PaginatedQueryList<ListDO> results = mapper.query(ListDO.class, queryExpression);

        Iterator<ListDO> resultsIterator = results.iterator();

        AmazonClientException lastException = null;

        if (resultsIterator.hasNext()) {
            final ListDO item = resultsIterator.next();

            // Demonstrate deleting a single item.
            try {
                mapper.delete(item);
            } catch (final AmazonClientException ex) {
                Log.e(LOG_TAG, "Failed deleting item : " + ex.getMessage(), ex);
                lastException = ex;
            }
        }

        final List<ListDO> batchOfItems = new LinkedList<ListDO>();
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

    private List<NoSQLOperationListItem> getSupportedDemoOperations(final Context context) {
        List<NoSQLOperationListItem> noSQLOperationsList = new ArrayList<NoSQLOperationListItem>();
        noSQLOperationsList.add(new NoSQLOperationListHeader(
            context.getString(R.string.nosql_operation_header_get)));
        noSQLOperationsList.add(new DemoGetWithPartitionKeyAndSortKey(context));

        noSQLOperationsList.add(new NoSQLOperationListHeader(
            context.getString(R.string.nosql_operation_header_primary_queries)));
        noSQLOperationsList.add(new DemoQueryWithPartitionKeyOnly(context));
        noSQLOperationsList.add(new DemoQueryWithPartitionKeyAndFilter(context));
        noSQLOperationsList.add(new DemoQueryWithPartitionKeyAndSortKeyCondition(context));
        noSQLOperationsList.add(new DemoQueryWithPartitionKeySortKeyConditionAndFilter(context));

        noSQLOperationsList.add(new NoSQLOperationListHeader(
            context.getString(R.string.nosql_operation_header_secondary_queries, "DateSorted")));

        noSQLOperationsList.add(new DemoDateSortedQueryWithPartitionKeyOnly(context));
        noSQLOperationsList.add(new DemoDateSortedQueryWithPartitionKeyAndFilterCondition(context));
        noSQLOperationsList.add(new DemoDateSortedQueryWithPartitionKeyAndSortKeyCondition(context));
        noSQLOperationsList.add(new DemoDateSortedQueryWithPartitionKeySortKeyAndFilterCondition(context));
        noSQLOperationsList.add(new NoSQLOperationListHeader(
            context.getString(R.string.nosql_operation_header_scan)));
        noSQLOperationsList.add(new DemoScanWithoutFilter(context));
        noSQLOperationsList.add(new DemoScanWithFilter(context));
        return noSQLOperationsList;
    }

    @Override
    public void getSupportedDemoOperations(final Context context,
                                           final SupportedDemoOperationsHandler opsHandler) {
        AWSMobileClient
            .defaultMobileClient()
            .getIdentityManager()
            .getUserID(new IdentityManager.IdentityHandler() {
                @Override
                public void handleIdentityID(final String identityId) {
                    cognitoIdentityId = identityId;
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            opsHandler.onSupportedOperationsReceived(getSupportedDemoOperations(context));
                        }
                    });
                }

                @Override
                public void handleError(final Exception exception) {
                    // This should never happen since the Identity ID is retrieved
                    // when the Application starts.
                    cognitoIdentityId = null;
                    opsHandler.onSupportedOperationsReceived(new ArrayList<NoSQLOperationListItem>());
                }
            });
    }
}
