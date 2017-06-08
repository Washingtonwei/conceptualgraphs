//
//  WordnetManager.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Jun 11 2003.
//
package charger.gloss.wn;

import charger.gloss.GenericTypeDescriptor;
import charger.gloss.AbstractTypeDescriptor;
import charger.obj.*;
import charger.util.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
/* 
 $Header$ 
 */
/*
 CharGer - Conceptual Graph Editor
 Copyright reserved 1998-2014 by Harry S. Delugach
        
 This package is free software; you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as
 published by the Free Software Foundation; either version 2.1 of the
 License, or (at your option) any later version. This package is 
 distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 details. You should have received a copy of the GNU Lesser General Public
 License along with this package; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/**
 * Takes care of WordNet access when it is needed.
 */
public class WordnetManager {

    private static boolean _JWNLinitialized = false;
    public static String wordnetDictionaryFilename =
            charger.Global.Prefs.getProperty("wordnetDictionaryFilename", "/dev/null");		// make sure it's working
    public static net.didion.jwnl.dictionary.Dictionary dict = null;
    private static boolean initializationFailed = false;
    private Transcript transcript = null;
    private static double activeVersion = 0.0;        // should be 1.7, or 2.0 or something like that

    private WordnetManager(Transcript t) {
        transcript = t;
    }

    private WordnetManager() {
        transcript = new Transcript();
    }

    /**
     * Tests whether the wordnet system is available. If wordnet isn't
     * available, then many features will be of limited use, but they should use
     * this test so that they needn't crash.
     *
     * @return <code>true</code> if Wordnet is initialized and
     * available; <code>false</code> otherwise. Wordnet is only initialized once
     * per session, so if its availability changes during a session, nothing
     * will change. If Wordnet becomes unavailable during a session where it was
     * previously available, unpredictable (but probably bad) things will
     * happen.
     */
    public static boolean isWordnetAvailable() {
        if (!_JWNLinitialized) {
            getInstance();
        }
        return !initializationFailed;
    }

    /**
     * Identify the Wordnet version for the dictionary that is currently being
     * used.
     *
     * @return a Wordnet version (e.g., 1.7 or 2.0); if zero, then no dictionary
     * has yet been initialized.
     */
    public static double getActiveVersion() {
        return activeVersion;
    }

    /**
     * Create a new instance of a wordnet manager. If WordNet has not already
     * been made accessible, then initialize WordNet for access.
     *
     * @param t A transcript to be used for any interactive knowledge
     * acquisition.
     * @return a wordnet manager suitable for framing.
     */
    public static WordnetManager getInstance(Transcript t) {
        if (!_JWNLinitialized) {
            _JWNLinitialized = true;
            //initializeWordnet( wordnetDictionaryFilename );  // NEED to replace with parameter once it works
            initializeWordnet();
        }
        return new WordnetManager(t);
    }

    /**
     * Create a new instance of a wordnet manager. If WordNet has not already
     * been made accessible, then initialize WordNet for access. A default (but
     * currently useless) transcript will be instantiated too.
     *
     * @return a wordnet manager suitable for framing.
     */
    public static WordnetManager getInstance() {
        return getInstance(new Transcript());
    }

    private static void initializeWordnet() {
        try {
//            String propsFile = "CharGerWNPrefsOther.xml";
//            String os = System.getProperty("os.name");
//            if (os.startsWith("Mac")) {
//                propsFile = "CharGerWNPrefsMac.xml";
//            } else if (os.startsWith("Windows")) {
//                propsFile = "CharGerWNPrefsWindows.xml";
//            } else if (os.startsWith("Linux")) {
//                propsFile = "CharGerWNPrefsLinux.xml";
//            }
               
            charger.Global.info("before making file input stream from " + WordnetManager.wordnetDictionaryFilename);
         
            String props = file_properties_xml;
            ByteArrayInputStream fis = new ByteArrayInputStream( props.getBytes(Charset.forName("UTF-8")) );


                    // 2014 - time to update this to a string and not worry about where the file might reside
            
//            String fullname = "/config/" + propsFile;
//            Global.consoleMsg( "Load Wordnet prefs: looking for resource at " + fullname);
//            BufferedInputStream fis = (BufferedInputStream)Global.class.getResourceAsStream( fullname );
//            FileInputStream fis = null;
//        
//            try {
//                fis = new FileInputStream( "/Users/hsd/file_properties.xml");
////            if (fis == null) {
////                charger.Global.warning("File not found : " + fullname);
////                charger.Global.warning("Continuing without Wordnet available");
////            }
//            } catch ( FileNotFoundException ex ) {
//                Logger.getLogger( WordnetManager.class.getName() ).log( Level.SEVERE, null, ex );
//            }


            // this call results in JWNL exception:
            //  Unable to install net.didion.jwnl.dictionary.FileBackedDictionary
            // on the PC
            charger.Global.info("before initialize() call");
                        // This initialization call was failing because Wordnet's dict files weren't properly named.
                        // in the .../dict folder, files names index.xxx need to be renamed xxx.exc
            net.didion.jwnl.JWNL.initialize( fis );
            charger.Global.info("after initialize() call");
            dict = net.didion.jwnl.dictionary.Dictionary.getInstance();
            charger.Global.consoleMsg("Wordnet initialized. Publisher: " + JWNL.getVersion().getPublisher()
                    + "; version " + JWNL.getVersion().getNumber());
            activeVersion = JWNL.getVersion().getNumber();
            initializationFailed = false;
        } catch (net.didion.jwnl.JWNLException je) {
            //JOptionPane.showMessageDialog( null, "JWNL Exception: " + je.getMessage() ); 
            charger.Global.error("JWNL Exception at initialization: " + je.getMessage());
            je.printStackTrace();
            initializationFailed = true;
        }
        //charger.Global.info( "end of initializewordnet with initialization failed = " +
        //		initializationFailed );
    }

    /*private static void initializeWordnet( String dictionaryFile )
     {
     String propertiesText = 
     "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
     "<jwnl_properties language=\"en\">" + 
     "<version publisher=\"Princeton\" number=\"1.7\" language=\"en\"/>" +
     "<dictionary class=\"net.didion.jwnl.dictionary.FileBackedDictionary\">" +
     "<param name=\"morphological_processor\" value=\"net.didion.jwnl.dictionary.DefaultMorphologicalProcessor\"/>" +
     "<param name=\"file_manager\" value=\"net.didion.jwnl.dictionary.file_manager.FileManagerImpl\">" +
     "<param name=\"file_type\" value=\"net.didion.jwnl.princeton.file.PrincetonRandomAccessDictionaryFile\"/>" +
     "<param name=\"dictionary_path\" value=\"" + dictionaryFile + "\"/>" +
     "</param>" +
     "</dictionary>" +
     "<dictionary_element_factory class=\"net.didion.jwnl.princeton.data.PrincetonWN17DictionaryElementFactory\"/>" +
     "<resource class=\"PrincetonResource\"/>" +
     "</jwnl_properties>";
     try {
     charger.Global.info( "before making file input stream" );
     StringReader reader = new StringReader( propertiesText ); 
     BufferedReader breader = new BufferedReader( reader );
     BufferedInputStream fis = null; //new BufferedInputStream( reader );
			
     StringBufferInputStream sbis = new StringBufferInputStream( propertiesText ); 		// *deprecated
     // using a deprecated class because JWNL expects a stream from a string
     // this call results in JWNL exception:
     //  Unable to install net.didion.jwnl.dictionary.FileBackedDictionary
     // on the PC
     charger.Global.info( "before initialize() call" );
     net.didion.jwnl.JWNL.initialize( sbis );
     charger.Global.info( "after initialize() call" );
     dict = net.didion.jwnl.dictionary.Dictionary.getInstance();
     initializationFailed = false;
     }
     catch ( net.didion.jwnl.JWNLException je ) 
     { 
     //JOptionPane.showMessageDialog( null, "JWNL Exception: " + je.getMessage() ); 
     charger.Hub.error( "JWNL Exception at initialization: " + je.getMessage() ); 
     initializationFailed = true;
     }
     //charger.Global.info( "end of initializewordnet with initialization failed = " +
     //		initializationFailed );
     }
     */
    /**
     * Returns all senses, regardless of their part of speech.
     *
     * @param word The word taken directly from some text
     * @return the list of all synsets for that word.
     */
    public static Synset[] collectAllSenses(String word) {
        ArrayList senses = new ArrayList();
        if (!isWordnetAvailable()) {
            return new Synset[0];
        }
        try {
            IndexWordSet iwordset = dict.lookupAllIndexWords(word);
            IndexWord iwords[] = iwordset.getIndexWordArray();
            if (iwords.length > 0) {
                for (int iwnum = 0; iwnum < iwords.length; iwnum++) {
                    // process an index word
                    IndexWord iw = iwords[ iwnum];
                    Synset sensesForOne[] = iw.getSenses();
                    craft.Craft.say("processing indexword " + iwnum + " with lemma " + iw.getLemma() + "; "
                            + sensesForOne.length + " senses.");
                    for (int k = 0; k < sensesForOne.length; k++) {
                        Synset sense = sensesForOne[ k];
                        senses.add(sense);
                    }
                }
            }
        } catch (net.didion.jwnl.JWNLException je) {
            JOptionPane.showMessageDialog(null, "JWNL Exception: " + je.getMessage());
        }

        return (Synset[]) senses.toArray(new Synset[0]);
    }

    /**
     * Interactively query a user to choose a synset for a given word. Wordnet
     * is the first one tried, so it gets priority if it contains a sense for
     * the word. If Wordnet know nothing about the word, but the word contains
     * underscores in it, or has an alternation of upper and lower case, then
     * CRAFT will try to guess the constituent words.
     *
     * @param word The word for whom a synset is to be chosen
     * @param phrase A phrase in which the word appears, to aid the user
     * @param initialOption the initial synset to select
     * @param tryGeneric whether to query a generic "free-form" sense from the
     * user if Wordnet fails.
     * @return the descriptor corresponding to the definition chosen by the
     * user; <code>null</code> if the user cancelled, or if the word wasn't
     * found.
     * @see WordnetManager#chooseGenericSense
     * @see WNUtil#guessWordsFromPhrase
     */
    public AbstractTypeDescriptor queryForSense(
            String word, String phrase, AbstractTypeDescriptor initialOption, boolean tryGeneric) {
        if (word == null || word.equals("")) {
            return null;
        }
        if (charger.gloss.Preposition.isPreposition(word)) {
            // choose a relation for the preposition
            GenericTypeDescriptor prep = new GenericTypeDescriptor(word, "preposition", "related to");
            return prep;
        } else if (initialOption == null || initialOption instanceof WordnetTypeDescriptor) {
            craft.SenseQueryDialog dialog = new craft.SenseQueryDialog(
                    new Frame(), transcript, word, phrase, initialOption, tryGeneric);
            AbstractTypeDescriptor descr = dialog.getTypeDescriptor();
            if (descr == null) {
                return null;
            }

            if (descr instanceof WordnetTypeDescriptor) {
                WordnetTypeDescriptor wdescr = (WordnetTypeDescriptor) descr;
                Synset answer = wdescr.getSynset();
                //wd.setDefinition( wd.getSynset().getGloss() );
                //wd.setLabel( word );
                // a temporary setup to learn some things
                try {
                    Pointer[] pointers = answer.getPointers(PointerType.HYPERNYM);
                    for (int p = 0; p < pointers.length; p++) {
                        craft.Craft.say("hypernym " + p + " is " + pointers[ p].getTarget().toString());
                    }
                    //craft.Craft.say( "hypernym tree is:" );
                    //PointerUtils.getHypernymTree( answer ).print();
                } catch (JWNLException je) {
                    charger.Global.error("Wordnet exception: " + je.getMessage());
                }
                return wdescr;
            } else if (descr instanceof GenericTypeDescriptor) {
                // if we didn't find a wordnet descriptor
                GenericTypeDescriptor gdescr = (GenericTypeDescriptor) descr;
                return gdescr;
            }
        } else if (initialOption instanceof GenericTypeDescriptor) {
            craft.SenseQueryDialog dialog = new craft.SenseQueryDialog(
                    null, transcript, word, phrase, initialOption, true);
            return dialog.getTypeDescriptor();
            //TypeDescriptor gdescr = dialog.getTypeDescriptor();
            //return (GenericTypeDescriptor)gdescr;
        }
        return null;
    }

    /**
     * Invokes queryForSense on a word in a phrase. Assumes a null
     * WordnetTypeDescriptor for its default. Note that wordnet descriptor is
     * the default type here, so that it will always look for a wordnet
     * descriptor first.
     *
     * @see #queryForSense
     */
    public AbstractTypeDescriptor queryForSense(String word, String phrase, boolean tryGeneric) {
        return queryForSense(word, phrase, (WordnetTypeDescriptor) null, tryGeneric);
    }

    /**
     * Invokes queryForSense on a word, with no phrase.
     */
    public AbstractTypeDescriptor queryForSense(String word, AbstractTypeDescriptor initialOption, boolean tryGeneric) {
        return queryForSense(word, "", initialOption, tryGeneric);
    }

    /**
     * Invokes queryForSense on a word, with no phrase and no default.
     */
    public AbstractTypeDescriptor queryForSense(String word, boolean tryGeneric) {
        return queryForSense(word, "", (WordnetTypeDescriptor) null, tryGeneric);
    }

    /**
     * `	Removes trailing example sentences from a gloss entry and adds a
     * part-of-speech label to the front. This makes selections shorter and
     * somewhat easier.
     *
     * @param s Wordnet gloss entry
     * @param length the maximum length of the resulting string; if 0 (zero)
     * then don't truncate
     * @param includePOS whether to include a string denoting the part of speech
     * @param includeExamples whether to include explanatory or examples that
     * usually appear at the end of a glossary entry
     * @return a possibly shorter entry
     */
    public String getTrimmedGloss(Synset s, int length, boolean includePOS, boolean includeExamples) {
        String gloss = "";
        if (WordnetManager.isWordnetAvailable()) {
            gloss = s.getGloss();
        }
        if (includePOS) {
            gloss = s.getPOS().getLabel() + " -   " + gloss;
        }

        int maxlength = gloss.length();
        if (!includeExamples) {
            if (gloss.indexOf(";") != -1) {
                maxlength = gloss.indexOf(";");
            } else if (gloss.indexOf(":") != -1) {
                maxlength = gloss.indexOf(":");
            }
        }
        if (length != 0) {
            maxlength = Math.min(maxlength, length);
        }
        return gloss.substring(0, maxlength);
    }

    /**
     * Determines whether the word is in any ontology at all.
     *
     * @param word case insensitive
     * @return whether the word is in any ontology, no further indication.
     */
    public boolean wordExists(String word) {
        if (!isWordnetAvailable()) {
            return false;
        }
        try {
            IndexWordSet iwordset = dict.lookupAllIndexWords(word);
            if (iwordset.size() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (net.didion.jwnl.JWNLException je) {
            JOptionPane.showMessageDialog(null, "JWNL Exception: " + je.getMessage());
        }
        return false;
    }

    /**
     * A wrapper for Dictionary.getSynsetAt. First makes sure that wordnet has
     * been initialized and catches exceptions.
     */
    public static Synset getSynset(POS pos, long offset) {
        Synset s = null;
        if (!_JWNLinitialized) {
            _JWNLinitialized = true;
            //initializeWordnet( wordnetDictionaryFilename );  // NEED to replace with parameter once it works
            initializeWordnet();
        }
        if (!isWordnetAvailable() || offset == 0) {
            return null;
        }

        try {
            s = dict.getSynsetAt(pos, offset);	// for some reason, exception here is not caught
        } catch (net.didion.jwnl.JWNLException je) //catch ( Exception je )
        {
            JOptionPane.showMessageDialog(null, "JWNL Exception: " + je.getMessage());
        }
        return s;
    }

    /**
     * Associates a descriptor with a particular object. If the object is a
     * concept/relation/actor/graph, the intention is for that descriptor to be
     * associated with the object's type. If the object is a type/relation
     * label, then the intention is that the descriptor is associated with the
     * type itself. QUESTION: how should type and relation labels be handled?
     * When I use an instance of a type, should I automatically associate all
     * instances with the type's associated synset?
     *
     * @param descr the descriptor to associate. If s is null, there is no
     * effect; use forgetDescriptor.
     * @param gn the non-null GNode to be associated.
     * @return true if a new non-null descriptor was successfully associated
     * with the GNode; false otherwise.
     * @see WordnetManager#forgetDescriptors
     */
    public boolean attachDescriptor(AbstractTypeDescriptor descr, GNode gn) {
        charger.Global.info("attach descriptor " + descr);
        AbstractTypeDescriptor old = gn.getTypeDescriptor();
        if (descr == null) {
            return false;
        }

        gn.setTypeDescriptor(descr);

        if (old == null) {
            return true;		// since the new one cannot be null!
        }
        if (!old.equals(gn.getTypeDescriptor())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Associates a set of descriptors with a particular object. If the object
     * is a concept/relation/actor/graph, the intention is for that descriptor
     * to be associated with the object's type. If the object is a type/relation
     * label, then the intention is that the descriptor is associated with the
     * type itself. QUESTION: how should type and relation labels be handled?
     * When I use an instance of a type, should I automatically associate all
     * instances with the type's associated synset?
     *
     * @param ds the descriptors to associate. If s is null or zero-length,
     * there is no effect; use forgetDescriptor.
     * @param gn the non-null GNode to be associated.
     * @return true if one or more new non-null descriptors were successfully
     * associated with the GNode; false otherwise.
     * @see WordnetManager#forgetDescriptors
     */
    public boolean attachDescriptor(AbstractTypeDescriptor[] ds, GNode gn) {
        if (ds == null || ds.length == 0) {
            return false;
        }
        // compare the old and new descriptors for equals
        AbstractTypeDescriptor[] olds = gn.getTypeDescriptors();
        gn.setTypeDescriptors(ds);
        if (olds.length != ds.length) {
            return true;
        }
        for (int k = 0; k < olds.length; k++) {
            if (!olds[ k].equals(ds[ k])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the descriptor to null for this GNode.
     *
     * @param gn The GNode whose descriptor is to be erased
     * @return true if the descriptor wasn't null to begin with (i.e., the node
     * changed); false otherwise.
     * @see WordnetManager#attachDescriptor
     */
    public boolean forgetDescriptors(GNode gn) {
        if (gn.getTypeDescriptor() == null) {
            return false;
        }
        gn.clearDescriptors();
        return true;
    }

    /**
     * Return the POS constant (e.g., POS.NOUN, etc) corresponding to the label.
     *
     * @param label a label such as one returned by a call to POS.getLabel()
     */
    public static POS getPOSForLabel(String label) {
        if (label.equalsIgnoreCase("noun")) {
            return POS.NOUN;
        }
        if (label.equalsIgnoreCase("verb")) {
            return POS.VERB;
        }
        if (label.equalsIgnoreCase("adjective")) {
            return POS.ADJECTIVE;
        }
        if (label.equalsIgnoreCase("adverb")) {
            return POS.ADVERB;
        }
        return POS.NOUN;		// should never happen
    }

    public GenericTypeDescriptor chooseGenericSense(
            String word, String phrase, AbstractTypeDescriptor initialOption) {

        craft.SenseQueryDialog dialog = new craft.SenseQueryDialog(null, transcript, word, phrase, initialOption, true);
        AbstractTypeDescriptor d = dialog.getTypeDescriptor();
        if (d instanceof GenericTypeDescriptor) {
            return (GenericTypeDescriptor) d;
        } else {
            return null;
        }

    }
    
    public static String file_properties_xml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<jwnl_properties language=\"en\">\n" +
"	<version publisher=\"Princeton\" number=\"3.0\" language=\"en\"/>\n" +
"	<dictionary class=\"net.didion.jwnl.dictionary.FileBackedDictionary\">\n" +
"		<param name=\"morphological_processor\" value=\"net.didion.jwnl.dictionary.morph.DefaultMorphologicalProcessor\">\n" +
"			<param name=\"operations\">\n" +
"				<param value=\"net.didion.jwnl.dictionary.morph.LookupExceptionsOperation\"/>\n" +
"				<param value=\"net.didion.jwnl.dictionary.morph.DetachSuffixesOperation\">\n" +
"					<param name=\"noun\" value=\"|s=|ses=s|xes=x|zes=z|ches=ch|shes=sh|men=man|ies=y|\"/>\n" +
"					<param name=\"verb\" value=\"|s=|ies=y|es=e|es=|ed=e|ed=|ing=e|ing=|\"/>\n" +
"					<param name=\"adjective\" value=\"|er=|est=|er=e|est=e|\"/>\n" +
"                    <param name=\"operations\">\n" +
"                        <param value=\"net.didion.jwnl.dictionary.morph.LookupIndexWordOperation\"/>\n" +
"                        <param value=\"net.didion.jwnl.dictionary.morph.LookupExceptionsOperation\"/>\n" +
"                    </param>\n" +
"				</param>\n" +
"				<param value=\"net.didion.jwnl.dictionary.morph.TokenizerOperation\">\n" +
"					<param name=\"delimiters\">\n" +
"						<param value=\" \"/>\n" +
"						<param value=\"-\"/>\n" +
"					</param>\n" +
"					<param name=\"token_operations\">\n" +
"                        <param value=\"net.didion.jwnl.dictionary.morph.LookupIndexWordOperation\"/>\n" +
"						<param value=\"net.didion.jwnl.dictionary.morph.LookupExceptionsOperation\"/>\n" +
"						<param value=\"net.didion.jwnl.dictionary.morph.DetachSuffixesOperation\">\n" +
"							<param name=\"noun\" value=\"|s=|ses=s|xes=x|zes=z|ches=ch|shes=sh|men=man|ies=y|\"/>\n" +
"							<param name=\"verb\" value=\"|s=|ies=y|es=e|es=|ed=e|ed=|ing=e|ing=|\"/>\n" +
"							<param name=\"adjective\" value=\"|er=|est=|er=e|est=e|\"/>\n" +
"                            <param name=\"operations\">\n" +
"                                <param value=\"net.didion.jwnl.dictionary.morph.LookupIndexWordOperation\"/>\n" +
"                                <param value=\"net.didion.jwnl.dictionary.morph.LookupExceptionsOperation\"/>\n" +
"                            </param>\n" +
"						</param>\n" +
"					</param>\n" +
"				</param>\n" +
"			</param>\n" +
"		</param>\n" +
"		<param name=\"dictionary_element_factory\" value=\"net.didion.jwnl.princeton.data.PrincetonWN17FileDictionaryElementFactory\"/>\n" +
"		<param name=\"file_manager\" value=\"net.didion.jwnl.dictionary.file_manager.FileManagerImpl\">\n" +
"			<param name=\"file_type\" value=\"net.didion.jwnl.princeton.file.PrincetonRandomAccessDictionaryFile\"/>\n" +
"			<param name=\"dictionary_path\" value=\"" + WordnetManager.wordnetDictionaryFilename +   "\"/>\n" +
"		</param>\n" +
"	</dictionary>\n" +
"	<resource class=\"PrincetonResource\"/>\n" +
"</jwnl_properties>";
}
