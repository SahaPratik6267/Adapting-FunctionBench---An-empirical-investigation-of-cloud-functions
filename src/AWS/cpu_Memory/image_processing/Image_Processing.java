package AWS.cpu_Memory.image_processing;



public class Image_Processing {
    public static void main(String args[]) {
        String file ="D:\\Images\\cat.jpg";
        ops ops= new ops();
        ops.flipImage(file,"cat");

    }

}
