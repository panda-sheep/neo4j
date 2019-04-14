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
package org.neo4j.kernel.impl.core;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.neo4j.dbms.database.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.DatabaseEventHandlerAdapter;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertEquals;
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

public class TestShutdownSequence
{
    private GraphDatabaseService graphDb;
    private DatabaseManagementService managementService;

    @Before
    public void createGraphDb()
    {
        managementService = new TestGraphDatabaseFactory().newImpermanentService();
        graphDb = managementService.database( DEFAULT_DATABASE_NAME );
    }

    @Test
    public void canInvokeShutdownMultipleTimes()
    {
        managementService.shutdown();
        managementService.shutdown();
    }

    @Test
    public void eventHandlersAreOnlyInvokedOnceDuringShutdown()
    {
        final AtomicInteger counter = new AtomicInteger();
        graphDb.registerDatabaseEventHandler( new DatabaseEventHandlerAdapter()
        {
            @Override
            public void beforeShutdown()
            {
                counter.incrementAndGet();
            }
        } );
        managementService.shutdown();
        managementService.shutdown();
        assertEquals( 1, counter.get() );
    }

    @Test
    public void canRemoveFilesAndReinvokeShutdown() throws IOException
    {
        GraphDatabaseAPI databaseAPI = (GraphDatabaseAPI) this.graphDb;
        FileSystemAbstraction fileSystemAbstraction = getDatabaseFileSystem( databaseAPI );
        managementService.shutdown();
        fileSystemAbstraction.deleteRecursively( databaseAPI.databaseLayout().databaseDirectory() );
        managementService.shutdown();
    }

    @Test
    public void canInvokeShutdownFromShutdownHandler()
    {
        graphDb.registerDatabaseEventHandler( new DatabaseEventHandlerAdapter()
        {
            @Override
            public void beforeShutdown()
            {
                managementService.shutdown();
            }
        } );
        managementService.shutdown();
    }

    private static FileSystemAbstraction getDatabaseFileSystem( GraphDatabaseAPI databaseAPI )
    {
        return databaseAPI.getDependencyResolver().resolveDependency( FileSystemAbstraction.class );
    }
}
