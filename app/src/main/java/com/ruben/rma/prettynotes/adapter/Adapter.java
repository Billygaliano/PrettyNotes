package com.ruben.rma.prettynotes.adapter;

import java.util.ArrayList;

import com.ruben.rma.prettynotes.R;
import com.ruben.rma.prettynotes.model.Note;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Guillermo on 31/03/2016.
 */
public class Adapter extends ArrayAdapter<Note> {

    private final Context context;
    private final ArrayList<Note> modelsArrayList;

    public Adapter(Context context, ArrayList<Note> modelsArrayList) {
        super(context, R.layout.note_list_row, modelsArrayList);

        this.context = context;
        this.modelsArrayList = modelsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.note_list_row, parent, false);

        // 3. Get icons and title views from the rowView
        ImageView imageIcon = (ImageView) rowView.findViewById(R.id.image_icon);
        imageIcon.setVisibility(View.INVISIBLE);
        ImageView mapIcon = (ImageView) rowView.findViewById(R.id.map_icon);
        mapIcon.setVisibility(View.INVISIBLE);
        TextView titleView = (TextView) rowView.findViewById(R.id.title);

        Note note = modelsArrayList.get(position);

        // 4. Set the text for textView
        if(note.getLatitude() != null){
            mapIcon.setVisibility(View.VISIBLE);
        }
        if(note.getImage() != null){
            imageIcon.setVisibility(View.VISIBLE);
        }
        titleView.setText(modelsArrayList.get(position).getTittle());

        // 5. return rowView
        return rowView;
    }
}
