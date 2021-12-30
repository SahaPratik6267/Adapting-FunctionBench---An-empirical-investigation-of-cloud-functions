package AWS.cpu_Memory.image_processing;
import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ops {



    public void flipImage(String File, String Filename){

        ArrayList pathlist= new ArrayList<String>();
        String path;
        //Flip the image x axis


        path= "D:\\Images\\flipping_left2right"+Filename+".jpg";
        //Loading the OpenCV core library
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        //Reading the Image from the file and storing it in to a Matrix object
        Mat src = Imgcodecs.imread(File);
        //Creating an empty matrix to store the result
        Mat dst = new Mat();
        //Changing the orientation of an image
        Core.flip(src, dst, 1);
        //Writing the image

        Imgcodecs.imwrite(path, dst);
        pathlist.add(path);

        //Flip image Y Axis

        path= "D:\\Images\\flipping_top2bottom"+Filename+".jpg";
        Core.flip(src, dst, 0);
        Imgcodecs.imwrite(path, dst);
        pathlist.add(path);
        //testing push with inteliJ

    }


}
