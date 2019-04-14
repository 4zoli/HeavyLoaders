package hu.mobilapp.heavyloaders.Adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Formatter;
import java.util.List;

import hu.mobilapp.heavyloaders.Model.AppInfo;
import hu.mobilapp.heavyloaders.R;

public class AppAdapter extends ArrayAdapter<AppInfo> {


    LayoutInflater layoutInflater;
    PackageManager packageManager;
    List<AppInfo> apps;

    public AppAdapter(Context context, List<AppInfo> apps) {
        super(context, R.layout.app_item_layout, apps);
        layoutInflater = LayoutInflater.from(context);
        packageManager = context.getPackageManager();
        this.apps = apps;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AppInfo current = apps.get(position);
        View view = convertView;

        if(view == null)    {
            view = layoutInflater.inflate(R.layout.app_item_layout,parent,false);
        }

        TextView textViewTitle = (TextView)view.findViewById(R.id.titleTextView);
        textViewTitle.setText(current.label);

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(current.info.packageName,0);

            if(!TextUtils.isEmpty(packageInfo.versionName)){
                String versionInfo = String.format("%s",packageInfo.versionName);
                TextView textVersion = (TextView)view.findViewById(R.id.versionId);
                textVersion.setText(versionInfo);
            }

            if(!TextUtils.isEmpty(current.info.packageName)){
                TextView textSubTitle = (TextView)view.findViewById(R.id.subTitle);
                textSubTitle.setText(current.info.packageName);
            }

            if(!TextUtils.isEmpty(current.info.sourceDir)){
                TextView sizeTextView = (TextView)view.findViewById(R.id.sizeTextView);
                File file = new File(current.info.sourceDir);
                //sizeTextView.setText(String.valueOf(file.length() / 1000000.0) + "MB");
                double unFormattedSizeText = (double) (file.length()/1000000.0);

                StringBuilder stringBuilder = new StringBuilder();
                Formatter formatter = new Formatter(stringBuilder);
                formatter.format("%.2f", unFormattedSizeText);
                sizeTextView.setText(stringBuilder.toString() + "MB");
            }



        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ImageView imageView = (ImageView)view.findViewById(R.id.iconImage);
        Drawable background = current.info.loadIcon(packageManager);

        imageView.setBackgroundDrawable(background);


        return view;
    }
}

