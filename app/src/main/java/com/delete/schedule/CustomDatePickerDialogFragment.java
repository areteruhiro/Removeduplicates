package com.delete.schedule;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class CustomDatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private DateTimeCallback dateTimeCallback;

    public interface DateTimeCallback {
        void onDateTimeSet(Calendar calendar);
    }

    public void setDateTimeCallback(DateTimeCallback callback) {
        this.dateTimeCallback = callback;
    }

    

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 現在の日付を取得
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog を新しく作成して返す
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // 日付が選択されたときの処理をここに追加
        // 例えば、選択された日付を MainActivity に通知するなど
        // 選択された日付をカレンダーに設定
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, dayOfMonth);

        // コールバックを呼び出して選択された日付を通知
        if (dateTimeCallback != null) {
            dateTimeCallback.onDateTimeSet(selectedDate);
        }
    }
}
