package com.askokov.rtsc.boot;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.askokov.rtsc.R;
import com.askokov.rtsc.common.PInfo;

public class BoxAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    List<PInfo> objects;

    BoxAdapter(Context context, List<PInfo> infos) {
        ctx = context;
        objects = infos;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item, parent, false);
        }
        PInfo p = getInstalledApp(position);
        // заполняем View данными
        ((TextView) view.findViewById(R.id.appName)).setText(p.getAppname());

        Drawable icon;
        try {
            icon = ctx.getPackageManager().getApplicationIcon(p.getPname());
        } catch (PackageManager.NameNotFoundException ex) {
            icon = ctx.getPackageManager().getDefaultActivityIcon();
        }
        ((ImageView) view.findViewById(R.id.appIcon)).setImageDrawable(icon);

        CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
        // присваиваем чекбоксу обработчик
        cbBuy.setOnCheckedChangeListener(myCheckChangList);
        // пишем позицию
        cbBuy.setTag(position);
        // заполняем данными из товаров: в корзине или нет
        cbBuy.setChecked(p.isChecked());
        return view;
    }

    // товар по позиции
    public PInfo getInstalledApp(int position) {
        return ((PInfo) getItem(position));
    }

    // содержимое корзины
    public List<PInfo> getBox() {
        List<PInfo> box = new ArrayList<PInfo>();
        for (PInfo app : objects) {
            // если в корзине
            if (app.isChecked()) {
                box.add(app);
            }
        }
        return box;
    }

    public void clearBox() {
        for (PInfo app : objects) {
            app.setChecked(false);
        }
    }

    // обработчик для чекбоксов
    private OnCheckedChangeListener myCheckChangList = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // меняем данные (выбран или нет)
            getInstalledApp((Integer) buttonView.getTag()).setChecked(isChecked);
        }
    };
}