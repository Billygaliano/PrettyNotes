package com.ruben.rma.prettynotes.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ruben.rma.prettynotes.connectionws.DeleteHttp;
import com.ruben.rma.prettynotes.connectionws.PostHttp;
import com.ruben.rma.prettynotes.connectionws.UpdateHttp;
import com.ruben.rma.prettynotes.data.NoteBD;
import com.ruben.rma.prettynotes.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by RMA on 14/04/2015.
 */
public class ViewNote extends AppCompatActivity {
    //psee el un menu parecido al activity principal
    private static final int EDITAR = Menu.FIRST;
    private static final int BORRAR = Menu.FIRST+1;
    private static final int SALIR = Menu.FIRST+2;
    String title, content, email;
    TextView TITLE,CONTENT;
    NoteBD DB;
    private Toolbar mToolbar;
    LinearLayout mRevealView;
    boolean hidden = true;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mRevealView = (LinearLayout) findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.INVISIBLE);

        //obtenemos los paremos que se nos han pasado al hacer el paso de un activity a otro
        Bundle bundle=this.getIntent().getExtras();
        email = bundle.getString("email");

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

            case R.id.action_attach:

                int cx = (mRevealView.getLeft() + mRevealView.getRight());
//                int cy = (mRevealView.getTop() + mRevealView.getBottom())/2;
                int cy = mRevealView.getTop();

                int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {


                    SupportAnimator animator =
                            ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(800);

                    SupportAnimator animator_reverse = animator.reverse();

                    if (hidden) {
                        mRevealView.setVisibility(View.VISIBLE);
                        animator.start();
                        hidden = false;
                    } else {
                        animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                            @Override
                            public void onAnimationStart() {

                            }

                            @Override
                            public void onAnimationEnd() {
                                mRevealView.setVisibility(View.INVISIBLE);
                                hidden = true;

                            }

                            @Override
                            public void onAnimationCancel() {

                            }

                            @Override
                            public void onAnimationRepeat() {

                            }
                        });
                        animator_reverse.start();

                    }
                } else {
                    if (hidden) {
                        Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                        mRevealView.setVisibility(View.VISIBLE);
                        anim.start();
                        hidden = false;

                    } else {
                        Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, radius, 0);
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mRevealView.setVisibility(View.INVISIBLE);
                                hidden = true;
                            }
                        });
                        anim.start();

                    }
                }

                return true;

            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
                DB.updateNote(title, content, this.title);
                try {
                    JSONObject userParam = new JSONObject();
                    System.out.println("EMAIL: " + email);
                    userParam.put("idUser",0);
                    userParam.put("email",email);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("tittle", title);
                    jsonParam.put("content", content);
                    Date dateNote = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    jsonParam.put("dateNote", dateFormat.format(dateNote));
                    jsonParam.put("idUser", userParam);

                    new UpdateHttp(this).execute("http://192.168.1.127:8080/PrettyNotesWS/webresources/entity.note/"+ email + "/" + this.title, jsonParam.toString());

                }catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(this,MainActivity.class);
                intent.putExtra("email", email);
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
        alerta.setButton2("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alerta.setButton("Eliminar nota", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete();
            }
        });
        alerta.show();
    }

    private void delete(){
        DB = new NoteBD(this);
        DB.deleteNote(title);
        new DeleteHttp(this).execute("http://192.168.1.127:8080/PrettyNotesWS/webresources/entity.note/" + email + "/" + title);
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
}
