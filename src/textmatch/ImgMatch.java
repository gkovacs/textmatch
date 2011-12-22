package textmatch;

import java.awt.Rectangle;

import org.sikuli.script.*;

public class ImgMatch extends Rectangle {

    private String imgname;
    private String text;
    
    //int x,y,w,h;
    
    public ImgMatch(Match m, String imgname) {
        //super(m, m.getScreen());
        this.x = m.x;
        this.y = m.y;
        this.width = m.w;
        this.height = m.h;
        this.imgname = imgname;
        this.text = m.text();
    }
    
    public ImgMatch(int x, int y, int w, int h, String text, String imgname) {
        //super(m, m.getScreen());
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.imgname = imgname;
        this.text = text;
    }
    
    public String getImgName() {
        return imgname;
    }
    
    //@Override
    public String text() {
        return text;
    }
    
    @Override
    public String toString() {
        return text;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof ImgMatch) {
            ImgMatch o = (ImgMatch)other;
            return (this.text().equals(o.text()));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.text().hashCode();
    }

}
