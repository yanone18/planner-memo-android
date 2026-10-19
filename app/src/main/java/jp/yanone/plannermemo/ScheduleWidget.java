package jp.yanone.plannermemo;
import android.app.*;import android.appwidget.*;import android.content.*;import android.widget.*;
public class ScheduleWidget extends AppWidgetProvider {
 public static void refresh(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);android.content.ComponentName cn=new android.content.ComponentName(c,ScheduleWidget.class);int[] ids=m.getAppWidgetIds(cn);for(int id:ids){m.notifyAppWidgetViewDataChanged(id,R.id.widget_list);update(c,m,id);}}
 static void update(Context c,AppWidgetManager m,int id){
 boolean memo=c.getSharedPreferences("widget",Context.MODE_PRIVATE).getBoolean("memo_"+id,false);RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget);Intent i=new Intent(c,ScheduleService.class);i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);i.setData(android.net.Uri.parse(i.toUri(Intent.URI_INTENT_SCHEME)));v.setRemoteAdapter(R.id.widget_list,i);PendingIntent p=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);v.setOnClickPendingIntent(R.id.widget_open,p);
 v.setTextColor(R.id.widget_schedule,memo?0xff526175:0xffffffff);
 v.setTextColor(R.id.widget_memo,memo?0xffffffff:0xff526175);
 v.setInt(R.id.widget_schedule,"setBackgroundResource",memo?R.drawable.widget_tab_inactive:R.drawable.widget_tab_active);
 v.setInt(R.id.widget_memo,"setBackgroundResource",memo?R.drawable.widget_tab_active:R.drawable.widget_tab_inactive);
 for(int n=0;n<2;n++){Intent toggle=new Intent(c,ScheduleWidget.class).setAction("TOGGLE_"+n).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);toggle.setData(android.net.Uri.parse("plannermemo://widget/"+id+"/tab/"+n));PendingIntent pi=PendingIntent.getBroadcast(c,id*2+n,toggle,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);v.setOnClickPendingIntent(n==0?R.id.widget_schedule:R.id.widget_memo,pi);}
 m.updateAppWidget(id,v);}
 @Override public void onReceive(Context c,Intent i){super.onReceive(c,i);String a=i==null?null:i.getAction();if(Intent.ACTION_DATE_CHANGED.equals(a)||Intent.ACTION_TIMEZONE_CHANGED.equals(a)||Intent.ACTION_TIME_CHANGED.equals(a)){refresh(c);return;}if(a!=null&&a.startsWith("TOGGLE_")){int id=i.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);if(id>=0){c.getSharedPreferences("widget",Context.MODE_PRIVATE).edit().putBoolean("memo_"+id,a.equals("TOGGLE_1")).apply();AppWidgetManager m=AppWidgetManager.getInstance(c);m.notifyAppWidgetViewDataChanged(id,R.id.widget_list);update(c,m,id);}}}
 @Override public void onDeleted(Context c,int[] ids){super.onDeleted(c,ids);android.content.SharedPreferences.Editor e=c.getSharedPreferences("widget",Context.MODE_PRIVATE).edit();for(int id:ids)e.remove("memo_"+id);e.apply();}
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)update(c,m,id);m.notifyAppWidgetViewDataChanged(ids,R.id.widget_list);}
}
