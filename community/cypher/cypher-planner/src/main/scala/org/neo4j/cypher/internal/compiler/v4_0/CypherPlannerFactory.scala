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
package org.neo4j.cypher.internal.compiler.v4_0

import java.time.Clock

import org.neo4j.cypher.internal.compiler.v4_0.phases.{PlannerContext, LogicalPlanState}
import org.neo4j.cypher.internal.compiler.v4_0.planner.logical._
import org.neo4j.cypher.internal.v4_0.frontend.phases.{ASTRewriter, Monitors, Transformer}
import org.neo4j.cypher.internal.v4_0.rewriting.RewriterStepSequencer
import org.neo4j.cypher.internal.v4_0.rewriting.rewriters.IfNoParameter

class CypherPlannerFactory[C <: PlannerContext, T <: Transformer[C, LogicalPlanState, LogicalPlanState]] {
  val monitorTag = "cypher4.0"

  def costBasedCompiler(config: CypherPlannerConfiguration,
                        clock: Clock,
                        monitors: Monitors,
                        rewriterSequencer: String => RewriterStepSequencer,
                        updateStrategy: Option[UpdateStrategy],
                        contextCreator: ContextCreator[C]): CypherPlanner[C] = {
    val metricsFactory = CachedMetricsFactory(SimpleMetricsFactory)
    val actualUpdateStrategy: UpdateStrategy = updateStrategy.getOrElse(defaultUpdateStrategy)
    CypherPlanner(monitors, rewriterSequencer,
      metricsFactory, config, actualUpdateStrategy, clock, contextCreator)
  }
}
