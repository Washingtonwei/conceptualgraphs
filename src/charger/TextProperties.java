package charger;

import java.util.*;
import javax.swing.*;

/**
 * Stores the strings needed to localize CharGer. So far only English is
 * supported (volunteers welcome!).
 *
 * @see Global#strs
 */
public class TextProperties extends Properties {

    /**
     * Set up the properties for charger's natural language strings.
     *
     * @param language an ISO abbreviation for the language desired. So far,
     * only "en" is supported. Volunteers are welcome for other languages.
     */
    public TextProperties( String language ) {
        super();
        if ( language.equals( "en" ) ) {
            setupEN();
        } else {
            setupEN();
        }
    }

    /**
     * Overrides superclass's method so that we can flash a dialog if a string
     * lookup fails.
     *
     * @param key Logical name of the string
     * @return the actual localized string; if not found, that means someone
     * forgot to put the string in this lookup table, but the method returns the
     * value of <code>key</code> just to be nice.
     */
    public String getProperty( String key ) {
        //Global.info( "looking up property " + key );
        String prop = super.getProperty( key );
        if ( prop == null ) {
            JOptionPane.showMessageDialog( null,
                    "String property lookup failed. Can't find string for " + key,
                    "TextProperties failure", JOptionPane.ERROR_MESSAGE );
            return key;
        } else {
            return prop;
        }
    }

    private void setupEN() {
        // menu names
        super.put( "FileMenuLabel", "File" );
        super.put( "EditMenuLabel", "Edit" );
        super.put( "ViewMenuLabel", "View" );
        super.put( "ToolsMenuLabel", "Tools" );
        super.put( "ExamineMenuLabel", "Examine" );
        super.put( "OperationMenuLabel", "Operation" );
        super.put( "WindowMenuLabel", "Window" );
        super.put( "UML2CGsMenuLabel", "UML2CGs" );

        // primarily file menu items
        super.put( "NewWindowLabel", "New" );
        super.put( "OpenLabel", "Open..." );
        super.put( "OpenAllLabel", "Open All" );
        super.put( "SaveLabel", "Save" );
        super.put( "SaveAsLabel", "Save As..." );
        super.put( "PrintLabel", "Print..." );
        super.put( "CloseLabel", "Close" );
        super.put( "QuitLabel", "Quit" );
        super.put( "ExportCGIFLabel", "Save As CGIF..." );
        super.put( "ExportTestXMLLabel", "Save As Test XML..." ); //Added by HSD 11/2/12
        super.put( "ExportAsImageLabel", "Export To Image" );
        super.put( "ExportLabel", "Export..." );
        super.put( "ImportCGIFLabel", "Open CGIF..." );
        super.put( "PageSetupLabel", "Page Setup..." );
        super.put( "PreferencesLabel", "Preferences..." );
        super.put( "DisplayAsEnglishLabel", "Show English..." );
        super.put( "DisplayAsCGIFLabel", "Show CGIF..." );
        super.put( "DisplayAsXMLLabel", "Show XML..." );
        super.put( "DisplayMetricsLabel", "Show metrics..." );


        // MMAT support and opertions
        super.put( "MMATAnalysisLabel", "MM Team Models Analysis" );
        super.put( "RelationMatchingLabel", "Binary Relation Matching scores (current=master)" );
        super.put( "BestRelationMatchingLabel", "Best match on Binary Relation Matching" );


        // Knowledge base stuff, not really used anymore
        super.put( "SummarizeKnowledgeLabel", "Summarize everything..." );
        super.put( "MakeTypeHierarchyLabel", "Make type hierarchy" );

        super.put( "DisplayAsTextLabel", "Display As Text" );
        super.put( "SaveTextLabel", "Save Text..." );

        // primarily edit menu items
        super.put( "CutLabel", "Cut" );
        super.put( "CopyLabel", "Copy" );
        super.put( "PasteLabel", "Paste" );
        super.put( "CopyImageLabel", "Copy as Image");
        super.put( "DuplicateLabel", "Duplicate" );
        super.put( "UndoLabel", "Undo" );
        super.put( "RedoLabel", "Redo" );
        super.put( "ClearLabel", "Clear" );
        super.put( "SelectAllLabel", "Select All" );
        super.put( "MakeContextLabel", "Make Context" );
        super.put( "MakeCutLabel", "Make \"Cut\"" );
        super.put( "UnMakeContextLabel", "UnMake Context/\"Cut\"" );
        super.put( "AlignVLabel", "Align Vertically" );
        super.put( "AlignHLabel", "Align Horizontally" );
        super.put( "MinimizeLabel", "Shrink Selection" );
        super.put( "GraphModalityLabel", "Set Graph Modality" );
        super.put( "ChangeFontLabel", "Change Font..." );
        super.put( "ChangeColorLabel", "Change Color" );
        super.put( "ChangeTextColorLabel", "Text..." );
        super.put( "ChangeFillColorLabel", "Fill..." );
        super.put( "ChangeColorDefaultLabel", "To Current Defaults" );
        super.put( "ChangeColorFactoryLabel", "To Factory Defaults" );
        super.put( "ChangeColorBlackAndWhiteLabel", "To Black and White" );
        super.put( "ChangeColorGrayscaleLabel", "To Grayscale" );

        super.put( "TestingItemsMenuLabel", "Testing Items" );
        super.put( "SpringAlgorithmLabel", "Spring Algorithm" );
        super.put( "SugiyamaAlgorithmLabel", "Sugiyama Algorithm" );
        super.put( "SpringScaledAlgorithmLabel", "Spring Algorithm (Scaled)" );

        // primarily view menu items
        super.put( "ZoomInLabel", "Zoom In" );
        super.put( "ZoomOutLabel", "Zoom Out" );
        super.put( "ActualSizeLabel", "Actual Size" );
        super.put( "CurrentSizeLabel", "Current: " );
        super.put( "FindLabel", "Find..." );
        super.put( "FindAgainLabel", "Find Again" );
        super.put( "ShowGlossLabel", "Show glossary text" );
        super.put( "ShowHistory", "Show object history" );
        super.put( "ShowSubgraphLabel", "Show Subgraph" );

        // primarily operation menu items
        super.put( "AttachOntologyLabel", "Attach glossary text" );
        super.put( "DeleteOntologyLabel", "Delete glossary text" );

        // primarily window menu items
        super.put( "BackToHubCmdLabel", "CharGer Main Window" );
        super.put( "BackToCraftCmdLabel", "CRAFT Main Window" );

        // button labels
        super.put( "DontSaveLabel", "Discard" );
        super.put( "CancelLabel", "Cancel" );
        super.put( "SkipIgnoreLabel", "Skip/Ignore" );
        super.put( "BrowseLabel", "Set folder ..." );
        super.put( "ChooseWorkingDirectoryLabel", "Set all folders..." );

        // miscellaneous information messages

        // miscellaneous error messages
    }
}