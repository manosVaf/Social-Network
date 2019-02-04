package Client;

import java.io.*;
//implements the Java FileFilter interface.

public class AudioFiltering implements FileFilter
{
  private final String[] okFileExtensions = 
    new String[] {"mp3", "flac"};
 
  public boolean accept(File file)
  {
    for (String extension : okFileExtensions)
    {
      if (file.getName().toLowerCase().endsWith(extension))
      {
        return true;
      }
    }
    return false;
  }
}
