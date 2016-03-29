package com.ruben.rma.prettynotes.activities;

import android.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.facebook.login.LoginManager;
import com.ruben.rma.prettynotes.data.NoteBD;
import com.ruben.rma.prettynotes.R;

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
    //Variables para asignar a los tres botones del menu una acción concreta con otro nombre que serán add delete y exist respectivamente
    private static final int ADD = Menu.FIRST;
    private static final int DELETE = Menu.FIRST + 1;
    private static final int EXIST = Menu.FIRST + 2;
    ListView lista;
    private Toolbar mToolbar;

    NoteBD DB;
    List<String> item = null;
    String getTitle;
    String email;


    @Override
    //Constructor por defecto
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
        //Se mostrará en la lista los correspondientes títulos de las notas en los cuales se puede clickar con lo que conllevará a su respectiva acción
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Obtenemos el titulo de la nota en la variable
                getTitle = (String) lista.getItemAtPosition(position);
                //Llamamos a la funciones alert con la cadena lista que nos mostrara un mensaje de alerta con la accion que queremos llevar a cabo
                actividad("edit");
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actividad("add");
            }
        });

        new GetHttp(this).execute("http://192.168.1.127:8080/PrettyNotesWS/webresources/entity.note/");


        //Esta funcion se encuentra en el constructor ya que hay que recorrer toda la base de datos y mostrar todos los titulos de las notas
        //en el activity principal
        showNotes();
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
        //Creamos un tipo de base de datos
        DB = new NoteBD(this);
        //Obtenemos el contenido de las notas
        Cursor c = DB.getNotes();
        item = new ArrayList<String>();
        String title = "";
        //Nos aseguramos de que existe al menos unr egistro
        if (c.moveToFirst() == false) {
            //el cursor esta vacio

        } else {
            //Recorremos el cursor hasta que no haya mas registros
            do {
                title = c.getString(1);
                //Pongo 1 xq columna empieza desde valor 0, y en el 0 esta el id de la nota, en el 1 el titulo de la nota y el el 2 el contenido
                item.add(title);
            } while (c.moveToNext());
        }
        //Vamos a crear un adaptador de tipo ArrayAdapter
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, item);
        lista.setAdapter(adaptador);
    }

    public String getNote() {
        String type = "", content = "";
        DB = new NoteBD(this);
        Cursor c = DB.getNote(getTitle);
        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya mas registros y obtenemos el conteenido de la nota
            do {
                content = c.getString(2);
                //Pongo 2 xq columna empieza desde valor 0, y en el 0 esta el id de la nota, en el 1 el titulo de la nota y el el 2 el contenido
            } while (c.moveToNext());
        }
        return content;
    }

    public void actividad(String act) {
        //Funcion que permite añadir, edirtar o ver una nota dependiendo de la subcadena que le pasemos
        String type = "", content = "";
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
                intent.putExtra("content", content);
                startActivity(intent);
            } else if (act.equals("see")) {
                //Si lo que deseamos es ver la nota solo debemos pasar al activity de ver nota y pasarle el titulo y el contenido en el pool (coleccion de objetos)
                content = getNote();
                Intent intent = new Intent(MainActivity.this, ViewNote.class);
                intent.putExtra("email", email);
                intent.putExtra("title", getTitle);
                intent.putExtra("content", content);
                startActivity(intent);
            }
        }
    }

    private void alert(String f) {
        AlertDialog alerta;
        //Se crea una ventana de alerta con la acción y opciones que querramos
        alerta = new AlertDialog.Builder(this).create();
        //Si la cadena es lista, cremaos una ventana que contenga un mensaje y tres botones con sus respectivas acciones (Ver, Eliminar, Editar (Notas))
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
                DB.deleteNotes();
            }
        }
    }

    public class GetHttp extends AsyncTask<String, Void, JSONArray> {
        private final Context context;


        public GetHttp(Context c) {
            this.context = c;
        }

        protected void onPreExecute() {
        }

        @Override
        protected JSONArray doInBackground(String... param) {

            String finalJson = "";
            String result = "";
            StringBuilder sb = new StringBuilder();
            HttpURLConnection con;
            JSONArray parentObject = null;

            try {


                URL url = new URL(param[0]);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");


                int statusCode = con.getResponseCode();

                if(statusCode !=200){

                }
                else {
                    InputStream in = new BufferedInputStream(con.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(in));

                    String line = "";
                    while((line = bufferedReader.readLine()) != null)
                        finalJson += line;

                    bufferedReader.close();
                    System.out.println("datos" + finalJson);
                }
                parentObject = new JSONArray(finalJson);

                result = "\nSending 'POST' request to URL : " + url + "\nResponse Code : " + statusCode;

                System.out.println("\nSending 'POST' request to URL : " + con.getRequestMethod() + " Y la URL: " + url);
                System.out.println("Response Code : " + statusCode);


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return parentObject;
        }

        protected void onPostExecute(JSONArray result) {
            int i;
            for (i=0; i<result.length();i++){
                JSONObject j = null;
                try {
                    j = result.getJSONObject(i);
                    String tittle = j.getString("tittle");
                    System.out.println(tittle);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}