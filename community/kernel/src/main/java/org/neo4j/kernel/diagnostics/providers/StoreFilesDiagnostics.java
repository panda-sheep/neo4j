/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.diagnostics.providers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.neo4j.helpers.Format;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.kernel.impl.store.StoreType;
import org.neo4j.logging.Logger;

import static java.util.stream.Collectors.toList;

public class StoreFilesDiagnostics extends NamedDiagnosticsProvider
{
    private static final String FORMAT_DATE_ISO = "yyyy-MM-dd'T'HH:mm:ssZ";
    private final DatabaseLayout databaseLayout;
    private final SimpleDateFormat dateFormat;

    public StoreFilesDiagnostics( DatabaseLayout databaseLayout )
    {
        super( "Store files" );
        this.databaseLayout = databaseLayout;
        dateFormat = new SimpleDateFormat( FORMAT_DATE_ISO );
        dateFormat.setTimeZone( TimeZone.getDefault() );
    }

    @Override
    public void dump( Logger logger )
    {
        logger.log( getDiskSpace( databaseLayout ) );
        logger.log( "Storage files: (filename : modification date - size)" );
        MappedFileCounter mappedCounter = new MappedFileCounter( databaseLayout );
        long totalSize = logStoreFiles( logger, "  ", databaseLayout.databaseDirectory(), mappedCounter );
        logger.log( "Storage summary: " );
        logger.log( "  Total size of store: " + Format.bytes( totalSize ) );
        logger.log( "  Total size of mapped files: " + Format.bytes( mappedCounter.getSize() ) );
    }

    private long logStoreFiles( Logger logger, String prefix, File dir, MappedFileCounter mappedCounter )
    {
        if ( !dir.isDirectory() )
        {
            return 0;
        }
        File[] files = dir.listFiles();
        if ( files == null )
        {
            logger.log( prefix + "<INACCESSIBLE>" );
            return 0;
        }
        long total = 0;

        // Sort by name
        List<File> fileList = Arrays.asList( files );
        fileList.sort( Comparator.comparing( File::getName ) );

        for ( File file : fileList )
        {
            long size;
            String filename = file.getName();
            if ( file.isDirectory() )
            {
                logger.log( prefix + filename + ":" );
                size = logStoreFiles( logger, prefix + "  ", file, mappedCounter );
                filename = "- Total";
            }
            else
            {
                size = file.length();
                mappedCounter.addFile( file );
            }

            String fileModificationDate = getFileModificationDate( file );
            String bytes = Format.bytes( size );
            String fileInformation = String.format( "%s%s: %s - %s", prefix, filename, fileModificationDate, bytes );
            logger.log( fileInformation );

            total += size;
        }
        return total;
    }

    private String getFileModificationDate( File file )
    {
        Date modifiedDate = new Date( file.lastModified() );
        return dateFormat.format( modifiedDate );
    }

    private static String getDiskSpace( DatabaseLayout databaseLayout )
    {
        File directory = databaseLayout.databaseDirectory();
        long free = directory.getFreeSpace();
        long total = directory.getTotalSpace();
        long percentage = total != 0 ? (free * 100 / total) : 0;
        return String.format( "Disk space on partition (Total / Free / Free %%): %s / %s / %s", total, free, percentage );
    }

    private static class MappedFileCounter
    {
        private final DatabaseLayout layout;
        private final List<File> mappedCandidates;
        private long size;

        MappedFileCounter( DatabaseLayout layout )
        {
            this.layout = layout;
            this.mappedCandidates = Arrays.stream( StoreType.values() )
                                     .map( StoreType::getDatabaseFile )
                                     .flatMap( layout::file )
                                     .collect( toList() );
        }

        void addFile( File file )
        {
            if ( canBeManagedByPageCache( file ) )
            {
                size += file.length();
            }
        }

        public long getSize()
        {
            return size;
        }

        /**
         * Returns whether or not store file by given file name should be managed by the page cache.
         *
         * @param storeFile file of the store file to check.
         * @return Returns whether or not store file by given file name should be managed by the page cache.
         */
        boolean canBeManagedByPageCache( File storeFile )
        {
            boolean isLabelScanStore = layout.labelScanStore().equals( storeFile );
            return isLabelScanStore || mappedCandidates.contains( storeFile );
        }
    }
}
