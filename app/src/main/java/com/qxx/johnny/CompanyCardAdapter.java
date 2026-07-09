package com.qxx.johnny;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.qxx.johnny.model.Company;

import java.util.List;

/**
 * 搜索/关注列表的卡片适配器：公司名 + 动态 chip（状态/法人/资本）。
 */
public class CompanyCardAdapter extends ArrayAdapter<Company> {
    private final LayoutInflater inflater;

    public CompanyCardAdapter(Context ctx) {
        super(ctx, 0);
        inflater = LayoutInflater.from(ctx);
    }

    public void setData(List<Company> list) {
        clear();
        addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_company_card, parent, false);
        }
        Company c = getItem(position);
        TextView tvName = convertView.findViewById(R.id.tv_name);
        LinearLayout chipRow = convertView.findViewById(R.id.chip_row);
        tvName.setText(c == null ? "" : c.name);

        chipRow.removeAllViews();
        if (c != null) {
            if (c.status != null) addChip(chipRow, c.status);
            if (c.legalPerson != null) addChip(chipRow, "法人:" + c.legalPerson);
            if (c.registeredCapital != null) addChip(chipRow, "资本:" + c.registeredCapital);
        }
        return convertView;
    }

    private void addChip(LinearLayout parent, String text) {
        TextView chip = new TextView(getContext());
        chip.setText(text);
        chip.setTextSize(12);
        chip.setTextColor(ContextCompat.getColor(getContext(), R.color.chip_text));
        chip.setBackgroundResource(R.drawable.bg_chip);
        chip.setPadding(16, 6, 16, 6);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 8, 0);
        chip.setLayoutParams(lp);
        parent.addView(chip);
    }
}
