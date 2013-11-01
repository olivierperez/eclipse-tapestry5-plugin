package com.anjlab.eclipse.tapestry5;

import java.util.List;

import org.eclipse.core.resources.IFile;

import com.anjlab.eclipse.tapestry5.TapestryUtils.FileNameBuilder;

public class DefaultAssetResolver implements AssetResolver
{
    @Override
    public IFile resolve(final String path, IFile relativeTo) throws AssetException
    {
        try
        {
            List<IFile> files = TapestryUtils.findTapestryFiles(relativeTo, true, new FileNameBuilder()
            {
                @Override
                public String getFileName(String fileName, String fileExtension)
                {
                    int lastIndexOfDash = fileName.lastIndexOf('/');
                    
                    if (lastIndexOfDash <= 0)
                    {
                        return path;
                    }
                    
                    return fileName.substring(0, lastIndexOfDash) + '/' + path;
                }
            });
            
            if (!files.isEmpty())
            {
                return files.get(0);
            }
            
            throw createAssetException(path, null);
        }
        catch (Throwable t)
        {
            throw createAssetException(path, t);
        }
    }

    private AssetException createAssetException(final String path, Throwable cause)
    {
        return new AssetException("Couldn't resolve asset from path '" + path + "'", cause);
    }
}
