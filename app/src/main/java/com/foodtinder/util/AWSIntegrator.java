package com.foodtinder.util;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import android.os.AsyncTask;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Created by jimmytran on 4/25/16.
 */
public class AWSIntegrator {
    private static final String LOG_TAG = AWSIntegrator.class.getSimpleName();
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
    public static void call(String fName, String rPayload){

        final String functionName = fName;
        final String requestPayload = rPayload;

        new AsyncTask<Void, Void, InvokeResult>() {
            @Override
            protected InvokeResult doInBackground(Void... params) {
                try {
                    final ByteBuffer payload =
                            ENCODER.encode(CharBuffer.wrap(requestPayload));

                    final InvokeRequest invokeRequest =
                            new InvokeRequest()
                                    .withFunctionName(functionName)
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
                        throw new Exception(invokeResult.getFunctionError());
                    } else {
                        final ByteBuffer resultPayloadBuffer = invokeResult.getPayload();
                        final String resultPayload = DECODER.decode(resultPayloadBuffer).toString();
                        System.out.println(resultPayload);
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
        }.execute();
    }
}
