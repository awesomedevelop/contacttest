package com.awesomedevelop.contactapp;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    private static ArrayList<ContactData> contacts;
    private static ArrayList <ContactData> updated_contacts;
    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView.Adapter adapter;
    static View.OnClickListener myOnClickListener;
    int  httprequest=1;
    int progress_status =0;
    JSONArray jArray = null;
    String result = null;
    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    StringBuilder sb = null;
    private static ArrayList<Data> server_contacts;
    InputStream is = null;
    String s;
    ProgressBar pb;
    Thread thread = new Thread();

    IsInternetExist isExist = new IsInternetExist();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        myOnClickListener = new MyOnClickListener(this);
        pb = (ProgressBar) findViewById(R.id.main_progress);
        pb.setProgress(progress_status);
        fetchContacts();
        adapter = new ContactAdapter(MainActivity.this,contacts);
        recyclerView.setAdapter(adapter);

        getSupportActionBar().setTitle("Контакты");








       if (isExist.isNetworkConnected(getApplicationContext())==true){
           new task().execute();
           pb.setVisibility(View.VISIBLE);
       }
        if (isExist.isNetworkConnected(getApplicationContext())==false){
            Toast.makeText(getApplicationContext(),"Отсутсвует интернет соединение",Toast.LENGTH_SHORT).show();
        }


    }



















public void filter_contact(String filter){

    // contacts = new ArrayList<ContactData>();
    ContentResolver cr = getContentResolver();

    //  ContentResolver cr = getApplicationContext().getContentResolver(); //Activity/Application android.content.Context
    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
            null ,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE '%"+filter+"%'",
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
    if(cursor.moveToFirst())
    {
        updated_contacts = new ArrayList<ContactData>();
        do
        {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
            {
                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{ id },

                        null);
                while (pCur.moveToNext())
                {
                    String name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String photo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                    // String temp = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.));

                    updated_contacts.add(new ContactData(name,phone,photo));
                    //    Log.i("[NAME]", temp);
                    break;
                }
                pCur.close();
            }

        } while (cursor.moveToNext()) ;
    }




}

public void fetchContacts(){


   // contacts = new ArrayList<ContactData>();
    ContentResolver cr = getContentResolver();

  //  ContentResolver cr = getApplicationContext().getContentResolver(); //Activity/Application android.content.Context
    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,null , null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
    if(cursor.moveToFirst())
    {
       contacts = new ArrayList<ContactData>();
        do
        {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
            {
                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{ id },

                        null);
                while (pCur.moveToNext())
                {
                    String name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String photo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                   // String temp = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.));

                    contacts.add(new ContactData(name,phone,photo));
                //    Log.i("[NAME]", temp);
                    break;
                }
                pCur.close();
            }

        } while (cursor.moveToNext()) ;
    }

    }






public class task extends AsyncTask<String, Integer, Void> {
        ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
//            dialog = new ProgressDialog(MainActivity.this);
//            dialog.setMessage("Загрузка данных");
//            dialog.setTitle("Синхронизация");
//            dialog.setIndeterminate(false);
//            dialog.setMax(contacts.size());
//            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            dialog.setCancelable(false);
//
//
//
//            dialog.show();
        }


        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(String... params) {

            pb.setMax(contacts.size());


            for (int i=0;i<contacts.size();i++) {
                //  s = phone+ String.valueOf(i);
                if(isExist.isNetworkConnected(getApplicationContext())==true) {

                    try {
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost("http://contactsync.esy.es/test.php");

                        nameValuePairs.add(new BasicNameValuePair("name", contacts.get(i).getName()));
                        nameValuePairs.add(new BasicNameValuePair("phone", contacts.get(i).getPhone()));


                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                        HttpResponse response = httpclient.execute(httppost);
                        HttpEntity entity = response.getEntity();
                        is = entity.getContent();

                        pb.setProgress(progress_status += 1);
                        //  publishProgress((int)i*100/contacts.size());


                    } catch (Exception e) {
                        httprequest = 0;

                        Log.e("log_tag", "Error in http connection" + e.toString());
                        break;

                    }
                    //Переводим ответ в строку
                    if (httprequest == 1) {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                            sb = new StringBuilder();
                            sb.append(reader.readLine() + "\n");

                            String line = "0";
                            while ((line = reader.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            is.close();
                            result = sb.toString();
                            //   Log.i("[RESPONSE]",result);

                        } catch (Exception e) {
                            Log.e("log_tag", "Error converting result " + e.toString());
                        }
                    }


                }
                else {break;}

            }// end loop
            progress_status=0;
            pb.setProgress(progress_status);

            return null;
        }

        protected void onPostExecute(Void v) {
         //   dialog.dismiss();
            if (isExist.isNetworkConnected(getApplicationContext())==true){
            new getcontacts().execute();}
        }//конец

//        @Override
//        public void onProgressUpdate(Integer... args){
//            dialog.setProgress(args[0]);
//        }


    }






public class getcontacts extends AsyncTask<String, Integer, Void> {
        ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Загрузка данных");
            dialog.setTitle("Синхронизация");
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);



           // dialog.show();
        }


        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(String... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://contactsync.esy.es/getcontact.php");


                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();



                //  publishProgress((int)i*10);
            } catch (Exception e) {
                httprequest = 0;
                Log.e("log_tag", "Error in http connection" + e.toString());
            }
            //Переводим ответ в строку
            pars_server_contacts();

            return null;
        }

        protected void onPostExecute(Void v) {
            pb.setVisibility(View.INVISIBLE);
            progress_status=0;
            contacts.clear();
            fetchContacts();
            adapter = new ContactAdapter(MainActivity.this,contacts);
            recyclerView.setAdapter(adapter);


        }//конец

        @Override
        public void onProgressUpdate(Integer... args){
            dialog.setProgress(args[0]);
        }
    }



public void pars_server_contacts(){

    if (httprequest == 1) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");

            String line = "0";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            //   Log.i("[RESPONSE]",result);

        } catch (Exception e) {
            Log.e("log_tag", "Error converting result " + e.toString());
        }
    } //конец перевода
    if (httprequest == 1) {    //распарсить строку
        String name;
        String phone;
        String delete;
        server_contacts = new ArrayList<Data>();
        try {
            JSONArray jArray = new JSONArray(result);
            JSONObject json_data = null;
            for (int i = 0; i < jArray.length(); i++) {
                json_data = jArray.getJSONObject(i);

                //dialog.dismiss();
                name = json_data.getString("name");
               // Log.i("[getNAME]",name);
                phone = json_data.getString("phone");
                //   Log.i("[getPHONE]",phone);
                delete = json_data.getString("del");
                server_contacts.add(new Data(name,phone,delete));



            }
            // Log.i("[SERVER_COUNT]",String.valueOf(server_contacts.size()));
            //   Log.i("[LOCAL_COUNT]",String.valueOf(contacts.size()));
            compare();

        } catch (JSONException e1) {
          //  Toast.makeText(getBaseContext(), e1.toString(),Toast.LENGTH_LONG).show();
        } catch (ParseException e1) {
            //Toast.makeText(getBaseContext(), e1.toString(),Toast.LENGTH_LONG).show();
        }

    } else if (httprequest == 0) {
      //  Toast.makeText(getApplicationContext(), "Сервер не найден", Toast.LENGTH_SHORT).show();
    }
    //коенц парсинга
}




public void compare (){

    pb.setMax(server_contacts.size());
    for (int i=0; i< server_contacts.size();i++){
        int flag = 0;

        for(int l=0; l < contacts.size();l++){
            if (server_contacts.get(i).getPhone().equals(contacts.get(l).getPhone())){

                break;
            }
            else {
                flag+=1;
               // Log.i(server_contacts.get(i).getPhone(),contacts.get(l).getPhone());
            }

        }
        if (flag==contacts.size()){
            Log.i("COMPARE","NOT EXIST");
            Log.i("NUM",server_contacts.get(i).getPhone());


           if (server_contacts.get(i).getDelete().equals("1")){
                Log.i("Не добавлять",server_contacts.get(i).getName());
            }
            else {
               insert_contact(server_contacts.get(i).getName(), server_contacts.get(i).getPhone());
           }

        }

        if(server_contacts.get(i).getDelete().equals("1")){
            Log.i("DELETE",server_contacts.get(i).getName());
            delete_contact(server_contacts.get(i).getPhone(),server_contacts.get(i).getName());
        }


        pb.setProgress(progress_status+=1);
    }

  //  Toast.makeText(getApplicationContext(),"Синхронизация завершена",Toast.LENGTH_SHORT).show();
}





public void insert_contact (String display_name, String contact_phone){


    // For brevity the ContactsContract and
// ContactsContract.CommonDataKinds prefixes are ignored.
    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    int rawContactInsertIndex = ops.size();
    ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build());
    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,display_name)
            .build());
    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact_phone)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .build());
// For brevity, the try-catch statement is ignored.
// Normally it's needed.
    try {
        getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
    } catch (RemoteException e){
        e.printStackTrace();
    } catch (OperationApplicationException p){
        p.printStackTrace();
    }
}




public void delete_contact(String phone, String name){

    Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
    Cursor cur = getApplicationContext().getContentResolver().query(contactUri, null, null, null, null);
    try {
        if (cur.moveToFirst()) {
            do {
                if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                    String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                    getApplicationContext().getContentResolver().delete(uri, null, null);

                }

            } while (cur.moveToNext());
        }

    } catch (Exception e) {
        System.out.println(e.getStackTrace());
    }


}






private class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            nextactivity(v);

        }

        private void nextactivity(View v){
            String contactId = null;
            int selectedItemPosition = recyclerView.getChildPosition(v);
            RecyclerView.ViewHolder viewHolder
                    = recyclerView.findViewHolderForPosition(selectedItemPosition);
            TextView textViewName
                    = (TextView) viewHolder.itemView.findViewById(R.id.text_phone);
            String selected_phone = (String) textViewName.getText();
            ContentResolver contentResolver = context.getContentResolver();

            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(selected_phone));

            String[] projection = new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

            Cursor cursor =
                    contentResolver.query(
                            uri,
                            projection,
                            null,
                            null,
                            null);

            if(cursor!=null) {
                while(cursor.moveToNext()){
                    //String contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                     contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                    //Log.i( "contactMatch name: " , contactName);
                   // Log.i( "contactMatch id: " , contactId);
                }
                cursor.close();
            }


            Intent intent = new Intent(Intent.ACTION_VIEW);
            uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
            intent.setData(uri);
            context.startActivity(intent);




        }


    }










    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);



        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        android.support.v7.widget.SearchView searchView =
                (android.support.v7.widget.SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {

                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                     filter_contact(s);
                    }
                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e){e.printStackTrace();}

                //filter_contact(s);
                adapter = new ContactAdapter(MainActivity.this,updated_contacts);
                recyclerView.setAdapter(adapter);




                return true;
            }
        });













        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
        //noinspection SimplifiableIfStatement

            case R.id.action_sync:

             if (progress_status==0){


                 if(isExist.isNetworkConnected(getApplicationContext())==true) {
                     pb.setVisibility(View.VISIBLE);
                     pb.setProgress(0);
                     new task().execute();
                 }



             }
              if (isExist.isNetworkConnected(getApplicationContext())!=true) {
                 Toast.makeText(getApplicationContext(),"Отсутствует интернет соединение",Toast.LENGTH_SHORT).show();
             }
                 if (progress_status!=0){
                 Toast.makeText(getApplicationContext(),"Синхронизация уже запущена",Toast.LENGTH_SHORT).show();
             }

            return true;

            case R.id.action_settings:
        return true;
        default:
        return super.onOptionsItemSelected(item);


        }
    }
}
