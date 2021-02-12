package com.example.scheduleapp;

import androidx.core.text.HtmlCompat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Subjectクラスに関するユーティリティ
 */
public class SubjectUtility {

    /**
     * Json形式で渡されてきたイベントデータをSubject型を持つリストへ変換
     * @param jsonString Json形式の文字列
     */
    public static List<Subject> calendarJsonToSubjectList(String jsonString) throws JsonSyntaxException {
        List<ArrayList> yearDataList =  new Gson().fromJson(jsonString,List.class);
        List<Subject> subjectList = new ArrayList<>();
        Map<Integer,Subject> subjectMap= new HashMap<>();

        for(ArrayList aList  : yearDataList) {
            LinkedTreeMap jsonMap = (LinkedTreeMap) aList.get(0);
            LinkedTreeMap dataTreeMap = (LinkedTreeMap) jsonMap.get("data");
            LinkedTreeMap dateTreeMap = (LinkedTreeMap) dataTreeMap.get("date");
            Integer year = ((Double)dateTreeMap.get("year")).intValue();
            Integer month = ((Double)dateTreeMap.get("mon")).intValue();
            ArrayList<LinkedTreeMap> weekMap =(ArrayList<LinkedTreeMap>) dataTreeMap.get("weeks");
            for(LinkedTreeMap weekTreeMap : weekMap){
                ArrayList<LinkedTreeMap> days = (ArrayList<LinkedTreeMap>)weekTreeMap.get("days");
                for(LinkedTreeMap dayTreeMap :days){
                    //イベントを持っているか
                    if(!(boolean)dayTreeMap.get("hasevents")){
                        continue;
                    }
                    //日を取得
                    Integer day = ((Double) dayTreeMap.get("mday")).intValue();
                    //イベント情報取得
                    ArrayList<LinkedTreeMap> events = (ArrayList)dayTreeMap.get("events");
                    for(LinkedTreeMap eventTreeMap : events){
                        //各イベントデータ取得
                        //イベントタイプを取得。
                        String courseName = (String)eventTreeMap.get("calendareventtype");
                        //コースイベントならそのまま、それ以外ならイベントを後ろにつける
                        if(courseName.equals("course")) {
                            courseName = (String) ((LinkedTreeMap) eventTreeMap.get("course")).get("fullname");
                        }
                        else{
                            courseName+="イベント";
                        }
                        String title = ((String)eventTreeMap.get("name")).replaceAll("( の提出期限が到来しています。)$","");
                        String description = (String)eventTreeMap.get("description");
                        if(description.equals("")){
                            description = "<p></p>";
                        }
                        String formattedTime =(String)eventTreeMap.get("formattedtime");

                        //イベントIDを取得
                        Integer eventId = ((Double)eventTreeMap.get("id")).intValue();

                        //時刻を取得
                        String timeString = HtmlCompat.fromHtml(formattedTime,HtmlCompat.FROM_HTML_MODE_COMPACT).toString();
                        List<Integer[]> timeList = new ArrayList<>();
                        Calendar startTime = null;
                        Calendar endTime;

                        Pattern aPattern = Pattern.compile("[0-9][0-9]:[0-9][0-9]");
                        Matcher aMacher = aPattern.matcher(timeString);
                        while(aMacher.find()){
                            Integer[] anIntegerArray = new Integer[2];
                            String[] aStringList = aMacher.group().split(":");
                            anIntegerArray[0] = Integer.valueOf(aStringList[0]);
                            anIntegerArray[1] = Integer.valueOf(aStringList[1]);
                            //先頭に追加することで最初の時間は必ず終了時間になる
                            timeList.add(0,anIntegerArray);
                        }

                        endTime = DayUtility.createCalendar(year,month,day,timeList.get(0)[0],timeList.get(0)[1],0);
                        if(timeList.size()>1){
                            startTime =  DayUtility.createCalendar(year,month,day,timeList.get(1)[0],timeList.get(1)[1],0);
                        }
                        if(subjectMap.containsKey(eventId)){
                            Subject addedSubject = subjectMap.get(eventId);
                            if(startTime!=null){
                                if(addedSubject.getStartTime()==null || startTime.compareTo(addedSubject.getStartTime())<0){
                                    addedSubject.setStartTime(startTime);
                                }
                            }
                            if(endTime.compareTo(addedSubject.getEndTime())>0){
                                addedSubject.setEndTime(endTime);
                            }
                            continue;
                        }
                        Subject aSubject = new Subject(eventId,title,description,courseName,startTime,endTime);
                        //現在時刻よりもカレンダーの予定が過去のものなら保存しない
//                        if(aSubject.getRepresentativeTime()<System.currentTimeMillis())
//                            continue;

                        System.out.println(aSubject.getTitle());
                        if(aSubject.getStartTime()!=null) System.out.println(aSubject.getStartTime().getTime());
                        System.out.println(aSubject.getEndTime().getTime());
                        subjectList.add(aSubject);
                        subjectMap.put(eventId,aSubject);
                    }
                }
            }
        }
        return subjectList;
    }
}

