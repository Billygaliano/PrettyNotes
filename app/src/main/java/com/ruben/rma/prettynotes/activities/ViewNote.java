package com.ruben.rma.prettynotes.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ruben.rma.prettynotes.data.NoteBD;
import com.ruben.rma.prettynotes.R;

/**
 * Created by RMA on 14/04/2015.
 */
public class ViewNote extends AppCompatActivity {
    //psee el un menu parecido al activity principal
    private static final int EDITAR = Menu.FIRST;
    private static final int BORRAR = Menu.FIRST+1;
    private static final int SALIR = Menu.FIRST+2;
    String title,content;
    TextView TITLE,CONTENT;
    NoteBD DB;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //obtenemos los paremos que se nos han pasado al hacer el paso de un activity a otro
        Bundle bundle=this.getIntent().getExtras();

        //lo insertamos en sus respectivas variables para tratar con ellos
        title =bundle.getString("title");
        content= bundle.getString("content");

        //Hacemos referencia a los textview de vernota.xml
        TITLE=(TextView)findViewById(R.id.textView_titulo);
        CONTENT=(TextView)findViewById(R.id.textView_content);
        //Le cambiamos el texto al titulo y el contenido para que posea el de la nota correspondiente que estemos viendo
        TITLE.setText(title);
        CONTENT.setText(content);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.menu_view_note, menu);
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        //Mediante el getitemid se obtiene el valor del boton pulsado
        switch (id){
            //Si el botn pulsado es salir, la app termina
            case R.id.action_save:
                actividad("edit");
                return true;

            case R.id.action_delete:
                alert();
                return true;

            default:
                return super.onOptionsItemSelected(item);

         }
    }
    public void actividad (String f){
        DB =new NoteBD(this);
        String title, content, msj;
        title=TITLE.getText().toString();
        content=CONTENT.getText().toString();

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
                DB.updateNote(title,content,this.title);
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
            }
        }
    }

    public  void Mensaje (String msj){
        Toast toast = Toast.makeText(this,msj, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
        toast.show();
    }

    private void alert(){
        AlertDialog alerta;
        //Cremaos nuestra ventana de alerta con dos botones
        alerta= new AlertDialog.Builder(this).create();
        alerta.setTitle("Mensaje de confirmación");
        alerta.setMessage("¿Desea eliminar la nota?");
        alerta.setButton("Eliminar nota", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete();
            }
        });
        alerta.setButton2("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alerta.show();
    }

    private void delete(){
        DB = new NoteBD(this);
        DB.deleteNote(title);
        actividad("delete");
    }
}
