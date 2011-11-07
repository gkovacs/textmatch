package textmatch;

import java.util.List;

class MatchResults {
    final List<ImgMatch> match;
    final double ratio;
    final String templateMatchText;
    
    MatchResults(List<ImgMatch> match, double ratio, String templateMatchText) {
        this.match = match;
        this.ratio = ratio;
        this.templateMatchText = templateMatchText;
    }
}