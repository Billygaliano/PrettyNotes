package com.ruben.rma.prettynotes.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import com.facebook.login.LoginManager;
import com.ruben.rma.prettynotes.adapter.Adapter;
import com.ruben.rma.prettynotes.connectionws.DeleteHttp;
import com.ruben.rma.prettynotes.connectionws.PostHttp;
import com.ruben.rma.prettynotes.data.NoteBD;
import com.ruben.rma.prettynotes.R;
import com.ruben.rma.prettynotes.model.Note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int ADD = Menu.FIRST;
    private static final int DELETE = Menu.FIRST + 1;
    private static final int EXIST = Menu.FIRST + 2;
    ListView lista;
    private Toolbar mToolbar;

    NoteBD DB = new NoteBD(this);
    List<String> item = null;
    String getTitle;
    String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Se asocia con el contexto menu
        requestWindowFeature(Window.FEATURE_CONTEXT_MENU);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = this.getIntent().getExtras();
        email = bundle.getString("email");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        lista = (ListView) findViewById(R.id.listView_Lista);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actividad("add");
            }
        });

        new GetHttp(this).execute("http://192.168.1.127:8080/PrettyNotesWS/webresources/entity.note/" + email);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        /*El primer valor del boton es la colocacion del mismo en la pantalla, el segundo es
         el final int creado anteriormente y que despues nos servira para saber que boton se
            ha pulsado. Por ultimo encontra,os e valor del texto del boton, dado por el valor de los
            string contenidos en string.xml
        */
        menu.add(1, ADD, 0, R.string.menu_crear);
        menu.add(2, DELETE, 0, R.string.menu_borrar_todas);
        menu.add(3, EXIST, 0, R.string.menu_salir);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case ADD:
                actividad("add");
                return true;
            case DELETE:
                alert("deletes");
                return true;
            case EXIST:
                finish();
                Intent intent = new Intent(this, Login.class);
                LoginManager.getInstance().logOut();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void showNotes() {
        DB = new NoteBD(this);
        Cursor c = DB.getNoteByUser(email);
        item = new ArrayList<String>();
        ArrayList<Note> notes = new ArrayList<>();

        //Nos aseguramos de que existe al menos unr egistro
        if (c.moveToFirst() == false) {
            //el cursor esta vacio
        } else {
            //Recorremos el cursor hasta que no haya mas registros
            do {
                notes.add(new Note(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6)));

            } while (c.moveToNext());
        }
        //Vamos a crear un adaptador de tipo ArrayAdapter
        Adapter adapter = new Adapter(this, notes);
        lista.setAdapter(adapter);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Obtenemos el titulo de la nota en la variable
                Note n = (Note) lista.getItemAtPosition(position);
                getTitle = n.getTittle();

                //Llamamos a la funciones alert con la cadena lista que nos mostrara un mensaje de alerta con la accion que queremos llevar a cabo
                actividad("edit");
            }
        });
    }

    public Note getNote() {
        Note content = new Note();
        DB = new NoteBD(this);
        Cursor c = DB.getNote(getTitle);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya mas registros y obtenemos el conteenido de la nota
            do {
                //Pongo 2 xq columna empieza desde valor 0, y en el 0 esta el id de la nota, en el 1 el titulo de la nota y el el 2 el contenido
                content.setContent(c.getString(2));
                content.setLatitude(c.getString(3));
                content.setLongitude(c.getString(4));
                content.setImage(c.getString(5));
            } while (c.moveToNext());
        }
        return content;
    }

    public void actividad(String act) {
        //Funcion que permite añadir, edirtar o ver una nota dependiendo de la subcadena que le pasemos
        String type = "";
        Note content;

        if (act.equals("add")) {
            //Si lo que añadimos es una nota pasamos al activity de Agragar nota pasandole en el pool el tipo que es para que pueda reconocerlo y saber que accion realizará
            type = "add";
            Intent intent = new Intent(MainActivity.this, AddNote.class);
            intent.putExtra("email", email);
            intent.putExtra("type", type);
            startActivity(intent);
        } else {
            if (act.equals("edit")) {
                //Si lo que queremos es editar la nota además del typo de accion le tenemos que pasar tanto el titulo como el contenido dentro del pool
                type = "edit";
                //Ontiene el el contenido de la nota
                content = getNote();
                Intent intent = new Intent(MainActivity.this, ViewNote.class);
                intent.putExtra("type", type);
                intent.putExtra("email", email);
                intent.putExtra("title", getTitle);
                intent.putExtra("content", content.getContent());
                intent.putExtra("latitude", content.getLatitude());
                intent.putExtra("longitude", content.getLongitude());
                intent.putExtra("image", content.getImage());
                startActivity(intent);
            } else if (act.equals("see")) {
                //Si lo que deseamos es ver la nota solo debemos pasar al activity de ver nota y pasarle el titulo y el contenido en el pool (coleccion de objetos)
                content = getNote();
                Intent intent = new Intent(MainActivity.this, ViewNote.class);
                intent.putExtra("email", email);
                intent.putExtra("title", getTitle);
                intent.putExtra("content", content.getContent());
                intent.putExtra("latitude", content.getLatitude());
                intent.putExtra("longitude", content.getLongitude());
                intent.putExtra("image", content.getImage());
                startActivity(intent);
            }
        }
    }

    private void alert(String f) {
        AlertDialog alerta;
        //Se crea una ventana de alerta con la acción y opciones que querramos
        alerta = new AlertDialog.Builder(this).create();

        if (f.equals("list")) {
            alerta.setTitle("Título: " + getTitle);
            alerta.setMessage("¿Qué acción desea realizar?");
            alerta.setButton("Ver", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Si clicamos sobre el boton ver la accion que conlleva es la de la funcion actividad con la cadena see
                    actividad("see");
                }
            });

            alerta.setButton2("Eliminar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Si clicamos sobre el boton ver la accion que conlleva es la de la funcion delete con la cadena delete
                    delete("delete");
                    Intent intent = getIntent();
                    startActivity(intent);
                }
            });

            alerta.setButton3("Editar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Si clicamos sobre el boton ver la accion que conlleva es la de la funcion actividad con la cadena edit
                    actividad("edit");
                }
            });
        } else {
            if (f.equals("deletes")) {
                alerta.setTitle("Mensaje de confirmación");
                alerta.setMessage("¿Qué acción desea realizar?");
                //Si la cadena en la funcione alert es deletes mostramos un mensaje de confirmación indicancion si deseea eliminar todas las notas
                //Dando dos opciones, tanto cancelar y no se hace nada como eliminar notas que llamará a la funcion delete con cadena "deletes" y esta se encargará de borrarlas
                alerta.setButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                alerta.setButton2("Eliminar notas", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete("deletes");
                        Intent intent = getIntent();
                        startActivity(intent);
                    }
                });
            }
        }

        alerta.show();
    }

    private void delete(String f) {
        DB = new NoteBD(this);
        //Si la cadena es delete llamamos a la funcion deleteNote de la clase NoteBD que es la que
        // contiene la base de datos y le pasamos el titulo para borrar la nota seleccionada
        if (f.equals("delete")) {
            DB.deleteNote(getTitle);
        } else {
            //Si la cadena es "deletes" llamamos a la funcion deleteNotes de la clase de base de datos y borrará todas las notas
            if (f.equals("deletes")) {
                Cursor c = DB.getNoteByUser(email);

                if (c.moveToFirst()) {
                    do {
                        try {
                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("tittle", c.getString(1));
                            jsonParam.put("email", email);
                            new DeleteHttp(this).execute("http://192.168.1.127:8080/PrettyNotesWS/webresources/entity.note/1/1", jsonParam.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } while (c.moveToNext());

                    DB.deleteNotes();
                }
            }
        }
    }

    public class GetHttp extends AsyncTask<String, Void, JSONArray> {
        private final Context context;
        private ProgressDialog progress;
        public GetHttp(Context c) {
            this.context = c;
        }

        protected void onPreExecute() {
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected JSONArray doInBackground(String... param) {

            String finalJson = "";
            String result = "";
            StringBuilder sb = new StringBuilder();
            HttpURLConnection conGet;
            JSONArray parentObject = null;

            try {
                URL url = new URL(param[0]);
                conGet = (HttpURLConnection) url.openConnection();
                conGet.setRequestMethod("GET");
                int statusCode = conGet.getResponseCode();

                if(statusCode !=200){

                }
                else {
                    InputStream in = new BufferedInputStream(conGet.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(in));

                    String line = "";
                    while((line = bufferedReader.readLine()) != null)
                        finalJson += line;

                    bufferedReader.close();
                }
                parentObject = new JSONArray(finalJson);

                result = "\nSending 'POST' request to URL : " + url + "\nResponse Code : " + statusCode;

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                progress.dismiss();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                progress.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                progress.dismiss();
            }

            return parentObject;
        }

        protected void onPostExecute(JSONArray result) {
            int i;
            boolean exist = false;
            Cursor c = DB.getNoteByUser(email);
            if(result != null){
                if (result.length() != 0) {
                    for (i = 0; i < result.length(); i++) {
                        JSONObject j = null;
                        try {
                            j = result.getJSONObject(i);
                            if (c.moveToFirst()) {
                                //Recorremos el cursor hasta que no haya mas registros y obtenemos el conteenido de la nota
                                do {

                                    if (c.getString(1).equals(j.getString("tittle"))) {
                                        exist = true;
                                    }
                                    //Pongo 2 xq columna empieza desde valor 0, y en el 0 esta el id de la nota, en el 1 el titulo de la nota y el el 2 el contenido
                                } while (c.moveToNext());

                                if (!exist) {
                                    if(j.length() == 8){
                                        DB.addNote(j.getString("tittle"), j.getString("content"), j.getString("dateNote"), j.getString("latitude"), j.getString("longitude"), email, j.getString("photo"));
                                    }else if(j.length() == 7){
                                        DB.addNote(j.getString("tittle"), j.getString("content"), j.getString("dateNote"), j.getString("latitude"), j.getString("longitude"), email, null);
                                    }else if(j.length() == 6 ){
                                        DB.addNote(j.getString("tittle"), j.getString("content"), j.getString("dateNote"), null, null, email, j.getString("photo"));
                                    }else if (j.length() == 5){
                                        DB.addNote(j.getString("tittle"), j.getString("content"), j.getString("dateNote"), null, null, email, null);

                                    }
                                }
                                exist = false;
                            } else {
                                if(j.length() == 8){
                                    DB.addNote(j.getString("tittle"), j.getString("content"), j.getString("dateNote"), j.getString("latitude"), j.getString("longitude"), email, j.getString("photo"));
                                }else if(j.length() == 7){
                                    DB.addNote(j.getString("tittle"), j.getString("content"), j.getString("dateNote"), j.getString("latitude"), j.getString("longitude"), email, null);
                                }else if(j.length() == 6 ){
                                    DB.addNote(j.getString("tittle"), j.getString("content"), j.getString("dateNote"), null, null, email, j.getString("photo"));
                                }else if (j.length() == 5){
                                    DB.addNote(j.getString("tittle"), j.getString("content"), j.getString("dateNote"), null, null, email, null);
                                }
                            }
                            progress.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    progress.dismiss();
                }

                Boolean existJSON = false;
                Cursor cursor = DB.getNoteByUser(email);
                if (cursor.moveToFirst()) {
                    //Recorremos el cursor hasta que no haya mas registros y obtenemos el conteenido de la nota
                    do {

                        for (i=0; i<result.length();i++){
                            JSONObject j = null;

                            try {
                                j = result.getJSONObject(i);
                                if(cursor.getString(1).equals(j.getString("tittle"))){
                                    existJSON = true;
                                }
                            }catch(JSONException e){
                                e.printStackTrace();
                            }
                        }

                        if(!existJSON){
                            JSONObject jsonParam = new JSONObject();

                            try {
                                JSONObject userParam = new JSONObject();
                                userParam.put("idUser", 0);
                                userParam.put("email", email);
                                jsonParam.put("tittle", cursor.getString(1));
                                jsonParam.put("content", cursor.getString(2));
                                jsonParam.put("dateNote",cursor.getString(3));
                                jsonParam.put("photo",cursor.getString(4));
                                jsonParam.put("latitude",cursor.getString(5));
                                jsonParam.put("longitude",cursor.getString(6));
                                jsonParam.put("idUser",userParam);
                                new PostHttp(context).execute("http://192.168.1.127:8080/PrettyNotesWS/webresources/entity.note?", jsonParam.toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        existJSON = false;
                        //Pongo 2 xq columna empieza desde valor 0, y en el 0 esta el id de la nota, en el 1 el titulo de la nota y el el 2 el contenido
                    } while (cursor.moveToNext());
                }
            }

            showNotes();
        }
    }
}