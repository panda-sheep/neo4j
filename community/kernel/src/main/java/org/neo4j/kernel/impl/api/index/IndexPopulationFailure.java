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
package org.neo4j.kernel.impl.api.index;

import org.neo4j.helpers.Exceptions;
import org.neo4j.kernel.api.exceptions.index.IndexPopulationFailedKernelException;
import org.neo4j.storageengine.api.schema.SchemaDescriptor;

public abstract class IndexPopulationFailure
{
    public abstract String asString();

    public abstract IndexPopulationFailedKernelException asIndexPopulationFailure(
            SchemaDescriptor descriptor, String indexUserDescriptor );

    public static IndexPopulationFailure failure( final Throwable failure )
    {
        return new IndexPopulationFailure()
        {
            @Override
            public String asString()
            {
                return Exceptions.stringify( failure );
            }

            @Override
            public IndexPopulationFailedKernelException asIndexPopulationFailure(
                    SchemaDescriptor descriptor, String indexUserDescription )
            {
                return new IndexPopulationFailedKernelException( indexUserDescription, failure );
            }
        };
    }

    public static IndexPopulationFailure failure( final String failure )
    {
        return new IndexPopulationFailure()
        {
            @Override
            public String asString()
            {
                return failure;
            }

            @Override
            public IndexPopulationFailedKernelException asIndexPopulationFailure(
                    SchemaDescriptor descriptor, String indexUserDescription )
            {
                return new IndexPopulationFailedKernelException( indexUserDescription, failure );
            }
        };
    }

    public static String appendCauseOfFailure( String message, String causeOfFailure )
    {
        return String.format( "%s: Cause of failure:%n" +
                "==================%n%s%n==================", message, causeOfFailure );
    }
}
