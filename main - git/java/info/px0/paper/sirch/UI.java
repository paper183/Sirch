package info.px0.paper.sirch;

import info.px0.paper.sirch.config.GlobalConfig;
import info.px0.paper.sirch.config.ResultConfig;
import info.px0.paper.sirch.config.DisplayResultConfig;
import info.px0.paper.sirch.config.ServerConfig;
import info.px0.paper.sirch.config.RoomConfig;
import info.px0.paper.sirch.irc.IRCHandler;
import info.px0.paper.sirch.settings.SettingsActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UI {

    private static boolean bInitConf = true;

    private ResultArrayAdapter aaAdapter;
    private static ArrayList<DisplayResultConfig> alResults = new ArrayList<>();
    private DownloadArrayAdapter aaDownloads;
    private static ArrayList<DisplayResultConfig> alDownloads = new ArrayList<>();
    private static Handler hdlMain;
    private static WeakReference<Context> mContext;

    void initialize() {
        hdlMain = new Handler(Looper.getMainLooper()){

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1: //String debug
                        String strMsg = (String) msg.obj;
                        addLine(strMsg);
                        break;
                    case 2: //Result
                        DisplayResultConfig resAdd = new DisplayResultConfig((ResultConfig)msg.obj);
                        addResult(resAdd);
                        break;
                    case 3: //DCCUpdate
                        DisplayResultConfig drcUpdate = (DisplayResultConfig) msg.obj;
                        updateDownload(drcUpdate);
                        break;
                    case 4: //Toast messages (Add concatenation ? Ex: connected to.... / joined #... // connected to... / joined #...)
                        String strToast = (String) msg.obj;
                        addLine("Toast: " + strToast);
                        Toast.makeText(UI.mContext.get(), strToast, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        GlobalConfig.updateHdl(hdlMain);
    }

    public static boolean isbInitConf() {
        return bInitConf;
    }

    public static void setbInitConf(boolean bInitConf) {
        UI.bInitConf = bInitConf;
    }

    public static WeakReference<Context> getmContext() {
        return mContext;
    }

    static void setmContext(WeakReference<Context> mContext) {
        UI.mContext = mContext;
    }

    public static Handler getHdlMain() {
        return hdlMain;
    }

    public static void setHdlMain(Handler hdlMain) {
        UI.hdlMain = hdlMain;
    }

    public ArrayList<DisplayResultConfig> getAlDownloads() {
        return alDownloads;
    }

    public void setAlDownloads(ArrayList<DisplayResultConfig> alDownloads) {
        UI.alDownloads = alDownloads;
    }

    public ArrayAdapter<DisplayResultConfig> getAaDownloads() {
        return aaDownloads;
    }

    public void setAaDownloads(DownloadArrayAdapter aaDownloads) {
        this.aaDownloads = aaDownloads;
    }

    public ArrayList<DisplayResultConfig> getAlResults() {
        return alResults;
    }

    public void setAlResults(ArrayList<DisplayResultConfig> alResults) {
        UI.alResults = alResults;
    }

    public ArrayAdapter<DisplayResultConfig> getAaAdapter() {
        return aaAdapter;
    }

    public void setAaAdapter(ResultArrayAdapter aaAdapter) {
        this.aaAdapter = aaAdapter;
    }

    private void addResult(DisplayResultConfig rcResult) {
        aaAdapter.add(rcResult);
    }

    private void addDownload(DisplayResultConfig drcDownload) {
        if (aaDownloads == null)
            alDownloads.add(drcDownload);
        else
            aaDownloads.add(drcDownload);
    }

    public static void dialogSettingsInvalid(String strTitle, String strMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
        builder.setMessage(strMsg)
                .setTitle(strTitle)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(mContext.get(), SettingsActivity.class);
                        mContext.get().startActivity(i);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void dialogInfo(String strTitle, String strMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
        builder.setMessage(strMsg)
                .setTitle(strTitle)
                .setPositiveButton(R.string.btn_ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateDownload(DisplayResultConfig drcDownload) {
        for (int i = 0; i < alDownloads.size(); i++) {
            if (alDownloads.get(i).equals(drcDownload)) {
                switch (drcDownload.getiStatus()) {
                    case 2://if new dcc connection initiated by user privmsg
                        switch (alDownloads.get(i).getiStatus()) {
                            case 1:
                            case 5:
                                GlobalConfig.newDccHandler(drcDownload);
                                break;
                        }
                        break;
                    case 5://only update status and extra text from accepted request, in case there's more info already (filename, size), using specific constructor
                    case 6:
                        drcDownload = new DisplayResultConfig(alDownloads.get(i), drcDownload.getiStatus(), drcDownload.getStrExtra());
                        break;
                }
                alDownloads.set(i, drcDownload);
                if (aaDownloads != null) aaDownloads.notifyDataSetChanged();
            }
        }
    }

    /*public static DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };*/

    void clearResult() {
        aaAdapter.clear();
    }

    /*public ListView getLvResults() {
        return lvResults;
    }*/

    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    void setLvResults(ListView lvResults, Context uiContext) {
        //this.lvResults = lvResults;
        aaAdapter = new ResultArrayAdapter(uiContext, alResults);
        lvResults.setAdapter(aaAdapter);

        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id)
            {
                DisplayResultConfig drcRequested = aaAdapter.getItem(position);
                if (drcRequested != null) drcRequested.setiStatus(1);
                GlobalConfig.addRequest(drcRequested);
                addDownload(drcRequested);
                aaAdapter.remove(drcRequested);
            }
        });
    }

    private class ResultArrayAdapter extends ArrayAdapter<DisplayResultConfig> {
        private final Context context;
        private final ArrayList<DisplayResultConfig> alRes;

        ResultArrayAdapter(Context context, ArrayList<DisplayResultConfig> alResp) {
            super(context, R.layout.result_layout, alResp);
            this.context = context;
            this.alRes = alResp;
        }
        @Override @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder mViewHolder;

            if (convertView == null) {
                mViewHolder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = inflater.inflate(R.layout.result_layout, parent, false);

                mViewHolder.textView1 = convertView.findViewById(R.id.firstLine);
                mViewHolder.textView3 = convertView.findViewById(R.id.secondLine);
                mViewHolder.textView2 = convertView.findViewById(R.id.fromLine);
                mViewHolder.imageView = convertView.findViewById(R.id.icon);

                convertView.setTag(mViewHolder);
            }
            else
                mViewHolder = (ViewHolder) convertView.getTag();

            DisplayResultConfig drc = alRes.get(position);

            String strFrom = context.getString(R.string.result_from) + " " + drc.getStrNickname() + " " + drc.getStrRooms();

            mViewHolder.textView1.setText(fromHtml(drc.getStrFilename()).toString());
            mViewHolder.textView2.setText(drc.getReadableSize());
            mViewHolder.textView3.setText(strFrom);

            mViewHolder.imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp);

            return convertView;
        }

    }

    /*public void setLvResults(ListView lvResults) {
        this.lvResults = lvResults;
    }*/

    /*public ListView getLvDownloads() {
        return lvDownloads;
    }*/
    private static class ViewHolder {
        private TextView textView1;
        private TextView textView2;
        private TextView textView3;
        private ImageView imageView;
    }

    void setLvDownloads(ListView lvDownloads, Context uiContext) {
        //lvDownloads = lvDownloads;
        aaDownloads = new DownloadArrayAdapter(uiContext, alDownloads);
        lvDownloads.setAdapter(aaDownloads);
    }

    private class DownloadArrayAdapter extends ArrayAdapter<DisplayResultConfig> {
        private final Context context;
        private final ArrayList<DisplayResultConfig> alDown;

        DownloadArrayAdapter(Context context, ArrayList<DisplayResultConfig> alDownp) {
            super(context, R.layout.download_layout, alDownp);
            this.context = context;
            alDown = alDownp;
        }

        @Override @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder mViewHolder;

            if (convertView == null) {
                mViewHolder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = inflater.inflate(R.layout.download_layout, parent, false);

                mViewHolder.textView1 = convertView.findViewById(R.id.firstLine);
                mViewHolder.textView2 = convertView.findViewById(R.id.secondLine);
                mViewHolder.textView3 = convertView.findViewById(R.id.sizeLine);
                mViewHolder.imageView = convertView.findViewById(R.id.icon);

                convertView.setTag(mViewHolder);
            }
            else
                mViewHolder = (ViewHolder) convertView.getTag();

            DisplayResultConfig drc = alDown.get(position);
            mViewHolder.textView1.setText(fromHtml(drc.getStrFilename()).toString());

            String strSt;
            int icon;


            switch (drc.getiStatus()) {
                case 0:
                    strSt = context.getString(R.string.dl_st_waiting);
                    icon = R.drawable.ic_hourglass_empty_black_24dp;
                    break;
                case 1:
                    strSt = context.getString(R.string.dl_st_requested);
                    icon = R.drawable.ic_hourglass_empty_black_24dp;
                    break;
                case 2:
                    strSt = context.getString(R.string.dl_st_incoming);
                    icon = R.drawable.ic_file_download_black_24dp;
                    break;
                case 3:
                    strSt = context.getString(R.string.dl_st_downloading) + " " + drc.getiProgress() + "%";
                    icon = R.drawable.ic_file_download_black_24dp;
                    break;
                case 4:
                    strSt = context.getString(R.string.dl_st_completed);
                    icon = R.drawable.ic_done_black_24dp;
                    break;
                case 5:
                    strSt = context.getString(R.string.dl_st_accepted) + (drc.getStrExtra().isEmpty() ? "" : " " + drc.getStrExtra());
                    icon = R.drawable.ic_query_builder_black_24dp;
                    break;
                case 6:
                    strSt = context.getString(R.string.dl_st_denied) + (drc.getStrExtra().isEmpty() ? "" : " " + drc.getStrExtra());
                    icon = R.drawable.ic_error_outline_black_24dp;
                    break;
                case 88:
                    strSt = context.getString(R.string.dl_st_reversedcc);
                    icon = R.drawable.ic_warning_black_24dp;
                    break;
                //TODO: Add user accepted or too many files etc... detect with privmsg, test accuracy
                default:
                    strSt = context.getString(R.string.dl_st_error);
                    icon = R.drawable.ic_warning_black_24dp;
                    break;
            }


            strSt = drc.getStrNickname() + " - " + strSt;

            mViewHolder.textView2.setText(strSt);

            mViewHolder.textView3.setText(drc.getReadableSize());

            mViewHolder.imageView.setImageResource(icon);

            return convertView;
        }

    }


    public static void addLine(String sLine){
        if (BuildConfig.DEBUG) {
            Log.d("Sirch",sLine);
        }
    }

    static void searchSend(String strUserInput){
        // TODO log search and time temporarily, toast user msg if searching too fast or same thing twice too fast etc. Maybe check if joined to rooms/had time to try
        try {
            for (IRCHandler ircHandlers: GlobalConfig.getIrcHandles())
            {
                //addLine("DEBUG: " + ircHandlers.getScServer().getStrHostname() + " " + ircHandlers.getScServer().getIsConnected());
                if (ircHandlers.getScServer().getIsConnected()) {
                    for (RoomConfig servRooms : ircHandlers.getScServer().getRcRooms())
                    {
                        if (servRooms.getbJoined())
                            ircHandlers.getLbqSend().put("PRIVMSG " + servRooms.getStrRoomName() + " :" + servRooms.getStrFindCmd() + " " + strUserInput);
                        else
                            servRooms.setStrDelayedSearch(strUserInput);
                    }
                }
                else
                    for (RoomConfig servRooms : ircHandlers.getScServer().getRcRooms())
                        servRooms.setStrDelayedSearch(strUserInput);
            }
            //etUserInput.setText("");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static void delayedSearch(String strUserInput){
            for (IRCHandler ircHandlers: GlobalConfig.getIrcHandles())
                for (RoomConfig servRooms : ircHandlers.getScServer().getRcRooms())
                    servRooms.setStrDelayedSearch(strUserInput);
    }

}
