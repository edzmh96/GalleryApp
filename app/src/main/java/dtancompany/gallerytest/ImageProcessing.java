package dtancompany.gallerytest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by david on 2015-06-30.
 */
public class ImageProcessing {
    final static int MAX_FACES = 20;
    final static int MAX_BITMAP_SIZE = 600;

    public static Bitmap getBitmap(BitmapFactory.Options options, String imagePath,

                            int reqWidth, int reqHeight, boolean lessThanReq){
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, lessThanReq);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight, boolean lessThanReq) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            if (!lessThanReq) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }
            // Calculate the largest inSampleSize value that is a power of 2 and makes both
            // height and width smaller than the requested height and width.
            else {
                while ((height / inSampleSize) > reqHeight
                        || (width / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }
        }

        return inSampleSize;
    }

/*
    public static void getFaces(Context context, String imagePath) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bitmap = getBitmap(options, imagePath, MAX_BITMAP_SIZE, MAX_BITMAP_SIZE, true);
        //Bitmap bitmap = BitmapFactory.decodeFile(imagePath,options);

        //make sure the width of the image is even to comply with face detector api
        if (bitmap.getWidth() % 2 == 1) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() - 1, bitmap.getHeight());
        }

        //get Face objects
        FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), MAX_FACES);
        FaceDetector.Face[] faceArray = new FaceDetector.Face[MAX_FACES];

        faceDetector.findFaces(bitmap, faceArray);

        int faceCount = faceDetector.findFaces(bitmap, faceArray);

        List<FaceDetector.Face> faceList = new ArrayList<>(Arrays.asList(faceArray));

        int i = 0;
        while (i < faceList.size()) {
            FaceDetector.Face face = faceList.get(i);
            if (face == null || face.confidence() < FaceDetector.Face.CONFIDENCE_THRESHOLD) {
                faceList.remove(i);
            } else {
                i++;
            }
        }

        Log.v("getFaces", String.valueOf(faceCount));
        Log.v("getFaces", "Bitmap width = " + bitmap.getWidth());
        Log.v("getFaces", "Bitmap height = " + bitmap.getHeight());

        if (faceCount == 0) {
            return;
        }

        PointF point = new PointF();


        String dir = imagePath.substring(0,imagePath.length()-4);
        File dirFile = new File(dir);
        dirFile.mkdir();

        i = 0;
        for (FaceDetector.Face face : faceList) {

            face.getMidPoint(point);
            float eyesDist = face.eyesDistance();
            //make bitmap of the face only
            int x = (int) (Math.max(point.x - 2*eyesDist,0)); //make sure starting point isn't off the image
            int y = (int) (Math.max(point.y - 2*eyesDist,0));
            int width = (int) (Math.min(4*eyesDist,bitmap.getWidth()-x)); //make sure cropped image isn't bigger than base image
            int height = (int) (Math.min(4*eyesDist,bitmap.getHeight()-y));

            Bitmap cropped = Bitmap.createBitmap(bitmap, x, y, width, height);

            //specific dir for each image
            //TODO: specific dir for each person
            File file = new File(dir+"/1-portrait-"+i+".jpg");
            //make a jpeg file from bitmap
            try (
                    OutputStream fileOut = new FileOutputStream(file);
                    BufferedOutputStream buffOut = new BufferedOutputStream(fileOut)
            ) {
                cropped.compress(Bitmap.CompressFormat.JPEG, 100, buffOut);
                buffOut.flush();
            } catch (Exception e) {
                Log.e("getFaces", "stack trace", e);
            }
            i++;
        }
    }
    */

}