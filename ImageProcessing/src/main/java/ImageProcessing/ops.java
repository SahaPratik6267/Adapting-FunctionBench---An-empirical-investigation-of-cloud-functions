package ImageProcessing;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.plaf.IconUIResource;

import static java.lang.System.out;

public class ops {

    String TMP="/tmp/";



    public ArrayList flipImage(String File, String Filename){


        ArrayList pathlist= new ArrayList<String>();
        String path;
        //Flip the image x axis


        path= TMP+"flipping_left2right"+Filename;
        //Loading the OpenCV core library
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        //Reading the Image from the file and storing it in to a Matrix object
        Mat src = Imgcodecs.imread(File.toString());
        //Creating an empty matrix to store the result
        Mat dst = new Mat();
        //Changing the orientation of an image
        Core.flip(src, dst, 1);
        //Writing the image

        Imgcodecs.imwrite(path, dst);
        pathlist.add(path);

        //Flip image Y Axis

        path= TMP+"flipping_top2bottom"+Filename;
        Core.flip(src, dst, 0);
        Imgcodecs.imwrite(path, dst);
        pathlist.add(path);

        return pathlist;


    }

    public ArrayList rotateImage(String File, String Filename){
        ArrayList pathlist= new ArrayList<String>();
        String path;
        //Rotate the image 90 degree


        path= TMP+"rotate90"+Filename;
        //Loading the OpenCV core library
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        //Reading the Image from the file and storing it in to a Matrix object
        Mat src = Imgcodecs.imread(File);
        //Creating an empty matrix to store the result
        Mat dst = new Mat();
        //Changing the orientation of an image
        Core.rotate(src, dst, Core.ROTATE_90_CLOCKWISE);

        //Writing the image

        Imgcodecs.imwrite(path, dst);
        pathlist.add(path);



        path= TMP+"rotate180"+Filename;


        Core.rotate(src, dst, Core.ROTATE_180);
        //Writing the image

        Imgcodecs.imwrite(path, dst);
        pathlist.add(path);


        path= TMP+"rotate270"+Filename;

        Core.rotate(src, dst, Core.ROTATE_90_COUNTERCLOCKWISE);


        //Writing the image

        Imgcodecs.imwrite(path, dst);
        pathlist.add(path);

        return pathlist;

    }

    public ArrayList filter(String File, String Filename){
        ArrayList pathlist= new ArrayList<String>();
        String path;

        path= TMP+"blurred"+Filename;
        //Loading the OpenCV core library
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        //Reading the Image from the file and storing it in to a Matrix object
        Mat src = Imgcodecs.imread(File);
        //Creating an empty matrix to store the result
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        for (int i = 1; i < 10; i = i + 2) {
            Imgproc.blur(src, dst, new Size(i, i),
                    new Point(-1, -1));
        }
        Imgcodecs.imwrite(path, dst);
        pathlist.add(path);


        path= TMP+"sharpened"+Filename;

        Imgproc.GaussianBlur(src, dst, new Size(0,0), 10);
        Core.addWeighted(src, 1.5, dst, -0.5, 0, dst);
        Imgcodecs.imwrite(path, dst);
        pathlist.add(path);




        path= TMP+"contour"+Filename;

        //Converting the source image to binary
        Mat gray = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
        Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY_INV);
        //Finding Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchey = new Mat();
        Imgproc.findContours(binary, contours, hierarchey, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);
        //Drawing the Contours
        Scalar color = new Scalar(0, 0, 255);
        Imgproc.drawContours(src, contours, -1, color, 2, Imgproc.LINE_8,
                hierarchey, 2, new Point() ) ;



        Imgcodecs.imwrite(path,src);
        pathlist.add(path);

        return pathlist;

    }

    public ArrayList grayscale(String File, String Filename){
        ArrayList pathlist= new ArrayList<String>();
        String path;


        path= TMP+"gray"+Filename;
        Mat src = Imgcodecs.imread(File);
        Mat gray = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(path,gray);
        pathlist.add(path);

        return pathlist;
    }

    public ArrayList resize(String File, String Filename){
        ArrayList pathlist= new ArrayList<String>();
        String path;

        path= TMP+"resized"+Filename;

        Mat src = Imgcodecs.imread(File);

        Mat resizeimage = new Mat();
        Size sz = new Size(100,100);
        Imgproc.resize( src, resizeimage, sz );

        Imgcodecs.imwrite(path,resizeimage);
        pathlist.add(path);

        return pathlist;
    }

}


