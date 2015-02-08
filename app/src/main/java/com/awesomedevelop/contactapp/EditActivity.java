package com.awesomedevelop.contactapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class EditActivity extends ActionBarActivity {
    String intent_phone,intent_name,intent_photo;
    ImageView display_image;
    TextView display_name;
    EditText edit_name;
    EditText edit_phone;
    Button edit_button,cancel_button;
    int  httprequest=1;
    int progress_status =0;
    JSONArray jArray = null;
    String result = null;
    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    StringBuilder sb = null;
    private static ArrayList<Data> server_contacts;
    InputStream is = null;
    String s;
    String contactId = null;
    IsInternetExist isExist = new IsInternetExist();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        getSupportActionBar().setTitle("Редактирование");
        Intent intent =  getIntent();
        intent_phone=intent.getStringExtra("phone");
        intent_name=intent.getStringExtra("name");
        intent_photo=intent.getStringExtra("photo");
      //  Toast.makeText(getApplicationContext(),intent_photo,Toast.LENGTH_SHORT).show();
        edit_button = (Button)findViewById(R.id.button);
        cancel_button = (Button)findViewById(R.id.button_cancel);
        display_image = (ImageView)findViewById(R.id.edit_profile_image);
        display_name = (TextView)findViewById(R.id.edit_display_name);
        edit_name = (EditText)findViewById(R.id.edit_name);
        edit_phone = (EditText)findViewById(R.id.edit_phone);
        Picasso.with(getApplicationContext()).load(intent_photo).transform(new CircleTransform()).into(display_image);
        display_name.setText(intent_name);
        edit_name.setText(intent_name);
        edit_phone.setText(intent_phone);


        ContentResolver contentResolver = getApplicationContext().getContentResolver();

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(intent_phone));

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
        Log.i("CONTACT_ID",contactId);




    cancel_button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditActivity.this.finish();
        }
    });




    edit_button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           // Toast.makeText(getApplicationContext(),"cool",Toast.LENGTH_SHORT).show();

            if (isExist.isNetworkConnected(getApplicationContext())==true) {
                edit_contact();
                new task().execute();
            }
            else {
                showDialog();
            }

        }
    });


    }




    public void delete_contact(){

        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(intent_phone));
        Cursor cur = getApplicationContext().getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(intent_phone)) {
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





   public void edit_contact(){
       String name,phone;
       name =String.valueOf( edit_name.getText());
       phone = String.valueOf(edit_phone.getText());
       ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

       // Name
       ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
               .withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
               .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
               .build());
       ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
               .withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
               .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
               .build());


       try{
       getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
   } catch (RemoteException e){e.printStackTrace();}
       catch (OperationApplicationException d){d.printStackTrace();}

       // Number

     // .withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?"+ " AND " + ContactsContract.CommonDataKinds.Organization.TYPE + "=?", new String[]{String.valueOf(id), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)});



   }



    public class task extends AsyncTask<String, Integer, Void> {
        ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
//
        }


        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(String... params) {





                //  s = phone+ String.valueOf(i);

            String  name,phone;
            name = String.valueOf(edit_name.getText());
            phone= String.valueOf(edit_phone.getText());
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://contactsync.esy.es/update.php");

                    nameValuePairs.add(new BasicNameValuePair("name",name ));
                    nameValuePairs.add(new BasicNameValuePair("phone",intent_phone));
                    nameValuePairs.add(new BasicNameValuePair("newphone",phone));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();




                } catch (Exception e) {
                    httprequest = 0;

                    Log.e("log_tag", "Error in http connection" + e.toString());


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



            return null;
        }

        protected void onPostExecute(Void v) {
            Toast.makeText(getApplicationContext(),"Данные обновлены",Toast.LENGTH_SHORT).show();

        }//конец



    }

    public class delete_task extends AsyncTask<String, Integer, Void> {
       // ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
//
        }


        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(String... params) {





            //  s = phone+ String.valueOf(i);

            String  name,phone;
            name = String.valueOf(edit_name.getText());
            phone= String.valueOf(edit_phone.getText());
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://contactsync.esy.es/delete.php");


                nameValuePairs.add(new BasicNameValuePair("phone",intent_phone));


                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();




            } catch (Exception e) {
                httprequest = 0;

                Log.e("log_tag", "Error in http connection" + e.toString());


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
            delete_contact();


            return null;
        }

        protected void onPostExecute(Void v) {
            Toast.makeText(getApplicationContext(),"Контакт удалён",Toast.LENGTH_SHORT).show();
            EditActivity.this.finish();
        }//конец



    }


    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);

         builder.setTitle("");

        builder.setMessage("Отсутствует интернет соединение, изминения будут внесены только локально!");
        builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edit_contact();
            }
        });





        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    public void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);

        builder.setTitle("");

        builder.setMessage("Отсутствует интернет соединение,контакт будет удалён только локально!");
        builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete_contact();
            }
        });





        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete:

            if (isExist.isNetworkConnected(getApplicationContext())==true){

                new delete_task().execute();
            }
            else {
                showDeleteDialog();
            }





            return true;

            case R.id.action_settings:
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
        }

}
