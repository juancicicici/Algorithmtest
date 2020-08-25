package sensetime.senseme.humanaction.detect;

import java.util.ArrayList;
public class ArrayInfo {

    private String name;
    private int[] imgnum;
    private int[] is_labeled;
    private ArrayList<ArrayInfo.points> anno;
    private ArrayList<ArrayInfo.annorect> annorect;
    private ArrayList<ArrayInfo.getImgName> image;

    public ArrayInfo(){
        super();
    }

    public ArrayInfo(ArrayList<ArrayInfo.getImgName> image, ArrayList<ArrayInfo.annorect> annon,int[] imgnum,int[] is_labeled){
        this.image = image;
        this.annorect = annon;
        this.imgnum = imgnum;
        this.is_labeled = is_labeled;

    }

    public static class annopoints{
        private ArrayList<ArrayInfo.points> point;
        public annopoints(){
            super();
        }
        public annopoints(ArrayList<ArrayInfo.points>  mpoints){
            this.point = mpoints;
        }
    }

    public static class annorect{
        private int[] x1;
        private int[] y1;
        private int[] x2;
        private int[] y2;
        private int[] score;
        private int[] track_id;
        private String scale;
        private ArrayList<annopoints> annopoints;

        public annorect(){super();}

        public annorect(int[] x1, int[] y1, int[] x2, int[] y2, int[] score, String scale, int[] track_id, ArrayList<annopoints> mAnnopoints){
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.score = score;
            this.track_id = track_id;
            this.scale = scale;
            this.annopoints = mAnnopoints;

        }

    }

    public static class points{

        private int[] id;
        private float[] x;
        private float[] y;
        private int[] is_visible;

        public points(){
            super();
        }

        public points(int[] ID, float[] x, float[] y, int[] is_variable){
            super();

            this.id = ID;

            this.x = x;

            this.y = y;

            this.is_visible = is_variable;
        }

    }

    public static class getImgName{
        private String name;

        public getImgName(){super();}

        public getImgName(String name){
            this.name=name;
        }

    }

}
