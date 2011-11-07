package textmatch;

import org.sikuli.script.*;

public class ImgMatch extends Match {

    private String imgname;
    private String text;
    
    public ImgMatch(Match m, String imgname) {
        super(m, m.getScreen());
        this.x = m.x;
        this.y = m.y;
        this.w = m.w;
        this.h = m.h;
        this.imgname = imgname;
        this.text = m.text();
    }
    
    public String getImgName() {
        return imgname;
    }
    
    @Override
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
