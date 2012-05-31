package textmatch;

import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;

public class AccuracyReporter {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        POMsgSource msgsrc = new POMsgSource(new FileInputStream(args[0]));
        int matches = 0;
        int misses = 0;
        int falsepositives = 0;
        int noannotations = 0;
        int notinscreenshots = 0;
        List<String> msgidblocks = msgsrc.splitIntoMsgIdBlocks();
        for (String msgidblock : msgidblocks) {
            MsgAnnotation computerAnnotation = msgsrc.annotationFromMsgIdBlock(msgidblock);
            if (msgsrc.noMatchesForMsgIdBlock(msgidblock)) {
                if (computerAnnotation != null)
                    falsepositives++;
                notinscreenshots++;
                continue;
            }
            List<String> manualFiles = msgsrc.checkedAnnotationFileListFromMsgIdBlock(msgidblock);
            if (manualFiles.size() == 0) {
                noannotations++;
                continue;
            }
            String computerAnnotationFile = null;
            if (computerAnnotation != null) {
                computerAnnotationFile = computerAnnotation.filename;
                if (computerAnnotationFile.indexOf('/') != -1) {
                    computerAnnotationFile = computerAnnotationFile.substring(computerAnnotationFile.lastIndexOf('/')+1);
                }
            }
            if (computerAnnotationFile != null && manualFiles.contains(computerAnnotationFile)) {
                matches++;
            } else {
                misses++;
            }
        }
        System.out.println("total:" + msgidblocks.size());
        System.out.println("notinscreenshots:" + notinscreenshots);
        System.out.println("matches:" + matches);
        System.out.println("misses:" + misses);
        System.out.println("falsepositives:" + falsepositives);
        System.out.println("noannotations:" + noannotations);
    }

}
