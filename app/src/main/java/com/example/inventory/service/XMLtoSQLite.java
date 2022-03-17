/*
package com.example.inventory.service;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class XMLtoSQLite extends SQLiteOpenHelper {

    private final Context fContext;

    // Set TAG for error catching
    public static String TAG = "XMLtoSQLite";

    // Set database columns
    public static String column_ID = null;
    public static String column_post_content = null;
    public static String column_post_title = null;

    public XMLtoSQLite(Context context) {
        super(context, "amawal", null, 1);
        fContext = context;
    }


    public void createDataBase (SQLiteDatabase db) throws IOException {
        db.execSQL("CREATE TABLE amawal_posts (" + "ID INTEGER PRIMARY KEY,"
                + "post_content TEXT," + "post_content TEXT" + ");");

        // Add default records amawal_posts
        ContentValues Columns = new ContentValues();

        // Get XML resource file
        Resources res = fContext.getResources();

        // Open XML file
        int eventType = -1;
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            XmlResourceParser database = res.getXml(res.x);
            String name = database.getText();
            Log.d(TAG, name);

            try {
                if (database.getEventType() == XmlResourceParser.START_TAG)
                {
                    String s = database.getName();
//
                    if (s.equals("table"))
                    {
                        database.next(); // moving to the next node
                        if (database.getName() != null  && database.getName().equalsIgnoreCase ( "column"))
                        {
                            column_ID = database.getText(); // to get  value getText() method should be used
                            database.next();

                            column_post_content = database.getText();
                            database.next();

                            column_post_title = database.getText();

                            // Insert the values inside the DB
                            Columns.put("ID", column_ID);
                            Columns.put("post_content", column_post_content);
                            Columns.put("post_title", column_post_title);

                            db.insert("amawal", null, Columns);

                        }

                        Log.d(TAG, column_ID);
                        Log.d(TAG, column_post_content);
                        Log.d(TAG, column_post_title);
                    }
                }
            }
            //Catch errors
            catch (XmlPullParserException e)
            {
                Log.e(TAG, e.getMessage(), e);
            }
            catch (IOException e)
            {
                Log.e(TAG, e.getMessage(), e);

            }
            finally
            {
                //Close the XML file
                database.close();
            }
        }
    }

 */
/*Update database to latest version *//*


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Crude update, make sure to implement a correct one when needed.

        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS animals");
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

    }

}
*/
