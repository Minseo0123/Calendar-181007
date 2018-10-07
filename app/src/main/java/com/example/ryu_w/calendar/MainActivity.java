package com.example.ryu_w.calendar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    MaterialCalendarView materialCalendarView;
    DBHelper myHelper;
    SQLiteDatabase sqlDB;
    public static Context context;
    public static boolean bln_save = false;
    public boolean sv_isvisible = false;
    public ScrollView sv1;
    public Cursor cursor;
    public String[] result;
    public String send_date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DB 연결//
        String dbName = "TKLabsDB";
        String databasePath = getFilesDir().getPath() + "/" + dbName;
        myHelper = new DBHelper(MainActivity.this, databasePath, null);
        sqlDB = myHelper.getReadableDatabase();

        context = this;

        // 캘린더 만들기 //
        materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        materialCalendarView.setTopbarVisible(false);

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        top_date();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                new onDayDecorator()
        );

        // 달 바뀔 때 위에 topBar 달,년 바뀌게 //
        materialCalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                top_date();
            }
        });

        // 캘린더 - 특정일 지정 //
        cursor = sqlDB.rawQuery("SELECT datetime FROM plantTable;", null);;
        final List<String> arr_date = new ArrayList<String>();
        while(cursor.moveToNext()){
            arr_date.add(cursor.getString(0).substring(0, 10));
        }
        result = new String[arr_date.size()];
        arr_date.toArray(result);
        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());
        cursor.moveToFirst();

        // 캘린더 - 클릭이벤트 //
        sv1 = (ScrollView) findViewById(R.id.sv);
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                TextView tv_nodata = (TextView) findViewById(R.id.no_data);
                TextView tv_data = (TextView) findViewById(R.id.data);
                sv1.setVisibility(View.VISIBLE);
                sv_isvisible = true;
                int i=0;
                String datetime = date.getYear() + "-" +
                        (date.getMonth() > 8 ? date.getMonth() + 1 : "0" + (date.getMonth()+1))  + "-" +
                        (date.getDay() > 9 ? date.getDay() : "0" + date.getDay());
                send_date = datetime;

                while (datetime.equals(result[i]) == false ){
                     i++;
                     if(i >= result.length) break;
                }
                if (i < result.length) {
                    String datta = "";
                    for (int j=0; j < data_view(datetime).length; j++){
                        datta += data_view(datetime)[j] + "\n\n" ;
                    }
                    tv_data.setText(datta);
                    tv_nodata.setVisibility(View.GONE);
                    tv_data.setVisibility(View.VISIBLE);
                } else {
                    tv_nodata.setVisibility(View.VISIBLE);
                    tv_data.setVisibility(View.GONE);
                }
            }
        });

        // 데이터 쓰기 버튼 //
        Button btn_Add = (Button) findViewById(R.id.btn_add);
        btn_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WriteDailyLog.class);
                intent.putExtra("send_date", send_date); // 선택 날짜 데이터 보냄
                startActivity(intent);
                if(bln_save == true){
                    onResume();
                    bln_save = false;
                }
            }
        });

        // 오늘 버튼 //
        Button btn_Today = (Button) findViewById(R.id.btn_today);
        btn_Today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarDay today = CalendarDay.today(); // 오늘 날짜 받아옴.
                materialCalendarView.setCurrentDate(today, true); // 오늘 날짜로 설정.
                top_date();
            }
        });
    }

    // 점찍는 API
    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        String[] Time_Result;
        ApiSimulator(String[] Time_Result){
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 -를 기준으로짜르고 string을 int 로 변환
            for(int i = 0 ; i < Time_Result.length ; i ++){
                String[] time = Time_Result[i].split("-");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int dayy = Integer.parseInt(time[2]);

                calendar.set(year,month-1,dayy);
                CalendarDay day = CalendarDay.from(calendar);
                dates.add(day);
            }
            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (isFinishing()) {
                return;
            }

            materialCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays));
        }
    }

    public String[] data_view(String date) {
        cursor = sqlDB.rawQuery("SELECT * FROM plantTable WHERE datetime like '" + date + "%';", null);;
        final List<String> arr_data = new ArrayList<String>();
        while(cursor.moveToNext()){
            arr_data.add("기온 : " + cursor.getString(1) + "℃  뿌리 온도 : " + cursor.getString(2) + "℃\n습도 : " +
                    cursor.getString(3) + "%  CO2 : "+ cursor.getString(4) + "ppm");
        }
        String[] arr_result;
        arr_result = new String[arr_data.size()];
        arr_data.toArray(arr_result);
        cursor.close();
        return arr_result;
    }

    // 종료 뒤로가기 출처: http://best421.tistory.com/71 [Updates available]
    long pressedTime = 0;
    @Override
    public void onBackPressed() {
        if (sv_isvisible == true){
            sv1.setVisibility(View.INVISIBLE);
            sv_isvisible = false;
        } else if ( pressedTime == 0 ) {
            Toast.makeText(MainActivity.this, " 한 번 더 누르시면 종료됩니다." , Toast.LENGTH_SHORT).show();
            pressedTime = System.currentTimeMillis();
        } else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);

            if ( seconds > 1500 ) {
                Toast.makeText(MainActivity.this, " 한 번 더 누르시면 종료됩니다." , Toast.LENGTH_SHORT).show();
                pressedTime = 0 ;
            }
            else {
                sqlDB.close();
                super.onBackPressed();
                finish(); // app 종료 시키기
            }
        }
    }

    static String[] currentDay;
    public void top_date(){
        // 월, 년 표시M
        TextView tv_Month = (TextView)findViewById(R.id.month);
        TextView tv_Year = (TextView)findViewById(R.id.year);
        CalendarDay crntDay = materialCalendarView.getCurrentDate();
        currentDay = crntDay.toString().substring(12,19).split("-");
        tv_Month.setText(Integer.parseInt(currentDay[1]) + 1 +"월");
        tv_Year.setText(currentDay[0]);
    }
}
