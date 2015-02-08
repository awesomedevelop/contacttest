package com.awesomedevelop.contactapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Taras on 29.01.2015.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    private ArrayList<ContactData> contactDataSet;
    public Context mContext;
    private int lastPosition = -1;
    public static class MyViewHolder extends RecyclerView.ViewHolder  {
        TextView textName;
        TextView textPhone;
       ImageView qBadge;
       ImageButton imageButton;
        public MyViewHolder(View itemView){
            super (itemView);
            this.qBadge = (ImageView)itemView.findViewById(R.id.badge);
            this.textName = (TextView)itemView.findViewById(R.id.text_display_name);
            this.textPhone = (TextView)itemView.findViewById(R.id.text_phone);
            this.imageButton = (ImageButton)itemView.findViewById(R.id.imageButton);

        }



    }

    public ContactAdapter(Context context, ArrayList<ContactData> contacts){
        this.contactDataSet= contacts;
        mContext=context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_card, parent, false);

          view.setOnClickListener(MainActivity.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }




    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final TextView textViewName = holder.textName;
        TextView textViewPhone = holder.textPhone;
        ImageView qb = holder.qBadge;
        ImageButton imageButton = holder.imageButton;
        //textViewName.setText(perfumeDataSet.get(listPosition).getName());
      textViewName.setText(contactDataSet.get(listPosition).getName());
      textViewPhone.setText(contactDataSet.get(listPosition).getPhone());

        String src = contactDataSet.get(listPosition).getPhoto();
        Picasso.with(mContext)
                .load(src)
                .transform(new CircleTransform())
                .resize(300,300)

                .resize(300,300)
                .into(qb);


            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, EditActivity.class);

                    intent.putExtra("phone",contactDataSet.get(listPosition).getPhone());
                    intent.putExtra("name",contactDataSet.get(listPosition).getName());
                    intent.putExtra("photo",contactDataSet.get(listPosition).getPhoto());
                    mContext.startActivity(intent);

                }
            });

    }





    @Override
    public int getItemCount() {
        return contactDataSet.size();
    }





}
