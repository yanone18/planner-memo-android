package jp.yanone.plannermemo;
import android.content.*;import android.widget.*;import android.app.*;import org.json.*;import java.time.*;import java.time.format.*;import java.util.*;
public class ScheduleService extends RemoteViewsService {
 @Override public RemoteViewsFactory onGetViewFactory(Intent intent){return new Factory(getApplicationContext(),intent.getIntExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,-1));}
 static class Factory implements RemoteViewsFactory {
  final Context ctx;final int widgetId;JSONArray rows=new JSONArray();Factory(Context c,int id){ctx=c;widgetId=id;}
  @Override public void onCreate(){onDataSetChanged();}
  @Override public void onDataSetChanged(){rows=new JSONArray();try{JSONObject s=new JSONObject(ctx.getSharedPreferences("widget",Context.MODE_PRIVATE).getString("state","{}"));if(ctx.getSharedPreferences("widget",Context.MODE_PRIVATE).getBoolean("memo_"+widgetId,false)){String memo=s.optString("memo","");for(String line:memo.split("\n",-1)){JSONObject r=new JSONObject();r.put("memoLine",line);rows.put(r);}return;}JSONObject schedule=s.optJSONObject("schedule");if(schedule==null)schedule=new JSONObject();JSONObject eventColors=s.optJSONObject("eventColors");LocalDate today=LocalDate.now();for(int n=0;n<365;n++){LocalDate d=today.plusDays(n);String key=d.toString(),value=schedule.optString(key,"");JSONObject row=new JSONObject();row.put("date",key);row.put("value",value);
if(eventColors!=null){JSONObject style=eventColors.optJSONObject(key);if(style!=null)row.put("style",style);}
rows.put(row);}}catch(Exception ignored){}}
  @Override public void onDestroy(){}
  @Override public int getCount(){return rows.length();}
  @Override public RemoteViews getViewAt(int pos){if(pos<0||pos>=rows.length())return null;RemoteViews v=new RemoteViews(ctx.getPackageName(),R.layout.widget_row);try{JSONObject row=rows.getJSONObject(pos);if(row.has("memoLine")){RemoteViews mv=new RemoteViews(ctx.getPackageName(),R.layout.widget_memo_row);mv.setTextViewText(R.id.memo_line,row.optString("memoLine"));return mv;}LocalDate d=LocalDate.parse(row.getString("date"));String[] days={"月","火","水","木","金","土","日"};v.setTextViewText(R.id.day,d.getDayOfMonth()+"日("+days[d.getDayOfWeek().getValue()-1]+")");v.setTextColor(R.id.day,d.getDayOfWeek()==DayOfWeek.SUNDAY?0xffc03d4a:d.getDayOfWeek()==DayOfWeek.SATURDAY?0xff1769bd:0xff526175);String value=row.optString("value");
// Always clear recycled widget text state before applying this date's styles.
v.setTextViewText(R.id.event,value);
v.setTextViewTextSize(R.id.event,android.util.TypedValue.COMPLEX_UNIT_SP,15f);
// Month headings are shown in the scrolling list, including the first month.
boolean monthStart=pos==0 || d.getDayOfMonth()==1;
v.setViewVisibility(R.id.month_heading,monthStart?android.view.View.VISIBLE:android.view.View.GONE);
v.setTextViewText(R.id.month_heading,d.getYear()+"年"+d.getMonthValue()+"月");
// Add a narrow side bearing to each colored event without changing row height.
JSONObject styles=row.optJSONObject("style");
java.util.regex.Matcher scan=java.util.regex.Pattern.compile("[^\\s\\u3000]+").matcher(value);
StringBuilder padded=new StringBuilder();int previous=0;int token=0;
while(scan.find()){
 padded.append(value,previous,scan.start());
 JSONObject st=styles==null?null:(styles.has("bg")?(token==0?styles:null):styles.optJSONObject(String.valueOf(token)));
 boolean colored=st!=null&&!st.optString("bg","").isEmpty();
 if(colored)padded.append('\u2002');
 padded.append(value,scan.start(),scan.end());
 if(colored)padded.append('\u2002');
 previous=scan.end();token++;
}
padded.append(value.substring(previous));
android.text.SpannableString decorated=new android.text.SpannableString(padded.toString());
java.util.regex.Matcher matcher=java.util.regex.Pattern.compile("[^\\s\\u3000\\u2002]+").matcher(padded.toString());
int partIndex=0;boolean hasColored=false;
while(matcher.find()){
 JSONObject style=styles==null?null:(styles.has("bg")?(partIndex==0?styles:null):styles.optJSONObject(String.valueOf(partIndex)));
 if(style!=null){String bg=style.optString("bg","");try{
 int background=android.graphics.Color.parseColor(bg);
 int foreground=android.graphics.Color.parseColor(style.optString("fg","#FFFFFF"));
 int from=matcher.start(),to=matcher.end();
 if(from>0&&padded.charAt(from-1)=='\u2002')from--;
 if(to<padded.length()&&padded.charAt(to)=='\u2002')to++;
 decorated.setSpan(new android.text.style.BackgroundColorSpan(background),from,to,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 decorated.setSpan(new android.text.style.ForegroundColorSpan(foreground),from,to,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 hasColored=true;
 }catch(Exception ignored){}}
 partIndex++;
}
v.setTextViewText(R.id.event,decorated);
if(hasColored)v.setTextViewTextSize(R.id.event,android.util.TypedValue.COMPLEX_UNIT_SP,14.5f);
}catch(Exception ignored){}return v;}
  @Override public RemoteViews getLoadingView(){return null;}
  @Override public int getViewTypeCount(){return 2;}
  @Override public long getItemId(int p){return p;}
  @Override public boolean hasStableIds(){return false;}
 }
}
