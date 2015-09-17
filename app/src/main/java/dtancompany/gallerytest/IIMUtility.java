package dtancompany.gallerytest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.nmote.iim4j.IIM;
import com.nmote.iim4j.IIMDataSetInfoFactory;
import com.nmote.iim4j.IIMFile;
import com.nmote.iim4j.IIMReader;
import com.nmote.iim4j.dataset.DataSet;
import com.nmote.iim4j.dataset.DataSetInfo;
import com.nmote.iim4j.dataset.InvalidDataSetException;
import com.nmote.iim4j.serialize.SerializationException;
import com.nmote.iim4j.stream.FileIIMInputStream;
import com.nmote.iim4j.stream.IIMNotFoundException;
import com.nmote.iim4j.stream.JPEGIIMInputStream;
import com.nmote.iim4j.stream.JPEGUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by david on 2015-06-18.
 */
public class IIMUtility {
    //TODO: should this class be a static utility, or should it need instances (with context)

    /*
        Simple file copying method using channels
     */
    //TODO: move this method somewhere
    public static void copyFile(File src, File dest) throws IOException {
        /*
        try (
                FileOutputStream fileOut = new FileOutputStream(dest);
                FileInputStream fileIn = new FileInputStream(src)
        ) { */
        FileOutputStream fileOut = null;
        FileInputStream fileIn = null;
        try {
            fileOut = new FileOutputStream(dest);
            fileIn = new FileInputStream(src);
            FileChannel inChannel = fileIn.getChannel();
            FileChannel outChannel = fileOut.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (fileIn != null) { fileIn.close(); }
            if (fileOut != null) { fileOut.close(); }
        }
    }



    /*
        This method makes a copy of a jpeg without any iptc data
        and inserts a new iimFile
        Any old iptc data is removed
     */
    public static boolean insertIIM(Context context, IIMFile iimFile, File jpegFile)
            throws IOException, InvalidDataSetException {
        File dir = context.getCacheDir();
        File tempFile = File.createTempFile("prefix", "suffix", dir);

        FileOutputStream fileOut = null;
        FileInputStream fileIn = null;
        BufferedOutputStream bufferedOut = null;
        BufferedInputStream bufferedIn = null;

        /*try (
                FileOutputStream fileOut = new FileOutputStream(tempFile);
                FileInputStream fileIn = new FileInputStream(jpegFile);
                OutputStream bufferedOut = new BufferedOutputStream(fileOut);
                InputStream bufferedIn = new BufferedInputStream(fileIn)
        ) { */

        try {
            fileOut = new FileOutputStream(tempFile);
            fileIn = new FileInputStream(jpegFile);
            //bufferedOut = new BufferedOutputStream(fileOut);
            //bufferedIn = new BufferedInputStream(fileIn);

            //check if iimfile has no contents
            List<DataSet> dataSetList = iimFile.getDataSets();
            if (dataSetList.size() == 0) {
                //if empty, then remove iim data from the file
                JPEGUtil.removeIIMFromJPEG(fileOut, fileIn);
            }
            else{
                JPEGUtil.insertIIMIntoJPEG(fileOut, iimFile, fileIn);
            }
        } finally {
            //if (bufferedIn != null) { bufferedIn.close(); }
            //if (bufferedOut != null) { bufferedOut.close(); }
            if (fileIn != null) { fileIn.close();}
            if (fileOut != null) { fileOut.close(); }
        }

        copyFile(tempFile, jpegFile);

        if (!tempFile.delete()) {
            tempFile.deleteOnExit();
        }
        return true;
    }

    /*
        Tries to retrieve IIM data from jpeg source file
     */
    //TODO: make the iim readers auto-closable (?)
    public static IIMFile getIIM(File src) throws IOException, InvalidDataSetException, IIMNotFoundException {
        Log.v("getIIM", "file length is " + src.length());
        IIMFile ret = new IIMFile();
        /*
        FileIIMInputStream fileIn = null;
        JPEGIIMInputStream jpegIn = null;
        IIMReader reader = null;
        try {
            fileIn = new FileIIMInputStream(src);
            jpegIn = new JPEGIIMInputStream(fileIn);
            reader = new IIMReader(jpegIn, new IIMDataSetInfoFactory());
            ret.readFrom(reader, 1000);
        } */
        IIMReader reader = null;

        try {
            //the above does not work, use this instead
            reader = new IIMReader(new JPEGIIMInputStream(new FileIIMInputStream(src)),
                    new IIMDataSetInfoFactory());
            ret.readFrom(reader, 1000);
        }catch (EOFException e){
            removeIIM(src);
        }
        finally {
            if (reader != null) { reader.close(); }
        }
        Log.v("IIMUtility", "IIMFile is now " + ret.toString());
        return ret;
    }

    /*
        Adds keyword to target IIMFile ; Keywords should not exceed 64 octets (iptc iim 4.2)
     */
    public static boolean addKeyword(Context context, IIMFile target, String keyword)
            throws InvalidDataSetException, SerializationException {
        if (keyword == null || target == null) {
            return false;
        }

        if (keyword.equals("")) { return false; }
        List<DataSet> dataSetList = target.getDataSets();

        for (DataSet ds : dataSetList) {
            Object value = ds.getValue();
            DataSetInfo info = ds.getInfo();

            //2:25 is the record# and dataSet# for keywords
            if (info.toString().equals("2:25") && value.equals(keyword)) {
                Toast.makeText(context, "Tag already exists", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        target.add(IIM.KEYWORDS, keyword);
        Log.v("IIMUtility","target is now " + target.toString());
        return true;
    }

    public static boolean addKeyword(Context context, File target, String keyword)
            throws InvalidDataSetException, IOException {
        IIMFile iimFile = null;
        try {
            iimFile = getIIM(target);
        } catch (IIMNotFoundException e) {
            iimFile = new IIMFile();
        }
        catch (IOException e) {
            if (e.getMessage().contains("invalid magic number") ) {
                Toast.makeText(context,"Can only tag JPEG files",Toast.LENGTH_SHORT).show();
            }
            else {
                throw e;
            }
        }
        if (addKeyword(context, iimFile, keyword)) {
            insertIIM(context, iimFile, target);
            return true;
        }
        return false;
    }

    public static boolean addKeyword(Context context, Set<File> targets, String keyword)
            throws InvalidDataSetException, IOException {
        boolean allKeywordsAdded = true;
        for (File file : targets) {
            allKeywordsAdded = allKeywordsAdded && addKeyword(context, file, keyword);
        }
        return allKeywordsAdded;
    }

    public static boolean removeKeyword(IIMFile target, String keyword)
            throws InvalidDataSetException, SerializationException {
        if (keyword == null) {
            return false;
        }
        List<DataSet> dataSetList = target.getDataSets();

        boolean result = false;
        for (Iterator<DataSet> i = dataSetList.iterator(); i.hasNext();) {
            DataSet ds = i.next();
            Object value = ds.getValue();
            DataSetInfo info = ds.getInfo();
            if (info.toString().equals("2:25") && value.equals(keyword)) {
                i.remove();
                result = true;
            }
        }

        Log.v("IIMUtility","target is now " + target.toString());
        return result;
    }

    public static void removeKeyword(Context context, File target, String keyword)
            throws InvalidDataSetException, IOException {
        IIMFile iimFile;
        try {
            iimFile = getIIM(target);
        } catch (IIMNotFoundException e) {
            iimFile = new IIMFile();
        }
        if (removeKeyword(iimFile, keyword) ) {
            insertIIM(context, iimFile, target);
        }
    }

    public static void removeKeyword(Context context, Set<File> targets, String keyword)
            throws InvalidDataSetException, IOException {
        for (File file : targets) {
            removeKeyword(context, file, keyword);
        }
    }

    public static void removeIIM(File target)
            throws IOException {
        FileOutputStream fileOut = null;
        FileInputStream fileIn = null;
        File tempFile = File.createTempFile("prefix", "suffix");
        try {
            fileOut = new FileOutputStream(tempFile);
            fileIn = new FileInputStream(target);
            JPEGUtil.removeIIMFromJPEG(fileOut, fileIn);
            copyFile(tempFile,target);
        }
        finally {
            if (fileIn != null) { fileIn.close(); }
            if (fileOut != null) { fileOut.close(); }
            tempFile.deleteOnExit();
        }
    }

    /*
        Copies IIM from one jpeg file to another file
     */
    public static void copyIIM(Context context, File src, File dest) throws Exception {
        IIMFile iimFile = getIIM(src);
        insertIIM(context, iimFile, dest);
    }

    /*
        Prints all iptc data in jpeg file, if it exists
     */
    public static void printIIM(Context context, File src) throws java.io.IOException, InvalidDataSetException {

        try {
            IIMFile iimFile = getIIM(src);

            for (DataSet ds : iimFile.getDataSets()) {
                Object value = ds.getValue();
                DataSetInfo info = ds.getInfo();
                Log.v("IIMUtility", info.toString() + " " + info.getName() + ": " + value);
            }

        } catch (IIMNotFoundException e) {
            //IIMFile iimFile = new IIMFile();
            //insertIIM(context, iimFile,src);
        }

        /*Toast.makeText(getActivity(), "Finished", Toast.LENGTH_SHORT)
                .show(); */
    }

    public static void printIIM(Context context, Set<File> src) throws java.io.IOException, InvalidDataSetException {
        for (File file : src) {
            printIIM(context, file);
        }
    }

    //TODO: combine with printIIM (?)

    public static void printKeywords(Set<File> src) throws java.io.IOException, InvalidDataSetException {

        for (File file : src) {
            try {
                getKeywords(file);
            } catch (IIMNotFoundException e) {
                Log.v("IIMUtility", file.toString() + " has no keywords.");
                Log.e("IIMUtility", "Stack trace: ", e);
                //throw e;
            }
        }
    }

    public static ArrayList<String> getKeywords(File src) throws java.io.IOException,
            InvalidDataSetException, IIMNotFoundException {

        ArrayList<String> ret = new ArrayList<>();

        Log.v("IIMUtility", "Reading " + src.toString());
        IIMFile iimFile = getIIM(src);
        boolean hasKeywords = false;
        List<DataSet> dataSetList = iimFile.getDataSets();

        for (DataSet ds : dataSetList) {
            Object value = ds.getValue();
            DataSetInfo info = ds.getInfo();
            if (info.toString().equals("2:25")) //2:25 is the record# and dataSet# for keywords
            {
                ret.add((String) value);
                //Log.v("IIMUtility", (String) value);
                hasKeywords = true;
            }
        }
        if (!hasKeywords) {
            Log.v("IIMUtility", src.toString() + " has no keywords.");
        }

        return ret;
    }

    private static String getDateString() {
        Calendar calendar = Calendar.getInstance();
        String month = String.valueOf(calendar.get(Calendar.MONTH));
        if (month.length() == 1) { month = "0"+month; }
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (day.length() == 1) { day = "0"+month; }
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        return year+month+day;
    }

    public static void addMandatoryData(Context context, File file) throws
            java.io.IOException, InvalidDataSetException
    {
        IIMFile iimFile;
        try {
            iimFile = getIIM(file);
        } catch (IIMNotFoundException e) {
            iimFile = new IIMFile();
        }
        addMandatoryData(iimFile);
        insertIIM(context, iimFile, file);

    }

    //adds mandatory iim data for compliance with the standard
    //removes old data first
    //date should be in the format CCYYMMDD
    public static void addMandatoryData(IIMFile iimFile) throws
            SerializationException, InvalidDataSetException{


        String date = getDateString();
        List<DataSet> dataSetList = iimFile.getDataSets();
        int envelope = 0;
        String oldDate = "";
        for (DataSet ds : dataSetList) {
            String infoId = ds.getInfo().toString();
            switch(infoId) {
                case "1:40":
                    envelope = (int) ds.getValue();
                    dataSetList.remove(ds);
                    break;

                case "1:70":
                    oldDate = (String) ds.getValue();
                case "1:00":
                case "1:20":
                case "1:22":
                case "1:30":
                    dataSetList.remove(ds);
            }
        }

            //all values should be ascii
            iimFile.add(IIM.MODEL_VERSION, 4); //2 octets
            iimFile.add(IIM.FILE_FORMAT, 11); // 2 octets
            iimFile.add(IIM.FILE_FORMAT_VERSION, 1); //2 octets
            iimFile.add(IIM.DATE_SENT, String.valueOf(date)); //8 octets
            iimFile.add(IIM.SERVICE_IDENTIFIER, "Pantoscope"); //10 octets
            iimFile.add(IIM.ENVELOPE_NUMBER, oldDate.equals(date) ? envelope : 0); // 8 octets

    }

}
