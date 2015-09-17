package dtancompany.gallerytest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Edward on 15-07-02.
 */
public class FaceDetect {

    int faceWidth;
    int faceHeight;
    int maxFaces;
    Bitmap b;
    FaceDetector.Face[] faces;
    float eyeDistance;

    public FaceDetect(int maxFaces, Bitmap b){

        this.faceWidth = b.getWidth();
        this.faceHeight = b.getHeight();
        this.maxFaces = maxFaces;
        faces = new FaceDetector.Face[maxFaces];
        this.b = b;

    }
    public List<FaceDetector.Face> detectFaces(){
        FaceDetector faceDetector = new FaceDetector(faceWidth, faceHeight, maxFaces);
        faceDetector.findFaces(b, faces);
        List<FaceDetector.Face> faceList= new ArrayList<>(Arrays.asList(faces));
        for (Iterator<FaceDetector.Face> iterator = faceList.iterator(); iterator.hasNext();) {
            FaceDetector.Face face = iterator.next();
            if(face == null){ iterator.remove();}
            else if (face.confidence() < FaceDetector.Face.CONFIDENCE_THRESHOLD) {
                iterator.remove();
            }
        }
        return faceList;

    }



    public void drawFaces(Bitmap picture, List<FaceDetector.Face> faces){

        Canvas canvas = new Canvas(picture);

        Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(3);
        myPaint.setTextSize(100);
        // lol remove this when done
        int i = 0;
        for(FaceDetector.Face face : faces){
            PointF midpoint = new PointF();
            face.getMidPoint(midpoint);
            eyeDistance = face.eyesDistance();
            Log.d("SquareBottom", String.valueOf((int) (midpoint.y + eyeDistance * 2)));

            canvas.drawRect((int) (midpoint.x - eyeDistance * 2),

            (int) (midpoint.y - eyeDistance * 2),

            (int) (midpoint.x + eyeDistance * 2),

            (int) (midpoint.y + eyeDistance * 2), myPaint);


            // lol
            //canvas.drawText(String.valueOf(i), midpoint.x, midpoint.y, myPaint);
            //i++;


        }
    }

    public static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
            imgIn.copyPixelsToBuffer(map);
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }





}
