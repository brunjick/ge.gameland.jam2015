package ge.gameland.jam2015;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import fr.ganfra.materialspinner.MaterialSpinner;

@SuppressWarnings("ConstantConditions")
public class RegistrationFragment extends Fragment implements View.OnClickListener {

    private static final String URL_FORM_SUBMIT = "https://docs.google.com/forms/d/1KUTfes6WeOY3M-MOp06IxXa6S5CNuarFYhGW3Mn5Dwk/formResponse";
    private static final String SUBMIT_SUCCESS_TITLE = "<title>გმადლობთ!</title>";

    private String errorText;
    private String progressTitle;
    private String progressMessage;
    private String[] spinnerItems;
    private Callback mCallback;
    private ProgressDialog mProgressDialog;
    private ArrayList<TextInputLayout> mTextViews;
    private MaterialSpinner mSpinner;

    public RegistrationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        errorText = getResources().getString(R.string.form_error_text);
        progressTitle = getResources().getString(R.string.submit_progress_title);
        progressMessage = getResources().getString(R.string.submit_progress_message);
        spinnerItems = getResources().getStringArray(R.array.form_spinner_items);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_registration, container, false);

        mTextViews = new ArrayList<>();
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_1236774317));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_740529747));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_341025569));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_287593469));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_1248049991));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_650778584));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_1386412726));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_540310440));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_1655340431));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_745559265));
        mTextViews.add((TextInputLayout) v.findViewById(R.id.entry_1767106365));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner = (MaterialSpinner) v.findViewById(R.id.entry_38241156);
        mSpinner.setAdapter(adapter);

        AppCompatButton button = (AppCompatButton) v.findViewById(R.id.button_submit);
        button.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        boolean validated = true;
        HashMap<String, String> formData = new HashMap<>();
        Pattern pattern = Pattern.compile("/(.*)$");

        for (TextInputLayout til : mTextViews) {
            String text = til.getEditText().getText().toString();
            if (text.length() > 0) {
                String resName = getResources().getResourceName(til.getId());
                Matcher matcher = pattern.matcher(resName);
                if (matcher.find())
                    formData.put(matcher.group(1), text);
                til.setErrorEnabled(false);
            } else {
                validated = false;
                til.setError(errorText);
            }
        }

        int selectedPos = mSpinner.getSelectedItemPosition();
        if (selectedPos > 0) {
            String resName = getResources().getResourceName(mSpinner.getId());
            Matcher matcher = pattern.matcher(resName);
            if (matcher.find())
                formData.put(matcher.group(1), spinnerItems[selectedPos - 1]);
        } else {
            mSpinner.setError(errorText);
        }

        if (validated) {
            mProgressDialog = ProgressDialog.show(getActivity(), progressTitle, progressMessage, true);
            doPostRequest(formData);
        }
    }

    private void doPostRequest(HashMap<String, String> requestParams) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams(requestParams);
        httpClient.post(URL_FORM_SUBMIT, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (parseResponseBody(responseBody).contains(SUBMIT_SUCCESS_TITLE)) {
                    mProgressDialog.dismiss();
                    mCallback.registrationSuccess();
                } else
                    onFailure(statusCode, null, null, null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mProgressDialog.dismiss();
                mCallback.registrationFailed();
            }
        });
    }

    private String parseResponseBody(byte[] body) {
        try {
            return new String(body, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            return "";
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void registrationSuccess();

        void registrationFailed();
    }
}
