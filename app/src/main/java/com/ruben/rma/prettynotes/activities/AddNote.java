package com.ruben.rma.prettynotes.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.util.Base64;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ruben.rma.prettynotes.connectionws.PostHttp;
import com.ruben.rma.prettynotes.data.NoteBD;
import com.ruben.rma.prettynotes.R;
import com.ruben.rma.prettynotes.model.Note;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by RMA on 14/04/2015.
 */
public class AddNote extends AppCompatActivity {

    EditText TITLE, CONTENT;
    NoteBD DB;
    private Toolbar mToolbar;
    LinearLayout mRevealView;
    boolean hidden = true, locationSaved = false;
    String msj, email;
    double latitude, longitude;
    int getId;

    private ListFragment mList;
    private MapAdapter mAdapter;

    private String APP_DIRECTORY = "DCIM/";
    private String MEDIA_DIRECTORY = APP_DIRECTORY + "Camera";
    private String TEMPORAL_PICTURE_NAME = "temporal.jpg";
    private String FOTO_TIEMPO = "";
    Uri path = null;
    private final int PHOTO_CODE = 100;
    private final int SELECT_PICTURE = 200;
    private ImageView imageView;

    private ImageButton btnSpeak;
    private TextView txtSpeechInput;
    private final int REQ_CODE_SPEECH_INPUT = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_note);

        imageView = (ImageView) findViewById(R.id.setPicture);
        ImageButton button = (ImageButton) findViewById(R.id.buttonImage);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRevealView.setVisibility(View.INVISIBLE);
                final CharSequence[] options = {"Tomar foto", "Elegir de galeria", "Cancelar"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(AddNote.this);
                builder.setTitle("Elige una opcion");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int seleccion) {
                        if (options[seleccion] == "Tomar foto") {
                            openCamera();
                        } else if (options[seleccion] == "Elegir de galeria") {
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                        }
                    }
                });
                builder.show();
            }
        });

        Bundle bundle = this.getIntent().getExtras();
        email = bundle.getString("email");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        TITLE = (EditText) findViewById(R.id.editText_Titulo);
        CONTENT = (EditText) findViewById(R.id.editText_Nota);
        mRevealView = (LinearLayout) findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.INVISIBLE);

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
        latitude =  0;
        LatLng  currentLocation = new LatLng(latitude, longitude);
        ImageButton mapButton = (ImageButton) findViewById(R.id.mapButton);
        NamedLocation[] LIST_LOCATIONS = new NamedLocation[]{
                new NamedLocation(getLocationName(latitude,longitude), currentLocation)
        };
        mAdapter = new MapAdapter(this, LIST_LOCATIONS);
        mList = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        mList.getView().setVisibility(View.INVISIBLE);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRevealView.setVisibility(View.INVISIBLE);
                mList.getView().setVisibility(View.VISIBLE);
                mList.setListAdapter(mAdapter);
                locationSaved = true;
            }
        });

        txtSpeechInput = (TextView) findViewById(R.id.editText_Nota);
        btnSpeak = (ImageButton) findViewById(R.id.audio_image);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRevealView.setVisibility(View.INVISIBLE);
                promptSpeechInput();
            }
        });
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

            case R.id.action_attach:
                int cx = (mRevealView.getLeft() + mRevealView.getRight());
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void addUpdateNotes(){
        //Se ha podido llegar a la funcion tanto para añadir una nota como para editarla
        DB =new NoteBD(this);
        Note note = new Note();
        //Convertimos el titulo y el contenido a cadena de texto
        note.setTittle(TITLE.getText().toString());
        note.setContent(CONTENT.getText().toString());
        String latitudeNote = String.valueOf(latitude);
        String longitudeNote = String.valueOf(longitude);
        note.setLatitude(latitudeNote);
        note.setLongitude(longitudeNote);

        if(note.getTittle().equals("")){
            //El titulo no puede estar vacío
            msj="Añade un título";
            TITLE.requestFocus();
            Mensaje(msj);
        }else{
            if(note.getContent().equals("")){
                //el contendido no puede estar vacío
                msj="Añade el contenido";
                CONTENT.requestFocus();
                Mensaje(msj);
            }else{
                //Una vez asegurados que han escrito en los dos campos recorremos la base de datos comprobando
                //que no hay una nota con igual título
                Cursor c = DB.getNote(note.getTittle());
                String gettitle="";
                //Nos aseguramos de que existe al menos un registro
                if(c.moveToFirst()){
                    //Recorremos el cursor hasta que no haya mas registros
                    do{
                        gettitle=c.getString(1);
                        getId = c.getInt(0);
                        //Pongo 1 xq columna empieza desde valor 0, y en el 0 esta el id de la nota, en el 1 el titulo de la nota y el el 2 el contenido
                    }while(c.moveToNext());
                }
                if(gettitle.equals(note.getTittle())){
                    TITLE.requestFocus();
                    msj="EL título de la nota ya existe";
                    Mensaje(msj);
                }else{
                    Date dateNote = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    if(locationSaved){
                        if(path != null) {
                            DB.addNote(note.getTittle(), note.getContent(), dateFormat.format(dateNote), note.getLatitude(), note.getLongitude(), email, path.toString());
                        }else{
                            DB.addNote(note.getTittle(), note.getContent(), dateFormat.format(dateNote), note.getLatitude(), note.getLongitude(), email, null);
                        }
                    }else{
                        if(path != null) {
                            DB.addNote(note.getTittle(), note.getContent(), dateFormat.format(dateNote), null, null, email, path.toString());
                        }else{
                            DB.addNote(note.getTittle(), note.getContent(), dateFormat.format(dateNote), null, null, email, null);
                        }
                    }

                    try {
                        JSONObject userParam = new JSONObject();
                        System.out.println("EMAIL: " + email);
                        userParam.put("idUser",0);
                        userParam.put("email",email);

                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("tittle", note.getTittle());
                        jsonParam.put("content", note.getContent());

                        if(path != null) {
                            int i =0;
                            //byte[] photo = convertImageToByte(path);
                            //System.out.println("Aqui los datos son estos: " + photo.toString());
                            //String p = Base64.encodeToString(photo, Base64.DEFAULT);
                            //jsonParam.put("photo", p);
                        }
                        if(locationSaved){
                            jsonParam.put("latitude", note.getLatitude());
                            jsonParam.put("longitude", note.getLongitude());
                        }else{
                            jsonParam.put("latitude", null);
                            jsonParam.put("longitude", null);
                        }
                        jsonParam.put("dateNote", dateFormat.format(dateNote));
                        jsonParam.put("idUser", userParam);



                        new PostHttp(this).execute("http://192.168.1.127:8080/PrettyNotesWS/webresources/entity.note?", jsonParam.toString());

                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Volvemos al activity principal
                    Intent intent = new Intent(AddNote.this,MainActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                }
            }
        }
    }

    public  void Mensaje (String msj){
        Toast toast = Toast.makeText(this,msj, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(data.location, 13f));
        map.addMarker(new MarkerOptions().position(data.location));

        // Set the map type back to normal.
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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

    public byte[] convertImageToByte(Uri uri){
        byte[] data = null;
        System.out.println("Uri: " + uri);
        try {
            ContentResolver cr = getBaseContext().getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            System.out.println("Error al convertir la foto");
        }
        System.out.println("Data : " + data.length + data.toString());
        return data;
    }
}
