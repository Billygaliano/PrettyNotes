package com.ruben.rma.prettynotes.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.ruben.rma.prettynotes.data.NoteBD;
import com.ruben.rma.prettynotes.R;

/**
 * Created by RMA on 14/04/2015.
 */
public class AddNote extends AppCompatActivity {

    EditText TITLE, CONTENT;
    String type, getTitle;
    private static  final int SALIR = Menu.FIRST;
    NoteBD DB;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_note);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        TITLE=(EditText)findViewById(R.id.editText_Titulo);
        CONTENT=(EditText)findViewById(R.id.editText_Nota);


    }
    //Metodo sobrescrito de la clase listaactivity que se encarga de crear el meu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
       int id=item.getItemId();
        //Mediante getItem se obtiene el vlaor del botn pulsado
        switch (id){
            case R.id.action_save:
                addUpdateNotes();
                return true;
            //break;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addUpdateNotes(){
        //Se ha podido llegar a la funcion tanto para añadir una nota como para editarla
        DB =new NoteBD(this);
        String title, content, msj;
        //Convertimos el titulo y el contenido a cadena de texto
        title=TITLE.getText().toString();
        content=CONTENT.getText().toString();
        //si el tipo es añadir

        if(title.equals("")){
            //El titulo no puede estar vacio
            msj="Añade un título";
            TITLE.requestFocus();
            Mensaje(msj);
        }else{
            if(content.equals("")){
                //el contendido no puede estar vacio
                msj="Añade el contenido";
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
                    Intent intent = new Intent(AddNote.this,MainActivity.class);
                    startActivity(intent);
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
