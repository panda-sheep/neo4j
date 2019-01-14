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
package org.neo4j.dbms.archive;

import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.isWritable;

public class Utils
{
    private Utils()
    {
    }

    static void checkWritableDirectory( Path directory ) throws FileSystemException
    {
        if ( !exists( directory ) )
        {
            throw new NoSuchFileException( directory.toString() );
        }
        if ( isRegularFile( directory ) )
        {
            throw new FileSystemException( directory.toString() + ": Not a directory" );
        }
        if ( !isWritable( directory ) )
        {
            throw new AccessDeniedException( directory.toString() );
        }
    }
}
