package com.ruben.rma.prettynotes;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by RMA on 14/04/2015.
 */
public class AgregarNota extends Activity{

    Button Add;
    EditText TITLE, CONTENT;
    String type, getTitle;
    private static  final int SALIR = Menu.FIRST;
    AdaptadorBD DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agregar_nota);

        Add = (Button)findViewById(R.id.button_add);
        TITLE=(EditText)findViewById(R.id.editText_Titulo);
        CONTENT=(EditText)findViewById(R.id.editText_Nota);
        Bundle bundle=this.getIntent().getExtras();
        String content;
        getTitle =bundle.getString("title");
        content=bundle.getString("content");

        type = bundle.getString("type");

        if(type.equals("add")){
            Add.setText("Nota");
        }else{
            if(type.equals("edit")){
                TITLE.setText(getTitle);
                CONTENT.setText(content);
                Add.setText("Update nota");
            }
        }
        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LLamamos en las funcion correspondiente
                addUpdateNotes();
            }
        });
    }
    //Metodo sobrescrito de la clase listaactivity que se encarga de crear el meu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        super.onCreateOptionsMenu(menu);

        menu.add(1, SALIR, 0, R.string.menu_salir);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
       int id=item.getItemId();
        //Mediante getItem se obtiene el vlaor del botn pulsado
        switch (id){
            case SALIR:
                /*El coockie se utiliza para sincronizar el almacen de cookies navegador entre la memoria RAM y el almacenamiento permanente
                Para obtener el mejor rendimiento, las cookies del navegador se guardan en la memoria RAM
                Un hilo separado guarda las cookies, impulsados por un temporizador*/
                CookieSyncManager.createInstance(this);
                android.webkit.CookieManager cookieManager= android.webkit.CookieManager.getInstance();
                cookieManager.removeAllCookie();
                Intent intent=new Intent(AgregarNota.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);//Para lanzar la actividad
                return true;
            //break;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addUpdateNotes(){
        //Se ha podido llegar a la funcion tanto para añadir una nota como para editarla
        DB =new AdaptadorBD(this);
        String title, content, msj;
        //Convertimos el titulo y el contenido a cadena de texto
        title=TITLE.getText().toString();
        content=CONTENT.getText().toString();
        //si el tipo es añadir
        if(type.equals("add")){
            if(title.equals("")){
                //El titulo no puede estar vacio
                msj="Ingrese un título";
                TITLE.requestFocus();
                Mensaje(msj);
            }else{
                if(content.equals("")){
                    //el contendido no puede estar vacio
                    msj="Ingrese la nota";
                    CONTENT.requestFocus();
                    Mensaje(msj);
                }else{
                    //Una vez asegurados que han escrito en los dos campos recorremos la base de datos comprobando
                    //que no hay una nota con igual título
                    Cursor c = DB.getNote(title);
                    String gettitle="";
                    //Nos aseguramos de que existe al menos un registro
                    if(c.moveToFirst()){
                        //Recorremos el cursor hasta que no haya mas registros
                        do{
                            gettitle=c.getString(1);
                            //Pongo 1 xq columna empieza desde valor 0, y en el 0 esta el id de la nota, en el 1 el titulo de la nota y el el 2 el contenido
                        }while(c.moveToNext());
                    }
                    if(gettitle.equals(title)){
                        TITLE.requestFocus();
                        msj="EL título de la nota ya existe";
                        Mensaje(msj);
                    }else{
                        //Si el titulo no existe llamos e la funcion de añadir nota de la la clase base de datos que se encanrgada de añadirla
                        DB.addNote(title,content);
                        //Volvemos al activity principal
                        Intent intent = new Intent(AgregarNota.this,MainActivity.class);
                        startActivity(intent);
                    }
                }
            }
        }else{
            if (type.equals("edit")){
                Add.setText("Update nota");
                if(title.equals("")){
                    msj="Ingrese un título";
                    TITLE.requestFocus();
                    Mensaje(msj);
                }else{
                    if (content.equals("")){
                        msj="Ingrese la nota";
                        CONTENT.requestFocus();
                        Mensaje(msj);
                    }else{
                       DB.updateNote(title,content,getTitle);
                       Intent intent = new Intent(AgregarNota.this,MainActivity.class);
                       startActivity(intent);
                    }
                }
            }
        }
    }

    public  void Mensaje (String msj){
        Toast toast = Toast.makeText(this,msj, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
        toast.show();
    }
}
