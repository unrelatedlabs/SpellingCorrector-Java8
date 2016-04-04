import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by peter on 4/3/16.
 */
public class SpellingTest {

    static  Spelling spellfixer;

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        spellfixer = new Spelling(Paths.get( ClassLoader.getSystemResource("big.txt").getPath()));;

        System.out.println( spellfixer.correct("spelaing") );

        HashMap<String, String> tests1 = loadTest("tests1.json");
        HashMap<String, String> tests2 = loadTest("tests2.json");

        test(tests1);
        test(tests2);
        System.out.println("Done in " + (System.currentTimeMillis() - start)/1000 + "s ");
    }

    private static void test(HashMap<String, String> tests) throws Exception {

        int ok = 0;
        int wrong = 0;
        for( String bad:tests.keySet()){
            String correct = spellfixer.correct(bad);
            boolean match = correct.equals( tests.get(bad));

            //System.out.println( ( match ? "  ":"+ " ) +   bad + " '" + correct + "'  '" + tests.get(bad) + "'" );

            if( match ){
                ok++;
            }else{
                wrong++;
            }
        }
        System.out.println("n " + tests.size() +  " Correct " + ok + " wrong " + wrong + " rate " + ((float)ok)/tests.size() );
    }

    static HashMap<String,String> loadTest(String name) throws IOException {
        HashMap<String,String> tests = new HashMap<>();

        for( String pair : new String(Files.readAllBytes( Paths.get( ClassLoader.getSystemResource(name).getPath())  )).split(",") ){
            pair = pair.replaceAll("\"","");
            String[] bad  = pair.split(":")[1].trim().split(" ");
            String good = pair.split(":")[0].trim();
            for( String b : bad){
                tests.put(b.trim(),good);
            }
        }
        return tests;
    }
}
