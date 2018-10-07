package com.example.ryu_w.calendar;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.time.Month;
import java.util.Calendar;

import static com.example.ryu_w.calendar.MainActivity.currentDay;

public class SaturdayDecorator implements DayViewDecorator {

    private final Calendar calendar = Calendar.getInstance();

    public  SaturdayDecorator(){
    }

    @Override
    public boolean shouldDecorate(CalendarDay day){
        day.copyTo(calendar);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);

        return weekDay == calendar.SATURDAY;
    }

    @Override
    public void decorate(DayViewFacade view){
        view.addSpan(new ForegroundColorSpan(Color.BLUE));
    }

}
