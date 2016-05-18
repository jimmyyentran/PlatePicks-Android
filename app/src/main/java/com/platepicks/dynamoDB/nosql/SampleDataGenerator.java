package com.platepicks.dynamoDB.nosql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SampleDataGenerator {
    public static final String SAMPLE_DATA_STRING_PREFIX = "demo-";
    public static final int SAMPLE_DATA_NUMBER_MIN = 1111000000;
    public static final int SAMPLE_DATA_NUMBER_MAX = 1111999999;

    /** Number of sample partitions for partition keys when there is a sort key. */
    public static final int SAMPLE_DATA_PARTITIONS = 4;

    private static final int RANDOM_NUMBER_MAX = SAMPLE_DATA_NUMBER_MAX - SAMPLE_DATA_NUMBER_MIN;
    private static final String[] sampleStringValues = {"apple", "banana", "orange", "pear", "pineapple", "lemon",
        "cherry", "avocado", "blueberry", "raspberry", "grape", "watermelon", "papaya"};

    private static final Random random = new Random();

    private static int getRandomUnsignedInt(final int max) {
        return Math.abs(random.nextInt()) % (max + 1);
    }
    private static int getRandomNumber() {
        return getRandomUnsignedInt(RANDOM_NUMBER_MAX);
    }

    private static int getRandomPartitionNumber() {
        return Math.abs(random.nextInt()) % SAMPLE_DATA_PARTITIONS;
    }

    private static int getRandomInsertionCount() {
        return getRandomUnsignedInt(sampleStringValues.length / 2) + 1;
    }

    /* package */ static int getRandomPartitionSampleNumber() {
        return getRandomPartitionNumber() + SAMPLE_DATA_NUMBER_MIN;
    }

    /* package */ static String getRandomPartitionSampleString(final String attributeName) {
        return String.format("%s%s-%s", SAMPLE_DATA_STRING_PREFIX, attributeName, getRandomPartitionNumber());
    }

    /* package */ static byte[] getRandomPartitionSampleBinary() {
        return String.format("%s%06d", SAMPLE_DATA_STRING_PREFIX, getRandomPartitionNumber()).getBytes();
    }

    /* package */ static double getRandomSampleNumber() {
        return SAMPLE_DATA_NUMBER_MIN + getRandomNumber();
    }

    /* package */ static String getSampleStringPrefix(final String attributeName) {
        return SampleDataGenerator.SAMPLE_DATA_STRING_PREFIX + attributeName + "-";
    }

    /* package */ static String getRandomSampleString(final String attributeName) {
        return String.format("%s%s-%06d", SAMPLE_DATA_STRING_PREFIX, attributeName, getRandomNumber());
    }

    /* package */ static boolean getRandomSampleBool() {
        return Math.abs(random.nextInt()) % 2 == 0;
    }

    /* package */ static byte[] getRandomSampleBinary() {
        return String.format("%s%06d", SAMPLE_DATA_STRING_PREFIX, getRandomNumber()).getBytes();
    }

    /* package */ static Set<String> getSampleStringSet() {
        final HashSet<String> sampleStringSet = new HashSet<>();
        final int itemsToInsert = getRandomInsertionCount();
        for (int i = 0; i < itemsToInsert; i++) {
            sampleStringSet.add(sampleStringValues[getRandomUnsignedInt(sampleStringValues.length - 1)]);
        }
        return sampleStringSet;
    }

    /* package */ static Set<Double> getSampleNumberSet() {
        final HashSet<Double> sampleNumberSet = new HashSet<>();
        final int itemsToInsert = getRandomInsertionCount();
        for (int i = 0; i < itemsToInsert; i++) {
            sampleNumberSet.add(getRandomSampleNumber());
        }
        return sampleNumberSet;
    }

    /* package */ static Set<byte[]> getSampleBinarySet() {
        final HashSet<byte[]> sampleBinarySet = new HashSet<>();
        final int itemsToInsert = getRandomInsertionCount();
        for (int i = 0; i < itemsToInsert; i++) {
            sampleBinarySet.add(getRandomSampleBinary());
        }
        return sampleBinarySet;
    }

    /* package */ static List<String> getSampleList() {
        final ArrayList<String> sampleStringList = new ArrayList<>();
        final int itemsToInsert = getRandomInsertionCount();
        for (int i = 0; i < itemsToInsert; i++) {
            sampleStringList.add(sampleStringValues[getRandomUnsignedInt(sampleStringValues.length - 1)]);
        }
        return sampleStringList;
    }

    /* package */ static Map<String, String> getSampleMap() {
        final Map<String, String> sampleStringMap = new HashMap<>();
        final int itemsToInsert = getRandomInsertionCount();
        for (int i = 0; i < itemsToInsert; i++) {
            sampleStringMap.put(sampleStringValues[i], getRandomSampleString(
                sampleStringValues[getRandomUnsignedInt(sampleStringValues.length - 1)]));
        }
        return sampleStringMap;
    }
}
