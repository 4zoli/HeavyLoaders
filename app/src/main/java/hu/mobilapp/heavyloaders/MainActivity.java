package hu.mobilapp.heavyloaders;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hu.mobilapp.heavyloaders.Adapter.AppAdapter;
import hu.mobilapp.heavyloaders.Model.AppInfo;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean mIncludeSystemApps;
    Switch sysIsCheckedSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        listView.setTextFilterEnabled(true);
        sysIsCheckedSwitch = findViewById(R.id.sysOnOffSwitch);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo app = (AppInfo)parent.getItemAtPosition(position);

                Intent myintent = getPackageManager().getLaunchIntentForPackage(app.info.packageName);
                if(myintent == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.appLauncher_notFound), Toast.LENGTH_LONG).show();
                } else startActivity(myintent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               refreshIt();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshIt();
    }

    private void refreshIt() {
        LoadAppInfoTask loadAppInfoTask = new LoadAppInfoTask();
        loadAppInfoTask.execute(PackageManager.GET_META_DATA);
    }

    class LoadAppInfoTask extends AsyncTask<Integer, Integer, List<AppInfo>>    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected List<AppInfo> doInBackground(Integer... params) {
            List<AppInfo> apps = new ArrayList<>();
            PackageManager packageManager = getPackageManager();
            List<ApplicationInfo> infos = packageManager.getInstalledApplications(params[0]);

            if(sysIsCheckedSwitch.isChecked() == true) {
                mIncludeSystemApps = true;
            }   else mIncludeSystemApps = false;

            for (ApplicationInfo info:infos)    {
               if(!mIncludeSystemApps && (info.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                     continue;
                }
                AppInfo app = new AppInfo();
                app.info = info;
                app.label = (String)info.loadLabel(packageManager);
                File file = new File(app.info.publicSourceDir);
                double size = file.getTotalSpace();
                apps.add(app);
            }
            Collections.sort(apps,new mComparator());
            return apps;
        }

        @Override
        protected void onPostExecute(List<AppInfo> appInfos) {
            super.onPostExecute(appInfos);
            listView.setAdapter(new AppAdapter(MainActivity.this,appInfos));
            swipeRefreshLayout.setRefreshing(false);
            String loaded_apps_number = getString(R.string.loaded_apps_number);
            Snackbar.make(listView, appInfos.size() + " " + loaded_apps_number, Snackbar.LENGTH_LONG).show();
        }
    }

    private class mComparator implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo aa, AppInfo ab) {
            File f1 = new File(aa.info.sourceDir);
            File f2 = new File(ab.info.sourceDir);
            double sa = f1.getAbsoluteFile().length();
            double sb = f2.getAbsoluteFile().length();

            return Double.compare(sb,sa);
        }
    }
}
