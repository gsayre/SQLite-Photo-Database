package com.example.asmt3;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    ImageView img = null;
    TextView sizeinfo = null;
    TextView taginfo = null;
    TextView size = null;
    TextView tag = null;
    SQLiteDatabase db = null;
    byte [] bmap = null;
    String sizeText = null;
    String tagText = null;
    Button nextB = null;
    Button prevB = null;
    TextView table = null;

    ArrayList<byte[]> bitmapArray = new ArrayList<byte[]>();
    ArrayList<String> idArray = new ArrayList<String>();
    ArrayList<String> tagArray = new ArrayList<String>();
    ArrayList<String> sizeArray = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = this.openOrCreateDatabase("SOMEDB", Context.MODE_PRIVATE, null);

        img = findViewById(R.id.imgV);
        sizeinfo = findViewById(R.id.sizeinfo);
        taginfo = findViewById(R.id.taginfo);
        size = findViewById(R.id.size);
        tag = findViewById(R.id.tag);

        nextB = findViewById(R.id.next);
        table = findViewById(R.id.Table);


        db.execSQL("DROP TABLE IF EXISTS Photos;");
        db.execSQL("DROP TABLE IF EXISTS Tags;");
        db.execSQL("CREATE TABLE Photos(ID INT,Photo Blob, Size Text);");
        db.execSQL("CREATE TABLE Tags(ID INT,Tag Text);");
    }

    void capture(View v) {
        Intent x = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(x, 123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");
        img.setBackgroundResource(0);
        img.setImageBitmap(imageBitmap);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        bmap = outputStream.toByteArray();

        sizeinfo.setText("Size: " + bmap.length);
        size.setText("" + bmap.length);
    }

    int photoNum = 1;

    public void save(View v) {

        String sizeText = size.getText().toString();
        String tagText = tag.getText().toString();


        if (TextUtils.isEmpty(size.getText()) || TextUtils.isEmpty(tag.getText()) || bmap == null) {
            // do nothing
            Toast t1 = Toast.makeText(this,"Missing size,tag, or picture",Toast.LENGTH_LONG);
            t1.show();
        } else {


            ContentValues cv = new ContentValues();
            cv.put("ID", photoNum);
            cv.put("Photo", bmap);
            cv.put("Size", sizeText);
            db.insert("Photos", null, cv);
            Toast t2 = Toast.makeText(this, "Photo Inserted",Toast.LENGTH_LONG);
            t2.show();

            String[] splitTags = tagText.split(";");


            for (int i = 0; i < splitTags.length; i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("ID", photoNum);
                contentValues.put("Tag", splitTags[i]);
                db.insert("Tags", null, contentValues);
            }

            String dbStr = "";
            Cursor c = db.rawQuery("SELECT Photos.ID, Tags.Tag, Photos.Size FROM Photos, Tags WHERE Photos.ID = Tags.ID", null);
            while(c.moveToNext()) {
                dbStr += c.getString(0);
                dbStr = dbStr + ", " + c.getString(1);
                dbStr = dbStr + ", " + c.getString(2) + "\n";
            }
            table.setText(dbStr);

            size.setText("");
            tag.setText("");
            img.setImageResource(0);
            bmap = null;
            photoNum++;
        }
    }

    public void load(View v) {
        idArray.clear();
        bitmapArray.clear();
        tagArray.clear();
        sizeArray.clear();

        String sizet = size.getText().toString();
        String tagt = tag.getText().toString();
        String[] splitTags = tagt.split(";");
        String tagResult = "";


        if (TextUtils.isEmpty(size.getText())) {
            for (int i = 0; i < splitTags.length; i++) {
                String[] queryTags = new String[] {splitTags[i]};
                Cursor c = db.rawQuery("SELECT Photos.ID, Photos.Photo, Tags.Tag, Photos.Size FROM Photos, Tags WHERE Tags.Tag = ? AND Photos.ID = Tags.ID;", queryTags);
                while (c.moveToNext()) {
                    idArray.add(c.getString(0));
                    bitmapArray.add(c.getBlob(1));
                    sizeArray.add(c.getString(3));

                }
            }
            for (int j = 0; j < idArray.size(); j++) {
                String[] idNumbs = new String[] {idArray.get(j)};
                Cursor c2 = db.rawQuery("SELECT Tags.Tag FROM Photos, Tags WHERE Tags.ID = ? AND Photos.ID = Tags.ID;", idNumbs);
                while(c2.moveToNext()) {
                    tagResult += c2.getString(0) + "; ";
                }
                tagArray.add(tagResult);
                tagResult = "";
            }



            Bitmap bmapRes = BitmapFactory.decodeByteArray(bitmapArray.get(0), 0, bitmapArray.get(0).length);

            taginfo.setText("Tags: " + tagArray.get(0));
            sizeinfo.setText("Size: " + sizeArray.get(0));
            img.setImageBitmap(bmapRes);


        } else if (TextUtils.isEmpty(tag.getText())) {

            Cursor c = db.rawQuery("SELECT Photos.ID, Photos.Photo, Tags.Tag, Photos.Size FROM Photos, Tags WHERE (Photos.Size * 1.25) >= ? OR (Photos.Size *.75) <= ? AND Photos.ID = Tags.ID;", new String[]{sizet, sizet});
            while (c.moveToNext()) {
                idArray.add(c.getString(0));
                bitmapArray.add(c.getBlob(1));
                sizeArray.add(c.getString(3));

            }

        for (int j = 0; j < idArray.size(); j++) {
            String[] idNumbs = new String[] {idArray.get(j)};
            Cursor c2 = db.rawQuery("SELECT Tags.Tag FROM Photos, Tags WHERE Tags.ID = ? AND Photos.ID = Tags.ID;", idNumbs);
            while(c2.moveToNext()) {
                tagResult += c2.getString(0) + "; ";
            }
            tagArray.add(tagResult);
            tagResult = "";
        }

            Bitmap bmapRes = BitmapFactory.decodeByteArray(bitmapArray.get(0), 0, bitmapArray.get(0).length);


            taginfo.setText("Tags: " + tagArray.get(0));
            sizeinfo.setText("Size: " + sizeArray.get(0));
            img.setImageBitmap(bmapRes);

        } else {
            for (int i = 0; i < splitTags.length; i++) {
                String[] queryTags = new String[] {splitTags[i]};
                Cursor c = db.rawQuery("SELECT Photos.ID, Photos.Photo, Tags.Tag, Photos.Size FROM Photos, Tags WHERE Tags.Tag = ? AND  (Photos.Size * 1.25) >= ? OR (Photos.Size *.75) <= ? AND Photos.ID = Tags.ID;", new String[]{splitTags[i], sizet, sizet});
                while (c.moveToNext()) {
                    idArray.add(c.getString(0));
                    bitmapArray.add(c.getBlob(1));
                    sizeArray.add(c.getString(3));

                }
            }
            for (int j = 0; j < idArray.size(); j++) {
                String[] idNumbs = new String[] {idArray.get(j)};
                Cursor c2 = db.rawQuery("SELECT Tags.Tag FROM Photos, Tags WHERE Tags.ID = ? AND Photos.ID = Tags.ID;", idNumbs);
                while(c2.moveToNext()) {
                    tagResult += c2.getString(0) + "; ";
                }
                tagArray.add(tagResult);
                tagResult = "";
            }
            Bitmap bmapRes = BitmapFactory.decodeByteArray(bitmapArray.get(0), 0, bitmapArray.get(0).length);
            taginfo.setText("Tags: " + tagArray.get(0));
            sizeinfo.setText("Size: " + sizeArray.get(0));
            img.setImageBitmap(bmapRes);

        }
    }
    Integer position = 0;
    public void next(View v) {
        Log.v("POS", "" + position);
        switch (v.getId()) {
            case R.id.next: {
                position--;
                if (position < 0) {
                    Log.v("IDAR", "" + idArray.size());
                    Log.v("POS", "" + position);
                    position = idArray.size() - 1;
                    Bitmap bmap = BitmapFactory.decodeByteArray(bitmapArray.get(position), 0, bitmapArray.get(position).length);
                    taginfo.setText("Tags: " + tagArray.get(position));
                    sizeinfo.setText("Size: " + sizeArray.get(position));
                    img.setImageBitmap(bmap);
                } else {
                    Log.v("POS", "" + position);
                    Bitmap bmap = BitmapFactory.decodeByteArray(bitmapArray.get(position), 0, bitmapArray.get(position).length);
                    taginfo.setText("Tags: " + tagArray.get(position));
                    sizeinfo.setText("Size: " + sizeArray.get(position));
                    img.setImageBitmap(bmap);
                }
            }
        }
    }
}
