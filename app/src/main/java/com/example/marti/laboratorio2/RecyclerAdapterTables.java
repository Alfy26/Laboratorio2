package com.example.marti.laboratorio2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marti on 05/04/2016.
 * **/
public class RecyclerAdapterTables extends RecyclerView.Adapter<RecyclerAdapterTables.MyViewHolderTables> {
    private  List<ReservationDataTables> mData;
    private LayoutInflater mInflater;
    public CardView mCardView;
    private Context context;

    public RecyclerAdapterTables (Context context,List<ReservationDataTables> data) {
        this.mData=data;
        this.context=context;
        this.mInflater=LayoutInflater.from(context);
    }


    public static class MyViewHolderTables extends RecyclerView.ViewHolder {
        TextView time;
        TextView name;
        ImageButton btnInfo;
        public View view;
        public ReservationDataTables data;
        public MyViewHolderTables(final View itemView) {
            super(itemView);
            view=itemView;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),ShowTables.class);
                    int position = getAdapterPosition();
                    intent.putExtra("NumeroPersone",data.getNumber());
                    intent.putExtra("Note",data.getNotes());
                    intent.putExtra("Nome",data.getReservationName());
                    intent.putExtra("Cognome",data.getReservationSurname());
                    intent.putExtra("Orario",data.getReservationTime());
                    intent.putExtra("Email", data.getEmail());
                    intent.putExtra("Telefono", data.getPhone());
                    v.getContext().startActivity(intent);
                }
            });
            time = (TextView) itemView.findViewById(R.id.res_time);
            name = (TextView) itemView.findViewById(R.id.res_name);
            btnInfo = (ImageButton) itemView.findViewById(R.id.buttonInfo);
        }
    }

    @Override
    public MyViewHolderTables onCreateViewHolder(ViewGroup parent,int viewType) {
        View view = mInflater.inflate(R.layout.card_list_item,parent,false);
        MyViewHolderTables holder = new MyViewHolderTables(view);
        return holder;
    }
    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolderTables holder, final int position) {
        ReservationDataTables currentObj = mData.get(position);
        holder.data = mData.get(position);
        //holder.setData(currentObj,position);
        holder.time.setText(String.valueOf(currentObj.getReservationTime()));
        holder.name.setText(String.valueOf(currentObj.getReservationName()));
        holder.btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog delete = new AlertDialog.Builder(context).setTitle(context.getString(R.string.delete)).setMessage(context.getString(R.string.askDelete)).setIcon(R.drawable.ic_delete_black_18dp).setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            removeItem(position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
                delete.show();
            }
        });
    }
    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = context.getAssets().open("reservationDataTable.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
    public void removeItem(int position) throws JSONException
    {
        //PROBLEMI CON L'AGGIORNAMENTO IN JSON
        //reservationDataJSON.remove(position);
        // NON GESTISCE L'AGGIORNAMENTO DELLE LISTE!!! E' SOLO UN FORMATO.
        //PER QUESTO MOTIVO, MODIFICARE I DATI RICHIEDE L'USO DI ARRAYLIST.
        //DA QUESTE ARRAYLIST CREEREMO UN NUOVO OGGETTO JSON CHE RIASSUME (DOPO L'ELIMINAZIONE)
        //TUTTI GLI ORDINI RIMANENTI.
        //SALVEREMO NELLE SHARED PREFERENCES IL NUOVO OGGETTO JSON obj2



        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String JsonObject = app_preferences.getString("jsonTable", loadJSONFromAsset());

        JSONObject obj = new JSONObject(JsonObject);
        JSONObject obj2 = new JSONObject();
        JSONArray reservationDataJSON=obj.getJSONArray("reservationData");
        JSONArray reservationDataJSON2=new JSONArray();

        ArrayList<JSONObject> jsList= new ArrayList<JSONObject>();
        for(int k=0; k<reservationDataJSON.length();k++)
        {
            jsList.add(reservationDataJSON.getJSONObject(k));
        }


        //reservationDataJSON.remove(position);
        jsList.remove(position);

        for(int k=0;k<jsList.size();k++)
        {
            reservationDataJSON2.put(k,jsList.get(k));

        }
        obj2.put("reservationData",(Object) reservationDataJSON2);

        // logs for debugging
        Log.i("IOqualcosa", "OLD OBJECT" + obj.toString());
        Log.i( "IOqualcosa","OLD POSITION IS" + position);
        Log.i( "IOqualcosa","NEW OBJECT" + obj2.toString());

        SharedPreferences.Editor editor = app_preferences.edit();

        editor.putString("jsonTable", obj2.toString());
        editor.commit();
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size());
    }


}