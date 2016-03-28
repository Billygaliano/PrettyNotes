package com.ruben.rma.prettynotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.TextView;

/**
 * Created by RMA on 14/04/2015.
 */
public class VerNota extends Activity {
    //psee el un menu parecido al activity principal
    private static final int EDITAR = Menu.FIRST;
    private static final int BORRAR = Menu.FIRST+1;
    private static final int SALIR = Menu.FIRST+2;
    String title,content;
    TextView TITLE,CONTENT;
    AdaptadorBD DB;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ver_nota);

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        menu.add(1, EDITAR,0,R.string.menu_editar);
        menu.add(2, BORRAR,0,R.string.menu_eliminar);
        menu.add(3, SALIR, 0, R.string.menu_salir);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        //Mediante el getitemid se obtiene el valor del boton pulsado
        switch (id){
            //Si el botn pulsado es salir, la app termina
            case EDITAR:
                actividad("edit");
                return true;

            case BORRAR:
                alert();
                return true;

            case SALIR:
                actividad("delete");
                return true;
            default:
                return super.onOptionsItemSelected(item);

         }
    }
    public void actividad (String f){
        if(f.equals("edit")){
            String type ="edit";
            Intent intent=new Intent(VerNota.this,AgregarNota.class);
            intent.putExtra("type",type);
            intent.putExtra("title",title);
            intent.putExtra("content",content);
            startActivity(intent);

        }else if (f.equals("delete")){
            /*El cookiesyncmanager se utiliza para sincronizar el alamacen de cookies navegador entre la memoria RAM
             y el alamacenamiento permanente. Para obtener el mejor rendimiento, las cookies del navegador se guardan en la memoria RAM. Un hilo separado
             guarda las cookies, impulsados por un temporizador*/

            CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            Intent intent;
            intent = new Intent(VerNota.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
    private void alert(){
        AlertDialog alerta;
        //Cremaos nuestra ventana de alerta con dos botones
        alerta= new AlertDialog.Builder(this).create();
        alerta.setTitle("Mensaje de confirmación");
        alerta.setMessage("¿Desea eliminar la nota?");
        alerta.setButton("Delete nota", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete();
            }
        });
        alerta.setButton2("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alerta.show();
    }

    private void delete(){
        DB = new AdaptadorBD(this);
        DB.deleteNote(title);
        actividad("delete");
    }
}
