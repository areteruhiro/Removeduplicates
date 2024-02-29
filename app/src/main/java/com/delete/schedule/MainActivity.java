package com.delete.schedule;

import static com.delete.schedule.CustomDatePickerDialogFragment.DateTimeCallback;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    private Button mGetEventsButton;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Googleにログイン";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Google Calendar API の呼び出しのための認証情報を初期化する
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES)
        ).setBackOff(new ExponentialBackOff());

        // Activity のレイアウト要素を取得
        mCallApiButton = findViewById(R.id.call_api_button);
        mOutputText = findViewById(R.id.output_text);
        mGetEventsButton = findViewById(R.id.get_events_button);
        Spinner calendarSpinner = findViewById(R.id.calendar_spinner);

        // Google Calendar API を呼び出すボタンのクリックリスナーを設定
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });

// カレンダーの予定を取得するボタンのクリックリスナーを設定
        mGetEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Googleアカウントが選択されているかどうかを確認
                if (mCredential.getSelectedAccountName() == null) {
                    // Googleアカウントが選択されていない場合は、ユーザーにGoogleアカウントを選択するよう促す
                   // chooseAccount();
                } else {
                    // 選択されたカレンダーのIDを取得
                    String selectedCalendarId = (String) calendarSpinner.getSelectedItem();

                    // 選択されたカレンダーの予定を取得するメソッドを呼び出す
                    getEvents(selectedCalendarId);
                }
            }
        });





        // スピナーのリストアイテムを取得し、スピナーに設定
        List<String> initialCalendarIds = new ArrayList<>();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, initialCalendarIds);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calendarSpinner.setAdapter(spinnerAdapter);

        // スピナーのリスナーを設定
        calendarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 選択されたカレンダーのIDを取得
                String selectedCalendarId = (String) parent.getItemAtPosition(position);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 何もしない
            }
        });

        // カレンダーIDの取得とスピナーへの設定を非同期で行う
        new MakeRequestTask(mCredential, spinnerAdapter).execute();



// 開始日時と終了日時を保持するフィールド
        final DateTime[] startTime = new DateTime[1];
        final DateTime[] endTime = new DateTime[1];

// 開始日付選択ボタンのクリックリスナーを設定
        Button showStartDateDialogButton = findViewById(R.id.showDateDialogButton);
        showStartDateDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ダイアログを表示するための処理を追加する
                DialogFragment datePickerDialog = new CustomDatePickerDialogFragment();
                datePickerDialog.show(getSupportFragmentManager(), "datePicker");
                // CustomDatePickerDialogFragment を表示して開始日付を選択する
                CustomDatePickerDialogFragment startDatePickerDialog = new CustomDatePickerDialogFragment();
                startDatePickerDialog.setDateTimeCallback(new DateTimeCallback() {
                    @Override
                    public void onDateTimeSet(java.util.Calendar calendar) {
                        // 選択された日付をstartTimeに設定
                        startTime[0] = new DateTime(calendar.getTimeInMillis());
                    }
                });
            }
        });

// 終了日付選択ボタンのクリックリスナーを設定
        Button showEndDateDialogButton = findViewById(R.id.showDateDialogButton2);
        showEndDateDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePickerDialog = new CustomDatePickerDialogFragment();
                datePickerDialog.show(getSupportFragmentManager(), "datePicker");
                // CustomDatePickerDialogFragment を表示して終了日付を選択する
                CustomDatePickerDialogFragment endDatePickerDialog = new CustomDatePickerDialogFragment();
                endDatePickerDialog.setDateTimeCallback(new DateTimeCallback() {
                    @Override
                    public void onDateTimeSet(java.util.Calendar calendar) {
                        // 選択された日付をendTimeに設定
                        endTime[0] = new DateTime(calendar.getTimeInMillis());
                    }
                });
            }
        });


        }





    // フィールドとして mService を追加
    private com.google.api.services.calendar.Calendar mService = null;

    // カレンダーの予定を取得するメソッド
    private void getEvents(String calendarId) {
        new GetEventsTask().execute(calendarId);
    }

    class GetEventsTask extends AsyncTask<String, Void, List<Event>> {
        private DateTime startTime;
        private DateTime endTime;

        public GetEventsTask() {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        protected List<Event> doInBackground(String... params) {
            String calendarId = params[0];
            try {
                // Google Calendar APIを使用してイベントを取得するためのリクエストを作成
                Calendar service = new Calendar.Builder(
                        new NetHttpTransport(), GsonFactory.getDefaultInstance(), mCredential)
                        .setApplicationName("com.testabc.comm")
                        .build();


                // カレンダーからイベントを取得するリクエストを作成
                Events events = service.events().list(calendarId)
                        .setMaxResults(1000) // 取得するイベントの最大数
                        .setTimeMin(startTime) // 開始日時以降のイベントを取得
                        .setTimeMax(endTime) // 終了日時以前のイベントを取得
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();




                // 取得したイベントのリスト
                List<Event> items = events.getItems();

// 同じタイトルと開始日時を持つイベントが複数ある場合、削除対象のIDを保持する変数
                String eventIdToDelete = null;


// イベントを取得し、重複を確認して削除するイベントを決定
// 取得したイベントの中から同じタイトルと日時を持つイベントが複数ある場合、削除対象のIDを保持
                for (int i = 0; i < items.size(); i++) {
                    Event event1 = items.get(i);
                    String eventTitle1 = event1.getSummary();
                    DateTime eventStartTime1 = event1.getStart().getDateTime();
                    DateTime eventEndTime1 = event1.getEnd().getDateTime();

                    // itemsの残りのイベントと比較
                    for (int j = i + 1; j < items.size(); j++) {
                        Event event2 = items.get(j);
                        String eventTitle2 = event2.getSummary();
                        DateTime eventStartTime2 = event2.getStart().getDateTime();
                        DateTime eventEndTime2 = event2.getEnd().getDateTime();

                        // 重複を確認
                        if (eventTitle1.equals(eventTitle2) &&
                                eventStartTime1.equals(eventStartTime2) &&
                                eventEndTime1.equals(eventEndTime2)) {
                            // 重複したイベントが見つかった場合、片方を削除する
                            eventIdToDelete = event1.getId(); // または event2.getId() を選択
                            break; // 1つ見つかれば終了
                        }
                    }

                    if (eventIdToDelete != null) {
                        // 重複したイベントが見つかったので削除リクエストを送信
                        service.events().delete(calendarId, eventIdToDelete).execute();
                        break; // 削除したので処理終了
                    }
                }


// 削除対象のIDが見つかった場合、削除リクエストを送信
                if (eventIdToDelete != null) {
                    Log.d("MainActivity", "Attempting to delete event with ID: " + eventIdToDelete);
                    service.events().delete(calendarId, eventIdToDelete).execute();
                    Log.d("MainActivity", "Event ID to delete: " + eventIdToDelete);
                } else {
                    Log.d("MainActivity", "No event found to delete.");
                }



                return items;
            } catch (IOException e) {
                Log.e("MainActivity", "Error retrieving events", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            if (events != null) {
                // 取得したイベントをリストに保存
                for (Event event : events) {
                    Log.d("MainActivity", "Event summary: " + event.getSummary());
                    Log.d("MainActivity", "Event start time: " + event.getStart());
                    Log.d("MainActivity", "Event end time: " + event.getEnd());
                    Log.d("MainActivity", "Event id: " + event.getId());
                    // 必要に応じて、取得したイベントをUIに表示するなどの処理を行う
                }
            } else {
                // エラー処理
            }
        }
    }



    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
        );
        dialog.show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;

            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {}

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {}

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            // Google Calendar API を呼び出す
            new MakeRequestTask(mCredential, null).execute();
        }
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {

        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private ArrayAdapter<String> mSpinnerAdapter;

        MakeRequestTask(GoogleAccountCredential credential, ArrayAdapter<String> spinnerAdapter) {
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar
                    .Builder(transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
            mSpinnerAdapter = spinnerAdapter;
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getCalendarIds();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<String> getCalendarIds() throws IOException {
            List<String> calendarIds = new ArrayList<>();
            CalendarList calendarList = mService.calendarList().list().execute();
            List<CalendarListEntry> items = calendarList.getItems();
            for (CalendarListEntry calendarListEntry : items) {
                calendarIds.add(calendarListEntry.getId());
            }
            return calendarIds;
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            if (mSpinnerAdapter != null) {
                mSpinnerAdapter.clear();
            }
            mProgress = new ProgressDialog(MainActivity.this);
            mProgress.setMessage("Fetching calendar list...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> calendarIds) {
            mProgress.hide();
            if (calendarIds == null || calendarIds.isEmpty()) {
                mOutputText.setText("No calendars found.");
            } else {
               StringBuilder result = new StringBuilder("Calendar IDs:\n");
                for (String id : calendarIds) {
                   result.append(id).append("\n");
                }
                //mOutputText.setText(result.toString());

                // スピナーを初期化
                Spinner calendarSpinner = findViewById(R.id.calendar_spinner);
                if (calendarSpinner != null) {
                    // 取得したカレンダーIDをスピナーに追加
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, calendarIds);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    calendarSpinner.setAdapter(adapter);
                } else {
                    Log.e("MainActivity", "Spinner is null");
                }
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.dismiss();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }

    }
}
