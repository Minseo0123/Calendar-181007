package com.example.ryu_w.calendar;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import static android.text.TextUtils.isEmpty;

public class WriteDailyLog extends AppCompatActivity {

    EditText et_temp, et_rootT, et_humid, et_co2, et_date;
    TextView tv_sensor;
    long temp, rootT, humid, co2;
    DBHelper myHelper;
    SQLiteDatabase sqlDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailylog);

        Intent intent = getIntent();

        // 정보 전송 버튼 클릭
        Button btn_SendData = (Button) findViewById(R.id.btn_sendData);
        btn_SendData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                et_date = (EditText) findViewById(R.id.et_date);
                String date = et_date.getText().toString();

                // editText 형으로 숫자 받아오기
                et_temp = (EditText) findViewById(R.id.temp_text);
                et_rootT = (EditText) findViewById(R.id.rootT_text);
                et_humid = (EditText) findViewById(R.id.humid_text);
                et_co2 = (EditText) findViewById(R.id.co2_text);

                // editText -> String형 변환
                String s_temp = et_temp.getText().toString();
                String s_rootT = et_rootT.getText().toString();
                String s_humid = et_humid.getText().toString();
                String s_co2 = et_co2.getText().toString();

                //long 형으로 변환
                temp = Long.parseLong(s_temp);
                rootT = Long.parseLong(s_rootT);
                humid = Long.parseLong(s_humid);
                co2 = Long.parseLong(s_co2);

                String dbName = "TKLabsDB";
                try {
                    String databasePath = getFilesDir().getPath() + "/" + dbName;
                    myHelper = new DBHelper(WriteDailyLog.this, databasePath, null);
                    sqlDB = myHelper.getWritableDatabase();
                    sqlDB.execSQL("INSERT INTO plantTable VALUES ('" + date + "', " + temp + ", " + rootT + ", " + humid + ", " + co2 + ");");
                    sqlDB.close();
                    finish();
                    Intent intent =  new Intent(WriteDailyLog.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "입력되었습니다.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "모든 값을 입력 해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String receive_date = intent.getStringExtra("send_date");

        final Calendar calendar = Calendar.getInstance();
        final int cYear = calendar.get(Calendar.YEAR);
        final int cMonth = calendar.get(Calendar.MONTH);
        final int cDay = calendar.get(Calendar.DAY_OF_MONTH);
        et_date = (EditText) findViewById(R.id.et_date);

        if(isEmpty(receive_date)){
            receive_date = cYear + "-" +
                    (cMonth > 8 ? (cMonth + 1) : "0" + (cMonth + 1)) + "-" +
                    (cDay > 9 ? cDay : "0" + cDay);
        }

        // sensor_p1 글씨 검정색으로 설정 //
        tv_sensor = (TextView) findViewById(R.id.tv_sensor);
        SpannableStringBuilder builder = new SpannableStringBuilder("sensor_p1");
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv_sensor.append(builder);

        et_date.setText(receive_date);
        //et_date.setText(cYear + "-" +
        //        (cMonth > 8 ? (cMonth + 1) : "0" + (cMonth + 1)) + "-" +
        //        (cDay > 9 ? cDay : "0" + cDay));
        et_date.setInputType(0);

        et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener mDateSetListner = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        String date_selected = String.valueOf(year) + "-" +
                                String.valueOf(month > 8 ? (month + 1) : "0" + (month + 1)) + "-" +
                                String.valueOf(day > 9 ? day : "0" + day);
                        et_date.setText(date_selected);
                    }};
                DatePickerDialog dialog = new DatePickerDialog(WriteDailyLog.this, mDateSetListner, cYear, cMonth, cDay);
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                dialog.show();
            }
        });
        }

    // 뒤로가기 버튼 : 출처 : http://ccdev.tistory.com/12
    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this

        // 여기서 부터는 알림창의 속성 설정
        builder .setMessage("일지 작성을 취소 하시겠습니까?")        // 메세지 설정
                .setCancelable(true) // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                //.setTitle("종료 확인 대화 상자")        // 제목 설정
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        finish();
                        MainActivity.bln_save = true;
                    }
                })

                .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();    // 알림창 띄우기
    }

}
