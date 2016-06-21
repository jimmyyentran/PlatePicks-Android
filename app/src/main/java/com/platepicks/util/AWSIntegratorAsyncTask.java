package com.platepicks.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import com.google.gson.Gson;

/**
 * Created by alyza on 4/28/16.
 */
public class AWSIntegratorAsyncTask extends AsyncTask<Object, Void, InvokeResult>{
    private static final String LOG_TAG = AWSIntegratorAsyncTask.class.getSimpleName();
    private static final Charset CHARSET_UTF8 =
            Charset.forName("UTF-8");
    private static final CharsetEncoder ENCODER = CHARSET_UTF8.newEncoder();
    private static final CharsetDecoder DECODER = CHARSET_UTF8.newDecoder();
    /**
     * This static class can be called anywhere. Currently it's return type is void but we would
     * want it to return a json object
     * @param fName name of the function to call on AWS
     * @param rPayload json to be passed into the called function, it's ok
     *                       to take in a string for now, but in the future it will take in a json
     *                       object.
     * @return return json object
     */

//
//        final String functionName = fName;
//        final String requestPayload = rPayload;
//        String output = "testHardCode";

    AWSIntegratorInterface callerActivity;
    @Override
    protected InvokeResult doInBackground(Object... params) {
        try {
            String json = new Gson().toJson(params[1]); //added convert to json string
//            System.out.println(json);
            callerActivity = (AWSIntegratorInterface) params[2];

            if (!ConnectionCheck.isConnected((Context) params[2])) {
                final InvokeResult result = new InvokeResult();
                result.setStatusCode(500);
                result.setFunctionError("Internet check: no connection");
                return result;
            }

            final ByteBuffer payload =
//                    ENCODER.encode(CharBuffer.wrap((String) params[1]));
                    ENCODER.encode(CharBuffer.wrap(json)); //added send json string

            final InvokeRequest invokeRequest =
                    new InvokeRequest()
                            .withFunctionName((String) params[0])
                            .withInvocationType(InvocationType.RequestResponse)
                            .withPayload(payload);

            final InvokeResult invokeResult =
                    AWSMobileClient
                            .defaultMobileClient()
                            .getCloudFunctionClient()
                            .invoke(invokeRequest);

            return invokeResult;
        } catch (final Exception e) {
            Log.e(LOG_TAG, "AWS Lambda invocation failed : " + e.getMessage(), e);
            System.out.println("AWS Lambda invocation failed : " + e.getMessage());
            final InvokeResult result = new InvokeResult();
            result.setStatusCode(500);
            result.setFunctionError(e.getMessage());
            return result;
        }
    }

    @Override
    protected void onPostExecute(final InvokeResult invokeResult) {

        try {
            final int statusCode = invokeResult.getStatusCode();
            final String functionError = invokeResult.getFunctionError();
            final String logResult = invokeResult.getLogResult();

            if (statusCode != 200) {
                callerActivity.doSomethingOnAWSError();
                return;
            } else {
                final ByteBuffer resultPayloadBuffer = invokeResult.getPayload();
                final String resultPayload = DECODER.decode(resultPayloadBuffer).toString();
//                System.out.println("test:" + resultPayload);
                callerActivity.doSomethingWithResults(resultPayload);
            }

            if (functionError != null) {
                Log.e(LOG_TAG, "AWS Lambda Function Error: " + functionError);
            }

            if (logResult != null) {
                Log.d(LOG_TAG, "AWS Lambda Log Result: " + logResult);
            }
        }
        catch (final Exception e) {
            Log.e(LOG_TAG, "Unable to decode results. " + e.getMessage(), e);
            // FIX THROW HERE
//                    throw new Exception(e.getMessage());
        }
    }
}

