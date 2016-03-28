package com.ruben.rma.prettynotes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by RMA on 14/04/2015.
 */
public class NoteBD extends SQLiteOpenHelper {
    public  static final String TABLE_ID ="idNote";
    public  static final String TITLE ="title";
    public  static final String CONTENT ="content";

    public  static final String DATABASE ="Note";
    public  static final String TABLE="notes";

    public NoteBD(Context context) {
        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creamos la base de datos, de forma que sea atumatica con respecto a los id que se le añaden
        //Vital importancion los espaciones entre cadenas de caracteres ya que es como si solo formara una por las operaciones +
        db.execSQL("CREATE TABLE "+TABLE+"("+TABLE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+TITLE+" TEXT,"+CONTENT+" TEXT)");


    }
    //Existe porque debe implementarlo, ya que extiende del tipo SQL
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS"+TABLE);
        onCreate(db);
    }
    //Añadimos notas
    public void  addNote (String title, String content){
        ContentValues valores = new ContentValues();
        valores.put(TITLE, title);
        valores.put(CONTENT,content);
        //Funcion insertar
        this.getWritableDatabase().insert(TABLE,null,valores);
    }

    //Mediante este metodo se devuelve una nota con el titulo concreto
    public Cursor getNote (String condition){
        String columnas[]={TABLE_ID,TITLE,CONTENT};
        String[] args = new String[] {condition};
        //Accedemos a la base de datos para buscar
        Cursor c = this.getReadableDatabase().query(TABLE,columnas,TITLE+"=?",args,null,null,null);
        return c;
    }
    //eliminamos la nota con el string que coincida con el titulo
    public void deleteNote (String condition){
        String args[]={condition};
        this.getWritableDatabase().delete(TABLE,TITLE+"=?",args);
    }

    //Reenscribimos la nota editada
    public void updateNote (String title, String content, String condition){
        String args[]={condition};
        ContentValues valores = new ContentValues();
        valores.put(TITLE, title);
        valores.put(CONTENT,content);
        this.getWritableDatabase().update(TABLE,valores,TITLE+"=?",args);
    }

    //Mediante este metodo se devuelven todas las notas
    public Cursor getNotes(){
        String columnas[]={TABLE_ID, TITLE, CONTENT};
        Cursor c = this.getReadableDatabase().query(TABLE, columnas, null, null, null, null, null);
        return c;
    }

    //Eliminamos las notas
    public void deleteNotes(){
        //Funcion delete para eliminar toda la base de datos
        this.getWritableDatabase().delete(TABLE,null,null);
    }

}
