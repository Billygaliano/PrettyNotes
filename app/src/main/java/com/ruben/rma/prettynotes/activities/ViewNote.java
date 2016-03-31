package com.ruben.rma.prettynotes.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ruben.rma.prettynotes.connectionws.DeleteHttp;
import com.ruben.rma.prettynotes.connectionws.UpdateHttp;
import com.ruben.rma.prettynotes.data.NoteBD;
import com.ruben.rma.prettynotes.R;
import com.ruben.rma.prettynotes.model.Note;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by RMA on 14/04/2015.
 */
public class ViewNote extends AppCompatActivity implements TextToSpeech.OnInitListener {
    String title, content, email;
    TextView TITLE,CONTENT;
    NoteBD DB;
    private Toolbar mToolbar;
    LinearLayout mRevealView;
    boolean hidden = true;
    private ListFragment mList;
    private MapAdapter mAdapter;
    double latitude, longitude, oldLatitude, oldLongitude;
    private String APP_DIRECTORY = "DCIM/";
    private String MEDIA_DIRECTORY = APP_DIRECTORY + "Camera";
    private String TEMPORAL_PICTURE_NAME = ".jpg";
    private String FOTO_TIEMPO = "";
    Uri path = null;
    private final int PHOTO_CODE = 100;
    private final int SELECT_PICTURE = 200;
    private ImageView imageView;
    private String oldImage;
    private boolean locationSaved = false;

    private ImageButton btnSpeak;
    private TextView txtSpeechInput;
    private final int REQ_CODE_SPEECH_INPUT = 300;
    private Button btn;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);

        imageView = (ImageView) findViewById(R.id.setPicture);
        ImageButton button = (ImageButton) findViewById(R.id.buttonImage);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRevealView.setVisibility(View.INVISIBLE);
                final CharSequence[] options = {"Tomar foto", "Elegir de galeria", "Cancelar"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(ViewNote.this);
                builder.setTitle("Elige una opcion :D");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int seleccion) {
                        if (options[seleccion] == "Tomar foto") {
                            openCamera();
                        } else if (options[seleccion] == "Elegir de galeria") {
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                        } else if (options[seleccion] == "Cancelar") {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        LatLng  oldLocation = null;

        mRevealView = (LinearLayout) findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.INVISIBLE);

        mList = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        mList.getView().setVisibility(View.INVISIBLE);

        //obtenemos los paremos que se nos han pasado al hacer el paso de un activity a otro
        Bundle bundle = this.getIntent().getExtras();
        email = bundle.getString("email");

        //lo insertamos en sus respectivas variables para tratar con ellos
        title = bundle.getString("title");
        content = bundle.getString("content");

        if(bundle.getString("latitude") != null){
            oldLatitude = Double.parseDouble(bundle.getString("latitude"));
            oldLongitude = Double.parseDouble(bundle.getString("longitude"));
            oldLocation = new LatLng(oldLatitude, oldLongitude);
            mList.getView().setVisibility(View.VISIBLE);
            locationSaved = true;
        }

        if(bundle.getString("image") != null){
            oldImage = bundle.getString("image");
            path = Uri.parse(oldImage);
            imageView.setImageURI(Uri.parse(oldImage));
        }

        //Hacemos referencia a los textview de vernota.xml
        TITLE=(TextView)findViewById(R.id.textView_titulo);
        CONTENT=(TextView)findViewById(R.id.textView_content);
        //Le cambiamos el texto al titulo y el contenido para que posea el de la nota correspondiente que estemos viendo
        TITLE.setText(title);
        CONTENT.setText(content);


        // Set a custom list adapter for a list of locations
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = 0;
        latitude = 0;
        LatLng  currentLocation = new LatLng(latitude, longitude);

        ImageButton mapButton = (ImageButton) findViewById(R.id.mapButton);
        NamedLocation[] LIST_LOCATIONS;
        if(oldLocation != null){
            LIST_LOCATIONS = new NamedLocation[]{
                    new NamedLocation(getLocationName(oldLatitude,oldLongitude), oldLocation)
            };
        }else{
            LIST_LOCATIONS = new NamedLocation[]{
                new NamedLocation(getLocationName(latitude,longitude), currentLocation)
            };
        }
        mAdapter = new MapAdapter(this, LIST_LOCATIONS);
        mList.setListAdapter(mAdapter);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRevealView.setVisibility(View.INVISIBLE);
                mList.getView().setVisibility(View.VISIBLE);
                mList.setListAdapter(mAdapter);
                locationSaved = true;
            }
        });

        tts = new TextToSpeech(this, this);
        txtSpeechInput = (TextView) findViewById(R.id.textView_content);
        btnSpeak = (ImageButton) findViewById(R.id.audio_image);
        btn = (Button) findViewById(R.id.btn);


        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRevealView.setVisibility(View.INVISIBLE);
                promptSpeechInput();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                speakOut();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case PHOTO_CODE:
                if(resultCode == RESULT_OK){
                    String dir =  Environment.getExternalStorageDirectory() + File.separator
                            + MEDIA_DIRECTORY + File.separator + FOTO_TIEMPO + TEMPORAL_PICTURE_NAME;
                    decodeBitmap(dir);
                    path = Uri.parse(dir);
                }
                break;

            case SELECT_PICTURE:
                if(resultCode == RESULT_OK){
                    path = data.getData();
                    imageView.setImageURI(path);
                }
                break;

            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                }
                break;
            }
        }

    }

    private void decodeBitmap(String dir) {
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(dir);

        imageView.setImageBitmap(bitmap);
    }

    private void openCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        file.mkdirs();

        Date dateNote = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String datefoto = dateFormat.format(dateNote);
        FOTO_TIEMPO = datefoto.replace('-','_').replace(' ','_').replace(':','_');

        String path = Environment.getExternalStorageDirectory() + File.separator
                + MEDIA_DIRECTORY + File.separator + FOTO_TIEMPO + TEMPORAL_PICTURE_NAME;

        File newFile = new File(path);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
        startActivityForResult(intent, PHOTO_CODE);
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
        String msj;

        Note note = new Note();
        //Convertimos el titulo y el contenido a cadena de texto
        note.setTittle(TITLE.getText().toString());
        note.setContent(CONTENT.getText().toString());
        String latitudeNote = String.valueOf(latitude);
        String longitudeNote = String.valueOf(longitude);
        note.setLatitude(latitudeNote);
        note.setLongitude(longitudeNote);

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
                if(locationSaved){
                    if(path != null) {
                        DB.updateNote(note.getTittle(), note.getContent(), note.getLatitude(), note.getLongitude(), path.toString(), this.title);
                    }else{
                        DB.updateNote(note.getTittle(), note.getContent(), note.getLatitude(), note.getLongitude(), null, this.title);
                    }
                }else{
                    if(path != null) {
                        DB.updateNote(note.getTittle(), note.getContent(), null, null, path.toString(), this.title);
                    }else{
                        DB.updateNote(note.getTittle(), note.getContent(), null, null, null, this.title);
                    }
                }

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
                    if(locationSaved){
                        jsonParam.put("latitude", note.getLatitude());
                        jsonParam.put("longitude", note.getLongitude());
                    }else{
                        jsonParam.put("latitude", null);
                        jsonParam.put("longitude", null);
                    }

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
        Toast toast = Toast.makeText(this, msj, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
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

        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("tittle", title);
            jsonParam.put("email", email);
            new DeleteHttp(this).execute("http://192.168.1.127:8080/PrettyNotesWS/webresources/entity.note/1/1", jsonParam.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private void speakOut() {
        String text = txtSpeechInput.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adapter that displays a title and {@link com.google.android.gms.maps.MapView} for each item.
     * The layout is defined in <code>lite_list_demo_row.xml</code>. It contains a MapView
     * that is programatically initialised in
     * {@link #getView(int, android.view.View, android.view.ViewGroup)}
     */
    private class MapAdapter extends ArrayAdapter<NamedLocation> {

        private final HashSet<MapView> mMaps = new HashSet<MapView>();

        public MapAdapter(Context context, NamedLocation[] locations) {
            super(context, R.layout.add_note, R.id.lite_listrow_text, locations);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;

            // Check if a view can be reused, otherwise inflate a layout and set up the view holder
            if (row == null) {
                // Inflate view from layout file
                row = getLayoutInflater().inflate(R.layout.map_list, null);

                // Set up holder and assign it to the View
                holder = new ViewHolder();
                holder.mapView = (MapView) row.findViewById(R.id.lite_listrow_map);
                holder.title = (TextView) row.findViewById(R.id.lite_listrow_text);
                // Set holder as tag for row for more efficient access.
                row.setTag(holder);

                // Initialise the MapView
                holder.initializeMapView();

                // Keep track of MapView
                mMaps.add(holder.mapView);
            } else {
                // View has already been initialised, get its holder
                holder = (ViewHolder) row.getTag();
            }

            // Get the NamedLocation for this item and attach it to the MapView
            NamedLocation item = getItem(position);
            holder.mapView.setTag(item);

            // Ensure the map has been initialised by the on map ready callback in ViewHolder.
            // If it is not ready yet, it will be initialised with the NamedLocation set as its tag
            // when the callback is received.
            if (holder.map != null) {
                // The map is already ready to be used
                setMapLocation(holder.map, item);
            }

            // Set the text label for this item
            holder.title.setText(item.name);

            return row;
        }

        /**
         * Retuns the set of all initialised {@link MapView} objects.
         *
         * @return All MapViews that have been initialised programmatically by this adapter
         */
        public HashSet<MapView> getMaps() {
            return mMaps;
        }
    }

    /**
     * Displays a {@link AddNote.NamedLocation} on a
     * {@link com.google.android.gms.maps.GoogleMap}.
     * Adds a marker and centers the camera on the NamedLocation with the normal map type.
     */
    private static void setMapLocation(GoogleMap map, NamedLocation data) {
        // Add a marker for this item and set the camera
        if(data.location != null){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(data.location, 13f));
            map.addMarker(new MarkerOptions().position(data.location));

            // Set the map type back to normal.
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    /**
     * Holder for Views used in the {@link AddNote.MapAdapter}.
     * Once the  the <code>map</code> field is set, otherwise it is null.
     * When the {@link #onMapReady(com.google.android.gms.maps.GoogleMap)} callback is received and
     * the {@link com.google.android.gms.maps.GoogleMap} is ready, it stored in the {@link #map}
     * field. The map is then initialised with the NamedLocation that is stored as the tag of the
     * MapView. This ensures that the map is initialised with the latest data that it should
     * display.
     */
    class ViewHolder implements OnMapReadyCallback {

        MapView mapView;

        TextView title;

        GoogleMap map;

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(getApplicationContext());
            map = googleMap;
            NamedLocation data = (NamedLocation) mapView.getTag();
            if (data != null) {
                setMapLocation(map, data);
            }
        }

        /**
         * Initialises the MapView by calling its lifecycle methods.
         */
        public void initializeMapView() {
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }

    }

    /**
     * RecycleListener that completely clears the {@link com.google.android.gms.maps.GoogleMap}
     * attached to a row in the ListView.
     * Sets the map type to {@link com.google.android.gms.maps.GoogleMap#MAP_TYPE_NONE} and clears
     * the map.
     */
    private AbsListView.RecyclerListener mRecycleListener = new AbsListView.RecyclerListener() {

        @Override
        public void onMovedToScrapHeap(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder != null && holder.map != null) {
                // Clear the map and free up resources by changing the map type to none
                holder.map.clear();
                holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }

        }
    };

    /**
     * Location represented by a position ({@link com.google.android.gms.maps.model.LatLng} and a
     * name ({@link java.lang.String}).
     */
    private static class NamedLocation {

        public final String name;

        public final LatLng location;

        NamedLocation(String name, LatLng location) {
            this.name = name;
            this.location = location;
        }
    }

    private String getLocationName(double lattitude, double longitude) {
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(lattitude, longitude, 10);
            for (Address adrs : addresses) {
                if (adrs != null) {
                    String city = adrs.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                    } else {
                        //TO DO
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;

    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
