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
package org.neo4j.kernel.impl.api.index.sampling;

import org.junit.Before;
import org.junit.Test;

import org.neo4j.internal.kernel.api.exceptions.schema.IndexNotFoundKernelException;
import org.neo4j.kernel.api.index.IndexProviderDescriptor;
import org.neo4j.kernel.api.index.IndexReader;
import org.neo4j.kernel.api.index.IndexSample;
import org.neo4j.kernel.api.index.IndexSampler;
import org.neo4j.kernel.impl.api.index.IndexProxy;
import org.neo4j.kernel.impl.api.index.stats.IndexStatisticsStore;
import org.neo4j.kernel.impl.index.schema.CapableIndexDescriptor;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.NullLogProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.neo4j.internal.kernel.api.InternalIndexState.FAILED;
import static org.neo4j.internal.kernel.api.InternalIndexState.ONLINE;
import static org.neo4j.kernel.api.schema.SchemaDescriptorFactory.forLabel;
import static org.neo4j.kernel.impl.index.schema.IndexDescriptorFactory.forSchema;

public class OnlineIndexSamplingJobTest
{
    @Test
    public void shouldSampleTheIndexAndStoreTheValueWhenTheIndexIsOnline()
    {
        // given
        OnlineIndexSamplingJob job = new OnlineIndexSamplingJob( indexId, indexProxy, indexStatisticsStore, "Foo", logProvider );
        when( indexProxy.getState() ).thenReturn( ONLINE );

        // when
        job.run();

        // then
        verify( indexStatisticsStore ).replaceIndexCounts( indexId, indexUniqueValues, indexSize, indexSize );
        verifyNoMoreInteractions( indexStatisticsStore );
    }

    @Test
    public void shouldSampleTheIndexButDoNotStoreTheValuesIfTheIndexIsNotOnline()
    {
        // given
        OnlineIndexSamplingJob job = new OnlineIndexSamplingJob( indexId, indexProxy, indexStatisticsStore, "Foo", logProvider );
        when( indexProxy.getState() ).thenReturn( FAILED );

        // when
        job.run();

        // then
        verifyNoMoreInteractions( indexStatisticsStore );
    }

    private final LogProvider logProvider = NullLogProvider.getInstance();
    private final long indexId = 1;
    private final IndexProxy indexProxy = mock( IndexProxy.class );
    private final IndexStatisticsStore indexStatisticsStore = mock( IndexStatisticsStore.class );
    private final CapableIndexDescriptor indexDescriptor =
            forSchema( forLabel( 1, 2 ), IndexProviderDescriptor.UNDECIDED ).withId( indexId ).withoutCapabilities();
    private final IndexReader indexReader = mock( IndexReader.class );
    private final IndexSampler indexSampler = mock( IndexSampler.class );

    private final long indexUniqueValues = 21L;
    private final long indexSize = 23L;

    @Before
    public void setup() throws IndexNotFoundKernelException
    {
        when( indexProxy.getDescriptor() ).thenReturn( indexDescriptor );
        when( indexProxy.newReader() ).thenReturn( indexReader );
        when( indexReader.createSampler() ).thenReturn( indexSampler );
        when( indexSampler.sampleIndex() ).thenReturn( new IndexSample( indexSize, indexUniqueValues, indexSize ) );
    }
}
