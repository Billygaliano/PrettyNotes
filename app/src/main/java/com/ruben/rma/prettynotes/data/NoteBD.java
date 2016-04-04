package com.ruben.rma.prettynotes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteBD extends SQLiteOpenHelper {
    public static final String TABLE_ID ="idNote";
    public static final String TITLE ="title";
    public static final String CONTENT ="content";
    public static final String DATE ="date";
    public static final String ID_USER ="idUser";
    public static final String EMAIL ="email";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public  static final String IMAGE = "image";
    public static final String TABLE="notes";
    public static final String TABLE_USER="userNote";
    public static final String DATABASE ="Note";

    public NoteBD(Context context) {
        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creamos la base de datos, de forma que sea atumatica con respecto a los id que se le añaden
        //Vital importancion los espaciones entre cadenas de caracteres ya que es como si solo formara una por las operaciones +
        db.execSQL("CREATE TABLE "+TABLE+"("+TABLE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " + TITLE + " TEXT," + CONTENT + " TEXT," + DATE + " TEXT," + IMAGE + " TEXT," + EMAIL +" TEXT," + LATITUDE + " TEXT," + LONGITUDE + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_USER + "(" + ID_USER + " INTEGER PRIMARY KEY AUTOINCREMENT, " + EMAIL + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS"+TABLE);
        db.execSQL("DROP TABLE IF EXISTS"+TABLE_USER);
        onCreate(db);
    }

    public Cursor getUserNote (String condition){
        String columnas[]={ID_USER,EMAIL};
        String[] args = new String[] {condition};
        //Accedemos a la base de datos para buscar
        Cursor c = this.getReadableDatabase().query(TABLE_USER,columnas,EMAIL+"=?",args,null,null,null);

        return c;
    }

    public Long  addNote (String title, String content,String date, String latitude, String longitude, String email, String image){
        ContentValues valores = new ContentValues();
        valores.put(TITLE, title);
        valores.put(CONTENT, content);
        valores.put(DATE, date);
        valores.put(EMAIL, email);
        valores.put(IMAGE, image);
        valores.put(LATITUDE, latitude);
        valores.put(LONGITUDE, longitude);

        return this.getWritableDatabase().insert(TABLE,null,valores);
    }

    public Cursor getNote (String condition){
        String columnas[]={TABLE_ID,TITLE,CONTENT,LATITUDE,LONGITUDE,IMAGE};
        String[] args = new String[] {condition};
        //Accedemos a la base de datos para buscar
        Cursor c = this.getReadableDatabase().query(TABLE,columnas,TITLE+"=?",args,null,null,null);

        return c;
    }

    public Cursor getNoteByUser (String condition){
        String columnas[]={TABLE_ID,TITLE,CONTENT,DATE,IMAGE,LATITUDE,LONGITUDE};
        String[] args = new String[] {condition};
        //Accedemos a la base de datos para buscar
        Cursor c = this.getReadableDatabase().query(TABLE,columnas,EMAIL+"=?",args,null,null,null);

        return c;
    }

    public void deleteNote (String condition){
        String args[]={condition};
        this.getWritableDatabase().delete(TABLE,TITLE+"=?",args);
    }

    public void updateNote (String title, String content, String latitude, String longitude, String image, String condition){
        String args[]={condition};
        ContentValues valores = new ContentValues();
        valores.put(TITLE, title);
        valores.put(CONTENT,content);
        valores.put(LATITUDE,latitude);
        valores.put(LONGITUDE,longitude);
        valores.put(IMAGE,image);
        this.getWritableDatabase().update(TABLE,valores,TITLE+"=?",args);
    }

    public void deleteNotes(){
        //Funcion delete para eliminar toda la base de datos
        this.getWritableDatabase().delete(TABLE,null,null);
    }

}
