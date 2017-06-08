/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mm;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 * Manages the files for the MM experiments.
 * Instantiated to use with one particular folder; if multiple folders are needed then
 * use multiple instantiations.
 * Performs various utility services such as deleting report files if a team is renamed, etc.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class MMFileManager {
    
    /** This is the "root" folder for the project. Group folders are nested within, etc. */
    public File folderToUse = null;
    
    /**
     * Used for storing lists of old and new file names (not including absolute paths, 
     * since the file manager object only operates on one path at a time.
     */
    protected HashMap<String, String> oldNewFileList = new HashMap<>();

    
    /**
     * Sets this file manager to operate on one specific folder.
     * @param folder The folder to use for any following operations.
     */
    public MMFileManager( File folder ) {
        folderToUse = folder.getAbsoluteFile();
    }
    
    /**
     * Sets this file manager to operate on one specific folder.
     * @param folder The folder to use for any following operations.
     */
    public MMFileManager( String folder ) {
        folderToUse = new File( folder );
    }
    
     public static String[] getMMATFileList( File folder, final MFileType type ) {
         if ( MMConst.useNewFolderStructure ) {
             return getMMATFileListNEW( folder, type );
         } else {
             return getMMATFileListOLD( folder, type );
         }
     }
    /**
     * Gather a list of files relevant to the MMAT.
     * @param folder Method searches only in this folder
     * @param type The type of files to be gathered. If null then gather all of them.
     * @return a list of files of any of the types relevant to the MMAT
     * @see MFileType
     */
     public static String[] getMMATFileListOLD( File folder, final MFileType type ) {
        String foundOnes[] = {};
        if ( folder != null ) {
            foundOnes = folder.list( new FilenameFilter() {
                public boolean accept( File f, String name ) {
                    if ( type == null || MFile.getType( name ) == type  ) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } );
        }
        if ( foundOnes != null && foundOnes.length > 0 ) {
            Arrays.sort( foundOnes );
        }
        return foundOnes;
    }
     
    /**
     * Gather a list of files relevant to the MMAT.
     * @param folder Method searches only in this folder
     * @param type The type of files to be gathered. If null then gather all of them.
     * @return a list of files of any of the types relevant to the MMAT
     * @see MFileType
     */
     public static String[] getMMATFileListNEW( File folder,  MFileType type ) {
        
        String foundOnes[] = (String[])findFiles( folder, type).toArray( new String[1]);
        if ( foundOnes != null && foundOnes.length > 0 ) {
            Arrays.sort( foundOnes );
        }
        return foundOnes;
    }
     
     /** Looks in the current folder and recursively looks in all nested folders for 
      * files of the given type 
      * @param outerFolder The folder to start with
      * @param type The type of file to look for; if null then get all non-folder files
      * @return the list of files, regardless of their nesting
      * */
     public static ArrayList<String> findFiles( File outerFolder, final MFileType type ) {
         ArrayList<String> matchingFiles = new ArrayList<String>();
         
         if ( outerFolder != null ) {
//                       Global.info(" at find files, looking at outer folder " + outerFolder.getAbsolutePath() );
                                    // add all files that match the filter
            String files[] = outerFolder.list( new FilenameFilter() {
                public boolean accept( File f, String name ) {
//                              Global.info("Testing whether to accept as model file " + new File(f,name).getAbsolutePath());
                   if ( type == null || MFile.getType( name ) == type ) {
                         return true;
                     } else {
                         return false;
                     }
                 }
             } );

            if ( files != null ) {
//                        Global.info( "folder " + outerFolder.getAbsolutePath() + " found " + files.length + " model files.");
                 for ( String s : files ) {
                     if ( s != null ) matchingFiles.add( s );
                 }
             }

                    // recursively call on all folders
            File folders[] = outerFolder.listFiles( new FilenameFilter() {
                public boolean accept( File f, String name ) {
//                            Global.info("Testing whether to accept as folder " + new File(f,name).getAbsolutePath());
                    File newone = new File( f, name );
                    if ( newone.isDirectory() && ! name.equals( MMConst.MMSavedFolderName ) ) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } );
            if ( folders != null ) {
                 for ( File f : folders ) {
//                      Global.info(" at find files, looking at nested folder " + f.getAbsolutePath() );
                     ArrayList<String> nestedFiles = findFiles( f, type );
                     for ( String file : nestedFiles) {
//                            Global.info( "folder " + f.getAbsolutePath() + " found " + nestedFiles.size() + " folders.");
                         if ( file != null ) matchingFiles.add( file );
                     }
            
                 }
             }
         } 
         
         return matchingFiles;
     }
    
     
          /** Looks in the current folder and recursively looks in all nested folders for 
      * files of the given type 
      * @param outerFolder The folder to start with
      * @param type The type of file to look for; if null then get all non-folder files
      * @return the list of files, regardless of their nesting
      * */
     public static ArrayList<String> getMMATSavedOnlyFileList( File outerFolder  ) {
         ArrayList<String> matchingFiles = new ArrayList<String>();
         
         if ( outerFolder != null ) {
             String[] files = null;
//                       Global.info(" at find files, looking at outer folder " + outerFolder.getAbsolutePath() );
                                    // add all files that match the filter
             if ( outerFolder.isDirectory() && outerFolder.getName().equals( MMConst.MMSavedFolderName )) {
             files = outerFolder.list( new FilenameFilter() {
                public boolean accept( File f, String name ) {
//                              Global.info("Testing whether to accept as model file " + new File(f,name).getAbsolutePath());
                   if (  MFile.getType( name ) == MFileType.MODEL ) {
                         return true;
                     } else {
                         return false;
                     }
                 }
             } );
             }

            if ( files != null ) {
//                        Global.info( "folder " + outerFolder.getAbsolutePath() + " found " + files.length + " model files.");
                 for ( String s : files ) {
                     if ( s != null ) matchingFiles.add( s );
                 }
             }

                    // recursively call on all folders
            File folders[] = outerFolder.listFiles( new FilenameFilter() {
                public boolean accept( File f, String name ) {
//                            Global.info("Testing whether to accept as folder " + new File(f,name).getAbsolutePath());
                    File newone = new File( f, name );
                    if ( newone.isDirectory() ) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } );
            if ( folders != null ) {
                 for ( File f : folders ) {
//                      Global.info(" at find files, looking at nested folder " + f.getAbsolutePath() );
                     ArrayList<String> nestedFiles = getMMATSavedOnlyFileList( f );
                     for ( String file : nestedFiles) {
//                            Global.info( "folder " + f.getAbsolutePath() + " found " + nestedFiles.size() + " folders.");
                         if ( file != null ) matchingFiles.add( file );
                     }
            
                 }
             }
         } 
         
         return matchingFiles;
     }

     /**
      * Deletes any folders that are completely empty (i.e., do not have any folder or MM-relevant files).
       If a folder contains a ".DS_Store" file (a MacOS-specific file) then it's deleted first.
     * with a dot "." then that counts as empty.
     *
     * @param outerFolder
     */
    public static void removeEmptyFolders( File outerFolder ) {
        if ( ! outerFolder.isDirectory() ) return;
        File folders[] = outerFolder.listFiles( new FilenameFilter() {
            public boolean accept( File f, String name ) {
//                            Global.info("Testing whether to accept as folder " + new File(f,name).getAbsolutePath());
                File newone = new File( f, name );
                if ( newone.isDirectory() /*&& ! name.equals( MMConst.MMSavedFolderName )*/ ) {
                    return true;
                } else {
                    return false;
                }
            }
        } );
        File files[] = outerFolder.listFiles(  ) ;
        if ( folders.length == 0 ) {
             try {
           for ( File otherFile : files ) {
                if ( otherFile.getName().equals(".DS_Store")) 
                    Files.delete( otherFile.toPath());
            }
                Files.delete( outerFolder.toPath() );
            } catch ( IOException ex ) {
                ex.getMessage();
            }
            
        } else {
            for ( File folder : folders ) {
                    removeEmptyFolders( folder );
            }
        }
    }
     

         /** Changes the name of a single  file in the manager's identified folder.
          * The file could be a synonym file or an html file.
     * Looks to see if the newname already exists and gives the user a chance to
     * bail out. If user continues, then renames the  submittedFile model.
     * This method can be considered the continuation of startupRenamer.
     * Display a summary dialog when it's done.
     * @param oldFilename a previous model name, including extension. Should already exist.
     * @param newFilename the new model name, including extension. If it already exists, then the user
     * is alerted and can abort if necessary.
     * @return true if anything changed; false otherwise.
     */
    public boolean renameOneFile( String oldFilename, String newFilename ) {
        // Check to see if the new name is the same as the old name

        if ( newFilename.equalsIgnoreCase( oldFilename ) ) {
            JOptionPane.showMessageDialog( null, "Name \"" + newFilename
                    + "\" is still the same. No files changed." );
            return false;
        }

        File oldFile;
        if ( MMConst.useNewFolderStructure ) {
            MFile oldMFile = new MFile( oldFilename );
            oldFile = new File( oldMFile.mfileAbsoluteFolder( folderToUse ), oldFilename );
        } else {
            oldFile = new File( folderToUse, oldFilename );
        }
        if ( !oldFile.exists() ) {
            JOptionPane.showMessageDialog( null, "Name \"" + oldFilename
                    + "\" doesn't exist in folder " + oldFile.getParentFile() + ". No files changed." );
            return false;
        }
        
        int result = JOptionPane.YES_OPTION;

        File newFile;
        // First find out whether a file for the new file name already exists 
        if ( MMConst.useNewFolderStructure ) {
            MFile newMFile = new MFile( newFilename );
            newFile = new File( newMFile.mfileAbsoluteFolder( folderToUse ), newFilename );
        } else {
            newFile = new File( folderToUse, newFilename );
        }
        if ( newFile.exists() ) {
            result = JOptionPane.showConfirmDialog( null, "Warning!\n\n"
                    + "File: \"" + newFilename + "\" \n"
                    + "already exists. "
                    + "Do you want to add to and/or replace it?" );
        }

        if ( result == JOptionPane.YES_OPTION ) {
            try {
                if ( MMConst.useNewFolderStructure ) {
                    File parent = newFile.getParentFile();
                    if ( ! parent.exists() ) {
                        boolean ok = parent.mkdirs();
                        if ( !ok ) {
                            return false;
                        }
                    }
                }
                Files.move( oldFile.toPath(), newFile.toPath() );
                // TODO: need to clean up the old folder(s) if empty.
                
                //            if ( MMConst.useNewFolderStructure ) {
                //                File parent = newFile.getParentFile();
                //                boolean ok = parent.mkdirs();
                //                if ( ok ) {
                //                    oldFile.renameTo( newFile );
                //                } else {
                ////                   JOptionPane.showMessageDialog( null, "Error!\n\n"
                ////                    + "File: \"" + oldFile.getPath() + "\" \n"
                ////                    + " could not be moved to "
                ////                    + "File: \"" + newFile.getPath() + "\"");
                //                   return false;
                //                }
                //
                //            } else {
                //            }
                //            }
            } catch ( IOException ex ) {
                JOptionPane.showMessageDialog( null, "Error!\n\n"
                        + "File: \"" + oldFile.getPath() + "\" \n"
                        + " could not be moved to "
                        + "File: \"" + newFile.getPath() + "\"\n"
                        + "Error is: " + ex.getMessage());
                return false;
            }
        }
        return true;
    }
    
    
    
    /**
     * Renames a team to a new name. 
     * The team remains in the same group.
     * Includes any synonym or report file too.
     * Does not check if new team name already exists, will add or overwrite files as appropriate.
     * @param group the group in which the team appears -- because the same team name may appear in more than one group.
     * @param oldTeamName
     * @param newTeamName
     * @return The number of files that were renamed
     */
    public int renameTeam( String group, String oldTeamName, String newTeamName ) {
        String[] files = getMMATFileList( folderToUse, null );
        oldNewFileList.clear();
        for ( String name : files ) {
            MFile file = new MFile( name );
            if ( file.getGroup().equalsIgnoreCase( group ) ) {
                if ( file.getTeam().equalsIgnoreCase( oldTeamName ) ) {
                    file.setTeam( newTeamName );
                    oldNewFileList.put( name, file.makeFilename() );
                }
            }
        }
        return renameFiles( );
    }
    
        /**
     * Renames a team to a new name. 
     * The team remains in the same group.
     * Includes any synonym or report file too.
     * @param oldGroup group to move team from
     * @param newGroup move the team to this group -- if there's already a team with that name, adds or 
     * overwrites as appropriate.
     * @param teamName the team to move
     * @return The number of files that were moved
     */
    public int moveTeam( String oldGroup, String newGroup, String teamName ) {
        String[] files = getMMATFileList( folderToUse, null );
        oldNewFileList.clear();
        for ( String name : files ) {
            MFile file = new MFile( name );
            if ( file.getGroup().equalsIgnoreCase( oldGroup ) ) {
                if ( file.getTeam().equalsIgnoreCase( teamName ) ) {
                    file.setGroup( newGroup );
                    oldNewFileList.put( name, file.makeFilename() );
                }
            }
        }
        return renameFiles( );
    }


    /**
     * Renames a list of files.
     * One issue is whether this method should check all files for existence before renaming any of them.
     * At present, simply ignores files that would be overwritten and leaves the originals.
     * 
     * @return the number of files actually renamed. In normal use, should equal oldNewFileList.size() but 
     * in case one or more files already exist, they will not be overwritten.
     * @see #oldNewFileList
     */
    public int renameFiles( ) {
        int filesRenamed = 0;
        for ( String oldfile : oldNewFileList.keySet() ) {
            String newFile = oldNewFileList.get(  oldfile );
            
            boolean successful = renameOneFile( oldfile, newFile );
            if ( successful ) {
                filesRenamed++;
            }
        }
        return filesRenamed;
    }
    
    /** Performs the move in the file hierarchy when using the newer folder structure.
     * @param oldFile The current file. This must have an absolute path, or else it
     * may not be found.
     * @param newFile The new file; must be absolute in order to find its place.
     * 
     *
     *
     */
    public boolean moveFile( File oldFile, File newFile ) {
        if ( !oldFile.isAbsolute()  || !newFile.isAbsolute() ) {
                   JOptionPane.showMessageDialog( null, "Error!\n\n"
                    + "File: \"" + oldFile.getPath() + "\" \n"
                    + "File: \"" + newFile.getPath() + "\" \n"
                    + "need to be have fully absolute paths." );

             return false;
        }
        newFile.mkdirs();
        return true;

    }

}
