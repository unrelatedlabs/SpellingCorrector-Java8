# BASIC SPELL CORRECTION IN JAVA



I've recently come across a popular article by [Peter Norvig](http://norvig.com/) on basic spelling correction at [How to Write a Spelling Corrector](http://norvig.com/spell-correct.html)


Peter Norvig implemented a clever algorithm in 21 of Python code and there are now implementations in many other languages.



I first noticed that the first Java implementation is embarrassingly large at 372 lines of code. And the second java implementation clocking in at 35 lines of code.



**So let's see how this looks like in Java 8.**

**23** lines of code. ( I assume we are counting non empty lines only and exclude the import statements )

I tried to use the modern concepts like Streams, and except loading the dictionary this looks like it's written in a functional style.

It's also thread-safe, which is nice. Obviously, this is Java and it's object oriented. You could have multiple instances using different dictionaries.



Also, I'm afraid to say, that being Java, this turned out being embarrassingly slow.



Tested on a 2015 MacBook Pro.



Original Python code

- cpython 2.7 **11s**

- [PyPy](http://pypy.org/) 2.7 **5s**


My Java 8 code

- Java 1.8.0_73-b02 **21s**



Likely due to slow string operations. This could be made much faster in Java, but would also require more code.



    /**
     * Java 8 Spelling Corrector.
     * Copyright 2016 Peter Kuhar.
     *
     * Open source code under MIT license: http://www.opensource.org/licenses/mit-license.php
     */

    import java.nio.file.*;
    import java.util.*;
    import java.util.stream.*;

    public class Spelling {
        private Map<String,Integer> dict = new HashMap<>();

        public Spelling(Path dictionaryFile) throws Exception{
            Stream.of(new String(Files.readAllBytes( dictionaryFile )).toLowerCase().replaceAll("[^a-z ]","").split(" ")).forEach( (word) ->{
                dict.compute( word, (k,v) -> v == null ? 1 : v + 1  );
            });
        }

        Stream<String> edits1(final String word){
            Stream<String> deletes    = IntStream.range(0, word.length())  .mapToObj((i) -> word.substring(0, i) + word.substring(i + 1));
            Stream<String> replaces   = IntStream.range(0, word.length())  .mapToObj((i)->i).flatMap( (i) -> "abcdefghijklmnopqrstuvwxyz".chars().mapToObj( (c) ->  word.substring(0,i) + (char)c + word.substring(i+1) )  );
            Stream<String> inserts    = IntStream.range(0, word.length()+1).mapToObj((i)->i).flatMap( (i) -> "abcdefghijklmnopqrstuvwxyz".chars().mapToObj( (c) ->  word.substring(0,i) + (char)c + word.substring(i) )  );
            Stream<String> transposes = IntStream.range(0, word.length()-1).mapToObj((i)-> word.substring(0,i) + word.substring(i+1,i+2) + word.charAt(i) + word.substring(i+2) );
            return Stream.of( deletes,replaces,inserts,transposes ).flatMap((x)->x);
        }

        Stream<String> known(Stream<String> words){
            return words.filter( (word) -> dict.containsKey(word) );
        }

        String correct(String word){
            Optional<String> e1 = known(edits1(word)).max( (a,b) -> dict.get(a) - dict.get(b) );
            Optional<String> e2 = known(edits1(word).map( (w2)->edits1(w2) ).flatMap((x)->x)).max( (a,b) -> dict.get(a) - dict.get(b) );
            return dict.containsKey(word) ? word : ( e1.isPresent() ? e1.get() : (e2.isPresent() ? e2.get() : word));
        }
    }