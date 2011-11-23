package textmatch;

import java.util.List;

class MatchResults {
    final List<ImgMatch> match;
    final double ratio;
    final String templateMatchText;
    final IntPair matchIdxs;
    
    MatchResults(List<ImgMatch> match, double ratio, String templateMatchText, IntPair matchIdxs) {
        this.match = match;
        this.ratio = ratio;
        this.templateMatchText = templateMatchText;
        this.matchIdxs = matchIdxs;
    }
}