package com.platepicks.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.platepicks.R;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class CloudLogicDemoFragment extends DemoFragmentBase implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final String LOG_TAG = CloudLogicDemoFragment.class.getSimpleName();

    private static final String DEFAULT_FUNCTION_NAME = "hello-world";
    private static final String DEFAULT_REQUEST_CONTENTS = "{\n  \"key1\" : \"value1\",\n  \"key2\" : \"value2\",\n  \"key3\" : \"value3\"\n}";
    private static final Charset CHARSET_UTF8 =
            Charset.forName("UTF-8");
    private static final CharsetEncoder ENCODER = CHARSET_UTF8.newEncoder();
    private static final CharsetDecoder DECODER = CHARSET_UTF8.newDecoder();

    private View mInvokeButton;
    private View mResetButton;
    private EditText mFunctionField;
    private EditText mRequestField;
    private EditText mResultField;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view =
            inflater.inflate(R.layout.fragment_demo_cloud_logic, container, false);

        mResetButton = view.findViewById(R.id.cloudLogic_button_reset);
        mInvokeButton = view.findViewById(R.id.cloudLogic_button_invoke);
        mFunctionField = (EditText)view.findViewById(R.id.cloudLogic_editText_function);
        mRequestField = (EditText)view.findViewById(R.id.cloudLogic_editText_request);
        mResultField = (EditText)view.findViewById(R.id.cloudLogic_editText_result);

        mResetButton.setOnClickListener(this);
        mInvokeButton.setOnClickListener(this);
        mFunctionField.setOnEditorActionListener(this);
        mRequestField.setOnEditorActionListener(this);

        resetFields();

        return view;
    }

    @Override
    public void onClick(final View view) {
        Log.d(LOG_TAG, "onClick");

        if (mResetButton == view) {
            Log.d(LOG_TAG, "onClick - RESET");
            resetFields();
        } else if (mInvokeButton == view) {
            Log.d(LOG_TAG, "onClick - INVOKE");
            invokeFunction();
        }
    }

    private void resetFields() {
        mFunctionField.setText(DEFAULT_FUNCTION_NAME);
        mRequestField.setText(DEFAULT_REQUEST_CONTENTS);
        mResultField.setText("");
    }

    private void invokeFunction() {

        final String functionName = mFunctionField.getText().toString();
        final String requestPayload = mRequestField.getText().toString();

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
                        showError(invokeResult.getFunctionError());
                    } else {
                        final ByteBuffer resultPayloadBuffer = invokeResult.getPayload();
                        final String resultPayload = DECODER.decode(resultPayloadBuffer).toString();
                        mResultField.setText(resultPayload);
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
                    showError(e.getMessage());
                }
            }
        }.execute();
    }

    @Override
    public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {
        Log.d(LOG_TAG, "onEditorAction");

        if (EditorInfo.IME_ACTION_DONE == actionId) {
            final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return true;
        }

        return false;
    }

    public void showError(final String errorMessage) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getResources().getString(R.string.cloud_logic_error_title))
                .setMessage(errorMessage)
                .setNegativeButton(getActivity().getResources().getString(R.string.cloud_logic_error_dismiss), null)
                .create().show();
    }
}
