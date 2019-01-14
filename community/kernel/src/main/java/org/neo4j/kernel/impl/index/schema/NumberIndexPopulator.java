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
package org.neo4j.kernel.impl.index.schema;

import java.io.File;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.api.index.IndexProvider;
import org.neo4j.storageengine.api.StorageIndexReference;

import static org.neo4j.index.internal.gbptree.GBPTree.NO_HEADER_WRITER;

class NumberIndexPopulator extends NativeIndexPopulator<NumberIndexKey,NativeIndexValue>
{
    NumberIndexPopulator( PageCache pageCache, FileSystemAbstraction fs, File storeFile, IndexLayout<NumberIndexKey,NativeIndexValue> layout,
            IndexProvider.Monitor monitor, StorageIndexReference descriptor )
    {
        super( pageCache, fs, storeFile, layout, monitor, descriptor, NO_HEADER_WRITER );
    }

    @Override
    NativeIndexReader<NumberIndexKey, NativeIndexValue> newReader()
    {
        return new NumberIndexReader<>( tree, layout, descriptor );
    }
}
